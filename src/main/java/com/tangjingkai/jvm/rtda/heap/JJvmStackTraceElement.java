package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.rtda.Frame;
import com.tangjingkai.jvm.rtda.Thread;

import java.util.Arrays;

/**
 * Created by totran on 12/1/16.
 */
public class JJvmStackTraceElement {
    String fileName;
    String className;
    String methodName;
    int lineNumber;

    public JJvmStackTraceElement(String fileName, String className, String methodName, int lineNumber) {
        this.fileName = fileName;
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
    }

    public JJvmStackTraceElement(Frame frame) {
        JJvmMethod method = frame.getMethod();
        JJvmClass cls = method.getJJvmClass();
        this.fileName = cls.getSourceFile();
        this.className = cls.getJavaName();
        this.methodName = method.getName();
        this.lineNumber = method.getLineNumber(frame.getNextPC()-1);
    }

    public static JJvmStackTraceElement[] createStackTraceElements(JJvmObject tObj, Thread thread) {
        int skip = distanceToObject(tObj.getJJvmClass()) + 2;
        Frame[] frames = thread.getFrames();
        frames = Arrays.copyOfRange(frames, skip, frames.length);
        JJvmStackTraceElement[] stes = new JJvmStackTraceElement[frames.length];
        for (int i = 0; i < stes.length; i++) {
            stes[i] = new JJvmStackTraceElement(frames[i]);
        }
        return stes;
    }

    private static int distanceToObject(JJvmClass cls) {
        int distance = 0;
        for (JJvmClass c = cls.superClass; c != null; c = c.superClass) {
            distance++;
        }
        return distance;
    }
}
