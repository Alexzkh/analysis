package com.zqykj.infrastructure.storage;

import com.zqykj.infrastructure.storage.kv.FileKvStorage;
import com.zqykj.infrastructure.storage.kv.KvStorage;
import com.zqykj.infrastructure.storage.kv.MemoryKvStorage;
import com.zqykj.infrastructure.storage.kv.RocksDBKvStorage;

/**
 * Key-value Storage factory.
 */
public final class StorageFactory {

    /**
     * Create {@link KvStorage} implementation.
     *
     * @param type    type of {@link KvStorage}
     * @param label   label for {@code RocksStorage}
     * @param baseDir base dir of storage file.
     * @return implementation of {@link KvStorage}
     * @throws Exception exception during creating {@link KvStorage}
     */
    public static KvStorage createKvStorage(KvStorage.KvType type, final String label, final String baseDir)
            throws Exception {
        switch (type) {
            case File:
                return new FileKvStorage(baseDir);
            case Memory:
                return new MemoryKvStorage();
            case RocksDB:
                return new RocksDBKvStorage(baseDir);
            default:
                throw new IllegalArgumentException("this kv type : [" + type.name() + "] not support");
        }
    }

}
