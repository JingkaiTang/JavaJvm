package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/17/16.
 */
public class LocalVariableTableAttribute implements AttributeInfo {
    LocalVariableTableEntry[] localVariableTable;

    @Override
    public void readInfo(ClassReader reader) {
        int len = Short.toUnsignedInt(reader.readU2());
        localVariableTable = new LocalVariableTableEntry[len];
        for (int i = 0; i < len; i++) {
            localVariableTable[i] = LocalVariableTableEntry.readLocalVariableTableEntry(reader);
        }
    }

    /**
     * Created by totran on 11/17/16.
     */
    public static class LocalVariableTableEntry {
        short startPc;
        short length;
        short nameIndex;
        short descriptorIndex;
        short index;

        private LocalVariableTableEntry() {

        }

        public static LocalVariableTableEntry readLocalVariableTableEntry(ClassReader reader) {
            LocalVariableTableEntry entry = new LocalVariableTableEntry();
            entry.startPc = reader.readU2();
            entry.length = reader.readU2();
            entry.nameIndex = reader.readU2();
            entry.descriptorIndex = reader.readU2();
            entry.index = reader.readU2();
            return entry;
        }
    }
}
