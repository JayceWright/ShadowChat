package org.shadowchat;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class ChatServer extends WebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);

    public ChatServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String resourceDescriptor = handshake.getResourceDescriptor();
        if (!"/chat".equals(resourceDescriptor)) {
            logger.warn("Connection attempt to invalid path: {}", resourceDescriptor);
            conn.close(1008, "Invalid path, use /chat");
            return;
        }
        logger.info("New connection from {} on path {}", conn.getRemoteSocketAddress(), resourceDescriptor);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.info("Closed connection to {} with exit code {} additional info: {}", conn.getRemoteSocketAddress(), code, reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.info("Received message from {}: {}", conn.getRemoteSocketAddress(), message);
        broadcast(message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.error("An error occurred on connection {}: {}", conn != null ? conn.getRemoteSocketAddress() : "null", ex.getMessage(), ex);
    }

    @Override
    public void onStart() {
        logger.info("Server started successfully on port {}", getPort());
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    public static void main(String[] args) {
        String host = "0.0.0.0";
        int port = 8080;

        WebSocketServer server = new ChatServer(new InetSocketAddress(host, port));
        server.start();
        logger.info("ChatServer running on ws://{}:{}/chat", host, port);
    }
}
