package com.example.tm18app.adapters;

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
import com.example.tm18app.databinding.RoomItemBinding;
import com.example.tm18app.fragment.ChatMessagesFragment;
import com.example.tm18app.model.Chat;
import com.example.tm18app.util.TimeUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.MyViewHolder> {

    private List<Chat> mChatsList;
    private NavController mNavController;

    public ChatsAdapter(List<Chat> list, NavController navController) {
        this.mChatsList = list;
        this.mNavController = navController;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RoomItemBinding itemBinding = RoomItemBinding.inflate(inflater, parent, false);
        return new MyViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final Chat chat = mChatsList.get(position);
        holder.name.setText(chat.getReceiverName());
        holder.lastTs.setText(TimeUtils.parseTimestampToLocaleDatetime(chat.getLastTimestamp()));
        if(chat.getProfilePic() != null)
            Picasso.get()
                    .load(chat.getProfilePic())
                    .resize(70, 70)
                    .centerCrop()
                    .into(holder.profilePic);
    }

    @Override
    public int getItemCount() {
        return mChatsList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView profilePic;
        TextView name;
        TextView lastTs;

        MyViewHolder(@NonNull final RoomItemBinding binding) {
            super(binding.getRoot());

            profilePic = binding.imgView;
            name = binding.namesChatTv;
            lastTs = binding.lastTsChat;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString(ChatMessagesFragment.ROOM_ID,
                            String.valueOf(mChatsList.get(getAdapterPosition()).getId()));
                    mNavController.navigate(R.id.action_chatsFragment_to_chatMessagesFragment, bundle);
                }
            });
        }
    }
}
