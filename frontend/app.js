document.addEventListener('DOMContentLoaded', () => {
    const logsElement = document.getElementById('chat-logs');
    const inputElement = document.getElementById('chat-input');
    const usernameInput = document.getElementById('username-input');
    const cliPrompt = document.getElementById('cli-prompt');
    const ws = new WebSocket('ws://localhost:8080/chat');

    function getUsername() {
        return usernameInput.value.trim() || 'Guest';
    }

    usernameInput.addEventListener('input', () => {
        cliPrompt.textContent = `${getUsername()}@shadow-net:~$ `;
    });

    function appendLog(content, type = 'message', username = '') {
        const div = document.createElement('div');

        if (type === 'system') {
            div.textContent = content;
            div.style.color = 'gray';
            div.style.opacity = '0.7';
            div.style.fontStyle = 'italic';
        } else if (type === 'message') {
            div.textContent = `[${username}]: ${content}`;
        } else {
            div.textContent = content;
        }

        logsElement.appendChild(div);
        logsElement.scrollTop = logsElement.scrollHeight;
    }

    ws.onopen = () => {
        appendLog('[System]: Connection established to Shadow-Net.', 'system');
    };

    ws.onmessage = (event) => {
        try {
            const data = JSON.parse(event.data);
            if (data.type === 'system') {
                appendLog(data.content, 'system');
            } else if (data.type === 'message') {
                appendLog(data.content, 'message', data.username);
            } else {
                appendLog(`> ${event.data}`);
            }
        } catch (e) {
            // Fallback for non-JSON messages
            appendLog(`> ${event.data}`);
        }
    };

    ws.onclose = () => {
        appendLog('[System]: Connection lost to Shadow-Net.', 'system');
    };

    ws.onerror = (error) => {
        appendLog('[System]: Connection error.', 'system');
    };

    inputElement.addEventListener('keydown', (event) => {
        if (event.key === 'Enter') {
            const message = inputElement.value.trim();
            if (message && ws.readyState === WebSocket.OPEN) {
                const payload = {
                    type: "message",
                    username: getUsername(),
                    content: message
                };
                ws.send(JSON.stringify(payload));
                inputElement.value = '';
            }
        }
    });
});
