package com.example.websocket.adapters;

import java.util.ArrayList;
import java.util.List;

import com.example.websocket.R;
import com.example.websocket.R.id;
import com.example.websocket.R.layout;
import com.example.websocket.model.ChatMessage;
import com.example.websocket.model.UserType;
import com.example.websocket.model.ViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatListAdapter extends BaseAdapter {
    Context context;
    List<ChatMessage> data = new ArrayList<>();

    public ChatListAdapter(Context context, List<ChatMessage> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
    	return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	View view = convertView;
        ViewHolder viewHolder;
        if (view == null) {
        	viewHolder = new ViewHolder();
        	LayoutInflater inflater = (LayoutInflater) context
        			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	switch (data.get(position).getUserType()) {
        	case Server:
        		view = inflater.inflate(R.layout.chat_user1_item, null);
        		viewHolder.view = view;
        		viewHolder.authorView = (TextView) view.findViewById(R.id.author);
        		viewHolder.textView = (TextView) view.findViewById(R.id.text);
        		viewHolder.timeView = (TextView) view.findViewById(R.id.time);
        		view.setTag(viewHolder);
        		break;
        	case Me:
        	case Service:
        		view = inflater.inflate(R.layout.chat_user2_item, null);
        		viewHolder.view = view;
        		viewHolder.textView = (TextView) view.findViewById(R.id.service_message1);
        		viewHolder.timeView = (TextView) view.findViewById(R.id.service_date1);
        		break;
        	}
        	view.setTag(viewHolder);
        	
        } else
        	viewHolder = (ViewHolder) view.getTag();
        
        view = viewHolder.view;
        
    	if (viewHolder.authorView != null) {
    		viewHolder.authorView.setText(data.get(position).getUserType().name());
    		viewHolder.textView.setText(data.get(position).getMessageText());
    		viewHolder.timeView.setText(data.get(position).getMessageTime());
    	} else {
    		viewHolder.textView.setText(data.get(position).getMessageText());
    		viewHolder.timeView.setText(data.get(position).getMessageTime());
    	}
        
        return view;
    }
}
