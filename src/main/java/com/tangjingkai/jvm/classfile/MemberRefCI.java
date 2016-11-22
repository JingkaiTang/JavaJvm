package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/19/16.
 */
public class MemberRefCI implements ConstantInfo {
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
