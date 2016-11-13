package com.tangjingkai.jvm.classpath;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

/**
 * Created by totran on 11/13/16.
 */
public class Classpath implements Entry {
    private Entry bootClasspath;
    private Entry extClasspath;
    private Entry userClasspath;

    /**
     * Constructor
     *
     * @param jreOption for bootClasspath/extClasspath
     * @param cpOption  for userClasspath
     */
    public Classpath(String jreOption, String cpOption) {
        parseBootAndExtClasspath(jreOption);
        parseUserClasspath(cpOption);
    }

    private void parseBootAndExtClasspath(String jreOption) {
        String jreDir = getJreDir(jreOption);

        // jre/lib/*
        String jreLibDir = FilenameUtils.concat(jreDir, "lib");
        String jreLibPath = FilenameUtils.concat(jreLibDir, "*");
        bootClasspath = new WildcardEntry(jreLibPath);

        // jre/lib/ext/*
        String jreExtDir = FilenameUtils.concat(jreLibDir, "ext");
        String jreExtPath = FilenameUtils.concat(jreExtDir, "*");
        extClasspath = new WildcardEntry(jreExtPath);
    }

    private String getJreDir(String jreOption) {
        if (jreOption != null && !jreOption.equals("") && new File(jreOption).exists()) {
            return jreOption;
        }

        if (new File("./jre").exists()) {
            return "./jre";
        }

        String jh = System.getenv("JAVA_HOME");
        if (jh != null && !jh.equals("")) {
            return FilenameUtils.concat(jh, "jre");
        }

        throw new RuntimeException("Can not find jre folder!");
    }

    private void parseUserClasspath(String cpOption) {
        if (cpOption == null || cpOption.equals("")) {
            cpOption = ".";
        }
        userClasspath = EntryUtil.getEntry(cpOption);
    }

    @Override
    public byte[] readClass(String className) {
        className = String.format("%s.class", className);
        byte[] data = bootClasspath.readClass(className);
        if (data != null) {
            return data;
        }
        data = extClasspath.readClass(className);
        if (data != null) {
            return data;
        }
        return userClasspath.readClass(className);
    }

    @Override
    public String getPath() {
        return userClasspath.getPath();
    }
}
