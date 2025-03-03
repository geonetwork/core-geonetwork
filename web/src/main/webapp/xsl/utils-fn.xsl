<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="2.0">

  <!-- Search for any of the searchStrings provided -->
  <xsl:function name="geonet:contains-any-of" as="xs:boolean">
    <xsl:param name="arg" as="xs:string?"/>
    <xsl:param name="searchStrings" as="xs:string*"/>

    <xsl:sequence
      select="
      some $searchString in $searchStrings
      satisfies contains($arg,$searchString)
      "
    />
  </xsl:function>

  <xsl:function name="geonet:ends-with-any-of" as="xs:boolean">
    <xsl:param name="arg" as="xs:string?"/>
    <xsl:param name="searchStrings" as="xs:string*"/>

    <xsl:sequence
      select="
      some $searchString in $searchStrings
      satisfies ends-with($arg,$searchString)
      "
    />
  </xsl:function>


  <!-- Remove special character not allowed in CSS style named -->
  <xsl:function name="geonet:clear-string-for-css" as="xs:string">
    <xsl:param name="arg" as="xs:string"/>

    <xsl:value-of select="translate($arg, '-_:', '')"/>
  </xsl:function>


  <!-- Check for image file extension -->
  <xsl:function name="geonet:is-image" as="xs:boolean">
    <xsl:param name="fileName" as="xs:string"/>
    <xsl:choose>
      <xsl:when
        test="geonet:ends-with-any-of(lower-case($fileName), ('gif', 'png', 'jpg', 'jpeg', 'bmp', 'tiff'))">
        <xsl:value-of select="true()"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="false()"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>


  <!-- Compute thumbnail URL according to metadata (harvested or not)
  and thumbnail type (local or not). If thumbnail contains http, return the URL. -->
  <xsl:function name="geonet:get-thumbnail-url" as="xs:string">
    <xsl:param name="fileName" as="xs:string"/>
    <xsl:param name="info" as="node()?"/>
    <!-- usually /root/gui/locService -->
    <xsl:param name="baseUrl" as="xs:string?"/>

    <xsl:choose>
      <xsl:when test="contains($fileName ,'://')">
        <xsl:value-of select="$fileName"/>
      </xsl:when>
      <xsl:when test="$info/isHarvested = 'y'">
        <xsl:choose>
          <xsl:when test="$info/harvestInfo/smallThumbnail">
            <xsl:value-of select="concat($info/harvestInfo/smallThumbnail, $fileName)"/>
          </xsl:when>
          <xsl:otherwise>
            <!-- When harvested, thumbnail is stored in local node (eg. ogcwxs).
              Only GeoNetHarvester set smallThumbnail elements.
            -->
            <xsl:value-of
              select="concat($baseUrl,'/resources.get?id=',$info/id,'&amp;fname=',$fileName,'&amp;access=public')"
            />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of
          select="concat($baseUrl,'/resources.get?id=',$info/id,'&amp;fname=',$fileName,'&amp;access=public')"
        />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="geonet:getCodeListValue" as="xs:string">
    <xsl:param name="loc"/>
    <xsl:param name="schema"/>
    <xsl:param name="qname"/>
    <xsl:param name="code"/>


    <!--
      Get codelist from profil first and use use default one if not
      available.
    -->
    <xsl:variable name="codelistProfil">
      <xsl:choose>
        <xsl:when test="starts-with($schema,'iso19139.')">
          <xsl:value-of
            select="$loc/*[name(.)=$schema]/codelists/codelist[@name = $qname and not(@displayIf)]/entry[code = $code]/label"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of
            select="$loc/*[name(.)=$schema]/codelists/codelist[@name = $qname and not(@displayIf)]/entry[code = $code]/label"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="normalize-space($codelistProfil)!=''">
        <xsl:value-of select="$codelistProfil"/>
      </xsl:when>
      <xsl:otherwise>

        <xsl:value-of
          select="$loc/*[name(.)='iso19139']/codelists/codelist[@name = $qname and not(@displayIf)]/entry[code=$code]/label"/>

      </xsl:otherwise>
    </xsl:choose>

  </xsl:function>


  <!-- Return mimetype according to protocol and linkage extension -->
  <xsl:function name="geonet:protocolMimeType" as="xs:string">
    <xsl:param name="linkage" as="xs:string"/>
    <xsl:param name="protocol" as="xs:string?"/>
    <xsl:param name="mimeType" as="xs:string?"/>

    <xsl:choose>
      <xsl:when
        test="(starts-with($protocol,'WWW:LINK-') or starts-with($protocol,'WWW:DOWNLOAD-')) and $mimeType!=''">
        <xsl:value-of select="$mimeType"/>
      </xsl:when>
      <xsl:when test="starts-with($protocol,'WWW:LINK')">text/html</xsl:when>
      <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD') and contains($linkage,'.jpg')">
        image/jpeg
      </xsl:when>
      <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD') and contains($linkage,'.png')">
        image/png
      </xsl:when>
      <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD') and contains($linkage,'.gif')">
        image/gif
      </xsl:when>
      <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD') and contains($linkage,'.doc')">
        application/word
      </xsl:when>
      <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD') and contains($linkage,'.zip')">
        application/zip
      </xsl:when>
      <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD') and contains($linkage,'.pdf')">
        application/pdf
      </xsl:when>
      <xsl:when test="starts-with($protocol,'GLG:KML') and contains($linkage,'.kml')">
        application/vnd.google-earth.kml+xml
      </xsl:when>
      <xsl:when test="starts-with($protocol,'GLG:KML') and contains($linkage,'.kmz')">
        application/vnd.google-earth.kmz
      </xsl:when>
      <xsl:when test="starts-with($protocol,'OGC:WMS')">application/vnd.ogc.wms_xml</xsl:when>
      <xsl:when test="$protocol='ESRI:AIMS-'">application/vnd.esri.arcims_axl</xsl:when>
      <xsl:when test="$protocol!=''">
        <xsl:value-of select="$protocol"/>
      </xsl:when>
      <!-- fall back to the default content type -->
      <xsl:otherwise>text/plain</xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <!-- Replace a given list of $placeholders by a list of respective $values in the given $string. -->
  <xsl:function name="geonet:replacePlaceholders">
    <xsl:param name="string"/>
    <xsl:param name="placeholders"/>
    <xsl:param name="values"/>
    <xsl:choose>
      <xsl:when test="count($placeholders)=1">
        <xsl:value-of select="replace($string, $placeholders[position()=1], $values[position()=1])"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="geonet:replacePlaceholders(replace($string, $placeholders[position()=1], $values[position()=1]), $placeholders[position()>1], $values[position()>1])"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

</xsl:stylesheet>
