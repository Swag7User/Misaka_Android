package moe.ywp.misaka;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Pair;
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
import moe.ywp.misaka.helper.PublicUserProfile;

import java.math.BigInteger;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public MainWindow mainWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action MAIN activity newwww", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                if (mainWindow.p2p.getBlocking("test1") != null) {
                    System.err.println("NULL??? WHAT THE SHIT WHY??? this isn't ok");

                }


                int e = 0;
                e = (int) mainWindow.p2p.getBlocking("test1");
                if (mainWindow.p2p.getBlocking("test1") != null) {
                    System.err.println("NULL??? WHAT THE SHIT WHY??? this isn't ok");

                }
                System.err.println("magic happens here : " + e);

                String test = "misaka";
                System.err.println("TET HEX: " + toHex(test));

                System.err.println("what even is it???: " + mainWindow.p2p.getBlocking(test));
                System.err.println("what even is it???: " + mainWindow.p2p.getBlocking(test).toString());
                PublicUserProfile pub = (PublicUserProfile) mainWindow.p2p.getBlocking(test);
                if (mainWindow.p2p.getBlocking(test) != null) {
                    System.err.println("FINAL GTEST BEFORE I KMS not null");
                } else{
                    System.err.println("FINAL GTEST BEFORE I KMS null");

                }
                System.err.println("FINAL GTEST BEFORE I KMS OR NOT");
                System.err.println(pub.getUserID() + pub.getPeerAddress());



            }
        });

        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count = (int) mainWindow.p2p.getBlocking("test77");
                    System.err.println("click btn2: " + count);

                System.err.println("last rty: " + mainWindow.p2p.getBlocking("misaka"));

                System.err.println( "exists usr misaka: " + mainWindow.existsUser("misaka"));
                System.err.println( "exists usr mikoto: " + mainWindow.existsUser("mikoto"));
                System.err.println( "exists usr test77: " + mainWindow.existsUser("test77"));
                System.err.println( "exists usr test42: " + mainWindow.existsUser("test42"));
                System.err.println( "exists usr test48: " + mainWindow.existsUser("test48"));



            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        GlobalState state = ((GlobalState) getApplicationContext());
        mainWindow = state.getMainWindow();

        int i = 666;
        mainWindow.p2p.put("test1", i);
        System.err.println("put 666! ");



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
        getMenuInflater().inflate(R.menu.main, menu);
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
    public String toHex(String arg) {
        return String.format("%x", new BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

            String s = "misaka";
            System.err.println("HEX HEX ~~~~~~~~~~~~~~~SENDING~~~~~~~~~~~~~ HEX HEX");
            System.err.println("HEX misaka:" + toHex(s));
            Pair<Boolean, String> result = mainWindow.sendFriendRequest(s, "hi, pls accept");
            System.err.println("I SENT THSI SHIT YO: " + "misaka " + "hi, pls accept");

            if (result.first == true) {
                System.err.println("friend request sent");
            } else {
                System.err.println("friend request ERROR");
            }

            String newt = (String) mainWindow.p2p.getBlocking("test42");
            System.err.println("NEWT = " + newt);



        } else if (id == R.id.nav_slideshow) {

            Pair<Boolean, String> result = mainWindow.sendFriendRequest("mikoto", "hi, pls accept");
            System.err.println("I SENT THSI SHIT TO ME??? YO: " + "misaka " + "hi, pls accept");

            if (result.first == true) {
                System.err.println("friend request sent to myself");
            } else {
                System.err.println("friend request ERROR");
            }


        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
