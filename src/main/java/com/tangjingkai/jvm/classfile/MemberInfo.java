package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/14/16.
 */
public class MemberInfo {
    private ConstantPool constantPool;

    public short getAccessFlags() {
        return accessFlags;
    }

    private short accessFlags;
    private short nameIndex;
    private short descriptorIndex;
    private AttributeInfo[] attributes;

    private MemberInfo() {
    }

    public static MemberInfo readMemberInfo(ClassReader reader, ConstantPool constantPool) {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.constantPool = constantPool;
        memberInfo.accessFlags = reader.readU2();
        memberInfo.nameIndex = reader.readU2();
        memberInfo.descriptorIndex = reader.readU2();
        memberInfo.attributes = AttributeInfo.readAttributes(reader, constantPool);
        return memberInfo;
    }

    public static MemberInfo[] readMembers(ClassReader reader, ConstantPool constantPool) {
        int count = Short.toUnsignedInt(reader.readU2());
        MemberInfo[] memberInfos = new MemberInfo[count];
        for (int i = 0; i < count; i++) {
            memberInfos[i] = MemberInfo.readMemberInfo(reader, constantPool);
        }
        return memberInfos;
    }

    public String getName() {
        return constantPool.getUtf8(nameIndex);
    }

    public String getDescriptor() {
        return constantPool.getUtf8(descriptorIndex);
    }

    public CodeAttribute getCodeAttribute() {
        for (AttributeInfo attribute : attributes) {
            if (attribute instanceof CodeAttribute) {
                return (CodeAttribute) attribute;
            }
        }
        return null;
    }

    public ConstantValueAttribute getConstantValueAttribute() {
        for (AttributeInfo attrInfo : attributes) {
            if (attrInfo instanceof ConstantValueAttribute) {
                return (ConstantValueAttribute) attrInfo;
            }
        }
        return null;
    }
}
