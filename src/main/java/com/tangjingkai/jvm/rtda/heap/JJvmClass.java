package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.classfile.ClassFile;
import com.tangjingkai.jvm.rtda.Frame;
import com.tangjingkai.jvm.rtda.Thread;

import java.util.HashMap;
import java.util.Map;

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
    JJvmObject jClass;
    private String sourceFile;

    public JJvmObject getjClass() {
        return jClass;
    }

    public void setjClass(JJvmObject jClass) {
        this.jClass = jClass;
    }

    public JJvmClassLoader getClassLoader() {
        return loader;
    }

    boolean initStarted;

    public JJvmClass(ClassFile cf) {
        this.accessFlags = Short.toUnsignedInt(cf.getAccessFlags());
        this.name = cf.getClassName();
        this.superClassName = cf.getSuperClassName();
        this.sourceFile = cf.getSourceFile();
        this.interfaceNames = cf.getInterfaceNames();
        this.constantPool = new JJvmConstantPool(this, cf.getConstantPool());
        this.fields = JJvmField.extractFileds(this, cf.getFields());
        this.methods = JJvmMethod.extractMethods(this, cf.getMethods());
        this.initStarted = false;
    }

    public JJvmClass() {

    }

    public static JJvmClass loadArrayClass(JJvmClassLoader loader, String name) {
        JJvmClass jc = new JJvmClass();
        jc.accessFlags = JJvmAccessFlag.ACC_PUBLIC;
        jc.name = name;
        jc.loader = loader;
        jc.initStarted = true;
        jc.superClass = loader.loadClass("java/lang/Object");
        jc.interfaces = new JJvmClass[]{
                loader.loadClass("java/lang/Cloneable"),
                loader.loadClass("java/io/Serializable"),
        };
        return jc;
    }

    public JJvmObject newArray(int count) {
        if (!isArray()) {
            throw new RuntimeException("Not array class: " + name);
        }
        switch (name) {
            case "[Z":
                return new JJvmObject(this, new byte[count]);
            case "[B":
                return new JJvmObject(this, new byte[count]);
            case "[C":
                return new JJvmObject(this, new char[count]);
            case "[S":
                return new JJvmObject(this, new short[count]);
            case "[I":
                return new JJvmObject(this, new int[count]);
            case "[J":
                return new JJvmObject(this, new long[count]);
            case "[F":
                return new JJvmObject(this, new float[count]);
            case "[D":
                return new JJvmObject(this, new double[count]);
            default:
                return new JJvmObject(this, new JJvmObject[count]);
        }
    }

    public boolean isArray() {
        return name.charAt(0) == '[';
    }

    public boolean isInitStarted() {
        return initStarted;
    }

    public void startInit() {
        this.initStarted = true;
    }

    public JJvmConstantPool getConstantPool() {
        return constantPool;
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

    public String getPackageName() {
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

        if (cls.isArray()) {
            if (this.isArray()) {
                JJvmClass sc = cls.getComponentClass();
                JJvmClass tc = this.getComponentClass();
                return sc == tc || tc.isAssignableFrom(sc);
            } else {
                if (this.isInterface()) {
                    return this.isJlCloneable() || this.isJioSerializable();
                } else {
                    return this.isJlObejct();
                }
            }
        } else {
            if (cls.isInterface()) {
                if (this.isInterface()) {
                    return this.isSuperInterfaceOf(cls);
                } else {
                    return this.isJlObejct();
                }
            } else {
                if (this.isInterface()) {
                    return cls.isImplements(this);
                } else {
                    return cls.isSubClassOf(this);
                }
            }
        }
    }

    private boolean isSuperInterfaceOf(JJvmClass cls) {
        return cls.isSubClassOf(this);
    }

    private boolean isJlObejct() {
        return name.equals("java/lang/Object");
    }

    public boolean isImplements(JJvmClass iface) {
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

    public JJvmMethod getStaticMethod(String name, String descriptor) {
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

    public boolean isSuperClassOf(JJvmClass subClass) {
        return subClass.isSubClassOf(this);
    }

    public JJvmClass getSuperClass() {
        return superClass;
    }

    public String getName() {
        return name;
    }

    public void initClass(Thread thread) {
        startInit();
        scheduleClinit(thread);
        initSuperClass(thread);
    }

    private void initSuperClass(Thread thread) {
        if (!isInterface()) {
            if (superClass != null && !superClass.isInitStarted()) {
                superClass.initClass(thread);
            }
        }
    }

    private void scheduleClinit(Thread thread) {
        JJvmMethod clinit = getClinitMethod();
        if (clinit != null) {
            Frame frame = thread.buildFrame(clinit);
            thread.pushFrame(frame);
        }
    }

    public JJvmMethod getClinitMethod() {
        return getStaticMethod("<clinit>", "()V");
    }

    public JJvmClass getArrayClass() {
        String arrayClassName = getArrayClassName(name);
        return loader.loadClass(arrayClassName);
    }

    private static String getArrayClassName(String className) {
        return "[" + toDescriptor(className);
    }

    private static String toDescriptor(String className) {
        if (className.charAt(0) == '[') {
            return className;
        }
        if (primitiveTypes.containsKey(className)) {
            return primitiveTypes.get(className);
        }
        return "L" + className + ";";
    }

    private static final Map<String, String> primitiveTypes = new HashMap<>();

    static {
        primitiveTypes.put("void", "V");
        primitiveTypes.put("boolean", "Z");
        primitiveTypes.put("byte", "B");
        primitiveTypes.put("short", "S");
        primitiveTypes.put("int", "I");
        primitiveTypes.put("long", "J");
        primitiveTypes.put("char", "C");
        primitiveTypes.put("float", "F");
        primitiveTypes.put("double", "D");
    }

    public JJvmClass getComponentClass() {
        String componentClassName = getComponentClassName();
        return loader.loadClass(componentClassName);
    }

    public String getComponentClassName() {
        if (name.charAt(0) == '[') {
            String componentTypeDescriptor = name.substring(1);
            return toClassName(componentTypeDescriptor);
        }
        throw new RuntimeException("Not array: " + name);
    }

    private String toClassName(String descriptor) {
        if (descriptor.charAt(0) == '[') {
            return descriptor;
        }

        if (descriptor.charAt(0) == 'L') {
            return descriptor.substring(1, descriptor.length() - 1);
        }

        for (Map.Entry<String, String> entry : primitiveTypes.entrySet()) {
            if (entry.getValue().equals(descriptor)) {
                return entry.getKey();
            }
        }

        throw new RuntimeException("Invalid descriptor: " + descriptor);
    }

    public boolean isJlCloneable() {
        return name.equals("java/lang/Cloneable");
    }

    public boolean isJioSerializable() {
        return name.equals("java/lang/Serializable");
    }

    public JJvmField getField(String name, String descriptor, boolean isStatic) {
        for (JJvmClass c = this; c != null; c = c.superClass) {
            for (JJvmField field: c.fields) {
                if (field.isStatic() == isStatic &&
                        field.name.equals(name) &&
                        field.descriptor.equals(descriptor)) {
                    return field;
                }
            }
        }
        return null;
    }

    public String getJavaName() {
        return name.replace("/", ".");
    }

    public boolean isPrimitive() {
        return primitiveTypes.containsKey(name);
    }

    public JJvmObject getRefVar(String fieldName, String fieldDescriptor) {
        JJvmField field = getField(fieldName, fieldDescriptor, true);
        return (JJvmObject) staticVars.getRef(field.slotId);
    }

    public void setRefVar(String fieldName, String fieldDescriptor, JJvmObject ref) {
        JJvmField field = getField(fieldName, fieldDescriptor, true);
        staticVars.setRef(field.slotId, ref);
    }

    public JJvmMethod getInstanceMethod(String name, String descriptor) {
        return getMethod(name, descriptor, false);
    }

    private JJvmMethod getMethod(String name, String descriptor, boolean isStatic) {
        for (JJvmClass c = this; c != null; c = c.superClass) {
            for (JJvmMethod method : c.methods) {
                if (method.isStatic() == isStatic
                        && method.name.equals(name)
                        && method.descriptor.equals(descriptor)) {
                    return method;
                }
            }
        }
        return null;
    }

    public String getSourceFile() {
        return sourceFile;
    }
}
