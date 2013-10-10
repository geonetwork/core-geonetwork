package org.fao.geonet.kernel;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.User;
import org.fao.geonet.utils.Log;

import java.util.List;

/**
 * A runnable for indexing one or more metadata.
 */
final class IndexMetadataTask implements Runnable {

    private final ServiceContext context;
    private final List<String> ids;
    private int beginIndex;
    private int count;
    private User user;
    private DataManager dataManager;

    IndexMetadataTask(DataManager dataManager, ServiceContext context, List<String> ids) {
        this.dataManager = dataManager;
        synchronized (dataManager.indexing) {
            dataManager.indexing.add(this);
        }

        this.context = context;
        this.ids = ids;
        this.beginIndex = 0;
        this.count = ids.size();
        if(context.getUserSession() != null) {
            this.user = context.getUserSession().getPrincipal();
        }
    }
    IndexMetadataTask(DataManager dataManager, ServiceContext context, List<String> ids, int beginIndex, int count) {
        this.dataManager = dataManager;
        synchronized (dataManager.indexing) {
            dataManager.indexing.add(this);
        }

        this.context = context;
        this.ids = ids;
        this.beginIndex = beginIndex;
        this.count = count;
        if(context.getUserSession() != null) {
            this.user = context.getUserSession().getPrincipal();
        }
    }

    /**
     * TODO javadoc.
     */
    public void run() {
        context.setAsThreadLocal();
        try {
            // poll context to see whether servlet is up yet
            while (!context.isServletInitialized()) {
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "Waiting for servlet to finish initializing..");
                try {
                    Thread.sleep(10000); // sleep 10 seconds
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                if (ids.size() > 1) {
                    // servlet up so safe to index all metadata that needs indexing
                    for (int i = beginIndex; i < beginIndex + count; i++) {
                        try {
                            dataManager.indexMetadata(ids.get(i).toString());
                        } catch (Exception e) {
                            Log.error(Geonet.INDEX_ENGINE, "Error indexing metadata '" + ids.get(i) + "': " + e.getMessage()
                                                           + "\n" + Util.getStackTrace(e));
                        }
                    }
                } else {
                    dataManager.indexMetadata(ids.get(0));
                }
            } catch (Exception e) {
                Log.error(Geonet.DATA_MANAGER, "Reindexing thread threw exception");
                e.printStackTrace();
            }
            if (user != null && context.getUserSession().getUserId() == null) {
                context.getUserSession().loginAs(user);
            }
        }  finally {
            synchronized (dataManager.indexing) {
                dataManager.indexing.remove(this);
            }
        }
    }
}
