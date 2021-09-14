package com.zqykj.infrastructure.serialize.impl;


import com.zqykj.infrastructure.serialize.Serializer;
import com.zqykj.infrastructure.util.ByteUtils;
import com.zqykj.infrastructure.util.JacksonUtils;

import java.lang.reflect.Type;

/**
 * Serializer implement by jackson.
 *
 */
public class JacksonSerializer implements Serializer {
    
    @Override
    public <T> T deserialize(byte[] data) {
        throw new UnsupportedOperationException("Jackson serializer can't support deserialize json without type");
    }
    
    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        if (ByteUtils.isEmpty(data)) {
            return null;
        }
        return JacksonUtils.toObj(data, cls);
}

    @Override
    public <T> T deserialize(byte[] data, Type type) {
        if (ByteUtils.isEmpty(data)) {
            return null;
        }
        return JacksonUtils.toObj(data, type);
    }
    
    @Override
    public <T> byte[] serialize(T obj) {
        return JacksonUtils.toJsonBytes(obj);
    }
    
    @Override
    public String name() {
        return "JSON";
    }
}
