package com.tgc.researchchat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class ChatAdapterRecycler extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int LATEST_TYPE_MESSAGE_SENT = 3;
    private static final int LATEST_TYPE_MESSAGE_RECEIVED = 4;
    private Context context;
    private ArrayList<Message> arrayList;

    ChatAdapterRecycler(Context context, ArrayList<Message> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = arrayList.get(position);

        if (message.isSent() && position != arrayList.size() - 1)
            return VIEW_TYPE_MESSAGE_SENT;
        else if (!message.isSent() && position != arrayList.size() - 1)
            return VIEW_TYPE_MESSAGE_RECEIVED;
        else if (message.isSent() && position == arrayList.size() - 1)
            return LATEST_TYPE_MESSAGE_SENT;
        else if (!message.isSent() && position == arrayList.size() - 1)
            return LATEST_TYPE_MESSAGE_RECEIVED;
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_from_bottom);

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        } else if (viewType == LATEST_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            view.startAnimation(animation);
            return new SentMessageHolder(view);
        } else if (viewType == LATEST_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            view.startAnimation(animation);
            return new ReceivedMessageHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = arrayList.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
            case LATEST_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case LATEST_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
                break;

        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        ImageView messageImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            messageImage = itemView.findViewById(R.id.received_image);
        }

        void bind(Message message) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            String currentDateTimeString = sdf.format(message.getTime());

            messageText.setText(message.getMessage());
            timeText.setText(currentDateTimeString);
            Log.d(TAG, "bind: " + message.getMessage());
            if (message.getMessage().contains("New File Received: ") &&
                    (message.getMessage().contains("png") ||
                            message.getMessage().contains("jpg") ||
                            message.getMessage().contains("jpeg"))) {
                String[] fileName = message.getMessage().split(":");
                Log.d(TAG, "bind: Received Message" + fileName[1]);
                StringBuilder stringBuilder = new StringBuilder(fileName[1]);
                stringBuilder.deleteCharAt(0);
                String path = stringBuilder.toString();
                String directory = context.getObbDir() + "/downloadFolder/" + path;
                Log.d(TAG, "bind: Print Directory:" + directory);
                File imgFile = new File(directory);
                if (imgFile.exists()) {
                    Log.d(TAG, "bind: +Yeah Exists");
                    Glide.with(context)
                            .load(directory)
                            .override(500, 500)
                            .into(messageImage);
                }
            }
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        ImageView messageImage;
        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.send_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            messageImage = itemView.findViewById(R.id.sent_image);
        }

        void bind(Message message) {
            String newMessage = message.getMessage();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            String currentDateTimeString = sdf.format(message.getTime());

            if (message.getMessage().contains("New File Sent: ") &&
                    (message.getMessage().contains("png") ||
                            message.getMessage().contains("jpg") ||
                            message.getMessage().contains("jpeg"))) {
                String[] fileName = message.getMessage().split(":");
                Log.d(TAG, "bind: Received Message" + fileName[2]);
                String[] newStringArr = newMessage.split(":");
                newMessage = newStringArr[0] + newStringArr[1];
                Log.d(TAG, "bind: Directory:" + newMessage);
                String directory = newStringArr[2];
                Log.d(TAG, "bind: Print Directory:" + directory);
                File imgFile = new File(directory);
                if (imgFile.exists()) {
                    Log.d(TAG, "bind: +Yeah Exists");
                    Glide.with(context)
                            .load(directory)
                            .override(500, 500)
                            .into(messageImage);
                }
            }
            messageText.setText(newMessage);
            timeText.setText(currentDateTimeString);
        }
    }
}
