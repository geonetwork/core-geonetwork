<?xml version="1.0" encoding="UTF-8" ?>
<!-- Index a record for the any other languages. One document is created per language. -->
<xsl:stylesheet version="2.0"
            xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
            xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
            xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
            xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
            xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
            xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
            xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
            xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
            xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
            xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
            xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
            xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
						xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
						xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
            xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
						xmlns:gml="http://www.opengis.net/gml/3.2"
            xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
						xmlns:java="java:org.fao.geonet.util.XslUtil"
						xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
            exclude-result-prefixes="#all">

  <xsl:include href="common.xsl"/>

	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="no" />

  <xsl:template match="/">
    <Documents>
      <xsl:for-each select="$metadata/mdb:otherLocale/lan:PT_Locale">
        <xsl:call-template name="indexMetadata">
          <xsl:with-param name="lang"
                          select="java:threeCharLangCode(normalize-space(string(lan:language/lan:LanguageCode/@codeListValue)))"/>
          <xsl:with-param name="langId"
                          select="@id"/>
        </xsl:call-template>
      </xsl:for-each>
    </Documents>
  </xsl:template>

</xsl:stylesheet>
