package org.fao.geonet.client;

public class RemoteHarvesterApiClientException extends Exception {
    public RemoteHarvesterApiClientException(String message) {
        super(message);
    }

    public RemoteHarvesterApiClientException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
