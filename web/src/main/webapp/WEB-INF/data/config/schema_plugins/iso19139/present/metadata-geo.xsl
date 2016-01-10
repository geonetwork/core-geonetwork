<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:java="java:org.fao.geonet.util.GmlWktConverter" 
    xmlns:math="http://exslt.org/math" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd" 
    xmlns:gts="http://www.isotc211.org/2005/gts" xmlns:gco="http://www.isotc211.org/2005/gco" 
    xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:srv="http://www.isotc211.org/2005/srv" 
    xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink" 
    xmlns:geonet="http://www.fao.org/geonetwork" xmlns:exslt="http://exslt.org/common" 
    xmlns:saxon="http://saxon.sf.net/"
    extension-element-prefixes="saxon"
    exclude-result-prefixes="gmd gco gml gts srv gmx xlink exslt geonet java math">
    
	<xsl:output name="serialisation-output-format" method="xml" omit-xml-declaration="yes"/>

    <xsl:template mode="iso19139" match="gmd:EX_BoundingPolygon" priority="20">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>
      
        <xsl:apply-templates mode="iso19139" select="gmd:extentTypeCode">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>
      
        <xsl:apply-templates mode="elementEP" select="geonet:child">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>
      
        <xsl:apply-templates mode="iso19139" select="gmd:polygon">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- Create an hidden input field which store
    the GML geometry for editing on the client side.
    
    The input is prefixed by "_X" in order to process
    XML in DataManager.
    -->
    <xsl:template mode="iso19139" match="gmd:polygon" priority="20">
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
                    <xsl:variable name="wkt">
                        <xsl:apply-templates mode="wkt" select="gml:*"/>
                    </xsl:variable>  
                    <xsl:call-template name="showMap">
                        <xsl:with-param name="edit" select="$edit"/>
                        <xsl:with-param name="mode" select="'polygon'" />
                        <xsl:with-param name="coords" select="$wkt"/>
                        <xsl:with-param name="targetPolygon" select="$targetId"/>
                        <xsl:with-param name="eltRef" select="$targetId"/>
                    </xsl:call-template>
                </td>
            </xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <!-- Return WKT for GML node  -->
    <xsl:template mode="wkt" match="*">
        <xsl:variable name="gml-node">
            <xsl:apply-templates select="." mode="strip-geonet"/>
        </xsl:variable>
        <xsl:variable name="gml" select="saxon:serialize($gml-node, 'serialisation-output-format')"/>
        <xsl:value-of select="java:gmlToWkt($gml)"/>
    </xsl:template>

    <!-- Remove all elements added for editing -->
    <xsl:template mode="strip-geonet" match="geonet:*"/>

    <xsl:template mode="strip-geonet" match="*[
         contains(local-name(), 'CHOICE_ELEMENT') 
         or contains(local-name(), 'GROUP_ELEMENT') 
         or contains(local-name(), 'SEQUENCE_ELEMENT')
    ]">
        <xsl:apply-templates mode="strip-geonet" select="@*|node()"/>
    </xsl:template>

    <xsl:template mode="strip-geonet" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="strip-geonet" select="@*|node()"/>
         </xsl:copy>
    </xsl:template>

    <!-- Compute global bbox of current metadata record -->
    <xsl:template name="iso19139-global-bbox">
        <xsl:param name="separator" select="','"/>
        <xsl:if test="//gmd:EX_GeographicBoundingBox">
            <xsl:value-of select="math:min(//gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal)"/>
            <xsl:value-of select="$separator"/>
            <xsl:value-of select="math:min(//gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal)"/>
            <xsl:value-of select="$separator"/>
            <xsl:value-of select="math:max(//gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal)"/>
            <xsl:value-of select="$separator"/>
            <xsl:value-of select="math:max(//gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal)"/>
        </xsl:if>
    </xsl:template>
    
    <!-- Do not allow multiple polygons in same extent. -->
    <xsl:template mode="elementEP" match="geonet:child[@name='polygon' and @prefix='gmd' and preceding-sibling::gmd:polygon]" priority="20"/>


    <!-- ============================================================================= -->
    <xsl:template mode="iso19139" match="gmd:EX_GeographicBoundingBox" priority="2">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>
        
        <!-- regions combobox -->
        <xsl:variable name="places">
          <xsl:if test="$edit=true() and /root/gui/regions/record">
            <xsl:variable name="ref" select="geonet:element/@ref"/>
            <xsl:variable name="keyword" select="string(.)"/>
            
            <xsl:variable name="selection" select="concat(gmd:westBoundLongitude/gco:Decimal,';',gmd:eastBoundLongitude/gco:Decimal,';',gmd:southBoundLatitude/gco:Decimal,';',gmd:northBoundLatitude/gco:Decimal)"/>
            <xsl:variable name="lang" select="/root/gui/language"/>
            
            <select name="place" size="1" onChange="javascript:setRegion('{gmd:westBoundLongitude/gco:Decimal/geonet:element/@ref}', '{gmd:eastBoundLongitude/gco:Decimal/geonet:element/@ref}', '{gmd:southBoundLatitude/gco:Decimal/geonet:element/@ref}', '{gmd:northBoundLatitude/gco:Decimal/geonet:element/@ref}', this.options[this.selectedIndex], {geonet:element/@ref}, '{../../gmd:description/gco:CharacterString/geonet:element/@ref}')" class="md">
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
        
        <xsl:apply-templates mode="iso19139" select="gmd:extentTypeCode">
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
                <xsl:with-param name="sEl" select="gmd:southBoundLatitude"/>
                <xsl:with-param name="nEl" select="gmd:northBoundLatitude"/>
                <xsl:with-param name="eEl" select="gmd:eastBoundLongitude"/>
                <xsl:with-param name="wEl" select="gmd:westBoundLongitude"/>
                <xsl:with-param name="sValue" select="gmd:southBoundLatitude/gco:Decimal/text()"/>
                <xsl:with-param name="nValue" select="gmd:northBoundLatitude/gco:Decimal/text()"/>
                <xsl:with-param name="eValue" select="gmd:eastBoundLongitude/gco:Decimal/text()"/>
                <xsl:with-param name="wValue" select="gmd:westBoundLongitude/gco:Decimal/text()"/>
                <xsl:with-param name="sId" select="gmd:southBoundLatitude/gco:Decimal/geonet:element/@ref"/>
                <xsl:with-param name="nId" select="gmd:northBoundLatitude/gco:Decimal/geonet:element/@ref"/>
                <xsl:with-param name="eId" select="gmd:eastBoundLongitude/gco:Decimal/geonet:element/@ref"/>
                <xsl:with-param name="wId" select="gmd:westBoundLongitude/gco:Decimal/geonet:element/@ref"/>
                <xsl:with-param name="descId" select="../../gmd:description/gco:CharacterString/geonet:element/@ref"/>
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
