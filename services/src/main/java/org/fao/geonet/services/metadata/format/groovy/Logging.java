package org.fao.geonet.services.metadata.format.groovy;

import groovy.util.slurpersupport.GPathResult;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;

import java.util.Collection;
import java.util.Iterator;

/**
 * Methods for logging the handling of elements in this formatter.
 *
 * @author Jesse on 10/24/2014.
 */
public class Logging {

    static void debug(String message, GPathResult elem, Object... objects) {
        if (isDebugMode()) {
            Object[] objs = new Object[objects.length + 1];
            System.arraycopy(objects, 0, objs, 1, objects.length);
            objs[0] = "'" + createPath(elem) + "'";
            debug(String.format(message, objs));
        }
    }
    static void debug(String message, Collection<GPathResult> elem, Object... objects) {
        if (isDebugMode()) {
            Object[] objs = new Object[objects.length + 1];
            System.arraycopy(objects, 0, objs, 1, objects.length);
            StringBuilder builder = new StringBuilder();
            if (elem.isEmpty()) {
                builder.append("No Paths");
            } else {
                final Iterator<GPathResult> iterator = elem.iterator();
                GPathResult first = iterator.next();
                builder.append(createPath(first));
                while (iterator.hasNext()) {
                    GPathResult next = iterator.next();
                    builder.append(",").append(next.name());
                }
            }
            objs[0] = "'" + builder + "'";
            debug(String.format(message, objs));
        }
    }

    public static boolean isDebugMode() {
        return Log.isDebugEnabled(Geonet.FORMATTER);
    }

    public static String createPath(GPathResult element) {
        if (element == null) {
            return "null";
        }
        StringBuilder pathBuilder = new StringBuilder();
        Handler.createPath(element, pathBuilder);
        String rootPath = "";
        TransformationContext context = TransformationContext.getContext();
        if (context != null) {
            rootPath = context.getRootPath() + ">";
        }
        return rootPath + pathBuilder.toString();
    }

    static void debug(String message) {
        if (isDebugMode()) {
            Log.debug(Geonet.FORMATTER, message);
        }
    }
}
