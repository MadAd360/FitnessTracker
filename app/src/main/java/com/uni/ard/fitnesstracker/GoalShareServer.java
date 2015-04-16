package com.uni.ard.fitnesstracker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Jay on 16/04/2015.
 */
public class GoalShareServer extends AsyncTask {

    @Override
    protected Object doInBackground(Object[] params) {
        try {

            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */
            ServerSocket serverSocket = new ServerSocket(8888);
            Log.d("Server", "Server running");
            Socket client = serverSocket.accept();
            Log.d("Server", "Accepted");
            /**
             * If this code is reached, a client has connected and transferred data
             * Save the input stream from the client as a JPEG file
             */

            InputStream inputStream = client.getInputStream();
            Scanner s = new Scanner(inputStream).useDelimiter("\\A");
            String str =  s.hasNext() ? s.next() : "";

            Log.d("WiFi", "Received string '" + str + "'");

            serverSocket.close();
            return str;
        } catch (IOException e) {
            Log.d("WiFi", e.getMessage());
            return null;
        }
    }

}
