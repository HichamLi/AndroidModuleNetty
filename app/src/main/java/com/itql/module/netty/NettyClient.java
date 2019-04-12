package com.itql.module.netty;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.JdkLoggerFactory;

public class NettyClient {
    private NettyConfig mNettyConfig;
    private Channel mChannel;
    private Bootstrap mBootstrap;
    private Timer mCheckTimer;
    private volatile boolean mConnecting;
    private volatile long mNextHeartTime;
    private volatile long mNextConnectTime;
    private ChannelFutureListener mSendListener;
    private static final int[] mConnectInterval = {10000, 10000, 10000, 30000, 30000, 30000, 60000, 60000, 120000, 120000, 180000, 180000, 300000, 300000};
    private int mConnectCount = -1;

    public NettyClient(NettyConfig config) {
        mNettyConfig = config;
        mSendListener = new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    mNextHeartTime = System.currentTimeMillis() + mNettyConfig.getHeartInterval();
                    if (mNettyConfig.getCallback() != null)
                        mNettyConfig.getCallback().onSendSuccess();
                } else {
                    if (mNettyConfig.getCallback() != null) mNettyConfig.getCallback().onSendFail();
                }
            }
        };
    }

    private void connect() {
        try {
            if (mBootstrap == null) {
                InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE);
                mBootstrap = new Bootstrap()
                        .channel(NioSocketChannel.class)
                        .group(new NioEventLoopGroup())
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, mNettyConfig.getConnectTimeOut())
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) {
                                //获取管道
                                ChannelPipeline pipeline = socketChannel.pipeline();
                                //字符串解码器
                                pipeline.addLast(new StringDecoder());
                                //字符串编码器
                                pipeline.addLast(new StringEncoder());
                                //处理类
                                pipeline.addLast(new NettyClientHandler(mNettyConfig.getCallback()));
                            }
                        });
            }
            if (isOnline()) {
                System.out.println("已经在线");
                return;
            }
            if (mConnecting) {
                System.out.println("正在重连，跳过");
                return;
            }
            mConnecting = true;
            mBootstrap.connect(new InetSocketAddress(mNettyConfig.getHost(), mNettyConfig.getPort())).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    mConnecting = false;
                    mChannel = future.channel();
                    if (future.isSuccess()) {
                        mConnectCount = 0;
                        mNextConnectTime = 0;
                    } else {
                        mConnectCount++;
                        if (mConnectCount >= mConnectInterval.length) {
                            mConnectCount = 0;
                        }
                        mNextConnectTime = System.currentTimeMillis() + mConnectInterval[mConnectCount];
                    }
                }
            }).sync();
        } catch (Exception e) {
            e.printStackTrace();
            mConnecting = false;
        }
    }

    private boolean isOnline() {
        return mChannel != null && mChannel.isActive();
    }

    public void stop() {
        close();
        if (mBootstrap != null) {
            mBootstrap = null;
        }
    }

    private void close() {
        try {
            if (mChannel != null) {
                mChannel.close();
                mChannel = null;
            }
            if (mCheckTimer != null) {
                mCheckTimer.cancel();
                mCheckTimer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void start() {
        try {
            close();
            mCheckTimer = new Timer();
            mCheckTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isOnline()) {
                        if (System.currentTimeMillis() >= mNextHeartTime) {
                            sendHeart();
                        }
                    } else {
                        if (!mConnecting && System.currentTimeMillis() >= mNextConnectTime) {
                            connect();
                        }
                    }
                }
            }, 0L, 5000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendData(String s) {
        try {
            if (isOnline()) {
                mChannel.writeAndFlush(s).addListener(mSendListener);
            } else {
                if (mNettyConfig.getCallback() != null) mNettyConfig.getCallback().onSendFail();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendHeart() {
        try {
            mNextHeartTime = System.currentTimeMillis() + mNettyConfig.getHeartInterval();
            if (isOnline()) {
                mChannel.writeAndFlush(mNettyConfig.getHeartData());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

