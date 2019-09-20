package com.gexiang;

public class AppStatus {
    private static volatile boolean bQuit = false;
    public static void setbQuit(){
        bQuit = true;
    }
    public static boolean getAppStatus(){
        return bQuit;
    }
}
