import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

//add client's name
//add show client's name in chat window
//** add client's name from client first msg

public class ClientHandler {//client-marker
    private Server server;
    private Socket socket;//connecting
    private DataInputStream in;//client-in
    private DataOutputStream out;//client-out
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        // /auth login1 password1
                        if (str.startsWith("/auth")) {
                            String[] subStrings = str.split(" ", 3);
                            if (subStrings.length == 3) {
                                String nickFromDB = SQLHandler.getNickByLoginAndPassword(subStrings[1], subStrings[2]);
                                if (nickFromDB != null) {
                                    if (!server.isClientInChat(nickFromDB)) {
                                        nickname = nickFromDB;
                                        sendMsg("/authok " + nickname);
                                        server.subscribe(this);
                                        break;
                                    } else {
                                        sendMsg("This nick already in use");
                                    }
                                } else {
                                    sendMsg("Wrong login/password");
                                }
                            } else {
                                sendMsg("Wrong data format");
                            }
                        }
                        if (str.startsWith("/registration")) {
                            String[] subStr = str.split(" ");
                            // /registration login password nick
                            if (subStr.length == 4) {
                                if (SQLHandler.tryToRegister(subStr[1], subStr[2], subStr[3])) {
                                    sendMsg("Registration complete");
                                } else {
                                    sendMsg("Incorrect login/password/nickname");
                                }
                            }
                        }
                    }

                    while (true) {
                        String str = in.readUTF();
                        System.out.println("Сообщение от клиента: " + str);
                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                break;
                            } else if (str.startsWith("/w")) {
                                // /w nick hello m8! hi
                                final String[] subStrings = str.split(" ", 3);
                                if (subStrings.length == 3) {
                                    final String toUserNick = subStrings[1];
                                    if (server.isClientInChat(toUserNick)) {
                                        server.privateMsg(nickname, toUserNick, subStrings[2]);
                                        sendMsg("to " + toUserNick + ": " + subStrings[2]);
                                    } else {
                                        sendMsg("User with nick '" + toUserNick + "' not found in chat room");
                                    }
                                } else {
                                    sendMsg("Wrong private message");
                                }
                            } else if (str.startsWith("/changeNick")) {
                                // /changeNick newNick
                                String[] subStr = str.split(" ");
                                if (subStr.length >= 2) {
                                    String newNick = subStr[1];
                                    String oldNick = nickname;
                                    if (SQLHandler.changeNick(nickname, newNick)) {
                                        sendMsg("Nick was changed");
                                        nickname = newNick;
                                        server.broadcastClientList();
                                        server.broadcastMsg("Client: " + oldNick + " rename -> " + nickname);
                                        sendMsg("/renameOK " + nickname);
                                    } else {
                                        sendMsg("Wrong");
                                    }
                                } else {
                                    sendMsg("Wrong data format");
                                }


                            }
                        } else {
                            server.broadcastMsg(nickname + ": " + str);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    server.unsubscribe(this);
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
