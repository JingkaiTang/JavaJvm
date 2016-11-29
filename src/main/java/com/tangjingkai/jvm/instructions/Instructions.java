package com.tangjingkai.jvm.instructions;

import com.tangjingkai.jvm.rtda.*;
import com.tangjingkai.jvm.rtda.Thread;
import com.tangjingkai.jvm.rtda.heap.*;

import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by totran on 11/16/16.
 */

public class Instructions {
    private static InstructionGenerator[] igs = null;

    static {
        if (igs == null) {
            igs = new InstructionGenerator[256];
            Instructions insts = new Instructions();

            Method[] methods = Instructions.class.getDeclaredMethods();
            Arrays.stream(methods).forEach(method -> {
                try {
                    if (method.isAnnotationPresent(Bytecode.class)) {
                        Bytecode bytecode = method.getAnnotation(Bytecode.class);
                        igs[bytecode.value()] = (InstructionGenerator) method.invoke(insts, bytecode.value());
                    }

                    if (method.isAnnotationPresent(Bytecodes.class)) {
                        Bytecodes bytecodes = method.getAnnotation(Bytecodes.class);
                        for (Bytecode bytecode : bytecodes.value()) {
                            igs[bytecode.value()] = (InstructionGenerator) method.invoke(insts, bytecode.value());
                        }
                    }

                    if (method.isAnnotationPresent(BytecodeRange.class)) {
                        BytecodeRange bytecodeRange = method.getAnnotation(BytecodeRange.class);
                        for (int i = bytecodeRange.lower(); i <= bytecodeRange.upper(); i++) {
                            igs[i] = (InstructionGenerator) method.invoke(insts, i);
                        }
                    }

                    if (method.isAnnotationPresent(BytecodeRanges.class)) {
                        BytecodeRanges bytecodeRanges = method.getAnnotation(BytecodeRanges.class);
                        for (BytecodeRange bytecodeRange : bytecodeRanges.value()) {
                            for (int i = bytecodeRange.lower(); i <= bytecodeRange.upper(); i++) {
                                igs[i] = (InstructionGenerator) method.invoke(insts, i);
                            }
                        }
                    }
                } catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Unable to init Instructions!");
                }
            });

            for (int i = 0; i < 256; i++) {
                if (igs[i] == null) {
                    igs[i] = insts.unimplemented(i);
                }
            }
        }
    }

    public static Instruction decode(byte opcode) {
        return igs[Byte.toUnsignedInt(opcode)].gen();
    }

    /**
     * 0x00 nop
     */
    @Bytecode(0x00)
    private InstructionGenerator nop(int code) {
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        // pass
                    }
                };
            }
        };
    }

    /**
     * 0x01 aconst_null
     */
    @Bytecode(0x01)
    private InstructionGenerator aconst_null(int code) {
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        frame.getOperandStack().pushRef(null);
                    }
                };
            }
        };
    }

    private enum TypeLeader {
        I,
        L,
        F,
        D,
        A,
        B,
        C,
        S,
    }

    private static class TypeLeaderSelector {
        TypeLeader typeLeader = null;

        private TypeLeaderSelector(Builder builder, int code) {
            builder.data.forEach((typeLeader, codes) -> {
                Arrays.stream(codes).forEach(e -> {
                    if (code == e) {
                        this.typeLeader = typeLeader;
                    }
                });
            });

            if (this.typeLeader == null) {
                throw new RuntimeException(String.format("Unsupported code %02x in tls!", code));
            }
        }

        static class Builder {
            Map<TypeLeader, int[]> data = new TreeMap<>();

            Builder set(TypeLeader typeLeader, int... codes) {
                data.put(typeLeader, codes);
                return this;
            }

            TypeLeaderSelector build(int code) {
                return new TypeLeaderSelector(this, code);
            }
        }
    }

    private static class TypeLeaderAndBaseSelector {
        TypeLeader typeLeader = null;
        int base;

        private TypeLeaderAndBaseSelector(Builder builder, int code) {
            builder.data.forEach((typeLeader, selector) -> {
                if (code >= selector.lower && code <= selector.upper) {
                    this.typeLeader = typeLeader;
                    this.base = selector.base;
                }
            });

            if (this.typeLeader == null) {
                throw new RuntimeException(String.format("Unsupported code %02x in tlbs!", code));
            }
        }

        static class Builder {
            class Selector {
                int lower;
                int upper;
                int base;

                public Selector(int lower, int upper, int base) {
                    this.lower = lower;
                    this.upper = upper;
                    this.base = base;
                }
            }

            Map<TypeLeader, Selector> data = new TreeMap<>();

            Builder set(TypeLeader typeLeader, int lower, int upper, int base) {
                data.put(typeLeader, new Selector(lower, upper, base));
                return this;
            }

            Builder set(TypeLeader typeLeader, int lower, int upper) {
                return set(typeLeader, lower, upper, lower);
            }

            TypeLeaderAndBaseSelector build(int code) {
                return new TypeLeaderAndBaseSelector(this, code);
            }
        }
    }

    /**
     * 0x02 iconst_m1
     * 0x03 iconst_0
     * 0x04 iconst_1
     * 0x05 iconst_2
     * 0x06 iconst_3
     * 0x07 iconst_4
     * 0x08 iconst_5
     * 0x09 lconst_0
     * 0x0a lconst_1
     * 0x0b fconst_0
     * 0x0c fconst_1
     * 0x0d fconst_2
     * 0x0e dconst_0
     * 0x0f dconst_1
     */
    @BytecodeRange(lower = 0x02, upper = 0x0f)
    private InstructionGenerator tconst_n(int code) {
        TypeLeaderAndBaseSelector tlbs = new TypeLeaderAndBaseSelector.Builder()
                .set(TypeLeader.I, 0x02, 0x08, 0x03)
                .set(TypeLeader.L, 0x09, 0x0a)
                .set(TypeLeader.F, 0x0b, 0x0d)
                .set(TypeLeader.D, 0x0e, 0x0f)
                .build(code);
        TypeLeader tl = tlbs.typeLeader;
        int x_const = code - tlbs.base;
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        switch (tl) {
                            case I:
                                stack.pushInt(x_const);
                                break;
                            case L:
                                stack.pushLong(x_const);
                                break;
                            case F:
                                stack.pushFloat(x_const);
                                break;
                            case D:
                                stack.pushDouble(x_const);
                                break;
                            default:
                                throw new RuntimeException(String.format("Unsupported TypeLeader %s in tconst_n", tl));
                        }
                    }
                };
            }
        };
    }

    /**
     * 0x10 bipush
     * 0x11 sipush
     */
    @BytecodeRange(lower = 0x10, upper = 0x11)
    private InstructionGenerator tipush(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new Instruction() {
                    int val;

                    @Override
                    public void fetchOperands(BytecodeReader reader) {
                        switch (code) {
                            case 0x10:
                                val = reader.readU1();
                                break;
                            case 0x11:
                                val = reader.readU2();
                                break;
                            default:
                                throw new RuntimeException(String.format("Unsupported bytecode %02x in tipush!", code));
                        }
                    }

                    @Override
                    public void execute(Frame frame) {
                        frame.getOperandStack().pushInt(val);
                    }
                };
            }
        };
    }

    /**
     * 0x12 ldc
     * 0x13 ldc_w
     */
    @BytecodeRange(lower = 0x12, upper = 0x13)
    private InstructionGenerator ldc(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                IndexInstruction i = new IndexInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        JJvmConstantPool cp = frame.getMethod().getJJvmClass().getConstantPool();
                        Object c = cp.getConstant(index);
                        if (c instanceof Integer) {
                            stack.pushInt((Integer) c);
                        } else if (c instanceof Float) {
                            stack.pushFloat((Float) c);
                            //} else if (c instanceof String) {
                            // TODO: string
                            //} else if (c instanceof ClassRef) {
                            // TODO: ClassRef
                        } else {
                            throw new RuntimeException("Unimplemented ldc!");
                        }
                    }
                };
                if (code == 0x13) {
                    i.setWide();
                }
                return i;
            }
        };
    }

    /**
     * 0x14 ldc2_w
     */
    @Bytecode(0x14)
    private InstructionGenerator ldc2_w(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new IndexInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        JJvmConstantPool cp = frame.getMethod().getJJvmClass().getConstantPool();
                        Object c = cp.getConstant(index);
                        if (c instanceof Long) {
                            stack.pushLong((Long) c);
                        } else if (c instanceof Double) {
                            stack.pushDouble((Double) c);
                        } else {
                            throw new RuntimeException("java.lang.ClassFormatError");
                        }
                    }
                }.setWide();
            }
        };
    }


    /**
     * 0x15 iload
     * 0x16 lload
     * 0x17 fload
     * 0x18 dload
     * 0x19 aload
     */
    @BytecodeRange(lower = 0x15, upper = 0x19)
    private InstructionGenerator tload(int code) {
        TypeLeader tl = new TypeLeaderSelector.Builder()
                .set(TypeLeader.I, 0x15)
                .set(TypeLeader.L, 0x16)
                .set(TypeLeader.F, 0x17)
                .set(TypeLeader.D, 0x18)
                .set(TypeLeader.A, 0x19)
                .build(code)
                .typeLeader;
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new IndexInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        tload_execute(tl, frame, index);
                    }
                };
            }
        };
    }

    private void tload_execute(TypeLeader tl, Frame frame, int index) {
        LocalVars lv = frame.getLocalVars();
        OperandStack st = frame.getOperandStack();
        switch (tl) {
            case I:
                st.pushInt(lv.getInt(index));
                break;
            case L:
                st.pushLong(lv.getLong(index));
                break;
            case F:
                st.pushFloat(lv.getFloat(index));
                break;
            case D:
                st.pushDouble(lv.getDouble(index));
                break;
            case A:
                st.pushRef(lv.getRef(index));
                break;
            default:
                throw new RuntimeException(String.format("Unsupported TypeLeader %s in tload!", tl));
        }
    }

    /**
     * 0x1a iload_0
     * 0x1b iload_1
     * 0x1c iload_2
     * 0x1d iload_3
     * 0x1e lload_0
     * 0x1f lload_1
     * 0x20 lload_2
     * 0x21 lload_3
     * 0x22 fload_0
     * 0x23 fload_1
     * 0x24 fload_2
     * 0x25 fload_3
     * 0x26 dload_0
     * 0x27 dload_1
     * 0x28 dload_2
     * 0x29 dload_3
     * 0x2a aload_0
     * 0x2b aload_1
     * 0x2c aload_2
     * 0x2d aload_3
     */
    @BytecodeRange(lower = 0x1a, upper = 0x2d)
    private InstructionGenerator tload_n(int code) {
        TypeLeaderAndBaseSelector tlbs = new TypeLeaderAndBaseSelector.Builder()
                .set(TypeLeader.I, 0x1a, 0x1d)
                .set(TypeLeader.L, 0x1e, 0x21)
                .set(TypeLeader.F, 0x22, 0x25)
                .set(TypeLeader.D, 0x26, 0x29)
                .set(TypeLeader.A, 0x2a, 0x2d)
                .build(code);
        TypeLeader tl = tlbs.typeLeader;
        int index = code - tlbs.base;
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        tload_execute(tl, frame, index);
                    }
                };
            }
        };
    }

    /**
     * 0x2e iaload
     * 0x2f laload
     * 0x30 faload
     * 0x31 daload
     * 0x32 aaload
     * 0x33 baload
     * 0x34 caload
     * 0x35 saload
     */
    @BytecodeRange(lower = 0x2e, upper = 0x35)
    private InstructionGenerator taload(int code) {
        TypeLeader tl = new TypeLeaderSelector.Builder()
                .set(TypeLeader.I, 0x2e)
                .set(TypeLeader.L, 0x2f)
                .set(TypeLeader.F, 0x30)
                .set(TypeLeader.D, 0x31)
                .set(TypeLeader.A, 0x32)
                .set(TypeLeader.B, 0x33)
                .set(TypeLeader.C, 0x34)
                .set(TypeLeader.S, 0x35)
                .build(code)
                .typeLeader;
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        int index = stack.popInt();
                        JJvmObject arr = (JJvmObject) stack.popRef();
                        if (arr == null) {
                            throw new NullPointerException();
                        }
                        if (index < 0 || index >= arr.getArrayLength()) {
                            throw new ArrayIndexOutOfBoundsException();
                        }
                        switch (tl) {
                            case I:
                                stack.pushInt(arr.getInts()[index]);
                                break;
                            case L:
                                stack.pushLong(arr.getLongs()[index]);
                                break;
                            case F:
                                stack.pushFloat(arr.getFloats()[index]);
                                break;
                            case D:
                                stack.pushDouble(arr.getDoubles()[index]);
                                break;
                            case A:
                                stack.pushRef(arr.getRefs()[index]);
                                break;
                            case B:
                                stack.pushInt(arr.getBytes()[index]);
                                break;
                            case C:
                                stack.pushInt(arr.getChars()[index]);
                                break;
                            case S:
                                stack.pushInt(arr.getShorts()[index]);
                                break;
                        }
                    }
                };
            }
        };
    }

    /**
     * 0x36 istore
     * 0x37 lstore
     * 0x38 fstore
     * 0x39 dstore
     * 0x3a astore
     */
    @BytecodeRange(lower = 0x36, upper = 0x3a)
    private InstructionGenerator tstore(int code) {
        TypeLeader tl = new TypeLeaderSelector.Builder()
                .set(TypeLeader.I, 0x36)
                .set(TypeLeader.L, 0x37)
                .set(TypeLeader.F, 0x38)
                .set(TypeLeader.D, 0x39)
                .set(TypeLeader.A, 0x3a)
                .build(code)
                .typeLeader;
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new IndexInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        tstore_execute(tl, frame, index);
                    }
                };
            }
        };
    }

    private void tstore_execute(TypeLeader tl, Frame frame, int index) {
        OperandStack st = frame.getOperandStack();
        LocalVars lv = frame.getLocalVars();
        switch (tl) {
            case I:
                lv.setInt(index, st.popInt());
                break;
            case L:
                lv.setLong(index, st.popLong());
                break;
            case F:
                lv.setFloat(index, st.popFloat());
                break;
            case D:
                lv.setDouble(index, st.popDouble());
                break;
            case A:
                lv.setRef(index, st.popRef());
                break;
            default:
                throw new RuntimeException(String.format("Unsupported TypeLeader %s in tstore!", tl));
        }
    }

    /**
     * 0x3b istore_0
     * 0x3c istore_1
     * 0x3d istore_2
     * 0x3e istore_3
     * 0x3f lstore_0
     * 0x40 lstore_1
     * 0x41 lstore_2
     * 0x42 lstore_3
     * 0x43 fstore_0
     * 0x44 fstore_1
     * 0x45 fstore_2
     * 0x46 fstore_3
     * 0x47 dstore_0
     * 0x48 dstore_1
     * 0x49 dstore_2
     * 0x4a dstore_3
     * 0x4b astore_0
     * 0x4c astore_1
     * 0x4d astore_2
     * 0x4e astore_3
     */
    @BytecodeRange(lower = 0x3b, upper = 0x4e)
    private InstructionGenerator tstore_n(int code) {
        TypeLeaderAndBaseSelector tlbs = new TypeLeaderAndBaseSelector.Builder()
                .set(TypeLeader.I, 0x3b, 0x3e)
                .set(TypeLeader.L, 0x3f, 0x42)
                .set(TypeLeader.F, 0x43, 0x46)
                .set(TypeLeader.D, 0x47, 0x4a)
                .set(TypeLeader.A, 0x4b, 0x4e)
                .build(code);
        TypeLeader tl = tlbs.typeLeader;
        int index = code - tlbs.base;
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        tstore_execute(tl, frame, index);
                    }
                };
            }
        };
    }

    /**
     * 0x4f iastore
     * 0x50 lastore
     * 0x51 fastore
     * 0x52 dastore
     * 0x53 aastore
     * 0x54 bastore
     * 0x55 castore
     * 0x56 sastore
     */
    @BytecodeRange(lower = 0x4f, upper = 0x56)
    private InstructionGenerator tastore(int code) {
        TypeLeader tl = new TypeLeaderSelector.Builder()
                .set(TypeLeader.I, 0x4f)
                .set(TypeLeader.L, 0x50)
                .set(TypeLeader.F, 0x51)
                .set(TypeLeader.D, 0x52)
                .set(TypeLeader.A, 0x53)
                .set(TypeLeader.B, 0x54)
                .set(TypeLeader.C, 0x55)
                .set(TypeLeader.S, 0x56)
                .build(code)
                .typeLeader;
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        int i_val = 0;
                        long l_val = 0;
                        float f_val = 0;
                        double d_val = 0;
                        JJvmObject a_val = null;
                        byte b_val = 0;
                        char c_val = 0;
                        short s_val = 0;
                        switch (tl) {
                            case I:
                                i_val = stack.popInt();
                                break;
                            case L:
                                l_val = stack.popLong();
                                break;
                            case F:
                                f_val = stack.popFloat();
                                break;
                            case D:
                                d_val = stack.popDouble();
                                break;
                            case A:
                                a_val = (JJvmObject) stack.popRef();
                                break;
                            case B:
                                b_val = (byte) stack.popInt();
                                break;
                            case C:
                                c_val = (char) stack.popInt();
                                break;
                            case S:
                                s_val = (short) stack.popInt();
                                break;
                        }
                        int index = stack.popInt();
                        JJvmObject arr = (JJvmObject) stack.popRef();
                        if (arr == null) {
                            throw new NullPointerException();
                        }
                        if (index < 0 || index >= arr.getArrayLength()) {
                            throw new ArrayIndexOutOfBoundsException();
                        }
                        switch (tl) {
                            case I:
                                arr.getInts()[index] = i_val;
                                break;
                            case L:
                                arr.getLongs()[index] = l_val;
                                break;
                            case F:
                                arr.getFloats()[index] = f_val;
                                break;
                            case D:
                                arr.getDoubles()[index] = d_val;
                                break;
                            case A:
                                arr.getRefs()[index] = a_val;
                                break;
                            case B:
                                arr.getBytes()[index] = b_val;
                                break;
                            case C:
                                arr.getChars()[index] = c_val;
                                break;
                            case S:
                                arr.getShorts()[index] = s_val;
                                break;
                        }
                    }
                };
            }
        };
    }


    /**
     * 0x57 pop
     * 0x58 pop2
     */
    @BytecodeRange(lower = 0x57, upper = 0x58)
    private InstructionGenerator popn(int code) {
        boolean repeat = code == 0x58;
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        frame.getOperandStack().popSlot();
                        if (repeat) {
                            frame.getOperandStack().popSlot();
                        }
                    }
                };
            }
        };
    }

    /**
     * TODO:
     * check the instructions: dupn_xm
     * 0x59 dup
     * 0x5a dup_x1
     * 0x5b dup_x2
     * 0x5c dup2
     * 0x5d dup2_x1
     * 0x5e dup2_x2
     */
    @BytecodeRange(lower = 0x59, upper = 0x5e)
    private InstructionGenerator dupn_xm(int code) {
        int n = (code < 0x5c) ? 1 : 2;
        int m = (code - 0x59) % 3 + n;
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        Slot[] slots = new Slot[m];
                        for (int i = 0; i < m; i++) {
                            slots[i] = stack.popSlot();
                        }
                        for (int i = n - 1; i >= 0; i--) {
                            stack.pushSlot(new Slot(slots[i]));
                        }
                        for (int i = m - 1; i >= 0; i--) {
                            stack.pushSlot(slots[i]);
                        }
                    }
                };
            }
        };
    }

    /**
     * 0x5f swap
     */
    @Bytecode(0x5f)
    private InstructionGenerator swap(int code) {
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        Slot slot1 = stack.popSlot();
                        Slot slot2 = stack.popSlot();
                        stack.pushSlot(slot1);
                        stack.pushSlot(slot2);
                    }
                };
            }
        };
    }


    /**
     * 0x60 iadd
     * 0x61 ladd
     * 0x62 fadd
     * 0x63 dadd
     * 0x64 isub
     * 0x65 lsub
     * 0x66 fsub
     * 0x67 dsub
     * 0x68 imul
     * 0x69 lmul
     * 0x6a fmul
     * 0x6b dmul
     * 0x6c idiv
     * 0x6d ldiv
     * 0x6e fdiv
     * 0x6f ddiv
     * 0x70 irem
     * 0x71 lrem
     * 0x72 frem
     * 0x73 drem
     * <p>
     * 0x78 ishl
     * 0x79 lshl
     * 0x7a ishr
     * 0x7b lshr
     * 0x7c iushr
     * 0x7d lushr
     * 0x7e iand
     * 0x7f land
     * 0x80 ior
     * 0x81 lor
     * 0x82 ixor
     * 0x83 lxor
     */
    @BytecodeRange(lower = 0x60, upper = 0x73)
    @BytecodeRange(lower = 0x78, upper = 0x83)
    private InstructionGenerator top(int code) {
        int tlCode = code <= 0x73 ? code % 4 : code % 2;
        int opCode = code <= 0x73 ? (code - 0x60) / 4 : 5 + (code - 0x78) / 2;
        TypeLeader tl = Arrays.asList(
                TypeLeader.I,
                TypeLeader.L,
                TypeLeader.F,
                TypeLeader.D
        ).get(tlCode);
        Operator op = Arrays.asList(
                Operator.ADD,
                Operator.SUB,
                Operator.MUL,
                Operator.DIV,
                Operator.REM,
                Operator.SHL,
                Operator.SHR,
                Operator.USHR,
                Operator.AND,
                Operator.OR,
                Operator.XOR
        ).get(opCode);
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        switch (tl) {
                            case I:
                                int i2 = stack.popInt();
                                int i1 = stack.popInt();
                                stack.pushInt(iop(op, i1, i2));
                                break;
                            case L:
                                long l2 = stack.popLong();
                                long l1 = stack.popLong();
                                stack.pushLong(lop(op, l1, l2));
                                break;
                            case F:
                                float f2 = stack.popFloat();
                                float f1 = stack.popFloat();
                                stack.pushFloat(fop(op, f1, f2));
                                break;
                            case D:
                                double d2 = stack.popDouble();
                                double d1 = stack.popDouble();
                                stack.pushDouble(dop(op, d1, d2));
                                break;
                            default:
                                throw new RuntimeException("Unsupported TypeLeader!");
                        }
                    }
                };
            }
        };
    }

    private int iop(Operator op, int v1, int v2) {
        switch (op) {
            case ADD:
                return v1 + v2;
            case SUB:
                return v1 - v2;
            case MUL:
                return v1 * v2;
            case DIV:
                if (v2 == 0) {
                    throw new RuntimeException("java.lang.ArithmeticException: / by zero");
                }
                return v1 / v2;
            case REM:
                if (v2 == 0) {
                    throw new RuntimeException("java.lang.ArithmeticException: / by zero");
                }
                return v1 % v2;
            case SHL:
                return v1 << (v2 & 0x1f);
            case SHR:
                return v1 >> (v2 & 0x1f);
            case USHR:
                return v1 >>> (v2 & 0x1f);
            case AND:
                return v1 & v2;
            case OR:
                return v1 | v2;
            case XOR:
                return v1 ^ v2;
            default:
                throw new RuntimeException("Unsupported Operator!");
        }
    }

    private long lop(Operator op, long v1, long v2) {
        switch (op) {
            case ADD:
                return v1 + v2;
            case SUB:
                return v1 - v2;
            case MUL:
                return v1 * v2;
            case DIV:
                if (v2 == 0) {
                    throw new RuntimeException("java.lang.ArithmeticException: / by zero");
                }
                return v1 / v2;
            case REM:
                if (v2 == 0) {
                    throw new RuntimeException("java.lang.ArithmeticException: / by zero");
                }
                return v1 % v2;
            case SHL:
                return v1 << (v2 & 0x1f);
            case SHR:
                return v1 >> (v2 & 0x1f);
            case USHR:
                return v1 >>> (v2 & 0x1f);
            case AND:
                return v1 & v2;
            case OR:
                return v1 | v2;
            case XOR:
                return v1 ^ v2;
            default:
                throw new RuntimeException("Unsupported Operator!");
        }
    }

    private float fop(Operator op, float v1, float v2) {
        switch (op) {
            case ADD:
                return v1 + v2;
            case SUB:
                return v1 - v2;
            case MUL:
                return v1 * v2;
            case DIV:
                return v1 / v2;
            case REM:
                return v1 % v2;
            default:
                throw new RuntimeException("Unsupported Operator!");
        }
    }

    private double dop(Operator op, double v1, double v2) {
        switch (op) {
            case ADD:
                return v1 + v2;
            case SUB:
                return v1 - v2;
            case MUL:
                return v1 * v2;
            case DIV:
                return v1 / v2;
            case REM:
                return v1 % v2;
            default:
                throw new RuntimeException("Unsupported Operator!");
        }
    }

    private enum Operator {
        ADD,
        SUB,
        MUL,
        DIV,
        REM,
        NEG,
        SHL,
        SHR,
        USHR,
        AND,
        OR,
        XOR,
    }

    /**
     * 0x74 ineg
     * 0x75 lneg
     * 0x76 fneg
     * 0x77 dneg
     */
    @BytecodeRange(lower = 0x74, upper = 0x77)
    private InstructionGenerator tneg(int code) {
        TypeLeader tl = Arrays.asList(
                TypeLeader.I,
                TypeLeader.L,
                TypeLeader.F,
                TypeLeader.D
        ).get(code - 0x74);
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        switch (tl) {
                            case I:
                                stack.pushInt(-stack.popInt());
                                break;
                            case L:
                                stack.pushLong(-stack.popLong());
                                break;
                            case F:
                                stack.pushFloat(-stack.popFloat());
                                break;
                            case D:
                                stack.pushDouble(-stack.popDouble());
                                break;
                            default:
                                throw new RuntimeException("Unsupported TypeLeader!");
                        }
                    }
                };
            }
        };
    }


    /**
     * 0x84 iinc
     */
    @Bytecode(0x84)
    private InstructionGenerator iinc(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new IndexInstruction() {
                    int constant;

                    @Override
                    public void fetchOperands(BytecodeReader reader) {
                        super.fetchOperands(reader);
                        if (wide) {
                            constant = reader.readU2();
                        } else {
                            constant = reader.readU1();
                        }
                    }

                    @Override
                    public void execute(Frame frame) {
                        LocalVars localVars = frame.getLocalVars();
                        int val = localVars.getInt(index);
                        val += constant;
                        localVars.setInt(index, val);
                    }
                };
            }
        };
    }

    /**
     * 0x85 i2l
     * 0x86 i2f
     * 0x87 i2d
     * 0x88 l2i
     * 0x89 l2f
     * 0x8a l2d
     * 0x8b f2i
     * 0x8c f2l
     * 0x8d f2d
     * 0x8e d2i
     * 0x8f d2l
     * 0x90 d2f
     * 0x91 i2b
     * 0x92 i2c
     * 0x93 i2s
     */
    @BytecodeRange(lower = 0x85, upper = 0x93)
    private InstructionGenerator tx2ty(int code) {
        return new CachedInstructionGenerator(code) {
            TypeLeader tx = Arrays.asList(
                    TypeLeader.I,
                    TypeLeader.L,
                    TypeLeader.F,
                    TypeLeader.D,
                    TypeLeader.I
            ).get((code - 0x85) / 3);
            TypeLeader ty = new TypeLeaderSelector.Builder()
                    .set(TypeLeader.I, 0x88, 0x8b, 0x8e)
                    .set(TypeLeader.L, 0x85, 0x8c, 0x8f)
                    .set(TypeLeader.F, 0x86, 0x89, 0x90)
                    .set(TypeLeader.D, 0x87, 0x8a, 0x8d)
                    .set(TypeLeader.B, 0x91)
                    .set(TypeLeader.C, 0x92)
                    .set(TypeLeader.S, 0x93)
                    .build(code)
                    .typeLeader;

            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        Number val;
                        switch (tx) {
                            case I:
                                val = stack.popInt();
                                break;
                            case L:
                                val = stack.popDouble();
                                break;
                            case F:
                                val = stack.popFloat();
                                break;
                            case D:
                                val = stack.popDouble();
                                break;
                            default:
                                throw new RuntimeException("Unsupported TypeLeader!");
                        }
                        switch (tx) {
                            case I:
                                stack.pushInt(val.intValue());
                                break;
                            case L:
                                stack.pushLong(val.longValue());
                                break;
                            case F:
                                stack.pushFloat(val.floatValue());
                                break;
                            case D:
                                stack.pushDouble(val.doubleValue());
                                break;
                            case B:
                                stack.pushInt(val.byteValue());
                                break;
                            case C:
                                stack.pushInt((char) val.intValue());
                                break;
                            case S:
                                stack.pushInt(val.shortValue());
                                break;
                            default:
                                throw new RuntimeException("Unsupported TypeLeader!");
                        }
                    }
                };
            }
        };
    }

    /**
     * 0x94 lcmp
     */
    @Bytecode(0x94)
    private InstructionGenerator lcmp(int code) {
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        long v2 = stack.popLong();
                        long v1 = stack.popLong();
                        stack.pushInt(Long.compare(v1, v2));
                    }
                };
            }
        };
    }

    /**
     * 0x95 fcmpl
     * 0x96 fcmpg
     */
    @BytecodeRange(lower = 0x95, upper = 0x96)
    private InstructionGenerator fcmpop(int code) {
        boolean gFlag = code == 0x96;
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        float v2 = stack.popFloat();
                        float v1 = stack.popFloat();
                        if (v1 > v2) {
                            stack.pushInt(1);
                        } else if (v1 < v2) {
                            stack.pushInt(-1);
                        } else {
                            int i1 = Float.floatToIntBits(v1);
                            int i2 = Float.floatToIntBits(v2);
                            if (i1 == i2) {
                                stack.pushInt(0);
                            } else if (gFlag) {
                                stack.pushInt(1);
                            } else {
                                stack.pushInt(-1);
                            }
                        }
                    }
                };
            }
        };
    }

    /**
     * 0x97 dcmpl
     * 0x98 dcmpg
     */
    @BytecodeRange(lower = 0x97, upper = 0x98)
    private InstructionGenerator dcmpop(int code) {
        boolean gFlag = code == 0x98;
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        double v2 = stack.popDouble();
                        double v1 = stack.popDouble();
                        if (v1 > v2) {
                            stack.pushInt(1);
                        } else if (v1 < v2) {
                            stack.pushInt(-1);
                        } else {
                            long l1 = Double.doubleToLongBits(v1);
                            long l2 = Double.doubleToLongBits(v2);
                            if (l1 == l2) {
                                stack.pushInt(0);
                            } else if (gFlag) {
                                stack.pushInt(1);
                            } else {
                                stack.pushInt(-1);
                            }
                        }
                    }
                };
            }
        };
    }

    private enum Condition {
        EQ,
        NE,
        LT,
        LE,
        GT,
        GE,
    }

    /**
     * 0x99 ifeq
     * 0x9a ifne
     * 0x9b iflt
     * 0x9c ifge
     * 0x9d ifgt
     * 0x9e ifle
     */
    @BytecodeRange(lower = 0x99, upper = 0x9e)
    private InstructionGenerator ifcond(int code) {
        Condition cond = Arrays.asList(
                Condition.EQ,
                Condition.NE,
                Condition.LT,
                Condition.GE,
                Condition.GT,
                Condition.LE
        ).get(code - 0x99);
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new BranchInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        int val = frame.getOperandStack().popInt();
                        boolean flag = false;
                        switch (cond) {
                            case EQ:
                                flag = val == 0;
                                break;
                            case NE:
                                flag = val != 0;
                                break;
                            case LT:
                                flag = val < 0;
                                break;
                            case LE:
                                flag = val <= 0;
                                break;
                            case GT:
                                flag = val > 0;
                                break;
                            case GE:
                                flag = val >= 0;
                                break;
                            default:
                                throw new RuntimeException("Unsupported condition operator!");
                        }
                        if (flag) {
                            branch(frame, offset);
                        }
                    }
                };
            }
        };
    }

    /**
     * 0x9f if_icmpeq
     * 0xa0 if_icmpne
     * 0xa1 if_icmplt
     * 0xa2 if_icmpge
     * 0xa3 if_icmpgt
     * 0xa4 if_icmple
     */
    @BytecodeRange(lower = 0x9f, upper = 0xa4)
    private InstructionGenerator if_icmpcond(int code) {
        Condition cond = Arrays.asList(
                Condition.EQ,
                Condition.NE,
                Condition.LT,
                Condition.GE,
                Condition.GT,
                Condition.LE
        ).get(code - 0x9f);
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new BranchInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        int v2 = stack.popInt();
                        int v1 = stack.popInt();
                        boolean flag = false;
                        switch (cond) {
                            case EQ:
                                flag = v1 == v2;
                                break;
                            case NE:
                                flag = v1 != v2;
                                break;
                            case LT:
                                flag = v1 < v2;
                                break;
                            case LE:
                                flag = v1 <= v2;
                                break;
                            case GT:
                                flag = v1 > v2;
                                break;
                            case GE:
                                flag = v1 >= v2;
                                break;
                            default:
                                throw new RuntimeException("Unsupported condition operator!");
                        }
                        if (flag) {
                            branch(frame, offset);
                        }
                    }
                };
            }
        };
    }

    /**
     * 0xa5 if_acmpeq
     * 0xa6 if_acmpne
     */
    @BytecodeRange(lower = 0xa5, upper = 0xa6)
    private InstructionGenerator if_acmpcond(int code) {
        Condition cond = code == 0xa5 ? Condition.EQ : Condition.NE;
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new BranchInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        Object ref2 = stack.popRef();
                        Object ref1 = stack.popRef();
                        boolean flag = false;
                        switch (cond) {
                            case EQ:
                                flag = ref1 == ref2;
                                break;
                            case NE:
                                flag = ref1 != ref2;
                                break;
                            default:
                                throw new RuntimeException("Unsupported condition operator!");
                        }
                        if (flag) {
                            branch(frame, offset);
                        }
                    }
                };
            }
        };
    }

    private static void branch(Frame frame, int offset) {
        frame.setNextPC(frame.getThread().getPC() + offset);
    }


    /**
     * 0xa7 goto
     */
    @Bytecode(0xa7)
    private InstructionGenerator jump(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new BranchInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        branch(frame, offset);
                    }
                };
            }
        };
    }

    /*
     * TODO:
     * 0xa8 jsr
     * 0xa9 ret
     */

    /**
     * 0xaa tableswitch
     */
    @Bytecode(0xaa)
    private InstructionGenerator table_switch(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new Instruction() {
                    int defaultOffset;
                    int low;
                    int high;
                    int[] jumpOffsets;

                    @Override
                    public void fetchOperands(BytecodeReader reader) {
                        reader.skipPadding();
                        defaultOffset = reader.readU4();
                        low = reader.readU4();
                        high = reader.readU4();
                        int jumpOffsetsCount = high - low + 1;
                        jumpOffsets = reader.readU4s(jumpOffsetsCount);
                    }

                    @Override
                    public void execute(Frame frame) {
                        int index = frame.getOperandStack().popInt();
                        int offset = (index >= low && index <= high) ?
                                jumpOffsets[index - low] : defaultOffset;
                        branch(frame, offset);
                    }
                };
            }
        };
    }

    /**
     * 0xab lookupswitch
     */
    @Bytecode(0xab)
    private InstructionGenerator lookup_switch(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new Instruction() {
                    int defaultOffset;
                    int npairs;
                    int[] matchOffsets;

                    @Override
                    public void fetchOperands(BytecodeReader reader) {
                        reader.skipPadding();
                        defaultOffset = reader.readU4();
                        npairs = reader.readU4();
                        matchOffsets = reader.readU4s(npairs * 2);
                    }

                    @Override
                    public void execute(Frame frame) {
                        int key = frame.getOperandStack().popInt();
                        for (int i = 0; i < npairs * 2; i += 2) {
                            if (key == matchOffsets[i]) {
                                branch(frame, matchOffsets[i + 1]);
                                return;
                            }
                        }
                        branch(frame, defaultOffset);
                    }
                };
            }
        };
    }

    /**
     * 0xac ireturn
     * 0xad lreturn
     * 0xae freturn
     * 0xaf dreturn
     * 0xb0 areturn
     * 0xb1 return
     */
    @BytecodeRange(lower = 0xac, upper = 0xb1)
    private InstructionGenerator treturn(int code) {
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        if (code == 0xb1) {
                            frame.getThread().popFrame();
                            return;
                        }
                        OperandStack ret = frame.getThread().popFrame().getOperandStack();
                        OperandStack inv = frame.getThread().currentFrame().getOperandStack();
                        switch (code) {
                            case 0xac:
                                inv.pushInt(ret.popInt());
                                break;
                            case 0xad:
                                inv.pushLong(ret.popLong());
                                break;
                            case 0xae:
                                inv.pushFloat(ret.popFloat());
                                break;
                            case 0xaf:
                                inv.pushDouble(ret.popDouble());
                                break;
                            case 0xb0:
                                inv.pushRef(ret.popRef());
                                break;
                            default:
                                throw new RuntimeException("Unknown code!");
                        }
                    }
                };
            }
        };
    }


    private abstract class ActionByDiscriptor {
        public ActionByDiscriptor(String descriptor) {
            this.descriptor = descriptor;
        }

        private String descriptor;

        protected abstract void whenInt();

        protected abstract void whenFloat();

        protected abstract void whenLong();

        protected abstract void whenDouble();

        protected abstract void whenRef();

        public void action() {
            switch (descriptor.charAt(0)) {
                case 'Z':
                case 'B':
                case 'C':
                case 'S':
                case 'I':
                    whenInt();
                    break;
                case 'F':
                    whenFloat();
                    break;
                case 'J':
                    whenLong();
                    break;
                case 'D':
                    whenDouble();
                    break;
                case 'L':
                case '[':
                    whenRef();
                    break;
                default:
                    throw new RuntimeException(String.format("Unknown Descriptor %s", descriptor));
            }
        }
    }

    /**
     * 0xb2 getstatic
     */
    @Bytecode(0xb2)
    private InstructionGenerator getstatic(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new IndexInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        JJvmMethod method = frame.getMethod();
                        JJvmClass currentClass = method.getJJvmClass();
                        JJvmConstantPool cp = currentClass.getConstantPool();
                        FieldRef fieldRef = (FieldRef) cp.getConstant(index);
                        JJvmField field = fieldRef.resolvedField();
                        JJvmClass cls = field.getJJvmClass();

                        if (!cls.isInitStarted()) {
                            frame.revertNextPC();
                            cls.initClass(frame.getThread());
                            return;
                        }

                        if (!field.isStatic()) {
                            throw new RuntimeException("java.lang.IncompatibleClassChangeError");
                        }

                        String descriptor = field.getDescriptor();
                        int slotId = field.getSlotId();
                        JJvmSlots slots = cls.getStaticVars();
                        OperandStack stack = frame.getOperandStack();

                        new ActionByDiscriptor(descriptor) {
                            @Override
                            protected void whenInt() {
                                stack.pushInt(slots.getInt(slotId));
                            }

                            @Override
                            protected void whenFloat() {
                                stack.pushFloat(slots.getFloat(slotId));
                            }

                            @Override
                            protected void whenLong() {
                                stack.pushLong(slots.getLong(slotId));
                            }

                            @Override
                            protected void whenDouble() {
                                stack.pushDouble(slots.getDouble(slotId));
                            }

                            @Override
                            protected void whenRef() {
                                stack.pushRef(slots.getRef(slotId));
                            }
                        }.action();
                    }
                }.setWide();
            }
        };
    }


    /**
     * 0xb3 putstatic
     */
    @Bytecode(0xb3)
    private InstructionGenerator putstatic(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new IndexInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        JJvmMethod method = frame.getMethod();
                        JJvmClass currentClass = method.getJJvmClass();
                        JJvmConstantPool cp = currentClass.getConstantPool();
                        FieldRef fieldRef = (FieldRef) cp.getConstant(index);
                        JJvmField field = fieldRef.resolvedField();
                        JJvmClass cls = field.getJJvmClass();

                        if (!cls.isInitStarted()) {
                            frame.revertNextPC();
                            cls.initClass(frame.getThread());
                            return;
                        }

                        if (!field.isStatic()) {
                            throw new RuntimeException("java.lang.IncompatibleClassChangeError");
                        }
                        if (field.isFinal()) {
                            if (currentClass != cls || !method.getName().equals("<clinit>")) {
                                throw new RuntimeException("java.lang.IllegalAccessError");
                            }
                        }
                        String descriptor = field.getDescriptor();
                        int slotId = field.getSlotId();
                        JJvmSlots slots = cls.getStaticVars();
                        OperandStack stack = frame.getOperandStack();

                        new ActionByDiscriptor(descriptor) {
                            @Override
                            protected void whenInt() {
                                slots.setInt(slotId, stack.popInt());
                            }

                            @Override
                            protected void whenFloat() {
                                slots.setFloat(slotId, stack.popFloat());
                            }

                            @Override
                            protected void whenLong() {
                                slots.setLong(slotId, stack.popLong());
                            }

                            @Override
                            protected void whenDouble() {
                                slots.setDouble(slotId, stack.popDouble());
                            }

                            @Override
                            protected void whenRef() {
                                slots.setRef(slotId, stack.popRef());
                            }
                        }.action();
                    }
                }.setWide();
            }
        };
    }

    /**
     * 0xb4 getfield
     */
    @Bytecode(0xb4)
    private InstructionGenerator getfield(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new IndexInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        JJvmConstantPool cp = frame.getMethod().getJJvmClass().getConstantPool();
                        JJvmField field = ((FieldRef) cp.getConstant(index)).resolvedField();
                        if (field.isStatic()) {
                            throw new RuntimeException("java.lang.IncompatibleClassChangeError");
                        }

                        OperandStack stack = frame.getOperandStack();
                        JJvmObject ref = (JJvmObject) stack.popRef();
                        if (ref == null) {
                            throw new RuntimeException("java.lang.IncompatibleClassChangeError");
                        }

                        String descriptor = field.getDescriptor();
                        int slotId = field.getSlotId();
                        JJvmSlots slots = ref.getFields();

                        new ActionByDiscriptor(descriptor) {
                            @Override
                            protected void whenInt() {
                                stack.pushInt(slots.getInt(slotId));
                            }

                            @Override
                            protected void whenFloat() {
                                stack.pushFloat(slots.getFloat(slotId));
                            }

                            @Override
                            protected void whenLong() {
                                stack.pushLong(slots.getLong(slotId));
                            }

                            @Override
                            protected void whenDouble() {
                                stack.pushDouble(slots.getDouble(slotId));
                            }

                            @Override
                            protected void whenRef() {
                                stack.pushRef(slots.getRef(slotId));
                            }
                        }.action();
                    }
                }.setWide();
            }
        };
    }

    /**
     * 0xb5 putfield
     */
    @Bytecode(0xb5)
    private InstructionGenerator putfield(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new IndexInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        JJvmMethod curMethod = frame.getMethod();
                        JJvmClass curClass = curMethod.getJJvmClass();
                        JJvmConstantPool cp = curClass.getConstantPool();
                        FieldRef fieldRef = (FieldRef) cp.getConstant(index);
                        JJvmField field = fieldRef.resolvedField();

                        if (field.isStatic()) {
                            throw new RuntimeException("java.lang.IncompatibleClassChangeError");
                        }

                        if (field.isFinal()) {
                            if (curClass != field.getJJvmClass() || !curMethod.getName().equals("<init>")) {
                                throw new RuntimeException("java.lang.IllegalAccessError");
                            }
                        }

                        String descriptor = field.getDescriptor();
                        int slotId = field.getSlotId();
                        OperandStack stack = frame.getOperandStack();

                        new ActionByDiscriptor(descriptor) {
                            @Override
                            protected void whenInt() {
                                int val = stack.popInt();
                                JJvmObject ref = (JJvmObject) stack.popRef();
                                if (ref == null) {
                                    throw new RuntimeException("java.lang.NullPointerException");
                                }
                                ref.getFields().setInt(slotId, val);
                            }

                            @Override
                            protected void whenFloat() {
                                float val = stack.popFloat();
                                JJvmObject ref = (JJvmObject) stack.popRef();
                                if (ref == null) {
                                    throw new RuntimeException("java.lang.NullPointerException");
                                }
                                ref.getFields().setFloat(slotId, val);
                            }

                            @Override
                            protected void whenLong() {
                                long val = stack.popLong();
                                JJvmObject ref = (JJvmObject) stack.popRef();
                                if (ref == null) {
                                    throw new RuntimeException("java.lang.NullPointerException");
                                }
                                ref.getFields().setLong(slotId, val);
                            }

                            @Override
                            protected void whenDouble() {
                                double val = stack.popDouble();
                                JJvmObject ref = (JJvmObject) stack.popRef();
                                if (ref == null) {
                                    throw new RuntimeException("java.lang.NullPodoubleerException");
                                }
                                ref.getFields().setDouble(slotId, val);
                            }

                            @Override
                            protected void whenRef() {
                                Object val = stack.popRef();
                                JJvmObject ref = (JJvmObject) stack.popRef();
                                if (ref == null) {
                                    throw new RuntimeException("java.lang.NullPoObjecterException");
                                }
                                ref.getFields().setRef(slotId, val);
                            }
                        }.action();
                    }
                }.setWide();
            }
        };
    }

    /**
     * 0xb6 invokevirtual
     */
    @Bytecode(0xb6)
    private InstructionGenerator invokeVirtual(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new IndexInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        JJvmClass currentClass = frame.getMethod().getJJvmClass();
                        JJvmConstantPool cp = currentClass.getConstantPool();
                        MethodRef methodRef = (MethodRef) cp.getConstant(index);
                        JJvmMethod resolvedMethod = methodRef.resolvedMethod();
                        if (resolvedMethod.isStatic()) {
                            throw new IncompatibleClassChangeError();
                        }

                        JJvmObject ref = (JJvmObject) frame.getOperandStack().getRefFromTop(resolvedMethod.getArgSlotCount() - 1);

                        if (ref == null) {
                            // hack System.out.println()
                            if (methodRef.getName().equals("println")) {
                                fakePrintln(frame.getOperandStack(), methodRef.getDescriptor());
                                return;
                            }

                            throw new NullPointerException();
                        }

                        if (resolvedMethod.isProtected() &&
                                resolvedMethod.getJJvmClass().isSuperClassOf(currentClass) &&
                                !resolvedMethod.getJJvmClass().getPackageName().equals(currentClass.getPackageName()) &&
                                ref.getJJvmClass() != currentClass &&
                                !ref.getJJvmClass().isSubClassOf(currentClass)) {
                            throw new IllegalAccessError();
                        }

                        JJvmMethod methodToBeInvoked = MethodRef.lookupMethodInClass(ref.getJJvmClass(), methodRef.getName(), methodRef.getDescriptor());

                        if (methodToBeInvoked == null || methodToBeInvoked.isAbstract()) {
                            throw new AbstractMethodError();
                        }

                        invokeMethod(frame, methodToBeInvoked);

                    }
                }.setWide();
            }
        };
    }

    private void fakePrintln(OperandStack stack, String descriptor) {
        switch (descriptor) {
            case "(Z)V":
                System.out.println(stack.popInt() != 0);
                break;
            case "(C)V":
                System.out.println((char) stack.popInt());
                break;
            case "(B)V":
            case "(S)V":
            case "(I)V":
                System.out.println(stack.popInt());
                break;
            case "(J)V":
                System.out.println(stack.popLong());
                break;
            case "(F)V":
                System.out.println(stack.popFloat());
                break;
            case "(D)V":
                System.out.println(stack.popDouble());
                break;
            default:
                throw new RuntimeException("println: " + descriptor);
        }
        stack.popRef();
    }

    /**
     * 0xb7 invokespecial
     */
    @Bytecode(0xb7)
    private InstructionGenerator invokeSpecial(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new IndexInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        JJvmClass currentClass = frame.getMethod().getJJvmClass();
                        JJvmConstantPool cp = currentClass.getConstantPool();
                        MethodRef methodRef = (MethodRef) cp.getConstant(index);
                        JJvmClass resolvedClass = methodRef.resolvedClass();
                        JJvmMethod resolvedMethod = methodRef.resolvedMethod();

                        if (resolvedMethod.getName().equals("<init>") && resolvedMethod.getJJvmClass() != resolvedClass) {
                            throw new NoSuchMethodError();
                        }

                        if (resolvedMethod.isStatic()) {
                            throw new IncompatibleClassChangeError();
                        }

                        JJvmObject ref = (JJvmObject) frame.getOperandStack().getRefFromTop(resolvedMethod.getArgSlotCount() - 1);
                        if (ref == null) {
                            throw new NullPointerException();
                        }

                        if (resolvedMethod.isProtected() &&
                                resolvedMethod.getJJvmClass().isSuperClassOf(currentClass) &&
                                resolvedMethod.getJJvmClass().getPackageName().equals(currentClass.getPackageName()) &&
                                ref.getJJvmClass() != currentClass &&
                                !ref.getJJvmClass().isSubClassOf(currentClass)) {
                            throw new IllegalAccessError();
                        }

                        JJvmMethod methodToBeInvoked = resolvedMethod;
                        if (currentClass.isSuper() &&
                                resolvedClass.isSubClassOf(currentClass) &&
                                resolvedMethod.getName().equals("<init>")) {
                            methodToBeInvoked = MethodRef.lookupMethodInClass(currentClass.getSuperClass(), methodRef.getName(), methodRef.getDescriptor());
                        }

                        if (methodToBeInvoked == null || methodToBeInvoked.isAbstract()) {
                            throw new AbstractMethodError();
                        }

                        invokeMethod(frame, methodToBeInvoked);
                    }
                }.setWide();
            }
        };
    }

    /**
     * 0xb8 invokestatic
     */
    @Bytecode(0xb8)
    private InstructionGenerator invokeStatic(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new IndexInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        MethodRef methodRef = (MethodRef) frame.getMethod().getJJvmClass().getConstantPool().getConstant(index);
                        JJvmMethod method = methodRef.resolvedMethod();

                        JJvmClass cls = method.getJJvmClass();
                        if (!cls.isInitStarted()) {
                            frame.revertNextPC();
                            cls.initClass(frame.getThread());
                            return;
                        }

                        if (!method.isStatic()) {
                            throw new IncompatibleClassChangeError();
                        }
                        invokeMethod(frame, method);
                    }
                }.setWide();
            }
        };
    }

    /**
     * 0xb9 invokeinterface
     */
    @Bytecode(0xb9)
    private InstructionGenerator invokeInterface(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new Instruction() {
                    int index;
                    // byte count;
                    // byte zero;

                    @Override
                    public void fetchOperands(BytecodeReader reader) {
                        index = reader.readU2I();
                        reader.readU1(); // count
                        reader.readU1(); // must be zero
                    }

                    @Override
                    public void execute(Frame frame) {
                        JJvmConstantPool cp = frame.getMethod().getJJvmClass().getConstantPool();
                        InterfaceMethodRef methodRef = (InterfaceMethodRef) cp.getConstant(index);
                        JJvmMethod resolvedMethod = methodRef.resolvedInterfaceMethod();

                        if (resolvedMethod.isStatic() || resolvedMethod.isPrivate()) {
                            throw new IncompatibleClassChangeError();
                        }

                        JJvmObject ref = (JJvmObject) frame.getOperandStack().getRefFromTop(resolvedMethod.getArgSlotCount() - 1);

                        if (ref == null) {
                            throw new NullPointerException();
                        }

                        if (!ref.getJJvmClass().isImplements(methodRef.resolvedClass())) {
                            throw new IncompatibleClassChangeError();
                        }

                        JJvmMethod methodToBeInvoked = MethodRef.lookupMethodInClass(ref.getJJvmClass(), methodRef.getName(), methodRef.getDescriptor());

                        if (methodToBeInvoked == null || methodToBeInvoked.isAbstract()) {
                            throw new AbstractMethodError();
                        }

                        if (!methodToBeInvoked.isPublic()) {
                            throw new IllegalAccessError();
                        }

                        invokeMethod(frame, methodToBeInvoked);
                    }
                };
            }
        };
    }

    /*
     * TODO:
     * 0xba invokedynamic
     */

    private void invokeMethod(Frame frame, JJvmMethod method) {
        Thread thread = frame.getThread();
        Frame newFrame = thread.buildFrame(method);
        thread.pushFrame(newFrame);

        int argSlotCount = method.getArgSlotCount();
        if (argSlotCount > 0) {
            for (int i = argSlotCount - 1; i >= 0; i--) {
                newFrame.getLocalVars().setSlot(i, frame.getOperandStack().popSlot());
            }
        }

        if (method.isNative()) {
            if (method.getName().equals("registerNatives")) {
                thread.popFrame();
            } else {
                throw new RuntimeException(String.format("native method: %s.%s%s", method.getJJvmClass().getName(), method.getName(), method.getDescriptor()));
            }
        }
    }

    /**
     * 0xbb new
     */
    @Bytecode(0xbb)
    private InstructionGenerator inew(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new IndexInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        JJvmConstantPool cp = frame.getMethod().getJJvmClass().getConstantPool();
                        ClassRef classRef = (ClassRef) cp.getConstant(index);
                        JJvmClass cls = classRef.resolvedClass();

                        if (!cls.isInitStarted()) {
                            frame.revertNextPC();
                            cls.initClass(frame.getThread());
                            return;
                        }

                        if (cls.isInterface() || cls.isAbstract()) {
                            throw new RuntimeException("java.lang.InstantiationError");
                        }

                        JJvmObject ref = cls.newObject();
                        frame.getOperandStack().pushRef(ref);
                    }
                }.setWide();
            }
        };
    }

    /**
     * 0xbc newarray
     */
    @Bytecode(0xbc)
    private InstructionGenerator newarray(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new Instruction() {
                    byte atype;

                    @Override
                    public void fetchOperands(BytecodeReader reader) {
                        atype = reader.readU1();
                    }

                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        int count = stack.popInt();
                        if (count < 0) {
                            throw new NegativeArraySizeException();
                        }
                        JJvmClassLoader loader = frame.getMethod().getJJvmClass().getClassLoader();
                        JJvmClass arrClass = getPrimitiveArrayClass(loader, atype);
                        JJvmObject arr = arrClass.newArray(count);
                        stack.pushRef(arr);
                    }
                };
            }
        };
    }

    private JJvmClass getPrimitiveArrayClass(JJvmClassLoader loader, byte atype) {
        switch (atype) {
            case ATYPE.BOOLEAN:
                return loader.loadClass("[Z");
            case ATYPE.BYTE:
                return loader.loadClass("[B");
            case ATYPE.CHAR:
                return loader.loadClass("[C");
            case ATYPE.DOUBLE:
                return loader.loadClass("[D");
            case ATYPE.FLOAT:
                return loader.loadClass("[F");
            case ATYPE.INT:
                return loader.loadClass("[I");
            case ATYPE.LONG:
                return loader.loadClass("[J");
            case ATYPE.SHORT:
                return loader.loadClass("[S");
            default:
                throw new RuntimeException("Invalid atype!");
        }
    }

    private interface ATYPE {
        byte BOOLEAN = 4;
        byte CHAR = 5;
        byte FLOAT = 6;
        byte DOUBLE = 7;
        byte BYTE = 8;
        byte SHORT = 9;
        byte INT = 10;
        byte LONG = 11;
    }

    /**
     * 0xbd anewarray
     */
    @Bytecode(0xbd)
    private InstructionGenerator anewarray(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new IndexInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        JJvmConstantPool cp = frame.getMethod().getJJvmClass().getConstantPool();
                        ClassRef classRef = (ClassRef) cp.getConstant(index);
                        JJvmClass componentClass = classRef.resolvedClass();

                        OperandStack stack = frame.getOperandStack();
                        int count = stack.popInt();
                        if (count < 0) {
                            throw new NegativeArraySizeException();
                        }

                        JJvmClass arrClass = componentClass.getArrayClass();
                        JJvmObject arr = arrClass.newArray(count);
                        stack.pushRef(arr);
                    }
                }.setWide();
            }
        };
    }

    /**
     * 0xbe arraylength
     */
    @Bytecode(0xbe)
    private InstructionGenerator arrayLength(int code) {
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        JJvmObject arrRef = (JJvmObject) stack.popRef();
                        if (arrRef == null) {
                            throw new NullPointerException();
                        }
                        int arrLen = arrRef.getArrayLength();
                        stack.pushInt(arrLen);
                    }
                };
            }
        };
    }

    /*
     * TODO:
     * 0xbf athrow
     */

    /**
     * 0xc0 checkcast
     */
    @Bytecode(0xc0)
    private InstructionGenerator checkcast(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new IndexInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        JJvmObject ref = (JJvmObject) stack.popRef();
                        stack.pushRef(ref);
                        if (ref == null) {
                            return;
                        }

                        JJvmConstantPool cp = frame.getMethod().getJJvmClass().getConstantPool();
                        ClassRef classRef = (ClassRef) cp.getConstant(index);
                        JJvmClass jjvmClass = classRef.resolvedClass();
                        if (!ref.isInstanceOf(jjvmClass)) {
                            throw new RuntimeException("java.lang.ClassCastException");
                        }
                    }
                }.setWide();
            }
        };
    }

    /**
     * 0xc1 instanceof
     */
    @Bytecode(0xc1)
    private InstructionGenerator i_instanceof(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new IndexInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        OperandStack stack = frame.getOperandStack();
                        JJvmObject ref = (JJvmObject) stack.popRef();
                        if (ref == null) {
                            stack.pushInt(0);
                            return;
                        }

                        JJvmConstantPool cp = frame.getMethod().getJJvmClass().getConstantPool();
                        ClassRef classRef = (ClassRef) cp.getConstant(index);
                        JJvmClass jjvmClass = classRef.resolvedClass();
                        if (ref.isInstanceOf(jjvmClass)) {
                            stack.pushInt(1);
                        } else {
                            stack.pushInt(0);
                        }
                    }
                }.setWide();
            }
        };
    }

    /*
     * TODO:
     * 0xc2 monitorenter
     * 0xc3 monitorexit
     */

    /**
     * 0xc4 wide
     */
    @Bytecode(0xc4)
    private InstructionGenerator wide(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new Instruction() {
                    Instruction modifiedInstruction;

                    @Override
                    public void fetchOperands(BytecodeReader reader) {
                        modifiedInstruction = decode(reader.readU1());

                        if (!(modifiedInstruction instanceof VariableWideInstruction)) {
                            throw new RuntimeException("Not a wide instruction!");
                        }

                        ((VariableWideInstruction) modifiedInstruction).setWide();
                        modifiedInstruction.fetchOperands(reader);
                    }

                    @Override
                    public void execute(Frame frame) {
                        modifiedInstruction.execute(frame);
                    }
                };
            }
        };
    }

    /**
     * 0xc5 multianewarray
     */
    @Bytecode(0xc5)
    private InstructionGenerator multiANewArray(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new Instruction() {
                    short index;
                    int dimensions;
                    @Override
                    public void fetchOperands(BytecodeReader reader) {
                        index = reader.readU2();
                        dimensions = reader.readU1I();
                    }

                    @Override
                    public void execute(Frame frame) {
                        JJvmConstantPool cp = frame.getMethod().getJJvmClass().getConstantPool();
                        ClassRef classRef = (ClassRef) cp.getConstant(index);
                        JJvmClass arrClass = classRef.resolvedClass();
                        OperandStack stack = frame.getOperandStack();
                        int[] counts = popAndCheckCounts(stack);
                        JJvmObject arr = newMultiDimensionalArray(counts, arrClass);
                        stack.pushRef(arr);
                    }

                    private JJvmObject newMultiDimensionalArray(int[] counts, JJvmClass arrClass) {
                        JJvmObject arr = arrClass.newArray(counts[0]);
                        if (counts.length > 1) {
                            JJvmObject[] arrs = arr.getRefs();
                            for (int i = 0; i < arrs.length; i++) {
                                arrs[i] = newMultiDimensionalArray(Arrays.copyOfRange(counts, 1, counts.length), arrClass.getComponentClass());
                            }
                        }
                        return arr;
                    }

                    private int[] popAndCheckCounts(OperandStack stack) {
                        int[] counts = new int[dimensions];
                        for (int i = dimensions-1; i >= 0; i--) {
                            counts[i] = stack.popInt();
                            if (counts[i] < 0) {
                                throw new NegativeArraySizeException();
                            }
                        }
                        return counts;
                    }
                };
            }
        };
    }

    /**
     * 0xc6 ifnull
     * 0xc7 ifnonnull
     */
    @BytecodeRange(lower = 0xc6, upper = 0xc7)
    private InstructionGenerator ifopnull(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new BranchInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        Object ref = frame.getOperandStack().popRef();
                        if ((code == 0xc6 && ref == null) || (code == 0xc7 && ref != null)) {
                            branch(frame, offset);
                        }
                    }
                };
            }
        };
    }

    /**
     * 0xc8 goto_w
     */
    @Bytecode(0xc8)
    private InstructionGenerator goto_w(int code) {
        return new InstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new BranchInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        branch(frame, offset);
                    }
                }.setWide();
            }
        };
    }

    /*
     * TODO:
     * 0xc9 jsr_w
     * 0xca breakpoint
     * 0xfe impdep1
     * 0xff impdep2
     */

    private InstructionGenerator unimplemented(int code) {
        return new CachedInstructionGenerator(code) {
            @Override
            Instruction construct() {
                return new NoOperandsInstruction() {
                    @Override
                    public void execute(Frame frame) {
                        throw new RuntimeException(String.format("Unimplemented instruction with bytecode %02x!", code));
                    }
                };
            }
        };
    }

}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(Bytecodes.class)
@interface Bytecode {
    int value();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Bytecodes {
    Bytecode[] value();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(BytecodeRanges.class)
@interface BytecodeRange {
    int lower();

    int upper();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface BytecodeRanges {
    BytecodeRange[] value();
}

abstract class VariableWideInstruction implements Instruction {
    boolean wide = false;

    public VariableWideInstruction setWide() {
        wide = true;
        return this;
    }
}

abstract class BranchInstruction extends VariableWideInstruction {
    int offset;

    @Override
    public void fetchOperands(BytecodeReader reader) {
        if (wide) {
            offset = reader.readU4();
        } else {
            offset = reader.readU2();
        }
    }
}

abstract class IndexInstruction extends VariableWideInstruction {
    int index;

    @Override
    public void fetchOperands(BytecodeReader reader) {
        if (wide) {
            index = reader.readU2I();
        } else {
            index = reader.readU1I();
        }
    }
}

abstract class NoOperandsInstruction implements Instruction {
    @Override
    public void fetchOperands(BytecodeReader reader) {
        // pass
    }
}

abstract class InstructionGenerator {
    protected int code;

    InstructionGenerator(int code) {
        this.code = code;
    }

    Instruction gen() {
        return construct();
    }

    abstract Instruction construct();
}

abstract class CachedInstructionGenerator extends InstructionGenerator {
    private Instruction inst = null;

    CachedInstructionGenerator(int code) {
        super(code);
    }

    Instruction gen() {
        if (inst == null) {
            inst = construct();
        }
        return inst;
    }
}
