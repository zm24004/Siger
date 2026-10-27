package com.siger.security;

public final class Permisos {

    public static final String USERS_READ = "users:read";
    public static final String USERS_CREATE = "users:create";
    public static final String USERS_UPDATE = "users:update";
    public static final String USERS_STATE = "users:state";
    public static final String USERS_DELETE = "users:delete";
    public static final String ROLES_READ = "roles:read";
    public static final String ROLES_UPDATE = "roles:update";
    public static final String EVENTS_MANAGE = "events:manage";
    public static final String LOCATIONS_READ = "locations:read";
    public static final String LOCATIONS_CREATE = "locations:create";
    public static final String LOCATIONS_UPDATE = "locations:update";
    public static final String LOCATIONS_DELETE = "locations:delete";
    public static final String RESOURCES_READ = "resources:read";
    public static final String RESOURCES_CREATE = "resources:create";
    public static final String RESOURCES_UPDATE = "resources:update";
    public static final String RESOURCES_DELETE = "resources:delete";
    public static final String ATTENDANCE_REGISTER = "attendance:register";
    public static final String ATTENDANCE_READ = "attendance:read";
    public static final String ATTENDANCE_UPDATE = "attendance:update";
    public static final String RESERVATIONS_CREATE = "reservations:create";
    public static final String RESERVATIONS_READ_OWN = "reservations:read-own";
    public static final String RESERVATIONS_MANAGE_OWN = "reservations:manage-own";
    public static final String RESERVATIONS_READ_EVENT = "reservations:read-event";
    public static final String RESERVATIONS_READ_ALL = "reservations:read-all";
    public static final String RESERVATIONS_MANAGE_ALL = "reservations:manage-all";

    private Permisos() {
    }
}
