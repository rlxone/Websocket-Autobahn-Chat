package com.example.websocket.library;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import com.example.websocket.BuildConfig;
import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.greenrobot.eventbus.EventBus;

public class ExampleSocketConnection implements ClientWebSocket.MessageNotifier {
    private ClientWebSocket clientWebSocket;
    private Context context;
    public Gson gson = new Gson();
    private Handler socketConnectionHandler;

    private Runnable checkConnectionRunnable = new Runnable() {
		public void run() {
			try {
			    if (!clientWebSocket.getConnection().isOpen()) {
			        openConnection();
			    }
			    startCheckConnection();
			}
			catch (Exception e) {
				Log.i("Websocket", "Check runnable exception: " + e.getMessage());
			}
		}
	};

    private void startCheckConnection() {
        socketConnectionHandler.postDelayed(checkConnectionRunnable, 5000);
    }

    private void stopCheckConnection() {
        socketConnectionHandler.removeCallbacks(checkConnectionRunnable);
    }

    public ExampleSocketConnection(Context context) {
        this.context = context;
        socketConnectionHandler = new Handler();
    }

    public void openConnection() {
        /*if (!Preferences.getManager().isAuth()) {
            Log.i("Websocket", "Error: User is not authorize");
            return;
        }*/
        if (clientWebSocket != null) {
        	clientWebSocket.close();
        	stopCheckConnection();
        }
        try {
            clientWebSocket = new ClientWebSocket(this, BuildConfig.SOCKET_URL /*+ Preferences.getManager().getUserId()*/);
            clientWebSocket.connect();
            Log.i("Websocket", "Trying connect to websocket " + BuildConfig.SOCKET_URL /*+ Preferences.getManager().getUserId()*/);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initScreenStateListener();
        startCheckConnection();
    }
    
    public void sendMessage(String message) {
    	if (clientWebSocket != null) {
    		clientWebSocket.sendMessage(message);
    	}
    }
    
    public void closeConnection() {
        if (clientWebSocket != null) {
            clientWebSocket.close();
            clientWebSocket = null;
        }
        releaseScreenStateListener();
        stopCheckConnection();
    }

    /**
     * Screen state listener for socket live cycle
     */
    private void initScreenStateListener() {
        context.registerReceiver(screenStateReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        context.registerReceiver(screenStateReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    private void releaseScreenStateListener() {
        try {
            context.unregisterReceiver(screenStateReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver screenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Log.i("Websocket", "Screen ON");
                openConnection();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.i("Websocket", "Screen OFF");
                closeConnection();
            }
        }
    };

    public boolean isConnected() {
        return clientWebSocket != null &&
                clientWebSocket.getConnection() != null &&
                clientWebSocket.getConnection().isOpen();
    }

	public void onTextMessage(String message) {
    	RealTimeMessage realTimeMessage = new RealTimeMessage();
    	realTimeMessage.type = RealTimeMessage.Type.TextMessage;
    	ArrayList<Object> objects = new ArrayList<Object>();
    	objects.add(message);
    	realTimeMessage.objects = objects;
        EventBus.getDefault().post(realTimeMessage);
	}

	@Override
	public void onConnected(Map<String, List<String>> headers) {
    	RealTimeMessage realTimeMessage = new RealTimeMessage();
    	realTimeMessage.type = RealTimeMessage.Type.Connected;
    	ArrayList<Object> objects = new ArrayList<Object>();
    	objects.add(headers);
    	realTimeMessage.objects = objects;
        EventBus.getDefault().post(realTimeMessage);
	}

	@Override
	public void onError(WebSocketException cause) {
    	RealTimeMessage realTimeMessage = new RealTimeMessage();
    	realTimeMessage.type = RealTimeMessage.Type.Error;
    	ArrayList<Object> objects = new ArrayList<Object>();
    	objects.add(cause);
    	realTimeMessage.objects = objects;
        EventBus.getDefault().post(realTimeMessage);
	}

	@Override
	public void onDisconnected(WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
			boolean closedByServer) {
    	RealTimeMessage realTimeMessage = new RealTimeMessage();
    	realTimeMessage.type = RealTimeMessage.Type.Disconnected;
    	ArrayList<Object> objects = new ArrayList<Object>();
    	objects.add(serverCloseFrame);
    	objects.add(clientCloseFrame);
    	objects.add(closedByServer);
    	realTimeMessage.objects = objects;
        EventBus.getDefault().post(realTimeMessage);
	}

	@Override
	public void onUnexpectedError(WebSocketException cause) {
		RealTimeMessage realTimeMessage = new RealTimeMessage();
    	realTimeMessage.type = RealTimeMessage.Type.UnexpectedError;
    	ArrayList<Object> objects = new ArrayList<Object>();
    	objects.add(cause);
    	realTimeMessage.objects = objects;
        EventBus.getDefault().post(realTimeMessage);
	}

	@Override
	public void onPongFrame(WebSocketFrame frame) {
		RealTimeMessage realTimeMessage = new RealTimeMessage();
    	realTimeMessage.type = RealTimeMessage.Type.Pong;
    	ArrayList<Object> objects = new ArrayList<Object>();
    	objects.add(frame);
    	realTimeMessage.objects = objects;
        EventBus.getDefault().post(realTimeMessage);
	}

	@Override
	public void onMessageSend(String message) {
		RealTimeMessage realTimeMessage = new RealTimeMessage();
    	realTimeMessage.type = RealTimeMessage.Type.Send;
    	ArrayList<Object> objects = new ArrayList<Object>();
    	objects.add(message);
    	realTimeMessage.objects = objects;
        EventBus.getDefault().post(realTimeMessage);
	}
}
