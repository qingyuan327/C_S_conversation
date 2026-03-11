package com.mahaijuan.server;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务器类，用于启动服务器并处理客户端连接
 */
public class Server extends JFrame {
    private static JTextArea textArea; // 服务器消息显示文本框
    private JScrollPane scrollPane; // 滚动面板，用于显示服务器消息
    private int serverPort; // 服务器端口号
    private static final Map<String, String> userCredentials = new HashMap<>(); // 存储用户名和密码
    private static final Map<String, Socket> userMap = new ConcurrentHashMap<>(); // 存储用户名和对应套接字的映射
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_FILE = "server_log.txt";
    private boolean running = true; // 服务器运行状态标志
    private JTextField commandField; // 命令输入框
    private JButton sendButton; // 发送按钮
    
    /**
     * 构造函数，初始化服务器类
     * @param port 服务器端口号
     */
    public Server(int port) {
        this.serverPort = port;
        // 清空日志文件
        clearLogFile();
        // 设置用户界面
        setupUI();
        // 读取用户文件
        readUserFile();
        // 启动服务器
        startServer();
    }

    /**
     * 清空日志文件的方法
     */
    private void clearLogFile() {
        try (FileWriter writer = new FileWriter(LOG_FILE, false)) {
            writer.write("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置用户界面的方法
     */
    private void setupUI() {
        textArea = new JTextArea(); // 创建服务器消息显示文本框
        scrollPane = new JScrollPane(textArea); // 创建滚动面板并添加文本框

        commandField = new JTextField(20); // 创建命令输入框
        sendButton = new JButton("发送"); // 创建发送按钮

        JPanel inputPanel = new JPanel(); // 创建输入面板
        inputPanel.add(commandField); // 将命令输入框添加到输入面板
        inputPanel.add(sendButton); // 将发送按钮添加到输入面板

        add(scrollPane, BorderLayout.CENTER); // 将滚动面板添加到窗口中央
        add(inputPanel, BorderLayout.SOUTH); // 将输入面板添加到窗口底部

        setTitle("聊天小程序服务端"); // 设置窗口标题
        setSize(500, 300); // 设置窗口大小
        setLocation(0, 300); // 设置窗口位置
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置窗口关闭操作
        setVisible(true); // 显示窗口

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = commandField.getText().trim().toLowerCase();
                processCommand(command);
                commandField.setText(""); // 清空输入框
            }
        });
    }

    /**
     * 处理命令的方法
     * @param command 输入的命令
     */
    private void processCommand(String command) {
        switch (command) {
            case "list":
                showOnlineUsers();
                break;
            case "listall":
                showAllUsers();
                break;
            case "quit":
                shutdownServer();
                break;
            default:
                System.out.println("无效的命令。可用命令: list, listall, quit");
        }
    }
    
    /**
     * 读取用户文件的方法
     */
    private void readUserFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    userCredentials.put(parts[0], parts[1]);
                }
            }
            showMessage("已加载 " + userCredentials.size() + " 个用户");
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("读取用户文件失败: " + e.getMessage());
        }
    }

    /**
     * 启动服务器的方法
     */
    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            // 创建一个固定大小的线程池
            ExecutorService threadPool = Executors.newFixedThreadPool(20);
            showMessage("聊天室小程序已启动，监听端口: " + serverPort);
            while (running) {
                // 接受客户端连接
                Socket clientSocket = serverSocket.accept();
                showMessage("有新的朋友加入");
                // 将客户端连接交给套接字处理类处理
                threadPool.execute(new SocketHandler(clientSocket));
            }
        } catch (IOException e) {
            if (running) {
                // 打印异常堆栈信息
                e.printStackTrace();
                showMessage("服务器异常: " + e.getMessage());
            }
        }
    }

    /**
     * 显示在线用户的方法
     */
    public void showOnlineUsers() {
        System.out.println("当前在线用户列表:");
        if (userMap.isEmpty()) {
            System.out.println("  没有在线用户");
        } else {
            for (String userName : userMap.keySet()) {
                System.out.println("  - " + userName);
            }
        }
        System.out.println("总计: " + userMap.size() + " 个在线用户");
    }

    /**
     * 显示全部用户的方法
     */
    public void showAllUsers() {
        System.out.println("系统全部用户列表:");
        if (userCredentials.isEmpty()) {
            System.out.println("  没有注册用户");
        } else {
            for (String userName : userCredentials.keySet()) {
                System.out.println("  - " + userName);
            }
        }
        System.out.println("总计: " + userCredentials.size() + " 个注册用户");
    }

    /**
     * 关闭服务器的方法
     */
    public void shutdownServer() {
        System.out.println("服务器正在关闭...");
        running = false;

        // 通知所有在线用户服务器即将关闭
        for (Map.Entry<String, Socket> entry : userMap.entrySet()) {
            try {
                PrintStream printStream = new PrintStream(entry.getValue().getOutputStream());
                printStream.println("服务器正在关闭，您已断开连接。");
                entry.getValue().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 记录服务器关闭日志
        logMessage("服务器已关闭");

        // 退出应用程序
        System.exit(0);
    }

    /**
     * 显示服务器消息的方法
     * @param message 服务器消息
     */
    public static void showMessage(String message) {
        // 在GUI线程中更新文本区域
        SwingUtilities.invokeLater(() -> {
            textArea.append(message + "\n");
        });
        // 同时输出到控制台
        //System.out.println(message);
    }

    /**
     * 记录日志的方法
     * @param message 日志消息
     */
    public static void logMessage(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(dateFormat.format(new Date()) + " - " + message);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 创建服务器实例，监听端口8888
        new Server(8888);
    }

    public static Map<String, Socket> getUserMap() {
        return userMap;
    }

    public static SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public static Map<String, String> getUserCredentials() {
        return userCredentials;
    }
}