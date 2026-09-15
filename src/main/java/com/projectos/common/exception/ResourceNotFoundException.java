package com.projectos.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    private ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entityName, Long id) {
        return new ResourceNotFoundException(entityName + " with id " + id + " not found");
    }

    public static ResourceNotFoundException of(String entityName, Long id, String parentName, Long parentId) {
        return new ResourceNotFoundException(
                entityName + " with id " + id + " not found in " + parentName + " with id " + parentId);
    }
}
