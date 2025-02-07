package com.xcj.chat;

import java.io.Closeable;

public class Utils {
    public static void close(Closeable... targets){
        for(Closeable target:targets){
            try {
                if(null != target)
                    target.close();
            }catch (Exception E){
                E.printStackTrace();
            }
        }
    }
}
