<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs" version="2.0">

  
  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
  <!-- iso19115 brief formatting -->
  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
  
  <xsl:template name="iso19115Brief">
    <metadata>
      <xsl:variable name="download_check"><xsl:text>&amp;fname=&amp;access</xsl:text></xsl:variable>
      <xsl:variable name="id" select="gn:info/id"/>
      <xsl:variable name="uuid" select="gn:info/uuid"/>
      
      <xsl:if test="dataIdInfo/idCitation/resTitle">
        <title><xsl:value-of select="dataIdInfo/idCitation/resTitle"/></title>
      </xsl:if>
      <xsl:if test="dataIdInfo/idAbs">
        <abstract><xsl:value-of select="dataIdInfo/idAbs"/></abstract>
      </xsl:if>
      
      <xsl:for-each select="dataIdInfo/descKeys/keyword[text()]">
        <xsl:copy-of select="."/>
      </xsl:for-each>
      
      <xsl:for-each select="distInfo/distTranOps/onLineSrc">
        
        <xsl:comment>The links here are meant to replace the custom links as created in the next section</xsl:comment>
        
        <xsl:variable name="protocol" select="protocol"/>
        <xsl:variable name="linkage"  select="linkage"/>
        <xsl:variable name="name"     select="orName"/>
        <xsl:variable name="desc"     select="orDesc"/>
        
        <xsl:if test="string($linkage)!=''">
          
          <xsl:element name="link">
            <xsl:attribute name="title"><xsl:value-of select="$desc"/></xsl:attribute>
            <xsl:attribute name="href"><xsl:value-of select="$linkage"/></xsl:attribute>
            <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
            <xsl:choose>
              <xsl:when test="starts-with($protocol,'WWW:LINK-')">
                <xsl:attribute name="type">text/html</xsl:attribute>
              </xsl:when>
              <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.jpg')">
                <xsl:attribute name="type">image/jpeg</xsl:attribute>
              </xsl:when>
              <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.png')">
                <xsl:attribute name="type">image/png</xsl:attribute>
              </xsl:when>
              <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.gif')">
                <xsl:attribute name="type">image/gif</xsl:attribute>
              </xsl:when>
              <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.doc')">
                <xsl:attribute name="type">application/word</xsl:attribute>
              </xsl:when>
              <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.zip')">
                <xsl:attribute name="type">application/zip</xsl:attribute>
              </xsl:when>
              <xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.pdf')">
                <xsl:attribute name="type">application/pdf</xsl:attribute>
              </xsl:when>
              <xsl:when test="starts-with($protocol,'GLG:KML-') and contains($linkage,'.kml')">
                <xsl:attribute name="type">application/vnd.google-earth.kml+xml</xsl:attribute>
              </xsl:when>
              <xsl:when test="starts-with($protocol,'GLG:KML-') and contains($linkage,'.kmz')">
                <xsl:attribute name="type">application/vnd.google-earth.kmz</xsl:attribute>
              </xsl:when>
              <xsl:when test="starts-with($protocol,'OGC:WMS-')">
                <xsl:attribute name="type">application/vnd.ogc.wms_xml</xsl:attribute>
              </xsl:when>
              <xsl:when test="$protocol='ESRI:AIMS-'">
                <xsl:attribute name="type">application/vnd.esri.arcims_axl</xsl:attribute>
              </xsl:when>
              <xsl:when test="$protocol!=''">
                <xsl:attribute name="type"><xsl:value-of select="$protocol"/></xsl:attribute>
              </xsl:when>
              <xsl:otherwise>
                <!-- fall back to the default content type -->
                <xsl:attribute name="type">text/plain</xsl:attribute>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>
          
        </xsl:if>
        
        <!-- Generate a KML output link for a WMS service -->
        <xsl:if test="string($linkage)!='' and starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-map') and $name">
          
          <xsl:element name="link">
            <xsl:attribute name="title"><xsl:value-of select="$desc"/></xsl:attribute>
            <xsl:attribute name="href">
              <xsl:value-of select="concat('http://',/root/gui/env/server/host,':',/root/gui/env/server/port,/root/gui/locService,'/google.kml?uuid=',$uuid,'&amp;layers=',$name)"/>
            </xsl:attribute>
            <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
            <xsl:attribute name="type">application/vnd.google-earth.kml+xml</xsl:attribute>
          </xsl:element>
        </xsl:if>
        
        <!-- The old links still in use by some systems. Deprecated -->
        <xsl:choose>
          <xsl:when test="starts-with(./protocol,'WWW:DOWNLOAD-') and contains(./protocol,'http--download') and string($linkage)!='' and not(contains($linkage,$download_check))"> <!-- FIXME -->
            <link type="download"><xsl:value-of select="$linkage"/></link>
          </xsl:when>
          <xsl:when test="starts-with(./protocol,'OGC:WMS-') and contains(./protocol,'-get-map') and string($linkage)!='' and string($name)!=''">
            <link type="wms">
              <xsl:value-of select="concat('javascript:addWMSServerLayers(&#34;',$linkage,'&#34;);')"/>
            </link>
            <link type="googleearth">
              <xsl:value-of select="concat(/root/gui/locService,'/google.kml?uuid=',$uuid,'&amp;layers=',$name)"/>
            </link>
          </xsl:when>
          <xsl:when test="starts-with(./protocol,'OGC:WMS-') and contains(./protocol,'-get-capabilities') and string($linkage)!=''">
            <link type="wms">
              <xsl:value-of select="concat('javascript:runIM_selectService(&#34;',$linkage,'&#34;,2,',$id,');')"/>
            </link>
          </xsl:when>
          <xsl:when test="$linkage[text()]">
            <link type="url"><xsl:value-of select="$linkage[text()]"/></link>
          </xsl:when>
        </xsl:choose>
      </xsl:for-each>
      
      <xsl:if test="dataIdInfo/geoBox">
        <geoBox>
          <westBL><xsl:value-of select="dataIdInfo/geoBox/westBL"/></westBL>
          <eastBL><xsl:value-of select="dataIdInfo/geoBox/eastBL"/></eastBL>
          <southBL><xsl:value-of select="dataIdInfo/geoBox/southBL"/></southBL>
          <northBL><xsl:value-of select="dataIdInfo/geoBox/northBL"/></northBL>
        </geoBox>
      </xsl:if>
      
      <xsl:if test="not(gn:info/server)">
        <xsl:variable name="info" select="gn:info"/>
        
        <xsl:for-each select="dataIdInfo/graphOver">
          <xsl:if test="bgFileName != ''">
            <xsl:choose>
              
              <!-- the thumbnail is an url -->
              
              <xsl:when test="contains(bgFileName ,'://')">
                <image type="unknown"><xsl:value-of select="bgFileName"/></image>               
              </xsl:when>
              
              <!-- small thumbnail -->
              
              <xsl:when test="string(bgFileDesc)='thumbnail'">
                <xsl:choose>
                  <xsl:when test="$info/isHarvested = 'y'">
                    [<xsl:if test="$info/harvestInfo/smallThumbnail">
                      <image type="thumbnail">
                        <xsl:value-of select="concat($info/harvestInfo/smallThumbnail, bgFileName)"/>
                      </image>
                    </xsl:if>]
                  </xsl:when>
                  
                  <xsl:otherwise>
                    <image type="thumbnail">
                      <xsl:value-of select="concat(/root/gui/locService,'/resources.get?id=',$id,'&amp;fname=',bgFileName,'&amp;access=public')"/>
                    </image>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              
              <!-- large thumbnail -->
              
              <xsl:when test="string(bgFileDesc)='large_thumbnail'">
                <xsl:choose>
                  <xsl:when test="$info/isHarvested = 'y'">
                    <xsl:if test="$info/harvestInfo/largeThumbnail">
                      <image type="overview">
                        <xsl:value-of select="concat($info/harvestInfo/largeThumbnail, bgFileName)"/>
                      </image>
                    </xsl:if>
                  </xsl:when>
                  
                  <xsl:otherwise>
                    <image type="overview">
                      <xsl:value-of select="concat(/root/gui/locService,'/graphover.show?id=',$id,'&amp;fname=',bgFileName,'&amp;access=public')"/>
                    </image>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              
            </xsl:choose>
          </xsl:if>
        </xsl:for-each>
      </xsl:if>
      
      <xsl:copy-of select="gn:info"/>
    </metadata>
  </xsl:template>
</xsl:stylesheet>
