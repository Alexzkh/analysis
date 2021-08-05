package com.zqykj.infrastructure.storage.kv;

import com.zqykj.infrastructure.exception.ErrorCode;
import com.zqykj.infrastructure.exception.KvStorageException;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/8/2
 */
public class RocksDBKvStorage implements KvStorage {

    private static RocksDB rocksDB;
    private static String path ;


    /**
     * Ensure that a consistent view exists when implementing file copies.
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();


    static {
        RocksDB.loadLibrary();
    }

    public RocksDBKvStorage (String path){
        this.path = path;
    }

    @Override
    public byte[] get(byte[] key) throws KvStorageException {
        readLock.lock();
        try {
            return rocksDB.get(key);
        } catch (RocksDBException e) {
            throw new KvStorageException(ErrorCode.KVStorageReadError, e);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Map<byte[], byte[]> batchGet(List<byte[]> keys) throws KvStorageException {
        readLock.lock();
        try {
            Map<byte[], byte[]> result = new HashMap<>(keys.size());
            for (byte[] key : keys) {
                byte[] val = get(key);
                if (val != null) {
                    result.put(key, val);
                }
            }
            return result;
        } finally {
            readLock.unlock();
        }

    }

    @Override
    public void put(byte[] key, byte[] value) throws KvStorageException {
        readLock.lock();
        try {
            Options options = new Options();
            options.setCreateIfMissing(true);
            try {
                rocksDB = RocksDB.open(options, path);
                rocksDB.put(key, value);
            } catch (RocksDBException e) {
                throw new KvStorageException(ErrorCode.KVStorageWriteError, e);
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void batchPut(List<byte[]> keys, List<byte[]> values) throws KvStorageException {
        readLock.lock();
        try {
            if (keys.size() != values.size()) {
                throw new KvStorageException(ErrorCode.KVStorageBatchWriteError,
                        "key's size must be equal to value's size");
            }
            int size = keys.size();
            for (int i = 0; i < size; i++) {
                put(keys.get(i), values.get(i));
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void delete(byte[] key) throws KvStorageException {
        readLock.lock();
        try {
            rocksDB.delete(key);
        } catch (RocksDBException e) {
            throw new KvStorageException(ErrorCode.KVStorageDeleteError,
                    e);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void batchDelete(List<byte[]> keys) throws KvStorageException {
        readLock.lock();
        try {
            for (byte[] key : keys) {
                delete(key);
            }
        } finally {
            readLock.unlock();
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
        Options options = new Options();
        options.setCreateIfMissing(true);
        try {
            rocksDB = RocksDB.open(options, path);
            RocksIterator rocksIterator = rocksDB.newIterator();
            for (rocksIterator.seekToFirst(); rocksIterator.isValid(); rocksIterator.next()) {
                result.add(rocksIterator.key());
            }
            return result;
        } catch (RocksDBException e) {
            throw new KvStorageException(ErrorCode.KVStorageReadError, e);
        }
    }

    @Override
    public void shutdown() {
    }
}
