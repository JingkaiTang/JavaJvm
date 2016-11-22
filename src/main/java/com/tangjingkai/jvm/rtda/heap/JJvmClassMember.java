package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.classfile.MemberInfo;

/**
 * Created by totran on 11/19/16.
 */
public class JJvmClassMember {
    int accessFlags;
    String name;
    String descriptor;

    public int getAccessFlags() {
        return accessFlags;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public JJvmClass getJJvmClass() {
        return jjvmClass;
    }

    JJvmClass jjvmClass;

    protected JJvmClassMember(JJvmClass jjvmClass, MemberInfo memberInfo) {
        this.accessFlags = Short.toUnsignedInt(memberInfo.getAccessFlags());
        this.name = memberInfo.getName();
        this.descriptor = memberInfo.getDescriptor();
        this.jjvmClass = jjvmClass;
    }

    public boolean isPublic() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_PUBLIC);
    }

    public boolean isPrivate() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_PRIVATE);
    }

    public boolean isProtected() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_PROTECTED);
    }

    public boolean isStatic() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_STATIC);
    }

    public boolean isFinal() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_FINAL);
    }

    public boolean isSynthetic() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_SYNTHETIC);
    }

    public boolean isAccessibleTo(JJvmClass other) {
        if (isPublic()) {
            return true;
        }

        if (isProtected()) {
            return other == jjvmClass
                    || other.isSubClassOf(jjvmClass)
                    || other.getPackageName().equals(jjvmClass.getPackageName());
        }

        if (!isPrivate()) {
            return other.getPackageName().equals(jjvmClass.getPackageName());
        }

        return other == jjvmClass;
    }
}
