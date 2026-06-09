<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                version="2.0" exclude-result-prefixes="#all">

  <xsl:import href="process-utility.xsl"/>

  <!-- i18n information -->
  <xsl:variable name="add-extent-loc">
    <msg id="a" xml:lang="eng">Keyword field contains place keywords (ie. </msg>
    <msg id="b" xml:lang="eng">). Try to compute metadata extent using thesaurus.</msg>
    <msg id="a" xml:lang="fre">Certains mots clés sont de type géographique (ie. </msg>
    <msg id="b" xml:lang="fre">). Exécuter cette action pour essayer de calculer l'emprise à partir des thésaurus.</msg>
  </xsl:variable>

  <!-- GeoNetwork base url -->
  <xsl:param name="siteUrl" select="'http://localhost:8080/geonetwork/srv/eng'"/>
  <xsl:param name="gurl" select="$siteUrl"/>

  <!-- The UI language. Thesaurus search is made according to GUI language -->
  <xsl:param name="guiLang" select="'eng'"/>
  <xsl:param name="lang" select="$guiLang"/>

  <!-- Replace or not existing extent -->
  <xsl:param name="replace" select="'0'"/>
  <xsl:param name="boundingAll" select="'0'"/>
  <xsl:param name="addExtentFor" select="''"/>


  <xsl:variable name="replaceMode"
                select="geonet:parseBoolean($replace)"/>
  <xsl:variable name="boundingAllMode"
                select="geonet:parseBoolean($boundingAll)"/>
  <xsl:variable name="serviceUrl"
                select="concat(substring($gurl, 1, string-length($gurl)-4), 'api/registries/vocabularies/search?_content_type=xml&amp;q=')"/>



  <xsl:template name="list-add-extent-from-geokeywords">
    <suggestion process="add-extent-from-geokeywords"/>
  </xsl:template>



  <!-- Analyze the metadata record and return available suggestion
      for that process -->
  <xsl:template name="analyze-add-extent-from-geokeywords">
    <xsl:param name="root"/>

    <xsl:variable name="extentDescription"
                  select="string-join($root//gex:EX_Extent/gex:description/gco:CharacterString, ' ')"/>

    <xsl:variable name="geoKeywords"
                  select="$root//mri:descriptiveKeywords/*/mri:keyword[
                      not(gco:CharacterString/@gco:nilReason)
                      and (not(contains($extentDescription, gco:CharacterString))
                      or not(contains($extentDescription, gcx:Anchor)))
                      and ../mri:type/*/@codeListValue='place']"/>
    <xsl:if test="$geoKeywords">
      <suggestion process="add-extent-from-geokeywords" id="{generate-id()}" category="keyword" target="extent">
        <name><xsl:value-of select="geonet:i18n($add-extent-loc, 'a', $guiLang)"/><xsl:value-of select="string-join($geoKeywords/(gco:CharacterString|gcx:Anchor), ', ')"/>
          <xsl:value-of select="geonet:i18n($add-extent-loc, 'b', $guiLang)"/></name>
        <operational>true</operational>
        <params>{"gurl":{"type":"string", "defaultValue":"<xsl:value-of select="$gurl"/>"},
          "lang":{"type":"string", "defaultValue":"<xsl:value-of select="$lang"/>"},
          "addExtentFor":{"type":"string", "defaultValue":""},
          "boundingAll":{"type":"boolean", "defaultValue":"<xsl:value-of select="$boundingAll"/>"},
          "replace":{"type":"boolean", "defaultValue":"<xsl:value-of select="$replace"/>"}}
        </params>
      </suggestion>
    </xsl:if>

  </xsl:template>




  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

  <xsl:template
          match="mdb:identificationInfo/*"
          priority="2">
    <xsl:variable name="srv"
                  select="local-name(.)='SV_ServiceIdentification'
            or contains(@gco:isoType, 'SV_ServiceIdentification')"/>

    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <!-- Copy all elements from AbstractMD_IdentificationType-->
      <xsl:copy-of
              select="mri:citation|
                mri:abstract|
                mri:purpose|
                mri:credit|
                mri:status|
                mri:pointOfContact|
                mri:spatialRepresentationType|
                mri:spatialResolution|
                mri:temporalResolution|
                mri:topicCategory
                "/>


      <!-- Keep existing extent and compute
            from keywords -->

      <!-- replace or add extent. Default mode is add.
            All extent element are processed and if a geographicElement is found,
            it will be removed. Description, verticalElement and temporalElement
            are preserved.

            GeographicElement element having BoundingPolygon are preserved.
            -->
      <xsl:choose>
        <xsl:when test="$replaceMode">
          <xsl:for-each select="mri:extent">
            <xsl:if
                    test="gex:EX_Extent/mri:temporalElement or gex:EX_Extent/mri:verticalElement
                            or gex:EX_Extent/mri:geographicElement[mri:EX_BoundingPolygon]">
              <xsl:copy>
                <xsl:copy-of select="gex:EX_Extent"/>
              </xsl:copy>
            </xsl:if>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="mri:extent"/>
        </xsl:otherwise>
      </xsl:choose>

      <!-- New extent position is after existing ones. -->
      <xsl:call-template name="add-extent"/>

      <!-- End of data -->

      <!-- Data -->
      <xsl:copy-of
              select="mri:additionalDocumentation|
                mri:processingLevel|
                mri:resourceMaintenance|
                mri:graphicOverview|
                mri:resourceFormat|
                mri:descriptiveKeywords|
                mri:resourceSpecificUsage|
                mri:resourceConstraints|
                mri:associatedResource|
                mri:defaultLocale|
                mri:otherLocale|
                mri:environmentDescription|
                mri:supplementalInformation
                "/>

      <!-- Service -->
      <xsl:copy-of select="srv:*"/>
    </xsl:copy>
  </xsl:template>


  <!-- Loop on all non empty keywords -->
  <xsl:template name="add-extent">
    <!-- Only check keyword in main metadata language
     TODO: support multilingual keyword -->
    <xsl:variable name="extentList">
      <xsl:for-each
              select="if ($addExtentFor = '')
                      then mri:descriptiveKeywords/*/mri:keyword[
                          normalize-space((gco:CharacterString|gcx:Anchor)) != ''
                          and not(gco:CharacterString/@gco:nilReason)
                          and ../mri:type/*/@codeListValue='place']
                      else  mri:descriptiveKeywords/*/mri:keyword[
                          normalize-space((gco:CharacterString|gcx:Anchor)) = $addExtentFor]">
        <xsl:call-template name="get-bbox">
          <xsl:with-param name="word" select="gco:CharacterString"/>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$boundingAllMode">
        <mri:extent>
          <xsl:copy-of
            select="geonet:make-iso-extent(
              string(min($extentList//gex:westBoundLongitude)),
              string(min($extentList//gex:southBoundLatitude)),
              string(max($extentList//gex:eastBoundLongitude)),
              string(max($extentList//gex:northBoundLatitude)),
              string-join($extentList//gex:description, ', '))"/>
        </mri:extent>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="$extentList"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Search into current thesaurus and look for a bounding box -->
  <xsl:template name="get-bbox">
    <xsl:param name="word"/>

    <xsl:if test="normalize-space($word)!=''">
      <!-- Get keyword information -->
      <xsl:variable name="keyword" select="document(concat($serviceUrl, encode-for-uri($word)))"/>

      <!-- It should be one but if one keyword is found in more
          thant one thesaurus, then each will be processed.-->
      <xsl:for-each select="$keyword/response/keyword">
        <xsl:if test="geo and geo/west != '' and geo/south != '' and geo/east != '' and geo/north != '' and count(value[text() = $word]) = 1">
          <mri:extent>
            <xsl:copy-of select="geonet:make-iso19115-3-extent(geo/west, geo/south, geo/east, geo/north, $word)"/>
          </mri:extent>
        </xsl:if>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>


  <!-- Create an ISO 19139 extent fragment -->
  <xsl:function name="geonet:make-iso19115-3-extent" as="node()">
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
