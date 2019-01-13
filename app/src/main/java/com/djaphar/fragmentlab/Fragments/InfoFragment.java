package com.djaphar.fragmentlab.Fragments;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.djaphar.fragmentlab.R;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Objects;

public class InfoFragment extends Fragment {

    TextView deviceModelTV, androidVersionTV, RamTV, IPAddressTV;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_info, container, false);

        deviceModelTV = rootView.findViewById(R.id.deviceModelTV);
        androidVersionTV = rootView.findViewById(R.id.androidVersionTV);
        RamTV = rootView.findViewById(R.id.ramValueTV);
        IPAddressTV = rootView.findViewById(R.id.IPAddressTV);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        deviceModelTV.append(" " + Build.MODEL);
        androidVersionTV.append(" " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        ActivityManager manager = (ActivityManager) Objects.requireNonNull(getActivity()).getSystemService(Context.ACTIVITY_SERVICE);
        manager.getMemoryInfo(info);
        float ram = info.totalMem;
        RamTV.setText(String.format("%1$.2f", ram / 1024 / 1024 / 1024) + "GB");

        IPAddressTV.append(" " + getIpAddress());
    }

    public static String getIpAddress() {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intF = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddress = intF.getInetAddresses(); enumIpAddress.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) enumIpAddress.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }

        return null;
    }

}
