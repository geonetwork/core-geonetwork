package org.fao.geonet;

public interface Constants {
    String ENCODING = System.getProperty("geonetwork.file.encoding", "UTF-8");
    String ERROR = "error";
    String XML_CATALOG_FILES = "jeeves.xml.catalog.files";
    String XML_CATALOG_VERBOSITY = "jeeves.xml.catalog.verbosity";
    String XML_CATALOG_BLANKXSLFILE = "jeeves.xml.catalog.blankxslfile";
    interface BeanId {
        /**
         * Bean Id of the string representing the nodeId of the current application context.
         */
        String NODE_ID_BEAN_ID = "NODE_ID_BEAN_ID";
        /**
         * The bean id of the boolean that indicates if this context is the default context.
         */
        String IS_DEFAULT_CONTEXT_BEAN_ID = "IS_DEFAULT_CONTEXT_BEAN";

    }
}
