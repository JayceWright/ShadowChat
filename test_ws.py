import asyncio
import websockets

async def test_chat():
    uri = "ws://localhost:8080/chat"
    try:
        async with websockets.connect(uri) as websocket:
            print("Connected to WebSocket.")
            await websocket.send("Hello, Shadow-Net!")
            print("Message sent.")

            response = await websocket.recv()
            print(f"Received message: {response}")

            if response == "Hello, Shadow-Net!":
                print("Broadcast successful!")
            else:
                print("Broadcast failed.")
    except Exception as e:
        print(f"Error during WebSocket test: {e}")

if __name__ == "__main__":
    asyncio.run(test_chat())
