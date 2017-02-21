package com.example.websocket.adapters;

import java.util.ArrayList;
import java.util.List;

import com.example.websocket.R;
import com.example.websocket.R.id;
import com.example.websocket.R.layout;
import com.example.websocket.model.ChatMessage;
import com.example.websocket.model.UserType;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatListAdapter extends BaseAdapter {
    Context context;
    List<ChatMessage> data = new ArrayList<>();

    static class ViewHolder1 {
        public TextView authorView;
        public TextView textView;
        public TextView timeView;
    }	
    
    static class ViewHolder2 {
        public TextView authorView;
        public TextView textView;
        public TextView timeView;
    }	
    
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
        ViewHolder1 viewHolder1;
        ViewHolder2 viewHolder2;
        
        if (data.get(position).getUserType() == UserType.Server) {
            if (view == null) {
            	viewHolder1 = new ViewHolder1();
            	LayoutInflater inflater = (LayoutInflater) context
            			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        		view = inflater.inflate(R.layout.chat_user1_item, null);
        		viewHolder1.authorView = (TextView) view.findViewById(R.id.author);
        		viewHolder1.textView = (TextView) view.findViewById(R.id.text);
        		viewHolder1.timeView = (TextView) view.findViewById(R.id.time);
            	view.setTag(viewHolder1);
            	
            } else {
            	viewHolder1 = (ViewHolder1) view.getTag();
            }
            
            viewHolder1.authorView.setText(data.get(position).getUserType().name());
            viewHolder1.textView.setText(data.get(position).getMessageText());
            viewHolder1.timeView.setText(data.get(position).getMessageTime());
        } else {
            if (view == null) {
            	viewHolder2 = new ViewHolder2();
            	LayoutInflater inflater = (LayoutInflater) context
            			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        		view = inflater.inflate(R.layout.chat_user2_item, null);
        		viewHolder2.authorView = (TextView) view.findViewById(R.id.author);
        		viewHolder2.textView = (TextView) view.findViewById(R.id.text);
        		viewHolder2.timeView = (TextView) view.findViewById(R.id.time);
            	view.setTag(viewHolder2);
            	
            } else {
            	viewHolder2 = (ViewHolder2) view.getTag();
            }
            
            viewHolder2.authorView.setText(data.get(position).getUserType().name());
            viewHolder2.textView.setText(data.get(position).getMessageText());
    		viewHolder2.timeView.setText(data.get(position).getMessageTime());
        }
        
        return view;
    }
    
    @Override
    public int getViewTypeCount() {
    	return 2;
    }
    
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = data.get(position);
        return (message.getUserType().ordinal() >= 1) ? 1 : 0;
    }
}
