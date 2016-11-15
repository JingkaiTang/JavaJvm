package com.tangjingkai.jvm;

import com.tangjingkai.jvm.classfile.ClassFile;
import com.tangjingkai.jvm.classpath.Classpath;

import java.util.Arrays;

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
        Classpath cp = new Classpath(cmd.xjreOption, cmd.cpOption);
        System.out.println(String.format("classpath:%s class:%s args: %s", cp.getPath(), cmd.clsFile, Arrays.toString(cmd.args)));

        ClassFile cf = loadClass(cmd.clsFile, cp);

        System.out.println(String.format("class file:%s", cf));
    }

    public static ClassFile loadClass(String cls, Classpath cp) {
        String className = cls.replace(".", "/");
        byte[] data = cp.readClass(className);
        if (data == null) {
            throw new RuntimeException(String.format("Cloud not find or load main class %s", cls));
        }
        return new ClassFile(data);
    }
}
