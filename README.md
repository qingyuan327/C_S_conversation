"# C_S_conversation" 
# 简易聊天程序设计与实践

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-8%2B-blue.svg)](https://www.oracle.com/java/technologies/downloads/)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/your-username/simple-chat-program)

一款基于 Java Socket 实现的客户端/服务器架构简易聊天程序，支持多客户端连接、实名/匿名聊天、私聊/群聊等核心功能。

## 项目描述
本项目采用经典的 C/S（客户端/服务器）架构，基于 Java 原生 Socket 编程实现跨客户端的即时通信能力。服务端支持多线程并发处理客户端连接，提供用户认证、在线用户管理、消息路由等核心能力；客户端通过 Swing 实现可视化交互界面，支持登录验证、消息收发、聊天模式切换等功能，是学习 Java 网络编程、多线程开发和 Swing 界面设计的典型实践项目。

## 核心功能亮点
- 🔐 **用户认证**：基于用户名/密码的登录验证机制，保障聊天安全性
- 📡 **多客户端并发**：服务端采用线程池处理多客户端连接，支持高并发
- 🗨️ **多模式聊天**：支持群聊、一对一私聊，且可切换实名/匿名聊天模式
- 📋 **服务器管理**：服务端提供命令行/可视化界面，支持查看在线用户、全部用户、关闭服务器等操作
- 🎨 **可视化界面**：客户端/服务端均提供 Swing 图形界面，操作直观友好
- 📝 **日志记录**：服务端自动记录用户登录/退出、消息收发等关键行为日志

## 安装步骤
### 环境要求
- JDK 8 及以上版本
- 支持 Java 编译运行的环境（Windows/Linux/macOS）

### 编译与部署
1. 克隆本项目到本地
   ```bash
   git clone https://github.com/your-username/simple-chat-program.git
   cd simple-chat-program
   ```
2. 编译所有 Java 文件（建议创建 `bin` 目录存放编译后的类文件）
   ```bash
   mkdir bin
   javac -d bin src/com/mahaijuan/server/*.java src/com/mahaijuan/client/*.java
   ```
3. 准备用户认证文件：在项目根目录创建 `users.txt`，按 `用户名:密码` 格式添加用户（每行一个）
   ```
   user1:123456
   user2:654321
   admin:admin123
   ```

## 快速入门
### 1. 启动服务端
```bash
# 进入编译后的 bin 目录
cd bin
# 启动服务器（默认监听 8888 端口）
java com.mahaijuan.server.Server
```
服务端启动后会弹出可视化界面，同时控制台支持 `list`（查看在线用户）、`listall`（查看所有注册用户）、`quit`（关闭服务器）命令。

### 2. 启动客户端
```bash
# 新开终端，进入 bin 目录
cd bin
# 启动客户端（支持同时启动多个客户端，模拟多用户）
java com.mahaijuan.client.Client1
```
客户端启动后会弹出登录界面：
- 输入用户名（需匹配 `users.txt` 中的用户）
- 弹出密码输入框，输入对应密码
- 登录成功后进入聊天界面，支持：
  - 直接输入文本发送群聊消息
  - 格式 `@用户名-消息内容` 发送私聊消息
  - 输入 `@@anonymous` 切换匿名/实名模式
  - 输入 `@@list` 查看在线用户
  - 输入 `@@quit` 退出聊天

## 贡献指南
欢迎提交 Issue 和 Pull Request 来改进本项目，贡献流程如下：
1. Fork 本仓库到个人账号
2. 创建特性分支（`git checkout -b feature/your-feature`）
3. 提交代码修改（`git commit -m "Add some feature"`）
4. 推送分支到远程（`git push origin feature/your-feature`）
5. 打开 Pull Request，描述修改内容和解决的问题

### 贡献规范
- 代码风格：遵循 Java 代码规范（驼峰命名、注释清晰）
- 功能新增：需同步更新 README 文档，并保证服务端/客户端兼容性
- 问题修复：需附带测试步骤，确保修复效果

## 许可证信息
本项目采用 MIT 开源许可证 - 详见 [LICENSE](LICENSE) 文件。您可以自由使用、修改和分发本项目，前提是保留版权声明和许可证说明。