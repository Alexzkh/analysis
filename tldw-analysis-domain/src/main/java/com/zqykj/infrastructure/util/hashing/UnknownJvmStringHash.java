package com.zqykj.infrastructure.util.hashing;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
enum UnknownJvmStringHash implements StringHash {
    INSTANCE;

    @Override
    public long longHash(String s, LongHashFunction hashFunction, int off, int len) {
        return hashFunction.hashNativeChars(s, off, len);
    }

    @Override
    public void hash(final String s, final LongTupleHashFunction hashFunction,
                    final int off, final int len, final long[] result) {
        LongTupleHashFunction.hashNativeChars(hashFunction, s, off, len, result);
    }
}
