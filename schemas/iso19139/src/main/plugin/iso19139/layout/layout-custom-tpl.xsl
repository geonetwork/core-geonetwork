<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                version="2.0"
                exclude-result-prefixes="#all">

  <!--
    Display contact as table when mode is flat (eg. simple view) or if using xsl mode

    Match first node (or added one)
  -->
  <xsl:template mode="mode-iso19139"
                match="gmd:pointOfContact[gmd:CI_ResponsibleParty and (1 or @gn:addedObj = 'true') and $isFlatMode]|
                       gmd:contact[gmd:CI_ResponsibleParty and (1 or @gn:addedObj = 'true') and $isFlatMode]|
                       gmd:distributorContact[gmd:CI_ResponsibleParty and (1 or @gn:addedObj = 'true') and $isFlatMode]|
                       gmd:processor[gmd:CI_ResponsibleParty and (1 or @gn:addedObj = 'true') and $isFlatMode]|
                       gmd:citedResponsibleParty[gmd:CI_ResponsibleParty and (1 or @gn:addedObj = 'true') and $isFlatMode]"
                priority="2000">
    <xsl:call-template name="iso19139-table-contact"/>
  </xsl:template>


  <!-- Ignore the following -->
  <xsl:template mode="mode-iso19139"
                match="gmd:pointOfContact[gmd:CI_ResponsibleParty and position() > 1 and not(@gn:addedObj) and $isFlatMode]|
                       gmd:contact[gmd:CI_ResponsibleParty and position() > 1 and not(@gn:addedObj) and $isFlatMode]|
                       gmd:distributorContact[gmd:CI_ResponsibleParty and position() > 1 and not(@gn:addedObj) and $isFlatMode]|
                       gmd:processor[gmd:CI_ResponsibleParty and position() > 1 and not(@gn:addedObj) and $isFlatMode]|
                       gmd:citedResponsibleParty[gmd:CI_ResponsibleParty and position() > 1 and not(@gn:addedObj) and $isFlatMode]"
                priority="2000">
  </xsl:template>


  <!-- Define table layout -->
  <xsl:template name="iso19139-table-contact">
    <xsl:variable name="name" select="name()"/>

    <xsl:variable name="values">
      <header>
        <col>
          <xsl:value-of select="gn-fn-metadata:getLabel($schema, 'gmd:organisationName', $labels,'', '', '')/label"/>
        </col>
        <col>
          <xsl:value-of select="gn-fn-metadata:getLabel($schema, 'gmd:individualName', $labels,'', '', '')/label"/>
        </col>
        <col>
          <xsl:value-of
            select="gn-fn-metadata:getLabel($schema, 'gmd:electronicMailAddress', $labels,'', '', '')/label"/>
        </col>
        <col>
          <xsl:value-of
            select="gn-fn-metadata:getLabel($schema, 'gmd:role', $labels,'', '', '')/label"/>
        </col>
      </header>
      <xsl:for-each select="(.|following-sibling::*[name() = $name])/gmd:CI_ResponsibleParty">
        <row>
          <col>
            <xsl:copy-of select="gmd:organisationName"/>
          </col>
          <col>
            <xsl:copy-of select="gmd:individualName"/>
          </col>
          <col>
            <xsl:copy-of select="gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress"/>
          </col>
          <col>
            <xsl:copy-of select="gmd:role"/>
          </col>
          <col remove="">
            <xsl:copy-of select="ancestor::*[name() = $name]/gn:element"/>
          </col>
        </row>
      </xsl:for-each>
    </xsl:variable>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>

    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label"
                      select="gn-fn-metadata:getLabel($schema, $name, $labels, name(..), $isoType, $xpath)/label"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="subTreeSnippet">

        <xsl:call-template name="render-table">
          <xsl:with-param name="values" select="$values"/>
        </xsl:call-template>

      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>
