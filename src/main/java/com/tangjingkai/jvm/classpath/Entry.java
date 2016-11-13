package com.tangjingkai.jvm.classpath;

import java.io.File;

/**
 * Created by totran on 11/13/16.
 */
public interface Entry {
    String pathListSeparator = File.pathSeparator;

    byte[] readClass(String className);

    String getPath();
}
