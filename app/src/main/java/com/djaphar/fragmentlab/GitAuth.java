package com.djaphar.fragmentlab;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class GitAuth extends Fragment{

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_git_auth, container, false);

        final Fragment fragment = new GitRepoFragment();
        Button button = rootView.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "kekekeke", Toast.LENGTH_SHORT).show();
                getFragmentManager().beginTransaction().replace(R.id.main_fragment, fragment).commit();
            }
        });

        return rootView;
    }
}
