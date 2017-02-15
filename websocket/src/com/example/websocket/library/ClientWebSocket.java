package com.example.websocket.library;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

public class ClientWebSocket {
    private MessageNotifier listener;
    private String host;
    private WebSocket ws;

    public ClientWebSocket(MessageNotifier listener, String host) {
        this.listener = listener;
        this.host = host;
    }

    public void connect() {
        new Thread(new Runnable() {
			public void run() {
			    if (ws != null) {
			        reconnect();
			    } else {
			        try {
			            WebSocketFactory factory = new WebSocketFactory();
			            SSLContext context = NaiveSSLContext.getInstance("TLS");
			            factory.setSSLContext(context);
			            ws = factory.createSocket(host);
			            ws.addListener(new SocketListener());
			            ws.connect();
			        } catch (WebSocketException e) {
			            e.printStackTrace();
			        } catch (IOException e) {
			            e.printStackTrace();
			        } catch (NoSuchAlgorithmException e) {
			            e.printStackTrace();
			        }
			    }
			}
		}).start();
    }

    private void reconnect() {
        try {
            ws = ws.recreate().connect();
        } catch (WebSocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WebSocket getConnection() {
        return ws;
    }
    
    public void sendMessage(String message) {
    	if (ws != null && ws.isOpen()) {
    		ws.sendText(message);
    		listener.onMessageSend(message);
    	}
    }
    
    public void close() {
    	ws.disconnect();
    }
    
    public class SocketListener extends WebSocketAdapter {
        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
            super.onConnected(websocket, headers);
            listener.onConnected(headers);
        }

        public void onTextMessage(WebSocket websocket, String message) {
            listener.onTextMessage(message);
        }

        @Override
        public void onError(WebSocket websocket, WebSocketException cause) {
        	listener.onError(cause);
            reconnect();
        }

        @Override
        public void onDisconnected(WebSocket websocket,
                                   WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                                   boolean closedByServer) {
        	listener.onDisconnected(serverCloseFrame, clientCloseFrame, closedByServer);
            if (closedByServer) {
            	websocket.disconnect();
                reconnect();
            }
        }
        
        @Override
        public void onUnexpectedError(WebSocket websocket, WebSocketException cause) {
        	listener.onUnexpectedError(cause);
            reconnect();
        }

        @Override
        public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
            super.onPongFrame(websocket, frame);
            listener.onPongFrame(frame);
            websocket.sendPing("Are you there?");
        }
    }
    
    public interface MessageNotifier {
        void onTextMessage(String message);
        void onConnected(Map<String, List<String>> headers);
        void onError(WebSocketException cause);
        void onDisconnected(WebSocketFrame serverCloseFrame,
        		WebSocketFrame clientCloseFrame,
        		boolean closedByServer);
        void onUnexpectedError(WebSocketException cause);
        void onPongFrame(WebSocketFrame frame);
        void onMessageSend(String message);
    }
}
