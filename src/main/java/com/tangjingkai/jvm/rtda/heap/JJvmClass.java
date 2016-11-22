package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.classfile.ClassFile;

/**
 * Created by totran on 11/19/16.
 */
public class JJvmClass {
    int accessFlags;
    String name;
    String superClassName;
    String[] interfaceNames;
    JJvmConstantPool constantPool;
    JJvmField[] fields;
    JJvmMethod[] methods;
    JJvmClassLoader loader;
    JJvmClass superClass;
    JJvmClass[] interfaces;
    int instanceSlotCount;
    int staticSlotCount;
    JJvmSlots staticVars;

    public JJvmConstantPool getConstantPool() {
        return constantPool;
    }

    public JJvmClass(ClassFile cf) {
        this.accessFlags = Short.toUnsignedInt(cf.getAccessFlags());
        this.name = cf.getClassName();
        this.superClassName = cf.getSuperClassName();

        this.interfaceNames = cf.getInterfaceNames();
        this.constantPool = new JJvmConstantPool(this, cf.getConstantPool());
        this.fields = JJvmField.extractFileds(this, cf.getFields());
        this.methods = JJvmMethod.extractMethods(this, cf.getMethods());
    }

    public JJvmObject newObject() {
        return new JJvmObject(this);
    }

    public JJvmSlots getStaticVars() {
        return staticVars;
    }

    public boolean isPublic() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_PUBLIC);
    }

    public boolean isFinal() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_FINAL);
    }

    public boolean isSuper() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_SUPER);
    }

    public boolean isInterface() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_INTERFACE);
    }

    public boolean isAbstract() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_ABSTRACT);
    }

    public boolean isSynthetic() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_SYNTHETIC);
    }

    public boolean isAnnotation() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_ANNOTATION);
    }

    public boolean isEnum() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_ENUM);
    }

    public boolean isAccessibleTo(JJvmClass otherClass) {
        return isPublic() || getPackageName().equals(otherClass.getPackageName());
    }

    String getPackageName() {
        int i = name.lastIndexOf('/');
        if (i >= 0) {
            return name.substring(0, i);
        }
        return "";
    }

    public boolean isSubClassOf(JJvmClass jjvmClass) {
        for (JJvmClass cls = superClass; cls != null; cls = cls.superClass) {
            if (jjvmClass == superClass) {
                return true;
            }
        }
        return false;
    }

    public boolean isAssignableFrom(JJvmClass cls) {
        if (this == cls) {
            return true;
        }

        if (isInterface()) {
            return cls.isImplements(this);
        } else {
            return cls.isSubClassOf(this);
        }
    }

    private boolean isImplements(JJvmClass iface) {
        for (JJvmClass c = this; c != null; c = c.superClass) {
            if (c.interfaces == null) {
                continue;
            }
            for (JJvmClass i : c.interfaces) {
                if (i == iface || i.isSubInterfaceOf(iface)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSubInterfaceOf(JJvmClass iface) {
        if (interfaces == null) {
            return false;
        }

        for (JJvmClass i : interfaces) {
            if (iface == i || i.isSubInterfaceOf(iface)) {
                return true;
            }
        }

        return false;
    }

    public JJvmMethod getMainMethod() {
        return getStaticMethod("main", "([Ljava/lang/String;)V");
    }

    private JJvmMethod getStaticMethod(String name, String descriptor) {
        if (methods == null) {
            return null;
        }

        for (JJvmMethod method : methods) {
            if (method.isStatic() && method.name.equals(name) && method.descriptor.equals(descriptor)) {
                return method;
            }
        }

        return null;
    }
}
