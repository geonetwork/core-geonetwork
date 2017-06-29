package org.fao.geonet.harvester.wfsfeatures;

import io.searchbox.action.AbstractMultiTypeActionBuilder;
import io.searchbox.action.GenericResultAbstractAction;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Delete By Query API  is back in Elasticsearch version 5.0.
 * You need to install the plugin with the same name for this action to work.
 *
 * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/5.0/docs-delete-by-query.html">Delete By Query API</a>
 */
public class DeleteByQuery extends GenericResultAbstractAction {

    protected DeleteByQuery(DeleteByQuery.Builder builder) {
        super(builder);

        this.payload = builder.query;
        setURI(buildURI());
    }

    @Override
    protected String buildURI() {
        return super.buildURI() + "/_delete_by_query";
    }

    @Override
    public String getPathToResult() {
        return "ok";
    }

    @Override
    public String getRestMethodName() {
        return "POST";
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .appendSuper(super.hashCode())
            .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        return new EqualsBuilder()
            .appendSuper(super.equals(obj))
            .isEquals();
    }

    public static class Builder extends AbstractMultiTypeActionBuilder<DeleteByQuery, DeleteByQuery.Builder> {

        private String query;

        public Builder(String query) {
            this.query = query;
        }

        @Override
        public DeleteByQuery build() {
            return new DeleteByQuery(this);
        }
    }

}
