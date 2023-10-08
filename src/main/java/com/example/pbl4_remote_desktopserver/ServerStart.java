package com.example.pbl4_remote_desktopserver;

import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerStart {
    public static List<ClientHandler> clients = new ArrayList<>();
    private static Map<String, ClientHandler> clientMap = new HashMap<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(6003); // Chọn một cổng
            System.out.println("Máy chủ đang chạy và chờ kết nối...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Khách hàng đã kết nối: " + clientSocket);

                // Tạo một luồng mới để xử lý khách hàng
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendToClient(String sender, String receiver, String message) {
        ClientHandler recipient = clientMap.get(receiver);
        if (recipient != null) {
            recipient.sendMessage(sender, message);
        }
        else {
            System.out.println("Có đâu mà gửi");
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private String clientIP;

        private DataInputStream in;
        private DataOutputStream out;


        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            this.clientIP = socket.getInetAddress().getHostAddress();
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                clientIP = in.readUTF(); // Đọc tên của client
                System.out.println(clientIP);
                clientMap.put(clientIP, this); // Thêm vào danh sách các client

                String message;
                while (true) {
                    message = in.readUTF();
                    if (!message.isEmpty()) {
                        // Xử lý tin nhắn và gửi tới người nhận cụ thể
                        MessageHandler msg = new MessageHandler(message);
                        String response = msg.remoteResponse();
                        String receiver = msg.receiver();
                        sendToClient(clientIP, receiver, response);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                    clients.remove(this);
                    clientMap.remove(clientIP);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendMessage(String sender, String message) {
            try {
                out.writeUTF(sender + "," + message);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}





