<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                  xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                  xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                  xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                  xmlns:xs="http://www.w3.org/2001/XMLSchema"
                  xmlns:geonet="http://www.fao.org/geonetwork"
                  xmlns:util="java:org.fao.geonet.util.XslUtil"
                  xmlns:saxon="http://saxon.sf.net/"
                  extension-element-prefixes="saxon"
                  exclude-result-prefixes="#all">

  <xsl:import href="sibling-utility.xsl"/>
  <xsl:import href="../../iso19139/process/process-utility.xsl"/>
  <xsl:include href="../../iso19139/process/collection-merge-utility.xsl"/>

  <!--
  Create or update a record by combining information from a collection of records.
  -->
  <xsl:output method="xml" indent="yes"/>

  <!-- Comma separated list of UUIDs of the collection members.
  uuid,... or
  uuid#associationType#initiativeType#title#remoteUrl,...
  -->
  <xsl:param name="newProductMemberUuids"
             select="''"/>

  <!-- (optional) The association type. -->
  <xsl:param name="associationType" select="'isComposedOf'"/>

  <!-- (optional) The initiative type. -->
  <xsl:param name="initiativeType" select="''"/>

  <xsl:param name="updateAllFromMembers" select="true()"/>

  <xsl:param name="withXlink" select="false()"/>

  <xsl:variable name="metadata"
                select="/"/>

  <xsl:variable name="members">
    <xsl:for-each select="tokenize($newProductMemberUuids, ',')">
      <record uuid="{.}">
        <xsl:variable name="xmlDocument" select="util:getRecord(., $metadata/*/geonet:info/schema)"/>
        <xsl:copy-of select="$xmlDocument"/>
      </record>
    </xsl:for-each>
  </xsl:variable>

  <xsl:variable name="existingMemberUuids"
                select="if ($newProductMemberUuids != '')
                        then $newProductMemberUuids
                        else string-join(.//mri:associatedResource/*/mri:metadataReference/@uuidref, ',')"/>

  <xsl:variable name="existingMembers">
    <xsl:for-each select="tokenize($existingMemberUuids, ',')">
      <record uuid="{.}">
        <xsl:variable name="xmlDocument" select="util:getRecord(., $metadata/*/geonet:info/schema)"/>
        <xsl:copy-of select="$xmlDocument"/>
      </record>
    </xsl:for-each>
  </xsl:variable>


  <xsl:variable name="elements" as="node()*">
    <tag name="mri:descriptiveKeywords" context="mri:MD_DataIdentification|srv:SV_ServiceIdentification"
         groupBy="*/mri:thesaurusName/*/cit:title/*/text()"
         merge="mri:keyword"/>
    <!--<tag name="mri:extent" context="mri:MD_DataIdentification|srv:SV_ServiceIdentification"
         groupBy="*/(gex:geographicElement|gex:temporalElement)"
         merge="gex:geographicElement|gex:temporalElement"/>-->
    <tag name="gex:geographicElement" context="gex:EX_Extent"
         groupBy="*"
         merge="."/>
    <tag name="gex:temporalElement" context="gex:EX_Extent"
         groupBy="*"
         merge="."/>
    <!-- TODO: mri:defaultLocale can be in various places. -->
    <tag name="mri:defaultLocale" context="mri:MD_DataIdentification|srv:SV_ServiceIdentification"
         groupBy="mri:defaultLocaleCode/lan:PT_Locale/lan:language/lan:LanguageCode/@codeListValue"
         merge="mri:defaultLocaleCode"/>
    <tag name="mri:graphicOverview" context="mri:MD_DataIdentification|srv:SV_ServiceIdentification"
         groupBy="*/mcc:fileName/*/text()"
         merge="."
         limit="1"/>
    <tag name="mri:spatialRepresentationType" context="mri:MD_DataIdentification|srv:SV_ServiceIdentification"
         groupBy="mcc:MD_SpatialRepresentationTypeCode/@codeListValue"
         merge="mri:spatialRepresentationType"/>
    <tag name="mri:spatialResolution" context="mri:MD_DataIdentification|srv:SV_ServiceIdentification"
         groupBy="*/mri:equivalentScale/*/mri:denominator/*/text()"
         merge="mri:spatialResolution"/>
    <tag name="mri:topicCategory" context="mri:MD_DataIdentification|srv:SV_ServiceIdentification"
         groupBy="mri:MD_TopicCategoryCode/text()"
         merge="mri:MD_TopicCategoryCode"/>
    <tag name="mdb:contact" context="mdb:MD_Metadata"
         groupBy="*/cit:party/*/cit:contactInfo/*/cit:address/*/cit:electronicMailAddress/*/text()"
         merge="."/>
    <tag name="mri:pointOfContact" context="mri:MD_DataIdentification|srv:SV_ServiceIdentification"
         groupBy="*/cit:party/*/cit:contactInfo/*/cit:address/*/cit:electronicMailAddress/*/text()"
         merge="."/>
  </xsl:variable>


  <!-- Suggesting -->
  <xsl:variable name="collection-updater-loc">
    <msg id="a" xml:lang="eng">Update collection</msg>
    <msg id="a" xml:lang="fre">Mettre à jour la collection</msg>
  </xsl:variable>

  <xsl:template name="list-collection-updater">
    <suggestion process="collection-updater"/>
  </xsl:template>

  <!-- Analyze the metadata record and return available suggestion
    for that process -->
  <xsl:template name="analyze-collection-updater">
    <xsl:param name="root"/>
    <xsl:variable name="isACollection"
                  select="$root//mdb:metadataScope/*/mdb:resourceScope/*/@codeListValue = ('series', 'service')"/>
    <xsl:variable name="hasMembers"
                  select="$existingMemberUuids != ''"/>

    <xsl:if test="$isACollection and $hasMembers">
      <suggestion process="collection-updater" id="{generate-id()}" category="collection"
                  target="">
        <name xml:lang="en">
          <xsl:value-of select="geonet:i18n($collection-updater-loc, 'a', $guiLang)"/>
        </name>
        <operational>true</operational>
        <form/>
      </suggestion>
    </xsl:if>
  </xsl:template>



  <!-- Processing -->
  <xsl:template match="/">
    <xsl:variable name="expandedRecord">
      <xsl:apply-templates mode="expand" select="."/>
    </xsl:variable>

    <xsl:apply-templates mode="merge" select="$expandedRecord"/>
  </xsl:template>

  <!-- Create empty elements that will be populated by the merge
  and may not exist in current record.-->
  <xsl:template match="mri:MD_DataIdentification|srv:SV_ServiceIdentification" mode="expand">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="mri:citation" mode="expand"/>
      <xsl:apply-templates select="mri:abstract" mode="expand"/>
      <xsl:apply-templates select="mri:purpose" mode="expand"/>
      <xsl:apply-templates select="mri:credit" mode="expand"/>
      <xsl:apply-templates select="mri:status" mode="expand"/>

      <xsl:call-template name="copyOrAddElement">
        <xsl:with-param name="elements" select="mri:pointOfContact"/>
        <xsl:with-param name="name" select="'mri:pointOfContact'"/>
      </xsl:call-template>
      <xsl:call-template name="copyOrAddElement">
        <xsl:with-param name="elements" select="mri:spatialRepresentationType"/>
        <xsl:with-param name="name" select="'mri:spatialRepresentationType'"/>
      </xsl:call-template>
      <xsl:call-template name="copyOrAddElement">
        <xsl:with-param name="elements" select="mri:spatialResolution"/>
        <xsl:with-param name="name" select="'mri:spatialResolution'"/>
      </xsl:call-template>
      <xsl:call-template name="copyOrAddElement">
        <xsl:with-param name="elements" select="mri:temporalResolution"/>
        <xsl:with-param name="name" select="'mri:temporalResolution'"/>
      </xsl:call-template>
      <xsl:call-template name="copyOrAddElement">
        <xsl:with-param name="elements" select="mri:topicCategory"/>
        <xsl:with-param name="name" select="'mri:topicCategory'"/>
      </xsl:call-template>

      <mri:extent>
        <gex:EX_Extent>
          <gex:geographicElement/>
          <gex:temporalElement/>
        </gex:EX_Extent>
      </mri:extent>

      <xsl:apply-templates select="mri:additionalDocumentation" mode="expand"/>
      <xsl:apply-templates select="mri:processingLevel" mode="expand"/>
      <xsl:apply-templates select="mri:resourceMaintenance" mode="expand"/>

      <xsl:call-template name="copyOrAddElement">
        <xsl:with-param name="elements" select="mri:graphicOverview"/>
        <xsl:with-param name="name" select="'mri:graphicOverview'"/>
      </xsl:call-template>
      <xsl:apply-templates select="mri:resourceFormat" mode="expand"/>
      <xsl:call-template name="copyOrAddElement">
        <xsl:with-param name="elements" select="mri:descriptiveKeywords"/>
        <xsl:with-param name="name" select="'mri:descriptiveKeywords'"/>
      </xsl:call-template>
      <xsl:apply-templates select="mri:resourceSpecificUsage" mode="expand"/>
      <xsl:apply-templates select="mri:resourceConstraints" mode="expand"/>

      <xsl:apply-templates select="mri:associatedResource" mode="expand"/>
      <xsl:call-template name="addAssociatedResources"/>

      <xsl:call-template name="copyOrAddElement">
        <xsl:with-param name="elements" select="mri:defaultLocale"/>
        <xsl:with-param name="name" select="'mri:defaultLocale'"/>
      </xsl:call-template>
      <xsl:apply-templates select="mri:otherLocale" mode="expand"/>
      <xsl:apply-templates select="mri:environmentDescription" mode="expand"/>
      <xsl:apply-templates select="mri:supplementalInformation" mode="expand"/>

      <xsl:apply-templates select="srv:*" mode="expand"/>
    </xsl:copy>
  </xsl:template>



  <xsl:template match="mdb:identificationInfo/*/mri:citation/*" mode="expand">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="cit:title
                                  |cit:alternateTitle"
                           mode="expand"/>

      <xsl:for-each-group select="$existingMembers//mdb:MD_Metadata/mdb:identificationInfo
            /*/mri:citation/*/cit:date[*/cit:dateType/*/@codeListValue = 'publication']"
                          group-by="*/cit:date/gco:*">
        <xsl:sort select="*/cit:date/gco:*" order="descending"/>

        <xsl:if test="position() = 1">
          <xsl:copy-of select="."/>
        </xsl:if>
      </xsl:for-each-group>

      <xsl:apply-templates select="cit:edition
                                   |cit:editionDate
                                   |cit:identifier
                                   |cit:citedResponsibleParty
                                   |cit:presentationForm
                                   |cit:series
                                   |cit:otherCitationDetails
                                   |cit:collectiveTitle
                                   |cit:ISBN
                                   |cit:ISSN
                                   |cit:onlineResource
                                   |cit:graphic" mode="expand"/>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="@*|node()" mode="expand">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="expand"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="copyOrAddElement">
    <xsl:param name="elements" as="node()*"/>
    <xsl:param name="name" as="xs:string"/>
    <xsl:choose>
      <xsl:when test="count($elements) > 0">
        <xsl:copy-of select="$elements"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- TODO: Only add if one exists in members -->
        <xsl:element name="{$name}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



  <xsl:template name="addAssociatedResources">
    <xsl:variable name="context" select="."/>

    <xsl:if test="$newProductMemberUuids != ''">
      <xsl:for-each select="tokenize($newProductMemberUuids, ',')">
        <xsl:choose>
          <xsl:when test="contains(., '#')">
            <xsl:variable name="tokens" select="tokenize(., '#')"/>
            <xsl:call-template name="make-aggregate">
              <xsl:with-param name="uuid" select="$tokens[1]"/>
              <xsl:with-param name="context" select="$context"/>
              <xsl:with-param name="associationType" select="$tokens[2]"/>
              <xsl:with-param name="initiativeType" select="$tokens[3]"/>
              <xsl:with-param name="title" select="$tokens[4]"/>
              <xsl:with-param name="remoteUrl" select="$tokens[5]"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <!-- Same initiative type and association type
                        for all siblings -->
            <xsl:call-template name="make-aggregate">
              <xsl:with-param name="uuid" select="."/>
              <xsl:with-param name="context" select="$context"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()" mode="merge">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="merge"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2" mode="merge"/>

</xsl:stylesheet>
