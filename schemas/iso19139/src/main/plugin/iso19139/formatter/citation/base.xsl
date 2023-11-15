<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                xmlns:xslUtils="java:org.fao.geonet.util.XslUtil"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:import href="sharedFormatterDir/xslt/render-variables.xsl"/>
  <xsl:import href="../../layout/utility-tpl-multilingual.xsl"/>
  <xsl:import href="../../layout/utility-fn.xsl"/>

  <xsl:function name="gn-fn-iso19139:get-author-list">
    <xsl:param name="authors" as="node()*"/>
    <xsl:param name="langId" as="xs:string"/>

    <xsl:variable name="authorsNameAndOrgListTmp"
                  as="node()*">
      <xsl:for-each select="$authors">
        <author>
          <xsl:variable name="name"
                        select=".//gmd:individualName[1]"/>

          <xsl:for-each select="$name">
            <xsl:call-template name="localised">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:call-template>
          </xsl:for-each>
          <xsl:if test="normalize-space($name) != ''">(</xsl:if>
          <xsl:for-each select=".//gmd:organisationName">
            <xsl:call-template name="localised">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:call-template>
          </xsl:for-each>
          <xsl:if test="normalize-space($name) != ''">)</xsl:if>
        </author>
      </xsl:for-each>
    </xsl:variable>

    <xsl:for-each-group select="$authorsNameAndOrgListTmp" group-by=".">
      <xsl:copy-of select="."/>
    </xsl:for-each-group>
  </xsl:function>


  <xsl:template name="get-iso19139-citation">
    <xsl:param name="metadata" as="node()"/>
    <xsl:param name="language" as="xs:string"/>

    <xsl:variable name="langId"
                  select="gn-fn-iso19139:getLangId($metadata, $language)"/>

    <xsl:variable name="authors"
                  select="$metadata/gmd:identificationInfo/*/gmd:pointOfContact/
                                *[gmd:role/*/@codeListValue = $authorRolesList]"/>
    <xsl:variable name="authorsNameAndOrgList" as="node()*"
                  select="gn-fn-iso19139:get-author-list($authors, $langId)"/>

    <!-- What name is the data set called? -->
    <xsl:variable name="title"
                  select="$metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:title"/>

    <xsl:variable name="translatedTitle">
      <xsl:for-each select="$title">
        <xsl:call-template name="localised">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:variable>

    <!-- Is there a version or edition number associated with the data set? -->
    <xsl:variable name="edition"
                  select="'$metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:title/*/text()[. != '']'"/>

    <!-- What year was the data set published?  When was the data set posted online? -->
    <xsl:variable name="dates"
                  select="$metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:date/*[
                                  gmd:dateType/*/@codeListValue =
                                    ('publication', 'revision')]/
                                    gmd:date/gco:*[. != '']"/>

    <xsl:variable name="publicationDates">
      <xsl:perform-sort select="$dates">
        <xsl:sort select="." order="descending"/>
      </xsl:perform-sort>
    </xsl:variable>

    <xsl:variable name="lastPublicationDate"
                  select="$publicationDates[1]"/>

    <xsl:variable name="publishers"
                  select="$metadata/gmd:identificationInfo/*/gmd:pointOfContact/
                                *[gmd:role/*/@codeListValue = $publisherRolesList]"/>
    <xsl:variable name="publishersNameAndOrgList" as="node()*"
                  select="gn-fn-iso19139:get-author-list($publishers, $langId)"/>

    <!-- Electronic Retrieval Location -->
    <xsl:variable name="doiInResourceIdentifier"
                  select="(//gmd:identificationInfo/*/gmd:citation/*/
                              gmd:identifier/*/gmd:code[
                                contains(*/text(), 'datacite.org/doi/')
                                or contains(*/text(), 'doi.org')
                                or contains(*/@xlink:href, 'doi.org')]/*/(@xlink:href|text()))[1]"/>

    <xsl:variable name="doiProtocolRegex"
                  select="'(DOI|WWW:LINK-1.0-http--metadata-URL)'"/>

    <xsl:variable name="doiInOnline"
                  select="//gmd:distributionInfo//gmd:onLine/*[
                              matches(gmd:protocol/gco:CharacterString,
                               $doiProtocolRegex)]/gmd:linkage/gmd:URL[. != '']"/>

    <xsl:variable name="doiUrl"
                  select="if ($doiInResourceIdentifier != '')
                          then $doiInResourceIdentifier
                          else if ($doiInOnline != '')
                          then $doiInOnline
                          else ''"/>

    <xsl:variable name="landingPageUrl"
                  select="concat($nodeUrl, 'api/records/', $metadataUuid)"/>


    <xsl:variable name="keywords"
                  select="$metadata/gmd:identificationInfo/*//gmd:keyword"/>

    <xsl:variable name="translatedKeywords">
      <xsl:for-each select="$keywords">
        <keyword>
          <xsl:call-template name="localised">
            <xsl:with-param name="langId" select="$langId"/>
          </xsl:call-template>
        </keyword>
      </xsl:for-each>
    </xsl:variable>


    <xsl:variable name="additionalCitation">
      <xsl:for-each select=".//gmd:onLine/*[gmd:protocol/* = 'WWW:LINK-1.0-http--publication-URL']/gmd:description">
        <xsl:call-template name="localised">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:variable>

    <citation>
      <uuid><xsl:value-of
        select="$metadata/gmd:fileIdentifier/gco:CharacterString[. != '']"/></uuid>
      <authorsNameAndOrgList><xsl:copy-of select="$authorsNameAndOrgList"/></authorsNameAndOrgList>
      <lastPublicationDate><xsl:value-of select="$lastPublicationDate"/></lastPublicationDate>
      <translatedTitle><xsl:copy-of select="$translatedTitle"/></translatedTitle>
      <publishersNameAndOrgList><xsl:copy-of select="$publishersNameAndOrgList"/></publishersNameAndOrgList>
      <landingPageUrl><xsl:value-of select="$landingPageUrl"/></landingPageUrl>
      <doi><xsl:value-of select="replace($doiUrl, '.*doi.org/(.*)', '$1')"/></doi>
      <doiUrl><xsl:value-of select="$doiUrl"/></doiUrl>
      <xsl:copy-of select="$translatedKeywords"/>
      <additionalCitation><xsl:value-of select="$additionalCitation"/></additionalCitation>
    </citation>
  </xsl:template>
</xsl:stylesheet>
