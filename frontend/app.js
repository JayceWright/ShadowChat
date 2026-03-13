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

    function createLogElement(content, type = 'message', username = '', timestamp = null) {
        const div = document.createElement('div');
        div.classList.add('message-element');
        
        if (timestamp) {
            const date = new Date(timestamp);
            const timeSpan = document.createElement('span');
            timeSpan.className = 'timestamp';
            timeSpan.textContent = `[${date.toLocaleTimeString()}] `;
            div.appendChild(timeSpan);
        }

        if (type === 'system') {
            const systemSpan = document.createElement('span');
            systemSpan.className = 'system-message';
            systemSpan.textContent = content;
            div.appendChild(systemSpan);
        } else if (type === 'message') {
            const userSpan = document.createElement('span');
            userSpan.className = 'username';
            userSpan.textContent = `[${username}]`;
            div.appendChild(userSpan);

            div.appendChild(document.createTextNode(`: ${content}`));

            if (username === getUsername()) {
                div.classList.add('my-message');
            }
        } else {
            div.textContent = content;
        }
        return div;
    }

    function appendLog(content, type = 'message', username = '', timestamp = null) {
        const div = createLogElement(content, type, username, timestamp);
        logsElement.appendChild(div);
        logsElement.scrollTop = logsElement.scrollHeight;
    }

    ws.onopen = () => {
        // We let the server broadcast the system message about the new connection
    };

    ws.onmessage = (event) => {
        try {
            const data = JSON.parse(event.data);
            if (data.type === 'history') {
                const fragment = document.createDocumentFragment();
                data.messages.forEach(msg => {
                    if (msg.type === 'system') {
                        fragment.appendChild(createLogElement(msg.content, 'system', '', msg.timestamp));
                    } else if (msg.type === 'message') {
                        fragment.appendChild(createLogElement(msg.content, 'message', msg.username, msg.timestamp));
                    }
                });
                logsElement.appendChild(fragment);
                logsElement.scrollTop = logsElement.scrollHeight;
            } else if (data.type === 'system') {
                appendLog(data.content, 'system', '', data.timestamp);
            } else if (data.type === 'message') {
                appendLog(data.content, 'message', data.username, data.timestamp);
            } else {
                appendLog(`> ${event.data}`);
            }
        } catch (e) {
            // Fallback for non-JSON messages
            appendLog(`> ${event.data}`);
        }
    };

    ws.onclose = () => {
        // We let the server broadcast the system message about the disconnection
        // Only show a local message if desired, but we'll stick to server messages globally
    };

    ws.onerror = (error) => {
        appendLog('[System]: Connection error.', 'system');
    };

    inputElement.addEventListener('keydown', (event) => {
        if (event.key === 'Enter') {
            const message = inputElement.value.trim();

            if (message === '/clear') {
                logsElement.innerHTML = '';
                inputElement.value = '';
                return;
            }

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

    document.addEventListener('click', (event) => {
        const selection = window.getSelection();
        if (selection && selection.toString().length > 0) {
            return;
        }
        if (event.target !== usernameInput && event.target !== inputElement) {
            inputElement.focus();
        }
    });
});
