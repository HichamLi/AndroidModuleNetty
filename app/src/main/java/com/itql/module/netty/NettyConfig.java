package com.itql.module.netty;

public class NettyConfig {
    private String mHost;
    private int mPort;
    private int mHeartInterval;
    private String mHeartData;

    public NettyConfig(Builder builder) {
        mHost = builder.mHost;
        mPort = builder.mPort;
        mHeartInterval = builder.mHeartInterval;
        mHeartData = builder.mHeartData;
    }

    public String getHost() {
        return mHost;
    }

    public int getPort() {
        return mPort;
    }

    public int getHeartInterval() {
        return mHeartInterval;
    }

    public String getHeartData() {
        return mHeartData;
    }

    public static class Builder {
        String mHost;
        int mPort;
        int mHeartInterval = 50;
        String mHeartData = "H";

        public Builder setHost(String host) {
            mHost = host;
            return this;
        }

        public Builder setPort(int port) {
            mPort = port;
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

        public NettyConfig create() {
            return new NettyConfig(this);
        }
    }


}
