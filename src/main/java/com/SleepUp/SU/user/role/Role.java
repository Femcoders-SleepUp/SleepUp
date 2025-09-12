package com.SleepUp.SU.user.role;

public enum Role {
    USER,
    ADMIN;

    public String getName() {
        return "ROLE_" + this.name();
    }
}

