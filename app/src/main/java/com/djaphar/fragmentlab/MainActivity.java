package com.djaphar.fragmentlab;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.djaphar.fragmentlab.Fragments.ContactsFragment;
import com.djaphar.fragmentlab.Fragments.GitRepoFragment;
import com.djaphar.fragmentlab.Fragments.InfoFragment;
import com.djaphar.fragmentlab.Fragments.MapsFragment;
import com.djaphar.fragmentlab.Fragments.SensorAndScreenshotFragment;

import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Fragment gitRepoFragment, mapsFragment, contactsFragment, infoFragment, sensorAndScreenshotFragment, currentFragment;
    int currentCheckedItem;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        TextView ownerTextView, emailTextView;

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

        View headerView = navigationView.getHeaderView(0);
        ownerTextView = headerView.findViewById(R.id.ownerTextView);
        emailTextView = headerView.findViewById(R.id.emailTextView);

        ownerTextView.setText(Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).getString("Owner")));
        emailTextView.setText(getIntent().getExtras().getString("Email"));

        String avatarURL = getIntent().getExtras().getString("Avatar URL");
        ImageView avatarImageView = headerView.findViewById(R.id.avatarImageView);
        Glide.with(this).asBitmap().load(avatarURL).into(avatarImageView);

        gitRepoFragment = new GitRepoFragment();
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putStringArray("Repos", Objects.requireNonNull(getIntent().getExtras()).getStringArray("Repositories"));
        fragmentArgs.putString("Url", avatarURL);
        gitRepoFragment.setArguments(fragmentArgs);

        mapsFragment = new MapsFragment();
        contactsFragment = new ContactsFragment();
        infoFragment = new InfoFragment();
        sensorAndScreenshotFragment = new SensorAndScreenshotFragment();

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
            case R.id.nav_sensor_and_screenshot:
                fragment = sensorAndScreenshotFragment;
                break;
            case R.id.nav_logout:
                logout();
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

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.logout_dialog_title));
        builder.setMessage(getString(R.string.logout_dialog_message));
        builder.setNegativeButton(getString(R.string.logout_dialog_button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { }
        });
        builder.setPositiveButton(getString(R.string.logout_dialog_button_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.onBackPressed();
            }
        });
        builder.setCancelable(true);

        builder.show();
    }
}
