package com.itql.module.netty.callback;

public interface INettyCallback {
    void onConnectSuccess();

    void onDisconnect();

    void onMessage(String s);

    void onSendSuccess();

    void onSendFail();

    class SimpleCallback implements INettyCallback{

        @Override
        public void onConnectSuccess() {

        }

        @Override
        public void onDisconnect() {

        }

        @Override
        public void onMessage(String s) {

        }

        @Override
        public void onSendSuccess() {

        }

        @Override
        public void onSendFail() {

        }
    }
}
