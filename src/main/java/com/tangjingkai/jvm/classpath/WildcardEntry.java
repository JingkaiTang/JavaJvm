package com.tangjingkai.jvm.classpath;

import java.io.File;

/**
 * Created by totran on 11/13/16.
 */
public class WildcardEntry extends CompositeEntry {
    public WildcardEntry(String path) {
        super();
        File baseDir = new File(path.substring(0, path.length() - 1));
        if (baseDir.exists()) {
            for (String jarFile : baseDir.list((dir, name)
                    -> new File(dir, name).isFile()
                    && (name.endsWith(".jar") || name.endsWith(".JAR")))) {
                entries.add(new ZipEntry(new File(baseDir, jarFile).getPath()));
            }
        }
    }
}
