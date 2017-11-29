package com.linbit.linstor.core;

import com.linbit.ImplementationError;
import com.linbit.InvalidNameException;
import com.linbit.ServiceName;
import com.linbit.SystemService;
import com.linbit.SystemServiceStartException;
import com.linbit.linstor.NodeName;
import com.linbit.linstor.Resource;
import com.linbit.linstor.ResourceDefinition;
import com.linbit.linstor.ResourceName;
import com.linbit.linstor.SatelliteCoreServices;
import com.linbit.linstor.StorPoolName;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.security.Privilege;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.event.Level;

class DeviceManagerImpl implements Runnable, SystemService, DeviceManager
{
    private final Object sched = new Object();

    private final Satellite stltInstance;
    private final AccessContext wrkCtx;
    private final SatelliteCoreServices coreSvcs;

    private StltUpdateTrackerImpl updTracker;
    private StltUpdateTrackerImpl.UpdateBundle rcvPendingBundle;

    private Thread svcThr;

    private final AtomicBoolean runningFlag = new AtomicBoolean(false);
    private final AtomicBoolean shutdownFlag = new AtomicBoolean(false);

    private static final ServiceName devMgrName;
    static
    {
        try
        {
            devMgrName = new ServiceName("DeviceManager");
        }
        catch (InvalidNameException invName)
        {
            throw new ImplementationError(
                "The built-in name of the DeviceManager service is invalid",
                invName
            );
        }
    }
    public static final String SVC_INFO = "Manages storage, transport and replication resources";
    private ServiceName devMgrInstName;

    private DeviceHandler drbdHnd;

    DeviceManagerImpl(Satellite stltRef, AccessContext wrkCtxRef, SatelliteCoreServices coreSvcsRef)
    {
        stltInstance = stltRef;
        wrkCtx = wrkCtxRef;
        coreSvcs = coreSvcsRef;
        updTracker = new StltUpdateTrackerImpl(sched);
        rcvPendingBundle = new StltUpdateTrackerImpl.UpdateBundle();
        svcThr = null;
        devMgrInstName = devMgrName;
        drbdHnd = new DrbdDeviceHandler(coreSvcs);
    }

    /**
     * Dispatch resource to a specific handler depending on type
     */
    void dispatchResource(AccessContext wrkCtx, Resource rsc)
    {
        // Select the resource handler for the resource depeding on resource type
        // Currently, the DRBD resource handler is used for all resources
        DeviceHandlerInvocation devHndInv = new DeviceHandlerInvocation(drbdHnd, rsc);

        // TODO: Schedule the invocation on a worker pool
        devHndInv.run();
    }

    @Override
    public void setServiceInstanceName(ServiceName instanceName)
    {
        if (instanceName != null)
        {
            devMgrInstName = instanceName;
        }
    }

    @Override
    public void start() throws SystemServiceStartException
    {
        if (runningFlag.compareAndSet(false, true))
        {
            svcThr = new Thread(this);
            svcThr.start();
        }
    }

    @Override
    public void shutdown()
    {
        synchronized (sched)
        {
            shutdownFlag.set(true);
            sched.notify();
        }
    }

    @Override
    public void awaitShutdown(long timeout) throws InterruptedException
    {
        // Since svcThr may be set to null at any time when a currently running,
        // thread exits, copy the reference to avoid the race condition
        Thread waitThr = svcThr;
        if (waitThr != null)
        {
            waitThr.join(timeout);
        }
    }

    @Override
    public void nodeUpdateApplied(Set<NodeName> nodeSet)
    {
        synchronized (sched)
        {
            rcvPendingBundle.updNodeSet.removeAll(nodeSet);
            if (rcvPendingBundle.isEmpty())
            {
                sched.notify();
            }
        }
    }

    @Override
    public void rscDefUpdateApplied(Set<ResourceName> rscDfnSet)
    {
        synchronized (sched)
        {
            rcvPendingBundle.updRscDfnSet.removeAll(rscDfnSet);
            if (rcvPendingBundle.isEmpty())
            {
                sched.notify();
            }
        }
    }

    @Override
    public void storPoolUpdateApplied(Set<StorPoolName> storPoolSet)
    {
        synchronized (sched)
        {
            rcvPendingBundle.updStorPoolSet.removeAll(storPoolSet);
            if (rcvPendingBundle.isEmpty())
            {
                sched.notify();
            }
        }
    }

    @Override
    public void rscUpdateApplied(Map<ResourceName, Set<NodeName>> rscMap)
    {
        synchronized (sched)
        {
            rscUpdateAppliedImpl(rscMap);
            if (rcvPendingBundle.isEmpty())
            {
                sched.notify();
            }
        }
    }

    @Override
    public void updateApplied(
        Set<NodeName> nodeSet,
        Set<ResourceName> rscDfnSet,
        Set<StorPoolName> storPoolSet,
        Map<ResourceName, Set<NodeName>> rscMap
    )
    {
        synchronized (sched)
        {
            if (nodeSet != null)
            {
                rcvPendingBundle.updNodeSet.removeAll(nodeSet);
            }
            if (rscDfnSet != null)
            {
                rcvPendingBundle.updRscDfnSet.removeAll(rscDfnSet);
            }
            if (storPoolSet != null)
            {
                rcvPendingBundle.updStorPoolSet.removeAll(storPoolSet);
            }
            if (rscMap != null)
            {
                rscUpdateAppliedImpl(rscMap);
            }

            if (rcvPendingBundle.isEmpty())
            {
                sched.notify();
            }
        }
    }

    @Override
    public StltUpdateTracker getUpdateTracker()
    {
        return updTracker;
    }

    // Caller must hold the scheduler lock ('synchronized (sched)')
    private void rscUpdateAppliedImpl(Map<ResourceName, Set<NodeName>> rscMap)
    {
        for (Map.Entry<ResourceName, Set<NodeName>> entry : rscMap.entrySet())
        {
            ResourceName rscName = entry.getKey();
            Set<NodeName> pendNodeSet = rcvPendingBundle.updRscMap.get(rscName);
            if (pendNodeSet != null)
            {
                Set<NodeName> updNodeSet = entry.getValue();
                pendNodeSet.removeAll(updNodeSet);
                if (pendNodeSet.isEmpty())
                {
                    rcvPendingBundle.updRscMap.remove(rscName);
                }
            }
        }
    }

    @Override
    public void run()
    {
        StltUpdateTrackerImpl.UpdateBundle chgPendingBundle = new StltUpdateTrackerImpl.UpdateBundle();
        try
        {
            wrkCtx.getEffectivePrivs().enablePrivileges(Privilege.PRIV_OBJ_USE, Privilege.PRIV_MAC_OVRD);

            // TODO: Initial startup of all devices

            // TODO: Initial changes to reach the target state

            do
            {
                // Wait until resource updates are pending
                synchronized (sched)
                {
                    while (!shutdownFlag.get())
                    {
                        updTracker.collectUpdateNotifications(chgPendingBundle);
                    }
                    if (shutdownFlag.get())
                    {
                        break;
                    }

                    chgPendingBundle.copyUpdateRequestsTo(rcvPendingBundle);
                    // Request updates from the controller

                    // Wait for the notification that all requested updates
                    // have been received and applied
                    while (!shutdownFlag.get() && !rcvPendingBundle.isEmpty())
                    {
                        try
                        {
                            sched.wait();
                        }
                        catch (InterruptedException ignored)
                        {
                        }
                    }
                    if (shutdownFlag.get())
                    {
                        break;
                    }

                    // Merge check requests into update requests and clear the check requests
                    chgPendingBundle.updRscDfnSet.addAll(chgPendingBundle.chkRscSet);
                    chgPendingBundle.chkRscSet.clear();
                }

                for (ResourceName rscName : chgPendingBundle.updRscDfnSet)
                {
                    // Dispatch resources that were affected by changes to worker threads
                    // and to the resource's respective handler
                    ResourceDefinition rscDfn = stltInstance.rscDfnMap.get(rscName);
                    if (rscDfn != null)
                    {
                        Resource rsc = rscDfn.getResource(wrkCtx, stltInstance.localNode.getName());
                        dispatchResource(wrkCtx, rsc);
                    }
                }

                // TODO: Wait for the worker threads to finish
            }
            while (!shutdownFlag.get());
        }
        catch (AccessDeniedException accExc)
        {
            shutdownFlag.set(true);
            coreSvcs.getErrorReporter().reportError(
                Level.ERROR,
                new ImplementationError(
                    "The DeviceManager was started with an access context that does not have sufficient " +
                    "privileges to access all required information",
                    accExc
                )
            );
        }
        finally
        {
            runningFlag.set(false);
        }
    }

    @Override
    public ServiceName getServiceName()
    {
        return devMgrName;
    }

    @Override
    public String getServiceInfo()
    {
        return SVC_INFO;
    }

    @Override
    public ServiceName getInstanceName()
    {
        return devMgrInstName;
    }

    @Override
    public boolean isStarted()
    {
        return runningFlag.get();
    }

    static class DeviceHandlerInvocation implements Runnable
    {
        private final DeviceHandler handler;
        private final Resource rsc;

        DeviceHandlerInvocation(DeviceHandler handlerRef, Resource rscRef)
        {
            handler = handlerRef;
            rsc = rscRef;
        }

        @Override
        public void run()
        {
            handler.dispatchResource(rsc);
        }
    }
}