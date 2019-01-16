package com.djaphar.fragmentlab.SupportClasses;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.djaphar.fragmentlab.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class RepoRecyclerViewAdapter extends RecyclerView.Adapter<RepoRecyclerViewAdapter.ViewHolder>{

    private String[] myRepositories;
    private String myAvatarURL;
    private Context myContext;

    public RepoRecyclerViewAdapter(String[] repositories, String avatarURL, Context context) {
        myRepositories = repositories;
        myAvatarURL = avatarURL;
        myContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_repo_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Glide.with(myContext).asBitmap().load(myAvatarURL).into(viewHolder.avatar);
        viewHolder.repository.setText(myRepositories[i]);
    }

    @Override
    public int getItemCount() {
        return myRepositories.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView avatar;
        TextView repository;
        RelativeLayout parentLayout;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            repository = itemView.findViewById(R.id.repository);
            parentLayout = itemView.findViewById(R.id.parent_layout_repo);
        }
    }
}
