package com.djaphar.fragmentlab.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
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
import android.widget.ArrayAdapter;
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
    Task location;
    final float defaultZoom = 15f;
    LatLng latLng;
    private List<Polyline> polylines;
    private static final int PERMISSION_REQUEST_CODE = 123;

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

        String[] data = {getString(R.string.mode_walking), getString(R.string.mode_driving),
                                    getString(R.string.mode_transit), getString(R.string.mode_biking)};
        // TODO Адекватный внешний вид спиннера
        //SimpleAdapter adapter = new SimpleAdapter(thisFragment, data, R.layout.travel_mode_spinner_pattern, new int[] {R.id.spinnerTravelModeTV});
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(thisFragment, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        spinnerTravelMode.setAdapter(adapter);
        spinnerTravelMode.setPrompt(getString(R.string.travel_mode_spinner_title));

        spinnerTravelMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        mode = AbstractRouting.TravelMode.WALKING;
                        break;
                    case 1:
                        mode = AbstractRouting.TravelMode.DRIVING;
                        break;
                    case 2:
                        mode = AbstractRouting.TravelMode.TRANSIT;
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
        FusedLocationProviderClient fusedLocationProviderClient =
                        LocationServices.getFusedLocationProviderClient(thisFragment);
        try {
            // TODO: Изменить lastLocation на запрос реального местоположения
            location = fusedLocationProviderClient.getLastLocation();
            //location = fusedLocationProviderClient.requestLocationUpdates();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    markerHome = gMap.addMarker(new MarkerOptions().position(new LatLng(55.891765, 37.725044))
                                                                                .title(getString(R.string.marker_home)));
                    markerInst = gMap.addMarker(new MarkerOptions().position(new LatLng(55.794317, 37.701400))
                                                                                .title(getString(R.string.marker_inst)));
                    if (task.isSuccessful()) {
                        Location currentLocation = (Location) task.getResult();
                        latLng = new LatLng(Objects.requireNonNull(currentLocation).getLatitude(), currentLocation.getLongitude());
                        moveCameraAndSetMarkerMe(latLng, defaultZoom);
                    } else {
                        Toast.makeText(thisFragment, getString(R.string.toast_location_failure), Toast.LENGTH_LONG).show();
                        latLng = new LatLng(0, 0);
                    }
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void moveCameraAndSetMarkerMe(LatLng latLng, float zoom) {
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
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
            polyOptions.color(R.color.primary_dark_material_light);
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

//    public void erasePolylines(){
//        for (Polyline line : polylines) {
//            line.remove();
//        }
//        polylines.clear();
//    }
}
