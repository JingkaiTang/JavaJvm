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
            constantPool.constantInfos[i] = ConstantInfo.readConstantInfo(reader, constantPool);
            if (constantPool.constantInfos[i] instanceof ConstantInfo.LongCI
                    || constantPool.constantInfos[i] instanceof ConstantInfo.DoubleCI) {
                i++;
            }
        }
        return constantPool;
    }

    public String getClassName(short index) {
        ConstantInfo.ClassCI classInfo = (ConstantInfo.ClassCI) getConstantInfo(index);
        return getUtf8(classInfo.nameIndex);
    }

    public ConstantInfo getConstantInfo(short index) {
        return constantInfos[index];
    }

    public ConstantInfo.NameAndTypeCI.NTValue getNameAndType(short index) {
        ConstantInfo.NameAndTypeCI ntInfo = (ConstantInfo.NameAndTypeCI) getConstantInfo(index);
        ConstantInfo.NameAndTypeCI.NTValue ntv = new ConstantInfo.NameAndTypeCI.NTValue();
        ntv.name = getUtf8(ntInfo.nameIndex);
        ntv.type = getUtf8(ntInfo.descriptorIndex);
        return ntv;
    }

    public String getUtf8(short index) {
        ConstantInfo.Utf8CI utf8Info = (ConstantInfo.Utf8CI) getConstantInfo(index);
        return utf8Info.str;
    }
}
