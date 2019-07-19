<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://a9.com/-/spec/opensearch/1.1/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:georss="http://www.georss.org/georss"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:opensearch="http://a9.com/-/spec/opensearch/1.1/"
                xmlns:inspire_dls="http://inspire.ec.europa.eu/schemas/inspire_dls/1.0"
                exclude-result-prefixes="gmx xsl gmd gco srv java">
  <xsl:param name="nodeUrl" />
  <xsl:param name="requestedLanguage" select="string('eng')" />
  <xsl:param name="opensearchDescriptionFileName" select="'OpenSearchDescription.xml'"/>
  <xsl:param name="opensearchUrlSuffix" />
  <xsl:param name="atomDescribeServiceUrlSuffix" />
  <xsl:param name="atomDescribeDatasetUrlSuffix" />
  <xsl:param name="atomDownloadDatasetUrlSuffix" />
  <xsl:param name="nodeName" />
  <xsl:output method="xml" indent="no" encoding="utf-8"/>

  <xsl:template match="/root">
    <OpenSearchDescription xsi:schemaLocation="http://a9.com/-/spec/opensearch/1.1/ http://inspire-geoportal.ec.europa.eu/schemas/inspire/atom/1.0/opensearch.xsd">
      <xsl:apply-templates mode="service" select="response"/>
    </OpenSearchDescription>
  </xsl:template>

  <xsl:template mode="service" match="response">
      <!--URL of this document-->
      <xsl:choose>
        <xsl:when test="string(fileId)">
          <ShortName>INSPIRE Download</ShortName>
          <Description><xsl:value-of select="title"/>: <xsl:value-of select="subtitle"/></Description>
          <!-- TG Requirement 40 -->
          <!--URL of this document-->
          <Url type="application/opensearchdescription+xml" rel="self">
            <xsl:attribute name="template">
              <xsl:value-of
                select="concat($nodeUrl, $opensearchUrlSuffix, '/', $opensearchDescriptionFileName, '?uuid=', fileId)"/>
            </xsl:attribute>
          </Url>
        </xsl:when>
        <xsl:otherwise>
        </xsl:otherwise>
      </xsl:choose>

      <!-- TG Requirement 41 -->
      <!--Generic URL template for browser integration-->
      <Url type="text/html" rel="results">
        <xsl:attribute name="template">
          <xsl:value-of
            select="concat($nodeUrl, $requestedLanguage, '/', $opensearchUrlSuffix, '/htmlsearch?q={searchTerms?}')"/>
        </xsl:attribute>
      </Url>

      <!-- TG Requirement 42 -->
      <!-- Describe Spatial Data Set Operation request URL template to be used in
           order to retrieve the description of Spatial Object Types in a Spatial
           Dataset-->
      <Url type="application/atom+xml" rel="describedby">
        <xsl:attribute name="template">
          <xsl:value-of select="concat($nodeUrl, $atomDescribeDatasetUrlSuffix,'?spatial_dataset_identifier_code={inspire_dls:spatial_dataset_identifier_code?}&amp;spatial_dataset_identifier_namespace={inspire_dls:spatial_dataset_identifier_namespace?}&amp;language={language?}&amp;q={searchTerms?}')"/>
        </xsl:attribute>
      </Url>

      <!-- TG Requirement 43 -->
      <!-- Get Spatial Data Set Operation request URL template to be used in order
           to retrieve a Spatial Data Set-->
      <Url type="application/atom+xml" rel="results">
        <xsl:attribute name="template">
          <xsl:value-of select="concat($nodeUrl, $atomDescribeDatasetUrlSuffix,'?spatial_dataset_identifier_code={inspire_dls:spatial_dataset_identifier_code?}&amp;spatial_dataset_identifier_namespace={inspire_dls:spatial_dataset_identifier_namespace?}&amp;crs={inspire_dls:crs?}&amp;language={language?}&amp;q={searchTerms?}')"/>
        </xsl:attribute>
      </Url>
      <xsl:for-each select="fileTypes/fileType">
        <xsl:variable name="inspireMimeType">
          <xsl:choose>
            <xsl:when test=".='multipart/x-zip'">
              <xsl:value-of select="string('application/x-gmz')"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="."/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <Url type="{$inspireMimeType}" rel="results">
          <xsl:attribute name="template">
            <xsl:value-of select="concat($nodeUrl, $atomDownloadDatasetUrlSuffix,'?spatial_dataset_identifier_code={inspire_dls:spatial_dataset_identifier_code?}&amp;spatial_dataset_identifier_namespace={inspire_dls:spatial_dataset_identifier_namespace?}&amp;crs={inspire_dls:crs?}&amp;language={language?}&amp;q={searchTerms?}')"/>
          </xsl:attribute>
        </Url>
      </xsl:for-each>

      <Contact>
        <xsl:value-of select="authorEmail" />
      </Contact>
      <Tags>
        <xsl:value-of select="substring(keywords,1,256)"/>
      </Tags>
      <LongName><xsl:value-of select="substring(title,1,48)"/></LongName>
      <!-- TG Requirement 44 -->
      <!-- List of available Spatial Dataset Identifiers -->
      <xsl:for-each select="datasets/dataset">
          <xsl:variable name="codeVal" select="code" />
          <xsl:variable name="namespaceVal" select="namespace" />

          <xsl:for-each select="file">
              <Query role="example"
                  inspire_dls:spatial_dataset_identifier_namespace="{$namespaceVal}"
                  inspire_dls:spatial_dataset_identifier_code="{$codeVal}" inspire_dls:crs="{crs}" language="{lang}" title="{title}" count="{crsCount}"/>
         </xsl:for-each>

      </xsl:for-each>

      <xsl:choose>
        <xsl:when test="string(fileId)">
          <Developer>
            <xsl:value-of select="authorName"/>
          </Developer>
          <!-- TG Requirement 45 -->
          <!-- Languages supported by the service. The first language is the Default Language-->
          <xsl:for-each select="languages/language">
            <Language>
              <xsl:value-of select="."/>
            </Language>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise/>
      </xsl:choose>

  </xsl:template>
</xsl:stylesheet>
