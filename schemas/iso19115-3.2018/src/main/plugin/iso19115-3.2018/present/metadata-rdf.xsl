<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:geonet="http://www.fao.org/geonetwork" 
  xmlns:saxon="http://saxon.sf.net/"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
  xmlns:foaf="http://xmlns.com/foaf/0.1/"
  xmlns:void="http://www.w3.org/TR/void/" 
  xmlns:dcat="http://www.w3.org/ns/dcat#"
  xmlns:dc="http://purl.org/dc/elements/1.1/" 
  xmlns:dct="http://purl.org/dc/terms/"
  xmlns:dctype="http://purl.org/dc/dcmitype/" 
  xmlns:skos="http://www.w3.org/2004/02/skos/core#"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
  xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
  xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
  xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
  xmlns:gml="http://www.opengis.net/gml/3.2" 
  xmlns:ogc="http://www.opengis.net/rdf#"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:iso19115-3.2018="http://geonetwork-opensource.org/schemas/iso19115-3.2018"
  extension-element-prefixes="saxon" exclude-result-prefixes="#all">


  <!-- TODO : add Multilingual metadata support 
    See http://www.w3.org/TR/2004/REC-rdf-syntax-grammar-20040210/#section-Syntax-languages
    
    TODO : maybe some characters may be encoded / avoid in URIs
    See http://www.w3.org/TR/2004/REC-rdf-concepts-20040210/#dfn-URI-reference
  -->

  <!-- 
    Create reference block to metadata record and dataset to be added in dcat:Catalog usually.
  -->
  <!-- FIME : $url comes from a global variable. -->
  <xsl:template match="mds:MD_Metadata|*[contains(@gco:isoType,'MD_Metadata')]" mode="record-reference">
    <!-- TODO : a metadata record may contains aggregate. In that case create one dataset per aggregate member. -->
    <dcat:dataset rdf:resource="{$url}/resource/{iso19115-3.2018:getResourceCode(.)}"/>
    <dcat:record rdf:resource="{$url}/metadata/{mds:metadataIdentifier[position() = 1]/mcc:MD_Identifier/mcc:code/gco:CharacterString}"/>
  </xsl:template>
  
  
  <!--
    Convert ISO record to DCAT
    -->
  <xsl:template match="mds:MD_Metadata|*[contains(@gco:isoType,'MD_Metadata')]" mode="to-dcat">


    <!-- Catalogue records
      "A record in a data catalog, describing a single dataset."        
      
      xpath: //mds:MD_Metadata|//*[contains(@gco:isoType,'MD_Metadata')]
    -->
    <dcat:CatalogRecord rdf:about="{$url}/metadata/{mds:metadataIdentifier[position() = 1]/mcc:MD_Identifier/mcc:code/gco:CharacterString}">
      <!-- Link to a dcat:Dataset or a rdf:Description for services and feature catalogue. -->
      <foaf:primaryTopic rdf:resource="{$url}/resource/{iso19115-3.2018:getResourceCode(.)}"/>

      <!-- Metadata change date.
      "The date is encoded as a literal in "YYYY-MM-DD" form (ISO 8601 Date and Time Formats)." -->
      <xsl:variable name="date" select="substring-before(mds:dateInfo/cit:CI_Date[cit:dateType/cit:CI_DateTypeCode/@codeListValue='revision']/cit:date/gco:DateTime, 'T')"/>
      <dct:modified><xsl:value-of select="$date"/></dct:modified>
      <dct:issued><xsl:value-of select="$date"/></dct:issued>
      <!-- xpath: mds:dateInfo/cit:CI_Date[cit:dateType/cit:CI_DateTypeCode/@codeListValue='revision']cit:date/gco:DateTime -->
      
      <xsl:call-template name="add-reference-19115-3.2018">
        <xsl:with-param name="uuid" select="mds:metadataIdentifier[position() = 1]/mcc:MD_Identifier/mcc:code/gco:CharacterString"/>
      </xsl:call-template>
    </dcat:CatalogRecord>
    
    <xsl:apply-templates select="mds:identificationInfo/*" mode="to-dcat"/>
    
  </xsl:template>
  
  
  <!-- Add references for HTML and XML metadata record link -->
  <xsl:template name="add-reference-19115-3.2018">
    <xsl:param name="uuid"/>
    
    <dct:references>
      <rdf:Description rdf:about="{$url}/srv/eng/xml.metadata.get?uuid={$uuid}">
        <dct:format>
          <dct:IMT><rdf:value>application/xml</rdf:value><rdfs:label>XML</rdfs:label></dct:IMT>
        </dct:format>
      </rdf:Description>
    </dct:references>
    
    <dct:references>
      <rdf:Description rdf:about="{$url}?uuid={$uuid}">
        <dct:format>
          <dct:IMT><rdf:value>text/html</rdf:value><rdfs:label>HTML</rdfs:label></dct:IMT>
        </dct:format>
      </rdf:Description>
    </dct:references>
  </xsl:template>
  
  <!-- Create all references for iso19115-3.2018 record (if rdf.metadata.get) or records (if rdf.search) -->
  <xsl:template match="mds:MD_Metadata|*[contains(@gco:isoType,'MD_Metadata')]" mode="references">
    
    <!-- Keywords -->
    <xsl:for-each-group select="//mri:MD_Keywords[(mri:thesaurusName)]/mri:keyword/gco:CharacterString" group-by=".">
      <!-- FIXME maybe only do that, if keyword URI is available (when xlink is used ?) -->
      <skos:Concept rdf:about="{$url}/thesaurus/{iso19115-3.2018:getThesaurusCode(../../mri:thesaurusName)}/{encode-for-uri(.)}">
        <skos:inScheme rdf:resource="{$url}/thesaurus/{iso19115-3.2018:getThesaurusCode(../../mri:thesaurusName)}"/>
        <skos:prefLabel><xsl:value-of select="."/></skos:prefLabel>
      </skos:Concept>
    </xsl:for-each-group>
    
    
    <!-- Distribution 
      "Represents a specific available form of a dataset. Each dataset might be available in different 
      forms, these forms might represent different formats of the dataset, different endpoints,... 
      Examples of Distribution include a downloadable CSV file, an XLS file representing the dataset, 
      an RSS feed ..."
      
      Download, WebService, Feed
      
      xpath: //mds:distributionInfo/*/mrd:transferOptions/*/mrd:onLine/cit:CI_OnlineResource
    -->
    <xsl:for-each-group select="//mds:distributionInfo/*/mrd:transferOptions/*/mrd:onLine/cit:CI_OnlineResource" group-by="cit:linkage/*">
      <dcat:Distribution rdf:about="{cit:linkage/*}">
        <!-- 
          "points to the location of a distribution. This can be a direct download link, a link 
          to an HTML page containing a link to the actual data, Feed, Web Service etc. 
          the semantic is determined by its domain (Distribution, Feed, WebService, Download)." 
        -->
        <dcat:accessURL><xsl:value-of select="cit:linkage/*"/></dcat:accessURL>
        <!-- xpath: cit:linkage/* -->
        
        <xsl:if test="cit:name/gco:CharacterString!=''">
          <dct:title><xsl:value-of select="cit:name/gco:CharacterString"/></dct:title>
        </xsl:if>
        <!-- xpath: cit:name/gco:CharacterString -->
        
        <!-- "The size of a distribution.":N/A 
          <dcat:size></dcat:size>
        -->
        
        <xsl:if test="cit:protocol/gco:CharacterString!=''">
        <dct:format>
          <!-- 
            "the file format of the distribution." 
            
            "MIME type is used for values. A list of MIME types URLs can be found at IANA. 
            However ESRI Shape files have no specific MIME type (A Shape distribution is actually 
            a collection of files), currently this is still an open question?"
            
            In our case, Shapefile will be zipped !
            
            Mapping between protocol list and mime/type when needed
          -->
          <dct:IMT>
            <rdf:value><xsl:value-of select="cit:protocol/gco:CharacterString"/></rdf:value>
            <rdfs:label><xsl:value-of select="cit:protocol/gco:CharacterString"/></rdfs:label>
          </dct:IMT>
        </dct:format>
        </xsl:if>
        <!-- xpath: cit:protocol/gco:CharacterString -->
        
      </dcat:Distribution>
    </xsl:for-each-group>
    
    
    
    <xsl:for-each-group select="//cit:CI_Organisation[cit:name/gco:CharacterString!='']" group-by="cit:name/gco:CharacterString">
      <!-- Organization description. 
        Organization could be linked to a catalogue, a catalogue record.
        
        xpath: //cit:CI_Organisation/cit:name/gco:CharacterString
      -->
      <foaf:Organization rdf:about="{$url}/organization/{encode-for-uri(current-grouping-key())}">
        <foaf:name><xsl:value-of select="current-grouping-key()"/></foaf:name>
        <!-- xpath: cit:CI_Organisation/cit:name/gco:CharacterString -->
        <xsl:for-each-group select="//cit:CI_Organisation[cit:name/gco:CharacterString=current-grouping-key()]" group-by="cit:contactInfo/cit:CI_Contact/cit:address/cit:CI_Address/cit:electronicMailAddress/gco:CharacterString">
          <foaf:member rdf:resource="{$url}/organization/{encode-for-uri(iso19115-3.2018:getContactId(.))}"/>
        </xsl:for-each-group>
      </foaf:Organization>
    </xsl:for-each-group>
    
    
    <xsl:for-each-group select="//cit:CI_Organisation" group-by="cit:contactInfo/cit:CI_Contact/cit:address/cit:CI_Address/cit:electronicMailAddress/gco:CharacterString">
      <!-- Organization memeber
        
        xpath: //cit:CI_Organisation -->
      
      <foaf:Agent rdf:about="{$url}/person/{encode-for-uri(iso19115-3.2018:getContactId(.))}">
        <xsl:if test="cit:individual/cit:CI_Individual/cit:name/gco:CharacterString">
          <foaf:name><xsl:value-of select="cit:individual/cit:CI_Individual/cit:name/gco:CharacterString"/></foaf:name>
        </xsl:if>
        <!-- xpath: cit:individual/cit:CI_Individual/cit:name/gco:CharacterString -->
        <xsl:if test="cit:contactInfo/cit:CI_Contact/cit:phone/cit:CI_Telephone[cit:numberType/cit:CI_TelephoneTypeCode/@codeListValue='voice']/cit:number/gco:CharacterString">
          <foaf:phone><xsl:value-of select="cit:contactInfo/cit:CI_Contact/cit:phone/cit:CI_Telephone[cit:numberType/cit:CI_TelephoneTypeCode/@codeListValue='voice']/cit:number/gco:CharacterString"/></foaf:phone>
        </xsl:if>
        <!-- xpath: cit:contactInfo/cit:CI_Contact/cit:phone/cit:CI_Telephone[cit:numberType/cit:CI_TelephoneTypeCode/@codeListValue='voice']/cit:number/gco:CharacterString -->
        <xsl:if test="cit:contactInfo/cit:CI_Contact/cit:address/cit:CI_Address/cit:electronicMailAddress/gco:CharacterString">
          <foaf:mbox rdf:resource="mailto:{cit:contactInfo/cit:CI_Contact/cit:address/cit:CI_Address/cit:electronicMailAddress/gco:CharacterString}"/>
        </xsl:if>
        <!-- xpath: cit:contactInfo/cit:CI_Contact/cit:address/cit:CI_Address/cit:electronicMailAddress/gco:CharacterString -->
      </foaf:Agent>
    </xsl:for-each-group>
  </xsl:template>
  
  
  <!-- Service 
    Create a simple rdf:Description. To be improved.
    
    xpath: //srv:SV_ServiceIdentification||//*[contains(@gco:isoType, 'SV_ServiceIdentification')]
  -->
  <xsl:template match="srv:SV_ServiceIdentification|*[contains(@gco:isoType, 'SV_ServiceIdentification')]" mode="to-dcat">
    <rdf:Description rdf:about="{$url}/resource/{iso19115-3.2018:getResourceCode(../../.)}">
      <xsl:call-template name="to-dcat-19115-3.2018"/>
    </rdf:Description>
  </xsl:template>
  
  
  
  <!-- Dataset
    "A collection of data, published or curated by a single source, and available for access or 
    download in one or more formats."
    
    xpath: //mri:MD_DataIdentification|//*[contains(@gco:isoType, 'MD_DataIdentification')]
  -->
  <xsl:template match="mri:MD_DataIdentification|*[contains(@gco:isoType, 'MD_DataIdentification')]" mode="to-dcat">
    <dcat:Dataset rdf:about="{$url}/resource/{iso19115-3.2018:getResourceCode(../../.)}">
      <xsl:call-template name="to-dcat-19115-3.2018"/>
    </dcat:Dataset>
  </xsl:template>
  
  
  
  <!-- Build a dcat record for a dataset or service -->
  <xsl:template name="to-dcat-19115-3.2018">
    <!-- "A unique identifier of the dataset." -->
    <dct:identifier><xsl:value-of select="iso19115-3.2018:getResourceCode(../../.)"/></dct:identifier>
    <!-- xpath: mri:identificationInfo/*/mri:citation/*/cit:identifier/*/mcc:code --> 
    
    
    <dct:title><xsl:value-of select="mri:citation/*/cit:title/gco:CharacterString"/></dct:title>
    <!-- xpath: mri:identificationInfo/*/mri:citation/*/cit:title/gco:CharacterString -->
    
    
    <dct:abstract><xsl:value-of select="mri:abstract/gco:CharacterString"/></dct:abstract>
    <!-- xpath: mds:identificationInfo/*/mri:abstract/gco:CharacterString -->
    
    
    <!-- "A keyword or tag describing the dataset."
      Create dcat:keyword if no thesaurus name information available.
    -->
    <xsl:for-each select="mri:descriptiveKeywords/mri:MD_Keywords[not(mri:thesaurusName)]/mri:keyword/gco:CharacterString">
      <dcat:keyword><xsl:value-of select="."/></dcat:keyword>
    </xsl:for-each>
    <!-- xpath: mds:identificationInfo/*/mri:descriptiveKeywords/mri:MD_Keywords[not(mri:thesaurusName)]/mri:keyword/gco:CharacterString --> 
    
    
    <!-- "The main category of the dataset. A dataset can have multiple themes." 
      Create dcat:theme if gmx:Anchor or GEMET concepts or INSPIRE themes
    -->
    <xsl:for-each select="mri:descriptiveKeywords/mri:MD_Keywords[(mri:thesaurusName)]/mri:keyword/gco:CharacterString">
      <!-- FIXME maybe only do that, if keyword URI is available (when xlink is used ?) -->
      <dcat:theme rdf:resource="{$url}/thesaurus/{iso19115-3.2018:getThesaurusCode(../../mri:thesaurusName)}/{.}"/>
    </xsl:for-each>

    <xsl:for-each select="mri:topicCategory/mri:MD_TopicCategoryCode[.!='']">
      <!-- FIXME Is there any public URI pointing to topicCategory enumeration ? -->
      <dcat:theme rdf:resource="{$url}/thesaurus/iso/topicCategory/{.}"/>
    </xsl:for-each>
    
    <!-- Thumbnail -->
    <xsl:for-each select="mri:graphicOverview/mcc:MD_BrowseGraphic/mcc:fileName/gco:CharacterString">
      <foaf:thumbnail rdf:resource="{.}"/>
    </xsl:for-each>
    <!-- xpath: mds:identificationInfo/*/mri:graphicOverview/mcc:MD_BrowseGraphic/mcc:fileName/gco:CharacterString -->
    
    
    <!-- "Spatial coverage of the dataset." -->
    <xsl:for-each select="mri:extent/*/gex:geographicElement/gex:EX_GeographicBoundingBox">
      <xsl:variable name="coords" select="
        concat(gex:westBoundLongitude/gco:Decimal, ' ', gex:southBoundLatitude/gco:Decimal),
        concat(gex:westBoundLongitude/gco:Decimal, ' ', gex:northBoundLatitude/gco:Decimal),
        concat(gex:eastBoundLongitude/gco:Decimal, ' ', gex:northBoundLatitude/gco:Decimal),
        concat(gex:eastBoundLongitude/gco:Decimal, ' ', gex:southBoundLatitude/gco:Decimal),
        concat(gex:westBoundLongitude/gco:Decimal, ' ', gex:southBoundLatitude/gco:Decimal)
        ">
      </xsl:variable>
      <dct:spatial>
        <ogc:Polygon>
          <ogc:asWKT rdf:datatype="http://www.opengis.net/rdf#WKTLiteral">
            &lt;http://www.opengis.net/def/crs/OGC/1.3/CRS84&gt;
            Polygon((<xsl:value-of select="string-join($coords, ', ')"/>))
          </ogc:asWKT>
        </ogc:Polygon>
      </dct:spatial>
    </xsl:for-each>
    <!-- xpath: mds:identificationInfo/*/gex:extent/*/gex:geographicElement/gex:EX_GeographicBoundingBox --> 
    
    
    <!-- "The temporal period that the dataset covers." -->
    <!-- TODO could be improved-->
    <xsl:for-each select="mri:extent/*/gex:temporalElement/gex:EX_TemporalExtent/gex:extent/gml:TimePeriod">
      <dct:temporal>
        <xsl:value-of select="gml:beginPosition"/>
        <xsl:if test="gml:endPosition">
          / <xsl:value-of select="gml:endPosition"/>
        </xsl:if>
      </dct:temporal>
    </xsl:for-each>
    <!-- xpath: mds:identificationInfo/*/gex:extent/*/gex:temporalElement --> 
    
    <xsl:for-each select="mri:citation/*/cit:date/cit:CI_Date[cit:dateType/cit:CI_DateTypeCode/@codeListValue='creation']">
      <dct:issued><xsl:value-of select="cit:date/gco:Date|cit:date/gco:DateTime"/></dct:issued>
    </xsl:for-each>
    <xsl:for-each select="mri:citation/*/cit:date/cit:CI_Date[cit:dateType/cit:CI_DateTypeCode/@codeListValue='revision']">
      <dct:updated><xsl:value-of select="cit:date/gco:Date|cit:date/gco:DateTime"/></dct:updated>
    </xsl:for-each>
    
    <!-- "An entity responsible for making the dataset available" -->
    <xsl:for-each select="mri:pointOfContact//cit:CI_Organisation/cit:name/gco:CharacterString[.!='']">
      <dct:publisher rdf:resource="{$url}/organization/{encode-for-uri(.)}"/>
    </xsl:for-each>
    <!-- xpath: mds:identificationInfo/*/mri:pointOfContact -->
    
    
    <!-- "The frequency with which dataset is published." See placetime.com intervals. -->
    <xsl:for-each select="mri:resourceMaintenance/mmi:MD_MaintenanceInformation/mmi:maintenanceAndUpdateFrequency/mmi:MD_MaintenanceFrequencyCode">
      <dct:accrualPeriodicity><xsl:value-of select="@codeListValue"/></dct:accrualPeriodicity>
    </xsl:for-each>
    <!-- xpath: mds:identificationInfo/*/mri:resourceMaintenance/mmi:MD_MaintenanceInformation/mmi:maintenanceAndUpdateFrequency/mmi:MD_MaintenanceFrequencyCode/@codeListValue -->
    
    <!-- "This is usually geographical or temporal but can also be other dimension" ??? -->
    <xsl:for-each select="mri:spatialResolution/mri:MD_Resolution/mri:equivalentScale/mri:MD_RepresentativeFraction/mri:denominator/gco:Integer[.!='']">
      <dcat:granularity><xsl:value-of select="."/></dcat:granularity>
    </xsl:for-each>
    <!-- xpath: mds:identificationInfo/*/mri:spatialResolution/mri:MD_Resolution/mri:equivalentScale/mri:MD_RepresentativeFraction/mri:denominator/gco:Integer -->
    
    
    <!-- 
      "The language of the dataset."
      "This overrides the value of the catalog language in case of conflict"
    -->
    <xsl:for-each select="mri:defaultLocale/lan:PT_Locale/lan:language/lan:LanguageCode/@codeListValue">
      <dct:language><xsl:value-of select="."/></dct:language>
    </xsl:for-each>
    <!-- xpath: mds:identificationInfo/*/mri:defaultLocale/lan:PT_Locale/lan:language/lan:LanguageCode/@codeListValue -->
    
    
    <!-- "The license under which the dataset is published and can be reused." -->
    <xsl:for-each select="mri:resourceConstraints/mco:MD_LegalConstraints/*/mco:MD_RestrictionCode">
      <dct:license><xsl:value-of select="@codeListValue"/></dct:license>
    </xsl:for-each>
    <xsl:for-each select="mri:resourceConstraints/mco:MD_LegalConstraints/mco:otherConstraints/gco:CharacterString">
      <dct:license><xsl:value-of select="."/></dct:license>
    </xsl:for-each>
    <!-- xpath: mds:identificationInfo/*/mri:resourceConstraints/??? -->
    
    
    <xsl:for-each select="../../mds:distributionInfo/*/mrd:transferOptions/*/mrd:onLine">
      <dcat:distribution rdf:resource="{cit:CI_OnlineResource/cit:linkage/*}"/>
    </xsl:for-each>
    <!-- xpath: mds:distributionInfo/*/mrd:transferOptions/*/mrd:onLine/cit:CI_OnlineResource -->
    
    
    <!-- ISO19110 relation 
      "This usually consisits of a table providing explanation of columns meaning, values interpretation and acronyms/codes used in the data."
    -->
    <xsl:for-each select="../../mds:contentInfo/mrc:MD_FeatureCatalogueDescription/mrc:featureCatalogueCitation/@uuidref ">
      <dcat:dataDictionary rdf:resource="{$url}/metadata/{.}"/>
    </xsl:for-each>
    <!-- xpath: mds:contentInfo/mrc:MD_FeatureCatalogueDescription/mrc:featureCatalogueCitation/@uuidref -->
    
    <!-- Dataset relation
    -->
    <xsl:for-each select="srv:operatesOn/@uuidref ">
      <dct:relation rdf:resource="{$url}/metadata/{.}"/>
    </xsl:for-each>
    
    
    <xsl:for-each select="mri:associatedResource/mri:MD_AssociatedResource_Type">
      <dct:relation rdf:resource="{$url}/metadata/{mri:name/*/cit:identifier/mcc:MD_Identifier/mcc:code/gco:CharacterString}"/>
    </xsl:for-each>
    
    <!-- Source relation -->
    <xsl:for-each select="/root/gui/relation/sources/response/metadata">
      <dct:relation rdf:resource="{$url}/metadata/{geonet:info/uuid}"/>
    </xsl:for-each>
    
    
    <!-- Parent/child relation -->
    <xsl:for-each select="../../mds:parentMetadata/mcc:MD_Identifier/mcc:code/gco:CharacterString[.!='']">
      <dct:relation rdf:resource="{$url}/metadata/{.}"/>
    </xsl:for-each>
    <xsl:for-each select="/root/gui/relation/children/response/metadata">
      <dct:relation rdf:resource="{$url}/metadata/{geonet:info/uuid}"/>
    </xsl:for-each>
    
    <!-- Service relation -->
    <xsl:for-each select="/root/gui/relations/services/response/metadata">
      <dct:relation rdf:resource="{$url}/metadata/{geonet:info/uuid}"/>
    </xsl:for-each>
    
    
    <!-- 
      "A related document such as technical documentation, agency program page, citation, etc."            
      
      TODO : only for URL ?
      <xsl:for-each select="mri:citation/*/cit:otherCitationDetails/gco:CharacterString">
      <dct:reference rdf:resource="url?"/>
      </xsl:for-each>
    -->
    <!-- xpath: mds:identificationInfo/*/cit:citation/*/cit:otherCitationDetails/gco:CharacterString -->
    
    
    <!-- "describes the quality of data." -->
    <xsl:for-each select="../../mds:dataQualityInfo/*/dqm:lineage/dqm:LI_Lineage/dqm:statement/gco:CharacterString">
      <dcat:dataQuality>
        <!-- rdfs:literal -->
        <xsl:value-of select="."/>
      </dcat:dataQuality>
    </xsl:for-each>
    <!-- xpath: mds:dataQualityInfo/*/dqm:lineage/dqm:LI_Lineage/dqm:statement/gco:CharacterString -->
    
    
    <!-- FIXME ? 
      <void:dataDump></void:dataDump>-->
  </xsl:template>
  
  
  
  
  
  
  
  
  <!-- 
    Get resource (dataset or service) identifier if set and return metadata UUID if not.
  -->
  <xsl:function name="iso19115-3.2018:getResourceCode" as="xs:string">
    <xsl:param name="metadata" as="node()"/>
    
    <xsl:value-of select="if ($metadata/mds:identificationInfo/*/mri:citation/*/cit:identifier/*/mcc:code/gco:CharacterString!='')
      then $metadata/mds:identificationInfo/*/mri:citation/*/cit:identifier/*/mcc:code/gco:CharacterString 
      else $metadata/mds:metadataIdentifier[position() = 1]/mcc:MD_Identifier/mcc:code/gco:CharacterString"/>
  </xsl:function>
  
  
  <!-- 
    Get thesaurus identifier, otherCitationDetails value, citation @id or thesaurus title.
  -->
  <xsl:function name="iso19115-3.2018:getThesaurusCode" as="xs:string">
    <xsl:param name="thesaurusName" as="node()"/>
    
    <xsl:value-of select="if ($thesaurusName/*/cit:otherCitationDetails/*!='') then $thesaurusName/*/cit:otherCitationDetails/*
      else if ($thesaurusName/cit:CI_Citation/@id!='') then $thesaurusName/cit:CI_Citation/@id!=''
      else encode-for-uri($thesaurusName/*/cit:title/gco:CharacterString)"/>
  </xsl:function>
  
  <!-- 
    Get contact identifier (for the time being = email and node generated identifier if no email available)
  -->
  <xsl:function name="iso19115-3.2018:getContactId" as="xs:string">
    <xsl:param name="responsibleParty" as="node()"/>
    
    <xsl:value-of select="if ($responsibleParty/cit:contactInfo/cit:CI_Contact/cit:address/cit:CI_Address/cit:electronicMailAddress/gco:CharacterString!='')
      then $responsibleParty/cit:contactInfo/cit:CI_Contact/cit:address/cit:CI_Address/cit:electronicMailAddress/gco:CharacterString
      else generate-id($responsibleParty)"/>
  </xsl:function>
  
</xsl:stylesheet>
