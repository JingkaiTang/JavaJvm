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

    class UnparsedAttribute implements AttributeInfo {
        String name;
        int length;
        byte[] info;

        public UnparsedAttribute(String name, int length) {
            this.name = name;
            this.length = length;
        }

        @Override
        public void readInfo(ClassReader reader) {
            info = reader.readBytes(length);
        }
    }

    class MarkerAttribute implements AttributeInfo {
        @Override
        public void readInfo(ClassReader reader) {
            // pass
        }
    }

    class DeprecatedAttribute extends MarkerAttribute {
    }

    class SyntheticAttribute extends MarkerAttribute {
    }

    class SourceFileAttribute implements AttributeInfo {
        ConstantPool constantPool;
        short sourceFileIndex;

        public SourceFileAttribute(ConstantPool constantPool) {
        }

        @Override
        public void readInfo(ClassReader reader) {
            sourceFileIndex = reader.readU2();
        }

        public String getFileName() {
            return constantPool.getUtf8(sourceFileIndex);
        }
    }

    class ConstantValueAttribute implements AttributeInfo {
        short constantValueIndex;

        @Override
        public void readInfo(ClassReader reader) {
            constantValueIndex = reader.readU2();
        }

        public short getConstantValueIndex() {
            return constantValueIndex;
        }
    }

    class CodeAttribute implements AttributeInfo {
        ConstantPool constantPool;
        short maxStack;
        short maxLocals;
        byte[] code;
        ExceptionTableEntry[] exceptionTable;
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
            exceptionTable = ExceptionTableEntry.readExceptionTable(reader);
            attributes = AttributeInfo.readAttributes(reader, constantPool);
        }
    }

    class ExceptionsAttribute implements AttributeInfo {
        short[] exceptionIndexTable;

        @Override
        public void readInfo(ClassReader reader) {
            exceptionIndexTable = reader.readU2s();
        }

        public short[] getExceptionIndexTable() {
            return exceptionIndexTable;
        }
    }

    class LineNumberTableAttribute implements AttributeInfo {
        LineNumberTableEntry[] lineNumberTable;

        @Override
        public void readInfo(ClassReader reader) {
            int len = Short.toUnsignedInt(reader.readU2());
            lineNumberTable = new LineNumberTableEntry[len];
            for (int i = 0; i < len; i++) {
                lineNumberTable[i] = LineNumberTableEntry.readLineNumberTableEntry(reader);
            }
        }
    }

    class LocalVariableTableAttribute implements AttributeInfo {
        LocalVariableTableEntry[] localVariableTable;

        @Override
        public void readInfo(ClassReader reader) {
            int len = Short.toUnsignedInt(reader.readU2());
            localVariableTable = new LocalVariableTableEntry[len];
            for (int i = 0; i < len; i++) {
                localVariableTable[i] = LocalVariableTableEntry.readLocalVariableTableEntry(reader);
            }
        }
    }

    class LocalVariableTableEntry {
        short startPc;
        short length;
        short nameIndex;
        short descriptorIndex;
        short index;

        private LocalVariableTableEntry() {

        }

        public static LocalVariableTableEntry readLocalVariableTableEntry(ClassReader reader) {
            LocalVariableTableEntry entry = new LocalVariableTableEntry();
            entry.startPc = reader.readU2();
            entry.length = reader.readU2();
            entry.nameIndex = reader.readU2();
            entry.descriptorIndex = reader.readU2();
            entry.index = reader.readU2();
            return entry;
        }
    }

    class LineNumberTableEntry {
        short startPc;
        short lineNumber;

        private LineNumberTableEntry() {
        }

        public static LineNumberTableEntry readLineNumberTableEntry(ClassReader reader) {
            LineNumberTableEntry entry = new LineNumberTableEntry();
            entry.startPc = reader.readU2();
            entry.lineNumber = reader.readU2();
            return entry;
        }
    }

    class ExceptionTableEntry {
        short startPc;
        short endPc;
        short handlerPc;
        short catchType;

        private ExceptionTableEntry() {

        }

        public static ExceptionTableEntry readExceptionTableEntry(ClassReader reader) {
            ExceptionTableEntry entry = new ExceptionTableEntry();
            entry.startPc = reader.readU2();
            entry.endPc = reader.readU2();
            entry.handlerPc = reader.readU2();
            entry.catchType = reader.readU2();
            return entry;
        }

        public static ExceptionTableEntry[] readExceptionTable(ClassReader reader) {
            int len = Short.toUnsignedInt(reader.readU2());
            ExceptionTableEntry[] exceptionTable = new ExceptionTableEntry[len];
            for (int i = 0; i < len; i++) {
                exceptionTable[i] = readExceptionTableEntry(reader);
            }
            return exceptionTable;
        }
    }
}
