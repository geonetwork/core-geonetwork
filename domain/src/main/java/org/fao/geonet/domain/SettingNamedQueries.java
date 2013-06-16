package org.fao.geonet.domain;

public final class SettingNamedQueries {
    public static final class QUERY_FIND_CHILDREN_BY_NAME {
        public static final String NAME = "findChildrenByName";
        public static final String PARAMETER_NAME = "name";
        public static final String PARAMETER_PARENTID = "parentid";
        static final String QUERY = "select s from Setting s where s.parent.id = :" + PARAMETER_PARENTID + " and s.name = :"
                + PARAMETER_NAME;
    }

    public static final class QUERY_FIND_ALL_CHILDREN {
        public static final String NAME = "findAllChildren";
        public static final String PARAMETER_PARENTID = QUERY_FIND_CHILDREN_BY_NAME.PARAMETER_PARENTID;
        static final String QUERY = "select s from Setting s where s.parent.id = :" + PARAMETER_PARENTID;
    }

    public static final class QUERY_FIND_ROOT {
        public static final String NAME = "findRoot";
        static final String QUERY = "select s from Setting s where s.parent.id IS NULL";
    }

    private SettingNamedQueries() {
        // disallow instantiation
    }
}
