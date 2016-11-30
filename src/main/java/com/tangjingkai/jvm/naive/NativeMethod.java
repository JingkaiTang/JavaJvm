package com.tangjingkai.jvm.naive;

import com.tangjingkai.jvm.rtda.Frame;

/**
 * Created by totran on 11/29/16.
 */
public interface NativeMethod {
    void execute(Frame frame);
}
