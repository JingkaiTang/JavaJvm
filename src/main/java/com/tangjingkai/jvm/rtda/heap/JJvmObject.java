package com.tangjingkai.jvm.rtda.heap;

/**
 * Created by totran on 11/19/16.
 */
public class JJvmObject {
    JJvmClass cls;
    Object data;

    public JJvmObject(JJvmClass jjvmClass) {
        this.cls = jjvmClass;
        this.data = new JJvmSlots(jjvmClass.instanceSlotCount);
    }

    public JJvmObject(JJvmClass jjvmClass, Object arr) {
        this.cls = jjvmClass;
        this.data = arr;
    }

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
}
