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

  <!-- Who is the creator of the data set?  This can be an individual, a group of individuals, or an organization. -->
  <xsl:param name="authorRoles"
                select="'custodian,author'"/>
  <xsl:variable name="authorRolesList"
                select="tokenize($authorRoles, ',')"/>

  <!-- What entity is responsible for producing and/or distributing the data set?  Also, is there a physical location associated with the publisher? -->
  <xsl:param name="publisherRoles"
                select="'publisher'"/>

  <xsl:variable name="publisherRolesList"
                select="tokenize($publisherRoles, ',')"/>

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
    <textResponse><xsl:value-of select="normalize-space(concat(
                                  (if ($hasAuthor)
                                     then string-join(authorsNameAndOrgList/*, ', ')
                                     else ''),
                                  (if ($hasAuthor)
                                     then ' '
                                     else ''),
                                  (if (lastPublicationDate != '')
                                    then concat('(', substring(lastPublicationDate, 1, 4), '). ')
                                    else if ($hasAuthor) then '. ' else ''),
                                  normalize-space(translatedTitle),
                                  '. ',
                                  (if ($hasPublisher)
                                     then concat(string-join(publishersNameAndOrgList/*, ', '), '. ')
                                     else ''),
                                  if (doiUrl != '') then doiUrl else landingPageUrl))"/>
    </textResponse>
  </xsl:template>


  <xsl:template mode="citation" match="citation[lower-case($format) = 'bibtex']" name="citation-bibtex">
    <!-- https://en.wikipedia.org/wiki/BibTeX -->
    <textResponse>@misc{<xsl:value-of select="uuid"/>,
      author = {<xsl:value-of select="normalize-space(string-join(authorsNameAndOrgList/*, ', '))"/>},
      publisher = {<xsl:value-of select="normalize-space(string-join(publishersNameAndOrgList/*, ', '))"/>},
      title = {<xsl:value-of select="normalize-space(translatedTitle)"/>},
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
        <xsl:text>AU  - </xsl:text><xsl:value-of select="normalize-space(.)"/><xsl:text>&#13;&#10;</xsl:text>
      </xsl:for-each>
      <xsl:text>TI  - </xsl:text><xsl:value-of select="normalize-space(translatedTitle)"/><xsl:text>&#13;&#10;</xsl:text>
      <!-- TODO: ET -->
      <xsl:if test="language != ''">
        <xsl:text>LA  - </xsl:text><xsl:value-of select="language"/><xsl:text>&#13;&#10;</xsl:text>
      </xsl:if>
      <xsl:for-each select="publishersNameAndOrgList/*[. != '']">
        <xsl:text>PB  - </xsl:text><xsl:value-of select="normalize-space(.)"/><xsl:text>&#13;&#10;</xsl:text>
      </xsl:for-each>
      <xsl:for-each select="keyword[. != '']">
        <xsl:text>KW  - </xsl:text><xsl:value-of select="."/><xsl:text>&#13;&#10;</xsl:text>
      </xsl:for-each>
      <xsl:text>UR  - </xsl:text><xsl:value-of select="if (doiUrl != '') then doiUrl else landingPageUrl"/><xsl:text>&#13;&#10;</xsl:text>
      <xsl:if test="doi != ''">
        <xsl:text>DO  - </xsl:text><xsl:value-of select="doi"/><xsl:text>&#13;&#10;</xsl:text>
      </xsl:if>
      <xsl:text>ID  - </xsl:text><xsl:value-of select="uuid"/><xsl:text>&#13;&#10;</xsl:text>
      <xsl:if test="lastPublicationDate != ''">
        <xsl:text>DA  - </xsl:text><xsl:value-of select="concat(replace(substring(lastPublicationDate, 1, 12), '-', '/'), '/')" /><xsl:text>&#13;&#10;</xsl:text>
        <xsl:text>PY  - </xsl:text><xsl:value-of select="substring(lastPublicationDate, 1, 4)" /><xsl:text>&#13;&#10;</xsl:text>
      </xsl:if>
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

            <span><xsl:copy-of select="translatedTitle/(text()|*)"/>.</span>

            <xsl:call-template name="citation-contact">
              <xsl:with-param name="contact" select="publishersNameAndOrgList"/>
            </xsl:call-template>
            <br></br>

            <xsl:variable name="url"
                          select="if (doiUrl != '') then doiUrl else landingPageUrl"/>
            <a href="{$url}">
              <xsl:value-of select="$url"/>
            </a>
            <br></br>

            <xsl:if test="additionalCitation != ''">
              <br></br>
              <em><xsl:value-of select="$schemaStrings/citationAdditional"/></em>
              <br></br>
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
      <xsl:if test="position() = last()">&#160;</xsl:if>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
