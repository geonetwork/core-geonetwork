<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                  xmlns:gco="http://www.isotc211.org/2005/gco"
				  xmlns:srv="http://www.isotc211.org/2005/srv"
                  xmlns:gmd="http://www.isotc211.org/2005/gmd">

    <xsl:template match="gmd:MD_Metadata">
    	<service>
	        <serviceType><xsl:value-of select="gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName"/></serviceType>
	        <serviceTypeVersion><xsl:value-of select="gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceTypeVersion"/></serviceTypeVersion>
	    </service>
    </xsl:template>
</xsl:stylesheet>