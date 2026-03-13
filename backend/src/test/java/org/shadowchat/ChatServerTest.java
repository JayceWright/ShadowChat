package org.shadowchat;

import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import static org.junit.jupiter.api.Assertions.*;

class ChatServerTest {

    @Test
    void testAddToHistoryEnforcesLimit() {
        ChatServer server = new ChatServer(new InetSocketAddress("localhost", 0));
        int limit = ChatServer.MAX_HISTORY;

        // Add more messages than the limit
        for (int i = 0; i < limit + 10; i++) {
            server.addToHistory("message " + i);
        }

        assertEquals(limit, server.chatHistory.size(), "History size should not exceed MAX_HISTORY");
    }

    @Test
    void testAddToHistoryRemovesOldest() {
        ChatServer server = new ChatServer(new InetSocketAddress("localhost", 0));
        int limit = ChatServer.MAX_HISTORY;

        // Add exactly the limit
        for (int i = 0; i < limit; i++) {
            server.addToHistory("message " + i);
        }

        // Add one more
        server.addToHistory("newest message");

        assertEquals(limit, server.chatHistory.size());
        assertEquals("message 1", server.chatHistory.getFirst(), "Oldest message should have been removed");
        assertEquals("newest message", server.chatHistory.getLast(), "Newest message should be at the end");
    }
}
