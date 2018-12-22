package com.djaphar.fragmentlab;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    MainActivity mainActivity;
    GoogleMap gMap;
    Context thisFragment;
    Task location;
    final float defaultZoom = 15f;
    LatLng latLng;
    Polyline currentPolyline;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);
        mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        thisFragment = mainActivity.mapsFragment.getContext();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        Objects.requireNonNull(supportMapFragment).getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        getDeviceLocation();
        if (ActivityCompat.checkSelfPermission(thisFragment, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(thisFragment,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        gMap.setMyLocationEnabled(true);
    }

    public void getDeviceLocation() {
        FusedLocationProviderClient fusedLocationProviderClient =
                        LocationServices.getFusedLocationProviderClient(thisFragment);
        try {
            location = fusedLocationProviderClient.getLastLocation();
            //location = fusedLocationProviderClient.requestLocationUpdates();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Location currentLocation = (Location) task.getResult();
                        latLng = new LatLng(Objects.requireNonNull(currentLocation).getLatitude(), currentLocation.getLongitude());
                        moveCameraAndSetMarkers(latLng, defaultZoom);
                    } else {
                        Toast.makeText(thisFragment, "Невозможно получить текущее местоположение",
                                                                                    Toast.LENGTH_LONG).show();
                        latLng = new LatLng(0, 0);
                    }
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void moveCameraAndSetMarkers(LatLng latLng, float zoom) {
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        //Marker markerHome = gMap.addMarker(new MarkerOptions().position(new LatLng(55.891765, 37.725044)).title("Дом"));
        Marker markerInstitute = gMap.addMarker(new MarkerOptions().position(new LatLng(55.794317, 37.701400)).title("Универ"));
        Marker markerMe = gMap.addMarker(new MarkerOptions().position(latLng).title("Я тут"));
        buildRoute(markerInstitute, markerMe);
    }

    private void buildRoute(Marker markerStart, Marker markerFinish) {
        String url = getUrl(markerStart.getPosition(), markerFinish.getPosition(), "driving");
        //Строим маршрут
    }

    private String getUrl(LatLng start, LatLng finish, String directionMode) {
        String origin = "origin=" + start.latitude + "," + start.longitude;
        String destination = "destination=" + finish.latitude + "," + finish.longitude;
        String mode = "mode=" + directionMode;
        String params = origin + "&" + destination + "&" + mode;

        return "https://maps.googleapis.com/maps/api/directions/json?" + params + "&key=" + getString(R.string.mapDirectionsKey);
    }
}
