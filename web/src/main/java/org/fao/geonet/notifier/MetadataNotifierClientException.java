package org.fao.geonet.notifier;


public class MetadataNotifierClientException extends Exception {

	private static final long serialVersionUID = 1335641171109181321L;

	public MetadataNotifierClientException(Throwable e) {
		super(e);
	}

	public MetadataNotifierClientException(String newMessage) {
		super(newMessage);
	}

	public MetadataNotifierClientException(String msg, Throwable e) {
		super(msg, e);
	}
}
