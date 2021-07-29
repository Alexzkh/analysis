package com.zqykj.infrastructure.serialize;


import com.zqykj.infrastructure.serialize.impl.HessianSerializer;
import com.zqykj.infrastructure.spi.AnalysisServiceLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * 序列化工厂
 * 默认是java--Hession 序列化方式。
 */
public class SerializeFactory {
    
    public static final String HESSIAN_INDEX = "Hessian".toLowerCase();
    
    private static final Map<String, Serializer> SERIALIZER_MAP = new HashMap<String, Serializer>(4);
    
    public static String defaultSerializer = HESSIAN_INDEX;
    
    static {
        Serializer serializer = new HessianSerializer();
        SERIALIZER_MAP.put(HESSIAN_INDEX, serializer);
        for (Serializer item : AnalysisServiceLoader.load(Serializer.class)) {
            SERIALIZER_MAP.put(item.name().toLowerCase(), item);
        }
    }
    
    public static Serializer getDefault() {
        return SERIALIZER_MAP.get(defaultSerializer);
    }
    
    public static Serializer getSerializer(String type) {
        return SERIALIZER_MAP.get(type.toLowerCase());
    }
}
