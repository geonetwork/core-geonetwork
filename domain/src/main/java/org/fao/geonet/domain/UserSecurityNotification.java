package org.fao.geonet.domain;

public enum UserSecurityNotification {
    /**
     * Indicates the the hash is in need up an update.  Could be because the database has been upgraded and has an old
     * version of the hashcode or a new version or salt has been created.
     */
    UPDATE_HASH_REQUIRED,
    /**
     * Notification that database has an unrecognized notification.  This is an error and the System administrator should
     * review the users table.
     */
    UNKNOWN;

    /**
     * Look up the notification or return the unknown notification if it is not found.
     *
     * @param notificationName the name of the notification to look up.
     * @return
     */
    public static UserSecurityNotification find(String notificationName) {
        for (UserSecurityNotification notification : values()) {
            if (notification.toString().equalsIgnoreCase(notificationName)) {
                return notification;
            }
        }
        return UNKNOWN;
    }
}
