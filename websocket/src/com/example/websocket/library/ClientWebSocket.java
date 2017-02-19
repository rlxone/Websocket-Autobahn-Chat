package com.example.websocket.library;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketError;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import android.util.Log;

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

    public void connect() throws IOException, WebSocketException {
    	new Thread(new Runnable() {
			public void run() {
				try {
		            WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(3000);
		            SSLContext context = NaiveSSLContext.getInstance("TLS");
		            factory.setSSLContext(context);
		            ws = factory.createSocket(host);
		            ws.getSocket().setKeepAlive(true);
		            ws.addListener(new SocketListener());
		            ws.connect();
		        } catch (IOException e) {
		            e.printStackTrace();
		        } catch (NoSuchAlgorithmException e) {
		            e.printStackTrace();
		        } catch (WebSocketException e) {
					try {
						connect();
					} catch (IOException | WebSocketException e1) {
						e1.printStackTrace();
					}
				}
			}
		}).start();
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
    	public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
    		super.onConnectError(websocket, exception);
    		//reconnect();
    	}
    	
    	@Override
    	public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
    		//super.onCloseFrame(websocket, frame);
    	}
    	
        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
            super.onConnected(websocket, headers);
            listener.onConnected(headers);
        }

        public void onTextMessage(WebSocket websocket, String message) {
            listener.onTextMessage(message);
        }

        @Override
        public void onError(WebSocket websocket, WebSocketException cause) throws WebSocketException, IOException {
        	listener.onError(cause);
        	//connect();
        }
        
        @Override
        public void onDisconnected(WebSocket websocket,
                                   WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                                   boolean closedByServer) throws WebSocketException, IOException {
        	if (clientCloseFrame.getCloseCode() != 1000) {
        		connect();
        		listener.onDisconnected(serverCloseFrame, clientCloseFrame, closedByServer);
        	} else {
        		if (closedByServer) {
        			listener.onDisconnectedByServer(serverCloseFrame, clientCloseFrame, closedByServer);
        		} else {
        			listener.onDisconnected(serverCloseFrame, clientCloseFrame, closedByServer);
        		}
        	}
        }
        
        @Override
        public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws WebSocketException, IOException {
        	listener.onUnexpectedError(cause);
            //reconnect();
        }

        @Override
        public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
            super.onPongFrame(websocket, frame);
            listener.onPongFrame(frame);
        }
        
        @Override
        public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        	super.onPingFrame(websocket, frame);
        	listener.onPingFrame(frame);
        }
    }
    
    public interface MessageNotifier {
        void onTextMessage(String message);
        void onConnected(Map<String, List<String>> headers);
        void onError(WebSocketException cause);
        void onDisconnected(WebSocketFrame serverCloseFrame,
        		WebSocketFrame clientCloseFrame,
        		boolean closedByServer);
        void onDisconnectedByServer(WebSocketFrame serverCloseFrame,
        		WebSocketFrame clientCloseFrame,
        		boolean closedByServer);
        void onUnexpectedError(WebSocketException cause);
        void onPongFrame(WebSocketFrame frame);
        void onPingFrame(WebSocketFrame frame);
        void onMessageSend(String message);
    }
}
