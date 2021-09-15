/**
 * @author Mcj
 */
package com.zqykj.repository.config;

/**
 * <h1> BootstrapMode </h1>
 */
public enum BootstrapMode {

    /**
     * Repository proxies are instantiated eagerly, just like any other Spring bean, except explicitly marked as lazy.
     * Thus, injection into repository clients will trigger initialization.
     */
    DEFAULT,

    /**
     * Repository bean definitions are considered lazy and clients will get repository proxies injected that will
     * initialize on first access. Repository initialization is triggered on application context bootstrap completion.
     */
    DEFERRED,

    /**
     * Repository bean definitions are considered lazy, lazily inject and only initialized on first use, i.e. the
     * application might have fully started without the repositories initialized.
     */
    LAZY
}
