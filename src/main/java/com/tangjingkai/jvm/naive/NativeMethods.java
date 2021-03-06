package com.tangjingkai.jvm.naive;

import com.tangjingkai.jvm.instructions.Instruction;
import com.tangjingkai.jvm.instructions.Instructions;
import com.tangjingkai.jvm.rtda.Frame;
import com.tangjingkai.jvm.rtda.LocalVars;
import com.tangjingkai.jvm.rtda.OperandStack;
import com.tangjingkai.jvm.rtda.heap.*;

import java.util.Arrays;
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

        // java.lang.System::arraycopy
        register(
                "java/lang/System",
                "arraycopy",
                "(Ljava/lang/Object;ILjava/lang/Object;II)V",
                new NativeMethod() {
                    private boolean checkArrayCopy(JJvmObject src, JJvmObject dest) {
                        JJvmClass srcClass = src.getJJvmClass();
                        JJvmClass destClass = dest.getJJvmClass();
                        if (!srcClass.isArray() || !destClass.isArray()) {
                            return false;
                        }
                        if (srcClass.getComponentClass().isPrimitive() || destClass.getComponentClass().isPrimitive()) {
                            return srcClass == destClass;
                        }
                        return true;
                    }

                    @Override
                    public void execute(Frame frame) {
                        LocalVars vars = frame.getLocalVars();
                        JJvmObject src = (JJvmObject) vars.getRef(0);
                        int srcPos = vars.getInt(1);
                        JJvmObject dest = (JJvmObject) vars.getRef(2);
                        int destPos = vars.getInt(3);
                        int length = vars.getInt(4);

                        if (src == null || dest == null) {
                            throw new NullPointerException();
                        }

                        if (!checkArrayCopy(src, dest)) {
                            throw new ArrayStoreException();
                        }

                        if (srcPos < 0 ||
                                destPos < 0 ||
                                length < 0 ||
                                srcPos + length > src.getArrayLength() ||
                                destPos + length > dest.getArrayLength()) {
                            throw new IndexOutOfBoundsException();
                        }

                        System.arraycopy(src.getData(), srcPos, dest.getData(), destPos, length);
                    }
                }
        );

        // java.lang.Float::floatToRawIntBits
        register(
                "java/lang/Float",
                "floatToRawIntBits",
                "(F)I",
                frame -> {
                    float val = frame.getLocalVars().getFloat(0);
                    int bits = Float.floatToRawIntBits(val);
                    frame.getOperandStack().pushInt(bits);
                }
        );

        // java.lang.Double::doubleToRawLongBits
        register(
                "java/lang/Double",
                "doubleToRawLongBits",
                "(D)J",
                frame -> {
                    double val = frame.getLocalVars().getDouble(0);
                    long bits = Double.doubleToRawLongBits(val);
                    frame.getOperandStack().pushLong(bits);
                }
        );

        // java.lang.Double::longBitsToDouble
        register(
                "java/lang/Double",
                "longBitsToDouble",
                "(J)D",
                frame -> {
                    long bits = frame.getLocalVars().getLong(0);
                    double val = Double.longBitsToDouble(bits);
                    frame.getOperandStack().pushDouble(val);
                }
        );

        // java.lang.String::intern
        register(
                "java/lang/String",
                "intern",
                "()Ljava/lang/String;",
                frame -> {
                    JJvmObject thisRef = (JJvmObject) frame.getLocalVars().getThis();
                    JJvmObject interned = InternedStrings.getInterned(thisRef);
                    frame.getOperandStack().pushRef(interned);
                }
        );

        // java.lang.Object::hashCode
        register(
                "java/lang/Object",
                "hashCode",
                "()I",
                frame -> {
                    JJvmObject thisRef = (JJvmObject) frame.getLocalVars().getThis();
                    frame.getOperandStack().pushInt(thisRef.hashCode());
                }
        );

        // java.lang.Object::clone
        register(
                "java/lang/Object",
                "clone",
                "()Ljava/lang/Object;",
                frame -> {
                    JJvmObject thisRef = (JJvmObject) frame.getLocalVars().getThis();
                    JJvmClass cloneable = thisRef.getJJvmClass().getClassLoader().loadClass("java/lang/Cloneable");
                    if (!thisRef.getJJvmClass().isImplements(cloneable)) {
                        throw new RuntimeException(new CloneNotSupportedException());
                    }
                    frame.getOperandStack().pushRef(thisRef.clone());
                }
        );

        // sun.misc.VM::initialize
        register(
                "sun/misc/VM",
                "initialize",
                "()V",
                frame -> {
                    JJvmClassLoader classLoader = frame.getMethod().getJJvmClass().getClassLoader();
                    JJvmClass jlSysClass = classLoader.loadClass("java/lang/System");
                    JJvmMethod initSysClass = jlSysClass.getStaticMethod("initializeSystemClass", "()V");
                    Instructions.invokeMethod(frame, initSysClass);
                }
        );

        // java.lang.Throwable::fillInStackTrace
        register(
                "java/lang/Throwable",
                "fillInStackTrace",
                "(I)Ljava/lang/Throwable;",
                frame -> {
                    JJvmObject thisRef = (JJvmObject) frame.getLocalVars().getThis();
                    frame.getOperandStack().pushRef(thisRef);
                    JJvmStackTraceElement[] stes = JJvmStackTraceElement.createStackTraceElements(thisRef, frame.getThread());
                    thisRef.setExtra(stes);
                }
        );

        // java.lang.System::setOut0
        register(
                "java/lang/System",
                "setOut0",
                "(Ljava/io/PrintStream;)V",
                frame -> {
                    JJvmObject out = (JJvmObject) frame.getLocalVars().getRef(0);
                    JJvmClass sysClass = frame.getMethod().getJJvmClass();
                    sysClass.setRefVar("out", "Ljava/io/PrintStream;", out);
                }
        );

        // java.io.FileOutputStream::writeBytes
        register(
                "java/io/FileOutputStream",
                "writeBytes",
                "([BIIZ)V",
                frame -> {
                    LocalVars vars = frame.getLocalVars();
                    // this
                    JJvmObject b = (JJvmObject) vars.getRef(1);
                    int off = vars.getInt(2);
                    int len = vars.getInt(3);
                    // append
                    byte[] data = b.getBytes();
                    String str = new String(data, off, len);
                    System.out.println(str);
                }
        );

        // java.lang.System::initProperties
        register(
                "java/lang/System",
                "initProperties",
                "(Ljava/util/Properties;)Ljava/util/Properties;",
                frame -> {
                    LocalVars vars = frame.getLocalVars();
                    JJvmObject props = (JJvmObject) vars.getRef(0);
                    OperandStack stack = frame.getOperandStack();
                    stack.pushRef(props);
                    // ...
                }
        );

        // java.io.FileInputStream::initIDs
        register(
                "java/io/FileInputStream",
                "initIDs",
                "()V",
                frame -> {}
        );

        // java.io.FileDescriptor::initIDs
        register(
                "java/io/FileDescriptor",
                "initIDs",
                "()V",
                frame -> {}
        );
    }
}
