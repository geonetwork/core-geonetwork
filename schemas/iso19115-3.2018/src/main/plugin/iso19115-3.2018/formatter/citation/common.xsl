<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:tr="java:org.fao.geonet.api.records.formatters.SchemaLocalizations"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:param name="format"
             select="'html'"/>

  <xsl:variable name="formats" as="node()*">
    <format key="html"/>
    <format key="text"/>
    <format key="ris" extension=".ris" mime="application/x-research-info-systems"/>
    <format key="bibtex" extension=".bst" url="http://www.bibtex.org/"/>
  </xsl:variable>

  <xsl:param name="doiProtocolRegex"
             select="'(DOI|WWW:LINK-1.0-http--metadata-URL)'"/>



  <xsl:template mode="citation" match="citation[lower-case($format) = '?']">
    <textResponse>["<xsl:value-of select="string-join($formats/@key, '&quot;,&quot;')"/>"]</textResponse>
  </xsl:template>

  <xsl:template mode="citation" match="citation[lower-case($format) = 'text']">
    <xsl:variable name="hasAuthor"
                  select="count(authorsNameAndOrgList/*) > 0"/>
    <xsl:variable name="hasPublisher"
                  select="count(publishersNameAndOrgList/*) > 0"/>
    <textResponse>
      <xsl:value-of select="concat(
                                  (if ($hasAuthor)
                                     then string-join(authorsNameAndOrgList/*, ', ')
                                     else ''),
                                  (if (lastPublicationDate != '')
                                    then concat(' (', substring(lastPublicationDate, 1, 4), '). ')
                                    else if ($hasAuthor) then '. ' else ''),
                                  translatedTitle,
                                  '. ',
                                  (if ($hasPublisher)
                                     then concat(string-join(publishersNameAndOrgList/*, ', '), '. ')
                                     else ''),
                                  if (doiUrl != '') then doiUrl else landingPageUrl)"/>
    </textResponse>
  </xsl:template>


  <xsl:template mode="citation" match="citation[lower-case($format) = 'bibtex']">
    <!-- https://en.wikipedia.org/wiki/BibTeX -->
    <textResponse>@data{<xsl:value-of select="uuid"/>,
        author = {<xsl:value-of select="string-join(authorsNameAndOrgList/*, ' and ')"/>},
        publisher = {<xsl:value-of select="string-join(publishersNameAndOrgList/*, ' and ')"/>},
        title = {<xsl:value-of select="translatedTitle"/>},
        <xsl:if test="lastPublicationDate != ''">year = {<xsl:value-of select="substring(lastPublicationDate, 1, 4)"/>},</xsl:if>
        <xsl:if test="doi != ''">doi = {<xsl:value-of select="doi"/>},</xsl:if>
        url = {<xsl:value-of select="if (doiUrl != '') then doiUrl else landingPageUrl"/>}
      }</textResponse>
  </xsl:template>


  <xsl:template mode="citation" match="citation[lower-case($format) = 'ris']">
    <!-- https://en.wikipedia.org/wiki/RIS_(file_format) -->
    <textResponse>
      <!-- TODO: add support for MAP, DBASE, CTLG, AGGR? -->
      <xsl:text>TY  - </xsl:text><xsl:value-of select="'DATA'"/><xsl:text>&#13;&#10;</xsl:text>
      <xsl:for-each select="authorsNameAndOrgList/*[. != '']">
        <xsl:text>AU  - </xsl:text><xsl:value-of select="."/><xsl:text>&#13;&#10;</xsl:text>
      </xsl:for-each>
      <xsl:text>TI  - </xsl:text><xsl:value-of select="translatedTitle"/><xsl:text>&#13;&#10;</xsl:text>
      <!-- TODO: LA, ET -->
      <xsl:for-each select="publishersNameAndOrgList/*[. != '']">
        <xsl:text>PB  - </xsl:text><xsl:value-of select="."/><xsl:text>&#13;&#10;</xsl:text>
      </xsl:for-each>
      <xsl:for-each select="keyword[. != '']">
        <xsl:text>KW  - </xsl:text><xsl:value-of select="."/><xsl:text>&#13;&#10;</xsl:text>
      </xsl:for-each>
      <xsl:text>UR  - </xsl:text><xsl:value-of select="if (doiUrl != '') then doiUrl else landingPageUrl"/><xsl:text>&#13;&#10;</xsl:text>
      <xsl:if test="doi != ''">
        <xsl:text>DO  - </xsl:text><xsl:value-of select="doi"/><xsl:text>&#13;&#10;</xsl:text>
      </xsl:if>
      <xsl:text>ID  - </xsl:text><xsl:value-of select="uuid"/><xsl:text>&#13;&#10;</xsl:text>
      <xsl:text>ER  -</xsl:text>
    </textResponse>
  </xsl:template>


  <xsl:template mode="citation" match="citation[lower-case($format) = ('html', '')]">
    <xsl:variable name="hasAuthor"
                  select="count(authorsNameAndOrgList/*) > 0"/>
    <xsl:variable name="hasPublisher"
                  select="count(publishersNameAndOrgList/*) > 0"/>
    <blockquote>
      <div class="row">
        <div class="col-md-3">
          <i class="fa fa-quote-left pull-right"><xsl:comment/></i>
        </div>
        <div class="col-md-9">
          <p>
            <xsl:call-template name="citation-contact">
              <xsl:with-param name="contact" select="authorsNameAndOrgList"/>
            </xsl:call-template>

            <xsl:value-of select="if (lastPublicationDate != '')
                      then concat('(', substring(lastPublicationDate, 1, 4), ').')
                      else if ($hasAuthor) then '.'
                      else ''"/>

            <div><xsl:copy-of select="translatedTitle/(text()|*)"/>.</div>

            <xsl:call-template name="citation-contact">
              <xsl:with-param name="contact" select="publishersNameAndOrgList"/>
            </xsl:call-template>
            <br/>
            <xsl:variable name="url"
                          select="if (doiUrl != '') then doiUrl else landingPageUrl"/>
            <a href="{$url}">
              <xsl:value-of select="$url"/>
            </a>
            <br/>

            <xsl:if test="additionalCitation != ''">
              <br/>
              <em><xsl:value-of select="$schemaStrings/citationAdditional"/></em>
              <br/>
              <xsl:value-of select="additionalCitation"/>
            </xsl:if>
          </p>
        </div>
      </div>
    </blockquote>
  </xsl:template>

  <xsl:template name="citation-contact">
    <xsl:param name="contact" as="node()*"/>

    <xsl:for-each select="$contact/author">
      <xsl:for-each select="(text()|*)">
        <span>
          <xsl:copy-of select="@*"/>
          <xsl:value-of select="."/>
        </span>
      </xsl:for-each>
      <xsl:if test="position() != last()">, </xsl:if>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
