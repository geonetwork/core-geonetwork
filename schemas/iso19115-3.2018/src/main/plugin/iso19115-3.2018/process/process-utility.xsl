<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:mas="http://standards.iso.org/iso/19115/-3/mas/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mda="http://standards.iso.org/iso/19115/-3/mda/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/2.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="2.0" exclude-result-prefixes="#all">

  <!-- Language of the GUI -->
  <xsl:param name="guiLang" select="'eng'"/>

  <!-- Webapp name-->
  <xsl:param name="baseUrl" select="''"/>

  <!-- Catalog URL from protocol to lang -->
  <xsl:param name="catalogUrl" select="''"/>
  <xsl:param name="nodeId" select="''"/>
  <xsl:variable name="schema" select="'iso19115-3.2018'"/>

  <!-- Search for any of the searchStrings provided -->
  <xsl:function name="gn:parseBoolean" as="xs:boolean">
    <xsl:param name="arg"/>
    <xsl:value-of
      select="if ($arg='on' or $arg=true() or $arg='true' or $arg='1') then true() else false()"/>
  </xsl:function>

  <!-- Return the message identified by the id in the required language
  or return the english message if not found. -->
  <xsl:function name="gn:i18n" as="xs:string">
    <xsl:param name="loc"/>
    <xsl:param name="id"/>
    <xsl:param name="lang"/>
    <xsl:value-of
      select="if ($loc/msg[@id=$id and @xml:lang=$lang]) then $loc/msg[@id=$id and @xml:lang=$lang] else $loc/msg[@id=$id and @xml:lang='en']"/>
  </xsl:function>

  <!--
  Retrive a WMS capabilities document.
  -->
  <xsl:function name="gn:get-wms-capabilities" as="node()">
    <xsl:param name="url" as="xs:string"/>
    <xsl:param name="version" as="xs:string"/>

    <xsl:copy-of
      select="gn:get-wxs-capabilities($url, 'WMS', $version)"/>

  </xsl:function>

  <xsl:function name="gn:get-wxs-capabilities" as="node()">
    <xsl:param name="url" as="xs:string"/>
    <xsl:param name="type" as="xs:string"/>
    <xsl:param name="version" as="xs:string"/>
    <xsl:variable name="sep" select="if (contains($url, '?')) then '&amp;' else '?'"/>

    <xsl:copy-of
      select="document(concat($url, $sep,
      if (contains(upper-case($url), 'SERVICE=')) then '' else concat('SERVICE=', $type),
      if (contains(upper-case($url), 'VERSION=')) then '' else concat('&amp;VERSION=', $version),
      if (contains(upper-case($url), 'REQUEST=')) then '' else '&amp;REQUEST=GetCapabilities'))"/>

  </xsl:function>

  <!-- Create a GetMap request for the layer which could be used to set a thumbnail.
  TODO : add projection, width, height
  -->
  <xsl:function name="gn:get-wms-thumbnail-url" as="xs:string">
    <xsl:param name="url" as="xs:string"/>
    <xsl:param name="version" as="xs:string"/>
    <xsl:param name="layer" as="xs:string"/>
    <xsl:param name="bbox" as="xs:string"/>

    <xsl:value-of
      select="concat($url, '?SERVICE=WMS&amp;VERSION=', $version, '&amp;REQUEST=GetMap&amp;SRS=EPSG:4326&amp;WIDTH=400&amp;HEIGHT=400&amp;FORMAT=image/png&amp;STYLES=&amp;LAYERS=', $layer, '&amp;BBOX=', $bbox)"/>

  </xsl:function>


  <!-- Create an ISO 19139 extent fragment -->
  <xsl:function name="gn:make-iso-extent" as="node()">
    <xsl:param name="w" as="xs:string"/>
    <xsl:param name="s" as="xs:string"/>
    <xsl:param name="e" as="xs:string"/>
    <xsl:param name="n" as="xs:string"/>
    <xsl:param name="description" as="xs:string?"/>

    <gex:EX_Extent>
      <xsl:if test="normalize-space($description)!=''">
        <gex:description>
          <gco:CharacterString>
            <xsl:value-of select="$description"/>
          </gco:CharacterString>
        </gex:description>
      </xsl:if>
      <gex:geographicElement>
        <gex:EX_GeographicBoundingBox>
          <gex:westBoundLongitude>
            <gco:Decimal>
              <xsl:value-of select="$w"/>
            </gco:Decimal>
          </gex:westBoundLongitude>
          <gex:eastBoundLongitude>
            <gco:Decimal>
              <xsl:value-of select="$e"/>
            </gco:Decimal>
          </gex:eastBoundLongitude>
          <gex:southBoundLatitude>
            <gco:Decimal>
              <xsl:value-of select="$s"/>
            </gco:Decimal>
          </gex:southBoundLatitude>
          <gex:northBoundLatitude>
            <gco:Decimal>
              <xsl:value-of select="$n"/>
            </gco:Decimal>
          </gex:northBoundLatitude>
        </gex:EX_GeographicBoundingBox>
      </gex:geographicElement>
    </gex:EX_Extent>
  </xsl:function>

</xsl:stylesheet>
