package com.tangjingkai.jvm.rtda.heap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by totran on 11/29/16.
 */
public class InternedStrings {
    static Map<String, JJvmObject> data = new HashMap<>();

    public static JJvmObject getString(JJvmClassLoader loader, String str) {
        if (data.containsKey(str)) {
            return data.get(str);
        }

        char[] chars = str.toCharArray();
        JJvmObject jChars = new JJvmObject(loader.loadClass("[C"), chars);
        JJvmObject jString = loader.loadClass("java/lang/String").newObject();
        jString.setRefVar("value", "[C", jChars);

        data.put(str, jString);
        return jString;
    }

    public static String unwrapString(JJvmObject jString) {
        return new String(jString.getRefVar("value", "[C").getChars());
    }

    public static JJvmObject getInterned(JJvmObject jString) {
        String str = unwrapString(jString);
        if (data.containsKey(str)) {
            return data.get(str);
        }

        data.put(str, jString);
        return jString;
    }
}
