package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/17/16.
 */
public class CodeAttribute implements AttributeInfo {
    public short getMaxStack() {
        return maxStack;
    }

    public short getMaxLocals() {
        return maxLocals;
    }

    public byte[] getCode() {
        return code;
    }

    public ExceptionsAttribute.ExceptionTableEntry[] getExceptionTable() {
        return exceptionTable;
    }

    ConstantPool constantPool;
    short maxStack;
    short maxLocals;
    byte[] code;
    ExceptionsAttribute.ExceptionTableEntry[] exceptionTable;
    AttributeInfo[] attributes;

    public CodeAttribute(ConstantPool constantPool) {
        this.constantPool = constantPool;
    }

    @Override
    public void readInfo(ClassReader reader) {
        maxStack = reader.readU2();
        maxLocals = reader.readU2();
        int codeLen = reader.readU4();
        code = reader.readBytes(codeLen);
        exceptionTable = ExceptionsAttribute.ExceptionTableEntry.readExceptionTable(reader);
        attributes = AttributeInfo.readAttributes(reader, constantPool);
    }
}
