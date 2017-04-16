/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moe.ywp.misaka.network;


import moe.ywp.misaka.LoginActivity;
import moe.ywp.misaka.MainActivity;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;


/**
 *
 * @author Sebastian
 */
public class ObjectReplyHandler implements ObjectDataReply {

    private LoginActivity mainWindow;

    public ObjectReplyHandler(LoginActivity _mainWindow) {
        mainWindow = _mainWindow;
    }

    @Override
    public Object reply(PeerAddress pa, Object o) throws Exception {
        System.err.println("ObjectReplyhandler");
        if (o instanceof FriendRequestMessage) {
 /*           Runnable task = () -> {
                mainWindow.handleIncomingFriendRequest((FriendRequestMessage) o);
            };
            Platform.runLater(task);*/
            System.err.println("instanceof FriendRequestMessage");
        }/* else if (o instanceof OnlineStatusMessage) {
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
        return null;
    }
}
