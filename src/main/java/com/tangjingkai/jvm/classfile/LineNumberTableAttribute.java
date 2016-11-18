package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/17/16.
 */
public class LineNumberTableAttribute implements AttributeInfo {
    LineNumberTableEntry[] lineNumberTable;

    @Override
    public void readInfo(ClassReader reader) {
        int len = Short.toUnsignedInt(reader.readU2());
        lineNumberTable = new LineNumberTableEntry[len];
        for (int i = 0; i < len; i++) {
            lineNumberTable[i] = LineNumberTableEntry.readLineNumberTableEntry(reader);
        }
    }

    /**
     * Created by totran on 11/17/16.
     */
    public static class LineNumberTableEntry {
        short startPc;
        short lineNumber;

        private LineNumberTableEntry() {
        }

        public static LineNumberTableEntry readLineNumberTableEntry(ClassReader reader) {
            LineNumberTableEntry entry = new LineNumberTableEntry();
            entry.startPc = reader.readU2();
            entry.lineNumber = reader.readU2();
            return entry;
        }
    }
}
