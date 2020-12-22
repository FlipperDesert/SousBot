package com.videojames.sousbot.data;

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
import com.videojames.sousbot.R;

import java.util.ArrayList;


/**
    Contains the ArrayList of messages sent to and received from the chatbot. This class also
    handles the display of the messages in the message window. user is the check against the "user"
    number in ChatMessage and is set to be not the user number by default.
 */
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<ChatMessage> messages;
    int user = 100;

    public MessageAdapter(ArrayList<ChatMessage> messages) {
        this.messages = messages;
    }

    /**
        Checks the incoming message when there is activity for the user ID using getItemViewType()
        and sets the convertView using MessageViewHolder() based on whether it is from the user or
        the bot. If it is the user, the send_message is set on the right, else the bot's message
        is displayed on the left.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView;

        if (viewType == user) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.send_message,
                    parent, false);
        } else {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.receive_message,
                    parent, false);
        }

        return new MessageViewHolder(convertView);
    }

    /**
        Checks the user of the received message is not empty and is 1 (the user). If it is, return
        true. Else, return the current number for the bot.
     */
    @Override
    public int getItemViewType(int input) {

        ChatMessage message = messages.get(input);
        if (message.getUser() != null && message.getUser().equals("1")) {
            return user;
        } else {
            return input;
        }
    }

    /**
        Checks the message type and sets the content accordingly in the message window. If the
        message is text it takes the input from the user or Watson and places it into a
        MessageViewHolder.messageBody() with a HtmlCompat() to interpret the HTML tags from Watson.
        If the message is an image it uses Glide() and ImageView() to display the image URL
        received from Watson into MessageViewHolder.imageBody()
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        String type = message.getType();
        if (type.equalsIgnoreCase("Text")){

            ((MessageViewHolder) holder).messageBody.setText(HtmlCompat.fromHtml(message.getText(),
                    0));
        }
        else if (type.equalsIgnoreCase("Image")) {

            ((MessageViewHolder) holder).messageBody.setVisibility(View.GONE);
            ImageView messageImage = ((MessageViewHolder) holder).imageBody;

            Glide.with(messageImage.getContext()).load(message.getUrl()).into(messageImage);

        }
    }

    /**
     * Returns the size of messages
     */
    @Override
    public int getItemCount() {
        return messages.size();
    }


}

/**
    Sets the message and image bodies for the received message using the RecyclerView superclass.
    itemView finds the message_body and image fields in our drawable folder.
 */
class MessageViewHolder extends RecyclerView.ViewHolder {
    // public View avatar;
    public TextView messageBody;
    public ImageView imageBody;

    public MessageViewHolder (View view) {
        super(view);

        messageBody = itemView.findViewById(R.id.message_body);
        imageBody = itemView.findViewById(R.id.image);
    }
}
