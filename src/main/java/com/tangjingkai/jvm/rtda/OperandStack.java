package com.tangjingkai.jvm.rtda;

/**
 * Created by totran on 11/16/16.
 */
public class OperandStack {
    Stack<Slot> stack;

    @Override
    public String toString() {
        return "OperandStack{" +
                "stack=" + stack +
                '}';
    }

    public Object getRefFromTop(int d) {
        Slot[] slots = new Slot[d];
        for (int i = 0; i < d; i++) {
            slots[i] = stack.pop();
        }
        Slot slot = stack.top();
        for (int i = d-1; i >= 0; i--) {
            stack.push(slots[i]);
        }
        return slot.ref;
    }

    public OperandStack(int maxStack) {
        stack = new Stack<>(maxStack);
    }

    public void pushSlot(Slot slot) {
        stack.push(slot);
    }

    public Slot popSlot() {
        return stack.pop();
    }

    public void pushInt(int val) {
        stack.push(new Slot().setInt(val));
    }

    public int popInt() {
        return stack.pop().getInt();
    }

    public void pushInt(byte val) {
        pushInt(Byte.toUnsignedInt(val));
    }

    public void pushInt(short val) {
        pushInt(Short.toUnsignedInt(val));
    }

    public void pushFloat(float val) {
        stack.push(new Slot().setFloat(val));
    }

    public float popFloat() {
        return stack.pop().getFloat();
    }

    public void pushLong(long val) {
        pushInt((int) val);
        pushInt((int) (val >>> 32));
    }

    public long popLong() {
        return (Integer.toUnsignedLong(stack.pop().getInt()) << 32) | Integer.toUnsignedLong(stack.pop().getInt());
    }

    public void pushDouble(double val) {
        pushLong(Double.doubleToLongBits(val));
    }

    public double popDouble() {
        return Double.longBitsToDouble(popLong());
    }

    public void pushRef(Object ref) {
        stack.push(new Slot().setRef(ref));
    }

    public Object popRef() {
        return stack.pop().getRef();
    }
}
