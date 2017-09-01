    package org.fao.geonet.kernel.datamanager;

import java.nio.file.Path;
import java.util.Set;

import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.exceptions.SchemaMatchConflictException;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.jdom.Element;

import jeeves.server.context.ServiceContext;

public interface IMetadataSchemaUtils {

        public void init(ServiceContext context, Boolean force) throws Exception;

        Path getSchemaDir(String name);

        boolean existsSchema(String name);

        Set<String> getSchemas();

        MetadataSchema getSchema(String name);

        String getMetadataSchema(String id) throws Exception;

        String autodetectSchema(Element md) throws SchemaMatchConflictException, NoSchemaMatchesException;

        String autodetectSchema(Element md, String defaultSchema) throws SchemaMatchConflictException, NoSchemaMatchesException;
}

  