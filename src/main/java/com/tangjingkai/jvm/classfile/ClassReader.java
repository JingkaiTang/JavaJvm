package com.tangjingkai.jvm.classfile;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by totran on 11/14/16.
 */
public class ClassReader {
    private ByteBuffer buf;

    public ClassReader(byte[] data) {
        this.buf = ByteBuffer.wrap(data)
                .order(ByteOrder.BIG_ENDIAN)
                .asReadOnlyBuffer();
    }

    public byte[] readBytes(int n) {
        byte[] data = new byte[n];
        buf.get(data);
        return data;
    }

    public byte readU1() {
        return buf.get();
    }

    public short readU2() {
        return buf.getShort();
    }

    public short[] readU2s() {
        int count = Short.toUnsignedInt(readU2());
        short[] u2s = new short[count];
        for (int i = 0; i < count; i++) {
            u2s[i] = readU2();
        }
        return u2s;
    }

    public int readU4() {
        return buf.getInt();
    }
}
