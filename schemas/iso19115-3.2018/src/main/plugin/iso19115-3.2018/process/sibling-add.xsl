<?xml version="1.0" encoding="UTF-8"?>
<!--
Stylesheet used to add a reference to a related record using aggregation info.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn="http://www.fao.org/geonetwork"
                exclude-result-prefixes="#all" version="2.0">

  <xsl:import href="sibling-utility.xsl"/>

  <!-- The uuid of the target record -->
  <xsl:param name="uuidref"/>
  <xsl:param name="nodeUrl"/>

  <!-- A list of uuids of the target records
  Each record is described by
  'uuid#associationType#initiativeType#title#remoteUrl,uuid#...'
  and are comma separated.
  title and remoteUrl may be empty for local record.
  -->
  <xsl:param name="uuids"/>

  <!-- (optional) The association type. Default: crossReference. -->
  <xsl:param name="associationType" select="'crossReference'"/>

  <!-- (optional) The initiative type. -->
  <xsl:param name="initiativeType" select="''"/>


  <xsl:template match="mri:MD_DataIdentification|
                      *[@gco:isoType='mri:MD_DataIdentification']|
                      srv:SV_ServiceIdentification">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="mri:citation"/>
      <xsl:apply-templates select="mri:abstract"/>
      <xsl:apply-templates select="mri:purpose"/>
      <xsl:apply-templates select="mri:credit"/>
      <xsl:apply-templates select="mri:status"/>
      <xsl:apply-templates select="mri:pointOfContact"/>
      <xsl:apply-templates select="mri:spatialRepresentationType"/>
      <xsl:apply-templates select="mri:spatialResolution"/>
      <xsl:apply-templates select="mri:temporalResolution"/>
      <xsl:apply-templates select="mri:topicCategory"/>
      <xsl:apply-templates select="mri:extent"/>
      <xsl:apply-templates select="mri:additionalDocumentation"/>
      <xsl:apply-templates select="mri:processingLevel"/>
      <xsl:apply-templates select="mri:resourceMaintenance"/>
      <xsl:apply-templates select="mri:graphicOverview"/>
      <xsl:apply-templates select="mri:resourceFormat"/>
      <xsl:apply-templates select="mri:descriptiveKeywords"/>
      <xsl:apply-templates select="mri:resourceSpecificUsage"/>
      <xsl:apply-templates select="mri:resourceConstraints"/>
      <xsl:apply-templates select="mri:associatedResource"/>

      <xsl:call-template name="fill"/>

      <xsl:apply-templates select="mri:defaultLocale"/>
      <xsl:apply-templates select="mri:otherLocale"/>
      <xsl:apply-templates select="mri:environmentDescription"/>
      <xsl:apply-templates select="mri:supplementalInformation"/>

      <xsl:apply-templates select="srv:*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="fill">
    <xsl:variable name="context" select="."/>

    <xsl:if test="$uuidref != ''">
      <xsl:call-template name="make-aggregate">
        <xsl:with-param name="uuid" select="$uuidref"/>
        <xsl:with-param name="context" select="$context"/>
      </xsl:call-template>
    </xsl:if>

    <xsl:if test="$uuids != ''">
      <xsl:for-each select="tokenize($uuids, ',')">
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
              <xsl:with-param name="nodeUrl" select="$nodeUrl"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <!-- Same initiative type and association type
            for all siblings -->
            <xsl:call-template name="make-aggregate">
              <xsl:with-param name="uuid" select="."/>
              <xsl:with-param name="context" select="$context"/>
              <xsl:with-param name="nodeUrl" select="$nodeUrl"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>

      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="gn:*"
                priority="2"/>
</xsl:stylesheet>
