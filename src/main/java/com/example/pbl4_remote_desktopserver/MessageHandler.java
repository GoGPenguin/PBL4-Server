package com.example.pbl4_remote_desktopserver;

public class MessageHandler {
    private  String receiver;
    private String data;
    private String[] parts;
    private String status;

    public MessageHandler(String message) {
        parts = message.split(",");
        this.receiver = parts[0];
        this.status = parts[1];
        this.data = parts[2];
    }

    public String remoteResponse() {
        String response = receiver + "," + status + "," + data;
        return response;
    }

    public String receiver() {
        return receiver;
    }

}
