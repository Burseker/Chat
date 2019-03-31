package com.javarush.client;

import com.javarush.Connection;
import com.javarush.ConsoleHelper;
import com.javarush.Message;
import com.javarush.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {

    protected Connection connection;
    private volatile boolean clientConnected = false;

    //TODO
    public static void main(String[] args) {
        Client client = new Client();
//        System.out.println(client.getServerAddress());
//        System.out.println(client.getServerPort());
//        System.out.println(client.getUserName());

        client.run();
    }

    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        synchronized (this) {
            try {
                this.wait();
                //Thread.currentThread().wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                ConsoleHelper.writeMessage("Waiting client connection error");
                //return;
            }
        }

        if(clientConnected){
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }

        String message;
        while( clientConnected ){
            message = ConsoleHelper.readString();
            if(message.toLowerCase().equals("exit")) break;
            if(shouldSendTextFromConsole()){
                sendTextMessage(message);
//                ConsoleHelper.writeMessage(message);
            }
        }
    }

    protected String getServerAddress(){
        String ipAdress;

//        do {
//            ConsoleHelper.writeMessage("Enter server adress(IP or \"localhost\")");
//            ipAdress = ConsoleHelper.readString();
//        } while (!ipAdress.matches("\\b(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\." +
//                "(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\." +
//                "(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\." +
//                "(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\b") && !ipAdress.equals("localhost"));

        ConsoleHelper.writeMessage("Enter server adress(IP or \"localhost\")");
        ipAdress = ConsoleHelper.readString();
        return ipAdress;
    }

    protected int getServerPort(){
        ConsoleHelper.writeMessage("Enter server port");
        int port = ConsoleHelper.readInt();
        return port;
    }

    protected String getUserName(){
        String userName;

        ConsoleHelper.writeMessage("Enter user name");
        userName = ConsoleHelper.readString();
//        do {
//        } while (userName.isEmpty());

        return userName;
    }

    protected void sendTextMessage(String text){
        try {
            Message tesxMessage = new Message(MessageType.TEXT, text);
            connection.send(tesxMessage);
        } catch (IOException e) {
            e.printStackTrace();
            ConsoleHelper.writeMessage("Error: Unable to send message. Connection is closed");
            try {
                connection.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            clientConnected = false;
        }
    }

    protected boolean shouldSendTextFromConsole(){
        return true;
    }

    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    public class SocketThread extends Thread{

        public void run(){
            String adress = getServerAddress();
            int port = getServerPort();
            try (Socket socket = new Socket(adress, port)) {
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                notifyConnectionStatusChanged(false);
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException{
            Message recMessage;
            while (true){
                recMessage = connection.receive();
                if(recMessage.getType() == MessageType.NAME_REQUEST){
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                } else if(recMessage.getType() == MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    break;
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{

            Message recMessage;

            while(true) {
                recMessage = connection.receive();

                if(recMessage.getType() == MessageType.TEXT)
                    processIncomingMessage(recMessage.getData());
                else if(recMessage.getType() == MessageType.USER_ADDED)
                    informAboutAddingNewUser(recMessage.getData());
                else if(recMessage.getType() == MessageType.USER_REMOVED)
                    informAboutDeletingNewUser(recMessage.getData());
                else
                    throw new IOException("Unexpected MessageType");

//                switch(recMessage.getType()){
//                    case TEXT:
//                        processIncomingMessage(recMessage.getData());
//                        break;
//                    case USER_ADDED:
//                        informAboutAddingNewUser(recMessage.getData());
//                        break;
//                    case USER_REMOVED:
//                        informAboutDeletingNewUser(recMessage.getData());
//                        break;
//                    default:
//                        throw new IOException("Unexpected MessageType");
//                }
            }
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(userName + " added to chat");
        }

        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(userName + " deleted from chat");
        }
    }
}
