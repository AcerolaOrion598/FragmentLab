package com.djaphar.fragmentlab;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.djaphar.fragmentlab.Fragments.ContactsFragment;
import com.djaphar.fragmentlab.Fragments.GitRepoFragment;
import com.djaphar.fragmentlab.Fragments.InfoFragment;
import com.djaphar.fragmentlab.Fragments.MapsFragment;
import com.djaphar.fragmentlab.Fragments.SensorAndCameraFragment;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Fragment gitRepoFragment, mapsFragment, contactsFragment, infoFragment, sensorAndCameraFragment, currentFragment;
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
        String owner = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).getString("Owner"));
        ownerTextView.setText(owner);
        emailTextView.setText(getIntent().getExtras().getString("Email"));
        new DownloadAvatarTask((ImageView) headerView.findViewById(R.id.avatarImageView))
                .execute(getIntent().getExtras().getString("Avatar URL"));

        gitRepoFragment = new GitRepoFragment();
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putStringArray("Repos", Objects.requireNonNull(getIntent().getExtras()).getStringArray("Repositories"));
        fragmentArgs.putString("Own", owner);
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

    private class DownloadAvatarTask extends AsyncTask<String, Void, Bitmap> {

        ImageView avatarImageView;

        DownloadAvatarTask(ImageView imageView) {
            avatarImageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap avatar = null;

            try {
                InputStream in = new java.net.URL(urlDisplay).openStream();
                avatar = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

            return avatar;
        }

        protected void onPostExecute(Bitmap resultBitmap) {
            avatarImageView.setImageBitmap(resultBitmap);
        }
    }
}
