package com.tangjingkai.jvm;

import com.tangjingkai.jvm.classpath.Classpath;
import com.tangjingkai.jvm.rtda.heap.JJvmClass;
import com.tangjingkai.jvm.rtda.heap.JJvmClassLoader;
import com.tangjingkai.jvm.rtda.heap.JJvmMethod;

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
            new Jvm(cmd).startJvm();
        }
    }

    Cmd cmd;

    public Jvm(Cmd cmd) {
        this.cmd = cmd;
    }

    public void startJvm() {
        Classpath cp = new Classpath(cmd.xjreOption, cmd.cpOption);
        JJvmClassLoader classLoader = new JJvmClassLoader(cp);

        String className = cmd.clsFile.replace('.', '/');
        JJvmClass mainClass = classLoader.loadClass(className);
        JJvmMethod mainMethod = mainClass.getMainMethod();

        if (mainMethod == null) {
            System.out.println(String.format("Main method not found in class %s", cmd.clsFile));
        } else {
            new Interpreter().interpret(mainMethod);
        }
    }


}
