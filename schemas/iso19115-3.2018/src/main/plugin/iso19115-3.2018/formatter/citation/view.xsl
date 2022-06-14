<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:import href="sharedFormatterDir/xslt/render-variables.xsl"/>
  <xsl:include href="../../layout/utility-tpl-multilingual.xsl"/>
  <xsl:include href="../../layout/utility-fn.xsl"/>
  <xsl:include href="citation-common.xsl"/>

  <xsl:variable name="metadata"
                select="/root/mdb:MD_Metadata"/>

  <xsl:variable name="configuration"
                select="/empty"/>

  <xsl:variable name="langId"
                select="gn-fn-iso19115-3.2018:getLangId($metadata, $language)"/>

  <xsl:variable name="allLanguages">
    <xsl:call-template name="get-iso19115-3.2018-other-languages"/>
  </xsl:variable>

  <xsl:template match="/">

    <!-- Who is the creator of the data set?  This can be an individual, a group of individuals, or an organization. -->
    <xsl:variable name="authorRoles"
                  select="('custodian', 'author')"/>
    <xsl:variable name="authors"
                  select="$metadata/mdb:identificationInfo/*/mri:pointOfContact/
                                *[cit:role/*/@codeListValue = $authorRoles]"/>
    <xsl:variable name="authorsNameAndOrgList">
      <xsl:for-each select="$authors">
        <author>
          <xsl:variable name="name"
                        select=".//cit:individual/*/cit:name[1]"/>

          <xsl:for-each select="$name">
            <xsl:call-template name="get-iso19115-3.2018-localised">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:call-template>
          </xsl:for-each>
          <xsl:if test="normalize-space($name) != ''">(</xsl:if>
          <xsl:for-each select="cit:party/*/cit:name">
            <xsl:call-template name="get-iso19115-3.2018-localised">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:call-template>
          </xsl:for-each>
          <xsl:if test="normalize-space($name) != ''">)</xsl:if>
        </author>
      </xsl:for-each>
    </xsl:variable>

    <!-- What name is the data set called? -->
    <xsl:variable name="title"
                  select="$metadata/mdb:identificationInfo/*/mri:citation/*/cit:title"/>

    <xsl:variable name="translatedTitle">
      <xsl:for-each select="$title">
        <xsl:call-template name="get-iso19115-3.2018-localised">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:variable>

    <!-- Is there a version or edition number associated with the data set? -->
    <xsl:variable name="edition"
                  select="'$metadata/mdb:identificationInfo/*/mri:citation/*/cit:title/*/text()[. != '']'"/>

    <!-- What year was the data set published?  When was the data set posted online? -->
    <xsl:variable name="dates"
                  select="$metadata/mdb:identificationInfo/*/mri:citation/*/cit:date/*[
                                  cit:dateType/*/@codeListValue =
                                    ('publication', 'revision')]/
                                    cit:date/gco:*[. != '']"/>

    <xsl:variable name="publicationDates">
      <xsl:perform-sort select="$dates">
        <xsl:sort select="." order="descending"/>
      </xsl:perform-sort>
    </xsl:variable>

    <xsl:variable name="lastPublicationDate"
                  select="$publicationDates[1]"/>

    <!-- What entity is responsible for producing and/or distributing the data set?  Also, is there a physical location associated with the publisher? -->
    <xsl:variable name="publisherRoles"
                  select="('publisher')"/>
    <xsl:variable name="publishers"
                  select="$metadata/mdb:identificationInfo/*/mri:pointOfContact/
                                *[cit:role/*/@codeListValue = $publisherRoles]"/>

    <xsl:variable name="publishersNameAndOrgList">
      <xsl:for-each select="$publishers">
        <author>
          <xsl:variable name="name"
                        select=".//cit:individual/*/cit:name[1]"/>

          <xsl:for-each select="$name">
            <xsl:call-template name="get-iso19115-3.2018-localised">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:call-template>
          </xsl:for-each>
          <xsl:if test="normalize-space($name) != ''">(</xsl:if>
          <xsl:for-each select="cit:party/*/cit:name">
            <xsl:call-template name="get-iso19115-3.2018-localised">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:call-template>
          </xsl:for-each>
          <xsl:if test="normalize-space($name) != ''">)</xsl:if>
        </author>
      </xsl:for-each>
    </xsl:variable>


    <!-- Electronic Retrieval Location -->
    <xsl:variable name="doiInResourceIdentifier"
                  select="(//mdb:identificationInfo/*/mri:citation/*/
                              cit:identifier/*/mcc:code[
                                contains(*/text(), 'datacite.org/doi/')
                                or contains(*/text(), 'doi.org')
                                or contains(*/@xlink:href, 'doi.org')]/*/(@xlink:href|text()))[1]"/>

    <xsl:variable name="doiInOnline"
                  select="//mdb:distributionInfo//mrd:onLine/*[
                              matches(cit:protocol/gco:CharacterString,
                               $doiProtocolRegex)]/cit:linkage/gco:CharacterString[. != '']"/>

    <xsl:variable name="doiUrl"
                  select="if ($doiInResourceIdentifier != '')
                          then $doiInResourceIdentifier
                          else if ($doiInOnline != '')
                          then $doiInOnline
                          else ''"/>

    <xsl:variable name="landingPageUrl"
                  select="concat($nodeUrl, 'api/records/', $metadataUuid)"/>

    <xsl:variable name="keywords"
                  select="$metadata//mri:keyword"/>

    <xsl:variable name="translatedKeywords">
      <xsl:for-each select="$keywords">
        <keyword>
          <xsl:call-template name="get-iso19115-3.2018-localised">
            <xsl:with-param name="langId" select="$langId"/>
          </xsl:call-template>
        </keyword>
      </xsl:for-each>
    </xsl:variable>

    <xsl:variable name="citationInfo">
      <citation>
        <uuid><xsl:value-of
          select="$metadata/mdb:metadataIdentifier/*/mcc:code/gco:CharacterString[. != '']"/></uuid>
        <authorsNameAndOrgList><xsl:copy-of select="$authorsNameAndOrgList"/></authorsNameAndOrgList>
        <lastPublicationDate><xsl:value-of select="$lastPublicationDate"/></lastPublicationDate>
        <translatedTitle><xsl:value-of select="$translatedTitle"/></translatedTitle>
        <publishersNameAndOrgList><xsl:copy-of select="$publishersNameAndOrgList"/></publishersNameAndOrgList>
        <landingPageUrl><xsl:value-of select="$landingPageUrl"/></landingPageUrl>
        <doi><xsl:value-of select="replace($doiUrl, '.*doi.org/(.*)', '$1')"/></doi>
        <doiUrl><xsl:value-of select="$doiUrl"/></doiUrl>
        <xsl:copy-of select="$translatedKeywords"/>
      </citation>
    </xsl:variable>

    <xsl:apply-templates mode="citation" select="$citationInfo"/>
  </xsl:template>
</xsl:stylesheet>
