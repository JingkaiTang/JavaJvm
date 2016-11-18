package com.tangjingkai.jvm.instructions;

/**
 * Created by totran on 11/16/16.
 */
public class BytecodeReader {
    private byte[] code;
    private int pc;

    public int getPC() {
        return pc;
    }

    public BytecodeReader reset(byte[] code, int pc) {
        this.code = code;
        this.pc = pc;
        return this;
    }

    public byte readU1() {
        return code[pc++];
    }

    public int readU1I() {
        return Byte.toUnsignedInt(readU1());
    }

    public short readU2() {
        byte high = readU1();
        byte low = readU1();
        return (short) ((high << 8) | (low & 0xff));
    }

    public int readU2I() {
        return Short.toUnsignedInt(readU2());
    }

    public int readU4() {
        byte v3 = readU1();
        byte v2 = readU1();
        byte v1 = readU1();
        byte v0 = readU1();
        return (v3 << 24) | ((v2 & 0xff) << 16) | ((v1 & 0xff) << 8) | (v0 & 0xff);
    }

    public void skipPadding() {
        while (pc % 4 != 0) {
            readU1();
        }
    }

    public int[] readU4s(int n) {
        int[] data = new int[n];
        for (int i = 0; i < n; i++) {
            data[i] = readU4();
        }
        return data;
    }
}
