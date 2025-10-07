package com.SleepUp.SU.user.role;

public enum Role {
    USER,
    ADMIN;

    public String getRoleName() {
        return "ROLE_" + this.name();
    }
}