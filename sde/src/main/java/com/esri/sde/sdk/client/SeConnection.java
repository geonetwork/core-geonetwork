package com.esri.sde.sdk.client;

import java.util.Vector;

public class SeConnection {

    public static int SE_TRYLOCK_POLICY = 0;
    public static /* GEOT-947 final*/ int SE_UNPROTECTED_POLICY = 0;
    public static int SE_ONE_THREAD_POLICY = 1;
    /**
     * added by heikki doeleman.
     */
    public static int SE_LOCK_POLICY = 2;

    public SeConnection(String a, int i, String b, String c, String d) {
        throw new UnsupportedOperationException("this is the dummy api");
    }

    public String getDatabaseName() throws SeException {
        return null;
    }

    public String getUser() throws SeException {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Vector getLayers() throws SeException {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Vector getRasterColumns() throws SeException {
        return null;
    }

    public SeRelease getRelease() {
        return null;
    }

    public boolean isClosed() {
        return false;
    }

    public void close() throws SeException {
    }

    public void commitTransaction() throws SeException {
    }

    public void rollbackTransaction() throws SeException {
    }

    public void setConcurrency(int i) throws SeException {
    }

    public int setTransactionAutoCommit(int i) throws SeException {
        return -1;
    }

    public void startTransaction() throws SeException {
    }

    public SeDBMSInfo getDBMSInfo() throws SeException {
        return null;
    }
}
