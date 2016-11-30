package com.tangjingkai.jvm.rtda;

import java.util.Arrays;

/**
 * Created by totran on 11/16/16.
 */
public class LocalVars {

    Slot[] slots;

    @Override
    public String toString() {
        return "LocalVars{" +
                "slots=" + Arrays.toString(slots) +
                '}';
    }

    public LocalVars(int maxLocals) {
        slots = new Slot[maxLocals];
        Arrays.setAll(slots, i -> new Slot());
    }

    public void setSlot(int index, Slot slot) {
        slots[index] = slot;
    }

    public void setInt(int index, int val) {
        slots[index].setInt(val);
    }

    public int getInt(int index) {
        return slots[index].getInt();
    }

    public void setFloat(int index, float val) {
        slots[index].setFloat(val);
    }

    public float getFloat(int index) {
        return slots[index].getFloat();
    }

    public void setLong(int index, long val) {
        slots[index].setInt((int) val);
        slots[index+1].setInt((int) (val >>> 32));
    }

    public long getLong(int index) {
        return (Integer.toUnsignedLong(slots[index+1].getInt()) << 32) | Integer.toUnsignedLong(slots[index].getInt());
    }

    public void setDouble(int index, double val) {
        setLong(index, Double.doubleToLongBits(val));
    }

    public double getDouble(int index) {
        return Double.longBitsToDouble(getLong(index));
    }

    public void setRef(int index, Object ref) {
        slots[index].setRef(ref);
    }

    public Object getRef(int index) {
        return slots[index].getRef();
    }

    public Object getThis() {
        return getRef(0);
    }
}
