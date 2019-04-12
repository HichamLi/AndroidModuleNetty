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
        private String mHost;
        private int mPort;
        private int mConnectTimeOut = 10000;
        private long mHeartInterval = 30000L;
        private String mHeartData = "H";
        private INettyCallback mCallback;

        public Builder(String host, int port) {
            mHost = host;
            mPort = port;
        }

        /**
         * @param timeOut 单位毫秒，最小是5000（5秒）
         * @return Builder
         */
        public Builder setConnectTimeOut(int timeOut) {
            mConnectTimeOut = Math.max(timeOut, 5000);
            return this;
        }

        /**
         * @param interval 单位毫秒，最小是5000（5秒）
         * @return Builder
         */
        public Builder setHeartInterval(long interval) {
            mHeartInterval = Math.max(interval, 5000L);
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

        public NettyConfig build() {
            return new NettyConfig(this);
        }
    }
}
