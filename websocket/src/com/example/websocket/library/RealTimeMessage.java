package com.example.websocket.library;

import java.util.List;

public class RealTimeMessage {
	public enum Type {
		Connected,
		TextMessage,
		Error,
		UnexpectedError,
		Pong,
		Ping,
		Disconnected,
		DisconnectedByServer,
		Send
	}
	
	public Type type;
	public List<Object> objects;
}