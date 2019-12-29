package com.example.tm18app.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.R;
import com.example.tm18app.databinding.ChatRoomItemBinding;
import com.example.tm18app.fragment.ChatMessagesFragment;
import com.example.tm18app.model.ChatRoom;
import com.example.tm18app.util.TimeUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adapter for the {@link ChatRoom} {@link RecyclerView}
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.MyViewHolder> {

    private List<ChatRoom> mChatsList;
    private NavController mNavController;
    private Context mContext;
    private int mProfilePicDimen;


    public ChatsAdapter(List<ChatRoom> list, NavController navController, Context context) {
        this.mChatsList = list;
        this.mNavController = navController;
        this.mContext = context;
        mProfilePicDimen = mContext.getResources().getInteger(R.integer.thumbnail_profile_pic);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ChatRoomItemBinding itemBinding = ChatRoomItemBinding.inflate(inflater, parent, false);
        return new MyViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final ChatRoom chatRoom = mChatsList.get(position);
        holder.name.setText(chatRoom.getReceiverName());
        holder.lastTs.setText(TimeUtils.parseTimestampToLocaleDatetime(chatRoom.getLastTimestamp()));
        holder.name.setTypeface(null, Typeface.NORMAL);
        holder.lastTs.setTypeface(null, Typeface.NORMAL);
        holder.newMessages.setVisibility(View.GONE);
        if(chatRoom.getNewMessageInRoom() != 0){
            // if the chatroom has new messages, display messages stating that
            holder.name.setTypeface(null, Typeface.BOLD);
            holder.lastTs.setTypeface(null, Typeface.BOLD);
            holder.newMessages.setVisibility(View.VISIBLE);
        }
        if(chatRoom.getProfilePic() != null){
            holder.profilePic.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(chatRoom.getProfilePic())
                    .resize(mProfilePicDimen, mProfilePicDimen)
                    .centerCrop()
                    .into(holder.profilePic);
        }
        else
            holder.profilePic.setImageDrawable(mContext.getDrawable(R.drawable.ic_person_black_24dp));
    }

    @Override
    public int getItemCount() {
        return mChatsList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView profilePic;
        TextView name;
        TextView lastTs;
        TextView newMessages;

        MyViewHolder(@NonNull final ChatRoomItemBinding binding) {
            super(binding.getRoot());

            profilePic = binding.imgView;
            name = binding.namesChatTv;
            lastTs = binding.lastTsChat;
            newMessages = binding.newMessagesTv;

            binding.getRoot().setOnClickListener(view -> {
                Bundle bundle = new Bundle();
                bundle.putString(ChatMessagesFragment.ROOM_ID,
                        String.valueOf(mChatsList.get(getAdapterPosition()).getId()));
                bundle.putString(ChatMessagesFragment.ROOM_NAME,
                        mChatsList.get(getAdapterPosition()).getRoom());
                bundle.putString(ChatMessagesFragment.TO_ID,
                        String.valueOf(mChatsList.get(getAdapterPosition()).getReceiverId()));
                bundle.putString(ChatMessagesFragment.TO_NAME,
                        mChatsList.get(getAdapterPosition()).getReceiverName());
                bundle.putString(ChatMessagesFragment.PROFILE_PIC,
                        mChatsList.get(getAdapterPosition()).getProfilePic());
                mNavController.navigate(R.id.action_chatsFragment_to_chatMessagesFragment, bundle);
            });
        }
    }
}
