    package org.fao.geonet.kernel.datamanager;

import java.util.List;

import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.utils.Xml.ErrorHandler;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

public interface IMetadataValidator {

        public void init(ServiceContext context, Boolean force) throws Exception;

        void validateMetadata(String schema, Element xml, ServiceContext context, String fileName) throws Exception;

        void setNamespacePrefix(Element md);

        void validate(String schema, Document doc) throws Exception;

        void validate(String schema, Element md) throws Exception;

        Element validateInfo(String schema, Element md, ErrorHandler eh) throws Exception;

        Element doSchemaTronForEditor(String schema, Element md, String lang) throws Exception;

        boolean doValidate(String schema, String metadataId, Document doc, String lang);

        Pair<Element, String> doValidate(UserSession session, String schema, String metadataId, Element md, String lang, boolean forEditing)
                throws Exception;

        Element applyCustomSchematronRules(String schema, int metadataId, Element md, String lang, List<MetadataValidation> validations);

        boolean validate(Element xml);

        void setNamespacePrefix(Element md, Namespace ns);

        void setMetadataManager(IMetadataManager metadataManager);
}

  