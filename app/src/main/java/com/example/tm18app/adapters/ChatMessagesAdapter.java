package com.example.tm18app.adapters;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.model.ChatMessage;
import com.example.tm18app.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatMessagesAdapter  extends RecyclerView.Adapter<ChatMessagesAdapter.MyViewHolder> {

    private static final int OTHER = 0;
    private static final int ME = 1;

    private List<ChatMessage> mChatMessagesList ;
    private SharedPreferences prefs;

    public ChatMessagesAdapter(List<ChatMessage> list, SharedPreferences prefs) {
        this.mChatMessagesList = list;
        this.prefs = prefs;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if(viewType == OTHER)
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_message_other, parent, false);
        else if(viewType == ME)
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_message_own, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        int currentUserId = prefs.getInt(Constant.USER_ID, 0);
        ChatMessage message = mChatMessagesList.get(position);
        if(message.getSenderId() != currentUserId)
            return OTHER;
        else
            return ME;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ChatMessage chatMessage = mChatMessagesList.get(position);
        holder.contentTs.setText(TimeUtils.parseTimestampToLocaleTime(chatMessage.getTimestamp()));
        holder.contentText.setText(chatMessage.getText());
    }

    @Override
    public int getItemCount() {
        return mChatMessagesList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView contentText;
        TextView contentTs;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            contentText = itemView.findViewById(R.id.contentText);
            contentTs = itemView.findViewById(R.id.contentTimestamp);
        }
    }
}
