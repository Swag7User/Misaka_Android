package ch.uzh;

import android.util.Pair;
import ch.uzh.helper.FriendsListEntry;
import ch.uzh.helper.PrivateUserProfile;
import ch.uzh.helper.PublicUserProfile;
import ch.uzh.helper.ChatMessage;
import ch.uzh.helper.OnlineStatusMessage;
import ch.uzh.helper.FriendRequestMessage;
import ch.uzh.helper.ObjectReplyHandler;
import ch.uzh.helper.P2POverlay;
import com.google.gson.Gson;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.peers.PeerAddress;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by Jesus on 19.04.2017.
 */
public class MainWindow {
    private int id;
    private String ip;
    private String username;
    private String password;
    private boolean bootstrapNode;


    private PrivateUserProfile userProfile;

    public P2POverlay p2p;
    private List<FriendsListEntry> friendsList;
    public Queue<ChatMessage> messageQueue;
    private List<FriendRequestMessage> friendRequestsList;
    private ScheduledExecutorService scheduler;

    private String currentChatPartner;


    public String getCurrentChatpartner(){
        return currentChatPartner;
    }

    public void setCurrentChatpartner(String userID){
        currentChatPartner = userID;
    }

    public MainWindow( P2POverlay p2p){
        this.p2p = p2p;
        messageQueue = new LinkedList();
    }


    public void acceptFriendRequest(FriendRequestMessage message) {
        // Add user
        addFriend(message.getSenderUserID());

        // Remove friend request
        friendRequestsList.remove(message);

        // Save User Profile
        savePrivateUserProfile();
        System.err.println("userid: " + message.getSenderUserID() + "messagetxt: " +  message.getMessageText() + "peeraddress: " + message.getSenderPeerAddress());
        FriendsListEntry newFriend = new FriendsListEntry(message.getSenderUserID());
        newFriend.setPeerAddress(message.getSenderPeerAddress());
        //friendListController.updateFriends(); TODO: update to android friendlist
       /* Pair<Boolean, String> result = sendFriendRequest(message.getSenderUserID(), "hi, pls accept 2");

        if (result.getKey() == true) {
            System.err.println("response friend request sent");
        } else {
            System.err.println("response friend request ERROR");
        }*/
    }

    private boolean savePrivateUserProfile() {
        Gson gson = new Gson();
        String json = gson.toJson(userProfile);
        System.err.println("IMMA GONNA PRINT MY JSON");
        System.err.println(json);
        System.err.println("PRINTED MY JSON");
        System.err.println("errors? " + userProfile.getUserID() + " - " + userProfile.getPassword());

        return p2p.put(userProfile.getUserID() + userProfile.getPassword(), json);
    }

    public void declineFriendRequest(FriendRequestMessage message) {
        // Remove friend request
        friendRequestsList.remove(message);

        // Save User Profile
        savePrivateUserProfile();
    }

    public void handleIncomingFriendRequest(FriendRequestMessage requestMessage) {
        // Ignore requests from users already in the list
        if (userProfile.isFriendsWith(requestMessage.getSenderUserID())) {
            System.err.println("user already in friendlist");
            return;
        }

        // Ignore multiple requests
/*        if (userProfile.hasFriendRequestFromUser(requestMessage.getSenderUserID())) {
            return;
        }*/

        // Add friend request
        friendRequestsList.add(requestMessage);

        // Save the change
        System.err.println("Am I here? lul");
        boolean isSaved = savePrivateUserProfile();
        if (isSaved == true){
            System.err.println("saved succesfully");
        } else{
            System.err.println("saved UNsuccesfully");

        }
        System.err.println("why am i not here tough");

        // Show visual message
        int i = 0;
        int i2 = 0;
        System.err.println("friendlistsize: " + friendsList.size());
        for(FriendsListEntry e : friendsList){
            System.err.println("i: " + i++);
            System.err.println(e.toString());
        }
        System.err.println(" BEFORE, AFTER");
        //FriendListController.showIncomingFriendRequest(requestMessage);
        acceptFriendRequest(requestMessage);
        System.err.println("friendlistsize: " + friendsList.size());
        for(FriendsListEntry e : friendsList){
            System.err.println("i2: " + i2++);
            System.err.println("friendlistitem: " + e.getUserID());
        }

    }

    public boolean existsUser(String userID) {
        return (p2p.getBlocking(userID) != null);
    }


    public Pair<Boolean, String> sendFriendRequest(String userID, String messageText) {
        // Check if user already exists in friends list
        if (getFriendsListEntry(userID) != null) {
            System.err.println("User already in friendslist");
            return new Pair<>(false, "User already in friendslist");
        }

        // Check if user exists in the network
        if (!existsUser(userID)) {
            System.err.println("User was not found");
            return new Pair<>(false, "User was not found");
        }

        // Get public profile of friend we want to add
        String jsonFriendProfile = (String) p2p.getBlocking(userID);
        Gson gson = new Gson();
        PublicUserProfile friendProfile = gson.fromJson(jsonFriendProfile, PublicUserProfile.class);
        // Create friend request message
        FriendRequestMessage friendRequestMessage = new FriendRequestMessage("FriendRequestMessage", p2p.getPeerAddress(), userProfile.getUserID(), messageText);

        Gson gsonFriendRequest = new Gson();
        String jsonFriendRequest = gsonFriendRequest.toJson(friendRequestMessage);

        // Try to send direct friend request first, (in case user is online)
        boolean sendDirect = false;
        if (friendProfile.getPeerAddress() != null) {
            sendDirect = p2p.sendBlocking(friendProfile.getPeerAddress(), jsonFriendRequest);
        }

        // If that failed, or other has no peer address, append to pub profile of other friend
        if (sendDirect == false) {
            // Friend is not online, append to public profile
            friendProfile.getPendingFriendRequests().add(jsonFriendRequest);
            if (p2p.put(userID, friendProfile) == false) {
                return new Pair<>(false, "Error sending friend request");
            }
        }



        // Addd as friend
        if (addFriend(userID) == false) {
            return new Pair<>(false, "Error, adding the friend");
        }

        return new Pair<>(true, "Friend request to " + userID + " was sent");
    }

    public void donothing(){
        System.err.println("nothing");
    }

    public FriendsListEntry getFriendsListEntry(String userID) {
        if(friendsList == null){
            return null;
        }
        for (FriendsListEntry e : friendsList) {
            if (e.getUserID().equals(userID)) {
                return e;
            }
        }
        return null;
    }

    public List<FriendsListEntry> getFriendsList() {
        if(friendsList == null){
            return friendsList = new ArrayList<FriendsListEntry>();
        }else{
            return friendsList;
        }
    }

    public boolean addFriend(String userID) {
        // Add to list
        System.err.println("ADD THIS BFF: " + userID);
        friendsList.add(new FriendsListEntry(userID));
        //friendsList.sort(new FriendsListComparator());
        //mainController.sortFriendsListView();

        // Send ping
        pingUser(userID, true, true);

        //friendListController.updateFriends(); TODO: update to android friendlist

        // Save profile
        return savePrivateUserProfile();
    }

    private void pingUser(String userID, boolean onlineStatus, boolean replyPongExpected) {
        p2p.getNonBLocking(userID, new BaseFutureAdapter<FutureGet>() {
            @Override
            public void operationComplete(FutureGet f) throws Exception {
                FriendsListEntry friendsListEntry = getFriendsListEntry(userID);
                assert (friendsListEntry != null);
                if (f.isSuccess()) {
                    Gson publicUserProfileGson = new Gson();
                    String publicUserProfileJson = (String) f.data().object();

                    PublicUserProfile publicUserProfile = publicUserProfileGson.fromJson(publicUserProfileJson, PublicUserProfile.class);
                    // Set peer address in friendslist
                    PeerAddress peerAddress = publicUserProfile.getPeerAddress();
                    friendsListEntry.setPeerAddress(peerAddress);

                    // Send ping
                    if (peerAddress != null) {
                        pingAddress(publicUserProfile.getPeerAddress(), onlineStatus, replyPongExpected);
                    }
                } else {
                    // Can't find other peer, maybe he deleted his account? -> show offline
                    System.out.println("User " + userID + " doesnt seem to exist anymore");
                    friendsListEntry.setOnline(false);
                    friendsListEntry.setPeerAddress(null);
                }
            }
        });
    }

    public void pingAddress(PeerAddress pa, boolean onlineStatus, boolean replyPongExpected) {
        // Send ping
        OnlineStatusMessage msg = new OnlineStatusMessage("OnlineStatusMessage", p2p.getPeerAddress(), userProfile.getUserID(), onlineStatus, replyPongExpected);
        Gson onlineStatusMessageGson = new Gson();
        String onlineStatusMessageJson = onlineStatusMessageGson.toJson(msg);

        p2p.sendNonBlocking(pa, onlineStatusMessageJson, false);
    }

    public void logout() {


        // Tell "friends" that i'm going offline
        pingAllFriends(false);

        // Set PeerAddress in public Profile to null
        Object objectPublicUserProfile = p2p.getBlocking(userProfile.getUserID());
        if (objectPublicUserProfile == null) {
            System.out.println("Could not retrieve public userprofile");
            return;
        }

        Gson publicUserprofileGson = new Gson();
        String publicUserProfileJson = (String) objectPublicUserProfile;


        PublicUserProfile publicUserProfile = publicUserprofileGson.fromJson(publicUserProfileJson, PublicUserProfile.class);
        publicUserProfile.setPeerAddress(null);

        String newPublicUserProfileJson = publicUserprofileGson.toJson(publicUserProfile);

        if (p2p.put(userProfile.getUserID(), newPublicUserProfileJson) == false) {
            System.out.println("Could not update peer address in public user profile");
            return;
        }

        p2p.setObjectDataReply(null);

        userProfile = null;
        friendsList = null;
        shutdown();


    }

    private void pingAllFriends(boolean onlineStatus) {
        if (friendsList == null){
            return;
        }
        for (FriendsListEntry entry : friendsList) {
            String userID = entry.getUserID();

            // For friends that are online, send direct to their PeerAddress
            if (entry.isOnline()) {
                OnlineStatusMessage ping = new OnlineStatusMessage("OnlineStatusMessage", p2p.getPeerAddress(), userProfile.getUserID(),
                        onlineStatus, onlineStatus);
                Gson pingAllFriendsGson = new Gson();
                String pingAllFriendsJson = pingAllFriendsGson.toJson(ping);

                p2p.sendNonBlocking(entry.getPeerAddress(), pingAllFriendsJson, false);
            } // For friends that are offline and in the case that we want to tell
            // them we're coming online, use pingUser method to first check for
            // their peerAddress (if any).
            else if (!entry.isOnline() && onlineStatus == true) {
                pingUser(userID, onlineStatus, onlineStatus);

            }
        }
    }

    public void shutdown() {
        if (userProfile != null) {
            logout();
        }
        // Shutdown Tom P2P stuff
        p2p.shutdown();
        //Platform.exit(); javafx remnant
        System.exit(0);
    }

    public void handleIncomingOnlineStatus(OnlineStatusMessage msg) {
        synchronized (this) {
            FriendsListEntry e = getFriendsListEntry(msg.getSenderUserID());

            // If friend is in friendslist
            if (e != null) {

                // Show notification if user came online
/*                if (msg.isOnline() && !e.isOnline()) {
                    Notifications.create().text("User " + msg.getSenderUserID() + " just came online")
                            .showInformation();
                }*/

                // Set online/offline
                e.setOnline(msg.isOnline());
                e.setPeerAddress(msg.getSenderPeerAddress());
                e.setWaitingForHeartbeat(false);

                //sortFriendsListView();

                // Send pong back if wanted
                if (msg.isReplyPongExpected()) {
                    pingAddress(msg.getSenderPeerAddress(), true, false);
                }
            }
        }
    }

    public void sendChatMessage(String text, FriendsListEntry friendsListEntry) {
        ChatMessage chatMessage = new ChatMessage("ChatMessage", p2p.getPeerAddress(), userProfile.getUserID(), text);
        System.err.println("SENDING THIS: peeraddress: " + p2p.getPeerAddress() + " userID: " + userProfile.getUserID() + " text: " + text);
        System.err.println("SENDING TO: peeraddress: " + friendsListEntry.getPeerAddress() + " userID: " + friendsListEntry.getUserID() + " text: " + text);

        Gson chatMessageGson = new Gson();
        String ChatMessageJson = chatMessageGson.toJson(chatMessage);


        p2p.sendNonBlocking(friendsListEntry.getPeerAddress(), ChatMessageJson, false);
    }



    public void handleIncomingChatMessage(ChatMessage msg) {
        FriendsListEntry e = getFriendsListEntry(msg.getSenderUserID());

        // If friend is in friendslist
        if (e != null) {
            System.err.println("Message received from: " + msg.getSenderUserID() + " Messagetext: " + msg.getMessageText());
            //openChat.showIncomingChatMessage(msg.getSenderUserID(), msg.getMessageText());
            // msgWindowController.addChatBubble(msg.getMessageText(), msg.getSenderUserID(), false); TODO: update to android msgs
            System.err.println("offer msg to queueu");
            messageQueue.offer(msg);
            System.err.println("queueu: " + messageQueue.peek());


        }
        else{
            System.err.println("That's my purse, i don't know you!");
        }
    }

    public void register(String username, String password) {
        try {

            this.username = username;
            this.password = password;
            System.err.println("username: " + username);
            System.err.println("hashed password: " + password);

            Pair<Boolean, String> result = createAccount(username, password);

            if (result.first == true) {
                System.err.println("Account creation OK");
            } else {
                System.err.println("Account creation FUCKED UP, OH NOEZ");
            }

        } catch (Exception e) {
            System.err.println("Caught Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Pair<Boolean, String> createAccount(String userID, String password) {
        // Check if the user is already in the friendslist

        // Check if account exists
        if (p2p.getBlocking(userID) != null) {
            System.err.println("NULL??? WHAT THE SHIT WHY???");
            return new Pair<>(false, "Could not create user account. UserID already taken."); //TODO: LOGIN NOW

        }

        // Create private UserProfile
        userProfile = new PrivateUserProfile(userID, password);

        // TODO: Encrypt it with password
        if (savePrivateUserProfile() == false) {
            System.err.println("Error. Could not save private UserProfile");
            return new Pair<>(false, "Error. Could not save private UserProfile");
        }

        // Create public UserProfile
        PublicUserProfile publicUserProfile;
        publicUserProfile = new PublicUserProfile(userID,    null);
        Gson gson = new Gson();
        String jsonPublic = gson.toJson(publicUserProfile);

        if (p2p.put(userID, jsonPublic)) {
            login2R(userID, password);
            return new Pair<>(true, "User account for user \"" + userID + "\" successfully created");
        } else {
            return new Pair<>(false, "Network DHT error. Could not save public UserProfile");
        }
    }

    public void loginR(String username, String password, final int id, final String ip) {
        try {
            this.username = username;
            this.password = password;
            System.err.println("username:" + username);
            System.err.println("unhashed password:" + password);
            //this.clientIP = ip;
            //this.clientId = id;

            Pair<Boolean, String> result = login2R(username, password);

            if (result.first == false) {
                System.err.println("NOT Loged in successfully, SOMETHING BROKE");
            } else {
                System.err.println("Logged in A-Okay");
            }


        } catch (Exception e) {
            System.err.println("Caught Exception: " + e.getMessage());
            e.printStackTrace();
        }
//        try {
//            MainWindow mainWindow = new MainWindow();
//            mainWindow.draw(loginWindow.getStage());
//
//        } catch (Exception e) {
//            System.err.println("Caught Exception: " + e.getMessage());
//            e.printStackTrace();
//
//        }
    }

    public Pair<Boolean, String> login2R(String userID, String password) {


/*        if (BCrypt.checkpw(insecurePassword, hashed))
            System.out.println("It matches");
        else
            System.out.println("It does not match");

*/

        // Get userprofile if password and username are correct
        Object getResult = p2p.getBlocking(userID + password);
        if (getResult == null) {
            System.err.println("Login data not valid, Wrong UserID/password?");
            return new Pair<>(false, "Login data not valid, Wrong UserID/password?");
        }

        System.err.println("Whatdo we have here?");
        System.err.println(getResult);
        Gson gson = new Gson();
        userProfile = gson.fromJson((String) getResult, PrivateUserProfile.class);
        //userProfile = (PrivateUserProfile) getResult;


        // Get public user profile
        Object objectPublicUserProfile = p2p.getBlocking(userID);
        if (objectPublicUserProfile == null) {
            System.err.println("Could not retrieve public userprofile");
            return new Pair<>(false, "Could not retrieve public userprofile");
        }

        PublicUserProfile publicUserProfile = gson.fromJson((String) objectPublicUserProfile, PublicUserProfile.class);
        //PublicUserProfile publicUserProfile = (PublicUserProfile) objectPublicUserProfile;

        // **** FRIENDS LIST ****
        // Reset all friends list entries to offline and unkown peer address
        for (FriendsListEntry e : userProfile.getFriendsList()) {
            e.setOnline(false);
            e.setPeerAddress(null);
            e.setWaitingForHeartbeat(false);
        }
         friendsList = ((userProfile.getFriendsList()));

         friendRequestsList = (userProfile.getFriendRequestsList());




        // Set current IP address in public user profile
        publicUserProfile.setPeerAddress(p2p.getPeerAddress());
        String jsonPublic = gson.toJson(publicUserProfile);

        // Save public user profile
        if (p2p.put(userID, jsonPublic) == false) {
            System.err.println("Could not update public user profile");
            return new Pair<>(false, "Could not update public user profile");
        }

        // Set reply handler
        p2p.setObjectDataReply(new ObjectReplyHandler(this));

        // Send out online status to all friends
           pingAllFriends(true);

        // Schedule new thread to check periodically if friends are still online
        scheduler = Executors.newScheduledThreadPool(1);


            final Runnable pinger = new Runnable() {
                public void run() { System.out.println("pinged online to all friends");
                    pingAllOnlineFriends();
                }
            };
            final ScheduledFuture<?> beeperHandle =
                    scheduler.scheduleAtFixedRate(pinger, 10, 10, SECONDS);




/*
        scheduler.scheduleAtFixedRate(() -> {
            pingAllOnlineFriends();
        }, 10, 10, SECONDS);
*/

        System.err.println("Login successful???? yea m8");
        return new Pair<>(true, "Login successful");
    }

    private void pingAllOnlineFriends() {
        for (FriendsListEntry entry : friendsList) {
            if (entry.isOnline()) {
                // If friend din't reply since last call, set him offline
                if (entry.isWaitingForHeartbeat()) {
                    entry.setOnline(false);
                    // sortFriendsListView();
                }

                // Flag friend until he replies
                entry.setWaitingForHeartbeat(true);

                OnlineStatusMessage ping = new OnlineStatusMessage("OnlineStatusMessage", p2p.getPeerAddress(), userProfile.getUserID(),
                        true, true);
                Gson pingAllOnlineFriendsGson = new Gson();
                String pingAllOnlineFriendsJson = pingAllOnlineFriendsGson.toJson(ping);

                p2p.sendNonBlocking(entry.getPeerAddress(), pingAllOnlineFriendsJson, false);
            }
        }

    }

    public MainWindow getMainWindow(){
        return this;
    }




}
