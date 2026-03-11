package com.mahaijuan.server;

import java.util.Scanner;

/**
 * 命令处理类，用于处理服务器控制台命令
 */
public class CommandHandler implements Runnable {
    private final Server server;
    private boolean running = true;
    private final Scanner scanner;

    public CommandHandler(Server server) {
        this.server = server;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        // 启动时显示提示
        System.out.println("\n服务器命令行界面已启动");
        System.out.println("可用命令: list, listall, quit\n");
        
        while (running) {
            // 显示提示符并读取命令
            System.out.print("服务器命令提示符> ");
            if (scanner.hasNextLine()) {
                String command = scanner.nextLine().trim().toLowerCase();
                
                // 处理命令前添加空行，提高可读性
                System.out.println();
                
                processCommand(command);
                
                // 处理命令后添加空行，提高可读性
                System.out.println();
            }
        }
    }

    /**
     * 处理命令的方法
     * @param command 输入的命令
     */
    private void processCommand(String command) {
        switch (command) {
            case "list":
                server.showOnlineUsers();
                break;
            case "listall":
                server.showAllUsers();
                break;
            case "quit":
                server.shutdownServer();
                running = false;
                break;
            default:
                System.out.println("无效的命令。可用命令: list, listall, quit");
        }
    }
}