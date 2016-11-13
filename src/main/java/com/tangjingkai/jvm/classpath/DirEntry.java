package com.tangjingkai.jvm.classpath;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by totran on 11/13/16.
 */
public class DirEntry implements Entry {
    private String absDir;

    public DirEntry(String path) {
        absDir = new File(path).getAbsolutePath();
    }

    @Override
    public byte[] readClass(String className) {
        File classFile = new File(absDir, className);
        try {
            return FileUtils.readFileToByteArray(classFile);
        } catch (IOException e) {
        }
        return null;
    }

    @Override
    public String getPath() {
        return absDir;
    }
}
