package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/15/16.
 */
public interface ConstantInfo {
    static ConstantInfo readConstantInfo(ClassReader reader, ConstantPool constantPool) {
        byte tag = reader.readU1();
        ConstantInfo constantInfo = buildConstantInfo(tag, constantPool);
        constantInfo.readInfo(reader);
        return constantInfo;
    }

    static ConstantInfo buildConstantInfo(byte tag, ConstantPool constantPool) {
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

    void readInfo(ClassReader reader);

    class TAG {
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

    class LongCI implements ConstantInfo {
        long val;

        @Override
        public void readInfo(ClassReader reader) {
            long highBytes = Integer.toUnsignedLong(reader.readU4());
            long lowBytes = Integer.toUnsignedLong(reader.readU4());
            val = (highBytes << 32) | lowBytes;
        }
    }

    class DoubleCI implements ConstantInfo {
        double val;

        @Override
        public void readInfo(ClassReader reader) {
            long highBytes = Integer.toUnsignedLong(reader.readU4());
            long lowBytes = Integer.toUnsignedLong(reader.readU4());
            val = Double.longBitsToDouble((highBytes << 32) | lowBytes);
        }
    }

    class NameAndTypeCI implements ConstantInfo {
        short nameIndex;
        short descriptorIndex;

        @Override
        public void readInfo(ClassReader reader) {
            nameIndex = reader.readU2();
            descriptorIndex = reader.readU2();
        }

        static class NTValue {
            String name;
            String type;
        }
    }

    class MemberRefCI implements ConstantInfo {
        ConstantPool constantPool;
        short classIndex;
        short nameAndTypeIndex;

        public MemberRefCI(ConstantPool constantPool) {
            this.constantPool = constantPool;
        }

        @Override
        public void readInfo(ClassReader reader) {
            classIndex = reader.readU2();
            nameAndTypeIndex = reader.readU2();
        }

        public String getClassName() {
            return constantPool.getClassName(classIndex);
        }

        public NameAndTypeCI.NTValue getNameAndDescriptor() {
            return constantPool.getNameAndType(nameAndTypeIndex);
        }
    }

    class ClassCI implements ConstantInfo {
        ConstantPool constantPool;
        short nameIndex;

        public ClassCI(ConstantPool constantPool) {
            this.constantPool = constantPool;
        }

        @Override
        public void readInfo(ClassReader reader) {
            nameIndex = reader.readU2();
        }

        public String getName() {
            return constantPool.getUtf8(nameIndex);
        }
    }

    class Utf8CI implements ConstantInfo {
        String str;

        @Override
        public void readInfo(ClassReader reader) {
            int length = Short.toUnsignedInt(reader.readU2());
            byte[] data = reader.readBytes(length);
            str = new String(data);
        }
    }

    class IntegerCI implements ConstantInfo {
        int val;

        @Override
        public void readInfo(ClassReader reader) {
            val = reader.readU4();
        }
    }

    class FloatCI implements ConstantInfo {
        float val;

        @Override
        public void readInfo(ClassReader reader) {
            int bytes = reader.readU4();
            val = Float.intBitsToFloat(bytes);
        }
    }

    class StringCI implements ConstantInfo {
        ConstantPool constantPool;
        short stringIndex;

        public StringCI(ConstantPool constantPool) {
            this.constantPool = constantPool;
        }

        @Override
        public void readInfo(ClassReader reader) {
            stringIndex = reader.readU2();
        }

        public String getString() {
            return constantPool.getUtf8(stringIndex);
        }
    }

    class FieldRefCI extends MemberRefCI {
        public FieldRefCI(ConstantPool constantPool) {
            super(constantPool);
        }
    }

    class MethodRefCI extends MemberRefCI {
        public MethodRefCI(ConstantPool constantPool) {
            super(constantPool);
        }
    }

    class InterfaceMethodRefCI extends MemberRefCI {
        public InterfaceMethodRefCI(ConstantPool constantPool) {
            super(constantPool);
        }
    }

    class MethodTypeCI implements ConstantInfo {
        short descriptorIndex;

        @Override
        public void readInfo(ClassReader reader) {
            descriptorIndex = reader.readU2();
        }
    }

    class MethodHandleCI implements ConstantInfo {
        byte refKind;
        short refIndex;

        @Override
        public void readInfo(ClassReader reader) {
            refKind = reader.readU1();
            refIndex = reader.readU2();
        }
    }

    class InvokeDynamicCI implements ConstantInfo {
        short bootstrapMethodAttrIndex;
        short nameAndTypeIndex;

        @Override
        public void readInfo(ClassReader reader) {
            bootstrapMethodAttrIndex = reader.readU2();
            nameAndTypeIndex = reader.readU2();
        }
    }
}
