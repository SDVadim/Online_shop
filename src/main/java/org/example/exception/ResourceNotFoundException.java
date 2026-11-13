package org.example.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(Resource resource, Long id) {
        super(resource + " with id: " + id + " not found");
    }

    public enum Resource {
        CATEGORY,
        PRODUCT,
        USER;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }
}