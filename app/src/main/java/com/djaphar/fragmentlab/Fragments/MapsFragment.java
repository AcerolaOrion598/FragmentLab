package com.djaphar.fragmentlab.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.djaphar.fragmentlab.MainActivity;
import com.djaphar.fragmentlab.R;
import com.djaphar.fragmentlab.SupportClasses.TravelModeSpinnerAdapter;
import com.djaphar.fragmentlab.SupportClasses.TravelModeSpinnerItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapsFragment extends Fragment implements OnMapReadyCallback, RoutingListener {

    MainActivity mainActivity;
    Button buttonMe, buttonInst;
    Spinner spinnerTravelMode;
    AbstractRouting.TravelMode mode;
    Marker markerHome, markerInst, markerMe;
    GoogleMap gMap;
    Context thisFragment;
    LocationManager locationManager;
    private List<Polyline> polylines;
    private static final int PERMISSION_REQUEST_CODE = 123;
    boolean justOpened = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);
        mainActivity = (MainActivity) getActivity();
        thisFragment = this.getContext();
        buttonMe = rootView.findViewById(R.id.buttonMe);
        buttonInst = rootView.findViewById(R.id.buttonInst);
        spinnerTravelMode = rootView.findViewById(R.id.spinnerTravelMode);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        Objects.requireNonNull(supportMapFragment).getMapAsync(this);
        polylines = new ArrayList<>();

        ArrayList<TravelModeSpinnerItem> travelModeList = new ArrayList<>();
        travelModeList.add(new TravelModeSpinnerItem(getString(R.string.mode_driving)));
        travelModeList.add(new TravelModeSpinnerItem(getString(R.string.mode_transit)));
        travelModeList.add(new TravelModeSpinnerItem(getString(R.string.mode_walking)));
        travelModeList.add(new TravelModeSpinnerItem(getString(R.string.mode_biking)));
        TravelModeSpinnerAdapter adapter = new TravelModeSpinnerAdapter(thisFragment, travelModeList);
        spinnerTravelMode.setAdapter(adapter);

        spinnerTravelMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int selectedPosition, long l) {
                switch (selectedPosition) {
                    case 0:
                        mode = AbstractRouting.TravelMode.DRIVING;
                        break;
                    case 1:
                        mode = AbstractRouting.TravelMode.TRANSIT;
                        break;
                    case 2:
                        mode = AbstractRouting.TravelMode.WALKING;
                        break;
                    case 3:
                        mode = AbstractRouting.TravelMode.BIKING;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        buttonMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildRouteFromMarker(markerMe);
            }
        });

        buttonInst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildRouteFromMarker(markerInst);
            }
        });

        justOpened = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        getDeviceLocation();
    }

    private void buildRouteFromMarker(Marker markerStart) {
        if (mode != null) {
            buildRoute(markerStart, markerHome, mode);
        } else {
            Toast.makeText(thisFragment, getString(R.string.mode_null), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean hasPermissions() {
        int res;
        String[] permissions = new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION};

        for (String perms : permissions) {
            res = Objects.requireNonNull(this.getContext()).checkCallingOrSelfPermission(perms);
            if (res != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    private void requestPerms() {
        String[] permissions = new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        getDeviceLocation();
        if (ActivityCompat.checkSelfPermission(thisFragment, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(thisFragment,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        gMap.getUiSettings().setMyLocationButtonEnabled(false);
        gMap.setMyLocationEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        LatLng moscowLatLng = new LatLng(55.754070, 37.619924);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(moscowLatLng, 9.8f));
        markerHome = gMap.addMarker(new MarkerOptions().position(new LatLng(55.891765, 37.725044))
                .title(getString(R.string.marker_home)));
        markerInst = gMap.addMarker(new MarkerOptions().position(new LatLng(55.794317, 37.701400))
                .title(getString(R.string.marker_inst)));
        if (hasPermissions()) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(thisFragment, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(thisFragment,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            gMap.getUiSettings().setMyLocationButtonEnabled(false);
            gMap.setMyLocationEnabled(true);
        } else {
            requestPerms();
        }
    }

    public void getDeviceLocation() {
        locationManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void moveCameraAndSetMarkerMe(LatLng latLng, float zoom) {
        if (justOpened) {
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
            justOpened = false;
        }
        markerMe = gMap.addMarker(new MarkerOptions().position(latLng).title(getString(R.string.marker_me)));
    }

    private void buildRoute(Marker markerStart, Marker markerFinish, AbstractRouting.TravelMode mode) {
        Routing routing = new Routing.Builder()
                .travelMode(mode)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(markerStart.getPosition(), markerFinish.getPosition())
                .key(getString(R.string.mapDirectionsKey))
                .build();
        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(thisFragment, getString(R.string.toast_route_failure_known) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(thisFragment, getString(R.string.toast_route_failure_unknown), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        for (int i = 0; i <route.size(); i++) {

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(R.color.colorPrimaryDark);
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = gMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(thisFragment, getString(R.string.toast_connection_success), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() { }

    @Override
    public void onRoutingCancelled() { }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (markerMe != null) {
                markerMe.remove();
            } else {
                buttonMe.setEnabled(true);
            }
            moveCameraAndSetMarkerMe(myLatLng, 15f);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) { }

        @Override
        public void onProviderEnabled(String s) { }

        @Override
        public void onProviderDisabled(String s) { }
    };
}
