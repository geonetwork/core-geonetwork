package org.fao.geonet.kernel.reusable;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;

import org.apache.lucene.document.Document;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.util.ISODate;
import org.jdom.Element;

/**
 * Result from {@link Utils#getReferencingMetadata(jeeves.resources.dbms.Dbms, String, boolean)}
 * 
 * @author jeichar
 */
public class MetadataRecord
{
    /**
     * Metadata id
     */
    public final String  id;
    /**
     * Owner of metadata
     */
    public final String  ownerId;
    /**
     * xml data string. May be null if loadMetadata in
     * {@link Utils#getReferencingMetadata(jeeves.resources.dbms.Dbms, String, boolean)} is false
     */
    public final String data;

    /**
     * xml. May be null if loadMetadata in
     * {@link Utils#getReferencingMetadata(jeeves.resources.dbms.Dbms, String, boolean)} is false
     */
    public final Element xml;

    public final List<String> xlinks;
    private String ownerEmail = null;
    private String ownerName = null;
    private XmlSerializer xmlSerializer;

    public MetadataRecord(XmlSerializer xmlSerializer, Document element, List<String> xlinks, Dbms dbms, boolean loadMetadata) throws Exception
    {
        this.xmlSerializer = xmlSerializer;
        id = element.get("_id");
        ownerId = element.get("_owner");
        this.xlinks = Collections.unmodifiableList(xlinks);
        if(loadMetadata) {
            data = ((Element) dbms.select("select data from metadata where id="+id).getChildren().get(0)).getChildTextTrim("data");
            xml = jeeves.utils.Xml.loadString(data, false);
        } else {
            data = null;
            xml = null;
        }
    }

    public void commit(Dbms dbms, ServiceContext srvContext) throws Exception
    {
        xmlSerializer.update(dbms, id, xml, new ISODate().toString(), true, srvContext);
    }

    public String email(Dbms dbms) throws SQLException {
        loadOwnerInfo(dbms);
        return ownerEmail;
    }

    public String name(Dbms dbms) throws SQLException {
        loadOwnerInfo(dbms);
        return ownerName;
    }

    private void loadOwnerInfo(Dbms dbms) throws SQLException {
        if(ownerEmail == null) {
            String query = "SELECT email,name FROM Users WHERE id=" + ownerId;
            Element emailRecord = dbms.select(query);
            if(emailRecord.getChildren().size() == 0) {
                ownerEmail = "";
                ownerName = "";
            } else {
                Element owner = (Element) emailRecord.getChildren().get(0);
                ownerEmail = owner.getChildTextNormalize("email");
                ownerName = owner.getChildTextNormalize("name");
            }
        }
    }
}