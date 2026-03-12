import asyncio
import websockets

async def test_chat():
    uri = "ws://localhost:8080/chat"
    try:
        async with websockets.connect(uri) as websocket:
            print("Connected to WebSocket.")
            import json
            payload = {
                "type": "message",
                "username": "TestBot",
                "content": "Hello, Shadow-Net!"
            }
            await websocket.send(json.dumps(payload))
            print("Message sent.")

            response = await websocket.recv()
            print(f"Received message 1 (history): {response}")

            response2 = await websocket.recv()
            print(f"Received message 2: {response2}")

            if "Hello, Shadow-Net!" in response2:
                print("Broadcast successful! And we skipped 'New hacker connected' successfully.")
            else:
                print("Broadcast failed or received unexpected message.")
    except Exception as e:
        print(f"Error during WebSocket test: {e}")

if __name__ == "__main__":
    asyncio.run(test_chat())
