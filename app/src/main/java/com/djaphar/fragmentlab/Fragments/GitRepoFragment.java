package com.djaphar.fragmentlab.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.djaphar.fragmentlab.R;
import com.djaphar.fragmentlab.SupportClasses.RepoRecyclerViewAdapter;

import java.util.Objects;

public class GitRepoFragment extends Fragment {

    RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_git_repo, container, false);
        recyclerView = rootView.findViewById(R.id.repo_recycler_view);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] parsedJson = Objects.requireNonNull(getArguments()).getStringArray("Repos");
        String avatarURL = getArguments().getString("Url");
        RepoRecyclerViewAdapter adapter = new RepoRecyclerViewAdapter(parsedJson, avatarURL, this.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }
}
