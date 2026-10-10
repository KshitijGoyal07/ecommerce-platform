package com.ecommerce.model;

/** Describes the workspace available to a particular user role. */
public interface DashboardAccess {
    String getDashboardPath();

    String getWorkspaceDescription();
}
