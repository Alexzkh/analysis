/**
 * @作者 Mcj
 */
package com.zqykj.core;


import com.zqykj.domain.Page;

public interface ScoredPage<T> extends Page<T> {

    float getMaxScore();
}
