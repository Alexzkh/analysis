package com.zqykj.infrastructure.spi;

/**
 * service loader exception.
 *
 */
public class ServiceLoaderException extends RuntimeException {
    
    private static final long serialVersionUID = -4133484884875183141L;
    
    private final Class<?> clazz;
    
    public ServiceLoaderException(Class<?> clazz, Exception caused) {
        super(String.format("Can not load class `%s` by SPI ", clazz.getName()), caused);
        this.clazz = clazz;
    }
    
    public Class<?> getClazz() {
        return clazz;
    }
}
