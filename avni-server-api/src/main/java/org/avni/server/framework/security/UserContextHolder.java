package org.avni.server.framework.security;

import org.avni.server.domain.Organisation;
import org.avni.server.domain.User;
import org.avni.server.domain.UserContext;

public class UserContextHolder {
    private static final ThreadLocal<UserContext> userContext = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void create(UserContext context) {
        userContext.set(context);
    }

    public static UserContext getUserContext() {
        return userContext.get();
    }

    public static void clear() {
        userContext.remove();
    }

    // Any child entity of user not eagerly loaded may give no session error
    public static User getUser() {
        UserContext context = getUserContext();
        return context != null ? context.getUser() : null;
    }

    public static Long getUserId() {
        User user = getUser();
        return user == null ? null : user.getId();
    }

    // Any child entity of organisation not eagerly loaded may give no session error
    public static Organisation getOrganisation() {
        UserContext context = getUserContext();
        return context != null ? context.getOrganisation() : null;
    }

    public static String getUserName() {
        User user = UserContextHolder.getUser();
        if (user == null) return null;
        return user.getUsername();
    }
}
