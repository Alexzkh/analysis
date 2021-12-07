package com.zqykj.infrastructure.util.hashing;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
interface StringHash {
    long longHash(String s, LongHashFunction hashFunction, int off, int len);
    void hash(String s, LongTupleHashFunction hashFunction, int off, int len, long[] result);
}
