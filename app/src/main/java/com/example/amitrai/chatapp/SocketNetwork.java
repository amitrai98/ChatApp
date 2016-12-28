package com.example.amitrai.chatapp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.content.ContentValues.TAG;

/**
 * Created by amitrai on 28/12/16.
 */

public class SocketNetwork {

    private static final String SOCKET_CONNECTION_URL = "http://192.168.3.39:3003";
    private static final int SOCKET_CONNECTION_PORT = 3003;
    private static final int SERVER_CONNECTION_ERROR = 101;
    private static final int SERVER_CONNECTION_TIMEOUT = 102;
    private static final int SERVER_DISCONNECTED = 103;
    private static SocketNetwork mInstance;
    private Socket mSocket;
    private int RECONNECTION_ATTEMPT = 10;
    private long CONNECTION_TIMEOUT = 30000;
    private static NetworkInterface mNetworkInterface;
    private Activity activity;
    boolean isConnected = false;

    public static SocketNetwork getInstance(Context context, NetworkInterface interfaces) {
        mNetworkInterface = interfaces;
        if (mInstance == null) {
            mInstance = new SocketNetwork();
        }
        return mInstance;
    }

    public void connectToSocket(Activity activity) {
        try {
            this.activity = activity;
            IO.Options opts = new IO.Options();
            opts.port = SOCKET_CONNECTION_PORT;
            opts.timeout = CONNECTION_TIMEOUT;
            opts.reconnection = true;
            opts.reconnectionAttempts = RECONNECTION_ATTEMPT;
            opts.reconnectionDelay = 1000;
            opts.forceNew = true;
            mSocket = IO.socket(SOCKET_CONNECTION_URL);

           /*mSocket = IO.socket(NetworkConstant.SOCKET_CONNECTION_URL);
            mSocket.io().timeout(CONNECTION_TIMEOUT);
            mSocket.io().reconnection(true);
            mSocket.io().reconnectionAttempts(RECONNECTION_ATTEMPT);*/
            makeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The purpose of the method is to return the instance of socket
     *
     * @return
     */
    public Socket getSocket() {
        return mSocket;
    }

    /**
     * The purpose of this method is to connect with the socket
     */

    public void makeConnection() {
        if (mSocket != null) {
            mSocket.connect();
            mSocket.emit("join", "sachin");
            if (mSocket.connected())
                registerConnectionAttributes();


            mSocket.on("message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, "message received");
                }
            });
        }
    }

    /**
     * The purpose of this method is to disconnect from the socket interface
     */
    public void disconnectFromSocket() {
        unregisterConnectionAttributes();
        mSocket.disconnect();
        mSocket = null;
        mInstance = null;
    }

    public void registerConnectionAttributes() {
        try {
            if(mSocket.connected()) {
                mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectionError);
                mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectionTimeOut);
                mSocket.on(Socket.EVENT_DISCONNECT, onServerDisconnect);

                mSocket.on(Socket.EVENT_CONNECT,onConnect);
                mSocket.on(Socket.EVENT_DISCONNECT,onDisconnect);
                mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
                mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
                mSocket.on("update-people", onUpdatePeople);
                mSocket.on("new-message", onNewMessage);
                mSocket.on("chat-message\n", chatMessage);
                mSocket.on("user joined", onUserJoined);
                mSocket.on("user left", onUserLeft);
                mSocket.on("typing", onTyping);
                mSocket.on("stop typing", onStopTyping);
                mSocket.connect();
            }} catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void unregisterConnectionAttributes() {
        try {
            mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectionError);
            mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectionTimeOut);
            mSocket.off(Socket.EVENT_DISCONNECT, onServerDisconnect);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * The purpose of this method is to get the call back for any type of connection error
     */
    private Emitter.Listener onConnectionError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("Response", "onConnectionError");
            mNetworkInterface.networkCallReceive(SERVER_CONNECTION_ERROR);
        }
    };

    /**
     * The purpose of this method to get the call back for connection getting timed out
     */
    private Emitter.Listener onConnectionTimeOut = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("Response", "onConnectionTimeOut");
            mNetworkInterface.networkCallReceive(SERVER_CONNECTION_TIMEOUT);
        }
    };
    /**
     * The purpose of this method is to receive the call back when the server get disconnected
     */
    private Emitter.Listener onServerDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("Response", "onServerDisconnection");
            mNetworkInterface.networkCallReceive(SERVER_DISCONNECTED);
        }
    };

    /**
     * The purpose of this method is register a method on server
     *
     * @param methodOnServer
     * @param handlerName
     */
    public void registerHandler(String methodOnServer, Emitter.Listener handlerName) {
        try {
            if(mSocket.connected())
                mSocket.on(methodOnServer, handlerName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The purpose of this method is to unregister a method from server
     *
     * @param methodOnServer
     * @param handlerName
     */
    public void unRegisterHandler(String methodOnServer, Emitter.Listener handlerName) {
        try {
            mSocket.off(methodOnServer, handlerName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The purpose of this method is to send the data to the server
     *
     * @param methodOnServer
     * @param request
     */
    public void sendDataToServer(String methodOnServer, JSONObject request) {
        Log.e("JSON ", request.toString());
        try {
            if(mSocket.connected())

            {


                registerConnectionAttributes();
                mSocket.emit(methodOnServer, "hello");


            }
            else
            {
                mNetworkInterface.networkCallReceive(SERVER_CONNECTION_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public interface NetworkInterface {
        public void networkCallReceive(int responseType);
    }


    private Object mUsername = "android_test";
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if(!isConnected) {
                        if(null!=mUsername)
                            getSocket().emit("add user", mUsername);
                        Toast.makeText(activity,
                                "connected", Toast.LENGTH_LONG).show();
                        isConnected = true;
                    }

                    Log.e(TAG, "got connected");
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
             activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isConnected = false;
                    Toast.makeText(activity,
                            "disconnected", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
             activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity,
                            "error while connection", Toast.LENGTH_LONG).show();
                }
            });
        }
    };




    private Emitter.Listener onUpdatePeople = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
             activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }

//                    removeTyping(username);
                    Toast.makeText( activity,
                            message, Toast.LENGTH_LONG).show();
                }
            });
        }
    };


    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
             activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }

//                    removeTyping(username);
                    Toast.makeText( activity,
                            message, Toast.LENGTH_LONG).show();
                }
            });
        }
    };


    private Emitter.Listener chatMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
             activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }

//                    removeTyping(username);
                    Toast.makeText( activity,
                            message, Toast.LENGTH_LONG).show();
                }
            });
        }
    };



    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
             activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }

                    Toast.makeText( activity,
                            "a new user has joined", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
             activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }

                    Toast.makeText( activity,
                            "user has left", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
             activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
             activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

}