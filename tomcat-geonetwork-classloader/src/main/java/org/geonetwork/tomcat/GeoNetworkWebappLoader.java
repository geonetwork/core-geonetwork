package org.geonetwork.tomcat;

import org.apache.catalina.loader.WebappClassLoader;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * GeoNetwork custom class loader for Tomcat to prioritize local Apache Xerces from WEB-INF over the version in
 * Tomcat endorsed folder to avoid issues with xml-resolver.
 *
 * Other solution, that doesn't require a custom class loader, would be to copy xml-resolver in Tomcat endorsed folder,
 * but this seem not an option for ESTAT project.
 *
 * The library should be copy in tomcat/lib folder and requires defining a context file for GeoNetwork.
 * This can be included in META-INF folder of GeoNetwork for convenience:
 *
 * <Context>
 *   <Loader loaderClass="org.geonetwork.tomcat.GeoNetworkWebappLoader"
 *     useSystemClassLoaderAsParent="false"
 *     delegate="false"/>
 * </Context>
 *
 * @author Jose García
 *
 */
public class GeoNetworkWebappLoader extends WebappClassLoader {
    private Set<String> localLibraries = new HashSet<String>();

    private static final org.apache.juli.logging.Log log=
            org.apache.juli.logging.LogFactory.getLog( GeoNetworkWebappLoader.class );

    public GeoNetworkWebappLoader() {
        super();

        if (log.isDebugEnabled())
            log.debug("  GeoNetworkWebappLoader class loader ");

        localLibraries.add("org.apache.xerces");
    }

    public GeoNetworkWebappLoader(ClassLoader parent) {
        super(parent);

        if (log.isDebugEnabled())
            log.debug("  GeoNetworkWebappLoader class loader ");

        localLibraries.add("org.apache.xerces");
    }

    @Override
    public synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {

        if (log.isDebugEnabled())
            log.debug("loadClass(" + name + ", " + resolve + ")");
        Class<?> clazz = null;

        // Log access to stopped classloader
        if (!started) {
            try {
                throw new IllegalStateException();
            } catch (IllegalStateException e) {
                log.info(sm.getString("webappClassLoader.stopped", name), e);
            }
        }

        // (0) Check our previously loaded local class cache
        clazz = findLoadedClass0(name);
        if (clazz != null) {
            if (log.isDebugEnabled())
                log.debug("  Returning class from cache");
            if (resolve)
                resolveClass(clazz);
            return (clazz);
        }

        // (0.1) Check our previously loaded class cache
        clazz = findLoadedClass(name);
        if (clazz != null) {
            if (log.isDebugEnabled())
                log.debug("  Returning class from cache");
            if (resolve)
                resolveClass(clazz);
            return (clazz);
        }

        // (0.2) Try loading the class with the system class loader, to prevent
        //       the webapp from overriding J2SE classes
        // If preference to load from local, ignore the system class loader for now
        boolean loadFromLocal = isLocalLibrary(name);

        if (!loadFromLocal) {
            try {
                clazz = system.loadClass(name);
                if (clazz != null) {
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }

        // (0.5) Permission to access this class when using a SecurityManager
        if (securityManager != null) {
            int i = name.lastIndexOf('.');
            if (i >= 0) {
                try {
                    securityManager.checkPackageAccess(name.substring(0,i));
                } catch (SecurityException se) {
                    String error = "Security Violation, attempt to use " +
                            "Restricted Class: " + name;
                    log.info(error, se);
                    throw new ClassNotFoundException(error, se);
                }
            }
        }

        boolean delegateLoad = delegate || filter(name);

        // (1) Delegate to our parent if requested
        if (delegateLoad) {
            if (log.isDebugEnabled())
                log.debug("  Delegating to parent classloader1 " + parent);
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            try {
                clazz = Class.forName(name, false, loader);
                if (clazz != null) {
                    if (log.isDebugEnabled())
                        log.debug("  Loading class from parent");
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }

        // (2) Search local repositories
        if (log.isDebugEnabled())
            log.debug("  Searching local repositories");
        try {
            clazz = findClass(name);
            if (clazz != null) {
                if (log.isDebugEnabled())
                    log.debug("  Loading class from local repository");
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
        } catch (ClassNotFoundException e) {
            // Ignore
        }

        // (3) Delegate to parent unconditionally
        if (!delegateLoad) {
            if (log.isDebugEnabled())
                log.debug("  Delegating to parent classloader at end: " + parent);
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            try {
                clazz = Class.forName(name, false, loader);
                if (clazz != null) {
                    if (log.isDebugEnabled())
                        log.debug("  Loading class from parent");
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }


        // (0.2) Try loading the class with the system class loader, to prevent
        //       the webapp from overriding J2SE classes
        // If preference to load from local, but not available check in the system class loader
        if (loadFromLocal) {
            try {
                clazz = system.loadClass(name);
                if (clazz != null) {
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }

        throw new ClassNotFoundException(name);

    }

    private boolean isLocalLibrary(String name) {
        Iterator<String> libraryIt = localLibraries.iterator();
        while (libraryIt.hasNext()) {
            String library = libraryIt.next();

            if (name.startsWith(library)) return true;
        }

        return false;
    }
}
