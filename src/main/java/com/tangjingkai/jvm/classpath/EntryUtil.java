package com.tangjingkai.jvm.classpath;

/**
 * Created by totran on 11/13/16.
 */
public class EntryUtil {
    public static Entry getEntry(String path) {
        if (path.contains(Entry.pathListSeparator)) {
            return new CompositeEntry(path);
        }

        if (path.endsWith("*")) {
            return new WildcardEntry(path);
        }

        if (path.endsWith(".jar") || path.endsWith(".JAR")
                || path.endsWith(".zip") || path.endsWith(".ZIP")) {
            return new ZipEntry(path);
        }

        return new DirEntry(path);
    }
}
