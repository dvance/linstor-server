package com.linbit.drbdmanage;

import com.linbit.ServiceName;
import com.linbit.SystemService;
import com.linbit.drbdmanage.core.DrbdManage;
import com.linbit.drbdmanage.netcom.Peer;
import com.linbit.drbdmanage.propscon.Props;
import com.linbit.drbdmanage.security.AccessContext;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Common debug methods of the Controller and Satellite modules
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public interface CommonDebugControl
{
    DrbdManage getInstance();
    String getProgramName();
    String getModuleType();
    String getVersion();
    Map<ServiceName, SystemService> getSystemServiceMap();
    Peer getPeer(String peerId);
    Map<String, Peer> getAllPeers();
    Set<String> getApiCallNames();
    Map<NodeName, Node> getNodesMap();
    Map<ResourceName, ResourceDefinition> getRscDfnMap();
    Props getConf();
    ReadWriteLock getConfLock();
    void shutdown(AccessContext accCtx);
}
