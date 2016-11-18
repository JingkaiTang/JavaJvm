package com.tangjingkai.jvm.rtda;

/**
 * Created by totran on 11/16/16.
 */
public class Slot {
    int num;
    Object ref;

    @Override
    public String toString() {
        return "Slot{" +
                "num=" + num +
                ", ref=" + ref +
                '}';
    }

    public Slot() {

    }

    public Slot(Slot slot) {
        this.num = slot.getNum();
        this.ref = slot.getRef();
    }

    public int getNum() {
        return num;
    }

    public Slot setNum(int num) {
        this.num = num;
        return this;
    }

    public Object getRef() {
        return ref;
    }

    public Slot setRef(Object ref) {
        this.ref = ref;
        return this;
    }

    public int getInt() {
        return getNum();
    }

    public Slot setInt(int val) {
        setNum(val);
        return this;
    }

    public float getFloat() {
        return Float.intBitsToFloat(getInt());
    }

    public Slot setFloat(float val) {
        setInt(Float.floatToIntBits(val));
        return this;
    }
}
