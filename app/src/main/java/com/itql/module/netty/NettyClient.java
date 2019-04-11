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
    private Timer mHeartTimer;
    private volatile boolean mConnecting;

    private NettyClient() {
        NettyConfig.Builder builder = new NettyConfig.Builder();
        mNettyConfig = builder.setHost(BuildConfig.TCP_HOST).setPort(BuildConfig.TCP_PORT).create();
    }

    public static final class Holder {
        private static final NettyClient INSTANCE = new NettyClient();
    }

    public static NettyClient getInstance() {
        return Holder.INSTANCE;
    }

    public void setNettyConfig(NettyConfig config) {
        mNettyConfig = config;
    }

    private void connect() {
        try {
            if (isOnline()) {
                System.out.println("已经在线");
                return;
            }
            if (mBootstrap == null) {
                mBootstrap = new Bootstrap()
                        .channel(NioSocketChannel.class)
                        .group(new NioEventLoopGroup())
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, mNettyConfig.getConnectTimeOut() * 1000)
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
            if (mConnecting) {
                System.out.println("正在重连，跳过");
                return;
            }
            System.out.println("重连");
            mConnecting = true;
            mBootstrap.connect(new InetSocketAddress(mNettyConfig.getHost(), mNettyConfig.getPort())).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    System.out.println("重连操作结束");
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
            if (mCheckTimer != null) {
                mCheckTimer.cancel();
                mCheckTimer = null;
            }
            if (mHeartTimer != null) {
                mHeartTimer.cancel();
                mHeartTimer = null;
            }
            if (mChannel != null) {
                mChannel.close();
                mChannel = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                    if (!isOnline() && !mConnecting) {
                        connect();
                    }
                }
            }, 2000L, mNettyConfig.getCheckInterval());

            mHeartTimer = new Timer();
            mHeartTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isOnline()) {
                        sendHeart();
                    }
                }
            }, 2000L, mNettyConfig.getHeartInterval());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendData(String s) {
        try {
            if (isOnline()) {
                mChannel.writeAndFlush(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

