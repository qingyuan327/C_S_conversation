package com.mahaijuan.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * 客户端处理类，用于处理客户端的聊天界面和消息收发
 */
public class ClientHandle extends JFrame implements ActionListener, KeyListener, Runnable {
    private JTextArea chatArea; // 聊天区域文本框
    private JScrollPane scrollPane; // 滚动面板，用于显示聊天区域
    private JPanel inputPanel; // 输入面板，用于放置输入框和发送按钮
    private JTextField inputField; // 输入文本框
    private JButton sendButton; // 发送按钮
    private PrintStream outputStream; // 输出流，用于向服务器发送消息
    private Socket socket; // 客户端套接字
    private String userName; // 用户名

    /**
     * 构造函数，初始化客户端处理类
     * @param name 用户名
     * @param clientSocket 客户端套接字
     */
    public ClientHandle(String name, Socket clientSocket) {
        this.userName = name;
        this.socket = clientSocket;
        // 初始化用户界面
        initUI();
    }

    /**
     * 初始化用户界面的方法
     */
    private void initUI() {
        chatArea = new JTextArea(); // 创建聊天区域文本框
        scrollPane = new JScrollPane(chatArea); // 创建滚动面板并添加聊天区域
        inputPanel = new JPanel(); // 创建输入面板
        inputField = new JTextField(10); // 创建输入文本框
        sendButton = new JButton("发送"); // 创建发送按钮

        inputPanel.add(inputField); // 将输入文本框添加到输入面板
        inputPanel.add(sendButton); // 将发送按钮添加到输入面板

        add(scrollPane, BorderLayout.CENTER); // 将滚动面板添加到窗口中央
        add(inputPanel, BorderLayout.SOUTH); // 将输入面板添加到窗口底部

        setTitle(userName + " 聊天框"); // 设置窗口标题
        setSize(300, 300); // 设置窗口大小
        setLocation(700, 300); // 设置窗口位置
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置窗口关闭操作
        setVisible(true); // 显示窗口

        sendButton.addActionListener(this); // 为发送按钮添加事件监听器
        inputField.addKeyListener(this); // 为输入文本框添加按键监听器
    }

    /**
     * 线程运行方法，用于接收服务器发送的消息
     */
    @Override
    public void run() {
        try (Scanner scanner = new Scanner(socket.getInputStream())) {
            while (true) {
                while (scanner.hasNext()) {
                    // 将接收到的消息添加到聊天区域
                    chatArea.append(scanner.next() + System.lineSeparator());
                }
            }
        } catch (IOException e) {
            // 打印异常堆栈信息
            e.printStackTrace();
        }
    }

    /**
     * 处理动作事件的方法，当点击发送按钮时调用
     * @param event 动作事件
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        // 调用发送消息到服务器的方法
        sendMessageToServer();
    }

    /**
     * 处理按键按下事件的方法，当按下回车键时调用
     * @param e 按键事件
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            // 调用发送消息到服务器的方法
            sendMessageToServer();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    /**
     * 发送消息到服务器的方法
     */
    private void sendMessageToServer() {
        String message = inputField.getText(); // 获取输入文本框中的消息
        chatArea.append("我：" + message + "\n"); // 将消息添加到聊天区域
        try {
            outputStream = new PrintStream(socket.getOutputStream()); // 创建输出流
            if (message.startsWith("@@")) {
                // 发送系统命令
                outputStream.println(message);
            } else if (message.startsWith("@") && message.contains("-")) {
                // 发送私聊消息
                outputStream.println(message);
            } else {
                // 发送群聊消息
                outputStream.println(message);
            }
            outputStream.flush(); // 刷新输出流
            inputField.setText(""); // 清空输入文本框
        } catch (IOException e) {
            // 打印异常堆栈信息
            e.printStackTrace();
        }
    }
}