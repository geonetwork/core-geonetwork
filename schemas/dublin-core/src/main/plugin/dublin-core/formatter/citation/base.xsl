<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:xslUtils="java:org.fao.geonet.util.XslUtil"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:import href="sharedFormatterDir/xslt/render-variables.xsl"/>
  <xsl:import href="../../layout/utility-fn.xsl"/>

  <xsl:template name="get-dublin-core-citation">
    <xsl:param name="metadata" as="node()"/>
    <xsl:param name="language" as="xs:string"/>

    <!-- Who is the creator of the data set?  This can be an individual, a group of individuals, or an organization. -->
    <xsl:variable name="authors"
                  select="$metadata/dc:creator"/>
    <xsl:variable name="authorsNameAndOrgList">
      <xsl:for-each select="$authors">
        <author>
          <xsl:variable name="name"
                        select="normalize-space(.)"/>
          <xsl:value-of select="$name"/>
        </author>
      </xsl:for-each>
    </xsl:variable>

    <!-- What name is the data set called? -->
    <xsl:variable name="title"
                  select="$metadata/dc:title"/>

    <xsl:variable name="translatedTitle">
      <xsl:for-each select="$title">
        <xsl:value-of select="normalize-space(.)"/>
      </xsl:for-each>
    </xsl:variable>

    <!-- Is there a version or edition number associated with the data set? -->


    <!-- What year was the data set published?  When was the data set posted online? -->
    <xsl:variable name="dates"
                  select="$metadata/dct:modified[. != '']"/>

    <xsl:variable name="publicationDates">
      <xsl:perform-sort select="$dates">
        <xsl:sort select="." order="descending"/>
      </xsl:perform-sort>
    </xsl:variable>

    <xsl:variable name="lastPublicationDate"
                  select="$publicationDates[1]"/>

    <!-- What entity is responsible for producing and/or distributing the data set?  Also, is there a physical location associated with the publisher? -->
    <xsl:variable name="publishers"
                  select="$metadata/dc:publisher"/>

    <xsl:variable name="publishersNameAndOrgList">
      <xsl:for-each select="$publishers">
        <author>
          <xsl:variable name="name"
                        select="normalize-space(.)"/>
          <xsl:value-of select="$name"/>
        </author>
      </xsl:for-each>
    </xsl:variable>


    <!-- Electronic Retrieval Location -->
    <xsl:variable name="doiInResourceIdentifier"
                  select="(//dc:identifier[
                                contains(text(), 'datacite.org/doi/')
                                or contains(text(), 'doi.org')])[1]"/>

    <xsl:variable name="doiInOnline"
                  select="//(dc:relation|dct:references)[
                                contains(text(), 'datacite.org/doi/')
                                or contains(text(), 'doi.org')]"/>

    <xsl:variable name="doiUrl"
                  select="if ($doiInResourceIdentifier != '')
                          then $doiInResourceIdentifier
                          else if ($doiInOnline != '')
                          then $doiInOnline
                          else ''"/>

    <xsl:variable name="landingPageUrl"
                  select="concat($nodeUrl, 'api/records/', $metadataUuid)"/>

    <xsl:variable name="keywords"
                  select="$metadata//dc:subject"/>

    <xsl:variable name="translatedKeywords">
      <xsl:for-each select="$keywords">
        <keyword><xsl:value-of select="."/></keyword>
      </xsl:for-each>
    </xsl:variable>

    <citation>
      <uuid><xsl:value-of
        select="$metadata/dc:identifier[. != '']"/></uuid>
      <authorsNameAndOrgList><xsl:copy-of select="$authorsNameAndOrgList"/></authorsNameAndOrgList>
      <lastPublicationDate><xsl:value-of select="$lastPublicationDate"/></lastPublicationDate>
      <translatedTitle><xsl:value-of select="$translatedTitle"/></translatedTitle>
      <publishersNameAndOrgList><xsl:copy-of select="$publishersNameAndOrgList"/></publishersNameAndOrgList>
      <landingPageUrl><xsl:value-of select="$landingPageUrl"/></landingPageUrl>
      <doi><xsl:value-of select="replace($doiUrl, '.*doi.org/(.*)', '$1')"/></doi>
      <doiUrl><xsl:value-of select="$doiUrl"/></doiUrl>
      <xsl:copy-of select="$translatedKeywords"/>
    </citation>
  </xsl:template>
</xsl:stylesheet>
