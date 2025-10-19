package org.thingai.base.log;

import java.util.Arrays;

public abstract class ILog {
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARN = 3;
    public static final int ERROR = 4;

    public static int logLevel = DEBUG; // Default log level
    public static boolean ENABLE_LOGGING = false; // Flag to enable or disable logging

    protected static ILog instance;

    public static void d(String tag, String... object) {
        if (ENABLE_LOGGING && logLevel <= DEBUG) {
            String message = String.join(" ", object);
            System.out.println("DEBUG: " + tag + ": " + message);
        }
    }

    public static void i(String tag, String... object) {
        if (ENABLE_LOGGING && logLevel <= INFO) {
            String message = String.join(" ", object);
            System.out.println("INFO: " + tag + ": " + message);
        }
    }

    public static void w(String tag, String... object) {
        if (ENABLE_LOGGING && logLevel <= WARN) {
            String message = String.join(" ", object);
            System.out.println("WARN: " + tag + ": " + message);
        }
    }

    public static void e(String tag, String... object) {
        if (ENABLE_LOGGING && logLevel <= ERROR) {
            String message = String.join(" ", object);
            System.err.println("ERROR: " + tag + ": " + message);
        }
    }
}
