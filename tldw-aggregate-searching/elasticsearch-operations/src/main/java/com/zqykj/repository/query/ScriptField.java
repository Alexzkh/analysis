/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import org.elasticsearch.script.Script;

/**
 * <h1> 针对Field script 处理 </h1>
 */
public class ScriptField {

    private final String fieldName;
    private final Script script;

    public ScriptField(String fieldName, Script script) {
        this.fieldName = fieldName;
        this.script = script;
    }

    public String fieldName() {
        return fieldName;
    }

    public Script script() {
        return script;
    }
}
