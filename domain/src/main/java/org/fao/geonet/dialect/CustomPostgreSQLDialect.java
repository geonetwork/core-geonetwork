package org.fao.geonet.dialect;

import org.hibernate.dialect.PostgreSQLDialect;

@SuppressWarnings("deprecation")
public class CustomPostgreSQLDialect extends PostgreSQLDialect {

    @Override
    public String getQuerySequencesString() {
        return "SELECT "
                + "        relname "
                + "FROM "
                + "        pg_class "
                + "WHERE "
                + "        relkind='S' "
                + "AND "
                + "        pg_table_is_visible(oid)";
    }

}