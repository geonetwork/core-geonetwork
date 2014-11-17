package org.fao.geonet.guiservices.versioning;

import java.util.List;

public final class MetadataActionListSingleton {
    private static volatile MetadataActionListSingleton unique;
    private volatile List<MetadataAction> metadataActions;

    private MetadataActionListSingleton() {
    }

    public static MetadataActionListSingleton getInstance() {
        if (unique == null) {
            synchronized (MetadataActionListSingleton.class) {
                if (unique == null) {
                    unique = new MetadataActionListSingleton();
                }
            }
        }
        return unique;
    }

    public synchronized List<MetadataAction> getMetadataActions() {
        return metadataActions;
    }

    public synchronized void setMetadataActions(final List<MetadataAction> mAList) {
        this.metadataActions = mAList;
    }
}
