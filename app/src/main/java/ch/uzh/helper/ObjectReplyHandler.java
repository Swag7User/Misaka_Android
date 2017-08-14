package ch.uzh.helper;


import ch.uzh.MainWindow;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectReplyHandler implements ObjectDataReply {

    private static final Logger log = LoggerFactory.getLogger(ObjectReplyHandler.class);

    private MainWindow mainWindow;

    public ObjectReplyHandler(MainWindow _mainWindow) {
        mainWindow = _mainWindow;
    }

    public String parse(String jsonLine) {
        JsonElement jelement = new JsonParser().parse(jsonLine);
        JsonObject jobject = jelement.getAsJsonObject();
        String result = jobject.get("identifier").toString();
        result = result.replace("\"", "");
        return result;
    }

    @Override
    public Object reply(PeerAddress pa, final Object o) throws Exception {
        Gson gsonReply = new Gson();
        String jsonReply = (String) o;
        log.info("ObjectReplyhandler");
        log.info(jsonReply);
        String identifier;
        try {
            identifier = parse(jsonReply);
            log.info("identifier is: " + identifier);
        } catch (Exception e) {
            log.info("Nope, didn't work to parse this json");
            e.printStackTrace();
            identifier = "shit happens";
        }

        if (identifier.equals("FriendRequestMessage")) {
            final Runnable r = new Runnable() {
                public void run() {
                    log.info("~~~~~~~~~~~~~~~FriendRequest message handling~~~~~~~~~~~~~");
                    mainWindow.handleIncomingFriendRequest(gsonReply.fromJson(jsonReply, FriendRequestMessage.class));
                }
            };            r.run();
        } else if (identifier.equals("shit happens")) {
            log.info("~~~~~~~~~~~~~~~error handling~~~~~~~~~~~~~");
            final Runnable r = new Runnable() {
                public void run() {
                    mainWindow.donothing();
                }
            };            r.run();
        } else if (identifier.equals("ChatMessage")) {
            log.info("~~~~~~~~~~~~~~~Chat message handling~~~~~~~~~~~~~");
            final Runnable r = new Runnable() {
                public void run() {
                    ChatMessage msg = gsonReply.fromJson(jsonReply, ChatMessage.class);
                    mainWindow.handleIncomingChatMessage(msg);
                }
            };            r.run();
        } else if (identifier.equals("OnlineStatusMessage")) {
            log.info("~~~~~~~~~~~~~~~online status handling~~~~~~~~~~~~~");
            final Runnable r = new Runnable() {
                public void run() {
                    mainWindow.handleIncomingOnlineStatus(gsonReply.fromJson(jsonReply, OnlineStatusMessage.class));
                }
            };            r.run();

        } else if (identifier.equals("AudioFrame")) {
            log.info("~~~~~~~~~~~~~~~audioframe handling~~~~~~~~~~~~~");
            final Runnable r = new Runnable() {
                public void run() {
                    mainWindow.handleIncomingAudioFrame(gsonReply.fromJson(jsonReply, AudioFrame.class));
                }
            };            r.run();

        } else if (identifier.equals("VideoFrame")) {
            log.info("~~~~~~~~~~~~~~~videoframe handling~~~~~~~~~~~~~");
            final Runnable r = new Runnable() {
                public void run() {
                    mainWindow.handleIncomingVideoFrame(gsonReply.fromJson(jsonReply, VideoFrame.class));
                }
            };            r.run();

        } else {
            log.info("~~~~~~~~~~~~~~~all has failed~~~~~~~~~~~~~");

        }

        log.info(" ~~~~~~~~~~~~~~~end of objectreplyhandler~~~~~~~~~~~~~ ");

        return null;
    }
}
