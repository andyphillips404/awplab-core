package com.awplab.core.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andyphillips404 on 5/8/17.
 */
public class MapUtils {
    private MapUtils() {
    }

    public static Map<String, Object> createDataMap(Object... keysAndData) {

        HashMap<String, Object> data = new HashMap<>();
        for (int x = 0; x < keysAndData.length; x += 2) {
            data.put(keysAndData[x].toString(), keysAndData[x+1]);
        }
        return data;
    }


}
