package com.zqykj.infrastructure.storage.kv;


import com.zqykj.infrastructure.exception.KvStorageException;

import java.util.List;
import java.util.Map;

/**
 * Universal KV storage interface.
 */
public interface KvStorage {

    enum KvType {
        /**
         * Local file storage.
         */
        File,

        /**
         * Local memory storage.
         */
        Memory,

        /**
         * RocksDB storage.
         */
        RocksDB,
    }


    /**
     * get data by key.
     *
     * @param key byte[]
     * @return byte[]
     * @throws KvStorageException KVStorageException
     */
    byte[] get(byte[] key) throws KvStorageException;

    /**
     * batch get by List byte[].
     *
     * @param keys List byte[]
     * @return Map byte[], byte[]
     * @throws KvStorageException KvStorageException
     */
    Map<byte[], byte[]> batchGet(List<byte[]> keys) throws KvStorageException;

    /**
     * write data.
     *
     * @param key   byte[]
     * @param value byte[]
     * @throws KvStorageException KvStorageException
     */
    void put(byte[] key, byte[] value) throws KvStorageException;

    /**
     * batch write.
     *
     * @param keys   List byte[]
     * @param values List byte[]
     * @throws KvStorageException KvStorageException
     */
    void batchPut(List<byte[]> keys, List<byte[]> values) throws KvStorageException;

    /**
     * delete with key.
     *
     * @param key byte[]
     * @throws KvStorageException KvStorageException
     */
    void delete(byte[] key) throws KvStorageException;

    /**
     * batch delete with keys.
     *
     * @param keys List byte[]
     * @throws KvStorageException KvStorageException
     */
    void batchDelete(List<byte[]> keys) throws KvStorageException;

    /**
     * do snapshot.
     *
     * @param backupPath snapshot file save path
     * @throws KvStorageException KVStorageException
     */
    void doSnapshot(final String backupPath) throws KvStorageException;

    /**
     * load snapshot.
     *
     * @param path The path to the snapshot file
     * @throws KvStorageException KVStorageException
     */
    void snapshotLoad(String path) throws KvStorageException;

    /**
     * Get all keys.
     *
     * @return all keys
     * @throws KvStorageException KVStorageException
     */
    List<byte[]> allKeys() throws KvStorageException;

    /**
     * shutdown.
     */
    void shutdown();

}
