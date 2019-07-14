package com.example.multiThread.KDB;


import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.lang.reflect.Array;

public class KdbExample {
    public static void main(String[] args) throws IOException, c.KException {
//        String s = "(`a`b; `c`d`e; enlist `f)!10 20 30";
//        "flip ([] c1:10 20 30; c2:1.1 2.2 3.3)"
        String s = "flip `c`d!(10 40 50;22 44 6)";
        c c = new c("localhost", 5001);
        Object result = c.k(s);
//        System.out.println(new ResultFormatter().formatResult(result));
//        System.out.println(ArrayUtils.toString(result));

//        if (result.getClass().isArray()) {
////            for (Object ele : (Object [])result){
////            }
        //work for array only
//            System.out.println(ArrayUtils.toString(result));
//        } else {
//            System.out.println(Objects.toString(result));
//        }
        System.out.println(formatKdbResult(result));
        c.close();
    }

    public static String formatKdbResult(Object object) {
        if (object instanceof c.Dict) {
            return formatDict(object);
        } else if (object instanceof c.Flip) {
//            return ArrayUtils.toString(object);
            return formatFlip(object);
        } else {
            return ArrayUtils.toString(object);
        }
    }

    public static String formatDict(Object object) {
        c.Dict dict = ((c.Dict) object);
        Object[] keys = (Object[]) dict.x;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.length; i++) {
            sb.append(String.format("%s| %s\n", ArrayUtils.toString(keys[i]), ArrayUtils.toString(Array.get(dict.y, i))));
        }
        return sb.toString();
    }

    public static String formatFlip(Object object) {
        c.Flip dict = ((c.Flip) object);
        String[] keys = dict.x;
        StringBuilder sb = new StringBuilder();
        sb.append(String.join("\t", keys))
                .append("\n");
        int length = ArrayUtils.getLength(Array.get(dict.y, 0));
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < keys.length; j++) {
                sb.append(Array.get(Array.get(dict.y, j), i));
                sb.append("\t");
//                sb.append(String.format("%s| %s\n", ArrayUtils.toString(keys[i]), ArrayUtils.toString(Array.get(dict.y, i))));
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}