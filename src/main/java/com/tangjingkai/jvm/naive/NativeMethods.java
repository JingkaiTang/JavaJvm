package com.tangjingkai.jvm.naive;

import com.tangjingkai.jvm.rtda.heap.InternedStrings;
import com.tangjingkai.jvm.rtda.heap.JJvmClass;
import com.tangjingkai.jvm.rtda.heap.JJvmClassLoader;
import com.tangjingkai.jvm.rtda.heap.JJvmObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by totran on 11/29/16.
 */
public class NativeMethods {
    static final Map<String, NativeMethod> registry = new HashMap<>();

    static final NativeMethod emptyNativeMethod = frame -> {
    };

    public static void register(String className, String methodName, String methodDescriptor, NativeMethod method) {
        String key = buildKey(className, methodName, methodDescriptor);
        registry.put(key, method);
    }

    private static String buildKey(String className, String methodName, String methodDescriptor) {
        return String.format("%s~%s~%s", className, methodName, methodDescriptor);
    }

    public static NativeMethod findNativeMethod(String className, String methodName, String methodDescriptor) {
        String key = buildKey(className, methodName, methodDescriptor);
        if (registry.containsKey(key)) {
            return registry.get(key);
        }

        if (methodDescriptor.equals("()V") && methodName.equals("registerNatives")) {
            return emptyNativeMethod;
        }
        return null;
    }

    static {
        // java.lang.Object::getClass
        register(
                "java/lang/Object",
                "getClass",
                "()Ljava/lang/Class;",
                frame -> {
                    JJvmObject thisRef = (JJvmObject) frame.getLocalVars().getThis();
                    JJvmObject cls = thisRef.getJJvmClass().getjClass();
                    frame.getOperandStack().pushRef(cls);
                }
        );

        // java.lang.Class::getPrimitiveClass
        register(
                "java/lang/Class",
                "getPrimitiveClass",
                "(Ljava/lang/String;)Ljava/lang/Class;",
                frame -> {
                    JJvmObject nameObj = (JJvmObject) frame.getLocalVars().getRef(0);
                    String name = InternedStrings.unwrapString(nameObj);

                    JJvmClassLoader loader = frame.getMethod().getJJvmClass().getClassLoader();
                    JJvmObject cls = loader.loadClass(name).getjClass();

                    frame.getOperandStack().pushRef(cls);
                }
        );

        // java.lang.Class::getName0
        register(
                "java/lang/Class",
                "getName0",
                "()Ljava/lang/String;",
                frame -> {
                    JJvmObject thisRef = (JJvmObject) frame.getLocalVars().getThis();
                    JJvmClass cls = (JJvmClass) thisRef.getExtra();

                    String name = cls.getJavaName();
                    JJvmObject nameObj = InternedStrings.getString(cls.getClassLoader(), name);

                    frame.getOperandStack().pushRef(nameObj);
                }
        );

        // java.lang.Class::desiredAssertionStatus0
        register(
                "java/lang/Class",
                "desiredAssertionStatus0",
                "(Ljava/lang/Class;)Z",
                frame -> frame.getOperandStack().pushBoolean(false)
        );
    }
}
