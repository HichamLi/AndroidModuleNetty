package com.itql.module.netty.callback;

public interface INettyCallback {
	void onConnectSuccess();

	void onDisconnect();

	void onMessage(String s);
}
