package com.example.pbl4_remote_desktopserver;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ServerStart {
    public static List<ClientHandler> clients = new ArrayList<>();

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
}

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final String clientIP;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.clientIP = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        try {
            // Create DataInputStream and DataOutputStream for communication
            DataInputStream authenticationInput = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream responseOutput = new DataOutputStream(clientSocket.getOutputStream());

            // Receive combinedString (ip + "," + pwd) from the client
            String combinedString = authenticationInput.readUTF();

            String[] parts = combinedString.split(",");
            if (parts.length != 3) {
                responseOutput.writeUTF("Invalid input format");
                responseOutput.flush();
                clientSocket.close();
                return;
            }
            String status = parts[0];
            String ip = parts[1];
            String pwd = parts[2];

            System.out.println(ip + " " + pwd);

            // Perform authentication logic here (e.g., check credentials)
            boolean isAuthenticated = authenticateUser(ip, pwd);

            // Send authentication result back to the client
            if (isAuthenticated) {
                responseOutput.writeUTF("Authenticated");
                responseOutput.flush();
            } else {
                responseOutput.writeUTF("Authentication Failed");
                responseOutput.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getClientIP() {
        return clientIP;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    private boolean authenticateUser(String ip, String pwd) {
        // Tìm kiếm và check var
        // Tìm kiếm xem máy client đã kết nối với máy chủ chưa và gửi phản hồi
        // Nếu đã kết nối, gửi pwd tới ip để nhận response
        // Nếu chưa kết nối gửi về client đang request là máy chưa kết nối
        for (ClientHandler clientHandler : ServerStart.clients) {
            System.out.println(clientHandler.getClientIP());
            if (clientHandler.getClientIP().equals(ip)) {
                try {
                    // Create a DataOutputStream to send a message to the client
                    DataOutputStream clientOutput = new DataOutputStream(clientHandler.getClientSocket().getOutputStream());

                    String messageToSend = pwd;
                    clientOutput.writeUTF(messageToSend);
                    clientOutput.flush();

                    // Create a DataInputStream to receive a response from the client
                    DataInputStream clientInput = new DataInputStream(clientHandler.getClientSocket().getInputStream());

                    // Read the response from the client
                    String response = clientInput.readUTF();

                    if (response.equals("True")) return true;
                    else return false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}

