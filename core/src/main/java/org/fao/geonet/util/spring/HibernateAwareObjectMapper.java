package org.fao.geonet.util.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

/**
 * Object Mapper to support JSON serialization and deserialization of Hibernate (http://hibernate.org)
 * specific datatypes and properties; especially lazy-loading aspects.
 *
 * See https://github.com/FasterXML/jackson-datatype-hibernate.
 *
 */
public class HibernateAwareObjectMapper extends ObjectMapper {

    public HibernateAwareObjectMapper() {
        Hibernate5Module hbm = new Hibernate5Module();
        hbm.enable(Hibernate5Module.Feature.FORCE_LAZY_LOADING);

        registerModule(hbm);
    }
}
