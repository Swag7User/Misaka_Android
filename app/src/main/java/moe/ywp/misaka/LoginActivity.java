package moe.ywp.misaka;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import moe.ywp.misaka.Userstuff.FriendsListEntry;
import moe.ywp.misaka.Userstuff.PrivateUserProfile;
import moe.ywp.misaka.Userstuff.PublicUserProfile;
import moe.ywp.misaka.helper.Password;
import moe.ywp.misaka.network.ObjectReplyHandler;
import moe.ywp.misaka.network.P2POverlay;
import android.util.Pair;
import moe.ywp.misaka.helper.*;

import java.io.IOException;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;

public class LoginActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String username;
    private String password;
    private int clientId;
    private String clientIP;
    private PrivateUserProfile userProfile;


    private P2POverlay p2p;
    private EditText usernameField;
    private EditText passwordField;
    private TextView errorLable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        usernameField = (EditText) findViewById(R.id.usernameEditText);
        passwordField = (EditText) findViewById(R.id.passwordEditText);
        errorLable = (TextView) findViewById(R.id.errorLable);


        FloatingActionButton fabLog = (FloatingActionButton) findViewById(R.id.fabLog);
        fabLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action LOGIN", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                System.err.println("clicked the login button");
                System.err.println("CLICK CLICK CLICK");
                if (loginCheck() == false) {
                    return;
                } else {
                    reg();
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);


            }
        });

        Button loginbtn = (Button) findViewById(R.id.loginbutton);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.err.println("clicked the REAL login button");
                if (loginCheck() == false) {
                    System.err.println("LOGINCHECK FALSE");
                    return;
                } else {
                    int id = getId();
                    login(id, null);
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);


            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        String bootstrapIP = "192.168.1.15";
        System.setProperty("java.net.preferIPv6Addresses", "false");
        p2p = new P2POverlay();

        // Try to bootstrap yay
        Pair<Boolean, String> result = p2p.bootstrap(bootstrapIP);
        if (result.first == false) {
            System.err.println("Aw shit, didn't work\n");
        } else{
            System.err.println("it's AWRIGHT\n");
        }

        System.out.println("Bootstrapped to: " + bootstrapIP
                + "My IP: " + p2p.getPeerAddress().inetAddress().getHostAddress());

    }

    public void reg() {
        try {

            username = usernameField.getText().toString();
            password = passwordField.getText().toString();
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
        publicUserProfile = new PublicUserProfile(userID, userProfile.getKeyPair().getPublic(),
                null);

        if (p2p.put(userID, publicUserProfile)) {
            login2(userID, password);
            System.err.println("User account for user \"" + userID + "\" successfully created");
            return new Pair<>(true, "User account for user \"" + userID + "\" successfully created");
        } else {
            System.err.println("Network DHT error. Could not save public UserProfile");
            return new Pair<>(false, "Network DHT error. Could not save public UserProfile");
        }
    }

    private boolean savePrivateUserProfile() {
        // TODO: encrypt before saving

        return p2p.put(userProfile.getUserID() + userProfile.getPassword(), userProfile);
    }


    public boolean loginCheck() {
        if (usernameField.getText().length() < 3) {
            errorLable.setText("Username too short, at least 3 characters");
            return false;
        }
        if (Password.checkPassword(passwordField.getText().toString()) == false) {
            errorLable.setText("Password too short, please chose at least 10 characters");
            return false;
        }
        try {
            if (Password.passwordContainsTop(passwordField.getText().toString(), this) == false) {
                errorLable.setText("Password too common, please chose another one");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Aw shit, password check borkered");
        }
        return true;
    }

    public void login(final int id, final String ip) {
        try {
            username = usernameField.getText().toString();
            password = passwordField.getText().toString();
            System.err.println("username:" + username);
            System.err.println("unhashed password:" + password);
            this.clientIP = ip;
            this.clientId = id;

            Pair<Boolean, String> result = login2(username, password);

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

    public Pair<Boolean, String> login2(String userID, String password) {


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

        System.err.println(getResult.toString());
        userProfile = (PrivateUserProfile) getResult;


        // Get public user profile
        Object objectPublicUserProfile = p2p.getBlocking(userID);
        if (objectPublicUserProfile == null) {
            System.err.println("Could not retrieve public userprofile");
            return new Pair<>(false, "Could not retrieve public userprofile");
        }
        PublicUserProfile publicUserProfile = (PublicUserProfile) objectPublicUserProfile;

        // **** FRIENDS LIST ****
        // Reset all friends list entries to offline and unkown peer address
        for (FriendsListEntry e : userProfile.getFriendsList()) {
            e.setOnline(false);
            e.setPeerAddress(null);
            e.setWaitingForHeartbeat(false);
        }
       // friendsList = FXCollections.synchronizedObservableList(FXCollections.observableList(userProfile.getFriendsList()));

       // friendRequestsList = FXCollections.observableList(userProfile.getFriendRequestsList());




        // Set current IP address in public user profile
        publicUserProfile.setPeerAddress(p2p.getPeerAddress());

        // Save public user profile
        if (p2p.put(userID, publicUserProfile) == false) {
            System.err.println("Could not update public user profile");
            return new Pair<>(false, "Could not update public user profile");
        }

        // Set reply handler
        p2p.setObjectDataReply(new ObjectReplyHandler(this));

        // Send out online status to all friends
     //   pingAllFriends(true);

        // Schedule new thread to check periodically if friends are still online
    /*    scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            pingAllOnlineFriends();
        }, 10, 10, SECONDS);
*/
        System.err.println("Login successful");
        return new Pair<>(true, "Login successful");
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.network) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private int getId() {
        int id = ((Long) System.currentTimeMillis()).intValue();
        return id;
    }
}
