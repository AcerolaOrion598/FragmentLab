package com.djaphar.fragmentlab;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;


public class SensorAndCameraFragment extends Fragment {

    static final int REQUEST_IMAGE_CAPTURE_AND_SAVE = 1;
    private static final int PERMISSION_REQUEST_CODE = 123;
    MainActivity mainActivity;
    ImageView testImageView;
    TextView batteryTV;
    Button takePictureButton;
    Uri outputFileUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sensor_and_camera, container, false);
        mainActivity = (MainActivity) getActivity();
        testImageView = rootView.findViewById(R.id.testImageView);
        batteryTV = rootView.findViewById(R.id.batteryTV);
        takePictureButton = rootView.findViewById(R.id.takePictureButton);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasPermissions()) {
                    takeAndSavePictureIntent();
                } else {
                    requestPerms();
                }
            }
        });

        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mainActivity.registerReceiver(null, batteryFilter);
        String status = "Заряд батареи: " + Integer.toString(Objects.requireNonNull(batteryStatus)
                                                            .getIntExtra("level", -1)) + "%";
        batteryTV.setText(status);
    }

    private void takeAndSavePictureIntent() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDirectory =  new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                                                                                                imageFileName + ".jpg");
        outputFileUri = Uri.fromFile(storageDirectory);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE_AND_SAVE);
    }

    private boolean hasPermissions() {
        int res;
        String[] permissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        for (String perms : permissions) {
            res = Objects.requireNonNull(this.getContext()).checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }

        return true;
    }

    private void requestPerms() {
        String[] permissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        takeAndSavePictureIntent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        testImageView.setImageURI(outputFileUri);
    }
}
