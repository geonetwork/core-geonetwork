<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gfcold="http://www.isotc211.org/2005/gfc"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gcoold="http://www.isotc211.org/2005/gco" xmlns:gmi="http://www.isotc211.org/2005/gmi" xmlns:gmx="http://www.isotc211.org/2005/gmx"
  xmlns:gsr="http://www.isotc211.org/2005/gsr" xmlns:gss="http://www.isotc211.org/2005/gss" xmlns:gts="http://www.isotc211.org/2005/gts" xmlns:srvold="http://www.isotc211.org/2005/srv"
  xmlns:gml30="http://www.opengis.net/gml" xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0" xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0" xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0" xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0" xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/2.0" xmlns:mas="http://standards.iso.org/iso/19115/-3/mas/1.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0" xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0" xmlns:mda="http://standards.iso.org/iso/19115/-3/mda/1.0"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0" xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/2.0" xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
  xmlns:mic="http://standards.iso.org/iso/19115/-3/mic/1.0" xmlns:mil="http://standards.iso.org/iso/19115/-3/mil/1.0" xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
  xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0" xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0" xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
  xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0" xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0" xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0" xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0" xmlns:mai="http://standards.iso.org/iso/19115/-3/mai/1.0"
  xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0" xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0" xmlns:gml="http://www.opengis.net/gml/3.2"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" exclude-result-prefixes="#all">
  <xsl:import href="../utility/multiLingualCharacterStrings.xsl"/>
  <xsl:import href="../utility/dateTime.xsl"/>
  <!-- Define if parent identifier should be defined using a uuidref
      attribute or a CI_Citation with a title. -->
  <xsl:param name="isParentIdentifierDefinedWithUUIDAttribute" select="true()" as="xs:boolean"/>
  <!--
    root element templates
  -->
  <xsl:template match="gmd:fileIdentifier" priority="5" mode="from19139to19115-3.2018">
    <!--
    gmd:fileIdentifier is changed from a gco:CharacterString to a MD_Identifer
		which now includes a codespace. This transform assumes a form of
		namespace:code for the fileIdentifier
    -->
    <xsl:element name="mdb:metadataIdentifier">
      <xsl:apply-templates select="@*" mode="from19139to19115-3.2018"/>
      <mcc:MD_Identifier>
        <xsl:for-each select="*">
          <mcc:code>
            <!-- The code could be a gco:CharacterString or any substitution for gco:CharacterString -->
            <xsl:variable name="nameSpacePrefix">
              <xsl:call-template name="getNamespacePrefix"/>
            </xsl:variable>
            <xsl:element name="{concat($nameSpacePrefix, ':',local-name())}">
              <xsl:choose>
                <xsl:when test="contains(.,':')">
                  <xsl:value-of select="substring-after(.,':')"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="."/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:element>
          </mcc:code>
          <xsl:if test="contains(.,':')">
            <mcc:codeSpace>
              <gco:CharacterString>
                <xsl:value-of select="substring-before(.,':')"/>
              </gco:CharacterString>
            </mcc:codeSpace>
          </xsl:if>
        </xsl:for-each>
      </mcc:MD_Identifier>
    </xsl:element>
  </xsl:template>


  <xsl:template match="gfcold:FC_FeatureCatalogue/@uuid" priority="5" mode="from19139to19115-3.2018">
    <xsl:element name="mdb:metadataIdentifier">
      <mcc:MD_Identifier>
        <mcc:code>
          <gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
        </mcc:code>
      </mcc:MD_Identifier>
    </xsl:element>
  </xsl:template>

  <xsl:template match="gfcold:typeName/gcoold:LocalName
                      |gfcold:aliases/gcoold:LocalName
                      |gfcold:memberName/gcoold:LocalName" priority="5"
                mode="from19139to19115-3.2018">
    <xsl:value-of select="."/>
  </xsl:template>


  <xsl:template match="gmd:language|gmd:locale" priority="5" mode="from19139to19115-3.2018">
    <xsl:variable name="nameSpacePrefix">
      <xsl:call-template name="getNamespacePrefix"/>
    </xsl:variable>
    <xsl:variable name="elementName" select="if (local-name() = 'language') then 'defaultLocale' else 'otherLocale'"/>
    <xsl:element name="{concat($nameSpacePrefix, ':', $elementName)}">
      <!--<xsl:element name="{'mdb:defaultLocale'}">-->
      <xsl:apply-templates select="@*" mode="from19139to19115-3.2018"/>
      <lan:PT_Locale>
        <xsl:copy-of select="gmd:PT_Locale/@*"/>
        <xsl:call-template name="writeCodelistElement">
          <xsl:with-param name="elementName" select="'lan:language'"/>
          <xsl:with-param name="codeListName" select="'lan:LanguageCode'"/>
          <xsl:with-param name="codeListValue"
            select="
            gcoold:CharacterString |
            gmd:LanguageCode/@codeListValue |
            gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode/@codeListValue"/>
        </xsl:call-template>
        <xsl:choose>
          <xsl:when test="../gmd:characterSet">
            <xsl:call-template name="writeCodelistElement">
              <xsl:with-param name="elementName" select="'lan:characterEncoding'"/>
              <xsl:with-param name="codeListName" select="'lan:MD_CharacterSetCode'"/>
              <xsl:with-param name="codeListValue" select="../gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <lan:characterEncoding gco:nilReason="unknown"/>
          </xsl:otherwise>
        </xsl:choose>
      </lan:PT_Locale>
    </xsl:element>
  </xsl:template>
  <xsl:template match="gmd:characterSet" priority="5" mode="from19139to19115-3.2018">
    <xsl:choose>
      <!-- if ../gmd/language exists, characterSet has already been translated to defaultLocale -->
      <xsl:when test="../gmd:language"/>
      <xsl:otherwise>
        <xsl:variable name="nameSpacePrefix">
          <xsl:call-template name="getNamespacePrefix"/>
        </xsl:variable>
        <xsl:element name="{concat($nameSpacePrefix,':','defaultLocale')}">
          <!--<xsl:element name="{'mdb:defaultLocale'}">-->
          <xsl:apply-templates select="@*" mode="from19139to19115-3.2018"/>
          <lan:PT_Locale>
            <xsl:call-template name="writeCodelistElement">
              <xsl:with-param name="elementName" select="'lan:characterEncoding'"/>
              <xsl:with-param name="codeListName" select="'lan:MD_CharacterSetCode'"/>
              <xsl:with-param name="codeListValue" select="gmd:MD_CharacterSetCode/@codeListValue"/>
            </xsl:call-template>
          </lan:PT_Locale>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="gmd:parentIdentifier" priority="5" mode="from19139to19115-3.2018">
    <!--
         gmd:parentIdentifier is changed from a gco:CharacterString to a
         mdb:parentMetadata element. This transform support two types
         of conversion depending on the
         isParentIdentifierDefinedWithUUIDAttribute parameter.

         a) if $isParentIdentifierDefinedWithUUIDAttribute is true, then
         populate an uuidref attribute based on the gco:CharacterString value

         b) if false, assumes a form of namespace:code for the
         parentIdentifier and populate the title and identifier of the
         citation.
    -->
    <xsl:element name="mdb:parentMetadata">
      <xsl:choose>
        <xsl:when test="$isParentIdentifierDefinedWithUUIDAttribute">
          <xsl:attribute name="uuidref" select="gcoold:CharacterString"/>
        </xsl:when>
        <xsl:otherwise>
          <cit:CI_Citation>
            <cit:title>
              <gco:CharacterString>
                <xsl:value-of select="gcoold:CharacterString"/>
              </gco:CharacterString>
            </cit:title>
            <cit:identifier>
              <mcc:MD_Identifier>
                <mcc:code>
                  <gco:CharacterString>
                    <xsl:choose>
                      <xsl:when test="contains(gcoold:CharacterString,':')">
                        <xsl:value-of select="substring-after(gcoold:CharacterString,':')"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="gcoold:CharacterString"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </gco:CharacterString>
                </mcc:code>
                <xsl:if test="contains(gcoold:CharacterString,':')">
                  <mcc:codeSpace>
                    <gco:CharacterString>
                      <xsl:value-of select="substring-before(gcoold:CharacterString,':')"/>
                    </gco:CharacterString>
                  </mcc:codeSpace>
                </xsl:if>
              </mcc:MD_Identifier>
            </cit:identifier>
          </cit:CI_Citation>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>

  <xsl:template match="gmd:updateScope" priority="5" mode="from19139to19115-3">
    <mmi:maintenanceScope>
      <mcc:MD_Scope>
        <xsl:call-template name="writeCodelistElement">
          <xsl:with-param name="elementName" select="'mcc:level'"/>
          <xsl:with-param name="codeListName" select="'mcc:MD_ScopeCode'"/>
          <xsl:with-param name="codeListValue" select="gmd:MD_ScopeCode/@codeListValue"/>
        </xsl:call-template>
      </mcc:MD_Scope>
    </mmi:maintenanceScope>
  </xsl:template>

  <xsl:template match="gmd:hierarchyLevel" priority="5" mode="from19139to19115-3.2018">
    <!-- ************************************************************************ -->
    <!-- gmd:hierarchyLevel and gmd:hierarchyLevelName are combined into a
			   new class: MD_MetadataScope to avoid ambiguity when there are multiple elements. -->
    <!-- ************************************************************************ -->
    <mdb:metadataScope>
      <mdb:MD_MetadataScope>
        <xsl:call-template name="writeCodelistElement">
          <xsl:with-param name="elementName" select="'mdb:resourceScope'"/>
          <xsl:with-param name="codeListName" select="'mcc:MD_ScopeCode'"/>
          <xsl:with-param name="codeListValue" select="gmd:MD_ScopeCode/@codeListValue|
                                  gmx:MX_ScopeCode/@codeListValue"/>
        </xsl:call-template>
        <xsl:if test="../gmd:hierarchyLevelName">
          <mdb:name>
            <gco:CharacterString>
              <xsl:value-of select="../gmd:hierarchyLevelName/gcoold:CharacterString"/>
            </gco:CharacterString>
          </mdb:name>
        </xsl:if>
      </mdb:MD_MetadataScope>
    </mdb:metadataScope>
  </xsl:template>
  <xsl:template match="gmd:dateStamp" priority="5" mode="from19139to19115-3.2018">
    <!--
      dateStamp is changed into a CI_Date that includes a dateType
    -->
    <xsl:choose>
      <xsl:when test="@*[local-name()='nilReason']">
        <xsl:element name="mdb:dateInfo">
          <xsl:attribute name="gco:nilReason" select="@*[local-name()='nilReason']"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <mdb:dateInfo>
          <cit:CI_Date>
            <cit:date>
              <xsl:call-template name="writeDateTime"/>
            </cit:date>
            <xsl:call-template name="writeCodelistElement">
              <xsl:with-param name="elementName" select="'cit:dateType'"/>
              <xsl:with-param name="codeListName" select="'cit:CI_DateTypeCode'"/>
              <xsl:with-param name="codeListValue" select="'revision'"/>
            </xsl:call-template>
          </cit:CI_Date>
        </mdb:dateInfo>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="gmd:metadataStandardName" priority="5"
                mode="from19139to19115-3.2018">
    <mdb:metadataStandard>
      <cit:CI_Citation>
        <xsl:choose>
          <!-- Replace default standard name with fixed value ....-->
          <xsl:when test="matches(gcoold:CharacterString, '^ISO\s?191(15|39)((:|\.)2003/19139)?$', 'i')">
            <cit:title>
              <gco:CharacterString>ISO 19115-3:2018</gco:CharacterString>
            </cit:title>
            <cit:edition>
              <gco:CharacterString>1.0</gco:CharacterString>
            </cit:edition>
          </xsl:when>
          <!--
            or combined custom ones metadataStandardName and gmd:metadataStandardVersion
            into a CI_Citation
          -->
          <xsl:otherwise>
            <xsl:call-template name="writeCharacterStringElement">
              <xsl:with-param name="elementName" select="'cit:title'"/>
              <xsl:with-param name="nodeWithStringToWrite" select="."/>
            </xsl:call-template>
            <xsl:call-template name="writeCharacterStringElement">
              <xsl:with-param name="elementName" select="'cit:edition'"/>
              <xsl:with-param name="nodeWithStringToWrite" select="../gmd:metadataStandardVersion"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </cit:CI_Citation>
    </mdb:metadataStandard>
  </xsl:template>
  <!-- gmd:spatialRepresentationInfo uses default templates -->
  <xsl:template match="gmi:geographicCoordinates" mode="from19139to19115-3.2018">
    <xsl:choose>
      <xsl:when test="normalize-space(.)=''">
        <msr:geographicCoordinates/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="msr:geographicCoordinates">
          <xsl:element name="gml:Point">
            <xsl:attribute name="gml:id">
              <xsl:value-of select="generate-id()"/>
            </xsl:attribute>
            <xsl:apply-templates select="./*" mode="from19139to19115-3.2018"/>
          </xsl:element>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="gmd:MD_PixelOrientationCode" mode="from19139to19115-3.2018">
    <xsl:element name="msr:MD_PixelOrientationCode">
      <xsl:choose>
        <xsl:when test=".='center'">
          <xsl:value-of select="'centre'"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>
  <!-- gmd:referenceSystemInfo uses default templates -->
  <!-- gmd:metadataExtensionInfo uses default templates -->
  <xsl:template match="gmd:identificationInfo" mode="from19139to19115-3.2018">
    <mdb:identificationInfo>
      <xsl:apply-templates select="@*" mode="from19139to19115-3.2018"/>
      <xsl:for-each select="./*">
        <xsl:variable name="nameSpacePrefix">
          <xsl:call-template name="getNamespacePrefix"/>
        </xsl:variable>
        <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
          <xsl:apply-templates select="@*" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:citation" mode="from19139to19115-3.2018"/>
          <xsl:call-template name="writeCharacterStringElement">
            <xsl:with-param name="elementName" select="'mri:abstract'"/>
            <xsl:with-param name="nodeWithStringToWrite" select="gmd:abstract"/>
          </xsl:call-template>
          <xsl:call-template name="writeCharacterStringElement">
            <xsl:with-param name="elementName" select="'mri:purpose'"/>
            <xsl:with-param name="nodeWithStringToWrite" select="gmd:purpose"/>
          </xsl:call-template>
          <xsl:apply-templates select="gmd:credit" mode="from19139to19115-3.2018"/>
          <xsl:call-template name="writeCodelistElement">
            <xsl:with-param name="elementName" select="'mri:status'"/>
            <xsl:with-param name="codeListValue" select="gmd:status/gmd:MD_ProgressCode/@codeListValue"/>
            <xsl:with-param name="codeListName" select="'mcc:MD_ProgressCode'"/>
          </xsl:call-template>
          <xsl:apply-templates select="gmd:pointOfContact" mode="from19139to19115-3.2018"/>
            <xsl:call-template name="writeCodelistElement">
              <xsl:with-param name="elementName" select="'mri:spatialRepresentationType'"/>
              <xsl:with-param name="codeListName" select="'mcc:MD_SpatialRepresentationTypeCode'"/>
              <xsl:with-param name="codeListValue" select="gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode/@codeListValue"/>
            </xsl:call-template>
          <xsl:apply-templates select="gmd:spatialResolution" mode="from19139to19115-3.2018"/>
          <!-- This is here to handle early adopters of temporalResolution -->
          <xsl:apply-templates select="gmd:temporalResolution" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:topicCategory" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:extent | srvold:extent" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:resourceMaintenance" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:graphicOverview" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:resourceFormat" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:descriptiveKeywords" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:resourceSpecificUsage" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:resourceConstraints" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:aggregationInfo" mode="from19139to19115-3.2018"/>
          <xsl:call-template name="collectiveTitle"/>
          <xsl:apply-templates select="gmd:language" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:characterSet" mode="from19139to19115-3.2018"/>
          <xsl:call-template name="writeCharacterStringElement">
            <xsl:with-param name="elementName" select="'mri:environmentDescription'"/>
            <xsl:with-param name="nodeWithStringToWrite" select="gmd:environmentDescription"/>
          </xsl:call-template>
          <xsl:call-template name="writeCharacterStringElement">
            <xsl:with-param name="elementName" select="'mri:supplementalInformation'"/>
            <xsl:with-param name="nodeWithStringToWrite" select="gmd:supplementalInformation"/>
          </xsl:call-template>
          <!-- Service Identification Information -->
          <xsl:if test="local-name()='SV_ServiceIdentification'">
            <xsl:if test="srvold:serviceType">
              <srv:serviceType>
                <gco:ScopedName>
                  <xsl:value-of select="srvold:serviceType/gcoold:LocalName"/>
                </gco:ScopedName>
              </srv:serviceType>
            </xsl:if>
            <xsl:call-template name="writeCharacterStringElement">
              <xsl:with-param name="elementName" select="'srv:serviceTypeVersion'"/>
              <xsl:with-param name="nodeWithStringToWrite" select="srvold:serviceTypeVersion"/>
            </xsl:call-template>
            <xsl:call-template name="writeCodelistElement">
              <xsl:with-param name="elementName" select="'srv:couplingType'"/>
              <xsl:with-param name="codeListName" select="'srv:SV_CouplingType'"/>
              <xsl:with-param name="codeListValue" select="srvold:couplingType/srvold:SV_CouplingType/@codeListValue"/>
            </xsl:call-template>
            <xsl:apply-templates select="srvold:containsOperations" mode="from19139to19115-3.2018"/>
            <xsl:apply-templates select="srvold:operatesOn" mode="from19139to19115-3.2018"/>
          </xsl:if>
        </xsl:element>
      </xsl:for-each>
    </mdb:identificationInfo>
  </xsl:template>
  <xsl:template match="gmd:contentInfo[not(gmd:MD_FeatureCatalogueDescription) and
                                           not(gmd:MD_ImageDescription)]" mode="from19139to19115-3.2018">
    <xsl:if test="not(preceding-sibling::gmd:contentInfo)">
      <!-- ********************************************************************** -->
      <!-- First contentInfo section.                                             -->
      <!-- All contentInfo sections are transformed here to allow formation       -->
      <!-- of attributeGroups                                                     -->
      <!-- ********************************************************************** -->
      <xsl:element name="mdb:contentInfo">
        <xsl:for-each select="*">
          <!-- Process the first MD_CoverageDescription section. Get the mrc:attributeDescription from here. -->
          <xsl:element name="mrc:MD_CoverageDescription">
            <xsl:element name="mrc:attributeDescription">
              <xsl:element name="gco:RecordType">
                <xsl:for-each select=".//gcoold:RecordType">
                  <xsl:for-each select="@*">
                    <xsl:attribute name="{name(.)}" select="."/>
                  </xsl:for-each>
                  <xsl:value-of select="."/>
                </xsl:for-each>
              </xsl:element>
            </xsl:element>
            <!-- This loop goes back out to convert each gmd:contentInfo section into a separate mrc:AttributeGroup -->
            <xsl:for-each select="//gmd:contentInfo/gmd:MD_CoverageDescription | //gmd:contentInfo/gmi:MI_CoverageDescription">
              <xsl:element name="mrc:attributeGroup">
                <xsl:element name="mrc:MD_AttributeGroup">
                  <xsl:apply-templates mode="from19139to19115-3.2018"/>
                </xsl:element>
              </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="//gmi:rangeElementDescription">
              <xsl:element name="mrc:rangeElementDescription">
                <xsl:apply-templates mode="from19139to19115-3.2018"/>
              </xsl:element>
            </xsl:for-each>
          </xsl:element>
        </xsl:for-each>
      </xsl:element>
    </xsl:if>
  </xsl:template>
  <!-- transform contentInfo sections with Feature Catalogues -->
  <xsl:template match="gmd:contentInfo[gmd:MD_FeatureCatalogueDescription] | gmd:contentInfo[gmd:MD_ImageDescription]" mode="from19139to19115-3.2018">
    <xsl:element name="mdb:contentInfo">
      <xsl:apply-templates mode="from19139to19115-3.2018"/>
    </xsl:element>
  </xsl:template>
  <xsl:template match="//gmd:MD_FeatureCatalogueDescription/gmd:featureTypes" mode="from19139to19115-3.2018">
    <mrc:featureTypes>
      <mrc:MD_FeatureTypeInfo>
        <mrc:featureTypeName>
          <xsl:apply-templates mode="from19139to19115-3.2018"/>
        </mrc:featureTypeName>
      </mrc:MD_FeatureTypeInfo>
    </mrc:featureTypes>
  </xsl:template>
  <!-- gmd:portrayalCatalogueInfo uses default templates -->
  <!-- gmd:metadataConstraints uses default templates -->
  <!-- gmd:applicationSchemaInfo uses default templates -->
  <!-- gmd:metadataMaintenance uses default templates -->
  <xsl:template match="gmi:acquisitionInformation" mode="from19139to19115-3.2018">
    <xsl:element name="mdb:acquisitionInformation">
      <xsl:element name="mac:MI_AcquisitionInformation">
        <xsl:apply-templates select="gmi:MI_AcquisitionInformation/gmi:instrument" mode="from19139to19115-3.2018"/>
        <xsl:apply-templates select="gmi:MI_AcquisitionInformation/gmi:operation" mode="from19139to19115-3.2018"/>
        <xsl:apply-templates select="gmi:MI_AcquisitionInformation/gmi:platform" mode="from19139to19115-3.2018"/>
        <xsl:apply-templates select="gmi:MI_AcquisitionInformation/gmi:acquisitionPlan" mode="from19139to19115-3.2018"/>
        <xsl:apply-templates select="gmi:MI_AcquisitionInformation/gmi:objective" mode="from19139to19115-3.2018"/>
        <xsl:apply-templates select="gmi:MI_AcquisitionInformation/gmi:acquisitionRequirement" mode="from19139to19115-3.2018"/>
      </xsl:element>
    </xsl:element>
  </xsl:template>
  <xsl:template match="gmi:objectiveOccurance" mode="from19139to19115-3.2018">
    <!-- This element is mis-spelled in the 19115-2 schema -->
    <xsl:element name="mac:objectiveOccurence">
      <xsl:apply-templates select="@*" mode="from19139to19115-3.2018"/>
      <xsl:value-of select="."/>
    </xsl:element>
  </xsl:template>
  <!--
  gmd:spatialRepresentation templates
  -->
  <xsl:template match="gmi:geolocationInformation/gmi:MI_GCPCollection" mode="from19139to19115-3.2018">
    <xsl:element name="msr:MI_GCPCollection">
      <xsl:apply-templates select="gmi:gcp" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmi:collectionIdentification" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmi:collectionName" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmi:coordinateReferenceSystem" mode="from19139to19115-3.2018"/>
    </xsl:element>
  </xsl:template>
  <!--
  gmd:identificationInformation templates
  -->
  <xsl:template match="/*/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty" mode="from19139to19115-3.2018">
    <xsl:if test="not(preceding-sibling::gmd:citedResponsibleParty) and /*/gmd:dataSetURI">
      <!-- **********************************************************************
      The first citedResponsibleParty is special because the identifier
      created from the gmd:dataSetURI goes before it.
      WARNING: A record with a dataSetIdentifier and no
      citedResponsibleParties will fail.
      ********************************************************************** -->
      <cit:identifier>
        <mcc:MD_Identifier>
          <mcc:code>
            <gco:CharacterString>
              <xsl:value-of select="/*/gmd:dataSetURI/gcoold:CharacterString"/>
            </gco:CharacterString>
          </mcc:code>
        </mcc:MD_Identifier>
      </cit:identifier>
    </xsl:if>
    <!-- Avoid putting out empty citedResponsibleParties for just onlineResources (responsible parties without names) -->
    <xsl:if
      test="count(gmd:CI_ResponsibleParty/gmd:individualName/gcoold:CharacterString) + count(gmd:CI_ResponsibleParty/gmd:organisationName/gcoold:CharacterString) + count(gmd:CI_ResponsibleParty/gmd:positionName/gcoold:CharacterString) != 0">
      <cit:citedResponsibleParty>
        <xsl:apply-templates mode="from19139to19115-3.2018"/>
      </cit:citedResponsibleParty>
    </xsl:if>
  </xsl:template>
  <xsl:template match="/*/gmd:identificationInfo/*/gmd:resourceSpecificUsage/gmd:MD_Usage/gmd:usageDateTime" mode="from19139to19115-3.2018">
    <mri:usageDateTime>
      <gml:TimeInstant>
        <xsl:attribute name="gml:id">
          <xsl:value-of select="generate-id()"/>
        </xsl:attribute>
        <gml:timePosition>
          <xsl:value-of select="."/>
        </gml:timePosition>
      </gml:TimeInstant>
    </mri:usageDateTime>
  </xsl:template>





  <xsl:variable name="associatedResourceAsMetadataReferenceOnly"
                select="true()"/>

  <xsl:template match="gmd:aggregationInfo" priority="5" mode="from19139to19115-3.2018">
    <!--
   gmd:MD_AggregateInformation was renamed gmd:associatedResource in order
	 to clarify the intent of the class. It is used to provide information about
	 resources that are associated with the resource being described.
    -->
    <mri:associatedResource>
      <xsl:element name="mri:MD_AssociatedResource">
        <xsl:copy-of select="gmd:MD_AggregateInformation/@*"/>
        <!-- The name element is mapped from the existing gmd:aggregateDataSetName class.
					 The metadataReference replaces the gmd:aggregateDataSetIdentifier in order to
					 clarify the fact that it identifies and gives the location of the metadata
					 for the associated resources. -->

        <xsl:if test="not($associatedResourceAsMetadataReferenceOnly)">
          <xsl:choose>
            <xsl:when test="exists(gmd:MD_AggregateInformation/gmd:aggregateDataSetName)
              and exists(gmd:MD_AggregateInformation/gmd:aggregateDataSetIdentifier)">
              <!-- both name an identifier exist - use standard template -->
              <mri:name>
                <xsl:apply-templates select="gmd:MD_AggregateInformation/gmd:aggregateDataSetName/gmd:CI_Citation" mode="from19139to19115-3.2018"/>
              </mri:name>
            </xsl:when>
            <xsl:when test="exists(gmd:MD_AggregateInformation/gmd:aggregateDataSetName)">
              <!-- only an name exists - write it into a CI_Citation -->
              <mri:name>
                <xsl:apply-templates select="gmd:MD_AggregateInformation/gmd:aggregateDataSetName/gmd:CI_Citation" mode="from19139to19115-3.2018"/>
              </mri:name>
            </xsl:when>
            <xsl:when test="exists(gmd:MD_AggregateInformation/gmd:aggregateDataSetIdentifier)">
              <!-- only an identifier exists - write it into a CI_Citation -->
              <mri:name>
                <cit:CI_Citation>
                  <!-- No citation title or date exists -->
                  <cit:title gco:nilReason="unknown"/>
                  <cit:date gco:nilReason="unknown"/>
                  <cit:identifier>
                    <xsl:apply-templates select="gmd:MD_AggregateInformation/gmd:aggregateDataSetIdentifier/gmd:MD_Identifier" mode="from19139to19115-3.2018"/>
                  </cit:identifier>
                </cit:CI_Citation>
              </mri:name>
            </xsl:when>
          </xsl:choose>
        </xsl:if>

        <xsl:call-template name="writeCodelistElement">
          <xsl:with-param name="elementName" select="'mri:associationType'"/>
          <xsl:with-param name="codeListName" select="'mri:DS_AssociationTypeCode'"/>
          <xsl:with-param name="codeListValue" select="gmd:MD_AggregateInformation/gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue"/>
        </xsl:call-template>
        <xsl:call-template name="writeCodelistElement">
          <xsl:with-param name="elementName" select="'mri:initiativeType'"/>
          <xsl:with-param name="codeListName" select="'mri:DS_InitiativeTypeCode'"/>
          <xsl:with-param name="codeListValue" select="gmd:MD_AggregateInformation/gmd:initiativeType/gmd:DS_InitiativeTypeCode/@codeListValue"/>
        </xsl:call-template>

        <xsl:if test="$associatedResourceAsMetadataReferenceOnly">
          <xsl:variable name="uuidref"
                        select="gmd:MD_AggregateInformation/gmd:aggregateDataSetIdentifier/*/gmd:code/*/@uuidref"/>
          <mri:metadataReference>
            <xsl:copy-of select="gmd:MD_AggregateInformation/gmd:aggregateDataSetIdentifier/*/gmd:code/gmx:Anchor/@xlink:href"/>
            <xsl:copy-of select="if ($uuidref != '') then $uuidref else gmd:MD_AggregateInformation/gmd:aggregateDataSetIdentifier/*/gmd:code/*/text()"/>
          </mri:metadataReference>
        </xsl:if>

      </xsl:element>
    </mri:associatedResource>
  </xsl:template>

  <xsl:template match="gmd:aggregationInfo/gmd:MD_AggregateInformation/gmd:aggregateDataSetName/gmd:CI_Citation/gmd:citedResponsibleParty" mode="from19139to19115-3.2018">
    <xsl:if test="not(preceding-sibling::gmd:citedResponsibleParty) and ancestor::gmd:MD_AggregateInformation/gmd:aggregateDataSetIdentifier">
      <!-- **********************************************************************
      The first citedResponsibleParty is special because the identifier
      from the gmd:aggregateDataSetIdentifier goes before it.
      ********************************************************************** -->
      <cit:identifier>
        <xsl:apply-templates select="ancestor::gmd:MD_AggregateInformation/gmd:aggregateDataSetIdentifier/gmd:MD_Identifier" mode="from19139to19115-3.2018"/>
      </cit:identifier>
    </xsl:if>
    <cit:citedResponsibleParty>
      <xsl:apply-templates mode="from19139to19115-3.2018"/>
    </cit:citedResponsibleParty>
  </xsl:template>
  <!--
    gmd:referenceSystemInformation templates
  -->
  <xsl:template match="gmd:RS_Identifier" mode="from19139to19115-3.2018">
    <mcc:MD_Identifier>
      <xsl:apply-templates select="@*" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates mode="from19139to19115-3.2018"/>
    </mcc:MD_Identifier>
  </xsl:template>
  <!--
    gmd:contentInfo
  -->
  <xsl:template match="gmd:dimension" mode="from19139to19115-3.2018">
    <xsl:element name="mrc:attribute">
      <xsl:apply-templates mode="from19139to19115-3.2018"/>
    </xsl:element>
  </xsl:template>
  <xsl:template match="gmd:MD_Band" mode="from19139to19115-3.2018">
    <xsl:element name="mrc:MD_SampleDimension">
      <xsl:apply-templates mode="from19139to19115-3.2018"/>
    </xsl:element>
  </xsl:template>
  <xsl:template match="gmi:MI_Band" mode="from19139to19115-3.2018">
    <xsl:element name="mrc:MI_Band">
      <xsl:apply-templates mode="from19139to19115-3.2018"/>
    </xsl:element>
  </xsl:template>
  <xsl:template match="gmd:MD_Band/gmd:descriptor|
                       gmi:MI_Band/gmd:descriptor|
                       gmd:MD_RangeDimension/gmd:descriptor" mode="from19139to19115-3.2018">
    <xsl:element name="mrc:description">
      <xsl:element name="gco:CharacterString">
        <xsl:value-of select="."/>
      </xsl:element>
    </xsl:element>
  </xsl:template>
  <!--
  gmd:distributionInfo
  -->
  <xsl:template match="gmd:MD_DigitalTransferOptions/gmd:offLine/gmd:MD_Medium/gmd:name" mode="from19139to19115-3.2018">
    <mrd:name>
      <cit:CI_Citation>
        <cit:title>
          <gco:CharacterString>
            <xsl:value-of select="gmd:MD_MediumNameCode"/>
          </gco:CharacterString>
        </cit:title>
      </cit:CI_Citation>
    </mrd:name>
  </xsl:template>
  <!--
  gmd:metadataMaintenance
  -->
  <xsl:template match="gmd:MD_MaintenanceInformation/gmd:dateOfNextUpdate" mode="from19139to19115-3.2018">
    <mmi:maintenanceDate>
      <cit:CI_Date>
        <cit:date>
          <xsl:call-template name="writeDateTime"/>
        </cit:date>
        <xsl:call-template name="writeCodelistElement">
          <xsl:with-param name="elementName" select="'cit:dateType'"/>
          <xsl:with-param name="codeListName" select="'cit:CI_DateTypeCode'"/>
          <xsl:with-param name="codeListValue" select="'update'"/>
        </xsl:call-template>
      </cit:CI_Date>
    </mmi:maintenanceDate>
  </xsl:template>
  <!--
  gmi:gmi:acquisitionInformation templates
  -->
  <xsl:template match="gmi:MI_Operation/gmi:status/gmd:MD_ProgressCode" mode="from19139to19115-3.2018">
    <xsl:element name="mcc:MD_ProgressCode">
      <xsl:apply-templates select="@*" mode="from19139to19115-3.2018"/>
    </xsl:element>
  </xsl:template>
  <xsl:include href="defaults.xsl"/>
  <!--
    Empty High-Priority Templates to prevent
    independent actions on these elements
  -->
  <xsl:template match="gmd:hierarchyLevelName" priority="5" mode="from19139to19115-3.2018"/>
  <xsl:template match="gmd:metadataStandardVersion" priority="5" mode="from19139to19115-3.2018"/>
  <xsl:template match="gmd:dataSetURI" priority="5" mode="from19139to19115-3.2018"/>
  <!-- Match MD_ and MI_CoverageDescription -->
  <xsl:template match="gmd:contentInfo/gmd:MD_CoverageDescription/gmd:attributeDescription | gmd:contentInfo/gmi:MI_CoverageDescription/gmd:attributeDescription" priority="5" mode="from19139to19115-3.2018"/>
  <xsl:template match="gmd:contentInfo/gmi:MI_CoverageDescription/gmi:rangeElementDescription" priority="5" mode="from19139to19115-3.2018"/>
  <xsl:template match="gmd:contentInfo/gmd:MD_ImageDescription/gmd:contentType" priority="5" mode="from19139to19115-3.2018"/>
  <xsl:template match="gmd:MD_ExtendedElementInformation/gmd:shortName" priority="5" mode="from19139to19115-3.2018"/>
  <xsl:template match="gmd:MD_ExtendedElementInformation/gmd:domainCode" priority="5" mode="from19139to19115-3.2018"/>
  <xsl:template match="gmd:MD_Format/gmd:name" priority="5" mode="from19139to19115-3.2018"/>
  <xsl:template match="gmd:MD_Format/gmd:specification" priority="5" mode="from19139to19115-3.2018"/>
  <xsl:template match="gmd:MD_Format/gmd:version" priority="5" mode="from19139to19115-3.2018"/>
</xsl:stylesheet>
