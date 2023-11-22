package com.divtech.employee_management.util;


public class NotFoundException extends RuntimeException {

    public NotFoundException() {
        super();
    }

    public NotFoundException(final String message) {
        super(message);
    }


    public NotFoundException(final String objectName, final long id) {
        super(objectName + " not found with ID: " + id);
    }

}
