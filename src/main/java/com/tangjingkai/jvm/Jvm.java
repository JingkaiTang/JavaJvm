package com.tangjingkai.jvm;

import com.tangjingkai.jvm.classpath.Classpath;
import com.tangjingkai.jvm.rtda.Frame;
import com.tangjingkai.jvm.rtda.Thread;
import com.tangjingkai.jvm.rtda.heap.*;

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
            new Jvm(cmd).start();
        }
    }

    Cmd cmd;
    JJvmClassLoader classLoader;
    Thread mainThread;

    public Jvm(Cmd cmd) {
        this.cmd = cmd;
        Classpath cp = new Classpath(cmd.xjreOption, cmd.cpOption);
        this.classLoader = new JJvmClassLoader(cp);
        this.mainThread = new Thread();
    }

    public void start() {
        initVM();
        execMain();
    }

    private void execMain() {
        String className = cmd.clsFile.replace('.', '/');
        JJvmClass mainClass = classLoader.loadClass(className);
        JJvmMethod mainMethod = mainClass.getMainMethod();

        if (mainMethod == null) {
            System.out.println(String.format("Main method not found in class %s", cmd.clsFile));
            return;
        }
        JJvmObject argsArr = createArgsArray();
        Frame frame = mainThread.buildFrame(mainMethod);
        frame.getLocalVars().setRef(0, argsArr);
        mainThread.pushFrame(frame);
        new Interpreter().interpret(mainThread);
    }

    private JJvmObject createArgsArray() {
        JJvmClass stringClass = classLoader.loadClass("java/lang/String");
        int argsLen = cmd.args.length;
        JJvmObject argsArr = stringClass.getArrayClass().newArray(argsLen);
        JJvmObject[] jArgs = argsArr.getRefs();
        for (int i = 0; i < argsLen; i++) {
            jArgs[i] = InternedStrings.getString(classLoader, cmd.args[i]);
        }
        return argsArr;
    }

    private void initVM() {
        JJvmClass vmClass = classLoader.loadClass("sun/misc/VM");
        vmClass.initClass(mainThread);
        new Interpreter().interpret(mainThread);
    }


}
