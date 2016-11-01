package gHost.Logger;

import java.util.Date;

public class Logger {
    /* Color console text for easy identification of log messages. */
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";

    public static void log(Level level, String message){
        Date date = new Date();
        switch(level){
            case INFO:
                System.out.println(ANSI_PURPLE + level + ANSI_RESET + ": "+date+"\n"+ ANSI_CYAN + message + ANSI_RESET);
                System.out.println("Calling class: " + new Exception().getStackTrace()[1].getClassName()
                        + " from method: " + new Exception().getStackTrace()[1].getMethodName());
                break;
            case WARNING:
                System.out.println(ANSI_GREEN + level + ANSI_RESET + ": "+date+"\n"+ ANSI_BLUE + message + ANSI_RESET);
                System.out.println("Calling class: " + new Exception().getStackTrace()[1].getClassName()
                        + " from method: " + new Exception().getStackTrace()[1].getMethodName());
                break;
            case ERROR:
                System.out.println(ANSI_RED + level + ANSI_RESET + ": "+date+"\n"+ ANSI_YELLOW + message + ANSI_RESET);
                System.out.println("Calling class: " + new Exception().getStackTrace()[1].getClassName()
                        + " from method: " + new Exception().getStackTrace()[1].getMethodName());
                break;
            default:
                break;
        }
    }
}
