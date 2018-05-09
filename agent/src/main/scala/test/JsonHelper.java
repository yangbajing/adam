package test;

import com.alibaba.fastjson.JSON;

public class JsonHelper {
    public static int getInt(byte[] bytes) {
        return JSON.parseObject(bytes, Integer.class);
    }
}
