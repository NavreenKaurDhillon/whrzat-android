package com.codebrew.whrzat.ui.profile

import android.app.Activity
import android.util.Log
import com.codebrew.whrzat.util.Constants
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException




class NotificationSocket(val activity: Activity, var callback: SocketCallback) {
    private var otherUserID: String=""
    private var myUserId: String=""
    private var isConnected: Boolean = false
    private lateinit var socket: Socket

    private val onMessageReceived = Emitter.Listener { args ->
        val data: JSONObject
        if (args[0] != null) {
            data = args[0] as JSONObject
            Log.i(TAG, "Socket Message Received: " + data.toString())
            try {

              //  EventBus.getDefault().postSticky(ChatSocketReferesh(true))
                val message = data.getString("count")
                //from ios timestamp coming in point like 123455.4564 which crash the app so converted fist to double and dan long to get value bfore point
                activity.runOnUiThread { callback.onMessageReceived(message) }

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private val onChatMessageReceiv = Emitter.Listener { args ->
        val data: JSONObject
        if (args[0] != null) {
            data = args[0] as JSONObject
            Log.i(TAG, "Socket Message Received: " + data.toString())
            try {
                val message = data.getString("count")
                //from ios timestamp coming in point like 123455.4564 which crash the app so converted fist to double and dan long to get value bfore point
                activity.runOnUiThread { callback.onChatMessage(message) }

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }



    private val onConnectError = Emitter.Listener {
        Log.d(TAG, "Socket Connect error")
        isConnected = false
    }

    private val onDisconnect = Emitter.Listener {
        isConnected = false
        Log.d(TAG, "Socket Disconnected")
    }

    private val onConnect = Emitter.Listener {
        Log.d(TAG, "Socket Connected")
        isConnected = true
    }

    private val onReconnect = Emitter.Listener {
        Log.d(TAG, "Socket Reconnect")
        isConnected = true
    }

    fun connectSocket(otherUserId: String?, myUserId: String) {
        if (otherUserId != null && !isConnected) {
            try {
                otherUserID = otherUserId
                this.myUserId = myUserId
                val options = IO.Options()
                options.forceNew = true
                options.reconnection = false
                options.query = "access_token=" + myUserId
                socket = IO.socket(Constants.BASE_URL, options)
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }


            /*Prefs.with(activity).getString(Constants.ACCESS_TOKEN, "")*/
            socket.on(Socket.EVENT_CONNECT, onConnect)
            socket.on(Socket.EVENT_DISCONNECT, onDisconnect)
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
            socket.on(Socket.EVENT_RECONNECT, onReconnect)
            socket.on("Count"+myUserId, onMessageReceived)
            socket.on("msgC"+myUserId,onChatMessageReceiv)
            //  socket.on(EVENT_MESSAGE_STATUS + myUserId, onMessageStatusReceived);
            //  socket.on(EVENT_UNMATCH + otherUserId + "_" + myUserId, onUnMatchEventReceived);

            socket.connect()
        }
    }

    fun closeSocket() {
        socket.off()
        socket.disconnect()
        Log.d(TAG, "Socket off and disconnected")
        isConnected = false
    }

    fun sendTextMsg(myUserId: String) {
        val jsonObject = JSONObject()
        this.myUserId=myUserId
        try {
            jsonObject.put(Constants.USER_ID,myUserId)
            socket.emit(EVENT_SEND_COUNT, jsonObject)
              Log.i(TAG, "Socket Message send: " + jsonObject.toString())

        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    fun seen(myUserId: String) {
        val jsonObject = JSONObject()
        this.myUserId=myUserId
        try {
            jsonObject.put(Constants.USER_ID,myUserId)
            socket.emit(EVENT_SEND_COUNT, jsonObject)
            Log.i(TAG, "Socket Message send: " + jsonObject.toString())

        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    fun getchatCount(userId: String?, userId1: String?) {

        val jsonObject = JSONObject()
        this.myUserId=myUserId
        try {
            jsonObject.put(Constants.USER_ID,myUserId)
            socket.emit(EVENT_SEND_CHAT_COUNT, jsonObject)
            Log.i(TAG, "Socket Message send: " + jsonObject.toString())

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


    interface SocketCallback {

        fun onMessageReceived(message: String)

        fun onChatMessage(message:String)

    }

    companion object {
        val TAG = "ChatSocket"

        val EVENT_SEND_COUNT = "requestCount"
        val EVENT_SEND_CHAT_COUNT = "getUnread"
        val EVENT_SEEN = "makeRead"

        //  val EVENT_MESSAGE_RECEIVED = "Count".plus(myuser)

        //  private final static String EVENT_MESSAGE_STATUS = "status_";
        //  private final static String EVENT_UNMATCH = "unmatch_";

        val TO = "to"
        val FROM = "from"
        val MESSAGE = "message"
        val TIMESTAMP = "timeStamp"
        // private val TYPE = "messageType"


    }

}

