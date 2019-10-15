package com.tgc.researchchat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class chatServer extends Thread {

    private Context context;
    private String serverIpAddress;
    private Activity activity;
    private String ownIp;
    private String TAG = "CHATSERVER";
    private RecyclerView messageList;
    private ArrayList<Message> messageArray;
    private ChatAdapterRecycler mAdapter;
    private int port;

    chatServer(String ownIp, Activity activity, Context context, ChatAdapterRecycler mAdapter, RecyclerView messageList, ArrayList<Message> messageArray, int port, String serverIpAddress) {
        this.ownIp = ownIp;
        this.messageArray = messageArray;
        this.messageList = messageList;
        this.mAdapter = mAdapter;
        this.port = port;
        this.context = context;
        this.serverIpAddress = serverIpAddress;
        this.activity = activity;
    }

    @SuppressLint("SetTextI18n")
    public void run() {
        try {
            ServerSocket initSocket = new ServerSocket(port);
            initSocket.setReuseAddress(true);
            TextView textView;
            textView = activity.findViewById(R.id.textView);
            textView.setText("Server Socket Started at IP: " + ownIp + " and Port: " + port);
            textView.setBackgroundColor(Color.parseColor("#39FF14"));
            System.out.println(TAG + "started");
            while (!Thread.interrupted()) {
                Socket connectSocket = initSocket.accept();
                ReadFromClient handle = new ReadFromClient();
                handle.execute(connectSocket);
            }
            initSocket.close();
        } catch (IOException e) {
            TextView textView;
            textView = activity.findViewById(R.id.textView);
            textView.setText("Server Socket initialization failed. Port already in use.");
            textView.setBackgroundColor(Color.parseColor("#FF0800"));
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class ReadFromClient extends AsyncTask<Socket, Void, String> {
        String text;

        @Override
        protected String doInBackground(Socket... sockets) {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(sockets[0].getInputStream()));
                text = input.readLine();
                Log.i(TAG, "Received => " + text);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return text;
        }

        protected void onPostExecute(String result) {
            Log.d(TAG, "onPostExecute: Result" + result);
            if (result.charAt(0) == '1' && result.charAt(1) == ':') {
                StringBuilder stringBuilder = new StringBuilder(result);
                stringBuilder.deleteCharAt(0);
                stringBuilder.deleteCharAt(0);
                result = stringBuilder.toString();
                File path = context.getObbDir();
                Log.i(TAG, "FilesDir =>" + path + "\n");
                @SuppressLint("SimpleDateFormat") String fileName = new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-" + serverIpAddress + ".txt";
                File file = new File(path, fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file, true);
                    String history = "server: " + result + "\n";
                    fos.write(history.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                messageArray.add(new Message(result, 1, Calendar.getInstance().getTime()));
                messageList.setAdapter(mAdapter);
            } else {
                StringBuilder stringBuilder = new StringBuilder(result);
                stringBuilder.deleteCharAt(0);
                stringBuilder.deleteCharAt(0);
                result = stringBuilder.toString();
                ListView message_List;
                message_List = activity.findViewById(R.id.message_list);
                LayerDrawable layerDrawable = (LayerDrawable) message_List.getBackground();
                GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.shapeColor);
                gradientDrawable.setColor(Color.parseColor("#" + result));
            }
        }
    }

}
