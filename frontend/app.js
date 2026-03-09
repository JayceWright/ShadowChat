document.addEventListener('DOMContentLoaded', () => {
    const logsElement = document.getElementById('chat-logs');
    const inputElement = document.getElementById('chat-input');
    const ws = new WebSocket('ws://localhost:8080/chat');

    function appendLog(message, isSystem = false) {
        const div = document.createElement('div');
        div.textContent = message;
        if (isSystem) {
            div.style.opacity = '0.7';
            div.style.fontStyle = 'italic';
        }
        logsElement.appendChild(div);
        logsElement.scrollTop = logsElement.scrollHeight;
    }

    ws.onopen = () => {
        appendLog('[System]: Connection established to Shadow-Net.', true);
    };

    ws.onmessage = (event) => {
        appendLog(`> ${event.data}`);
    };

    ws.onclose = () => {
        appendLog('[System]: Connection lost to Shadow-Net.', true);
    };

    ws.onerror = (error) => {
        appendLog('[System]: Connection error.', true);
    };

    inputElement.addEventListener('keydown', (event) => {
        if (event.key === 'Enter') {
            const message = inputElement.value.trim();
            if (message && ws.readyState === WebSocket.OPEN) {
                ws.send(message);
                inputElement.value = '';
            }
        }
    });
});
