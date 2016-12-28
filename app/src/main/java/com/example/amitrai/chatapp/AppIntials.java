package com.example.amitrai.chatapp;

import android.app.Application;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by amitrai on 28/12/16.
 */

public class AppIntials extends Application{

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(Constants.SERVER_URL);
            IO.Options options = new IO.Options();
            options.port = Constants.PORT;
            IO.socket(Constants.SERVER_URL, options);
            mSocket.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}
