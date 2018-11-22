package com.djaphar.fragmentlab;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GitRepoFragment extends Fragment {

    TextView textView;
    String newJson;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_git_repo, container, false);

        textView = rootView.findViewById(R.id.textView2);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTextInTV(newJson);
    }

    public void getTextForTV(String getJson) {
        newJson = getJson;
    }

    public void setTextInTV(String setJson) {
        textView.setText(setJson);
    }
}
