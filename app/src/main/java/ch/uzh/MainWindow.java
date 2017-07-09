package ch.uzh;

import android.util.Pair;
import ch.uzh.helper.*;
import com.google.gson.Gson;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.peers.PeerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by Jesus on 19.04.2017.
 */
public class MainWindow {

    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);

    private int id;
    private String ip;
    private String username;
    private String password;
    private boolean bootstrapNode;

    Key publicKey;
    Key privateKey;

    byte[] publicKeySerialized;
    byte[] privateKeySerialized;

    private PrivateUserProfile userProfile;
    private EncryptedPrivateUserProfile encrypteduserProfile;

    public static boolean futurputSuccess = false;


    public P2POverlay p2p;
    private List<FriendsListEntry> friendsList;
    public Queue<ChatMessage> messageQueue;
    private List<FriendRequestMessage> friendRequestsList;
    private ScheduledExecutorService scheduler;
    private CallHandler callHandler;


    private String currentChatPartner;


    public String getCurrentChatpartner() {
        return currentChatPartner;
    }

    public void setCurrentChatpartner(String userID) {
        currentChatPartner = userID;
    }

    public MainWindow(P2POverlay p2p) {
        this.p2p = p2p;
        messageQueue = new LinkedList();
    }


    public void acceptFriendRequest(FriendRequestMessage message) {
        // Add user
        addFriend(message.getSenderUserID());

        // Remove friend request
        friendRequestsList.remove(message);

        // Save User Profile
        savePrivateUserProfileNonBlocking();
        log.info("userid: " + message.getSenderUserID() + "messagetxt: " + message.getMessageText() + "peeraddress: " + message.getSenderPeerAddress());
        FriendsListEntry newFriend = new FriendsListEntry(message.getSenderUserID());
        newFriend.setPeerAddress(message.getSenderPeerAddress());
        //friendListController.updateFriends(); TODO: update to android friendlist

    }

    public boolean savePrivateUserProfileNonBlocking() {
        String json = GsonHelper.createJsonString(encrypteduserProfile);
        log.info("IMMA GONNA PRINT MY JSON NON BLOCKING");
        log.info(json);
        log.info("PRINTED MY JSON");
        log.info("errors? " + userProfile.getUserID() + " - " + userProfile.getPassword());

        return p2p.putNonBlocking(userProfile.getUserID() + userProfile.getPassword(), json);
    }

    public String getUserID() {
        return (userProfile != null) ? userProfile.getUserID() : "error";
    }

    public void handleIncomingAudioFrame(AudioFrame frame) {
        if (true) {
            callHandler.addAudioFrame(frame.getData());
        }
    }

    public void declineFriendRequest(FriendRequestMessage message) {
        // Remove friend request
        friendRequestsList.remove(message);

        // Save User Profile
        savePrivateUserProfileNonBlocking();
    }

    public void startTransmitting() {
        // Create new call
        callHandler = new CallHandler(this, p2p, getFriendsListEntry(getCurrentChatpartner()));
        try {
            callHandler.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            stopTransmitting();
            System.out.println("LineUnavailableException");
        }
    }

    public void stopTransmitting() {
        if (callHandler == null) {
            return;
        }
        callHandler.stop();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        callHandler = null;
    }

    public void handleIncomingFriendRequest(FriendRequestMessage requestMessage) {
        // Ignore requests from users already in the list
        if (userProfile.isFriendsWith(requestMessage.getSenderUserID())) {
            log.info("user already in friendlist");
            return;
        }

        // Add friend request
        friendRequestsList.add(requestMessage);

        // Save the change
        log.info("Am I here? lul");
        boolean isSaved = savePrivateUserProfileNonBlocking();
        if (isSaved == true) {
            log.info("saved succesfully");
        } else {
            log.info("saved UNsuccesfully");

        }
        log.info("why am i not here tough");

        acceptFriendRequest(requestMessage);

    }

    public boolean existsUser(String userID) {
        return (p2p.getBlocking(userID) != null);
    }


    public Pair<Boolean, String> sendFriendRequest(String userID, String messageText) {
        // Check if user already exists in friends list
        if (getFriendsListEntry(userID) != null) {
            log.info("User already in friendslist");
            return new Pair<>(false, "User already in friendslist");
        }

        // Check if user exists in the network
        if (!existsUser(userID)) {
            log.info("User was not found");
            return new Pair<>(false, "User was not found");
        }

        // Get public profile of friend we want to add
        String jsonFriendProfile = (String) p2p.getBlocking(userID);
        Gson gson = new Gson();
        PublicUserProfile friendProfile = gson.fromJson(jsonFriendProfile, PublicUserProfile.class);
        // Create friend request message
        FriendRequestMessage friendRequestMessage = new FriendRequestMessage("FriendRequestMessage", p2p.getPeerAddress(), userProfile.getUserID(), messageText);

        String jsonFriendRequest = GsonHelper.createJsonString(friendRequestMessage);

        // Try to send direct friend request first, (in case user is online)
        boolean sendDirect = false;
        if (friendProfile.getPeerAddress() != null) {
            sendDirect = p2p.sendBlocking(friendProfile.getPeerAddress(), jsonFriendRequest);
        }

        // If that failed, or other has no peer address, append to pub profile of other friend
        if (sendDirect == false) {
            // Friend is not online, append to public profile
            friendProfile.getPendingFriendRequests().add(jsonFriendRequest);
            boolean now = p2p.putNonBlocking(userID, friendProfile);

            while (!futurputSuccess) {
                donothing();
            }
            futurputSuccess = false;

            if (now == false) {
                return new Pair<>(false, "Error sending friend request");
            }
        }


        // Addd as friend
        if (addFriend(userID) == false) {
            return new Pair<>(false, "Error, adding the friend");
        }

        return new Pair<>(true, "Friend request to " + userID + " was sent");
    }

    public void donothing() {
        log.info("nothing");
    }

    public FriendsListEntry getFriendsListEntry(String userID) {
        if (friendsList == null) {
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
        if (friendsList == null) {
            return friendsList = new ArrayList<FriendsListEntry>();
        } else {
            return friendsList;
        }
    }

    public boolean addFriend(String userID) {
        // Add to list
        log.info("ADD THIS BFF: " + userID);
        friendsList.add(new FriendsListEntry(userID));

        // Send ping
        pingUser(userID, true, true);

        //friendListController.updateFriends(); TODO: update to android friendlist

        // Save profile
        return savePrivateUserProfileNonBlocking();
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
        String onlineStatusMessageJson = GsonHelper.createJsonString(msg);

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

        String newPublicUserProfileJson = GsonHelper.createJsonString(publicUserProfile);

        boolean now = p2p.putNonBlocking(userProfile.getUserID(), newPublicUserProfileJson);

        while(!futurputSuccess){
            donothing();
        }
        futurputSuccess = false;

        if (now == false) {
            System.out.println("Could not update peer address in public user profile");
            return;
        }

        savePrivateUserProfileNonBlocking();

        p2p.setObjectDataReply(null);

        userProfile = null;
        friendsList = null;
        shutdown();


    }

    private void pingAllFriends(boolean onlineStatus) {
        if (friendsList == null) {
            return;
        }
        for (FriendsListEntry entry : friendsList) {
            String userID = entry.getUserID();

            // For friends that are online, send direct to their PeerAddress
            if (entry.isOnline()) {
                OnlineStatusMessage ping = new OnlineStatusMessage("OnlineStatusMessage", p2p.getPeerAddress(), userProfile.getUserID(),
                        onlineStatus, onlineStatus);
                String pingAllFriendsJson = GsonHelper.createJsonString(ping);

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
        log.info("SENDING THIS: peeraddress: " + p2p.getPeerAddress() + " userID: " + userProfile.getUserID() + " text: " + text);
        log.info("SENDING TO: peeraddress: " + friendsListEntry.getPeerAddress() + " userID: " + friendsListEntry.getUserID() + " text: " + text);

        String ChatMessageJson = GsonHelper.createJsonString(chatMessage);


        p2p.sendNonBlocking(friendsListEntry.getPeerAddress(), ChatMessageJson, false);
    }


    public void handleIncomingChatMessage(ChatMessage msg) {
        FriendsListEntry e = getFriendsListEntry(msg.getSenderUserID());

        // If friend is in friendslist
        if (e != null) {
            log.info("Message received from: " + msg.getSenderUserID() + " Messagetext: " + msg.getMessageText());
            log.info("offer msg to queueu");
            messageQueue.offer(msg);
            log.info("queueu: " + messageQueue.peek());


        } else {
            log.info("That's my purse, i don't know you!");
        }
    }

    public void register(String username, String password) {
        try {

            this.username = username;
            this.password = password;
            log.info("username: " + username);
            log.info("hashed password: " + password);

            Pair<Boolean, String> result = createAccount(username, password);

            if (result.first == true) {
                log.info("Account creation OK");
            } else {
                log.info("Account creation FUCKED UP, OH NOEZ");
            }

        } catch (Exception e) {
            log.info("Caught Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Pair<Boolean, String> createAccount(String userID, String password) {
        // Check if the user is already in the friendslist

        // Check if account exists
        if (p2p.getBlocking(userID) != null) {
            log.info("NULL??? WHAT THE SHIT WHY???");
            return new Pair<>(false, "Could not create user account. UserID already taken."); //TODO: LOGIN NOW

        }

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.genKeyPair();
            publicKeySerialized = kp.getPublic().getEncoded();
            privateKeySerialized = kp.getPrivate().getEncoded();
            publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeySerialized));
            privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeySerialized));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();

        }

        // Create private UserProfile
        userProfile = new PrivateUserProfile(userID, password, privateKeySerialized);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Encryption.encrypt(password, (Serializable) userProfile, baos);
            byte[] encryptedArray = baos.toByteArray();
            encrypteduserProfile = new EncryptedPrivateUserProfile(encryptedArray);
            baos.close();
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }

        // TODO: Encrypt it with password
        boolean saving = savePrivateUserProfileNonBlocking();

        log.info("saving is: " + saving);

        // Create public UserProfile
        PublicUserProfile publicUserProfile;
        publicUserProfile = new PublicUserProfile(userID, null, publicKeySerialized);
        String jsonPublic = GsonHelper.createJsonString(publicUserProfile);
        boolean now = p2p.putNonBlocking(userID, jsonPublic);

        while (!futurputSuccess) {
            donothing();
        }
        futurputSuccess = false;


        if (now) {
            login2R(userID, password);
            return new Pair<>(true, "User account for user \"" + userID + "\" successfully created");
        } else {
            log.info("Network DHT error. Could not save public UserProfile");
            return new Pair<>(false, "Network DHT error. Could not save public UserProfile");
        }
    }

    public void loginR(String username, String password, final int id, final String ip) {
        try {
            this.username = username;
            this.password = password;
            log.info("username:" + username);
            log.info("unhashed password:" + password);
            //this.clientIP = ip;
            //this.clientId = id;

            Pair<Boolean, String> result = login2R(username, password);

            if (result.first == false) {
                log.info("NOT Loged in successfully, SOMETHING BROKE");
            } else {
                log.info("Logged in A-Okay");
            }


        } catch (Exception e) {
            log.info("Caught Exception: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public Pair<Boolean, String> login2R(String userID, String password) {

        // Get userprofile if password and username are correct
        Object getResult = p2p.getBlocking(userID + password);
        if (getResult == null) {
            log.info("Login data not valid, Wrong UserID/password?");
            return new Pair<>(false, "Login data not valid, Wrong UserID/password?");
        }

        log.info("Whatdo we have here?");
        log.info(getResult.toString());
        Gson gson = new Gson();
        encrypteduserProfile = gson.fromJson((String) getResult, EncryptedPrivateUserProfile.class);
        log.info("Whatdo we have here FO REAL?");
        log.info(encrypteduserProfile.getEncryptedProfile().toString());
        ByteArrayInputStream bais = new ByteArrayInputStream(encrypteduserProfile.getEncryptedProfile());
        try {
            userProfile = (PrivateUserProfile) Encryption.decrypt(password, bais);
        }catch(IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e){
            e.printStackTrace();
        }
        log.info("userID: " + userProfile.getUserID());
        log.info("pw: " + userProfile.getPassword());
        if (!userProfile.getFriendsList().isEmpty()) {
            log.info("friends: " + userProfile.getFriendsList().get(0));
        }
        //userProfile = (PrivateUserProfile) getResult;


        // Get public user profile
        Object objectPublicUserProfile = p2p.getBlocking(userID);
        if (objectPublicUserProfile == null) {
            log.info("Could not retrieve public userprofile");
            return new Pair<>(false, "Could not retrieve public userprofile");
        }

        PublicUserProfile publicUserProfile = gson.fromJson((String) objectPublicUserProfile, PublicUserProfile.class);

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
        String jsonPublic = GsonHelper.createJsonString(publicUserProfile);

        // Save public user profile
        boolean now = p2p.putNonBlocking(userID, jsonPublic);

        while (!futurputSuccess) {
            donothing();
        }
        futurputSuccess = false;

        if (now == false) {
            log.info("Could not update public user profile");
            return new Pair<>(false, "Could not update public user profile");
        }

        // Set reply handler
        p2p.setObjectDataReply(new ObjectReplyHandler(this));

        // Send out online status to all friends
        pingAllFriends(true);

        // Schedule new thread to check periodically if friends are still online
        scheduler = Executors.newScheduledThreadPool(1);


        final Runnable pinger = new Runnable() {
            public void run() {
                System.out.println("pinged online to all friends");
                pingAllOnlineFriends();
            }
        };
        final ScheduledFuture<?> beeperHandle =
                scheduler.scheduleAtFixedRate(pinger, 10, 10, SECONDS);

        log.info("Login successful???? yea m8");
        return new Pair<>(true, "Login successful");
    }

    private void pingAllOnlineFriends() {
        for (FriendsListEntry entry : friendsList) {
            if (entry.isOnline()) {
                // If friend din't reply since last call, set him offline
                if (entry.isWaitingForHeartbeat()) {
                    entry.setOnline(false);
                }

                // Flag friend until he replies
                entry.setWaitingForHeartbeat(true);

                OnlineStatusMessage ping = new OnlineStatusMessage("OnlineStatusMessage", p2p.getPeerAddress(), userProfile.getUserID(),
                        true, true);
                Gson pingAllOnlineFriendsGson = new Gson();
                String pingAllOnlineFriendsJson = GsonHelper.createJsonString(ping);

                p2p.sendNonBlocking(entry.getPeerAddress(), pingAllOnlineFriendsJson, false);
            }
        }

    }

    public MainWindow getMainWindow() {
        return this;
    }


}
