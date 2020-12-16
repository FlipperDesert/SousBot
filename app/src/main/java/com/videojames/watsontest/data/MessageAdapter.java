package com.videojames.watsontest.data;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.videojames.watsontest.R;

import java.util.ArrayList;


public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected Activity activity;
    ArrayList<ChatMessage> messages;
    int user = 100;

    public MessageAdapter(ArrayList<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView;

        if (viewType == user) { // this message was sent by us so let's create a basic chat bubble on the right
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.send_message, parent, false);
        } else { // this message was sent by someone else so let's create an advanced chat bubble on the left
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.receive_message, parent, false);
        }

        return new MessageViewHolder(convertView);
    }

    @Override
    public int getItemViewType(int input) {

        ChatMessage message = messages.get(input);
        if (message.getUser() != null && message.getUser().equals("1")) {
            return user;
        } else {
            return input;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        String type = message.getType();
        if (type.equalsIgnoreCase("Text")){

            ((MessageViewHolder) holder).messageBody.setText(HtmlCompat.fromHtml(message.getText(), 0));
        }
        else if (type.equalsIgnoreCase("Image")) {

            ((MessageViewHolder) holder).messageBody.setVisibility(View.GONE);
            ImageView messageImage = ((MessageViewHolder) holder).imageBody;

            Glide.with(messageImage.getContext()).load(message.getUrl()).into(messageImage);

        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


}

class MessageViewHolder extends RecyclerView.ViewHolder {
    public View avatar;
    public TextView messageBody;
    public ImageView imageBody;

    public MessageViewHolder (View view) {
        super(view);

        messageBody = (TextView) itemView.findViewById(R.id.message_body);
        imageBody = (ImageView) itemView.findViewById(R.id.image);
    }
}
