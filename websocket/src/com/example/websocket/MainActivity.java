package com.example.websocket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.CalendarContract.EventsEntity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.example.websocket.adapters.ChatListAdapter;
import com.example.websocket.model.BroadcastMessage;
import com.example.websocket.model.ChatMessage;
import com.example.websocket.model.UserType;
import com.pinta.ws_service.WsManager;
import com.pinta.ws_service.Consts.BroadcastConstant;
import com.pinta.ws_service.Consts.WsConstant;
import com.pinta.ws_service.services.WsService;

public class MainActivity extends Activity implements View.OnClickListener, WsManager.WsCallbackListeners {
	
	private ListView chatListView;
	private ChatListAdapter chatListAdapter;
	private static ArrayList<ChatMessage> chatMessagesList = new ArrayList<>();
	private String userSubscribeChannel = "sgc:user1";
	private static int iconId = R.drawable.ic_smiles_smile;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupUI();
        initListeners();
    }
    
    private void setupUI() {
        setContentView(R.layout.activity_main);
		chatListView = (ListView) findViewById(R.id.chatListView);
		chatListAdapter = new ChatListAdapter(this, chatMessagesList);
		chatListView.setAdapter(chatListAdapter);
		chatListView.invalidateViews();
		setConnectionIcon(iconId);
    }
    
    private void setConnectionIcon(int resourceId) {
    	iconId = resourceId;
    	getActionBar().setIcon(iconId);
    }
    
    private void initConnection() {
    	if (!EventBus.getDefault().isRegistered(this)) {
    		EventBus.getDefault().register(this);
    	}
    	
        WsManager.getWsManager()
	        .setPort(BuildConfig.WEBSOCKET_URL)
	        .setLog(true)
	        .setHeartBeat(6000L)
	        .connect(this);
    }
    
    private void initListeners() {
        findViewById(R.id.b_subscribe).setOnClickListener(this);
        findViewById(R.id.b_call).setOnClickListener(this);
        findViewById(R.id.b_publish).setOnClickListener(this);
        findViewById(R.id.b_subscribe_off).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        WsManager.getWsManager().disconnect(this);
        super.onDestroy();
    }
    
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.b_subscribe:
                WsManager.getWsManager().subscribe(this,
                        ((EditText) findViewById(R.id.et_input_subscribe)).getText().toString());
                break;
            case R.id.b_call:
                WsManager.getWsManager().call(this,
                        ((EditText) findViewById(R.id.et_input_call)).getText().toString(),
                        ((EditText) findViewById(R.id.et_input_call_param)).getText().toString());
                break;
            case R.id.b_publish:
                WsManager.getWsManager().publish(this,
                        ((EditText) findViewById(R.id.et_input_publish_topic)).getText().toString(),
                        ((EditText) findViewById(R.id.et_input_publish_message)).getText().toString());
                break;
            case R.id.b_subscribe_off:
            	WsManager.getWsManager().unsubscribe(this,
            			((EditText) findViewById(R.id.et_input_subscribe)).getText().toString());
            	break;
        }
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_disconnect) {
			if (WsService.isCreated()) {
				onWsCloseCallbackListener("forced disconnect from server");
			}
			WsManager.getWsManager().disconnect(this);
		}
		if (id == R.id.action_connect) {
			initConnection();
		}
		return super.onOptionsItemSelected(item);
	}
    
    public ChatMessage giveMeChatMessage(String message, UserType userType) {
    	ChatMessage chatMessage = new ChatMessage();
    	chatMessage.setMessageText(message);
    	chatMessage.setUserType(userType);
    	chatMessage.setMessageTime(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new java.util.Date()));
    	return chatMessage;
    }
    
    @Override
    public void onWsOpenCallbackListener() {
    	WsManager.getWsManager().subscribe(this, userSubscribeChannel);
        Toast.makeText(this, "ws connected successfully", Toast.LENGTH_SHORT).show();
    	chatMessagesList.add(giveMeChatMessage("Connected", UserType.Service));
		chatListView.invalidateViews();
		setConnectionIcon(R.drawable.ic_smiles_smile_active);
    }

    @Override
    public void onWsCloseCallbackListener(String onCloseMessage) {
        Toast.makeText(this, "ws was closed with error: " + onCloseMessage, Toast.LENGTH_SHORT).show();
    	chatMessagesList.add(giveMeChatMessage("Disconnected", UserType.Service));
    	chatListView.invalidateViews();
    	setConnectionIcon(R.drawable.ic_smiles_smile);
    }

    @Override
    public void onWsSubscribeCallbackListener(String onSubscribeMessage) {
        Toast.makeText(this, "ws subscribe response: " + onSubscribeMessage, Toast.LENGTH_SHORT).show();
    	chatMessagesList.add(giveMeChatMessage(onSubscribeMessage, UserType.Server));
    	chatListView.invalidateViews();
    }

    @Override
    public void onWsCallCallbackListener(final String onCallMessage) {
        Toast.makeText(this, "ws call response: " + onCallMessage, Toast.LENGTH_SHORT).show();
		chatMessagesList.add(giveMeChatMessage(onCallMessage, UserType.Server));
		chatListView.invalidateViews();
    }

	@Override
	public void onWsUnSubscribeCallbackListener(String onUnSubscribeMessage) {
        Toast.makeText(this, "ws unsubscribe: " + onUnSubscribeMessage, Toast.LENGTH_SHORT).show();
		chatMessagesList.add(giveMeChatMessage(onUnSubscribeMessage, UserType.Service));
		chatListView.invalidateViews();
	}
	
	@Subscribe(threadMode = ThreadMode.MAIN)  
	public void onMessageEvent(BroadcastMessage event) {
		if (event.id == WsConstant.WS_SUBSCRIBE) {
			onWsSubscribeCallbackListener(event.message);
		} else if (event.id == WsConstant.WS_CALL) {
			onWsCallCallbackListener(event.message);
		} else if (event.id == WsConstant.WS_CONNECT_CLOSE) {
			onWsCloseCallbackListener(event.message);
		} else if (event.id == WsConstant.WS_CONNECT_OPEN) {
			onWsOpenCallbackListener();
		} else if (event.id == WsConstant.WS_UNSUBSCRIBE) {
			onWsUnSubscribeCallbackListener(event.message);
		}
	};
}