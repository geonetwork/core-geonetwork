package org.fao.geonet.translations.libretranslate;

public class LibreTranslateClientException extends Exception{
    public LibreTranslateClientException(String message) {
        super(message);
    }

    public LibreTranslateClientException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public LibreTranslateClientException(Throwable cause) {
        super(cause);
    }
}
