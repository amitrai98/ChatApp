package com.example.amitrai.chatapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private EditText edt_message = null;
    private Button btn_send = null;
    private RecyclerView recyle_view = null;
    private SocketNetwork network;
    private boolean isConnected;
    private String mUsername = "test";
    private String TAG = getClass().getSimpleName();
    private MessageAdapter adapter = null;
    private List<Message> list_message =  new ArrayList<Message>();
    private LinearLayoutManager manager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list_message.clear();
        initView();
        network.getSocket().on(Socket.EVENT_CONNECT,onConnect);
        network.getSocket().on(Socket.EVENT_DISCONNECT,onDisconnect);
        network.getSocket().on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        network.getSocket().on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        network.getSocket().on("update-people", onUpdatePeople);
        network.getSocket().on("new-message", onNewMessage);
        network.getSocket().on("chat-message\n", chatMessage);
        network.getSocket().on("user joined", onUserJoined);
        network.getSocket().on("user left", onUserLeft);
        network.getSocket().on("typing", onTyping);
        network.getSocket().on("stop typing", onStopTyping);
        network.getSocket().connect();
        isConnected = network.getSocket().connected();
    }

    /**
     * initalizing view elements
     */
    private void initView(){
        connectToServer();

        manager = new LinearLayoutManager(this);
        adapter = new MessageAdapter(list_message);
        recyle_view = (RecyclerView) findViewById(R.id.recycle_view);
        recyle_view.setAdapter(adapter);
        recyle_view.setLayoutManager(manager);
        edt_message = (EditText) findViewById(R.id.edt_message);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = edt_message.getText().toString();
                if (message.isEmpty()){
                    Toast.makeText(MainActivity.this, "please enter a message atleast", Toast.LENGTH_SHORT).show();
                }else {
                    sendMessage(message);
                    list_message.add(new Message("me", message));
                    edt_message.setText("");
                    adapter.notifyDataSetChanged();
                    manager.scrollToPosition(list_message.size()-1);
                }
            }
        });
    }

    /**
     * sends a string message
     */
    private void sendMessage(String message){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", message);
            if(network!=null)
                network.sendDataToServer("new message",jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * connects with io server
     */
    private void connectToServer(){
        try {
            network = SocketNetwork.getInstance(this, new SocketNetwork.NetworkInterface() {
                @Override
                public void networkCallReceive(int responseType) {
                    Log.e(TAG, "recevied a network call");
                }
            });
            network.connectToSocket(this);
            network.getSocket().on("chat_message", onNewMessage);
            network.getSocket().on("chat_message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, ""+args[0]);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(network != null){
            network.getSocket().off(Socket.EVENT_CONNECT, onConnect);
            network.getSocket().off(Socket.EVENT_DISCONNECT, onDisconnect);
            network.getSocket().off(Socket.EVENT_CONNECT_ERROR, onConnectError);
            network.getSocket().off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            network.getSocket().off("new message", onNewMessage);
            network.getSocket().off("user joined", onUserJoined);
            network.getSocket().off("user left", onUserLeft);
            network.getSocket().off("typing", onTyping);
            network.getSocket().off("stop typing", onStopTyping);
            network.disconnectFromSocket();
        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!isConnected) {
                        if(null!=mUsername)
                            network.getSocket().emit("add user", mUsername);
                        Toast.makeText(getApplicationContext(),
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isConnected = false;
                    Toast.makeText(getApplicationContext(),
                            "disconnected", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "error while connection", Toast.LENGTH_LONG).show();
                }
            });
        }
    };




    private Emitter.Listener onUpdatePeople = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
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
                    Toast.makeText(getApplicationContext(),
                            message, Toast.LENGTH_LONG).show();
                }
            });
        }
    };


    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
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
                    Toast.makeText(getApplicationContext(),
                            message, Toast.LENGTH_LONG).show();
                }
            });
        }
    };


    private Emitter.Listener chatMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
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
                    Toast.makeText(getApplicationContext(),
                            message, Toast.LENGTH_LONG).show();
                }
            });
        }
    };



    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
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

                    Toast.makeText(getApplicationContext(),
                            "a new user has joined", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
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

                    Toast.makeText(getApplicationContext(),
                            "user has left", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
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
            runOnUiThread(new Runnable() {
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

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
//            if (!mTyping) return;
//
//            mTyping = false;
//            mSocket.emit("stop typing");
        }
    };
}
