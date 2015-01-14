package jeeves;

import java.util.ArrayList;
import java.util.List;

/**
 * A factory for creating thread local object which registers references to the thread locals and sets them to null
 * at the start of a new web request to ensure they stay clean between requests.
 *
 * All threadlocal objects should be created using this class and this class should be called for each web request.
 *
 * Created by Jesse on 1/14/2015.
 */
public class ThreadLocalCleaner {
    private final List<ThreadLocal<?>> createdThreadLocals = new ArrayList<>();

    public <T> ThreadLocal<T> createInheritableThreadLocal(Class<T> type) {
        InheritableThreadLocal<T> threadLocal = new InheritableThreadLocal<>();
        this.createdThreadLocals.add(threadLocal);
        return threadLocal;
    }

    public void webRequestStarting() {
        for (ThreadLocal<?> threadLocal : this.createdThreadLocals) {
            threadLocal.set(null);
        }
    }
}
