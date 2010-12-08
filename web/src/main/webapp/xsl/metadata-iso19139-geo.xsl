<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:java="java:org.fao.geonet.util.XslUtil" 
    xmlns:math="http://exslt.org/math" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:geonet="http://www.fao.org/geonetwork" xmlns:exslt="http://exslt.org/common" exclude-result-prefixes="gmd gco gml gts srv xlink exslt geonet java math">

    <xsl:template mode="iso19139" match="gmd:EX_BoundingPolygon" priority="20">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>
        
        <xsl:apply-templates mode="iso19139" select="gmd:polygon">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>
        <xsl:apply-templates mode="elementEP" select="geonet:child">
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
    <!-- Display the extent widget composed of
        * 4 input text fields with bounds coordinates
        * a list of common places (in editing mode only) for quick zoom action
        * a coordinate system switcher. Coordinates are stored in WGS84 but could be displayed 
        or editied in antother projection. 
    -->
    <xsl:template mode="iso19139" match="gmd:EX_GeographicBoundingBox" priority="2">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>
        
        <xsl:variable name="geoBox">
            <xsl:apply-templates mode="iso19139GeoBox" select=".">
                <xsl:with-param name="schema" select="$schema"/>
                <xsl:with-param name="edit"   select="$edit"/>
            </xsl:apply-templates>
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
    
    
    <!-- Display coordinates with 2 fields:
     * one to store the value but hidden (always in WGS84 as defined in ISO). 
     This element is post via the form.
     * one to display the coordinate in user defined projection.
    -->
    <xsl:template mode="iso19139VertElement" match="*">
        <xsl:param name="schema" />
        <xsl:param name="edit" />
        <xsl:param name="name" />
        <xsl:param name="eltRef" />
        
        <xsl:variable name="title">
            <xsl:call-template name="getTitle">
                <xsl:with-param name="schema" select="$schema" />
                <xsl:with-param name="name" select="$name" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="helpLink">
            <xsl:call-template name="getHelpLink">
                <xsl:with-param name="schema" select="$schema" />
                <xsl:with-param name="name" select="$name" />
            </xsl:call-template>
        </xsl:variable>
        <b>
            <xsl:choose>
                <xsl:when test="$helpLink!=''">
                    <span id="tip.{$helpLink}" style="cursor:help;">
                        <xsl:value-of select="$title" />
                        <xsl:call-template name="asterisk">
                            <xsl:with-param name="link" select="$helpLink" />
                            <xsl:with-param name="edit" select="$edit" />
                        </xsl:call-template>
                    </span>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$title" />
                </xsl:otherwise>
            </xsl:choose>
        </b>
        <br/>
        <xsl:variable name="size" select="'8'"/>
        
        <xsl:choose>
        	<!-- Hidden text field is use to store WGS84 values which are stored in metadata records. -->
            <xsl:when test="$edit=true()">
                <xsl:call-template name="getElementText">
                    <xsl:with-param name="schema" select="$schema" />
                    <xsl:with-param name="edit" select="$edit" />
                    <xsl:with-param name="cols" select="$size" />
                    <xsl:with-param name="validator" select="'validateNumber(this, false)'" />
                    <xsl:with-param name="no_name" select="true()" />
                </xsl:call-template>
                <xsl:call-template name="getElementText">
                    <xsl:with-param name="schema" select="$schema" />
                    <xsl:with-param name="edit" select="true()" />
                    <xsl:with-param name="cols" select="$size" />
                    <xsl:with-param name="input_type" select="'hidden'" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <input class="md" type="text" id="{$eltRef}" value="{text()}" readonly="readonly" size="{$size}"/>
                <input class="md" type="hidden" id="_{$eltRef}" name="_{$eltRef}" value="{text()}" readonly="readonly"/>
            </xsl:otherwise>
        </xsl:choose>
        
    </xsl:template>

    <xsl:template mode="iso19139GeoBox" match="*">
        <xsl:param name="schema" />
        <xsl:param name="edit" />
        
        <xsl:variable name="eltRef">
            <xsl:choose>
                <xsl:when test="$edit=true()">
                    <xsl:value-of select="geonet:element/@ref"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="generate-id(.)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <!-- regions combobox -->
        <xsl:variable name="places">
            <xsl:if test="$edit=true() and /root/gui/regions/record">
                <xsl:variable name="ref" select="geonet:element/@ref"/>
                <xsl:variable name="keyword" select="string(.)"/>
                
                <xsl:variable name="selection" select="concat(gmd:westBoundLongitude/gco:Decimal,';',gmd:eastBoundLongitude/gco:Decimal,';',gmd:southBoundLatitude/gco:Decimal,';',gmd:northBoundLatitude/gco:Decimal)"/>            
                <xsl:variable name="lang" select="/root/gui/language"/>
                
                <select name="place" size="1" onChange="javascript:setRegion('{gmd:westBoundLongitude/gco:Decimal/geonet:element/@ref}', '{gmd:eastBoundLongitude/gco:Decimal/geonet:element/@ref}', '{gmd:southBoundLatitude/gco:Decimal/geonet:element/@ref}', '{gmd:northBoundLatitude/gco:Decimal/geonet:element/@ref}', this.options[this.selectedIndex], {$eltRef}, '{../../gmd:description/gco:CharacterString/geonet:element/@ref}')" class="md">
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
        
        
        <!-- Loop on all projections defined in config-gui.xml -->
        <xsl:for-each select="/root/gui/config/map/proj/crs">
            <input id="{@code}_{$eltRef}" type="radio" class="proj" name="proj_{$eltRef}" value="{@code}">
                <xsl:if test="@default='1'"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
            </input>
            <!-- Set label from loc file -->
            <label for="{@code}_{$eltRef}">
                <xsl:variable name="code" select="@code"/>
                <xsl:choose>
                    <xsl:when test="/root/gui/strings/*[@code=$code]"><xsl:value-of select="/root/gui/strings/*[@code=$code]"/></xsl:when>
                    <xsl:otherwise><xsl:value-of select="@code"/></xsl:otherwise>
                </xsl:choose>
            </label>
        	<xsl:text>&#160;&#160;</xsl:text>
        </xsl:for-each>
        
        
        <table>
            <tr>
                <td />
                <td class="padded" align="center">
                    <xsl:apply-templates mode="iso19139VertElement"
                        select="gmd:northBoundLatitude/gco:Decimal">
                        <xsl:with-param name="schema" select="$schema" />
                        <xsl:with-param name="edit" select="$edit" />
                        <xsl:with-param name="name" select="'gmd:northBoundLatitude'" />
                        <xsl:with-param name="eltRef" select="concat('n', $eltRef)"/>
                    </xsl:apply-templates>
                </td>
                <td >
                    <xsl:copy-of select="$places"/>
                </td>
            </tr>
            <tr>
                <td class="padded" style="align:center;vertical-align: middle">
                    <xsl:apply-templates mode="iso19139VertElement"
                        select="gmd:westBoundLongitude/gco:Decimal">
                        <xsl:with-param name="schema" select="$schema" />
                        <xsl:with-param name="edit" select="$edit" />
                        <xsl:with-param name="name" select="'gmd:westBoundLongitude'" />
                        <xsl:with-param name="eltRef" select="concat('w', $eltRef)"/>
                    </xsl:apply-templates>
                </td>
                
                <td class="padded">
                    <xsl:variable name="w" select="./gmd:westBoundLongitude/gco:Decimal"/>
                    <xsl:variable name="e" select="./gmd:eastBoundLongitude/gco:Decimal"/>
                    <xsl:variable name="n" select="./gmd:northBoundLatitude/gco:Decimal"/>
                    <xsl:variable name="s" select="./gmd:southBoundLatitude/gco:Decimal"/>
                    
                    <xsl:variable name="wID">
                        <xsl:choose>
                            <xsl:when test="$edit=true()"><xsl:value-of select="./gmd:westBoundLongitude/gco:Decimal/geonet:element/@ref"/></xsl:when>
                            <xsl:otherwise>w<xsl:value-of select="$eltRef"/></xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    
                    <xsl:variable name="eID">
                        <xsl:choose>
                            <xsl:when test="$edit=true()"><xsl:value-of select="./gmd:eastBoundLongitude/gco:Decimal/geonet:element/@ref"/></xsl:when>
                            <xsl:otherwise>e<xsl:value-of select="$eltRef"/></xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    
                    <xsl:variable name="nID">
                        <xsl:choose>
                            <xsl:when test="$edit=true()"><xsl:value-of select="./gmd:northBoundLatitude/gco:Decimal/geonet:element/@ref"/></xsl:when>
                            <xsl:otherwise>n<xsl:value-of select="$eltRef"/></xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    
                    <xsl:variable name="sID">
                        <xsl:choose>
                            <xsl:when test="$edit=true()"><xsl:value-of select="./gmd:southBoundLatitude/gco:Decimal/geonet:element/@ref"/></xsl:when>
                            <xsl:otherwise>s<xsl:value-of select="$eltRef"/></xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    
                    
                    <xsl:variable name="geom" >
                        <xsl:value-of select="concat('Polygon((', $w, ' ', $s,',',$e,' ',$s,',',$e,' ',$n,',',$w,' ',$n,',',$w,' ',$s, '))')"/>
                    </xsl:variable>
                    <xsl:call-template name="showMap">
                        <xsl:with-param name="edit" select="$edit" />
                        <xsl:with-param name="mode" select="'bbox'" />
                        <xsl:with-param name="coords" select="$geom"/>
                        <xsl:with-param name="watchedBbox" select="concat($wID, ',', $sID, ',', $eID, ',', $nID)"/>
                        <xsl:with-param name="eltRef" select="$eltRef"/>
                    </xsl:call-template>
                </td>
                
                <td class="padded"  style="align:center;vertical-align: middle">
                    <xsl:apply-templates mode="iso19139VertElement"
                        select="gmd:eastBoundLongitude/gco:Decimal">
                        <xsl:with-param name="schema" select="$schema" />
                        <xsl:with-param name="edit" select="$edit" />
                        <xsl:with-param name="name" select="'gmd:eastBoundLongitude'" />
                        <xsl:with-param name="eltRef" select="concat('e', $eltRef)"/>
                    </xsl:apply-templates>
                </td>
            </tr>
            <tr>
                <td />
                <td class="padded" align="center">
                    <xsl:apply-templates mode="iso19139VertElement"
                        select="gmd:southBoundLatitude/gco:Decimal">
                        <xsl:with-param name="schema" select="$schema" />
                        <xsl:with-param name="edit" select="$edit" />
                        <xsl:with-param name="name" select="'gmd:southBoundLatitude'" />
                        <xsl:with-param name="eltRef" select="concat('s', $eltRef)"/>
                    </xsl:apply-templates>
                </td>
                <td />
            </tr>
        </table>
    </xsl:template>



    <!-- Create a div with class name set to extentViewer in 
        order to generate a new map.
        
        TODO : this template could be used by other non ISO 
        standard. Move this to util XSL.
    -->
    <xsl:template name="showMap">
        <xsl:param name="edit" />
        <xsl:param name="coords"/>
        <!-- Indicate which drawing mode is used (ie. bbox or polygon) -->
        <xsl:param name="mode"/>
        <xsl:param name="targetPolygon"/>
        <xsl:param name="watchedBbox"/>
        <xsl:param name="eltRef"/>
        <div class="extentViewer" style="width:{/root/gui/config/map/metadata/width}; height:{/root/gui/config/map/metadata/height};" 
            edit="{$edit}" 
            target_polygon="{$targetPolygon}" 
            watched_bbox="{$watchedBbox}" 
            elt_ref="{$eltRef}"
            mode="{$mode}">
            <div style="display:none;" id="coords_{$eltRef}"><xsl:value-of select="$coords"/></div>
        </div>
    </xsl:template>

</xsl:stylesheet>
