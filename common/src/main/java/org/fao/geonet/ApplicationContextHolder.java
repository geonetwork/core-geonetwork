package org.fao.geonet;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * Holds the application context in a thread local.
 *
 * User: Jesse
 * Date: 11/26/13
 * Time: 11:04 AM
 */
public class ApplicationContextHolder {
    private static InheritableThreadLocal<ConfigurableApplicationContext> holder = new InheritableThreadLocal<ConfigurableApplicationContext>();

    public static ConfigurableApplicationContext get() {
        return holder.get();
    }

    public static void set(ConfigurableApplicationContext context) {
        holder.set(context);
    }

    public static void clear() {
        holder.remove();
    }

}
