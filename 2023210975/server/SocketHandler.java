package com.mahaijuan.server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 套接字处理类，用于处理客户端的连接和消息
 */
class SocketHandler implements Runnable {
    private Socket clientSocket; // 客户端套接字
    private String userName; // 当前用户的用户名
    private boolean isAnonymous = false; // 新增：匿名聊天标志

    /**
     * 构造函数，初始化套接字处理类
     * @param socket 客户端套接字
     */
    public SocketHandler(Socket socket) {
        this.clientSocket = socket;
    }

    /**
     * 线程运行方法，用于接收客户端发送的消息并处理
     */
    @Override
    public void run() {
        try (Scanner scanner = new Scanner(clientSocket.getInputStream());
             PrintStream printStream = new PrintStream(clientSocket.getOutputStream())) {
            // 验证用户名和密码
            while (!authenticateUser(scanner, printStream)) {
                printStream.println("用户名或密码错误，请重新输入。");
                Server.logMessage("用户登录失败，IP地址: " + clientSocket.getInetAddress() + ", 时间: " + Server.getDateFormat().format(new Date()));
            }
            // 注册用户
            Server.showMessage("[用户: " + userName + "] 上线了，他的[客户端为: " + clientSocket + "]!");
            Server.showMessage("当前在线人数为:" + Server.getUserMap().size() + "人");
            Server.logMessage("用户 " + userName + " 登录成功，IP地址: " + clientSocket.getInetAddress() + ", 时间: " + Server.getDateFormat().format(new Date()));

            String message;
            while (true) {
                if (scanner.hasNextLine()) {
                    message = scanner.nextLine();
                    // 处理接收到的消息
                    handleMessage(message);
                }
            }
        } catch (IOException e) {
            // 用户退出
            if (userName != null) {
                Server.getUserMap().remove(userName);
                Server.showMessage("用户:" + userName + "已下线!");
                Server.showMessage("当前在线人数为:" + Server.getUserMap().size() + "人");
                Server.logMessage("用户 " + userName + " 退出聊天室，时间: " + Server.getDateFormat().format(new Date()));
            }
            // 打印异常堆栈信息
            e.printStackTrace();
        }
    }

    /**
     * 验证用户的方法
     * @param scanner 输入扫描器
     * @param printStream 输出流
     * @return 验证是否成功
     * @throws IOException 当发生输入输出异常时抛出
     */
    private boolean authenticateUser(Scanner scanner, PrintStream printStream) throws IOException {
        if (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            if (input.startsWith("Sign:")) {
                String[] parts = input.split(":");
                if (parts.length == 3) {
                    String inputUserName = parts[1];
                    String inputPassword = parts[2];

                    // 验证用户名和密码
                    if (inputUserName.isEmpty() || inputPassword.isEmpty()) {
                        printStream.println("用户名或密码不能为空！");
                        return false;
                    }

                    // 输出调试信息到日志文件
                    Server.logMessage("DEBUG: 读取的用户名=[" + inputUserName + "], 密码=[" + inputPassword + "]");

                    if (Server.getUserCredentials().containsKey(inputUserName) &&
                            Server.getUserCredentials().get(inputUserName).equals(inputPassword)) {
                        this.userName = inputUserName;
                        Server.getUserMap().put(userName, clientSocket);
                        printStream.println("验证成功！");
                        Server.logMessage("用户 " + userName + " 验证成功");
                        return true;
                    } else {
                        // 输出预期密码到日志文件
                        Server.logMessage("DEBUG: 用户[" + inputUserName + "]的预期密码是[" +
                                Server.getUserCredentials().getOrDefault(inputUserName, "未知") + "]");
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 处理接收到的消息的方法
     * @param message 接收到的消息
     * @throws IOException 当发生输入输出异常时抛出
     */
    private void handleMessage(String message) throws IOException {
        if (message.startsWith("@") && message.contains("-")) {
            // 确保用户已注册
            ensureRegistered();
            String[] parts = message.split("@")[1].split("-");
            String targetUser = parts[0];
            String privateMsg = parts[1];
            // 发送私聊消息
            sendPrivateMessage(targetUser, privateMsg);
        } else if (message.startsWith("@@")) {
            // 确保用户已注册
            ensureRegistered();
            // 处理系统命令
            handleSystemCommand(message.substring(2));
        } else if (message.equalsIgnoreCase("exit")) {
            // 确保用户已注册
            ensureRegistered();
            // 用户主动退出
            PrintStream printStream = new PrintStream(clientSocket.getOutputStream());
            printStream.println("您已成功退出聊天室。");
            userLogout();
            // 关闭客户端连接
            clientSocket.close();
        } else {
            // 确保用户已注册
            ensureRegistered();
            // 发送群聊消息
            sendGroupMessage(message);
        }
    }

    /**
     * 确保用户已注册的方法
     * @throws IOException 当发生输入输出异常时抛出
     */
    private void ensureRegistered() throws IOException {
        if (userName == null) {
            PrintStream printStream = new PrintStream(clientSocket.getOutputStream());
            printStream.println("请先进行注册操作！");
            printStream.println("注册格式为:[用户名]");
        }
    }

    /**
     * 发送群聊消息的方法
     * @param message 群聊消息
     * @throws IOException 当发生输入输出异常时抛出
     */
    private void sendGroupMessage(String message) throws IOException {
        String sender = isAnonymous ? "匿名用户" : userName;
        Server.showMessage(sender + "群聊说:" + message);
        for (Map.Entry<String, Socket> entry : Server.getUserMap().entrySet()) {
            if (!entry.getValue().equals(clientSocket)) {
                PrintStream printStream = new PrintStream(entry.getValue().getOutputStream());
                printStream.println(sender + "群聊说:" + message);
            }
        }
    }

    /**
     * 发送私聊消息的方法
     * @param targetUser 目标用户
     * @param message 私聊消息
     * @throws IOException 当发生输入输出异常时抛出
     */
    private void sendPrivateMessage(String targetUser, String message) throws IOException {
        Socket targetSocket = Server.getUserMap().get(targetUser);
        if (targetSocket != null) {
            String sender = isAnonymous ? "匿名用户" : userName;
            PrintStream printStream = new PrintStream(targetSocket.getOutputStream());
            printStream.println(sender + "@你说:" + message);
            Server.showMessage(sender + "私聊" + targetUser + "说:" + message);
        } else {
            PrintStream printStream = new PrintStream(clientSocket.getOutputStream());
            printStream.println("目标用户不存在或不在线。");
        }
    }

    /**
     * 用户注销的方法
     */
    private void userLogout() {
        Server.getUserMap().remove(userName);
        Server.showMessage("用户:" + userName + "已下线!");
        Server.showMessage("当前在线人数为:" + Server.getUserMap().size() + "人");
        Server.logMessage("用户 " + userName + " 退出聊天室，时间: " + Server.getDateFormat().format(new Date()));
    }

    /**
     * 处理系统命令的方法
     * @param command 系统命令
     * @throws IOException 当发生输入输出异常时抛出
     */
    private void handleSystemCommand(String command) throws IOException {
        PrintStream printStream = new PrintStream(clientSocket.getOutputStream());
        switch (command) {
            case "list":
                StringBuilder onlineUsers = new StringBuilder("当前在线用户列表:\n");
                if (Server.getUserMap().isEmpty()) {
                    onlineUsers.append("  没有在线用户\n");
                } else {
                    for (String userName : Server.getUserMap().keySet()) {
                        onlineUsers.append("  - ").append(userName).append("\n");
                    }
                }
                onlineUsers.append("总计: ").append(Server.getUserMap().size()).append(" 个在线用户");
                printStream.println(onlineUsers.toString());
                break;
            case "quit":
                printStream.println("您已成功退出聊天室。");
                userLogout();
                clientSocket.close();
                break;
            case "showanonymous":
                printStream.println("当前聊天方式: " + (isAnonymous ? "匿名聊天" : "实名聊天"));
                break;
            case "anonymous":
                isAnonymous = !isAnonymous;
                printStream.println("已切换到 " + (isAnonymous ? "匿名聊天" : "实名聊天") + " 模式");
                break;
            default:
                printStream.println("无效的系统命令。可用命令: list, quit, showanonymous, anonymous");
        }
    }
}