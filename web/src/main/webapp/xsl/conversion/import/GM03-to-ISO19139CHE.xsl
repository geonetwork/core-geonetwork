<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns="http://www.fao.org/geonetwork"
				xmlns:util="xalan://org.fao.geonet.services.gm03.TranslateAndValidate"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="util">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" xalan:indent-amount="2"
                xmlns:xalan="http://xml.apache.org/xslt"/>

	<xsl:param name="uuid"/>
	<xsl:param name="validate"/>
	<xsl:param name="debugDir"/>
	<xsl:param name="debugFileName"/>
	<xsl:param name="webappDir"/>
	<xsl:template match="/">
		<xsl:copy-of select="util:toCheBootstrap(., $uuid, $validate, $debugFileName, $webappDir)"></xsl:copy-of>
	</xsl:template>
    <xsl:strip-space elements="*"/>
</xsl:stylesheet>