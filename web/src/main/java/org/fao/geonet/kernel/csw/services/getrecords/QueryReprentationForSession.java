package org.fao.geonet.kernel.csw.services.getrecords;

import org.fao.geonet.util.Sha1Encoder;
import org.jdom.Element;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;

public class QueryReprentationForSession {

    private final String language;
    private final String query;
    private final String userid;

    public QueryReprentationForSession( ServiceContext context, Element filterExpr ) {
        this.language = context.getLanguage();
        this.query = Sha1Encoder.encodeString (Xml.getString(filterExpr));
        this.userid = context.getUserSession().getUserId();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((language == null) ? 0 : language.hashCode());
        result = prime * result + ((query == null) ? 0 : query.hashCode());
        result = prime * result + ((userid == null) ? 0 : userid.hashCode());
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        QueryReprentationForSession other = (QueryReprentationForSession) obj;
        if (language == null) {
            if (other.language != null) return false;
        } else if (!language.equals(other.language)) return false;
        if (query == null) {
            if (other.query != null) return false;
        } else if (!query.equals(other.query)) return false;
        if (userid == null) {
            if (other.userid != null) return false;
        } else if (!userid.equals(other.userid)) return false;
        return true;
    }
}