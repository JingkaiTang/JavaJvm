package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/14/16.
 */
public interface AttributeInfo {
    void readInfo(ClassReader reader);

    static AttributeInfo[] readAttributes(ClassReader reader, ConstantPool constantPool) {
        int count = Short.toUnsignedInt(reader.readU2());
        AttributeInfo[] attributeInfos = new AttributeInfo[count];
        for (int i = 0; i < count; i++) {
            attributeInfos[i] = readAttribute(reader, constantPool);
        }
        return attributeInfos;
    }

    static AttributeInfo readAttribute(ClassReader reader, ConstantPool constantPool) {
        short attrNameIndex = reader.readU2();
        String attrName = constantPool.getUtf8(attrNameIndex);
        int attrLen = reader.readU4();
        AttributeInfo attributeInfo = buildAttribute(attrName, attrLen, constantPool);
        attributeInfo.readInfo(reader);
        return attributeInfo;
    }

    static AttributeInfo buildAttribute(String attrName, int attrLen, ConstantPool constantPool) {
        switch (attrName) {
            case "Code":
                return new CodeAttribute(constantPool);
            case "ConstantValue":
                return new ConstantValueAttribute();
            case "Deprecated":
                return new DeprecatedAttribute();
            case "Exceptions":
                return new ExceptionsAttribute();
            case "LineNumberTable":
                return new LineNumberTableAttribute();
            case "LocalVariableTable":
                return new LocalVariableTableAttribute();
            case "SourceFile":
                return new SourceFileAttribute(constantPool);
            case "Synthetic":
                return new SyntheticAttribute();
            default:
                return new UnparsedAttribute(attrName, attrLen);
        }
    }

}
