package com.javarush;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Enter server port");
        int serverPort = ConsoleHelper.readInt();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(serverPort);
            ConsoleHelper.writeMessage(String.format("Server is running on port %d", serverPort));

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Handler(clientSocket).start();
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Server error occured");
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendBroadcastMessage(Message message) {

//        connectionMap.forEach((K, V) -> {
//            try {
//                V.send(message);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });

        try {
            for (Map.Entry<String, Connection> element : connectionMap.entrySet()) {
                element.getValue().send(message);
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Broadcast massage is failed");
            e.printStackTrace();
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Remote addres connection established by adress: " + socket.getRemoteSocketAddress().toString());
            String userName = null;
            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                sendListOfUsers(connection, userName);
                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                ConsoleHelper.writeMessage("Error occured with remote addres connection: " + socket.getRemoteSocketAddress().toString());
            }

            if (userName != null) {
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }

            ConsoleHelper.writeMessage("Remote addres connection is closed by adress: " + socket.getRemoteSocketAddress().toString());
        }

        private void serverMainLoop(Connection connection, String userName)
                throws IOException, ClassNotFoundException {

            while (true) {
                Message recMessage = connection.receive();

                if (recMessage.getType() == MessageType.TEXT && recMessage.getData() != null) {
                    Message trmMessage = new Message(MessageType.TEXT, String.format("%s: %s", userName, recMessage.getData()));
                    sendBroadcastMessage(trmMessage);
                } else {
                    ConsoleHelper.writeMessage("Receive message is not text message");
                }
            }
        }

        private String serverHandshake(Connection connection)
                throws IOException, ClassNotFoundException {

            Message reciveMessage;

            do {
                connection.send(new Message(MessageType.NAME_REQUEST));
                reciveMessage = connection.receive();
            } while ((reciveMessage.getType() != MessageType.USER_NAME) ||
                    (reciveMessage.getData() == null) ||
                    reciveMessage.getData().isEmpty() ||
                    connectionMap.containsKey(reciveMessage.getData()));

            connectionMap.put(reciveMessage.getData(), connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED));

            return reciveMessage.getData();
        }

        private void sendListOfUsers(Connection connection, String userName)
                throws IOException {

            for (Map.Entry<String, Connection> element : connectionMap.entrySet()) {
                String user = element.getKey();
                if (!user.equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, user));
                }
            }
        }
    }
}
