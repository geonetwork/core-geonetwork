<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  xmlns:gmd="http://www.isotc211.org/2005/gmd"
                 xmlns:gco="http://www.isotc211.org/2005/gco"
                 xmlns:gml="http://www.opengis.net/gml"
                 xmlns:srv="http://www.isotc211.org/2005/srv"
                 xmlns:geonet="http://www.fao.org/geonetwork"
                 xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                 xmlns:gmx="http://www.isotc211.org/2005/gmx"
                 xmlns:java="java:org.fao.geonet.util.XslUtil"
                 xmlns:skos="http://www.w3.org/2004/02/skos/core#" version="2.0">
<xsl:import href="../../iso19139/index-fields/default.xsl"/>

<!-- SeaDataNet | MyOcean specific -->

<xsl:template mode="index" match="gmd:distributionInfo/gmd:MD_Distribution">

  <xsl:for-each
          select="gmd:distributor/gmd:MD_Distributor/gmd:distributorFormat/gmd:MD_Format/gmd:name/gco:CharacterString">
    <Field name="format" string="{string(.)}" store="true" index="true" />
  </xsl:for-each>

  <!-- index online protocol -->

  <xsl:for-each
          select="gmd:distributor/gmd:MD_Distributor/gmd:distributorTransferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:linkage/gmd:URL!='']">
    <xsl:variable name="download_check">
      <xsl:text>&amp;fname=&amp;access</xsl:text>
    </xsl:variable>
    <xsl:variable name="linkage" select="gmd:linkage/gmd:URL" />
    <xsl:variable name="title" select="normalize-space(gmd:name/gco:CharacterString|gmd:name/gmx:MimeFileType)" />
    <xsl:variable name="desc" select="normalize-space(gmd:description/gco:CharacterString)" />
    <xsl:variable name="protocol" select="normalize-space(gmd:protocol/gco:CharacterString)" />
    <xsl:variable name="mimetype" select="geonet:protocolMimeType($linkage, $protocol, gmd:name/gmx:MimeFileType/@type)" />

    <!-- If the linkage points to WMS service and no protocol specified, manage
      as protocol OGC:WMS -->
    <xsl:variable name="wmsLinkNoProtocol" select="contains(lower-case($linkage), 'service=wms') and not(string($protocol))" />

    <!-- ignore empty downloads -->
    <xsl:if test="string($linkage)!='' and not(contains($linkage,$download_check))">
      <Field name="protocol" string="{string($protocol)}" store="true" index="true" />
    </xsl:if>

    <xsl:if test="normalize-space($mimetype)!=''">
      <Field name="mimetype" string="{$mimetype}" store="true" index="true" />
    </xsl:if>

    <xsl:if test="contains($protocol, 'WWW:DOWNLOAD')">
      <Field name="download" string="true" store="false" index="true" />
    </xsl:if>

    <xsl:if test="$protocol = 'OGC:WMS' or $wmsLinkNoProtocol">
      <Field name="dynamic" string="true" store="false" index="true" />
    </xsl:if>

    <!-- ignore WMS links without protocol (are indexed below with mimetype
      application/vnd.ogc.wms_xml) -->
    <xsl:if test="not($wmsLinkNoProtocol)">
      <Field name="link" string="{concat($title, '|', $desc, '|', $linkage, '|', $protocol, '|', $protocol)}" store="true" index="false" />
    </xsl:if>

    <!-- Add KML link if WMS. TODO: unused in SXT anyway ? -->
    <xsl:if test="starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-map') and string($linkage)!='' and string($title)!=''">
      <!-- FIXME : relative path -->
      <Field name="link"
             string="{concat($title, '|', $desc, '|',
						'../../srv/en/google.kml?uuid=', /gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString, '&amp;layers=', $title,
						'|application/vnd.google-earth.kml+xml|application/vnd.google-earth.kml+xml')}"
             store="true" index="false" />
    </xsl:if>

    <!-- Try to detect Web Map Context by checking protocol or file extension -->
    <xsl:if test="starts-with($protocol,'OGC:WMC') or contains($linkage,'.wmc')">
      <Field name="link"
             string="{concat($title, '|', $desc, '|',
						$linkage, '|application/vnd.ogc.wmc|application/vnd.ogc.wmc')}"
             store="true" index="false" />
    </xsl:if>

    <xsl:if test="$wmsLinkNoProtocol">
      <Field name="link"
             string="{concat($title, '|', $desc, '|',
						$linkage, '|OGC:WMS|application/vnd.ogc.wms_xml')}"
             store="true" index="false" />
    </xsl:if>
  </xsl:for-each>
</xsl:template>

</xsl:stylesheet>
