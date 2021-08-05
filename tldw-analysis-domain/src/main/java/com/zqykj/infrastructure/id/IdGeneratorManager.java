package com.zqykj.infrastructure.id;

import com.zqykj.infrastructure.spi.AnalysisServiceLoader;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Id generator manager.
 */
@Component
public class IdGeneratorManager {

    private final Map<String, IdGenerator> generatorMap = new ConcurrentHashMap<>();

    private final Function<String, IdGenerator> supplier;

    public IdGeneratorManager() {
        this.supplier = s -> {
            IdGenerator generator;
            Collection<IdGenerator> idGenerators = AnalysisServiceLoader.load(IdGenerator.class);
            Iterator<IdGenerator> iterator = idGenerators.iterator();
            if (iterator.hasNext()) {
                generator = iterator.next();
            } else {
                generator = new SnowFlowerIdGenerator();
            }
            generator.init();
            return generator;
        };
    }

    public void register(String resource) {
        generatorMap.computeIfAbsent(resource, s -> supplier.apply(resource));
    }

    /**
     * Register resources that need to use the ID generator.
     *
     * @param resources resource name list
     */
    public void register(String... resources) {
        for (String resource : resources) {
            generatorMap.computeIfAbsent(resource, s -> supplier.apply(resource));
        }
    }

    /**
     * request next id by resource name.
     *
     * @param resource resource name
     * @return id
     */
    public long nextId(String resource) {
        if (generatorMap.containsKey(resource)) {
            return generatorMap.get(resource).nextId();
        }
        throw new NoSuchElementException(
                "The resource is not registered with the distributed " + "ID resource for the time being.");
    }

    public Map<String, IdGenerator> getGeneratorMap() {
        return generatorMap;
    }
}
