package com.example.tm18app.adapters;

import android.content.Context;
import android.content.SharedPreferences;
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

import java.util.List;

/**
 * Adapter for the {@link ChatMessage} {@link RecyclerView}
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class ChatMessagesAdapter  extends RecyclerView.Adapter<ChatMessagesAdapter.MyViewHolder> {

    private static final int OTHER = 0; // represents the other user
    private static final int ME = 1; // represents the currently logged in user

    private List<ChatMessage> mChatMessagesList ;
    private SharedPreferences mPrefs;
    private Context mContext;

    public ChatMessagesAdapter(List<ChatMessage> list, SharedPreferences prefs, Context context) {
        this.mChatMessagesList = list;
        this.mPrefs = prefs;
        this.mContext = context;
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
        int currentUserId = mPrefs.getInt(Constant.USER_ID, 0);
        ChatMessage message = mChatMessagesList.get(position);
        if(message.getSenderId() != currentUserId)
            return OTHER;
        else
            return ME;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ChatMessage chatMessage = mChatMessagesList.get(position);
        holder.contentTs.setText(TimeUtils
                .parseTimestampToLocaleDatetime(chatMessage.getTimestamp(), mContext));
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
