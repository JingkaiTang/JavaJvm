package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/17/16.
 */
public class ExceptionsAttribute implements AttributeInfo {
    short[] exceptionIndexTable;

    @Override
    public void readInfo(ClassReader reader) {
        exceptionIndexTable = reader.readU2s();
    }

    public short[] getExceptionIndexTable() {
        return exceptionIndexTable;
    }

    /**
     * Created by totran on 11/17/16.
     */
    public static class ExceptionTableEntry {
        short startPc;
        short endPc;
        short handlerPc;

        public short getStartPc() {
            return startPc;
        }

        public short getEndPc() {
            return endPc;
        }

        public short getHandlerPc() {
            return handlerPc;
        }

        public short getCatchType() {
            return catchType;
        }

        short catchType;

        private ExceptionTableEntry() {

        }

        public static ExceptionTableEntry readExceptionTableEntry(ClassReader reader) {
            ExceptionTableEntry entry = new ExceptionTableEntry();
            entry.startPc = reader.readU2();
            entry.endPc = reader.readU2();
            entry.handlerPc = reader.readU2();
            entry.catchType = reader.readU2();
            return entry;
        }

        public static ExceptionTableEntry[] readExceptionTable(ClassReader reader) {
            int len = Short.toUnsignedInt(reader.readU2());
            ExceptionTableEntry[] exceptionTable = new ExceptionTableEntry[len];
            for (int i = 0; i < len; i++) {
                exceptionTable[i] = readExceptionTableEntry(reader);
            }
            return exceptionTable;
        }
    }
}
