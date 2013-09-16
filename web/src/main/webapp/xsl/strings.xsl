<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	exclude-result-prefixes="#all">

  <xsl:output encoding="UTF-8" method="text"/>

  <!-- Use the link parameter to display a custom hyperlink instead of 
  a default GeoNetwork Jeeves service URL. -->
	<xsl:template match="/">
var Geonet = {
    translate: function (string){
        return Geonet[string] || string;
    },<xsl:apply-templates mode="js-translations" select="/root/gui/strings//*[@js]"/>
};
	</xsl:template>
    <xsl:template match="*" mode="js-translations">
    "<xsl:value-of select="name(.)"/>":"<xsl:value-of select="normalize-space(replace(translate(.,'&quot;', '`'), '[\n\r]+', ' '))"/>"<xsl:if test="position()!=last()">,</xsl:if>
    </xsl:template>
</xsl:stylesheet>
