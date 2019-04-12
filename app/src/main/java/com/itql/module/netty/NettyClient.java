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

public class NettyClient {
    private NettyConfig mNettyConfig;
    private Channel mChannel;
    private Bootstrap mBootstrap;
    private Timer mCheckTimer;
    private volatile boolean mConnecting;
    private volatile long mNextHeartTime;
    private NettyClientHandler mNettyClientHandler;
    private ChannelFutureListener mSendListener;

    public NettyClient(NettyConfig config) {
        mNettyConfig = config;
        mNettyClientHandler = new NettyClientHandler(mNettyConfig.getCallback());
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
                                pipeline.addLast(mNettyClientHandler);
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
            close();
            mConnecting = true;
            mBootstrap.connect(new InetSocketAddress(mNettyConfig.getHost(), mNettyConfig.getPort())).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    mConnecting = false;
                    mChannel = future.channel();
                    System.out.println(future.isSuccess() ? "重连成功" : "重连失败");
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
            closeChannel();
            if (mCheckTimer != null) {
                mCheckTimer.cancel();
                mCheckTimer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeChannel() {
        if (mChannel != null) {
            mChannel.close();
            mChannel = null;
        }
    }

    private void sendHeart() {
        sendData(mNettyConfig.getHeartData());
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
                        if (!mConnecting) {
                            connect();
                        }
                    }
                }
            }, 2000L, 5000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendData(String s) {
        try {
            if (isOnline()) {
                mChannel.writeAndFlush(s).addListener(mSendListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

