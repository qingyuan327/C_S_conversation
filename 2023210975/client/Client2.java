package com.mahaijuan.client;

import java.io.IOException;
import java.net.Socket;

/**
 * 客户端2类，用于连接服务器并启动登录界面
 */
public class Client2 {
    public static void main(String[] args) {
        try {
            // 调用连接服务器并登录的方法
            connectToServerAndLogin();
        } catch (IOException | InterruptedException e) {
            // 打印异常堆栈信息
            e.printStackTrace();
        }
    }

    /**
     * 连接到服务器并启动登录界面的方法
     * @throws IOException 当发生输入输出异常时抛出
     * @throws InterruptedException 当线程被中断时抛出
     */
    private static void connectToServerAndLogin() throws IOException, InterruptedException {
        // 创建一个与本地地址127.0.0.1，端口8888的服务器的套接字连接
        Socket socket = new Socket("127.0.0.1", 8888);
        // 创建登录界面实例
        new LoginFrame(socket);
    }
}