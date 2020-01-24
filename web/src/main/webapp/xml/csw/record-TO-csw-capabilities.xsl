<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:cit1="http://standards.iso.org/iso/19115/-3/cit/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:srv2="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:mac1="http://standards.iso.org/iso/19115/-3/mac/1.0"
                xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/2.0"
                xmlns:mas="http://standards.iso.org/iso/19115/-3/mas/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mda="http://standards.iso.org/iso/19115/-3/mda/1.0"
                xmlns:mdb1="http://standards.iso.org/iso/19115/-3/mdb/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/1.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:mic="http://standards.iso.org/iso/19115/-3/mic/1.0"
                xmlns:mil="http://standards.iso.org/iso/19115/-3/mil/1.0"
                xmlns:mrl1="http://standards.iso.org/iso/19115/-3/mrl/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mds1="http://standards.iso.org/iso/19115/-3/mds/1.0"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
                xmlns:mrc1="http://standards.iso.org/iso/19115/-3/mrc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:msr1="http://standards.iso.org/iso/19115/-3/msr/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:mai="http://standards.iso.org/iso/19115/-3/mai/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco139="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv139="http://www.isotc211.org/2005/srv"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
                exclude-result-prefixes="#all">

  <xsl:param name="outputLanguage" select="''"/>


  <xsl:template match="/">
    <xsl:variable name="isInspire" select="false"/>

    <csw:Capabilities xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                      xmlns:gml="http://www.opengis.net/gml"
                      xmlns:ows="http://www.opengis.net/ows"
                      xmlns:ogc="http://www.opengis.net/ogc"
                      xmlns:xlink="http://www.w3.org/1999/xlink"
                      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xmlns:inspire_ds="http://inspire.ec.europa.eu/schemas/inspire_ds/1.0"
                      xmlns:inspire_com="http://inspire.ec.europa.eu/schemas/common/1.0"
                      version="2.0.2"
                      xsi:schemaLocation="http://www.opengis.net/cat/csw/2.0.2 http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd http://inspire.ec.europa.eu/schemas/inspire_ds/1.0 http://inspire.ec.europa.eu/schemas/inspire_ds/1.0/inspire_ds.xsd">
      <xsl:attribute name="xsi:schemaLocation"
                     select="if ($isInspire)
                             then 'http://www.opengis.net/cat/csw/2.0.2 http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd http://inspire.ec.europa.eu/schemas/inspire_ds/1.0 http://inspire.ec.europa.eu/schemas/inspire_ds/1.0/inspire_ds.xsd'
                             else 'http://www.opengis.net/cat/csw/2.0.2 http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd'"/>

      <ows:ServiceIdentification>
        <ows:Title><xsl:value-of select="//mdb:identificationInfo/*/mri:citation/*/cit:title/*/text()|
                                         //gmd:identificationInfo/*/gmd:citation/*/gmd:title/*/text()"/></ows:Title>
        <ows:Abstract><xsl:value-of select="//mdb:identificationInfo/*/mri:abstract/*/text()|
                                         //gmd:identificationInfo/*/gmd:abstract/*/text()"/></ows:Abstract>
        <ows:Keywords>
          <!-- Keywords are automatically added by GeoNetwork
          according to catalogue content. -->
        </ows:Keywords>
        <ows:ServiceType>CSW</ows:ServiceType>
        <ows:ServiceTypeVersion>2.0.2</ows:ServiceTypeVersion>
        <ows:Fees>$FEES</ows:Fees>
        <ows:AccessConstraints>$ACCESS_CONSTRAINTS</ows:AccessConstraints>
      </ows:ServiceIdentification>
      <xsl:for-each select="//mdb:identificationInfo/*/mri:pointOfContact[1]">
        <ows:ServiceProvider>
          <ows:ProviderName>$PROVIDER_NAME</ows:ProviderName>
          <ows:ProviderSite xlink:href="$PROTOCOL://$HOST$PORT$SERVLET"/>
          <ows:ServiceContact>
            <ows:IndividualName>$IND_NAME</ows:IndividualName>
            <ows:PositionName>$POS_NAME</ows:PositionName>
            <ows:ContactInfo>
              <ows:Phone>
                <ows:Voice>$VOICE</ows:Voice>
                <ows:Facsimile>$FACSCIMILE</ows:Facsimile>
              </ows:Phone>
              <ows:Address>
                <ows:DeliveryPoint>$DEL_POINT</ows:DeliveryPoint>
                <ows:City>$CITY</ows:City>
                <ows:AdministrativeArea>$ADMIN_AREA</ows:AdministrativeArea>
                <ows:PostalCode>$POSTAL_CODE</ows:PostalCode>
                <ows:Country>$COUNTRY</ows:Country>
                <ows:ElectronicMailAddress>$EMAIL</ows:ElectronicMailAddress>
              </ows:Address>
              <ows:HoursOfService>$HOUROFSERVICE</ows:HoursOfService>
              <ows:ContactInstructions>$CONTACT_INSTRUCTION</ows:ContactInstructions>
            </ows:ContactInfo>
            <ows:Role>pointOfContact</ows:Role>
          </ows:ServiceContact>
        </ows:ServiceProvider>
      </xsl:for-each>
      <ows:OperationsMetadata>
        <ows:Operation name="GetCapabilities">
          <ows:DCP>
            <ows:HTTP>
              <ows:Get xlink:href="$PROTOCOL://$HOST$PORT$SERVLET/$NODE_ID/$LOCALE/$END-POINT"/>
              <ows:Post xlink:href="$PROTOCOL://$HOST$PORT$SERVLET/$NODE_ID/$LOCALE/$END-POINT"/>
            </ows:HTTP>
          </ows:DCP>
          <ows:Parameter name="sections">
            <ows:Value>ServiceIdentification</ows:Value>
            <ows:Value>ServiceProvider</ows:Value>
            <ows:Value>OperationsMetadata</ows:Value>
            <ows:Value>Filter_Capabilities</ows:Value>
          </ows:Parameter>
          <ows:Constraint name="PostEncoding">
            <ows:Value>XML</ows:Value>
            <ows:Value>SOAP</ows:Value>
          </ows:Constraint>
        </ows:Operation>
        <ows:Operation name="DescribeRecord">
          <ows:DCP>
            <ows:HTTP>
              <ows:Get xlink:href="$PROTOCOL://$HOST$PORT$SERVLET/$NODE_ID/$LOCALE/$END-POINT"/>
              <ows:Post xlink:href="$PROTOCOL://$HOST$PORT$SERVLET/$NODE_ID/$LOCALE/$END-POINT"/>
            </ows:HTTP>
          </ows:DCP>
          <ows:Parameter name="outputFormat">
            <ows:Value>application/xml</ows:Value>
          </ows:Parameter>
          <ows:Parameter name="schemaLanguage">
            <ows:Value>http://www.w3.org/TR/xmlschema-1/</ows:Value>
          </ows:Parameter>
          <ows:Parameter name="outputSchema"/>
          <ows:Constraint name="PostEncoding">
            <ows:Value>XML</ows:Value>
            <ows:Value>SOAP</ows:Value>
          </ows:Constraint>
        </ows:Operation>
        <ows:Operation name="GetDomain">
          <ows:DCP>
            <ows:HTTP>
              <ows:Get xlink:href="$PROTOCOL://$HOST$PORT$SERVLET/$NODE_ID/$LOCALE/$END-POINT"/>
              <ows:Post xlink:href="$PROTOCOL://$HOST$PORT$SERVLET/$NODE_ID/$LOCALE/$END-POINT"/>
            </ows:HTTP>
          </ows:DCP>
        </ows:Operation>
        <ows:Operation name="GetRecords">
          <ows:DCP>
            <ows:HTTP>
              <ows:Get xlink:href="$PROTOCOL://$HOST$PORT$SERVLET/$NODE_ID/$LOCALE/$END-POINT"/>
              <ows:Post xlink:href="$PROTOCOL://$HOST$PORT$SERVLET/$NODE_ID/$LOCALE/$END-POINT"/>
            </ows:HTTP>
          </ows:DCP>
          <!-- FIXME : Gets it from enum or conf -->
          <ows:Parameter name="resultType">
            <ows:Value>hits</ows:Value>
            <ows:Value>results</ows:Value>
            <ows:Value>validate</ows:Value>
          </ows:Parameter>
          <ows:Parameter name="outputFormat">
            <ows:Value>application/xml</ows:Value>
          </ows:Parameter>
          <ows:Parameter name="outputSchema"/>
          <ows:Parameter name="typeNames"/>
          <ows:Parameter name="CONSTRAINTLANGUAGE">
            <ows:Value>FILTER</ows:Value>
            <ows:Value>CQL_TEXT</ows:Value>
          </ows:Parameter>
          <ows:Constraint name="PostEncoding">
            <ows:Value>XML</ows:Value>
            <ows:Value>SOAP</ows:Value>
          </ows:Constraint>
        </ows:Operation>
        <ows:Operation name="GetRecordById">
          <ows:DCP>
            <ows:HTTP>
              <ows:Get xlink:href="$PROTOCOL://$HOST$PORT$SERVLET/$NODE_ID/$LOCALE/$END-POINT"/>
              <ows:Post xlink:href="$PROTOCOL://$HOST$PORT$SERVLET/$NODE_ID/$LOCALE/$END-POINT"/>
            </ows:HTTP>
          </ows:DCP>
          <ows:Parameter name="outputSchema"/>
          <ows:Parameter name="outputFormat">
            <ows:Value>application/xml</ows:Value>
          </ows:Parameter>
          <ows:Parameter name="resultType">
            <ows:Value>hits</ows:Value>
            <ows:Value>results</ows:Value>
            <ows:Value>validate</ows:Value>
          </ows:Parameter>
          <ows:Parameter name="ElementSetName">
            <ows:Value>brief</ows:Value>
            <ows:Value>summary</ows:Value>
            <ows:Value>full</ows:Value>
          </ows:Parameter>
          <ows:Constraint name="PostEncoding">
            <ows:Value>XML</ows:Value>
            <ows:Value>SOAP</ows:Value>
          </ows:Constraint>
        </ows:Operation>
        <ows:Operation name="Transaction">
          <ows:DCP>
            <ows:HTTP>
              <ows:Get xlink:href="$PROTOCOL://$HOST$PORT$SERVLET/$NODE_ID/$LOCALE/csw-publication"/>
              <ows:Post xlink:href="$PROTOCOL://$HOST$PORT$SERVLET/$NODE_ID/$LOCALE/csw-publication"/>
            </ows:HTTP>
          </ows:DCP>
        </ows:Operation>
        <ows:Operation name="Harvest">
          <ows:DCP>
            <ows:HTTP>
              <ows:Get xlink:href="$PROTOCOL://$HOST$PORT$SERVLET/$NODE_ID/$LOCALE/csw-publication"/>
              <ows:Post xlink:href="$PROTOCOL://$HOST$PORT$SERVLET/$NODE_ID/$LOCALE/csw-publication"/>
            </ows:HTTP>
          </ows:DCP>
          <ows:Parameter name="ResourceType">
            <ows:Value>http://www.isotc211.org/schemas/2005/gmd/</ows:Value>
          </ows:Parameter>
        </ows:Operation>
        <ows:Parameter name="service">
          <ows:Value>http://www.opengis.net/cat/csw/2.0.2</ows:Value>
        </ows:Parameter>
        <ows:Parameter name="version">
          <ows:Value>2.0.2</ows:Value>
        </ows:Parameter>
        <ows:Constraint name="IsoProfiles">
          <ows:Value>http://www.isotc211.org/2005/gmd</ows:Value>
        </ows:Constraint>
        <ows:Constraint name="PostEncoding">
          <ows:Value>SOAP</ows:Value>
        </ows:Constraint>

        <inspire_ds:ExtendedCapabilities>
          <inspire_com:ResourceLocator>
            <inspire_com:URL>$PROTOCOL://$HOST$PORT$SERVLET/$NODE_ID/$LOCALE/$END-POINT?SERVICE=CSW&amp;VERSION=2.0.2&amp;REQUEST=GetCapabilities</inspire_com:URL>
            <inspire_com:MediaType>application/xml</inspire_com:MediaType>
          </inspire_com:ResourceLocator>

          <inspire_com:ResourceLocator>
            <inspire_com:URL>$PROTOCOL://$HOST$PORT$SERVLET</inspire_com:URL>
            <inspire_com:MediaType>text/html</inspire_com:MediaType>
          </inspire_com:ResourceLocator>

          <inspire_com:ResourceType>service</inspire_com:ResourceType>

          <inspire_com:TemporalReference>
            <inspire_com:TemporalExtent>
              <inspire_com:IntervalOfDates>
                <inspire_com:StartingDate>2010-07-01T00:00:00</inspire_com:StartingDate>
                <inspire_com:EndDate>2011-07-01T00:00:00</inspire_com:EndDate>
              </inspire_com:IntervalOfDates>
            </inspire_com:TemporalExtent>
          </inspire_com:TemporalReference>

          <inspire_com:Conformity>
            <inspire_com:Specification
              xsi:type="inspire_com:citationInspireInteroperabilityRegulation_eng">
              <inspire_com:Title>COMMISSION REGULATION (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services</inspire_com:Title>
              <inspire_com:DateOfPublication>2010-12-08</inspire_com:DateOfPublication>
              <inspire_com:URI>OJ:L:2010:323:0011:0102:EN:PDF</inspire_com:URI>
              <inspire_com:ResourceLocator>
                <inspire_com:URL>
                  http://eur-lex.europa.eu/LexUriServ/LexUriServ.do?uri=OJ:L:2010:323:0011:0102:EN:PDF
                </inspire_com:URL>
                <inspire_com:MediaType>application/pdf</inspire_com:MediaType>
              </inspire_com:ResourceLocator>
            </inspire_com:Specification>

            <inspire_com:Degree>notEvaluated</inspire_com:Degree>
          </inspire_com:Conformity>


          <inspire_com:MetadataPointOfContact>
            <inspire_com:OrganisationName>$ORG_NAME</inspire_com:OrganisationName>
            <inspire_com:EmailAddress>$EMAIL</inspire_com:EmailAddress>
          </inspire_com:MetadataPointOfContact>

          <inspire_com:MetadataDate>2010-07-15</inspire_com:MetadataDate>
          <inspire_com:SpatialDataServiceType>discovery</inspire_com:SpatialDataServiceType>

          <inspire_com:MandatoryKeyword xsi:type="inspire_com:classificationOfSpatialDataService">
            <inspire_com:KeywordValue>infoCatalogueService</inspire_com:KeywordValue>
          </inspire_com:MandatoryKeyword>

          <inspire_com:Keyword xsi:type="inspire_com:inspireTheme_eng">
            <inspire_com:OriginatingControlledVocabulary>
              <inspire_com:Title>GEMET - INSPIRE themes</inspire_com:Title>
              <inspire_com:DateOfPublication>2008-06-01</inspire_com:DateOfPublication>
            </inspire_com:OriginatingControlledVocabulary>

            <inspire_com:KeywordValue>Orthoimagery</inspire_com:KeywordValue>
          </inspire_com:Keyword>

          <inspire_com:SupportedLanguages>
            <!--
            List of supported languages
            -->
          </inspire_com:SupportedLanguages>
          <inspire_com:ResponseLanguage>
            <inspire_com:Language>$INSPIRE_LOCALE</inspire_com:Language>
          </inspire_com:ResponseLanguage>
        </inspire_ds:ExtendedCapabilities>
      </ows:OperationsMetadata>

      <ogc:Filter_Capabilities>
        <ogc:Spatial_Capabilities>
          <ogc:GeometryOperands>
            <ogc:GeometryOperand>gml:Envelope</ogc:GeometryOperand>
            <ogc:GeometryOperand>gml:Point</ogc:GeometryOperand>
            <ogc:GeometryOperand>gml:LineString</ogc:GeometryOperand>
            <ogc:GeometryOperand>gml:Polygon</ogc:GeometryOperand>
          </ogc:GeometryOperands>
          <ogc:SpatialOperators>
            <ogc:SpatialOperator name="BBOX"/>
            <ogc:SpatialOperator name="Equals"/>
            <ogc:SpatialOperator name="Overlaps"/>
            <ogc:SpatialOperator name="Disjoint"/>
            <ogc:SpatialOperator name="Intersects"/>
            <ogc:SpatialOperator name="Touches"/>
            <ogc:SpatialOperator name="Crosses"/>
            <ogc:SpatialOperator name="Within"/>
            <ogc:SpatialOperator name="Contains"/>
            <!--
            <ogc:SpatialOperator name="Beyond"/>
            <ogc:SpatialOperator name="DWithin"/>
             The 'SpatialOperator' element can have a GeometryOperands child -->
          </ogc:SpatialOperators>
        </ogc:Spatial_Capabilities>
        <ogc:Scalar_Capabilities>
          <ogc:LogicalOperators/>
          <ogc:ComparisonOperators>
            <ogc:ComparisonOperator>EqualTo</ogc:ComparisonOperator>
            <ogc:ComparisonOperator>Like</ogc:ComparisonOperator>
            <ogc:ComparisonOperator>LessThan</ogc:ComparisonOperator>
            <ogc:ComparisonOperator>GreaterThan</ogc:ComparisonOperator>
            <!-- LessThanOrEqualTo is in OGC Filter Spec, LessThanEqualTo is in OGC CSW schema -->
            <ogc:ComparisonOperator>LessThanEqualTo</ogc:ComparisonOperator>
            <!--<ogc:ComparisonOperator>LessThanOrEqualTo</ogc:ComparisonOperator>-->
            <!-- GreaterThanOrEqualTo is in OGC Filter Spec, GreaterThanEqualTo is in OGC CSW schema -->
            <ogc:ComparisonOperator>GreaterThanEqualTo</ogc:ComparisonOperator>
            <!--<ogc:ComparisonOperator>GreaterThanOrEqualTo</ogc:ComparisonOperator>-->
            <ogc:ComparisonOperator>NotEqualTo</ogc:ComparisonOperator>
            <ogc:ComparisonOperator>Between</ogc:ComparisonOperator>
            <ogc:ComparisonOperator>NullCheck</ogc:ComparisonOperator>
            <!-- FIXME : Check NullCheck operation is available -->
          </ogc:ComparisonOperators>
        </ogc:Scalar_Capabilities>
        <ogc:Id_Capabilities>
          <ogc:EID/>
          <ogc:FID/>
        </ogc:Id_Capabilities>
      </ogc:Filter_Capabilities>
    </csw:Capabilities>

  </xsl:template>
</xsl:stylesheet>
