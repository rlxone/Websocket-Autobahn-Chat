package com.example.websocket;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.websocket.adapters.ChatListAdapter;
import com.example.websocket.library.BackgroundManager;
import com.example.websocket.library.ExampleSocketConnection;
import com.example.websocket.library.RealTimeMessage;
import com.example.websocket.model.ChatMessage;
import com.example.websocket.model.UserType;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

public class MainActivity extends Activity {
	
	private ExampleSocketConnection exampleSocketConnection;
	private ListView chatListView;
	private ChatListAdapter chatListAdapter;
	private List<ChatMessage> chatMessagesList = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        exampleSocketConnection = new ExampleSocketConnection(this);
        BackgroundManager.get(this.getApplication()).registerListener(appActivityListener);
		setupUI();
        setButtonsListeners();
	}
	
	public void setButtonsListeners() {
		Button buttonOne = (Button) findViewById(R.id.sendButton);
		buttonOne.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		    	String message = ((EditText) findViewById(R.id.sendEditText)).getText().toString();
		    	exampleSocketConnection.sendMessage(message);
		    }
		});
	}
	
	public void setupUI() {
		setContentView(R.layout.activity_main);
		chatListView = (ListView) findViewById(R.id.chatListView);
		chatListAdapter = new ChatListAdapter(this, chatMessagesList);
		chatListView.setAdapter(chatListAdapter);
        getActionBar().setIcon(R.drawable.ic_smiles_smile);
	}
	
	@Override
	public void onStart() {
	    super.onStart();
	    EventBus.getDefault().register(this);
	}

	@Override
	public void onStop() {
	    super.onStop();
	    EventBus.getDefault().unregister(this);
	}
	
    public void closeSocketConnection() {
        exampleSocketConnection.closeConnection();
    }

    public void openSocketConnection() {
        exampleSocketConnection.openConnection();
    }

    public boolean isSocketConnected() {
        return exampleSocketConnection.isConnected();
    }

    public void reconnect() {
        exampleSocketConnection.openConnection();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_disconnect) {
			closeSocketConnection();
		}
		if (id == R.id.action_connect) {
			if (!isSocketConnected()) {
				openSocketConnection();
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	private BackgroundManager.Listener appActivityListener = new BackgroundManager.Listener() {
        public void onBecameForeground() {
            openSocketConnection();
            Log.i("Websocket", "Became Foreground");
        }

        public void onBecameBackground() {
            closeSocketConnection();
            Log.i("Websocket", "Became Background");
        }
    };
    
    public ChatMessage giveMeChatMessage(String message, UserType userType) {
    	ChatMessage chatMessage = new ChatMessage();
    	chatMessage.setMessageText(message);
    	chatMessage.setUserType(userType);
    	chatMessage.setMessageTime(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new java.util.Date()));
    	return chatMessage;
    }
    
    public void onTextMessage(String message) {
    	chatMessagesList.add(giveMeChatMessage(message, UserType.Server));
    	chatListView.invalidateViews();
    }
    
    public void onSendMessage(String message) {
    	chatMessagesList.add(giveMeChatMessage(message, UserType.Me));
    	chatListView.invalidateViews();
    }

    public void onConnected(Map<String, List<String>> headers) throws Exception {
    	chatMessagesList.add(giveMeChatMessage("Connected", UserType.Service));
    	chatListView.invalidateViews();
    	getActionBar().setIcon(R.drawable.ic_smiles_smile_active);
    }

    public void onError(WebSocketException cause) {
    	chatMessagesList.add(giveMeChatMessage("Error", UserType.Service));
    	chatListView.invalidateViews();
    }

    public void onDisconnected(WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                               boolean closedByServer) {
    	chatMessagesList.add(giveMeChatMessage("Disconnect", UserType.Service));
    	chatListView.invalidateViews();
    	getActionBar().setIcon(R.drawable.ic_smiles_smile);
    }
    
    public void onUnexpectedError(WebSocketException cause) {
    	chatMessagesList.add(giveMeChatMessage("Unexpected Error", UserType.Service));
    	chatListView.invalidateViews();;
    }

    public void onPongFrame(WebSocketFrame frame) throws Exception {
    	chatMessagesList.add(giveMeChatMessage("Pong", UserType.Service));
    	//chatListView.invalidateViews();
    }
    
    @Subscribe(threadMode = ThreadMode.MAIN)  
    public void onMessageEvent(RealTimeMessage event) throws Exception {
    	Log.i("Websocket", event.type.name());
    	switch (event.type) {
    	case Connected:
    		onConnected((Map<String, List<String>>) event.objects.get(0));
    		break;
    	case Disconnected:
    		onDisconnected((WebSocketFrame) event.objects.get(0), (WebSocketFrame)event.objects.get(1), (boolean)event.objects.get(2));
    		break;
    	case Error:
    		onError((WebSocketException)event.objects.get(0));
    		break;
    	case UnexpectedError:
    		onUnexpectedError((WebSocketException)event.objects.get(0));
    		break;
    	case Pong:
    		onPongFrame((WebSocketFrame) event.objects.get(0));
    		break;
    	case Send:
    		onSendMessage((String) event.objects.get(0));
    		break;
    	case TextMessage:
    		onTextMessage((String) event.objects.get(0));
    		break;
		default:
			break;
    	}
    };
}
