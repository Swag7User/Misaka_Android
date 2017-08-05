package ch.uzh;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.*;
import android.util.Pair;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import ch.uzh.helper.FriendsListEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final Logger log = LoggerFactory.getLogger(MainActivity.class);


    public MainWindow mainWindow;
    private RecyclerView recyclerView;
    //private RecyclerView.Adapter mAdapter;
    private FriendListAdapter friendListAdapter;

    private final int REQUEST_PERMISSION_RECORD_AUDIO=1;

    // click on a cardview of a friend
    public void onClickCalled(String userID) {
        log.info(userID);
        mainWindow.setCurrentChatpartner(userID);
        mainWindow.removeNewMsgAlert(userID);
        Intent intent = new Intent(getApplicationContext(), MsgActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        GlobalState state = ((GlobalState) getApplicationContext());
        mainWindow = state.getMainWindow();

        recyclerView = (RecyclerView) findViewById(R.id.friendlistView);

        friendListAdapter = new FriendListAdapter(this, mainWindow.getFriendsList());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(friendListAdapter);

        Handler h = new Handler();
        int delay = 2000; //milliseconds

        h.postDelayed( new Runnable() {
            public void run() {
                log.info("updated friendslist");
                friendListAdapter.notifyDataSetChanged();
                h.postDelayed(this, 2000);
            }
        }, delay);

        //demoFriendList();

        showPhoneStatePermission();

    }

    private void showPhoneStatePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.RECORD_AUDIO)) {
                showExplanation("Permission Needed", "Rationale", android.Manifest.permission.RECORD_AUDIO, REQUEST_PERMISSION_RECORD_AUDIO);
            } else {
                requestPermission(android.Manifest.permission.RECORD_AUDIO, REQUEST_PERMISSION_RECORD_AUDIO);
            }
        } else {
            Toast.makeText(MainActivity.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_RECORD_AUDIO:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

    private void demoFriendList(){
        FriendsListEntry friend1 = new FriendsListEntry("He-Man");
        FriendsListEntry friend2 = new FriendsListEntry("Skeletor");
        mainWindow.getFriendsList().add(friend1);
        mainWindow.getFriendsList().add(friend2);
        friendListAdapter.notifyDataSetChanged();
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
    public void updateFriendsList(){
        friendListAdapter.notifyDataSetChanged();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

            mainWindow.savePrivateUserProfileNonBlocking();
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

            Intent intent = new Intent(getApplicationContext(), AddActivity.class);
            startActivity(intent);


        } else if (id == R.id.nav_slideshow) {

            Pair<Boolean, String> result = mainWindow.sendFriendRequest("mikoto", "hi, pls accept");
            log.info("I SENT THSI SHIT TO ME??? YO: " + "misaka " + "hi, pls accept");

            if (result.first == true) {
                log.info("friend request sent to myself");
            } else {
                log.info("friend request ERROR");
            }


        } else if (id == R.id.nav_manage) {
            friendListAdapter.notifyDataSetChanged();

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
            Intent intent = new Intent(getApplicationContext(), MsgActivity.class);
            startActivity(intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
