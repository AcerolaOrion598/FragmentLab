package com.djaphar.fragmentlab;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;


public class SensorAndCameraFragment extends Fragment {

    MainActivity mainActivity;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_SAVE_PHOTO = 2;
    ImageView testImageView;
    private static final int PERMISSION_REQUEST_CODE = 123;
    Button takePictureButton, savePictureButton;
    String currentPhotoPath;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sensor_and_camera, container, false);
        mainActivity = (MainActivity) getActivity();
        testImageView = rootView.findViewById(R.id.testImageView);
        takePictureButton = rootView.findViewById(R.id.takePictureButton);
        savePictureButton = rootView.findViewById(R.id.savePictureButton);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasPermissions()) {
                    takePictureIntent();
                } else {
                    requestPerms();
                }
            }
        });

        savePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePictureIntent();
            }
        });
    }

    private void takePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mainActivity.getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void savePictureIntent() {
        Intent savePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (savePictureIntent.resolveActivity(mainActivity.getPackageManager()) != null) {
            File photo = null;
            try {
                photo = createImageFile();
            } catch (IOException e) {
                Toast.makeText(this.getContext(), "Не удалось сохранить фото", Toast.LENGTH_SHORT).show();
            }

            if (photo != null) {
                savePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
                startActivityForResult(savePictureIntent, REQUEST_SAVE_PHOTO);
            }
        }
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
        takePictureIntent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                testImageView.setImageBitmap(imageBitmap);
                savePictureButton.setEnabled(true);
            }
        } else if (requestCode == REQUEST_SAVE_PHOTO && resultCode == Activity.RESULT_OK) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(currentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            mainActivity.sendBroadcast(mediaScanIntent);
            Toast.makeText(this.getContext(), "Фото сохранено в галерею", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);
        currentPhotoPath = "file:" + image.getAbsolutePath();

        return image;
    }
}
