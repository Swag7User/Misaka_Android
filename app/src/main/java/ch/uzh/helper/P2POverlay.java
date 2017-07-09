package ch.uzh.helper;

import ch.uzh.MainWindow;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;
import android.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * Basically a wrapper for TomP2P for the needs of this application.
 * @author Sebastian
 */
public class P2POverlay {

    private static final Logger log = LoggerFactory.getLogger(P2POverlay.class);


    private Peer peer;
    private PeerDHT peerDHT;
    private static Random rnd = new Random();

    public PeerAddress getPeerAddress() {
        return peer.peerAddress();
    }

    public boolean putNonBlocking(String key, Object value) {
        Data data;
        try {
            data = new Data(value);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }

        FuturePut futurePut = peerDHT.put(Number160.createHash(key)).data(data).start();

        futurePut.addListener(new BaseFutureAdapter<FuturePut>() {
            @Override
            public void operationComplete(FuturePut future) throws Exception {
                if(future.isSuccess()) { // this flag indicates if the future was successful
                    log.info("success");
                    MainWindow.futurputSuccess = true;
                } else {
                    log.info("failure");
                }
            }
        });



        return true;
    }

    public Object getBlocking(String key) {
        FutureGet futureGet = peerDHT.get(Number160.createHash(key)).start().awaitUninterruptibly();

        if (futureGet.isSuccess()) {
            try {
                return futureGet.data().object();
            } catch (Exception ex) {
                log.info("MY ARCH NEMESIS");
                ex.printStackTrace();
                return null;
            }
        } else {
            log.info("MY ARCH NEMESIS TOO TBH");
            return null;
        }
    }

    public boolean sendBlocking(PeerAddress recipient, Object o) {
        FutureDirect futureDirect = peer.sendDirect(recipient)
                .object(o).start().awaitUninterruptibly();

        return futureDirect.isSuccess();
    }

    public void sendNonBlocking(PeerAddress recipient, Object o, boolean forceUDP) {
        FutureDirect futureDirect = peer.sendDirect(recipient)
                .object(o).forceTCP(forceUDP).start();
    }

    public void getNonBLocking(String key, BaseFutureAdapter<FutureGet> baseFutureAdapter) {
        FutureGet futureGet = peerDHT.get(Number160.createHash(key)).start();
        futureGet.addListener(baseFutureAdapter);
    }

    public Pair<Boolean, String> bootstrap(String bootstrapIP) {
        int port = 4001;

        // Create TomP2P peer
        boolean peerCreated = false;
        do {
            try {
                // Create new peer
                peer = new PeerBuilder(new Number160(rnd)).ports(port++).start();
                peerDHT = new PeerBuilderDHT(peer).start();
                peerCreated = true;
            } catch (IOException ex) {
                log.info("Port already in use. " + ex.getMessage());
            }
        } while (!peerCreated && port < 4010);

        if (!peerCreated) {
            return new Pair<>(false, "Could not find any unused port");
        }

        try {
            peer.discover().inetAddress(InetAddress.getByName(bootstrapIP)).ports(4001).start().awaitUninterruptibly();
            FutureBootstrap futureBootstrap = peer.bootstrap().inetAddress(InetAddress.getByName(bootstrapIP)).ports(4001).start();
            futureBootstrap.awaitUninterruptibly();
            if (futureBootstrap.isSuccess()) {
                log.info("Bootstrap successful");
                return new Pair<>(true, "Bootstrap successful");
            } else {
                log.info("Could not bootstrap to well known peer");
                return new Pair<>(false, "Could not bootstrap to well known peer");
            }
        } catch (UnknownHostException ex) {
            log.info("Unknown bootstrap host. (UnknownHostException)");
            return new Pair<>(false, "Unknown bootstrap host. (UnknownHostException)");
        }

    }

    public void setObjectDataReply(ObjectDataReply replyHandler) {
        peer.objectDataReply(replyHandler);
    }

    public void shutdown() {
        if (peer != null) {
            BaseFuture shutdownBuilder = peer.shutdown();
            shutdownBuilder.awaitUninterruptibly();
            peer = null;
        }
    }
}
