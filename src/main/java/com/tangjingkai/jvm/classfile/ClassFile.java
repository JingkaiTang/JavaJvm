package com.tangjingkai.jvm.classfile;

import java.util.Arrays;

/**
 * Created by totran on 11/14/16.
 */
public class ClassFile {
    // private int magic;
    private short minorVersion;
    private short majorVersion;
    private ConstantPool constantPool;
    private short accessFlags;
    private short thisClass;
    private short superClass;
    private short[] interfaces;
    private MemberInfo[] fields;
    private MemberInfo[] methods;
    private AttributeInfo[] attributes;

    public ClassFile(byte[] data) {
        ClassReader cr = new ClassReader(data);
        read(cr);
    }

    @Override
    public String toString() {
        return "ClassFile{" +
                "minorVersion=" + minorVersion +
                ", majorVersion=" + majorVersion +
                ", constantPool=" + constantPool +
                ", accessFlags=" + accessFlags +
                ", thisClass=" + thisClass +
                ", superClass=" + superClass +
                ", interfaces=" + Arrays.toString(interfaces) +
                ", fields=" + Arrays.toString(fields) +
                ", methods=" + Arrays.toString(methods) +
                ", attributes=" + Arrays.toString(attributes) +
                '}';
    }

    private void read(ClassReader reader) {
        readAndCheckMagic(reader);
        readAndCheckVersion(reader);
        constantPool = ConstantPool.readConstantPool(reader);
        accessFlags = reader.readU2();
        thisClass = reader.readU2();
        superClass = reader.readU2();
        interfaces = reader.readU2s();
        fields = MemberInfo.readMembers(reader, constantPool);
        methods = MemberInfo.readMembers(reader, constantPool);
        attributes = AttributeInfo.readAttributes(reader, constantPool);
    }

    public String getClassName() {
        return constantPool.getClassName(thisClass);
    }

    public String getSuperClassName() {
        if (superClass > 0) {
            return constantPool.getClassName(superClass);
        }
        return "";
    }

    public String[] getInterfaceNames() {
        String[] interfaceNames = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            interfaceNames[i] = constantPool.getClassName(interfaces[i]);
        }
        return interfaceNames;
    }

    private void readAndCheckMagic(ClassReader reader) {
        int magic = reader.readU4();
        if (magic != 0xCAFEBABE) {
            throw new RuntimeException("java.lang.ClassFormatError: magic!");
        }
    }

    private void readAndCheckVersion(ClassReader reader) {
        minorVersion = reader.readU2();
        majorVersion = reader.readU2();
        if (majorVersion == 45) {
            return;
        } else if (majorVersion >= 46 && majorVersion <= 52) {
            if (minorVersion == 0) {
                return;
            }
        }
        throw new RuntimeException("java.lang.UnsupportedClassVersionError!");
    }
}
