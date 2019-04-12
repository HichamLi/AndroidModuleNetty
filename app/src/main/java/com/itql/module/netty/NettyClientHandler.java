package com.itql.module.netty;

import android.util.Log;

import com.itql.module.netty.callback.INettyCallback;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyClientHandler extends SimpleChannelInboundHandler<String> {
	public static final String TAG = "NettyClientHandler";
	private INettyCallback mCallback;

	public NettyClientHandler(INettyCallback callback) {
		mCallback = callback;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) {
		try {
			//收到消息，在这里进行解析
			if (mCallback != null) mCallback.onMessage(s);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) {
		try {
			super.channelUnregistered(ctx);
			Log.i(TAG, "channelUnregistered");
			if (mCallback != null) mCallback.onDisconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		try {
			super.channelActive(ctx);
			if (mCallback != null) mCallback.onConnectSuccess();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
