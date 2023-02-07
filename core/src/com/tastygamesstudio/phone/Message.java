package com.tastygamesstudio.phone;


public class Message {
    private byte[] bytes;
    private String message;

    public Message() {

    }

    public Message(byte[] bytes, String message) {
        this.bytes = bytes;
        this.message = message;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
