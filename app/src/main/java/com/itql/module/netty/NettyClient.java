package com.itql.module.netty;

import com.itql.module.netty.callback.INettyCallback;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyClient {
    private NettyConfig mNettyConfig;
    private Channel mChannel;
    private Bootstrap mBootstrap;
    private Timer mHeartTimer;
    private ChannelFuture mChannelFuture;
    private EventLoopGroup mGroup;
    private INettyCallback mCallback;

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
            if (isOnline()) return;
            mBootstrap = new Bootstrap();
            mBootstrap.channel(NioSocketChannel.class);
            mGroup = new NioEventLoopGroup();
            mBootstrap.group(mGroup);
            mBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) {
                    //获取管道
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    //字符串解码器
                    pipeline.addLast(new StringDecoder());
                    //字符串编码器
                    pipeline.addLast(new StringEncoder());
                    //处理类
                    pipeline.addLast(new NettyClientHandler(mCallback));
                }
            });
            mChannelFuture = mBootstrap.connect(new InetSocketAddress(BuildConfig.TCP_HOST, BuildConfig.TCP_PORT)).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    mChannel = future.channel();
                }
            }).sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isOnline() {
        return mChannel != null && mChannel.isActive() && mChannel.isWritable();
    }

    public void closeAll() {
        close();
        if (mHeartTimer != null) {
            mHeartTimer.cancel();
            mHeartTimer = null;
        }
    }

    private void close() {
        try {
            if (mChannel != null) {
                mChannel.close();
                mChannel = null;
            }
            if (mChannelFuture != null) {
                mChannelFuture.channel().close();
                mChannelFuture.cancel(true);
                mChannelFuture = null;
            }
            if (mGroup != null) {
                mGroup.shutdownGracefully();
                mGroup = null;
            }
            if (mBootstrap != null) {
                mBootstrap = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendHeart() {
        sendData(mNettyConfig.getHeartData());
    }

    public void start(INettyCallback callback) {
        try {
            closeAll();
            mHeartTimer = new Timer();
            if (mCallback == null) mCallback = callback;
            mHeartTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isOnline()) {
                        sendHeart();
                    } else {
                        close();
                        connect();
                    }
                }
            }, 2000, mNettyConfig.getHeartInterval());
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

