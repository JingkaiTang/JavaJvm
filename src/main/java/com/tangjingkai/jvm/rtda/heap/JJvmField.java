package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.classfile.ConstantValueAttribute;
import com.tangjingkai.jvm.classfile.MemberInfo;

/**
 * Created by totran on 11/19/16.
 */
public class JJvmField extends JJvmClassMember {
    int constValueIndex;
    int slotId;

    protected JJvmField(JJvmClass jjvmClass, MemberInfo memberInfo) {
        super(jjvmClass, memberInfo);
        ConstantValueAttribute constantValueAttribute = memberInfo.getConstantValueAttribute();
        if (constantValueAttribute != null) {
            this.constValueIndex = Short.toUnsignedInt(constantValueAttribute.getConstantValueIndex());
        }
    }

    public int getSlotId() {
        return slotId;
    }

    public static JJvmField[] extractFileds(JJvmClass jjvmClass, MemberInfo[] fields) {
        if (fields == null) {

            return null;
        }

        JJvmField[] jjvmFields = new JJvmField[fields.length];
        for (int i = 0; i < fields.length; i++) {
            jjvmFields[i] = new JJvmField(jjvmClass, fields[i]);
        }
        return jjvmFields;
    }

    public boolean isVolatile() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_VOLATILE);
    }

    public boolean isTransient() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_TRANSIENT);
    }

    public boolean isEnum() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_ENUM);
    }

    public boolean isLongOrDouble() {
        return descriptor.equals("J") || descriptor.equals("D");
    }
}
