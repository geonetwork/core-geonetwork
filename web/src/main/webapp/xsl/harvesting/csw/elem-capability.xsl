<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<!-- ============================================================================================= -->
	<!-- === Generate a table that represents a search on the remote node -->
	<!-- ============================================================================================= -->

	<xsl:template match="/root/search">
		<search id="{@id}">
			<xsl:apply-templates select="." mode="data"/>
		</search>
	</xsl:template>

	<!-- ============================================================================================= -->
		
	<xsl:template match="*" mode="data">
		<xsl:for-each select="/root/search/*">

      <!-- Queryable fields with a namespace are stored replacing : with __ to avoid issues in the SettingsManager -->
      <xsl:variable name="nameVal">
        <xsl:choose>
          <xsl:when test="contains(name(), ':')">
            <xsl:value-of select="concat(substring-before(name(), ':'), '__', substring-after(name(), ':'))" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="name()" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <capability>
        <xsl:attribute name="name">
          <xsl:value-of select="normalize-space($nameVal)" />
        </xsl:attribute>

        <xsl:value-of select="concat('csw.',normalize-space($nameVal))" />
      </capability>
		</xsl:for-each>
		
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template match="strings"/>
	<xsl:template match="env"/>

	<!-- ============================================================================================= -->

</xsl:stylesheet>
