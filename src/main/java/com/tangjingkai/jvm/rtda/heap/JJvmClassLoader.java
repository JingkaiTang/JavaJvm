package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.classfile.ClassFile;
import com.tangjingkai.jvm.classpath.Classpath;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by totran on 11/19/16.
 */
public class JJvmClassLoader {
    Classpath classPath;
    Map<String, JJvmClass> classMap;

    public JJvmClassLoader(Classpath cp) {
        this.classPath = cp;
        this.classMap = new HashMap<>();
    }

    public JJvmClass loadClass(String name) {
        if (classMap.containsKey(name)) {
            return classMap.get(name);
        }
        if (name.charAt(0) == '[') {
            return loadArrayClass(name);
        }
        return loadNonArrayClass(name);
    }

    private JJvmClass loadArrayClass(String name) {
        JJvmClass cls = JJvmClass.loadArrayClass(this, name);
        classMap.put(name, cls);
        return cls;
    }

    private JJvmClass loadNonArrayClass(String name) {
        byte[] data = readClass(name);
        JJvmClass cls = defineClass(data);
        link(cls);
        System.out.println(String.format("Loaded class %s!", name));
        return cls;
    }

    private void link(JJvmClass cls) {
        verify(cls);
        prepare(cls);
    }

    private void prepare(JJvmClass cls) {
        calcInstanceFieldSlotIds(cls);
        calcStaticFieldSlotIds(cls);
        allocAndInitStaticVars(cls);
    }

    private void allocAndInitStaticVars(JJvmClass cls) {
        cls.staticVars = new JJvmSlots(cls.staticSlotCount);
        if (cls.fields == null) {
            return;
        }
        for (JJvmField field : cls.fields) {
            if (field.isStatic() && field.isFinal()) {
                initStaticFinalVar(cls, field);
            }
        }
    }

    private void initStaticFinalVar(JJvmClass cls, JJvmField field) {
        JJvmSlots vars = cls.staticVars;
        JJvmConstantPool cp = cls.constantPool;
        int cpIndex = field.constValueIndex;
        int slotId = field.slotId;

        if (cpIndex > 0) {
            switch (field.descriptor) {
                case "Z":
                case "B":
                case "C":
                case "S":
                case "I":
                    vars.setInt(slotId, (Integer) cp.getConstant(cpIndex));
                    break;
                case "J":
                    vars.setLong(slotId, (Long) cp.getConstant(cpIndex));
                    break;
                case "F":
                    vars.setFloat(slotId, (Float) cp.getConstant(cpIndex));
                    break;
                case "D":
                    vars.setDouble(slotId, (Double) cp.getConstant(cpIndex));
                    break;
                case "Ljava/lang/String;":
                    String str = (String) cp.getConstant(cpIndex);
                    JJvmObject jStr = InternedStrings.getString(cls.getClassLoader(), str);
                    vars.setRef(slotId, jStr);
                    break;
            }
        }
    }

    private void calcStaticFieldSlotIds(JJvmClass cls) {
        int slotId = 0;
        if (cls.fields != null) {
            for (JJvmField field : cls.fields) {
                if (field.isStatic()) {
                    field.slotId = slotId;
                    slotId++;
                    if (field.isLongOrDouble()) {
                        slotId++;
                    }
                }
            }
        }
        cls.staticSlotCount = slotId;
    }

    private void calcInstanceFieldSlotIds(JJvmClass cls) {
        int slotId = cls.superClass == null ? 0 : cls.superClass.instanceSlotCount;

        if (cls.fields != null) {
            for (JJvmField field : cls.fields) {
                if (!field.isStatic()) {
                    field.slotId = slotId;
                    slotId++;
                    if (field.isLongOrDouble()) {
                        slotId++;
                    }
                }
            }
        }

        cls.instanceSlotCount = slotId;
    }

    private void verify(JJvmClass cls) {
        // TODO: check class format
    }

    private JJvmClass defineClass(byte[] data) {
        JJvmClass cls = new JJvmClass(new ClassFile(data));
        cls.loader = this;
        resolveSuperClass(cls);
        resolveInterfaces(cls);
        classMap.put(cls.name, cls);
        return cls;
    }

    private void resolveSuperClass(JJvmClass cls) {
        if (!cls.name.equals("java/lang/Object")) {
            cls.superClass = cls.loader.loadClass(cls.superClassName);
        }
    }

    private void resolveInterfaces(JJvmClass cls) {
        int interfaceCount = cls.interfaceNames.length;
        if (interfaceCount > 0) {
            cls.interfaces = new JJvmClass[interfaceCount];
            for (int i = 0; i < interfaceCount; i++) {
                cls.interfaces[i] = cls.loader.loadClass(cls.interfaceNames[i]);
            }
        }
    }

    private byte[] readClass(String name) {
        byte[] data = classPath.readClass(name);
        if (data == null) {
            throw new RuntimeException(String.format("Class %s not found!", name));
        }
        return data;
    }
}
