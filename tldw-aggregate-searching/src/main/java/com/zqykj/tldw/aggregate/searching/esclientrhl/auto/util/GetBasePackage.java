package com.zqykj.tldw.aggregate.searching.esclientrhl.auto.util;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Get base package list .
 **/
public class GetBasePackage {
    // Cache entity paths
    private static Map<Class, List<String>> entityPathsMap = null;

    static {
        entityPathsMap = new HashMap<>();
    }


    private Class<? extends Annotation> annotation;

    public GetBasePackage(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    /**
     * Get the path of the repository. If not, get the main startup path
     *
     * @param annotationMetadata
     * @return
     */
    public Stream<String> getBasePackage(AnnotationMetadata annotationMetadata) {
        Map<String, Object> annotationAttributes = annotationMetadata.getAnnotationAttributes(annotation.getName());
        AnnotationAttributes attributes = new AnnotationAttributes(annotationAttributes);
        EnableESTools.gainAnnoInfo(attributes);
        String[] value = EnableESTools.getValue();//annotationg中的注解
        String[] basePackages = EnableESTools.getBasePackages();//annotationg中的注解
        String[] entityPaths = EnableESTools.getEntityPath();//annotationg中的注解
        // No annotation parameter
        if (value.length == 0 && basePackages.length == 0) {
            String className = annotationMetadata.getClassName();
            return Stream.of(ClassUtils.getPackageName(className));
        }
        return Stream.of(Arrays.asList(value), Arrays.asList(basePackages), Arrays.asList(entityPaths)).flatMap(list -> list.stream());
    }

    /**
     * Get the path of the entity class, if not, take the main startup path
     *
     * @param annotationMetadata
     * @return
     */
    public Stream<String> getEntityPackage(AnnotationMetadata annotationMetadata) {
        // cache entity path .
        if (entityPathsMap.containsKey(annotation)) {
            return Stream.of(entityPathsMap.get(annotation)).flatMap(list -> list.stream());
        }
        Map<String, Object> annotationAttributes = annotationMetadata.getAnnotationAttributes(annotation.getName());
        AnnotationAttributes attributes = new AnnotationAttributes(annotationAttributes);
        String[] entityPaths = attributes.getStringArray("entityPath");//annotationg中的注解
        if (entityPaths.length == 0) {
            String className = annotationMetadata.getClassName();
            entityPathsMap.put(annotation, Arrays.asList(ClassUtils.getPackageName(className)));
            return Stream.of(ClassUtils.getPackageName(className));
        }
        entityPathsMap.put(annotation, Arrays.asList(entityPaths));
        return Stream.of(Arrays.asList(entityPaths)).flatMap(list -> list.stream());
    }

    public static Map<Class, List<String>> getEntityPathsMap() {
        return entityPathsMap;
    }
}
