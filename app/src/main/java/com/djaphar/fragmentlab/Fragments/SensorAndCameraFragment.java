package com.djaphar.fragmentlab.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.widget.Toast;

import com.djaphar.fragmentlab.MainActivity;
import com.djaphar.fragmentlab.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class SensorAndCameraFragment extends Fragment {

    static final int REQUEST_IMAGE_CAPTURE_AND_SAVE = 1;
    private static final int PERMISSION_REQUEST_CODE = 123;
    MainActivity mainActivity;
    ImageView testImageView;
    TextView sensorTV;
    Button takePictureButton, accelerometerButton;
    Uri outputFileUri;
    SensorManager sensorManager;
    Sensor sensorAccelerometer;
    Timer timer;
    TimerTask task;

    StringBuilder stringBuilder = new StringBuilder();
    float[] valuesAccelerometer = new float[3];
//    float[] valuesAccelerometerGravity = new float[3];
//    float[] valuesAccelerometerMotion = new float[3];

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sensor_and_camera, container, false);
        mainActivity = (MainActivity) getActivity();
        testImageView = rootView.findViewById(R.id.testImageView);
        sensorTV = rootView.findViewById(R.id.sensorTV);
        takePictureButton = rootView.findViewById(R.id.takePictureButton);
        accelerometerButton = rootView.findViewById(R.id.accelerometerButton);
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

        sensorManager = (SensorManager) mainActivity.getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        accelerometerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accelerometerButton.getText().toString().equals(getString(R.string.button_accelerometer_on))) {
                    accelerometerButton.setText(getString(R.string.button_accelerometer_off));
                    sensorManager.registerListener(listener, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
                    timer = new Timer();
                    task = new MyTimerTask();
                    timer.schedule(task, 0, 250);
                } else {
                    accelerometerButton.setText(getString(R.string.button_accelerometer_on));
                    sensorTV.setText(getString(R.string.accelerometer_text_view));
                    sensorManager.unregisterListener(listener);
                    timer.cancel();
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (accelerometerButton.getText().toString().equals(getString(R.string.button_accelerometer_off))) {
            sensorManager.unregisterListener(listener);
            timer.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometerButton.getText().toString().equals(getString(R.string.button_accelerometer_off))) {
            sensorManager.registerListener(listener, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
            task = new MyTimerTask();
            timer = new Timer();
            timer.schedule(task, 0, 250);
        }
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
            if (res != PackageManager.PERMISSION_GRANTED) {
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
        Toast.makeText(this.getContext(), getString(R.string.toast_picture_save), Toast.LENGTH_SHORT).show();
    }

    private void showInfo() {
        stringBuilder.setLength(0);

//        stringBuilder.append("Accelerometer: ").append(format(valuesAccelerometer))
//                .append("\nAccelerometer motion: ").append(format(valuesAccelerometerMotion))
//                .append("\nAccelerometer gravity: ").append(format(valuesAccelGravity));

        stringBuilder.append("Accelerometer: ").append(format(valuesAccelerometer));
        sensorTV.setText(stringBuilder);
    }

    private String format(float[] values) {
        return String.format("%1$.1f\t\t%2$.1f\t\t%3$.1f", values[0], values[1], values[2]);
    }

    SensorEventListener listener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
//            for (int i = 0; i < 3; i++) {
//                valuesAccelerometer[i] = sensorEvent.values[i];
//                valuesAccelerometerGravity[i] = (float) (0.1 * sensorEvent.values[i] + 0.9 * valuesAccelerometerGravity[i]);
//                valuesAccelerometerMotion[i] = sensorEvent.values[i] - valuesAccelerometerGravity[i];
//            }

            System.arraycopy(sensorEvent.values, 0, valuesAccelerometer, 0, 3);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) { }
    };

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showInfo();
                }
            });
        }
    }
}