package com.zqykj.tldw.aggregate.searching.esclientrhl.enums;

/**
 * analyzer type
 **/
public enum Analyzer {
    /**
     *<p>
     *The method that supports Chinese is word segmentation.
     *It converts the lexical unit to lowercase and removes stop words and punctuation.
     *</p>
     **/
    standard,

    /**
     *<p>
     *First, the text information is divided by non alphabetic characters, and then the lexical unit is unified into lowercase form.
     *The parser removes characters of numeric type.
     *</p>
     **/
    simple,

    /**
     *<p>
     *Just remove the space, the character does not lowcase, does not support Chinese.
     *</p>
     **/
    whitespace,//仅仅是去除空格，对字符没有lowcase化,不支持中文

    /**
     *<p>
     *Stop analyzer's function surpasses that of simple analyzer.
     *On the basis of simpleanalyzer, it adds and removes common English words (such as the, a, etc.)
     *</p>
     **/
    stop,
    keyword,
    pattern,
    fingerprint,

    /**
     *
     *language analyzer (English)
     *
     **/
    english,


    /**
     *smart chinese ik analyzer
     * @see {https://github.com/medcl/elasticsearch-analysis-ik/}
     *
     **/
    ik_smart,//ik中文智能分词 https://github.com/medcl/elasticsearch-analysis-ik/

    /**
     * chinese ik analyzer
     * @see {https://github.com/medcl/elasticsearch-analysis-ik/}
     *
     **/
    ik_max_word,//ik中文分词
}
