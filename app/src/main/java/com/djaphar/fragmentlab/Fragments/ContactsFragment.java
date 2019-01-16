package com.djaphar.fragmentlab.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.djaphar.fragmentlab.MainActivity;
import com.djaphar.fragmentlab.R;
import com.djaphar.fragmentlab.SupportClasses.ContactsRecyclerViewAdapter;
import com.tomash.androidcontacts.contactgetter.entity.ContactData;
import com.tomash.androidcontacts.contactgetter.main.contactsGetter.ContactsGetterBuilder;

import java.util.List;
import java.util.Objects;

public class ContactsFragment extends Fragment {

    MainActivity mainActivity;
    RecyclerView recyclerView;
    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        mainActivity = (MainActivity) getActivity();
        recyclerView = rootView.findViewById(R.id.contacts_recycler_view);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (hasPermissions()) {
            getContacts();
        } else {
            requestPerms();
        }
    }

    private boolean hasPermissions() {
        int res;
        String[] permissions = new String[] {Manifest.permission.READ_CONTACTS};

        for (String perms : permissions) {
            res = Objects.requireNonNull(this.getContext()).checkCallingOrSelfPermission(perms);
            if (res != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    private void requestPerms() {
        String[] permissions = new String[] {Manifest.permission.READ_CONTACTS};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        getContacts();
    }

    private void getContacts() {
        List<ContactData> namesNumbersList = new ContactsGetterBuilder(this.getContext()).onlyWithPhones().buildList();
        ContactsRecyclerViewAdapter adapter = new ContactsRecyclerViewAdapter(namesNumbersList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }
}
