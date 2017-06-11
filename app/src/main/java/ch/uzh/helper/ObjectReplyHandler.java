/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.uzh.helper;


import ch.uzh.MainWindow;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;


/**
 *
 * @author Sebastian
 */
public class ObjectReplyHandler implements ObjectDataReply {

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
        System.err.println("ObjectReplyhandler");
        System.err.println(jsonReply);
        String identifier;
        try {
            identifier = parse(jsonReply);
            System.err.println("identifier is: " + identifier);
        } catch (Exception e) {
            System.err.println("Nope, didn't work to parse this json");
            e.printStackTrace();
            identifier = "shit happens";
        }

        if (identifier.equals("FriendRequestMessage")) {
            System.err.println("~~~~~~~~~~~~~~~FriendRequest message incomming~~~~~~~~~~~~~");
            final Runnable r = new Runnable() {
                public void run() {
                    System.err.println("~~~~~~~~~~~~~~~FriendRequest message handling~~~~~~~~~~~~~");
                    mainWindow.handleIncomingFriendRequest(gsonReply.fromJson(jsonReply, FriendRequestMessage.class));
                    System.err.println("~~~~~~~~~~~~~~~FriendRequest message handled~~~~~~~~~~~~~");
                }
            };            r.run();
        } else if (identifier.equals("shit happens")) {
            System.err.println("~~~~~~~~~~~~~~~error handling~~~~~~~~~~~~~");
            final Runnable r = new Runnable() {
                public void run() {
                    mainWindow.donothing();
                }
            };            r.run();
        } else if (identifier.equals("ChatMessage")) {
            System.err.println("~~~~~~~~~~~~~~~ChatMessage~~~~~~~~~~~~~");
            final Runnable r = new Runnable() {
                public void run() {
                    ChatMessage msg = gsonReply.fromJson(jsonReply, ChatMessage.class);
                    mainWindow.handleIncomingChatMessage(msg);
                }
            };            r.run();
        } else if (identifier.equals("OnlineStatusMessage")) {
            System.err.println("~~~~~~~~~~~~~~~OnlineStatusMessage~~~~~~~~~~~~~");
            final Runnable r = new Runnable() {
                public void run() {
                    mainWindow.handleIncomingOnlineStatus(gsonReply.fromJson(jsonReply, OnlineStatusMessage.class));
                }
            };            r.run();

        } else if (identifier.equals("AudioFrame")) {
            System.err.println("~~~~~~~~~~~~~~~AudioFrame~~~~~~~~~~~~~");
            final Runnable r = new Runnable() {
                public void run() {
                    mainWindow.handleIncomingAudioFrame(gsonReply.fromJson(jsonReply, AudioFrame.class));
                }
            };            r.run();

        } else {
            System.err.println("~~~~~~~~~~~~~~~all has failed~~~~~~~~~~~~~");

        }



        /* else if (o instanceof OnlineStatusMessage) {
            Runnable task = () -> {
                mainWindow.handleIncomingOnlineStatus((OnlineStatusMessage) o);
            };
            Platform.runLater(task);
        } else if (o instanceof ChatMessage) {
            Runnable task = () -> {
                ChatMessage msg = (ChatMessage) o;
                mainWindow.handleIncomingChatMessage(msg);
            };
            Platform.runLater(task);
        } else if (o instanceof CallRequestMessage) {
            Runnable task = () -> {
                CallRequestMessage msg = (CallRequestMessage) o;
                mainWindow.handleIncomingCallRequestMessage(msg);
            };
            Platform.runLater(task);
        } else if (o instanceof CallAcceptMessage) {
            Runnable task = () -> {
                CallAcceptMessage msg = (CallAcceptMessage) o;
                mainWindow.handleIncomingCallAcceptMessage(msg);
            };
            Platform.runLater(task);
        } else if (o instanceof AudioFrame) {
            AudioFrame msg = (AudioFrame)o;
            mainWindow.handleIncomingAudioFrame(msg);
        }*/
        System.err.println(" ~~~~~~~~~~~~~~~end of objectreplyhandler~~~~~~~~~~~~~ ");

        return null;
    }
}
