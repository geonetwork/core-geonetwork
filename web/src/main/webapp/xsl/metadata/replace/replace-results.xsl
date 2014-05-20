<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="../../modal.xsl"/>

  <!--
   page content
   -->
  <xsl:template name="content">

    <xsl:call-template name="formLayout">
      <xsl:with-param name="title" select="/root/gui/massive-replace/massiveReplaceForm/title"/>
      <xsl:with-param name="content">
        <!-- defined in publish-utils.xsl -->
        <xsl:call-template name="form" />
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="form">
    <xsl:variable name="siteURL" select="concat(/root/gui/env/server/protocol,'://',/root/gui/env/server/host,':',/root/gui/env/server/port)"/>

    <xsl:choose>
      <xsl:when test="/root/request/test = 'true'">
        <h2>
          <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/testResultsTitle"/>
        </h2>

      </xsl:when>
      <xsl:otherwise>
        <h2>
          <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/resultsTitle"/>
        </h2>
      </xsl:otherwise>
    </xsl:choose>

      <xsl:variable name="total" select="count(/root/response/report/changed/metadata) +
                                             count(/root/response/report/notChanged/metadata) +
                                             count(/root/response/report/notOwner/metadata) +
                                             count(/root/response/report/notFound/metadata)" />

      <p><xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/processed"/>: <xsl:value-of select="$total"/></p>

      <p> <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/changed"/>: <xsl:value-of select="count(/root/response/report/changed/metadata)" /></p>

      <xsl:if test="count(/root/response/report/changed/metadata) > 0">
        <ul>
          <xsl:for-each select="/root/response/report/changed/metadata">
            <li>
              <a target="_blank">
                <xsl:attribute name="href"><xsl:value-of select="concat(/root/gui/locService, '/search#|', @uuid)" /></xsl:attribute>
                <xsl:value-of select="@title" />
              </a>


              <xsl:if test="count(change) > 0">
                <br/>Changes:
                <ul>
                  <xsl:for-each select="change">
                    <xsl:variable name="fieldId" select="fieldid" />
                    <li>
                      Field: <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/section[field/@key = $fieldId]/@id" /> / <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/section/field[@key = $fieldId]" /><br/>
                      Original: <xsl:value-of select="originalval" /><br/>
                      Changed: <xsl:value-of select="changedval" />
                    </li>
                  </xsl:for-each>
                </ul>
              </xsl:if>
            </li>
          </xsl:for-each>
        </ul>
      </xsl:if>

      <xsl:if test="count(/root/response/report/notChanged/metadata) > 0">
        <p> <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/notchanged"/>: <xsl:value-of select="count(/root/response/report/notChanged/metadata)" /></p>

        <ul>
          <xsl:for-each select="/root/response/report/notChanged/metadata">
            <li>
              <a target="_blank">
                <xsl:attribute name="href"><xsl:value-of select="concat($siteURL, /root/gui/url, '/metadata/', /root/gui/language, '/', @uuid)" /></xsl:attribute>
                <xsl:value-of select="@title" />
              </a>
            </li>
          </xsl:for-each>
        </ul>
      </xsl:if>

      <xsl:if test="count(/root/response/report/notOwner/metadata) > 0">
        <p> <xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/notowner"/>: <xsl:value-of select="count(/root/response/report/notOwner/metadata)" /></p>
      </xsl:if>

      <xsl:if test="count(/root/response/report/notFound/metadata) > 0">
        <p><xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/notfound"/>: <xsl:value-of select="count(/root/response/report/notFound/metadata)" /></p>
      </xsl:if>


      <!-- Errors -->
      <xsl:if test="count(/root/response/report/errors/error) > 0">
        <p><xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/errors"/>
          <xsl:value-of select="count(/root/response/report/errors/error)" /></p>

        <ul>
          <xsl:for-each select="/root/response/report/errors/error">
            <li>
              <xsl:value-of select="." />
            </li>
          </xsl:for-each>
        </ul>
      </xsl:if>

      <!-- Warnings -->
      <xsl:if test="count(/root/response/report/warnings/warning) > 0">
        <p><xsl:value-of select="/root/gui/massive-replace/massiveReplaceForm/warnings"/>
          <xsl:value-of select="count(/root/response/report/warnings/warning)" /></p>

        <ul>
          <xsl:for-each select="/root/response/report/warnings/warning">
            <li>
              <xsl:value-of select="." />
            </li>
          </xsl:for-each>
        </ul>
      </xsl:if>
  </xsl:template>
</xsl:stylesheet>