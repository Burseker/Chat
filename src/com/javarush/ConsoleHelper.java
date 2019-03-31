package com.javarush;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {

    private static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message){
        System.out.println(message);
    }

    public static String readString(){
        String result = "";
        boolean proceed = true;

        while(proceed) {
            try {
                result = bufferedReader.readLine();
                proceed = false;
            } catch (IOException e) {
                System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            }
        }

        return result;
    }

    public static int readInt(){
        int result = 0;
        boolean proceed = true;

        while(proceed){
            try {
                String s = readString();
                result = Integer.parseInt(s);
                proceed = false;
            } catch (NumberFormatException e){
                System.out.println("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            }
        }

        return result;
    }
}
