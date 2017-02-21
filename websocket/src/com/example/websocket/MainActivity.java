package com.example.websocket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import com.example.websocket.adapters.ChatListAdapter;
import com.example.websocket.model.ChatMessage;
import com.example.websocket.model.UserType;
import com.pinta.ws_service.WsManager;
import com.pinta.ws_service.services.WsService;

public class MainActivity extends Activity implements View.OnClickListener, WsManager.WsCallbackListeners {
	
	private ListView chatListView;
	private ChatListAdapter chatListAdapter;
	private ArrayList<ChatMessage> chatMessagesList = new ArrayList<>();
	private String userSubscribeChannel = "sgc:user1";
	
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
        getActionBar().setIcon(R.drawable.ic_smiles_smile);
    }
    
    private void initConnection() {
        WsManager.getWsManager().registerCallback(this, this);
        WsManager.getWsManager()
	        .setPort(BuildConfig.WEBSOCKET_URL)
	        .setLog(true)
	        .setHeartBeat(10000L)
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
        super.onDestroy();
        stopService(new Intent(this, WsService.class));
        //WsManager.getWsManager().disconnect(this);
    }
    
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	//WsManager.getWsManager().unregisterCallback(this);
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
			WsManager.getWsManager().disconnect(this);
			onWsCloseCallbackListener("forced disconnect from server");
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
		getActionBar().setIcon(R.drawable.ic_smiles_smile_active);
    }

    @Override
    public void onWsCloseCallbackListener(String onCloseMessage) {
        Toast.makeText(this, "ws was closed with error: " + onCloseMessage, Toast.LENGTH_SHORT).show();
    	chatMessagesList.add(giveMeChatMessage("Disconnected", UserType.Service));
    	chatListView.invalidateViews();
        getActionBar().setIcon(R.drawable.ic_smiles_smile);
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
}