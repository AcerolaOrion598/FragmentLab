package com.djaphar.fragmentlab;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Objects;

public class GitRepoFragment extends Fragment {

    TextView titleTV;
    ListView listView;
    String title;
    String[] parsedJson;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_git_repo, container, false);


        listView = rootView.findViewById(R.id.list_view_main);
        titleTV = rootView.findViewById(R.id.titleTV);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleTV.append(" " + title);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(this.getContext()),
                android.R.layout.simple_list_item_1, parsedJson);
        listView.setAdapter(adapter);
    }

    public void getRepositories(String[] getParsedJson) {
        parsedJson = getParsedJson;
    }

    public void getTextForTV(String authUser) {
        title = authUser;
    }
}
