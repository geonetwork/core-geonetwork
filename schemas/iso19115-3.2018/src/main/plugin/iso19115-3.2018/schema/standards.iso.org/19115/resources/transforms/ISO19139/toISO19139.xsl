<xsl:stylesheet version="2.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                xmlns:xs="http://www.w3.org/2001/XMLSchema" 
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco" 
                xmlns:gmi="http://www.isotc211.org/2005/gmi"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gsr="http://www.isotc211.org/2005/gsr"
                xmlns:gss="http://www.isotc211.org/2005/gss"
                xmlns:gts="http://www.isotc211.org/2005/gts" 
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:srv2="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/1.0"
                xmlns:mas="http://standards.iso.org/iso/19115/-3/mas/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mda="http://standards.iso.org/iso/19115/-3/mda/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/1.0"
                xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/1.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:mic="http://standards.iso.org/iso/19115/-3/mic/1.0"
                xmlns:mil="http://standards.iso.org/iso/19115/-3/mil/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/1.0"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/1.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/1.0"
                xmlns:mai="http://standards.iso.org/iso/19115/-3/mai/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:gco2="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gml32="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
                exclude-result-prefixes="#all">
  <xd:doc scope="stylesheet">
    <xd:desc>
      <xd:p>
        More work required on:
        <xd:ul>
          <xd:li>gmi:* not handled.</xd:li>
          <xd:li>Filter all new elements (see last template).</xd:li>
        </xd:ul>
      </xd:p>
    </xd:desc>
  </xd:doc>
  
  <xsl:output method="xml" indent="yes"/>
  
  <xsl:strip-space elements="*"/>
  
  <xsl:template name="add-namespaces">
    <!-- new namespaces -->
    <xsl:namespace name="xsi" select="'http://www.w3.org/2001/XMLSchema-instance'"/>
    <!-- Namespaces that include concepts outside of metadata -->
    <xsl:namespace name="gco" select="'http://www.isotc211.org/2005/gco'"/>
    <xsl:namespace name="gmd" select="'http://www.isotc211.org/2005/gmd'"/>
    <xsl:namespace name="srv" select="'http://www.isotc211.org/2005/srv'"/>
    <xsl:namespace name="gmx" select="'http://www.isotc211.org/2005/gmx'"/>
    <xsl:namespace name="gts" select="'http://www.isotc211.org/2005/gts'"/>
    <xsl:namespace name="gsr" select="'http://www.isotc211.org/2005/gsr'"/>
    <xsl:namespace name="gmi" select="'http://www.isotc211.org/2005/gmi'"/>
    <!-- external namespaces -->
    <xsl:namespace name="gml" select="'http://www.opengis.net/gml'"/>
    <xsl:namespace name="xlink" select="'http://www.w3.org/1999/xlink'"/>
  </xsl:template>
  
  <xsl:template match="/">
    <!-- 
    root element (MD_Metadata or MI_Metadata)
    -->
    <xsl:for-each select="/*">
      <xsl:variable name="nameSpacePrefix">
        <xsl:call-template name="getNamespacePrefix"/>
      </xsl:variable>
      <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
        <xsl:call-template name="add-namespaces"/>

        <xsl:apply-templates select="mdb:metadataIdentifier"/>
        <xsl:apply-templates select="mdb:defaultLocale"/>
        <xsl:apply-templates select="mdb:parentMetadata"/>
        <xsl:apply-templates select="mdb:metadataScope"/>
        <xsl:apply-templates select="mdb:contact"/>
        <xsl:apply-templates select="mdb:dateInfo"/>
        <xsl:apply-templates select="mdb:metadataStandard"/>
        <xsl:apply-templates select="mdb:metadataProfile"/>
        <xsl:apply-templates select="mdb:alternativeMetadataReference"/>
        <xsl:apply-templates select="mdb:otherLocale"/>
        <xsl:apply-templates select="mdb:metadataLinkage"/>
        <xsl:apply-templates select="mdb:spatialRepresentationInfo"/>
        <xsl:apply-templates select="mdb:referenceSystemInfo"/>
        <xsl:apply-templates select="mdb:metadataExtensionInfo"/>
        <xsl:apply-templates select="mdb:identificationInfo"/>
        <xsl:apply-templates select="mdb:contentInfo"/>
        <xsl:apply-templates select="mdb:distributionInfo"/>
        <xsl:apply-templates select="mdb:dataQualityInfo"/>
        <xsl:apply-templates select="mdb:resourceLineage"/>
        <xsl:apply-templates select="mdb:portrayalCatalogueInfo"/>
        <xsl:apply-templates select="mdb:metadataConstraints"/>
        <xsl:apply-templates select="mdb:applicationSchemaInfo"/>
        <xsl:apply-templates select="mdb:metadataMaintenance"/>
      </xsl:element>
    </xsl:for-each>
    <!-- end of main root element processing -->
  </xsl:template>
  
  <xsl:template match="mdb:metadataIdentifier" priority="5">
    <gmd:fileIdentifier>
      <gco:CharacterString>
        <xsl:choose>
          <xsl:when test="mcc:MD_Identifier/mcc:codeSpace/gco2:CharacterString">
            <xsl:value-of select="concat(mcc:MD_Identifier/mcc:codeSpace/gco2:CharacterString, ':',
                                         mcc:MD_Identifier/mcc:code/gco2:CharacterString)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="mcc:MD_Identifier/mcc:code/gco2:CharacterString"/>
          </xsl:otherwise>
        </xsl:choose>
        </gco:CharacterString>
    </gmd:fileIdentifier>
  </xsl:template>
  
  
  <xsl:template match="mdb:defaultLocale" priority="5">
    <gmd:language>
      <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/"
        codeListValue="{lan:PT_Locale/lan:language/lan:LanguageCode}"/>
    </gmd:language>
    <gmd:characterSet>
      <gmd:MD_CharacterSetCode 
        codeListValue="{lan:PT_Locale/lan:characterEncoding/lan:MD_CharacterSetCode}"
        codeList="http://www.isotc211.org/namespace/resources/codeList.xml#MD_CharacterSetCode"/>
    </gmd:characterSet>
  </xsl:template>
  
  
  <xsl:template match="mdb:parentMetadata" priority="5">
    <gmd:parentIdentifier>
      <gco:CharacterString>
        <xsl:value-of select="@uuidref|cit:CI_Citation/cit:title/gco2:CharacterString"/>
      </gco:CharacterString>
    </gmd:parentIdentifier>
  </xsl:template>
  

  <xsl:template match="mdb:metadataScope" priority="5">
    <!-- ISO19139 allows only one -->
    <xsl:if test="name(preceding-sibling::node()[1]) != name()">
      <gmd:hierarchyLevel>
        <gmd:MD_ScopeCode
            codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_ScopeCode"
            codeListValue="{mdb:MD_MetadataScope/mdb:resourceScope/mcc:MD_ScopeCode}"/>
      </gmd:hierarchyLevel>
      <xsl:if test="mdb:MD_MetadataScope/mdb:name">
       <gmd:hierarchyLevelName>
         <gco:CharacterString>
          <xsl:value-of select="mdb:MD_MetadataScope/mdb:name/gco2:CharacterString"/>
         </gco:CharacterString>
       </gmd:hierarchyLevelName>
      </xsl:if>
    </xsl:if>
  </xsl:template>
  
  
  <!-- Assume dateStamp is revision date in the source record. Standard says creation
  but implementations usually use date stamp as revision date. -->
  <xsl:template match="mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode='revision']" priority="5">
    <gmd:dateStamp>
      <xsl:apply-templates select="cit:CI_Date/cit:date/*"/>
    </gmd:dateStamp>
  </xsl:template>
  
  
  <xsl:template match="mdb:metadataStandard" priority="5">
    <gmd:metadataStandardName>
      <gco:CharacterString>
        <xsl:value-of select="cit:CI_Citation/cit:title/*"/>
      </gco:CharacterString>
    </gmd:metadataStandardName>
    <gmd:metadataStandardVersion>
      <gco:CharacterString>
        <xsl:value-of select="cit:CI_Citation/cit:edition/*"/>
      </gco:CharacterString>
    </gmd:metadataStandardVersion>
  </xsl:template>
  
  
  <xsl:template match="mdb:identificationInfo">
    <gmd:identificationInfo>
      <xsl:apply-templates select="@*"/>
      <xsl:for-each select="./*">
        <xsl:variable name="nameSpacePrefix">
          <xsl:call-template name="getNamespacePrefix"/>
        </xsl:variable>
        <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates select="mri:citation"/>
          <xsl:call-template name="writeCharacterStringElement">
            <xsl:with-param name="elementName" select="'gmd:abstract'"/>
            <xsl:with-param name="nodeWithStringToWrite" select="mri:abstract"/>
          </xsl:call-template>
          <xsl:call-template name="writeCharacterStringElement">
            <xsl:with-param name="elementName" select="'gmd:purpose'"/>
            <xsl:with-param name="nodeWithStringToWrite" select="mri:purpose"/>
          </xsl:call-template>
          <xsl:call-template name="writeCharacterStringElement">
            <xsl:with-param name="elementName" select="'gmd:credit'"/>
            <xsl:with-param name="nodeWithStringToWrite" select="mri:credit"/>
          </xsl:call-template>
          <xsl:call-template name="writeCodelistElement">
            <xsl:with-param name="elementName" select="'gmd:status'"/>
            <xsl:with-param name="codeListValue" select="mri:status/mri:MD_ProgressCode"/>
            <xsl:with-param name="codeListName" select="'gmd:MD_ProgressCode'"/>
          </xsl:call-template>
          <xsl:apply-templates select="mri:pointOfContact"/>
          <xsl:call-template name="writeCodelistElement">
            <xsl:with-param name="elementName" select="'gmd:spatialRepresentationType'"/>
            <xsl:with-param name="codeListName" select="'gmd:MD_SpatialRepresentationTypeCode'"/>
            <xsl:with-param name="codeListValue" select="mri:spatialRepresentationType/mri:MD_SpatialRepresentationTypeCode"/>
          </xsl:call-template>

          <xsl:apply-templates select="mri:resourceMaintenance"/>
          <xsl:apply-templates select="mri:graphicOverview"/>
          <xsl:apply-templates select="mri:resourceFormat"/>
          <xsl:apply-templates select="mri:descriptiveKeywords"/>
          <xsl:apply-templates select="mri:resourceSpecificUsage"/>
          <xsl:apply-templates select="mri:resourceConstraints"/>
          <xsl:apply-templates select="mri:associatedResource"/>
          <xsl:apply-templates select="mri:spatialResolution"/>
          <!-- This is here to handle early adopters of temporalResolution -->
          <xsl:apply-templates select="mri:temporalResolution"/>
          <xsl:apply-templates select="mri:language"/>
          <xsl:apply-templates select="mri:characterSet"/>
          <xsl:apply-templates select="mri:topicCategory"/>

          <xsl:call-template name="writeCharacterStringElement">
            <xsl:with-param name="elementName" select="'gmd:environmentDescription'"/>
            <xsl:with-param name="nodeWithStringToWrite" select="mri:environmentDescription"/>
          </xsl:call-template>

          <!-- Service Identification Information -->
          <xsl:if test="srv2:serviceType">
            <srv:serviceType>
              <gco:LocalName>
                <xsl:value-of select="srv2:serviceType/gco:ScopedName"/>
              </gco:LocalName>
            </srv:serviceType>
          </xsl:if>

          <xsl:call-template name="writeCharacterStringElement">
            <xsl:with-param name="elementName" select="'srv:serviceTypeVersion'"/>
            <xsl:with-param name="nodeWithStringToWrite" select="srv2:serviceTypeVersion"/>
          </xsl:call-template>

          <xsl:apply-templates select="mri:extent | srv:extent"/>
          <xsl:call-template name="writeCharacterStringElement">
            <xsl:with-param name="elementName" select="'gmd:supplementalInformation'"/>
            <xsl:with-param name="nodeWithStringToWrite" select="mri:supplementalInformation"/>
          </xsl:call-template>
          <xsl:call-template name="writeCodelistElement">
            <xsl:with-param name="elementName" select="'srv:couplingType'"/>
            <xsl:with-param name="codeListName" select="'srv:SV_CouplingType'"/>
            <xsl:with-param name="codeListValue" select="srv2:couplingType/srv2:SV_CouplingType"/>
          </xsl:call-template>
          <xsl:apply-templates select="srv2:containsOperations"/>
          <xsl:apply-templates select="srv2:operatesOn"/>
        </xsl:element>
      </xsl:for-each>
    </gmd:identificationInfo>
  </xsl:template>
  
  <xsl:template match="mdb:contentInfo">
    <gmd:contentInfo>
      <xsl:apply-templates select="*"/>
      <!-- TODO -->
    </gmd:contentInfo>
  </xsl:template>

  <xsl:template match="mri:associatedResource">
    <gmd:aggregationInfo>
      <gmd:MD_AggregateInformation>
        <xsl:apply-templates select="mri:MD_AssociatedResource/*"/>
      </gmd:MD_AggregateInformation>
    </gmd:aggregationInfo>
  </xsl:template>

  <xsl:template match="mri:MD_AssociatedResource/mri:name">
    <gmd:aggregateDataSetName>
      <xsl:apply-templates select="*"/>
    </gmd:aggregateDataSetName>
  </xsl:template>

  <xsl:template match="srv2:SV_ServiceIdentification/mri:extent" priority="2">
    <srv:extent>
      <xsl:apply-templates select="*"/>
    </srv:extent>
  </xsl:template>

  <xsl:template match="mdb:dataQualityInfo">
    <gmd:dataQualityInfo>
      <gmd:DQ_DataQuality>
        <xsl:if test="mdq:DQ_DataQuality/mdq:scope">
          <gmd:scope>
            <xsl:choose>
              <xsl:when test="mdq:DQ_DataQuality/mdq:scope/@*">
                <xsl:apply-templates select="mdq:DQ_DataQuality/mdq:scope/@*"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates select="mdq:DQ_DataQuality/mdq:scope/mcc:DQ_Scope/*"/>
              </xsl:otherwise>
            </xsl:choose>
          </gmd:scope>
        </xsl:if>
        
        <xsl:for-each select="mdq:DQ_DataQuality/mdq:report/*">
          <gmd:report>
            <xsl:element name="{concat('gmd:',local-name())}">
              <xsl:call-template name="writeCharacterStringElement">
                <xsl:with-param name="elementName" select="'gmd:nameOfMeasure'"/>
                <xsl:with-param name="nodeWithStringToWrite" select="mdq:measure/mdq:DQ_MeasureReference/mdq:nameOfMeasure"/>
              </xsl:call-template>
              <xsl:apply-templates select="mdq:measure/mdq:DQ_MeasureReference/mdq:measureIdentification"/>
              <xsl:call-template name="writeCharacterStringElement">
                <xsl:with-param name="elementName" select="'gmd:measureDescription'"/>
                <xsl:with-param name="nodeWithStringToWrite" select="mdq:measure/mdq:DQ_MeasureReference/mdq:measureDescription"/>
              </xsl:call-template>
              
              
              <xsl:call-template name="writeCodelistElement">
                <xsl:with-param name="elementName" select="'gmd:evaluationMethodType'"/>
                <xsl:with-param name="codeListName" select="'gmd:DQ_EvaluationMethodTypeCode'"/>
                <xsl:with-param name="codeListValue" select="mdq:evaluation/mdq:DQ_FullInspection/mdq:evaluationMethodType/mdq:DQ_EvaluationMethodTypeCode "/>
              </xsl:call-template>
              
              <xsl:call-template name="writeCharacterStringElement">
                <xsl:with-param name="elementName" select="'gmd:evaluationMethodDescription'"/>
                <xsl:with-param name="nodeWithStringToWrite" select="mdq:evaluation/mdq:DQ_FullInspection/mdq:evaluationMethodDescription"/>
              </xsl:call-template>
              
              <gmd:evaluationProcedure>
                <xsl:apply-templates select="mdq:evaluation/mdq:DQ_FullInspection/mdq:evaluationProcedure/cit:CI_Citation"/>
              </gmd:evaluationProcedure>
              <gmd:dateTime>
                <xsl:apply-templates select="mdq:evaluation/mdq:DQ_FullInspection/mdq:dateTime/gco2:DateTime"/>
              </gmd:dateTime>
              <xsl:apply-templates select="mdq:result"/>
            </xsl:element>
          </gmd:report>
        </xsl:for-each>
        
        
        <xsl:for-each select="/*/mdb:resourceLineage">
          <gmd:lineage>
            <gmd:LI_Lineage>
              <xsl:call-template name="writeCharacterStringElement">
                <xsl:with-param name="elementName" select="'gmd:statement'"/>
                <xsl:with-param name="nodeWithStringToWrite" select="mrl:LI_Lineage/mrl:statement"/>
              </xsl:call-template>
              
              <xsl:apply-templates select="mrl:LI_Lineage/mrl:processStep"/>
              <xsl:apply-templates select="mrl:LI_Lineage/mrl:source"/>
            </gmd:LI_Lineage>
          </gmd:lineage>
        </xsl:for-each>
      </gmd:DQ_DataQuality>
    </gmd:dataQualityInfo>
  </xsl:template>
  
  
  <xsl:template match="mmi:maintenanceDate">
    <gmd:dateOfNextUpdate>
      <xsl:apply-templates select="cit:CI_Date/cit:date/*"/>
    </gmd:dateOfNextUpdate>
  </xsl:template>


  <xsl:template match="mrl:stepDateTime|mri:usageDateTime">
    <gmd:dateTime>
      <gco:DateTime><xsl:value-of select="gml:TimeInstant/*"/></gco:DateTime>
      <!--<xsl:apply-templates select="*"/>-->
    </gmd:dateTime>
  </xsl:template>

  <xsl:template match="mdq:valueRecordType">
    <gmd:valueType>
      <xsl:apply-templates select="*"/>
    </gmd:valueType>
  </xsl:template>


  <xsl:template match="mdq:DQ_QuantitativeResult">
    <gmd:DQ_QuantitativeResult>
      <xsl:apply-templates select="mdq:valueRecordType"/>
      <xsl:apply-templates select="mdq:valueUnit"/>
      <xsl:apply-templates select="mdq:value"/>
    </gmd:DQ_QuantitativeResult>
  </xsl:template>




  <xsl:template match="cit:CI_Citation">
    <xsl:element name="gmd:CI_Citation">
      <xsl:apply-templates/>
      <!-- Special attention is required for CI_ResponsibleParties that are included in the CI_Citation only for a URL. These are currently identified as those 
        with no name elements (individualName, organisationName, or positionName)
      -->
      <xsl:for-each select=".//cit:CI_Responsibility[
        count(cit:party/cit:CI_Organisation/cit:individual/cit:CI_Individual/cit:name/gco2:CharacterString) +
        count(cit:party/cit:CI_Organisation/cit:individual/cit:CI_Individual/cit:positionName/gco2:CharacterString) +
        count(cit:party/cit:CI_Organisation/cit:organisationName/gco2:CharacterString) = 0]">
        <xsl:call-template name="CI_ResponsiblePartyToOnlineResource"/>
      </xsl:for-each>
    </xsl:element>
  </xsl:template>
  <xsl:template match="cit:CI_Citation/cit:date">
    <gmd:date>
      <xsl:apply-templates select="@*"/>
      <xsl:choose>
        <xsl:when test="@gco2:nilReason"/>
        <xsl:otherwise>
          <gmd:CI_Date>
            <gmd:date>
              <xsl:choose>
                <xsl:when test="descendant::gmd:date/@gco2:nilReason">
                  <xsl:copy-of select="descendant::gmd:date/@gco2:nilReason"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:call-template name="writeDateTime"/>
                </xsl:otherwise>
              </xsl:choose>
            </gmd:date>
            <xsl:for-each select="descendant::cit:dateType">
              <xsl:call-template name="writeCodelistElement">
                <xsl:with-param name="elementName" select="'gmd:dateType'"/>
                <xsl:with-param name="codeListName" select="'gmd:CI_DateTypeCode'"/>
                <xsl:with-param name="codeListValue" select="cit:CI_DateTypeCode"/>
              </xsl:call-template>
            </xsl:for-each>
          </gmd:CI_Date>
        </xsl:otherwise>
      </xsl:choose>
    </gmd:date>
  </xsl:template>
  <xsl:template match="cit:CI_Citation/cit:editionDate">
    <gmd:editionDate>
      <xsl:call-template name="writeDateTime"/>
    </gmd:editionDate>
  </xsl:template>
  <xsl:template match="cit:CI_Responsibility" priority="5">
    <xsl:choose>
      <xsl:when test="count(cit:party/cit:CI_Organisation/cit:individual/cit:CI_Individual/cit:name/gco2:CharacterString) +
        count(cit:party/cit:CI_Organisation/cit:individual/cit:CI_Individual/cit:positionName/gco2:CharacterString) +
        count(cit:party/cit:CI_Individual/cit:name/gco2:CharacterString) +
        count(cit:party/cit:CI_Individual/cit:positionName/gco2:CharacterString) +
        count(cit:party/cit:CI_Organisation/cit:name/gco2:CharacterString) > 0">
        <!-- 
          CI_ResponsibleParties that include name elements (individualName, organisationName, or positionName) are translated to CI_Responsibilities.
          CI_ResponsibleParties without name elements are assummed to be placeholders for CI_OnlineResources. They are transformed later in the process
          using the CI_ResponsiblePartyToOnlineReseource template
        -->
        <xsl:element name="gmd:CI_ResponsibleParty">
          <xsl:apply-templates select="@*"/>
          <xsl:if test="cit:party/cit:CI_Organisation/cit:individual/cit:CI_Individual/cit:name|
            cit:party/cit:CI_Individual/cit:name">
            <xsl:call-template name="writeCharacterStringElement">
              <xsl:with-param name="elementName" select="'gmd:individualName'"/>
              <xsl:with-param name="nodeWithStringToWrite" select="cit:party/cit:CI_Organisation/cit:individual/cit:CI_Individual/cit:name|
                cit:party/cit:CI_Individual/cit:name"/>
            </xsl:call-template>
          </xsl:if>
          
          <xsl:if test="cit:party/cit:CI_Organisation/cit:name">
            <xsl:call-template name="writeCharacterStringElement">
              <xsl:with-param name="elementName" select="'gmd:organisationName'"/>
              <xsl:with-param name="nodeWithStringToWrite" select="cit:party/cit:CI_Organisation/cit:name"/>
            </xsl:call-template>
          </xsl:if>

          <xsl:if test="cit:party/cit:CI_Organisation/cit:individual/cit:CI_Individual/cit:positionName|
            cit:party/cit:CI_Individual/cit:positionName">
            <xsl:call-template name="writeCharacterStringElement">
              <xsl:with-param name="elementName" select="'gmd:positionName'"/>
              <xsl:with-param name="nodeWithStringToWrite" select="cit:party/cit:CI_Organisation/cit:individual/cit:CI_Individual/cit:positionName|
                cit:party/cit:CI_Individual/cit:positionName"/>
            </xsl:call-template>
          </xsl:if>

          <!-- contactInformation comes before indivudual/position -->
          <xsl:call-template name="writeContactInformation"/>

          <xsl:choose>
            <xsl:when test="./cit:role/cit:CI_RoleCode">
              <xsl:call-template name="writeCodelistElement">
                <xsl:with-param name="elementName" select="'gmd:role'"/>
                <xsl:with-param name="codeListName" select="'gmd:CI_RoleCode'"/>
                <xsl:with-param name="codeListValue" select="cit:role/cit:CI_RoleCode/@codeListValue"/>
              </xsl:call-template>
            </xsl:when>
            <xsl:when test="./gmd:role/@*">
              <gmd:role>
                <xsl:apply-templates select="@*"/>
              </gmd:role>
            </xsl:when>
            <xsl:otherwise>
              <gmd:role gco:nilReason="missing"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="writeContactInformation">
    <xsl:for-each select="cit:party/*/cit:contactInfo">
      <gmd:contactInfo>
        <xsl:apply-templates/>
      </gmd:contactInfo>
    </xsl:for-each>
  </xsl:template>
  <xsl:template match="cit:party/*/cit:contactInfo/cit:CI_Contact/cit:phone">
    <!-- Only one phone number is allowed in ISO19139 -->
    <xsl:variable name="phoneType" 
                  select="if (cit:numberType/cit:CI_TelephoneTypeCode/@codeListValue != '')
                          then cit:numberType/cit:CI_TelephoneTypeCode/@codeListValue 
                          else cit:numberType/cit:CI_TelephoneTypeCode"/>
    <xsl:if test="count(preceding-sibling::node()[cit:numberType/cit:CI_TelephoneTypeCode = $phoneType]) = 0">
      <xsl:for-each select="cit:CI_Telephone">
        <gmd:phone>
          <gmd:CI_Telephone>
            <xsl:for-each select=".[cit:numberType/cit:CI_TelephoneTypeCode = 'voice' or
              cit:numberType/cit:CI_TelephoneTypeCode/@codeListValue = 'voice']/cit:number">
              <gmd:voice>
                <xsl:apply-templates select="gco2:CharacterString"/>
              </gmd:voice>
            </xsl:for-each>
            <xsl:for-each select=".[cit:numberType/cit:CI_TelephoneTypeCode = 'facsimile' or
              cit:numberType/cit:CI_TelephoneTypeCode/@codeListValue = 'facsimile']/cit:number">
              <gmd:facsimile>
                <xsl:apply-templates select="gco2:CharacterString"/>
              </gmd:facsimile>
            </xsl:for-each>
          </gmd:CI_Telephone>
        </gmd:phone>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>
  
  
  
  <xsl:template name="CI_ResponsiblePartyToOnlineResource">
    <!-- 
      Transform only the CI_OnlineResource element of the CI_ResponsibleParty 
    -->
    <xsl:apply-templates select=".//gmd:onlineResource"/>
  </xsl:template>
  <xsl:template match="cit:CI_OnlineResource/cit:linkage/gco2:CharacterString">
    <gmd:URL>
      <xsl:value-of select="."/>
    </gmd:URL>
  </xsl:template>
  <xsl:template match="mrd:MD_Format">
    <xsl:element name="gmd:MD_Format">
      <gmd:name>
        <xsl:apply-templates select="mrd:formatSpecificationCitation/cit:CI_Citation/cit:title/*"/>
      </gmd:name>
      <gmd:version>
        <xsl:apply-templates select="mrd:formatSpecificationCitation/cit:CI_Citation/cit:edition/*"/>
      </gmd:version>
      
      <xsl:apply-templates select="mrd:amendmentNumber"/>
      
      <xsl:if test="mrd:formatSpecificationCitation/cit:CI_Citation/cit:alternateTitle">
       <gmd:specification>
         <xsl:apply-templates select="mrd:formatSpecificationCitation/cit:CI_Citation/cit:alternateTitle/*"/>
       </gmd:specification>
      </xsl:if>
      <xsl:apply-templates select="mrd:fileDecompressionTechnique|mrd:formatDistributor"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="srv2:distributedComputingPlatform">
    <srv:DCP>
      <xsl:apply-templates select="*"/>
    </srv:DCP>
  </xsl:template>

  <xsl:template match="mcc:MD_Identifier">
    <gmd:RS_Identifier>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </gmd:RS_Identifier>
  </xsl:template>

  <xsl:template match="*">
    <xsl:variable name="nameSpacePrefix">
      <xsl:call-template name="getNamespacePrefix"/>
    </xsl:variable>
    <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="gml32:*">
    <xsl:element name="{local-name(.)}"
                 namespace="http://www.opengis.net/gml">
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="@gml32:*">
    <xsl:attribute name="gml:{local-name()}">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="@gco2:*">
    <xsl:attribute name="gco:{local-name()}">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="@*">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  <xsl:template name="writeCodelistElement">
    <xsl:param name="elementName"/>
    <xsl:param name="codeListName"/>
    <xsl:param name="codeListValue"/>
    <!-- The correct codeList Location goes here -->
    <xsl:variable name="codeListLocation" select="'codeListLocation'"/>
    <xsl:if test="$codeListValue">
      <xsl:element name="{$elementName}">
        <xsl:element name="{$codeListName}">
          <xsl:attribute name="codeList">
            <xsl:value-of select="$codeListLocation"/>
            <xsl:value-of select="'#'"/>
            <xsl:value-of select="substring-after($codeListName,':')"/>
          </xsl:attribute>
          <xsl:attribute name="codeListValue">
            <!-- the anyValidURI value is used for testing with paths -->
            <xsl:value-of select="$codeListValue"/>
            <!-- commented out for testing -->
            <!--<xsl:value-of select="$codeListValue"/>-->
          </xsl:attribute>
          <xsl:value-of select="$codeListValue"/>
        </xsl:element>
      </xsl:element>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="writeCharacterStringElement">
    <xsl:param name="elementName"/>
    <xsl:param name="nodeWithStringToWrite"/>
    
    <xsl:variable name="isMultilingual"
      select="count($nodeWithStringToWrite/gmd:PT_FreeText) > 0"/>
    <xsl:variable name="hasCharacterString"
      select="count($nodeWithStringToWrite/gco2:CharacterString) = 1"/>
    
    <xsl:choose>
      <xsl:when test="$nodeWithStringToWrite">
        <xsl:element name="{$elementName}">
          <xsl:copy-of select="$nodeWithStringToWrite/@*"/>
          <xsl:if test="$isMultilingual">
            <xsl:attribute name="xsi:type" select="'gmd:PT_FreeText_PropertyType'"/>
          </xsl:if>
          
          <xsl:if test="$hasCharacterString">
            <gco:CharacterString>
              <xsl:value-of select="$nodeWithStringToWrite/gco2:CharacterString"/>
            </gco:CharacterString>
          </xsl:if>
          <xsl:if test="$isMultilingual">
            <xsl:copy-of select="$nodeWithStringToWrite/gmd:PT_FreeText"/>
          </xsl:if>
        </xsl:element>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  
  
  
  <xsl:template name="characterStringSubstitutions">
    <xsl:param name="parentElement"/>
    <!-- This template takes a parent of a gco:CharacterString element and writes out the child for several possible substitutions  -->
    <xsl:for-each select="$parentElement/*">
      <xsl:choose>
        <xsl:when test="local-name(.)='CharacterString'">
          <xsl:copy-of select="."/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:element name="{concat('gmx:',local-name(.))}">
            <xsl:copy-of select="@*" copy-namespaces="no"/>
            <xsl:value-of select="."/>
          </xsl:element>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="writeDateTime">
    <!--
      have to account for gco:Date and gco:DateTime which are both valid descendants of gmd:date
     -->
    <gco:DateTime>
      <xsl:copy-of select="@* | descendant::gmd:date/@*"/>
      <xsl:for-each select="descendant::gco2:Date">
        <xsl:variable name="dateNodeString">
          <xsl:value-of select="xs:string(.)"/>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="string-length($dateNodeString)=6">
            <xsl:value-of select="concat(substring($dateNodeString,1,4),'-',substring($dateNodeString,5,2),'-01T00:00:00')"/>
          </xsl:when>
          <xsl:when test="string-length($dateNodeString)=7">
            <xsl:value-of select="concat(substring($dateNodeString,1,4),'-',substring($dateNodeString,6,2),'-01T00:00:00')"/>
          </xsl:when>
          <xsl:when test="string-length($dateNodeString)=8">
            <xsl:value-of select="concat(substring($dateNodeString,1,4),'-',substring($dateNodeString,5,2),'-',substring($dateNodeString,7,2),'T00:00:00')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat($dateNodeString,'T00:00:00')"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
      <xsl:for-each select="descendant::gco2:DateTime">
        <xsl:value-of select="."/>
      </xsl:for-each>
    </gco:DateTime>
  </xsl:template>
  
  <xsl:template name="getNamespacePrefix">
    <!-- this template determines the correct namespace prefix depending on the position of the element in the new XML -->
    <xsl:variable name="prefix">
      <xsl:choose>
        <xsl:when test="starts-with(name(),'gcx:')">
          <xsl:text>gmx</xsl:text>
        </xsl:when>
        <xsl:when test="starts-with(name(),'gco:')">
          <xsl:text>gco</xsl:text>
        </xsl:when>
        <xsl:when test="starts-with(name(),'gml:')">
          <xsl:text>gml</xsl:text>
        </xsl:when>
        <xsl:when test="starts-with(name(),'gts:')">
          <xsl:text>gts</xsl:text>
        </xsl:when>
        <xsl:when test="starts-with(name(),'srv:') and not(name()='srv:extent')">
          <xsl:text>srv</xsl:text>
        </xsl:when>
        <xsl:when test="ancestor-or-self::mco:MD_Constraints
          or ancestor-or-self::mco:MD_SecurityConstraints 
          or ancestor-or-self::mco:MD_LegalConstraints
          ">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <xsl:when test="ancestor-or-self::mcc:MD_BrowseGraphic">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <xsl:when test="ancestor-or-self::cit:CI_ResponsibleParty or ancestor-or-self::cit:CI_OnlineResource">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <xsl:when test="ancestor-or-self::mcc:MD_ScopeCode or ancestor-or-self::mcc:MX_ScopeCode 
          or ancestor-or-self::mcc:MD_ScopeDescription">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <xsl:when test="parent::mcc:MD_Identifier or self::mcc:MD_Identifier or parent::mcc:RS_Identifier or self::mcc:RS_Identifier">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <!--
          Changed 2013-03-06 to fix PresentationFormCode <xsl:when test="parent::gmd:CI_Citation or self::gmd:CI_Citation">-->
        <xsl:when test="ancestor-or-self::cit:CI_Citation">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <xsl:when test="ancestor-or-self::mpc:MD_PortrayalCatalogueReference">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <!--<xsl:when test="ancestor-or-self::gmd:MD_SpatialRepresentationTypeCode">
          <xsl:text>msr</xsl:text>
        </xsl:when>-->
        <xsl:when test="ancestor-or-self::mrs:MD_ReferenceSystem">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <xsl:when test="ancestor-or-self::mex:MD_MetadataExtensionInformation">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <xsl:when test="ancestor-or-self::gex:EX_Extent">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <xsl:when test="ancestor-or-self::msr:MD_Georectified or ancestor-or-self::msr:MI_Georectified
          or ancestor-or-self::msr:MD_Georeferenceable or ancestor-or-self::msr:MI_Georeferenceable
          or ancestor-or-self::msr:MD_GridSpatialRepresentation or ancestor-or-self::msr:MD_ReferenceSystem">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <xsl:when test="ancestor-or-self::mcc:DQ_Scope">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <xsl:when test="ancestor-or-self::mrd:MD_Distribution or ancestor-or-self::mrd:MD_Format">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <xsl:when test="ancestor-or-self::mri:MD_Resolution or ancestor-or-self::mri:MD_RepresentativeFraction or ancestor-or-self::mri:MD_VectorSpatialRepresentation">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <xsl:when test="ancestor-or-self::mmi:MD_MaintenanceInformation">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <xsl:when test="ancestor-or-self::mri:MD_DataIdentification 
          or ancestor-or-self::mri:MD_SpatialRepresentationTypeCode">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <xsl:when test="ancestor-or-self::mri:SV_ServiceIdentification">
          <xsl:text>srv</xsl:text>
        </xsl:when>
        <xsl:when test="ancestor-or-self::mrc:MD_CoverageDescription or ancestor-or-self::mrc:MI_CoverageDescription">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <xsl:when test="ancestor-or-self::mrl:LI_Lineage">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <xsl:when test="ancestor-or-self::mdq:DQ_DataQuality">
          <xsl:text>gmd</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>gmd</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="$prefix"/>
  </xsl:template>
  
  
  <!-- TODO: filter all new elements in ISO19115-3 not catched
  by previous templates. Check annex G.2 of ISO19115-1. -->
  <xsl:template match="cit:individual|
                       cit:party|
                       cit:graphic|
                       cit:CI_Citation/cit:onlineResource|
                       srv2:parameter|
                       mri:keywordClass|
                       mrd:formatSpecificationCitation|
                       mdb:dateInfo|
                       mdb:metadataProfile|
                       mdb:alternativeMetadataReference|
                       mdb:metadataLinkage|
                       mdb:resourceLineage|
                       mrl:LI_Source/mrl:scope|
                       mrl:sourceSpatialResolution|
                       mdq:derivedElement" priority="2"/>
</xsl:stylesheet>
