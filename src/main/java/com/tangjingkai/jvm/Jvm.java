package com.tangjingkai.jvm;

/**
 * Created by totran on 11/12/16.
 */
public class Jvm {
    public static final String VERSION = "version 0.0.1";

    public static void main(String[] args) {
        Cmd cmd = Cmd.parse(args);
        if (cmd == null) {
            System.exit(1);
        } else {
            startJvm(cmd);
        }
    }

    public static void startJvm(Cmd cmd) {
        System.out.println("cmd = " + cmd);
    }
}
