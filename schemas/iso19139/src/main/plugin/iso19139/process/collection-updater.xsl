<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                  xmlns:gco="http://www.isotc211.org/2005/gco"
                  xmlns:gmd="http://www.isotc211.org/2005/gmd"
                  xmlns:srv="http://www.isotc211.org/2005/srv"
                  xmlns:gmx="http://www.isotc211.org/2005/gmx"
                  xmlns:xlink="http://www.w3.org/1999/xlink"
                  xmlns:xs="http://www.w3.org/2001/XMLSchema"
                  xmlns:geonet="http://www.fao.org/geonetwork"
                  xmlns:util="java:org.fao.geonet.util.XslUtil"
                  xmlns:saxon="http://saxon.sf.net/"
                  extension-element-prefixes="saxon"
                  exclude-result-prefixes="#all">

  <xsl:import href="process-utility.xsl"/>
  <xsl:include href="collection-merge-utility.xsl"/>

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
                        else string-join(.//gmd:aggregationInfo/*/gmd:aggregateDataSetIdentifier/*/gmd:code/*/text(), ',')"/>

  <xsl:variable name="existingMembers">
    <xsl:for-each select="tokenize($existingMemberUuids, ',')">
      <record uuid="{.}">
        <xsl:variable name="xmlDocument" select="util:getRecord(., $metadata/*/geonet:info/schema)"/>
        <xsl:copy-of select="$xmlDocument"/>
      </record>
    </xsl:for-each>
  </xsl:variable>


  <xsl:variable name="elements" as="node()*">
    <tag name="gmd:descriptiveKeywords" context="gmd:MD_DataIdentification|srv:SV_ServiceIdentification"
         groupBy="*/gmd:thesaurusName/*/gmd:title/*/text()"
         merge="gmd:keyword"/>
    <tag name="gmd:geographicElement" context="gmd:EX_Extent"
         groupBy="*"
         merge="."/>
    <tag name="gmd:temporalElement" context="gmd:EX_Extent"
         groupBy="*"
         merge="."/>
    <!-- TODO: gmd:language can be in various places. -->
    <tag name="gmd:language" context="gmd:MD_DataIdentification|srv:SV_ServiceIdentification"
         groupBy="gmd:LanguageCode/@codeListValue"
         merge="gmd:LanguageCode"/>
    <tag name="gmd:graphicOverview" context="gmd:MD_DataIdentification|srv:SV_ServiceIdentification"
         groupBy="*/gmd:fileName/*/text()"
         merge="."
         limit="1"/>
    <tag name="gmd:characterSet" context="gmd:MD_DataIdentification|srv:SV_ServiceIdentification"
         groupBy="gmd:MD_CharacterSetCode/@codeListValue"
         merge="gmd:characterSet"/>
    <tag name="gmd:spatialRepresentationType" context="gmd:MD_DataIdentification|srv:SV_ServiceIdentification"
         groupBy="gmd:MD_SpatialRepresentationTypeCode/@codeListValue"
         merge="gmd:spatialRepresentationType"/>
    <tag name="gmd:spatialResolution" context="gmd:MD_DataIdentification|srv:SV_ServiceIdentification"
         groupBy="*/gmd:equivalentScale/*/gmd:denominator/*/text()"
         merge="gmd:spatialResolution"/>
    <tag name="gmd:topicCategory" context="gmd:MD_DataIdentification|srv:SV_ServiceIdentification"
         groupBy="gmd:MD_TopicCategoryCode/text()"
         merge="gmd:MD_TopicCategoryCode"/>
    <tag name="gmd:contact" context="gmd:MD_Metadata"
         groupBy="*/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/*/text()"
         merge="."/>
    <tag name="gmd:pointOfContact" context="gmd:MD_DataIdentification|srv:SV_ServiceIdentification"
         groupBy="*/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/*/text()"
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
                  select="$root//gmd:hierarchyLevel/*/@codeListValue = ('series', 'service')"/>
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
  <xsl:template match="gmd:MD_DataIdentification|srv:SV_ServiceIdentification" mode="expand">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="gmd:citation" mode="expand"/>
      <xsl:apply-templates select="gmd:abstract" mode="expand"/>
      <xsl:apply-templates select="gmd:purpose" mode="expand"/>
      <xsl:apply-templates select="gmd:credit" mode="expand"/>
      <xsl:apply-templates select="gmd:status" mode="expand"/>
      <xsl:call-template name="copyOrAddElement">
        <xsl:with-param name="elements" select="gmd:pointOfContact"/>
        <xsl:with-param name="name" select="'gmd:pointOfContact'"/>
      </xsl:call-template>
      <xsl:apply-templates select="gmd:resourceMaintenance" mode="expand"/>
      <xsl:call-template name="copyOrAddElement">
        <xsl:with-param name="elements" select="gmd:graphicOverview"/>
        <xsl:with-param name="name" select="'gmd:graphicOverview'"/>
      </xsl:call-template>
      <xsl:apply-templates select="gmd:resourceFormat" mode="expand"/>
      <xsl:call-template name="copyOrAddElement">
        <xsl:with-param name="elements" select="gmd:descriptiveKeywords"/>
        <xsl:with-param name="name" select="'gmd:descriptiveKeywords'"/>
      </xsl:call-template>
      <xsl:apply-templates select="gmd:resourceSpecificUsage" mode="expand"/>
      <xsl:apply-templates select="gmd:resourceConstraints" mode="expand"/>

      <xsl:apply-templates select="gmd:aggregationInfo" mode="expand"/>
      <xsl:call-template name="addAssociatedResources"/>

      <xsl:call-template name="copyOrAddElement">
        <xsl:with-param name="elements" select="gmd:spatialRepresentationType"/>
        <xsl:with-param name="name" select="'gmd:spatialRepresentationType'"/>
      </xsl:call-template>
      <xsl:call-template name="copyOrAddElement">
        <xsl:with-param name="elements" select="gmd:spatialResolution"/>
        <xsl:with-param name="name" select="'gmd:spatialResolution'"/>
      </xsl:call-template>
      <xsl:call-template name="copyOrAddElement">
        <xsl:with-param name="elements" select="gmd:language"/>
        <xsl:with-param name="name" select="'gmd:language'"/>
      </xsl:call-template>
      <xsl:call-template name="copyOrAddElement">
        <xsl:with-param name="elements" select="gmd:characterSet"/>
        <xsl:with-param name="name" select="'gmd:characterSet'"/>
      </xsl:call-template>
      <xsl:call-template name="copyOrAddElement">
        <xsl:with-param name="elements" select="gmd:topicCategory"/>
        <xsl:with-param name="name" select="'gmd:topicCategory'"/>
      </xsl:call-template>
      <xsl:apply-templates select="gmd:environmentDescription" mode="expand"/>

      <gmd:extent>
        <gmd:EX_Extent>
          <gmd:geographicElement/>
          <gmd:temporalElement/>
        </gmd:EX_Extent>
      </gmd:extent>

      <xsl:apply-templates select="gmd:supplementalInformation" mode="expand"/>

      <xsl:apply-templates select="srv:*" mode="expand"/>

      <xsl:apply-templates select="*[namespace-uri()!='http://www.isotc211.org/2005/gmd' and
                                     namespace-uri()!='http://www.isotc211.org/2005/srv']"
                           mode="expand"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gmd:identificationInfo/*/gmd:citation/*" mode="expand">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="gmd:title
                                   |gmd:alternateTitle"
                           mode="expand"/>

      <xsl:for-each-group select="$existingMembers//gmd:MD_Metadata/gmd:identificationInfo
            /*/gmd:citation/*/gmd:date[*/gmd:dateType/*/@codeListValue = 'publication']"
                          group-by="*/gmd:date/gco:*">
        <xsl:sort select="*/gmd:date/gco:*" order="descending"/>

        <xsl:if test="position() = 1">
          <xsl:copy-of select="."/>
        </xsl:if>
      </xsl:for-each-group>

      <xsl:apply-templates select="gmd:edition
                                   |gmd:editionDate
                                   |gmd:identifier
                                   |gmd:citedResponsibleParty
                                   |gmd:presentationForm
                                   |gmd:series
                                   |gmd:otherCitationDetails
                                   |gmd:collectiveTitle
                                   |gmd:ISBN
                                   |gmd:ISSN" mode="expand"/>
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

  <xsl:template name="make-aggregate">
    <xsl:param name="uuid"/>
    <xsl:param name="context"/>
    <xsl:param name="initiativeType" select="$initiativeType" required="no"/>
    <xsl:param name="associationType" select="$associationType" required="no"/>
    <xsl:param name="title" select="''" required="no"/>
    <xsl:param name="remoteUrl" select="''" required="no"/>

    <xsl:variable name="notExist"
                  select="count($context/gmd:aggregationInfo/gmd:MD_AggregateInformation[
      gmd:aggregateDataSetIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString = $uuid
      and gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue = $associationType
      and gmd:initiativeType/gmd:DS_InitiativeTypeCode/@codeListValue = $initiativeType
      ]) = 0"/>
    <xsl:if test="$notExist">
      <gmd:aggregationInfo>
        <gmd:MD_AggregateInformation>
          <gmd:aggregateDataSetIdentifier>
            <gmd:MD_Identifier>
              <gmd:code>
                <xsl:choose>
                  <xsl:when test="$remoteUrl != ''">
                    <gmx:Anchor>
                      <xsl:if test="$remoteUrl">
                        <xsl:attribute name="xlink:href" select="$remoteUrl"/>
                      </xsl:if>
                      <xsl:if test="$title != ''">
                        <xsl:attribute name="xlink:title" select="$title"/>
                      </xsl:if>
                      <xsl:value-of select="$uuid"/>
                    </gmx:Anchor>
                  </xsl:when>
                  <xsl:otherwise>
                    <gco:CharacterString><xsl:value-of select="$uuid"/></gco:CharacterString>
                  </xsl:otherwise>
                </xsl:choose>

              </gmd:code>
            </gmd:MD_Identifier>
          </gmd:aggregateDataSetIdentifier>
          <gmd:associationType>
            <gmd:DS_AssociationTypeCode
              codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#DS_AssociationTypeCode"
              codeListValue="{$associationType}"/>
          </gmd:associationType>
          <xsl:if test="$initiativeType != ''">
            <gmd:initiativeType>
              <gmd:DS_InitiativeTypeCode
                codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#DS_InitiativeTypeCode"
                codeListValue="{$initiativeType}"/>
            </gmd:initiativeType>
          </xsl:if>
        </gmd:MD_AggregateInformation>
      </gmd:aggregationInfo>
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
