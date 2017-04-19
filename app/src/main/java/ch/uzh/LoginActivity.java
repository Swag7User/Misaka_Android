package ch.uzh;

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
import ch.uzh.helper.PrivateUserProfile;
import ch.uzh.helper.Password;
import ch.uzh.helper.P2POverlay;
import android.util.Pair;

import java.io.IOException;
import java.math.BigInteger;

public class LoginActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public MainWindow mainWindow;

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
                System.err.println("clicked the reg button");
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

        mainWindow = new MainWindow(p2p);
        GlobalState state = ((GlobalState) getApplicationContext());
        state.setMainWindow(mainWindow);


    }

    public void reg() {
        try {

            username = usernameField.getText().toString();
            password = passwordField.getText().toString();
            System.err.println("username: " + username);
            System.err.println("hashed password: " + password);

            mainWindow.register(username, password);

        } catch (Exception e) {
            System.err.println("Caught Exception: " + e.getMessage());
            e.printStackTrace();
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

    public String toHex(String arg) {
        return String.format("%x", new BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
    }

    public void login(final int id, final String ip) {
        try {

            username = usernameField.getText().toString();
            password = passwordField.getText().toString();
            System.err.println("username:" + username);
            System.err.println("unhashed password:" + password);
            System.err.println("HEX HEX ~~~~~~~~~~~~~~~INPUT LOGIN~~~~~~~~~~~~~ HEX HEX");
            System.err.println("HEX username:" + toHex(username));
            System.err.println("HEX unhashed password:" + toHex(password));

            this.clientIP = ip;
            this.clientId = id;

            mainWindow.loginR(username, password, id, ip);


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

    public void login2(String userID, String password) {

        mainWindow.login2R(userID, password);

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
