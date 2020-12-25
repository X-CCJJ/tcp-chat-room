package com.xcj.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class Chat {
    private static CopyOnWriteArrayList<Channel> all = new CopyOnWriteArrayList<>();
    public static void main(String[] args) throws IOException {
        System.out.println("----Server----");
        ServerSocket server = new ServerSocket(8888);
        while (true){
            // 阻塞式等待连接accept
            Socket client = server.accept();
            System.out.println("一个客户端建立了连接");
            Channel c = new Channel(client);
            all.add(c);
            new Thread(c).start();
        }
    }
    static class Channel implements Runnable{
        private DataInputStream dis;
        private DataOutputStream dos;
        private Socket client;
        private boolean isRunning;
        private String name;
        public Channel(Socket client) {
            this.client = client;
            try {
                dis = new DataInputStream(client.getInputStream());
                dos = new DataOutputStream(client.getOutputStream());
                isRunning = true;
                this.name = receive();
                this.send("系统消息：欢迎来到聊天室，群聊请直接发送消息，私聊格式为@xxx:msg");
                sendOthers("系统消息：" + this.name+"来到了聊天室",true);
            } catch (IOException e) {
                release();
            }
        }

        // 接收消息
        private String receive(){
            String msg = "";
            try {
                msg = dis.readUTF();
            } catch (IOException e) {
                release();
            }
            return msg;
        }
        // 发送消息
        private void send(String msg){
            try {
                dos.writeUTF(msg);
                dos.flush();
            } catch (IOException e) {
                release();
            }
        }

        /**
         *  群聊：获取自己的消息，发给其他人
         *  私聊：约定数据格式：@xxx:msg
         * @param msg
         * @param isSys
         */
        private void sendOthers(String msg,boolean isSys){
            boolean isPrivate = msg.startsWith("@");
            if(isPrivate){ // 私聊
                int idx = -1;
                idx = msg.indexOf(":");
                if(idx != -1){
                    String targetName = msg.substring(1,idx);
                    msg = msg.substring(idx+1);
                    for (Channel other:all){
                        if(other.name.equals(targetName)){
                            other.send(this.name+"悄悄地对您说："+msg);
                        }
                    }
                }else send("私聊格式错误");

            }else {
                for (Channel other:all){
                    if (other == this)  continue;
                    if(!isSys){
                        other.send(this.name+"对所有人说："+msg); // 群聊消息
                    }else other.send(msg); // 系统消息
                }
            }
        }
        // 释放资源
        private void release(){
            this.isRunning =false;
            Utils.close(dis,dos,client);
            all.remove(this);
            sendOthers(this.name+"离开了聊天室",true);
        }

        @Override
        public void run() {
            while (isRunning){
                String msg = receive();
                if(!msg.equals("")){
                    sendOthers(msg,false);
                }
            }
        }
    }
}
