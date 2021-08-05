package com.zqykj.infrastructure.storage.kv;

import com.zqykj.infrastructure.exception.ErrorCode;
import com.zqykj.infrastructure.exception.KvStorageException;
import com.zqykj.infrastructure.util.ByteUtils;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Realization of KV storage based on memory.
 */
public class MemoryKvStorage implements KvStorage {

    private final Map<Key, byte[]> storage = new ConcurrentSkipListMap<>();

    @Override
    public byte[] get(byte[] key) throws KvStorageException {
        return storage.get(new Key(key));
    }

    @Override
    public Map<byte[], byte[]> batchGet(List<byte[]> keys) throws KvStorageException {
        Map<byte[], byte[]> result = new HashMap<>(keys.size());
        for (byte[] key : keys) {
            byte[] val = storage.get(new Key(key));
            if (val != null) {
                result.put(key, val);
            }
        }
        return result;
    }

    @Override
    public void put(byte[] key, byte[] value) throws KvStorageException {
        storage.put(new Key(key), value);
    }

    @Override
    public void batchPut(List<byte[]> keys, List<byte[]> values) throws KvStorageException {
        if (keys.size() != values.size()) {
            throw new KvStorageException(ErrorCode.KVStorageBatchWriteError.getCode(),
                    "key's size must be equal to value's size");
        }
        int size = keys.size();
        for (int i = 0; i < size; i++) {
            storage.put(new Key(keys.get(i)), values.get(i));
        }
    }

    @Override
    public void delete(byte[] key) throws KvStorageException {
        storage.remove(new Key(key));
    }

    @Override
    public void batchDelete(List<byte[]> keys) throws KvStorageException {
        for (byte[] key : keys) {
            storage.remove(new Key(key));
        }
    }

    @Override
    public void doSnapshot(String backupPath) throws KvStorageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void snapshotLoad(String path) throws KvStorageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<byte[]> allKeys() throws KvStorageException {
        List<byte[]> result = new LinkedList<>();
        for (Key each : storage.keySet()) {
            result.add(each.origin);
        }
        return result;
    }

    @Override
    public void shutdown() {
        storage.clear();
    }

    private static class Key implements Comparable<Key> {

        private final byte[] origin;

        private Key(byte[] origin) {
            this.origin = origin;
        }

        @Override
        public int compareTo(Key o) {
            return ByteUtils.compare(origin, o.origin);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;
            return Arrays.equals(origin, key.origin);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(origin);
        }
    }

}
