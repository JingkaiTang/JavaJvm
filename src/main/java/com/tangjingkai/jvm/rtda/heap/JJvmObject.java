package com.tangjingkai.jvm.rtda.heap;

/**
 * Created by totran on 11/19/16.
 */
public class JJvmObject implements Cloneable {
    JJvmClass cls;
    Object data;
    Object extra;

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    public JJvmObject(JJvmClass jjvmClass) {
        this.cls = jjvmClass;
        this.data = new JJvmSlots(jjvmClass.instanceSlotCount);
    }

    public Object getData() {
        return data;
    }

    public JJvmObject(JJvmClass jjvmClass, Object arr) {
        this.cls = jjvmClass;
        this.data = arr;
    }

    private JJvmObject() {}

    public JJvmClass getJJvmClass() {
        return cls;
    }

    public JJvmSlots getFields() {
        return (JJvmSlots) data;
    }

    public boolean isInstanceOf(JJvmClass jjvmClass) {
        return cls.isAssignableFrom(cls);
    }

    public byte[] getBytes() {
        return (byte[]) data;
    }

    public short[] getShorts() {
        return (short[]) data;
    }

    public int[] getInts() {
        return (int[]) data;
    }

    public long[] getLongs() {
        return (long[]) data;
    }

    public char[] getChars() {
        return (char[]) data;
    }

    public float[] getFloats() {
        return (float[]) data;
    }

    public double[] getDoubles() {
        return (double[]) data;
    }

    public JJvmObject[] getRefs() {
        return (JJvmObject[]) data;
    }

    public int getArrayLength() {
        if (data instanceof byte[]) {
            return getBytes().length;
        } else if (data instanceof short[]) {
            return getShorts().length;
        } else if (data instanceof int[]) {
            return getInts().length;
        } else if (data instanceof long[]) {
            return getLongs().length;
        } else if (data instanceof char[]) {
            return getChars().length;
        } else if (data instanceof float[]) {
            return getFloats().length;
        } else if (data instanceof double[]) {
            return getDoubles().length;
        } else if (data instanceof JJvmObject[]) {
            return getRefs().length;
        } else {
            throw new RuntimeException("Not array!");
        }
    }

    public void setRefVar(String name, String descriptor, JJvmObject ref) {
        JJvmField field = cls.getField(name, descriptor, false);
        ((JJvmSlots) data).setRef(field.slotId, ref);
    }

    public JJvmObject getRefVar(String name, String descriptor) {
        JJvmField field = cls.getField(name, descriptor, false);
        return (JJvmObject) ((JJvmSlots) data).getRef(field.slotId);
    }

    @Override
    public JJvmObject clone() {
        JJvmObject cl = new JJvmObject();
        cl.cls = cls;
        if (data instanceof byte[]) {
            cl.data = getBytes().clone();
        } else if (data instanceof short[]) {
            cl.data = getShorts().clone();
        } else if (data instanceof int[]) {
            cl.data = getInts().clone();
        } else if (data instanceof long[]) {
            cl.data = getLongs().clone();
        } else if (data instanceof char[]) {
            cl.data = getChars().clone();
        } else if (data instanceof float[]) {
            cl.data = getFloats().clone();
        } else if (data instanceof double[]) {
            cl.data = getDoubles().clone();
        } else if (data instanceof JJvmObject[]) {
            cl.data = getRefs().clone();
        } else {
            cl.data = ((JJvmSlots) data).clone();
        }

        return cl;
    }
}
