package org.fao.geonet.jms.message.versioning;

import org.fao.geonet.jms.message.Message;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.util.Set;

/**
 * @author jose garcia
 */
public class MetadataVersioningMessage extends Message {
    public static enum VersioningAction { VERSION_MD, ADD_HISTORY, DELETE_MD};

    private Set<String> ids;
    private String metadataContent;
    private String metadataLogMessage;

    private VersioningAction action;
    private String senderClientID;


    public Set<String> getIds() {
        return ids;
    }

    public void setIds(Set<String> ids) {
        this.ids = ids;
    }

    public String getMetadataContent() {
        return metadataContent;
    }

    public void setMetadataContent(String metadataContent) {
        this.metadataContent = metadataContent;
    }

    public String getMetadataLogMessage() {
        return metadataLogMessage;
    }

    public void setMetadataLogMessage(String metadataLogMessage) {
        this.metadataLogMessage = metadataLogMessage;
    }

    public VersioningAction getAction() {
        return action;
    }

    public void setAction(VersioningAction action) {
        this.action = action;
    }

    public String getSenderClientID() {
        return senderClientID;
    }

    public void setSenderClientID(String senderClientID) {
        this.senderClientID = senderClientID;
    }

    public MetadataVersioningMessage decode(String xml) {
        XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
        MetadataVersioningMessage mdVersioningMessage = (MetadataVersioningMessage)decoder.readObject();
        decoder.close();
        return mdVersioningMessage;
    }
}