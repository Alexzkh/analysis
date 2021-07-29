package com.zqykj.infrastructure.storage.kv;

import com.zqykj.infrastructure.exception.ErrorCode;
import com.zqykj.infrastructure.exception.KvStorageException;
import com.zqykj.infrastructure.util.ByteUtils;
import com.zqykj.infrastructure.util.DiskUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Kv storage based on file system.
 */
public class FileKvStorage implements KvStorage {

    private final String baseDir;

    /**
     * Ensure that a consistent view exists when implementing file copies.
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();

    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public FileKvStorage(String baseDir) throws IOException {
        this.baseDir = baseDir;
        DiskUtils.forceMkdir(baseDir);
    }

    @Override
    public byte[] get(byte[] key) throws KvStorageException {
        readLock.lock();
        try {
            final String fileName = new String(key);
            File file = Paths.get(baseDir, fileName).toFile();
            if (file.exists()) {
                return DiskUtils.readFileBytes(file);
            }
            return null;
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
            final String fileName = new String(key);
            File file = Paths.get(baseDir, fileName).toFile();
            try {
                DiskUtils.touch(file);
                DiskUtils.writeFile(file, value, false);
            } catch (IOException e) {
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
            final String fileName = new String(key);
            DiskUtils.deleteFile(baseDir, fileName);
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
        writeLock.lock();
        try {
            File srcDir = Paths.get(baseDir).toFile();
            File descDir = Paths.get(backupPath).toFile();
            DiskUtils.copyDirectory(srcDir, descDir);
        } catch (IOException e) {
            throw new KvStorageException(ErrorCode.IOCopyDirError, e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void snapshotLoad(String path) throws KvStorageException {
        writeLock.lock();
        try {
            File srcDir = Paths.get(path).toFile();
            // If snapshot path is non-exist, means snapshot is empty
            if (srcDir.exists()) {
                // First clean up the local file information, before the file copy
                DiskUtils.deleteDirThenMkdir(baseDir);
                File descDir = Paths.get(baseDir).toFile();
                DiskUtils.copyDirectory(srcDir, descDir);
            }
        } catch (IOException e) {
            throw new KvStorageException(ErrorCode.IOCopyDirError, e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<byte[]> allKeys() throws KvStorageException {
        List<byte[]> result = new LinkedList<>();
        File[] files = new File(baseDir).listFiles();
        if (null != files) {
            for (File each : files) {
                if (each.isFile()) {
                    result.add(ByteUtils.toBytes(each.getName()));
                }
            }
        }
        return result;
    }

    @Override
    public void shutdown() {
    }
}