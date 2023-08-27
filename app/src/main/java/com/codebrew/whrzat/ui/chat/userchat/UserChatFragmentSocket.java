package com.codebrew.whrzat.ui.chat.userchat;

import android.app.Activity;
import android.util.Log;

import com.codebrew.whrzat.event.ChatSocketReferesh;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class UserChatFragmentSocket {
    private final static String TAG = UserChatFragmentSocket.class.getSimpleName();

    private final static String EVENT_SEND_MESSAGE = "sendMessage";
    private final static String EVENT_MESSAGE_RECEIVED = "receiveMessage";

    //  private final static String EVENT_MESSAGE_STATUS = "status_";
    //  private final static String EVENT_UNMATCH = "unmatch_";

    private final static String TO = "to";
    private final static String FROM = "from";
    private final static String MESSAGE = "message";
    private final static String TIMESTAMP = "timeStamp";
    private final static String TYPE = "messageType";



    private String otherUserID,myUserId;

    private final SocketCallback callback;
    private final Activity activity;
    private boolean isConnected;
    private Socket socket;

    private Emitter.Listener onMessageReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.i(TAG, "Socket Message Received: " + data.toString());
            try {
                String message="";
                final String from = data.getString(FROM);
                final String timeStamp = data.getString(TIMESTAMP);
                final String recId = data.getString(TO);
                final int type = Integer.valueOf(data.getString(TYPE));

                EventBus.getDefault().postSticky(new ChatSocketReferesh(true));



                final String finalMessage = message;

                if(from.equals(otherUserID)) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onMessageReceived(finalMessage, timeStamp, from);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onMessageStatusReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.i(TAG, "Socket message status received: " + data.toString());
            try {
                final String timeStamp = data.getString(TIMESTAMP);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onMessageSentSuccessfully(timeStamp);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onUnMatchEventReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.onUnMatchByOtherUser();
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d(TAG, "Socket Connect error");
            isConnected = false;
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            isConnected = false;
            Log.d(TAG, "Socket Disconnected");
        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Socket Connected");
            isConnected = true;
        }
    };

    private Emitter.Listener onReconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Socket Reconnect");
            isConnected = true;
        }
    };

    public UserChatFragmentSocket(Activity activity, SocketCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    public void connectSocket(String otherUserId, String myUserId) {
        if (otherUserId != null && !isConnected) {
            try {
                otherUserID=otherUserId;
                this.myUserId=myUserId;
                IO.Options options = new IO.Options();
                options.forceNew = true;
                options.reconnection = false;
                options.query = "access_token=" + myUserId;
                socket = IO.socket("http://192.168.100.23:8002/",options);
                //socket.emit("UserAuth",)
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            socket.on(Socket.EVENT_CONNECT, onConnect);
            socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            socket.on(Socket.EVENT_RECONNECT, onReconnect);
            // socket.on(EVENT_MESSAGE_RECEIVED + myUserId + "_" + otherUserId, onMessageReceived);
            socket.on(EVENT_MESSAGE_RECEIVED , onMessageReceived);
            //  socket.on(EVENT_MESSAGE_STATUS + myUserId, onMessageStatusReceived);
            //  socket.on(EVENT_UNMATCH + otherUserId + "_" + myUserId, onUnMatchEventReceived);

            socket.connect();
        }
    }

    public void closeSocket() {
        socket.off();
        socket.disconnect();
        Log.d(TAG, "Socket off and disconnected");
        isConnected = false;
    }

    public void sendTextMsg(String mOtherUserId, String mMyUserId, String message, String currentTime) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(MESSAGE, message);
            jsonObject.put(TO, mOtherUserId);
            jsonObject.put(FROM, mMyUserId);
            jsonObject.put(TIMESTAMP, currentTime);
            socket.emit(EVENT_SEND_MESSAGE, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public interface SocketCallback {
        void onUnMatchByOtherUser();

        void onMessageReceived(String message, String timeStamp, String from);

        void onMessageSentSuccessfully(String timestamp);
    }
}