<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <!-- Default template to use (ISO19139 keyword by default). -->
  <xsl:variable name="defaultTpl" select="'to-iso19139-keyword'"/>

  <!-- TODO : use a global function -->
  <xsl:variable name="serviceUrl" select="concat(/root/gui/env/server/protocol, '://',
    /root/gui/env/server/host, ':', /root/gui/env/server/port, /root/gui/locService)"/>

  <!-- Load schema plugin conversion -->
  <xsl:include href="blanks/metadata-schema01/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema02/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema03/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema04/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema05/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema06/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema07/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema08/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema09/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema10/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema11/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema12/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema13/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema14/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema15/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema16/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema17/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema18/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema19/convert/thesaurus-transformation.xsl"/>
  <xsl:include href="blanks/metadata-schema20/convert/thesaurus-transformation.xsl"/>

  <xsl:template match="/">
    <xsl:variable name="tpl"
                  select="if (/root/request/transformation and /root/request/transformation != '')
      then /root/request/transformation else $defaultTpl"/>

    <xsl:variable name="keywords"
                  select="/root/*[name() != 'gui' and name() != 'request']/keyword"/>

    <xsl:choose>
      <xsl:when test="$keywords">
        <xsl:for-each-group select="$keywords"
                            group-by="thesaurus">
          <saxon:call-template name="{$tpl}"/>
        </xsl:for-each-group>
      </xsl:when>
      <xsl:otherwise>
        <saxon:call-template name="{$tpl}"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>
</xsl:stylesheet>
