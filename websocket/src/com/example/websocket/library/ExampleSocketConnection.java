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
import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.greenrobot.eventbus.EventBus;

public class ExampleSocketConnection implements ClientWebSocket.MessageNotifier {
    private ClientWebSocket clientWebSocket;
    private Context context;

    public ExampleSocketConnection(Context context) {
        this.context = context;
    }

    public void openConnection() throws WebSocketException, IOException {
    	if (clientWebSocket != null && clientWebSocket.getConnection().isOpen()) {
    		clientWebSocket.close();
    	}
    	if (clientWebSocket != null && clientWebSocket.getConnection().getState() == WebSocketState.CONNECTING) {
    		return;
    	}
    	try {
            clientWebSocket = new ClientWebSocket(this, BuildConfig.SOCKET_URL /*+ Preferences.getManager().getUserId()*/);
            clientWebSocket.connect();
            Log.i("Websocket", "Trying connect to websocket " + BuildConfig.SOCKET_URL /*+ Preferences.getManager().getUserId()*/);
        } catch (Exception e) {
            e.printStackTrace();
        }
    	initScreenStateListener();
    }
    
    public void sendMessage(String message) {
    	if (clientWebSocket != null) {
    		clientWebSocket.sendMessage(message);
    	}
    }
    
    public void sendPing() {
    	if (clientWebSocket != null) {
    		clientWebSocket.getConnection().sendPing();
    	}
    }
    
    public void closeConnection() {
        if (clientWebSocket != null) {
            clientWebSocket.close();
        }
        releaseScreenStateListener();
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
                try {
					openConnection();
				} catch (WebSocketException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
	
	@Override
	public void onPingFrame(WebSocketFrame frame) {
		RealTimeMessage realTimeMessage = new RealTimeMessage();
    	realTimeMessage.type = RealTimeMessage.Type.Ping;
    	ArrayList<Object> objects = new ArrayList<Object>();
    	objects.add(frame);
    	realTimeMessage.objects = objects;
        EventBus.getDefault().post(realTimeMessage);
	}
	
	@Override
	public void onDisconnectedByServer(WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
			boolean closedByServer) {
    	RealTimeMessage realTimeMessage = new RealTimeMessage();
    	realTimeMessage.type = RealTimeMessage.Type.DisconnectedByServer;
    	ArrayList<Object> objects = new ArrayList<Object>();
    	objects.add(serverCloseFrame);
    	objects.add(clientCloseFrame);
    	objects.add(closedByServer);
    	realTimeMessage.objects = objects;
        EventBus.getDefault().post(realTimeMessage);
	}
}
