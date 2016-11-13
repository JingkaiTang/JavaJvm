package com.tangjingkai.jvm.classpath;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

/**
 * Created by totran on 11/13/16.
 */
public class ZipEntry implements Entry {
    private String absPath;

    public ZipEntry(String path) {
        absPath = new File(path).getAbsolutePath();
    }

    @Override
    public byte[] readClass(String className) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(absPath))) {
            for (java.util.zip.ZipEntry ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry()) {
                if (ze.getName().equals(className)) {
                    return IOUtils.toByteArray(zis);
                }
            }
        } catch (IOException e) {
        }
        return null;
    }

    @Override
    public String getPath() {
        return absPath;
    }
}
