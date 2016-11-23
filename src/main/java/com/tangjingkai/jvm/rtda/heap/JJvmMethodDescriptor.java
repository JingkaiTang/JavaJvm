package com.tangjingkai.jvm.rtda.heap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by totran on 11/22/16.
 */
public class JJvmMethodDescriptor {
    List<String> parameterTypes = new ArrayList<>();
    String returnType;

    @Override
    public String toString() {
        return "JJvmMethodDescriptor{" +
                "parameterTypes=" + parameterTypes +
                ", returnType='" + returnType + '\'' +
                '}';
    }

    static class Parser {
        String raw;
        int offset;
        JJvmMethodDescriptor md;

        Parser(String descriptor) {
            this.raw = descriptor;
            this.offset = 0;
            md = new JJvmMethodDescriptor();
        }


        void startParams() {
            if (read() != '(') {
                parseError();
            }
        }

        void endParams() {
            if (read() != ')') {
                parseError();
            }
        }

        String returnType() {
            if (read() == 'V') {
                return "V";
            }
            unread();
            String t = parseType();
            if (t.equals("")) {
                parseError();
            }
            return t;
        }

        String parseType() {
            char t = read();
            switch (t) {
                case 'B':
                case 'C':
                case 'D':
                case 'F':
                case 'I':
                case 'J':
                case 'S':
                case 'Z':
                    return String.valueOf(t);
                case 'L':
                    return parseRef();
                case '[':
                    return parseArray();
                default:
                    unread();
                    return "";
            }
        }

        private String parseArray() {
            int arrStart = offset-1;
            parseType();
            return raw.substring(arrStart, offset);
        }

        private String parseRef() {
            int index = raw.substring(offset).indexOf(';');
            if (index >= 0) {
                String t = raw.substring(offset-1, offset+index);
                offset += index+1;
                return t;
            } else {
                parseError();
                return "";
            }
        }

        char read() {
            return raw.charAt(offset++);
        }

        void unread() {
            offset--;
        }

        void parseError() {
            throw new RuntimeException("Method Descriptor parse error! @descriptor: " + raw);
        }

        public JJvmMethodDescriptor parse() {
            startParams();
            while (true) {
                String t = parseType();
                if (t.equals("")) {
                    break;
                }
                md.parameterTypes.add(t);
            }
            endParams();
            md.returnType = returnType();
            return md;
        }
    }

    public static JJvmMethodDescriptor parse(String descriptor) {
        return new Parser(descriptor).parse();
    }
}
