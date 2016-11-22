package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/14/16.
 */
public class ConstantPool {
    public ConstantInfo[] getConstantInfos() {
        return constantInfos;
    }

    private ConstantInfo[] constantInfos;

    private ConstantPool(int n) {
        constantInfos = new ConstantInfo[n];
    }

    public static ConstantPool readConstantPool(ClassReader reader) {
        int count = Short.toUnsignedInt(reader.readU2());
        ConstantPool constantPool = new ConstantPool(count);
        for (int i = 1; i < count; i++) {
            constantPool.constantInfos[i] = ConstantInfos.readConstantInfo(reader, constantPool);
            if (constantPool.constantInfos[i] instanceof LongCI
                    || constantPool.constantInfos[i] instanceof DoubleCI) {
                i++;
            }
        }
        return constantPool;
    }

    public String getClassName(short index) {
        ClassCI classInfo = (ClassCI) getConstantInfo(index);
        return getUtf8(classInfo.nameIndex);
    }

    public ConstantInfo getConstantInfo(short index) {
        return constantInfos[index];
    }

    public NameAndTypeCI.NTValue getNameAndType(short index) {
        NameAndTypeCI ntInfo = (NameAndTypeCI) getConstantInfo(index);
        NameAndTypeCI.NTValue ntv = new NameAndTypeCI.NTValue();
        ntv.name = getUtf8(ntInfo.nameIndex);
        ntv.type = getUtf8(ntInfo.descriptorIndex);
        return ntv;
    }

    public String getUtf8(short index) {
        Utf8CI utf8Info = (Utf8CI) getConstantInfo(index);
        return utf8Info.str;
    }
}
