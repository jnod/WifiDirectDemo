package com.example.jordan.wifidirecttest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jordan on 12/29/15.
 */
public class JSONSocket {
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final JSONMessageCallback callback;

    private boolean run = false;
    byte[] buffer = new byte[1000];

    public JSONSocket(Socket socket, JSONMessageCallback callback) throws IOException {
        if (socket == null || callback == null) throw new NullPointerException();

        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        this.callback = callback;
    }

    public synchronized void start() throws IOException, JSONException {
        if (!run) {
            run = true;
            int i = 0;

            while ((i = inputStream.read(buffer)) > 0) {
                parseBuffer(i);

                if (!run) break;
            }
        }
    }

    private final StringBuilder builder = new StringBuilder(1000);

    private void parseBuffer(int length) throws JSONException {
        for (int i = 0; i < length; i++) {
            char element = (char) buffer[i];

            if (element == '\n') {
                // ignore lone newline characters
                if (builder.length() > 1) {
                    callback.receiveMessage(new JSONObject(builder.toString()));
                }

                builder.setLength(0);
            } else {
                builder.append(element);
            }
        }
    }

    public void stop() {
        run = false;
    }

    public void send(JSONObject message) throws IOException {
        outputStream.write((message.toString() + '\n').getBytes());
    }

    public interface JSONMessageCallback {
        void receiveMessage(JSONObject message);
    }
}
