<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:java="java:org.fao.geonet.util.XslUtil" 
    xmlns:math="http://exslt.org/math" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
    xmlns:gml="http://www.opengis.net/gml/3.2" 
    xmlns:xlink="http://www.w3.org/1999/xlink" 
    xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
    xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
    xmlns:geonet="http://www.fao.org/geonetwork" 
    xmlns:exslt="http://exslt.org/common" 
    exclude-result-prefixes="gco gml gex mds xlink exslt geonet java math">

    <xsl:template mode="iso19115-3.2018" match="gex:EX_BoundingPolygon" priority="20">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>
      
        <xsl:apply-templates mode="iso19115-3.2018" select="gex:extentTypeCode">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>
      
        <xsl:apply-templates mode="elementEP" select="geonet:child">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>
      
        <xsl:apply-templates mode="iso19115-3.2018" select="gex:polygon">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- Create an hidden input field which store
    the GML geometry for editing on the client side.
    
    The input is prefixed by "_X" in order to process
    XML in DataManager.
    -->
    <xsl:template mode="iso19115-3.2018" match="gex:polygon" priority="20">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>
        <xsl:variable name="targetId" select="geonet:element/@ref"/>
        <xsl:variable name="geometry">
            <xsl:apply-templates mode="editXMLElement"/>
        </xsl:variable>

        <xsl:apply-templates mode="complexElement" select=".">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
            <xsl:with-param name="content">
                <!-- TODO : hide this -->
                <textarea style="display:none;" id="_X{$targetId}" name="_X{$targetId}" rows="5" cols="40">
                    <xsl:value-of select="string($geometry)"/>
                </textarea>
                <td class="padded" style="width:100%;">
                    <xsl:variable name="ts" select="string(@ts)"/>
                    <xsl:variable name="cs" select="string(@cs)"/>
                    <xsl:variable name="wktCoords">
                        <xsl:apply-templates mode="gml" select="*"/>
                    </xsl:variable>
                    <xsl:variable name="geom">POLYGON(<xsl:value-of select="java:replace(string($wktCoords), '\),$', ')')"/>)</xsl:variable>
                    <xsl:call-template name="showMap">
                        <xsl:with-param name="edit" select="$edit"/>
                        <xsl:with-param name="mode" select="'polygon'" />
                        <xsl:with-param name="coords" select="$geom"/>
                        <xsl:with-param name="targetPolygon" select="$targetId"/>
                        <xsl:with-param name="eltRef" select="$targetId"/>
                    </xsl:call-template>
                </td>
            </xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="gml" match="gml:coordinates"><xsl:variable name="ts" select="string(@ts)"/><xsl:variable name="cs" select="string(@cs)"/>(<xsl:value-of select="java:takeUntil(java:toWktCoords(string(.),$ts,$cs), ';\Z')"/>),</xsl:template>
    <xsl:template mode="gml" match="gml:posList">(<xsl:value-of select="java:takeUntil(java:posListToWktCoords(string(.), string(@dimension)), ';\Z')"/>),</xsl:template>
    <xsl:template mode="gml" match="text()"/>
    
    <!-- Compute global bbox of current metadata record -->
    <xsl:template name="iso19115-3.2018-global-bbox">
        <xsl:param name="separator" select="','"/>
        <xsl:if test="//gex:EX_GeographicBoundingBox">
            <xsl:value-of select="math:min(//gex:EX_GeographicBoundingBox/gex:westBoundLongitude/gco:Decimal)"/>
            <xsl:value-of select="$separator"/>
            <xsl:value-of select="math:min(//gex:EX_GeographicBoundingBox/gex:southBoundLatitude/gco:Decimal)"/>
            <xsl:value-of select="$separator"/>
            <xsl:value-of select="math:max(//gex:EX_GeographicBoundingBox/gex:eastBoundLongitude/gco:Decimal)"/>
            <xsl:value-of select="$separator"/>
            <xsl:value-of select="math:max(//gex:EX_GeographicBoundingBox/gex:northBoundLatitude/gco:Decimal)"/>
        </xsl:if>
    </xsl:template>
    
    <!-- Do not allow multiple polygons in same extent. -->
    <xsl:template mode="elementEP" match="geonet:child[@name='polygon' and @prefix='gex' and preceding-sibling::gex:polygon]" priority="20"/>


    <!-- ============================================================================= -->
    <xsl:template mode="iso19115-3.2018" match="gex:EX_GeographicBoundingBox" priority="2">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>

        <!-- regions combobox -->
        <xsl:variable name="places">
          <xsl:if test="$edit=true() and /root/gui/regions/record">
            <xsl:variable name="ref" select="geonet:element/@ref"/>
            <xsl:variable name="keyword" select="string(.)"/>
            
            <xsl:variable name="selection" select="concat(gex:westBoundLongitude/gco:Decimal,';',gex:eastBoundLongitude/gco:Decimal,';',gex:southBoundLatitude/gco:Decimal,';',gex:northBoundLatitude/gco:Decimal)"/>
            <xsl:variable name="lang" select="/root/gui/language"/>
            
            <select name="place" size="1" onChange="javascript:setRegion('{gex:westBoundLongitude/gco:Decimal/geonet:element/@ref}', '{gex:eastBoundLongitude/gco:Decimal/geonet:element/@ref}', '{gex:southBoundLatitude/gco:Decimal/geonet:element/@ref}', '{gex:northBoundLatitude/gco:Decimal/geonet:element/@ref}', this.options[this.selectedIndex], {geonet:element/@ref}, '{../../gex:description/gco:CharacterString/geonet:element/@ref}')" class="md">
              <option value=""/>
              <xsl:for-each select="/root/gui/regions/record">
                <xsl:sort select="label/child::*[name() = $lang]" order="ascending"/>
                
                <xsl:variable name="value" select="concat(west,',',east,',',south,',',north)"/>
                <option value="{$value}">
                  <xsl:if test="$value=$selection">
                    <xsl:attribute name="selected"/>
                  </xsl:if>
                  <xsl:value-of select="label/child::*[name() = $lang]"/>
                </option>
              </xsl:for-each>
            </select>
          </xsl:if>
        </xsl:variable>
        
        <xsl:apply-templates mode="iso19115-3.2018" select="gex:extentTypeCode">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>
      
        <xsl:apply-templates mode="elementEP" select="geonet:child">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>
        
        <xsl:variable name="geoBox">
            <xsl:call-template name="geoBoxGUI">
                <xsl:with-param name="schema" select="$schema"/>
                <xsl:with-param name="edit"   select="$edit"/>
                <xsl:with-param name="id"   select="geonet:element/@ref"/>
                <xsl:with-param name="sEl" select="gex:southBoundLatitude"/>
                <xsl:with-param name="nEl" select="gex:northBoundLatitude"/>
                <xsl:with-param name="eEl" select="gex:eastBoundLongitude"/>
                <xsl:with-param name="wEl" select="gex:westBoundLongitude"/>
                <xsl:with-param name="sValue" select="gex:southBoundLatitude/gco:Decimal/text()"/>
                <xsl:with-param name="nValue" select="gex:northBoundLatitude/gco:Decimal/text()"/>
                <xsl:with-param name="eValue" select="gex:eastBoundLongitude/gco:Decimal/text()"/>
                <xsl:with-param name="wValue" select="gex:westBoundLongitude/gco:Decimal/text()"/>
                <xsl:with-param name="sId" select="gex:southBoundLatitude/gco:Decimal/geonet:element/@ref"/>
                <xsl:with-param name="nId" select="gex:northBoundLatitude/gco:Decimal/geonet:element/@ref"/>
                <xsl:with-param name="eId" select="gex:eastBoundLongitude/gco:Decimal/geonet:element/@ref"/>
                <xsl:with-param name="wId" select="gex:westBoundLongitude/gco:Decimal/geonet:element/@ref"/>
                <xsl:with-param name="descId" select="../../gex:description/gco:CharacterString/geonet:element/@ref"/>
                <xsl:with-param name="places" select="$places"/>
            </xsl:call-template>
        </xsl:variable>
                
        <xsl:apply-templates mode="complexElement" select=".">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit"   select="$edit"/>
            <xsl:with-param name="content">
                <tr>
                    <td align="center">
                        <xsl:copy-of select="$geoBox"/>
                    </td>
                </tr>
            </xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>
    
    

</xsl:stylesheet>
