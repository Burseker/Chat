package com.javarush.client;

import com.javarush.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
//        BotSocketThread botSocketThread = botClient.new BotSocketThread();
//        botSocketThread.processIncomingMessage("Vasia: время");
//        botSocketThread.processIncomingMessage("Vasia : время");
//        botSocketThread.processIncomingMessage("Vasia  :  вре мя");
//        botSocketThread.processIncomingMessage(" Vasia :день");
    }

    protected String getUserName(){
        return String.format("date_bot_%d", (int)(Math.random()*100));
    }

    protected SocketThread getSocketThread(){
        return new BotSocketThread();
    }

    protected boolean shouldSendTextFromConsole(){
        return false;
    }

    public class BotSocketThread extends Client.SocketThread{

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            //if()
            String[] arrMessage = message.trim().split("\\s*:\\s*");
            if(arrMessage.length != 2)
                return;

            String timeFormat;
            switch(arrMessage[1]){
                case "дата":
                    timeFormat = "d.MM.YYYY";
                    break;
                case "день":
                    timeFormat = "d";
                    break;
                case "месяц":
                    timeFormat = "MMMM";
                    break;
                case "год":
                    timeFormat = "YYYY";
                    break;
                case "время":
                    timeFormat = "H:mm:ss";
                    break;
                case "час":
                    timeFormat = "H";
                    break;
                case "минуты":
                    timeFormat = "m";
                    break;
                case "секунды":
                    timeFormat = "s";
                    break;
                default:
                    return;
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeFormat);
            String answer = String.format("Информация для %s: %s", arrMessage[0], simpleDateFormat.format(Calendar.getInstance().getTime()));
            sendTextMessage(answer);
            //System.out.println(answer);
        }
    }
}
