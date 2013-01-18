package jeeves;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JeevesCacheManager {

    private static final String TEN_SECOND_KEY = "JeevesCacheManagerTenSeconds";
    private static final String ETERNAL_KEY = "JeevesCacheManagerETERNAL";

    private static Map<String, Lock> locks = new HashMap<String, Lock>();
    
    /**
     * Looks in a very short term cache for the item if it is no longer in cache then loads the value from the loader and
     * caches the result in the cache.
     * 
     * This is very useful for data that has to be very frequently accessed but perhaps a small delay in updates is acceptable.
     * 
     * For example translations of groups from the database are likely acceptable to be eventually consistent.
     * 
     * @param key the key to use for looking up the object
     * @param loader an object for loading the data if the data has expired from the cache already
     */
    public static <V> V findInTenSecondCache(String key, Callable<V> loader) throws Exception {
        return find(TEN_SECOND_KEY, key, loader);
    }
    public static <V> V findInEternalCache(String key, Callable<V> loader) throws Exception {
        return find(ETERNAL_KEY, key, loader);
    }
    private static <V> V find(String cacheName, String key, Callable<V> loader) throws Exception {
        Lock lock = getLock(key);
        try {
            lock.tryLock(30, TimeUnit.SECONDS);
            JeevesJCS cache = JeevesJCS.getInstance(cacheName);
            
            @SuppressWarnings("unchecked")
            V value = (V) cache.get(key);
            
            if (value == null) {
                value = loader.call();
                cache.put(key, value);
            }
            return value;
        } finally {
            lock.unlock();
        }
    }

    private static synchronized Lock getLock(String key) {
        Lock lock = locks.get(key);
        if (lock==null) {
            lock = new ReentrantLock();
            locks.put(key, lock);
        }
        return lock;
    }
}
