package com.djaphar.fragmentlab.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class SensorAndScreenshotFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 123;
    MainActivity mainActivity;
    ImageView screenshotImageView;
    TextView sensorTV;
    Button accelerometerButton, takeScreenshotButton, saveScreenshotButton;
    SensorManager sensorManager;
    Sensor sensorAccelerometer;
    Timer timer;
    TimerTask task;
    Bitmap screenshotBitmap;
    boolean viewCreated = false;

    StringBuilder stringBuilder = new StringBuilder();
    float[] valuesAccelerometer = new float[3];
//    float[] valuesAccelerometerGravity = new float[3];
//    float[] valuesAccelerometerMotion = new float[3];

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sensor_and_screenshot, container, false);
        mainActivity = (MainActivity) getActivity();
        screenshotImageView = rootView.findViewById(R.id.screenshotImageView);
        sensorTV = rootView.findViewById(R.id.sensorTV);
        accelerometerButton = rootView.findViewById(R.id.accelerometerButton);
        takeScreenshotButton = rootView.findViewById(R.id.takeScreenshotButton);
        saveScreenshotButton = rootView.findViewById(R.id.saveScreenshotButton);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        viewCreated = true;
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
                    timer.schedule(task, 0, 125);
                } else {
                    accelerometerButton.setText(getString(R.string.button_accelerometer_on));
                    sensorTV.setText(getString(R.string.accelerometer_text_view));
                    sensorManager.unregisterListener(listener);
                    timer.cancel();
                }
            }
        });

        takeScreenshotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeScreenshot();
            }
        });

        saveScreenshotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasPermissions()) {
                    saveScreenshot();
                } else {
                    requestPerms();
                }
            }
        });
    }

    @Override
    public void onPause() {
        if (accelerometerButton.getText().toString().equals(getString(R.string.button_accelerometer_off))) {
            sensorManager.unregisterListener(listener);
            timer.cancel();
        }
        super.onPause();
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

    @Override
    public void onDetach() {
        super.onDetach();
        screenshotBitmap = null;
    }

    public void takeScreenshot() {
        View view = mainActivity.getWindow().getDecorView().getRootView();
        view.setDrawingCacheEnabled(true);
        screenshotBitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        screenshotImageView.setImageBitmap(screenshotBitmap);
    }

    public void saveScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy_MM-dd_hh:mm:ss", now);

        if (screenshotBitmap != null) {
            try {
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                        + "/" + now + ".jpg";
                File screenshotFile = new File(path);

                FileOutputStream outputStream = new FileOutputStream(screenshotFile);
                int quality = 100;
                screenshotBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                outputStream.flush();
                outputStream.close();
                Toast.makeText(this.getContext(), getString(R.string.toast_screenshot_save), Toast.LENGTH_SHORT).show();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this.getContext(), getString(R.string.toast_screenshot_empty), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasPermissions() {
        int res;
        String[] permissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        for (String perms : permissions) {
            res = Objects.requireNonNull(this.getContext()).checkCallingOrSelfPermission(perms);
            if (res != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    private void requestPerms() {
        String[] permissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        saveScreenshot();
    }

    private void showInfo() {
        stringBuilder.setLength(0);

//        stringBuilder.append("Accelerometer: ").append(format(valuesAccelerometer))
//                .append("\nAccelerometer motion: ").append(format(valuesAccelerometerMotion))
//                .append("\nAccelerometer gravity: ").append(format(valuesAccelerometerGravity));

        stringBuilder.append(getString(R.string.accelerometer_text_view)).append(" ").append(format(valuesAccelerometer));
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
