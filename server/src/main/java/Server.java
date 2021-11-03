import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {

    private Vector<ClientHandler> clients; //потокобезопасный аналог ArrayList

    public Server() {
        try {
            SQLHandler.connect();
            ServerSocket serverSocket = new ServerSocket(8189);
            clients = new Vector<>();
            while (true) {
                System.out.println("Waiting for connection client");
                Socket socket = serverSocket.accept();//waiting client
                ClientHandler c = new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            SQLHandler.disconnect();
        }
    }

    public void subscribe(ClientHandler client) {
        broadcastMsg(client.getNickname() + " in chat now");
        clients.add(client);
        client.sendMsg("Welcome to chat");
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler client) {
        broadcastMsg(client.getNickname() + " leaves chat");
        clients.remove(client);
        broadcastClientList();

    }

    public void broadcastMsg(String msg) {
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

    public void privateMsg(String nickFrom, String nickTo, String msg) {
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(nickTo)) {
                c.sendMsg(nickFrom + " " + msg);
                break;
            }
        }
    }

    public boolean isClientInChat(String nickname) {
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder();
        sb.append("/clientsList");
        for (ClientHandler c : clients) {
            sb.append(" " + c.getNickname());
        }
        broadcastMsg(sb.toString());
    }

}
