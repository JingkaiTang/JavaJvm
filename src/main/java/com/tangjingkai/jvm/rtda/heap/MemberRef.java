package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.classfile.MemberRefCI;
import com.tangjingkai.jvm.classfile.NameAndTypeCI;

/**
 * Created by totran on 11/19/16.
 */
public class MemberRef extends SymRef {
    String name;
    String descriptor;

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public MemberRef(JJvmConstantPool constantPool, MemberRefCI memberRefCI) {
        this.constantPool = constantPool;
        this.className = memberRefCI.getClassName();
        NameAndTypeCI.NTValue ntv = memberRefCI.getNameAndDescriptor();
        this.name = ntv.name;
        this.descriptor = ntv.type;
    }
}
