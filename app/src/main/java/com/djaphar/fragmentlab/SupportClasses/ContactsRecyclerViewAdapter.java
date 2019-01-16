package com.djaphar.fragmentlab.SupportClasses;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.djaphar.fragmentlab.R;
import com.tomash.androidcontacts.contactgetter.entity.ContactData;

import java.util.List;

public class ContactsRecyclerViewAdapter extends RecyclerView.Adapter<ContactsRecyclerViewAdapter.ViewHolder> {

    private List<ContactData> myNamesNumbersList;

    public ContactsRecyclerViewAdapter(List<ContactData> namesNumbersList) {
        myNamesNumbersList = namesNumbersList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_contacts_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.contactName.setText(myNamesNumbersList.get(i).getCompositeName());
        viewHolder.number.setText(myNamesNumbersList.get(i).getPhoneList().get(0).getMainData());
    }

    @Override
    public int getItemCount() {
        return myNamesNumbersList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView contactName, number;
        RelativeLayout parentLayout;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            contactName = itemView.findViewById(R.id.contact_name);
            number = itemView.findViewById(R.id.number);
            parentLayout = itemView.findViewById(R.id.parent_layout_contacts);
        }
    }
}
