package com.example.amitrai.chatapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by amitrai on 28/12/16.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder>{

    private List<Message> list_messages;


    public MessageAdapter(List<Message> list_messages){
        this.list_messages = list_messages;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.message_view, parent, false);

        // Return a new holder instance
        MyViewHolder viewHolder = new MyViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.txt_message.setText(list_messages.get(position).getUserName() +" : "+
                list_messages.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        return list_messages.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView txt_message = null;

        public MyViewHolder(View itemView) {
            super(itemView);
            txt_message = (TextView) itemView.findViewById(R.id.txt_message);
        }
    }
}
