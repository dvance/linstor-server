package com.linbit.linstor.netcom;

import com.linbit.InvalidNameException;
import com.linbit.ServiceName;
import com.linbit.linstor.Node;
import com.linbit.linstor.satellitestate.SatelliteState;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;

import javax.net.ssl.SSLException;
import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class PeerREST implements Peer
{
    private final String peerId;
    static ServiceName serviceName;

    public PeerREST(
        String peerIdRef
    )
    {
        peerId = peerIdRef;

        try
        {
            serviceName = new ServiceName("PeerREST");
        }
        catch (InvalidNameException ignored)
        {
        }
    }

    @Override
    public String getId()
    {
        return peerId;
    }

    @Override
    public Node getNode()
    {
        return null;
    }

    @Override
    public ServiceName getConnectorInstanceName()
    {
        return serviceName;
    }

    @Override
    public AccessContext getAccessContext()
    {
        return null;
    }

    @Override
    public void setAccessContext(AccessContext privilegedCtx, AccessContext newAccCtx) throws AccessDeniedException
    {
    }

    @Override
    public void attach(Object attachment)
    {
    }

    @Override
    public Object getAttachment()
    {
        return null;
    }

    @Override
    public Message createMessage()
    {
        return null;
    }

    @Override
    public boolean sendMessage(Message msg) throws IllegalMessageStateException
    {
        return false;
    }

    @Override
    public boolean sendMessage(byte[] data)
    {
        return false;
    }

    @Override
    public long getNextIncomingMessageSeq()
    {
        return 0;
    }

    @Override
    public void processInOrder(long peerSeq, Publisher<?> publisher)
    {
    }

    @Override
    public Flux<ByteArrayInputStream> apiCall(String apiCallName, byte[] data)
    {
        return Flux.empty();
    }

    @Override
    public void apiCallAnswer(long apiCallId, ByteArrayInputStream data)
    {
    }

    @Override
    public void apiCallError(long apiCallId, Throwable exc)
    {
    }

    @Override
    public void apiCallComplete(long apiCallId)
    {
    }

    @Override
    public void closeConnection()
    {
    }

    @Override
    public void closeConnection(boolean allowReconnect)
    {
    }

    @Override
    public void connectionClosing()
    {
    }

    @Override
    public boolean isConnected()
    {
        return false;
    }

    @Override
    public ConnectionStatus getConnectionStatus()
    {
        return ConnectionStatus.UNKNOWN;
    }

    @Override
    public void setConnectionStatus(ConnectionStatus status)
    {
    }

    @Override
    public boolean isConnected(boolean ensureAuthenticated)
    {
        return false;
    }

    @Override
    public boolean isAuthenticated()
    {
        return false;
    }

    @Override
    public void setAuthenticated(boolean authenticated)
    {
    }

    @Override
    public int outQueueCapacity()
    {
        return 0;
    }

    @Override
    public int outQueueCount()
    {
        return 0;
    }

    @Override
    public long msgSentCount()
    {
        return 0;
    }

    @Override
    public long msgRecvCount()
    {
        return 0;
    }

    @Override
    public long msgSentMaxSize()
    {
        return 0;
    }

    @Override
    public long msgRecvMaxSize()
    {
        return 0;
    }

    @Override
    public InetSocketAddress peerAddress()
    {
        return null;
    }

    @Override
    public InetSocketAddress localAddress()
    {
        return null;
    }

    @Override
    public void connectionEstablished() throws SSLException
    {
    }

    @Override
    public void waitUntilConnectionEstablished() throws InterruptedException
    {
    }

    @Override
    public TcpConnector getConnector()
    {
        return null;
    }

    @Override
    public void sendPing()
    {
    }

    @Override
    public void sendPong()
    {
    }

    @Override
    public void pongReceived()
    {
    }

    @Override
    public long getLastPingSent()
    {
        return 0;
    }

    @Override
    public long getLastPongReceived()
    {
        return 0;
    }

    @Override
    public ReadWriteLock getSatelliteStateLock()
    {
        return null;
    }

    @Override
    public SatelliteState getSatelliteState()
    {
        return null;
    }

    @Override
    public ReadWriteLock getSerializerLock()
    {
        return null;
    }

    @Override
    public void setFullSyncId(long timestamp)
    {
    }

    @Override
    public long getFullSyncId()
    {
        return 0;
    }

    @Override
    public long getNextSerializerId()
    {
        return 0;
    }

    @Override
    public void fullSyncFailed()
    {
    }

    @Override
    public boolean hasFullSyncFailed()
    {
        return true;
    }

    @Override
    public boolean hasNextMsgIn()
    {
        return false;
    }

    @Override
    public Message nextCurrentMsgIn()
    {
        return null;
    }


    @Override
    public String toString()
    {
        return "RestClient(" + getId() + ")";
    }
}
