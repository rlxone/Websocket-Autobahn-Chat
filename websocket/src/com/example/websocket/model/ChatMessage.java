package com.example.websocket.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatMessage implements Parcelable {
    private String messageText;
    private UserType userType;
    private String messageDateTime;
    
    public String getMessageTime() {
        return messageDateTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageDateTime = messageTime;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getMessageText() {
        return messageText;
    }

    public UserType getUserType() {
        return userType;
    }

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}
}
