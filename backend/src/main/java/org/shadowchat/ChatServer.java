package org.shadowchat;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer extends WebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);
    private final Gson gson = new Gson();

    private final LinkedList<String> chatHistory = new LinkedList<>();
    private final Map<WebSocket, String> activeUsers = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY = 50;

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
        
        // Send history to the new client
        JsonObject historyMessage = new JsonObject();
        historyMessage.addProperty("type", "history");
        JsonArray messagesArray = new JsonArray();
        synchronized (chatHistory) {
            for (String msg : chatHistory) {
                messagesArray.add(JsonParser.parseString(msg));
            }
        }
        historyMessage.add("messages", messagesArray);
        conn.send(gson.toJson(historyMessage));

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("type", "system");
        systemMessage.addProperty("content", "New hacker connected to the terminal.");
        systemMessage.addProperty("timestamp", System.currentTimeMillis());
        String sysMsgStr = gson.toJson(systemMessage);

        addToHistory(sysMsgStr);
        broadcast(sysMsgStr);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.info("Closed connection to {} with exit code {} additional info: {}", conn.getRemoteSocketAddress(), code, reason);
        activeUsers.remove(conn);
        
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("type", "system");
        systemMessage.addProperty("content", "Connection lost to a terminal.");
        systemMessage.addProperty("timestamp", System.currentTimeMillis());
        String sysMsgStr = gson.toJson(systemMessage);

        addToHistory(sysMsgStr);
        broadcast(sysMsgStr);
    }

    private void addToHistory(String message) {
        synchronized (chatHistory) {
            chatHistory.add(message);
            if (chatHistory.size() > MAX_HISTORY) {
                chatHistory.removeFirst();
            }
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.info("Received message from {}: {}", conn.getRemoteSocketAddress(), message);
        try {
            JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
            // Ignore system messages from the client to prevent spoofing
            if (jsonMessage.has("type") && "system".equals(jsonMessage.get("type").getAsString())) {
                logger.warn("Client {} attempted to send a system message", conn.getRemoteSocketAddress());
                return;
            }
            
            if (jsonMessage.has("username")) {
                activeUsers.put(conn, jsonMessage.get("username").getAsString());
            }

            if (jsonMessage.has("content") && "/users".equals(jsonMessage.get("content").getAsString().trim())) {
                Set<String> uniqueUsers = new HashSet<>(activeUsers.values());
                JsonObject usersMessage = new JsonObject();
                usersMessage.addProperty("type", "system");
                usersMessage.addProperty("content", "Active users: " + String.join(", ", uniqueUsers));
                usersMessage.addProperty("timestamp", System.currentTimeMillis());
                conn.send(gson.toJson(usersMessage));
                return;
            }

            // Inject server timestamp
            jsonMessage.addProperty("timestamp", System.currentTimeMillis());
            
            String updatedMessage = gson.toJson(jsonMessage);
            addToHistory(updatedMessage);

            // Broadcast the updated JSON
            broadcast(updatedMessage);
        } catch (JsonSyntaxException e) {
            logger.error("Received malformed JSON from {}: {}", conn.getRemoteSocketAddress(), message);
            // Optionally notify the specific sender about malformed message, 
            // but for now, just drop it so we don't break the whole app.
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.error("An error occurred on connection {}: {}", conn != null ? conn.getRemoteSocketAddress() : "null", ex.getMessage(), ex);
    }

    @Override
    public void onStart() {
        logger.info("Server started successfully on port {}", getPort());
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
