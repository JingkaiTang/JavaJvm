package com.tangjingkai.jvm.rtda.heap;

/**
 * Created by totran on 11/19/16.
 */
public interface JJvmAccessFlag {
    int ACC_PUBLIC = 0x0001; // cfm
    int ACC_PRIVATE = 0x0002; // fm
    int ACC_PROTECTED = 0x0004; // fm
    int ACC_STATIC = 0x0008; // fm
    int ACC_FINAL = 0x0010; // cfm
    int ACC_SUPER = 0x0020; // c
    int ACC_SYNCHRONIZED = 0x0020; // m
    int ACC_VOLATILE = 0x0040; // f
    int ACC_BRIDGE = 0x0040; // m
    int ACC_TRANSIENT = 0x0080; // f
    int ACC_VARARGS = 0x0080; // m
    int ACC_NATIVE = 0x0100; // m
    int ACC_INTERFACE = 0x0200; // c
    int ACC_ABSTRACT = 0x0400; // cm
    int ACC_STRICT = 0x0800; // m
    int ACC_SYNTHETIC = 0x1000; // cfm
    int ACC_ANNOTATION = 0x2000; // c
    int ACC_ENUM = 0x4000; // cf
}
