package com.codebrew.whrzat.ui.chat.userchat

import android.app.Activity
import android.util.Log
import com.codebrew.whrzat.ui.profile.NotificationSocket
import com.codebrew.whrzat.util.Constants
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException

class ChatSocket(val activity: Activity, var callback: SocketCallback) {
    private var otherUserID: String = ""
    private var myUserId: String = ""
    private var isConnected: Boolean = false
    private lateinit var socket: Socket

    private val onMessageReceived = Emitter.Listener { args ->
        val data: JSONObject
        if (args[0] != null) {
            data = args[0] as JSONObject
            Log.i(TAG, "Socket Message Received: " + data.toString())
            try {
                val from = data.getString(FROM)
                val timeStamp = data.getString(TIMESTAMP)
                val to = data.getString(TO)
                val message = data.getString(MESSAGE)
                val finalMessage = message


                if (from == otherUserID) {
                    //from ios timestamp coming in point like 123455.4564 which crash the app so converted fist to double and dan long to get value bfore point
                    activity.runOnUiThread { callback.onMessageReceived(finalMessage, timeStamp.toDouble(), from) }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private val onMessageStatusReceived = Emitter.Listener { args ->
        val data = args[0] as JSONObject
        Log.i(TAG, "Socket message status received: " + data.toString())
        try {

            val timeStamp = data.getString(TIMESTAMP)
            val from = data.getString(FROM)
            if (from == otherUserID) {
                activity.runOnUiThread { callback.onMessageSentSuccessfully(timeStamp) }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }


    private val onBlockedReceived = Emitter.Listener { args ->
        val data = args[0] as JSONObject
        Log.i(TAG, "Socket message status received: " + data.toString())
        try {
            val isBlocked = data.getBoolean("blocked")
            val from = data.getString("_id")
            if (from==otherUserID) {
                activity.runOnUiThread {
                    callback.onBlockedReceived(isBlocked)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    private val onUnMatchEventReceived = Emitter.Listener { activity.runOnUiThread { callback.onUnMatchByOtherUser() } }

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
               // socket = IO.socket("http://54.226.122.15:3001/", options)
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
            socket.on(EVENT_MESSAGE_RECEIVED, onMessageReceived)
            socket.on(BLOCK_CHECK + myUserId, onBlockedReceived)

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

    fun seen(myUserId: String, userId: String) {
        val jsonObject = JSONObject()
        // this.myUserId = us
        try {
            jsonObject.put(Constants.SENDER, myUserId)
            jsonObject.put(Constants.RECEIVER, userId)
            socket.emit(SEEN, jsonObject)
            Log.i(NotificationSocket.TAG, "Socket Message send: " + jsonObject.toString())

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


    fun sendTextMsg(mOtherUserId: String, mMyUserId: String, message: String, currentTime: String) {
        val jsonObject = JSONObject()

        try {
            jsonObject.put(MESSAGE, message)
            jsonObject.put(TO, mOtherUserId)
            jsonObject.put(FROM, mMyUserId)
            jsonObject.put(TIMESTAMP, currentTime)
            socket.emit(EVENT_SEND_MESSAGE, jsonObject)
            //  Log.i(TAG, "Socket Message send: " + jsonObject.toString())

        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }


    interface SocketCallback {
        fun onUnMatchByOtherUser()

        fun onMessageReceived(message: String, timeStamp: Double, from: String)

        fun onMessageSentSuccessfully(timestamp: String)

        fun onBlockedReceived(isBlocked: Boolean)
    }

    companion object {
        val TAG = "ChatSocket"

        val EVENT_SEND_MESSAGE = "sendMessage"
        val EVENT_MESSAGE_RECEIVED = "receiveMessage"
        val SEEN = "makeRead"
        val BLOCK_CHECK = "blockNotify"
        //  private final static String EVENT_MESSAGE_STATUS = "status_";
        //  private final static String EVENT_UNMATCH = "unmatch_";

        val TO = "to"
        val FROM = "from"
        val MESSAGE = "message"
        val TIMESTAMP = "timeStamp"
        // private val TYPE = "messageType"


    }
}