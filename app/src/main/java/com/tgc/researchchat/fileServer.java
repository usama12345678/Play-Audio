package com.tgc.researchchat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class fileServer extends Thread {

    private Context context;
    private String serverIpAddress;
    private String TAG = "FILE SERVER";
    private RecyclerView messageList;
    private ArrayList<Message> messageArray;
    private  ArrayList<MyFiles> filesArray;
    private ChatAdapterRecycler mAdapter;
    private ImageAdapter imageAdapter;
    private int port;

    fileServer(Context context, ChatAdapterRecycler mAdapter, RecyclerView messageList, ArrayList<Message> messageArray, int port, String serverIpAddress, ImageAdapter imageAdapter, ArrayList<MyFiles> filesArray) {
        this.messageArray = messageArray;
        this.messageList = messageList;
        this.filesArray = filesArray;
        this.mAdapter = mAdapter;
        this.port = port;
        this.context = context;
        this.serverIpAddress = serverIpAddress;
        this.imageAdapter = imageAdapter;
    }

    public void run() {
        try {
            ServerSocket fileSocket = new ServerSocket(port + 1);
            Log.d(TAG, "run: " + fileSocket.getLocalPort());
            fileSocket.setReuseAddress(true);
            System.out.println(TAG + "started");
            while (!Thread.interrupted()) {
                Socket connectFileSocket = fileSocket.accept();
                Log.d(TAG, "run: File Opened");
                fileFromClient handleFile = new fileFromClient();
                handleFile.execute(connectFileSocket);
            }
            fileSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class fileFromClient extends AsyncTask<Socket, Void, String> {
        String text;

        @Override
        protected String doInBackground(Socket... sockets) {
            try {
                File testDirectory = new File(context.getObbDir(), "downloadFolder");
                if (!testDirectory.exists())
                    testDirectory.mkdirs();
                try {
                    InputStream inputStream = sockets[0].getInputStream();
                    DataInputStream dataInputStream = new DataInputStream(inputStream);

                    String fileName = dataInputStream.readUTF();
                    File outputFile = new File(testDirectory, fileName);
                    text = fileName;

                    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
                    long fileSize = dataInputStream.readLong();
                    int bytesRead;
                    byte[] byteArray = new byte[8192 * 16];

                    while (fileSize > 0 && (bytesRead = dataInputStream.read(byteArray, 0, (int) Math.min(byteArray.length, fileSize))) != -1) {
                        outputStream.write(byteArray, 0, bytesRead);
                        fileSize -= bytesRead;
                    }
                    inputStream.close();
                    dataInputStream.close();
                    outputStream.flush();
                    outputStream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return text;
        }

        protected void onPostExecute(String result) {
            Log.d(TAG, "onPostExecute: Result" + result);
            messageArray.add(new Message("New File Received: " + result, 1, Calendar.getInstance().getTime()));
            messageList.setAdapter(mAdapter);
            if(result.contains("jpg") || result.contains("jpeg") || result.contains("png")){
                System.out.println(TAG + " image Adapter added");
                String directory = context.getObbDir() +"/downloadFolder/" + result;
                System.out.println("IMAGE DIRECTORY => " + directory);
                filesArray.add(new MyFiles(directory,1,Calendar.getInstance().getTime()));
                messageList.setAdapter(imageAdapter);
            }
            File filepath = context.getObbDir();
            Log.i(TAG, "FilesDir =>" + filepath + "\n");
            @SuppressLint("SimpleDateFormat")
            String fileName = new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-" + serverIpAddress + ".txt";
            File file = new File(filepath, fileName);
            try {
                FileOutputStream fos = new FileOutputStream(file, true);
                String history = "Server received a file from => " + serverIpAddress + "\n";
                fos.write(history.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}