package com.djaphar.fragmentlab;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.djaphar.fragmentlab.Fragments.ContactsFragment;
import com.djaphar.fragmentlab.Fragments.GitRepoFragment;
import com.djaphar.fragmentlab.Fragments.InfoFragment;
import com.djaphar.fragmentlab.Fragments.MapsFragment;
import com.djaphar.fragmentlab.Fragments.SensorAndCameraFragment;

import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Fragment gitRepoFragment, mapsFragment, contactsFragment, infoFragment, sensorAndCameraFragment, currentFragment;
    int currentCheckedItem;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        gitRepoFragment = new GitRepoFragment();
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putStringArray("Repos", Objects.requireNonNull(getIntent().getExtras()).getStringArray("Repositories"));
        fragmentArgs.putString("Own", Objects.requireNonNull(getIntent().getExtras().get("Owner")).toString());
        gitRepoFragment.setArguments(fragmentArgs);

        mapsFragment = new MapsFragment();
        contactsFragment = new ContactsFragment();
        infoFragment = new InfoFragment();
        sensorAndCameraFragment = new SensorAndCameraFragment();

        navigationView.setCheckedItem(R.id.nav_github_auth);
        getSupportFragmentManager().beginTransaction().add(R.id.main_fragment, gitRepoFragment).commit();
        currentCheckedItem = R.id.nav_github_auth;
        currentFragment = gitRepoFragment;
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(currentCheckedItem);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, currentFragment).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment fragment = null;
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_github_auth:
                fragment = gitRepoFragment;
                break;
            case R.id.nav_maps:
                fragment = mapsFragment;
                break;
            case R.id.nav_contacts:
                fragment = contactsFragment;
                break;
            case R.id.nav_info:
                fragment = infoFragment;
                break;
            case R.id.nav_sencor_and_camera:
                fragment = sensorAndCameraFragment;
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, fragment).commit();
            currentCheckedItem = id;
            currentFragment = fragment;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}
