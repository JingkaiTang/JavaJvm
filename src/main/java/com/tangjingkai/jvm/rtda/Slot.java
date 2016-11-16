package com.tangjingkai.jvm.rtda;

/**
 * Created by totran on 11/16/16.
 */
public class Slot {
    int num;
    Object ref;

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

    public Slot setInt(int val) {
        setNum(val);
        return this;
    }

    public int getInt() {
        return getNum();
    }

    public Slot setFloat(float val) {
        setInt(Float.floatToIntBits(val));
        return this;
    }

    public float getFloat() {
        return Float.intBitsToFloat(getInt());
    }
}
