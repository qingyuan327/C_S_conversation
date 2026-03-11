package com.mahaijuan.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

/**
 * 登录界面类，用于用户登录
 */
public class LoginFrame extends JFrame implements ActionListener, KeyListener, FocusListener {
    private Socket clientSocket; // 客户端套接字
    private String userName; // 用户名
    private JPanel panel; // 面板，用于放置输入框和按钮
    private JTextField inputField; // 输入文本框
    private JButton confirmButton; // 确认按钮
    private String hintText = "请输入用户名"; // 提示文本
    private String errorText = "用户名不为空或不规范，请重新输入"; // 错误提示文本

    /**
     * 构造函数，初始化登录界面类
     * @param socket 客户端套接字
     */
    public LoginFrame(Socket socket) {
        this.clientSocket = socket;
        // 设置用户界面
        setupUI();
    }

    /**
     * 设置用户界面的方法
     */
    private void setupUI() {
        panel = new JPanel(); // 创建面板
        inputField = new JTextField(20); // 创建输入文本框
        inputField.setText(hintText); // 设置输入文本框的初始提示文本
        inputField.setForeground(Color.gray); // 设置提示文本的颜色为灰色
        confirmButton = new JButton("确定"); // 创建确认按钮

        panel.add(inputField); // 将输入文本框添加到面板
        panel.add(confirmButton); // 将确认按钮添加到面板
        add(panel); // 将面板添加到窗口

        setTitle("用户登录"); // 设置窗口标题
        setSize(300, 100); // 设置窗口大小
        setLocation(700, 300); // 设置窗口位置
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置窗口关闭操作
        setVisible(true); // 显示窗口

        confirmButton.addActionListener(this); // 为确认按钮添加事件监听器
        inputField.addKeyListener(this); // 为输入文本框添加按键监听器
        inputField.addFocusListener(this); // 为输入文本框添加焦点监听器
    }

    /**
     * 处理动作事件的方法，当点击确认按钮时调用
     * @param event 动作事件
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        // 执行登录操作
        performLogin();
    }

    /**
     * 处理按键按下事件的方法，当按下回车键时调用
     * @param e 按键事件
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            // 执行登录操作
            performLogin();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    /**
     * 处理焦点获得事件的方法，当输入文本框获得焦点时调用
     * @param e 焦点事件
     */
    @Override
    public void focusGained(FocusEvent e) {
        String text = inputField.getText();
        if (text.equals(hintText) || text.equals(errorText)) {
            inputField.setText(""); // 清空输入文本框
            inputField.setForeground(Color.black); // 设置文本颜色为黑色
        }
    }

    /**
     * 处理焦点丢失事件的方法，当输入文本框失去焦点时调用
     * @param e 焦点事件
     */
    @Override
    public void focusLost(FocusEvent e) {
        String text = inputField.getText();
        if (text.equals("")) {
            inputField.setText(hintText); // 设置输入文本框的提示文本
            inputField.setForeground(Color.gray); // 设置提示文本的颜色为灰色
        }
    }

    /**
     * 执行登录操作的方法
     */
    private void performLogin() {
        PrintStream printStream = null;
        userName = inputField.getText(); // 获取输入文本框中的用户名
        if (userName.equals("") || userName.equals(hintText)) {
            inputField.setText(errorText); // 设置错误提示文本
            inputField.setForeground(Color.gray); // 设置错误提示文本的颜色为灰色
        } else {
            String password = JOptionPane.showInputDialog(this, "请输入密码");
            if (password != null && !password.isEmpty()) {
                String loginMessage = "Sign:" + userName + ":" + password; // 构造登录消息
                try {
                    printStream = new PrintStream(clientSocket.getOutputStream()); // 创建输出流
                    printStream.println(loginMessage); // 向服务器发送登录消息
                } catch (IOException e) {
                    // 打印异常堆栈信息
                    e.printStackTrace();
                }
                setVisible(false); // 隐藏登录窗口
                // 启动客户端聊天界面
                startClientChat();
            }
        }
    }

    /**
     * 启动客户端聊天界面的方法
     */
    private void startClientChat() {
        Thread clientThread = new Thread(new ClientHandle(userName, clientSocket));
        clientThread.start();
    }
}