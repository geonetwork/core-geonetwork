<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns="http://www.fao.org/geonetwork"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" xalan:indent-amount="2"
                xmlns:xalan="http://xml.apache.org/xslt"/>

    <xsl:include href="../GM03to19139CHE/CHE03-to-19139.xsl"/>
	<xsl:param name="uuid"/>
    <xsl:strip-space elements="*"/>
</xsl:stylesheet>