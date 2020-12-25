package com.xcj.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        System.out.println("----Client----");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("请输入用户名：");
        String name = br.readLine();
        Socket client = new Socket("localhost", 8888);
        // 客户端收发消息
        new Thread(new Send(client,name)).start();
        new Thread(new Receive(client)).start();
    }
}
