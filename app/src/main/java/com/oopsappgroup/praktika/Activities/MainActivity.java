package com.oopsappgroup.praktika.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.oopsappgroup.praktika.Fragments.LazyCatsDataBaseFragment;
import com.oopsappgroup.praktika.Fragments.WelcomeFragment;
import com.oopsappgroup.praktika.R;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //Fragment
    Fragment fragment;

    //Header Text View
    TextView personEmail;

    //Shared Preferences
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Select first item in Navigation Drawer
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_welcome_page));
        navigationView.setCheckedItem(R.id.nav_welcome_page);

        //Get header Text View
        View header = navigationView.getHeaderView(0);
        personEmail = (TextView)header.findViewById(R.id.tvPersonEmail);

        //Is User signed in?
        sp = this.getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE);
        if (!sp.getBoolean("User signed in", false)){

            //Starting login activity

            Intent loginActivity = new Intent(this, LoginActivity.class);
            startActivityForResult(loginActivity, 1);

        }else {
            refreshUserData();

            //On boarding
            if (sp.getBoolean("User's first enter", true))
                showSideNavigationPrompt();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) this.finish();

        if (requestCode == 1) {
            refreshUserData();

            //On boarding
            if (sp.getBoolean("User's first enter", true))
                showSideNavigationPrompt();
        }
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_welcome_page) {
            fragment = new WelcomeFragment();
        } else if (id == R.id.nav_database) {
            fragment = new LazyCatsDataBaseFragment();
        }else if (id == R.id.nav_signOut) {
            //Sign out from acc
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();

            //Start login activity
            Intent loginActivity = new Intent(this, LoginActivity.class);
            startActivityForResult(loginActivity, 1);

            //Putting value in sp for other app runs
            sp.edit().putBoolean("User signed in", false).apply();

        }else if (id == R.id.nav_send) {
            //Send debug info

            String debugInfo ="Debug-info:";
            debugInfo += "\n OS Version: " + System.getProperty("os.version") + " (" + android.os.Build.VERSION.INCREMENTAL + ")";
            debugInfo += "\n OS API Level: "+android.os.Build.VERSION.RELEASE + " ("+android.os.Build.VERSION.SDK_INT+")";
            debugInfo += "\n Device: " + android.os.Build.DEVICE;
            debugInfo += "\n Model (and Product): " + android.os.Build.MODEL + " ("+ android.os.Build.PRODUCT + ")";

            Intent send = new Intent(Intent.ACTION_SENDTO);
            String uriText = "mailto:" + Uri.encode("latmikael@gmail.com") +
                    "?subject=" + Uri.encode(getString(R.string.error_message_uri)) +
                    "&body=" + Uri.encode( debugInfo + "\n \n" + getString(R.string.describe_your_error));
            Uri uri = Uri.parse(uriText);

            send.setData(uri);

            try {
                startActivity(Intent.createChooser(send, getString(R.string.send_email)));
            } catch (android.content.ActivityNotFoundException ex) {
                FirebaseCrash.report(ex);
                Toast.makeText(MainActivity.this, R.string.email_apps_not_found, Toast.LENGTH_SHORT).show();
            }
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragments_container, fragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void refreshUserData(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address
            String name = user.getDisplayName();
            String email = user.getEmail();

            //Set person e-mail into header
            personEmail.setText(email);

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            String uid = user.getUid();
        }
    }

    public void showSideNavigationPrompt() {
        final MaterialTapTargetPrompt.Builder tapTargetPromptBuilder = new MaterialTapTargetPrompt.Builder(this)
                .setPrimaryText(R.string.menu_prompt_title)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setIcon(R.drawable.ic_drawer);
        final Toolbar tb = (Toolbar) this.findViewById(R.id.toolbar);
        tapTargetPromptBuilder.setTarget(tb.getChildAt(1));

        tapTargetPromptBuilder.setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener()
        {
            @Override
            public void onHidePrompt(MotionEvent event, boolean tappedTarget) {
                //Do something such as storing a value so that this prompt is never shown again
            }

            @Override
            public void onHidePromptComplete() {
                sp.edit().putBoolean("User's first enter", false).apply();
            }
        });
        tapTargetPromptBuilder.show();
    }
}
