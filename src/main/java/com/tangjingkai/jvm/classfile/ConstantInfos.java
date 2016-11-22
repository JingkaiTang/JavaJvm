package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/19/16.
 */
public class ConstantInfos {
    public static ConstantInfo readConstantInfo(ClassReader reader, ConstantPool constantPool) {
        byte tag = reader.readU1();
        ConstantInfo constantInfo = buildConstantInfo(tag, constantPool);
        constantInfo.readInfo(reader);
        return constantInfo;
    }

    public static ConstantInfo buildConstantInfo(byte tag, ConstantPool constantPool) {
        switch (tag) {
            case TAG.INTEGER:
                return new IntegerCI();
            case TAG.FLOAT:
                return new FloatCI();
            case TAG.LONG:
                return new LongCI();
            case TAG.DOUBLE:
                return new DoubleCI();
            case TAG.UTF8:
                return new Utf8CI();
            case TAG.STRING:
                return new StringCI(constantPool);
            case TAG.CLASS:
                return new ClassCI(constantPool);
            case TAG.FIELD_REF:
                return new FieldRefCI(constantPool);
            case TAG.METHOD_REF:
                return new MethodRefCI(constantPool);
            case TAG.INTERFACE_METHOD_REF:
                return new InterfaceMethodRefCI(constantPool);
            case TAG.NAME_AND_TYPE:
                return new NameAndTypeCI();
            case TAG.METHOD_TYPE:
                return new MethodTypeCI();
            case TAG.METHOD_HANDLE:
                return new MethodHandleCI();
            case TAG.INVOKE_DYNAMIC:
                return new InvokeDynamicCI();
            default:
                throw new RuntimeException("ClassFormatError: constant pool tag!");
        }
    }

    public static class TAG {
        final static byte CLASS = 7;
        final static byte FIELD_REF = 9;
        final static byte METHOD_REF = 10;
        final static byte INTERFACE_METHOD_REF = 11;
        final static byte STRING = 8;
        final static byte INTEGER = 3;
        final static byte FLOAT = 4;
        final static byte LONG = 5;
        final static byte DOUBLE = 6;
        final static byte NAME_AND_TYPE = 12;
        final static byte UTF8 = 1;
        final static byte METHOD_HANDLE = 15;
        final static byte METHOD_TYPE = 16;
        final static byte INVOKE_DYNAMIC = 18;
    }
}
