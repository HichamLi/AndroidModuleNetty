package com.itql.module.netty;

import com.itql.module.netty.callback.INettyCallback;

public class NettyConfig {
    private String mHost;
    private int mPort;
    private int mConnectTimeOut;
    private long mHeartInterval;
    private String mHeartData;
    private INettyCallback mCallback;

    public NettyConfig(Builder builder) {
        mHost = builder.mHost;
        mPort = builder.mPort;
        mConnectTimeOut = builder.mConnectTimeOut;
        mHeartInterval = builder.mHeartInterval;
        mHeartData = builder.mHeartData;
        mCallback = builder.mCallback;
    }

    public String getHost() {
        return mHost;
    }

    public int getPort() {
        return mPort;
    }

    public int getConnectTimeOut() {
        return mConnectTimeOut;
    }

    public long getHeartInterval() {
        return mHeartInterval;
    }

    public String getHeartData() {
        return mHeartData;
    }

    public INettyCallback getCallback() {
        return mCallback;
    }

    public static class Builder {
        String mHost;
        int mPort;
        int mConnectTimeOut = 10;
        int mHeartInterval = 50;
        String mHeartData = "H";
        INettyCallback mCallback;

        public Builder setHost(String host) {
            mHost = host;
            return this;
        }

        public Builder setPort(int port) {
            mPort = port;
            return this;
        }

        public Builder setConnectTimeOut(int timeOut) {
            mConnectTimeOut = timeOut;
            return this;
        }

        public Builder setHeartInterval(int interval) {
            mHeartInterval = interval;
            return this;
        }


        public Builder setHeartData(String data) {
            mHeartData = data;
            return this;
        }

        public Builder setCallback(INettyCallback callback) {
            mCallback = callback;
            return this;
        }

        public NettyConfig create() {
            return new NettyConfig(this);
        }
    }


}
