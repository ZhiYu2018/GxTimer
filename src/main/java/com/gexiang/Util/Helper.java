package com.gexiang.Util;

public class Helper {
    public static boolean isStrLenEqual(String str, int len){
        if(str == null){
            return false;
        }else{
            return (str.length() == len);
        }
    }

    public static boolean isStrLenLess(String str, int len){
        if(str == null){
            return false;
        }else{
            return (str.length() <= len);
        }
    }

    public static boolean isStrLenBigger(String str, int len){
        return ((str != null) && (str.length() > len));
    }

    public static boolean isValueRange(int src, int min, int max){
        return ((src >= min) && (src <= max));
    }
}
