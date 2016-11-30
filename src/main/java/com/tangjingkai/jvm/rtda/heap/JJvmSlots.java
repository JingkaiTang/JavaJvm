package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.rtda.LocalVars;
import com.tangjingkai.jvm.rtda.Slot;

/**
 * Created by totran on 11/19/16.
 */
public class JJvmSlots extends LocalVars implements Cloneable {
    public JJvmSlots(int maxLocals) {
        super(maxLocals);
    }

    private JJvmSlots() {}

    @Override
    public JJvmSlots clone() {
        JJvmSlots jjvmSlots = new JJvmSlots();
        jjvmSlots.slots = new Slot[slots.length];
        for (int i = 0; i < slots.length; i++) {
            jjvmSlots.slots[i] = slots[i].clone();
        }
        return jjvmSlots;
    }
}
