package jeeves.monitor;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ResourceTracker {

    /**
     * This collection tracks the threads that obtain a resource so that
     * if a resource leak is detected the Exceptions can be used to
     * identify the call sites of the non-closed resources.
     *
     * For performance this tracking is only done if the the
     * Log.Dbms.RESOURCE_TRACKING is set to DEBUG.
     */
    private final Multimap<Object, Exception> resourceAccessTracker = LinkedHashMultimap.create();
    private final Multimap<Object, Exception> directOpenResourceAccessTracker = LinkedHashMultimap.create();
    private static final Lock trackerLock = new ReentrantLock(false);

    private void open(Object resource, Multimap<Object, Exception> tracker) {

    }
    /**
     * Get a copy of the resources that were opened in 'direct' mode.  If
     * Log.Dbms.RESOURCE_TRACKING is in debug mode there will be exceptions
     * as the values of the returned Multimap.  This is for debugging only.
     *
     * The resource should not be used since they could be closed at anypoint.
     *
     * The exception are intended to give an idea of what opened the resources to
     * track down resource links.
     *
     * NEVER use the resource obtained through this method.
     *
     */
    public void openResource(Object resource) {
    	open(resource, resourceAccessTracker);
    }
    /**
     * Get a copy of the resources that were opened in 'normal'/'tracked' mode.  If
     * Log.Dbms.RESOURCE_TRACKING is in debug mode there will be exceptions
     * as the values of the returned Multimap.  This is for debugging only.
     *
     * The resource should not be used since they could be closed at anypoint.
     *
     * The exception are intended to give an idea of what opened the resources to
     * track down resource links.
     *
     * NEVER use the resource obtained through this method.
     *
     */
    public void openDirectResource(Object resource) {
    	open(resource, directOpenResourceAccessTracker);
    }
    
    private LinkedHashMultimap<Object, Exception> get(Multimap<Object, Exception> tracker) {
    	try {
            trackerLock.lock();
            return LinkedHashMultimap.create(tracker);
        } finally {
        	trackerLock.unlock();
        }
    }
	public LinkedHashMultimap<Object,Exception> getDirectOpenResources() {
		return get(directOpenResourceAccessTracker);
	}
	public Multimap<Object, Exception> getOpenResources() {
		return get(resourceAccessTracker);
	}
	public void removeAll(Object resource) {
        try {
            trackerLock.lock();
            resourceAccessTracker.removeAll(resource);
            directOpenResourceAccessTracker.removeAll(resource);
        } finally {
            trackerLock.unlock();
        }		
	}
	/**
	 * remove all resources so there are no memory leaks if the servlet is unregistered
	 */
    public void clean() {
        try {
            trackerLock.lock();
            resourceAccessTracker.clear();
            directOpenResourceAccessTracker.clear();
        } finally {
            trackerLock.unlock();
        }
    }
}
