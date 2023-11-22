package com.divtech.employee_management.constant;

public enum Role {
    USER("user"),
    ADMIN("admin");

    final String roleName;

    Role(final String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

}
