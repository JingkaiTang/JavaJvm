package com.tangjingkai.jvm.classpath;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by totran on 11/13/16.
 */
public class CompositeEntry implements Entry {
    protected List<Entry> entries;

    protected CompositeEntry() {
        entries = new ArrayList<>();
    }

    public CompositeEntry(String path) {
        this();
        String[] cps = path.split(pathListSeparator);
        for (String cp : cps) {
            entries.add(EntryUtil.getEntry(cp));
        }
    }

    public List<Entry> getEntries() {
        return entries;
    }

    @Override
    public byte[] readClass(String className) {
        for (Entry entry : entries) {
            byte[] data = entry.readClass(className);
            if (data != null) {
                return data;
            }
        }
        return null;
    }

    @Override
    public String getPath() {
        List<String> pathList = new ArrayList<>();
        entries.forEach(entry -> pathList.add(entry.getPath()));
        return StringUtils.join(pathList, pathListSeparator);
    }
}
