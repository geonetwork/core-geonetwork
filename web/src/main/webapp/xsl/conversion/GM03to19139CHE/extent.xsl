<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:util="xalan://org.fao.geonet.util.XslUtil"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="util xalan"
                >

    <xsl:template mode="Extent" match="GM03Core.Core.EX_Extent">
        <xsl:choose>
            <xsl:when test="GM03Core.Core.EX_ExtentgeographicElement[.//GM03Core.Core.EX_BoundingPolygon]">
                <gmd:extent>
                    <gmd:EX_Extent>
                       <xsl:apply-templates mode="Extent" select="description"/>

                        <xsl:variable name="polygon">
                            <xsl:if test="GM03Core.Core.EX_ExtentgeographicElement[.//GM03Core.Core.EX_BoundingPolygon]">
                                <xsl:apply-templates mode="Extent" select="GM03Core.Core.EX_ExtentgeographicElement[.//GM03Core.Core.EX_BoundingPolygon]"/>
                            </xsl:if>
                        </xsl:variable>
                        <xsl:if test="normalize-space($polygon) != ''">
                            <gmd:geographicElement>
                                <xsl:copy-of select="util:multipolygon(string(description), $polygon)"/>
                            </gmd:geographicElement>
                            <xsl:if test="GM03Core.Core.EX_ExtentgeographicElement[.//GM03Core.Core.EX_GeographicBoundingBox]">
                                <gmd:geographicElement>
                                    <xsl:copy-of select="util:bbox(string(description), $polygon)"/>
                                </gmd:geographicElement>
                            </xsl:if>
                        </xsl:if>
                        <xsl:apply-templates mode="Extent" select="GM03Core.Core.EX_ExtentgeographicElement[not(.//GM03Core.Core.EX_BoundingPolygon or .//GM03Core.Core.EX_GeographicBoundingBox)]"/>
                    </gmd:EX_Extent>
                </gmd:extent>
            </xsl:when>
            <xsl:when test="GM03Core.Core.EX_ExtentgeographicElement[.//GM03Core.Core.EX_GeographicBoundingBox]">
                <xsl:for-each select="GM03Core.Core.EX_ExtentgeographicElement//GM03Core.Core.EX_GeographicBoundingBox">
                    <gmd:extent>
                        <gmd:EX_Extent>
                            <xsl:apply-templates mode="Extent" select="ancestor::GM03Core.Core.EX_Extent/description"/>
                             <gmd:geographicElement>
                                <xsl:apply-templates mode="Extent" select="."/>
                            </gmd:geographicElement>
                            <xsl:apply-templates mode="Extent" select="GM03Core.Core.EX_ExtentgeographicElement[not(.//GM03Core.Core.EX_BoundingPolygon or .//GM03Core.Core.EX_GeographicBoundingBox)]"/>
                        </gmd:EX_Extent>
                    </gmd:extent>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <gmd:extent>
                    <gmd:EX_Extent>
                        <xsl:apply-templates mode="Extent" select="description"/>
                        <xsl:apply-templates mode="Extent" select="GM03Core.Core.EX_ExtentgeographicElement[not(.//GM03Core.Core.EX_BoundingPolygon or .//GM03Core.Core.EX_GeographicBoundingBox)]"/>
                    </gmd:EX_Extent>
                </gmd:extent>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:for-each select=".//GM03Core.Core.EX_ExtenttemporalElement[not(temporalElement/GM03Core.Core.EX_SpatialTemporalExtent)]">
           <gmd:extent>
               <gmd:EX_Extent>
                   <xsl:apply-templates mode="Temporal"/>
               </gmd:EX_Extent>
           </gmd:extent>
        </xsl:for-each>
        <xsl:if test=".//GM03Core.Core.EX_ExtenttemporalElement[temporalElement/GM03Core.Core.EX_SpatialTemporalExtent]">
           <xsl:comment>TODO GM03Core.Core.EX_ExtenttemporalElement without TM_Primitive</xsl:comment>
           <!-- Remove the not(temporalElement/GM03Core.Core.EX_SpatialTemporalExtent) from previous for-each and fix the spatial temporal.  
           It seems to be that XslUtil mutlipolygon and bbox expect the normal extent and cannot handle spatialExtent -->
        </xsl:if>
        <xsl:for-each select=".//GM03Core.Core.EX_VerticalExtent">
           <gmd:extent>
               <gmd:EX_Extent>
                   <xsl:apply-templates mode="Extent"/>
               </gmd:EX_Extent>
           </gmd:extent>
        </xsl:for-each>
    </xsl:template>

    <xsl:template mode="Extent" match="description">
        <gmd:description>
            <xsl:apply-templates mode="language"/>
        </gmd:description>
    </xsl:template>

    <!-- ================================================================================= -->

    <xsl:template mode="Extent" match="geographicElement[not(GM03Core.Core.EX_GeographicBoundingBox|GM03Core.Core.EX_BoundingPolygon)]">
        <gmd:geographicElement>
            <xsl:apply-templates mode="Extent"/>
        </gmd:geographicElement>
    </xsl:template>

    <xsl:template mode="Extent" match="geographicElement[GM03Core.Core.EX_BoundingPolygon]">
        <xsl:for-each select="GM03Core.Core.EX_BoundingPolygon">
            <gmd:geographicElement>
                <xsl:apply-templates mode="Extent" select="."/>
            </gmd:geographicElement>
        </xsl:for-each>
    </xsl:template>

    <xsl:template mode="Extent" match="geographicElement[GM03Core.Core.EX_GeographicBoundingBox]">
        <xsl:choose>
            <xsl:when test="ancestor::GM03Core.Core.EX_Extent//GM03Core.Core.EX_BoundingPolygon">
                <xsl:comment>
                    GeographicBBox elements an associated Polygon are ignored
                </xsl:comment>
            </xsl:when>
            <xsl:otherwise>
                <gmd:geographicElement>
                    <xsl:apply-templates mode="Extent" select="GM03Core.Core.EX_GeographicBoundingBox"/>
                </gmd:geographicElement>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template mode="Extent" match="GM03Core.Core.EX_GeographicDescription">
        <gmd:EX_GeographicDescription>
            <xsl:apply-templates mode="Extent"/>
        </gmd:EX_GeographicDescription>
    </xsl:template>

    <xsl:template mode="Extent" match="extentTypeCode">
        <gmd:extentTypeCode>
            <gco:Boolean>
                <xsl:value-of select="."/>
            </gco:Boolean>
        </gmd:extentTypeCode>
    </xsl:template>

    <xsl:template mode="Extent" match="geographicIdentifier">
        <gmd:geographicIdentifier>
            <xsl:apply-templates mode="Identifier"/>
        </gmd:geographicIdentifier>
    </xsl:template>

    <xsl:template mode="Extent" match="GM03Core.Core.EX_BoundingPolygon">
        <gmd:EX_BoundingPolygon>
            <xsl:apply-templates mode="BoundingPoly"/>
        </gmd:EX_BoundingPolygon>
    </xsl:template>

    <xsl:template mode="Extent" match="GM03Core.Core.EX_GeographicBoundingBox[not(ancestor::GM03Core.Core.EX_Extent//GM03Core.Core.EX_BoundingPolygon)]">
        <gmd:EX_GeographicBoundingBox>
            <xsl:apply-templates mode="BoundingPoly" select="extentTypeCode"/>
            <gmd:westBoundLongitude><xsl:apply-templates mode="ExtentCoord" select="westBoundLongitude"/></gmd:westBoundLongitude>
            <gmd:eastBoundLongitude><xsl:apply-templates mode="ExtentCoord" select="eastBoundLongitude"/></gmd:eastBoundLongitude>
            <gmd:southBoundLatitude><xsl:apply-templates mode="ExtentCoord" select="southBoundLatitude"/></gmd:southBoundLatitude>
            <gmd:northBoundLatitude><xsl:apply-templates mode="ExtentCoord" select="northBoundLatitude"/></gmd:northBoundLatitude>
        </gmd:EX_GeographicBoundingBox>
    </xsl:template>

    <xsl:template mode="ExtentCoord" match="text()">
        <gco:Decimal><xsl:value-of select="."/></gco:Decimal>
    </xsl:template>

    <!-- ================================================================================= -->

    <xsl:template mode="Temporal" match="GM03Core.Core.EX_ExtenttemporalElement/temporalElement">
        <gmd:temporalElement>
                <xsl:apply-templates mode="Temporal"/>
        </gmd:temporalElement>
    </xsl:template>

    <xsl:template mode="Temporal" match="GM03Core.Core.EX_TemporalExtent">
        <gmd:EX_TemporalExtent>
	        <gmd:extent>
	            <gml:TimePeriod gml:id="{util:randomId()}">
	                <xsl:apply-templates mode="TimePeriod"/>
	            </gml:TimePeriod>
	        </gmd:extent>
        </gmd:EX_TemporalExtent>
    </xsl:template>

    <xsl:template mode="Temporal" match="GM03Core.Core.EX_SpatialTemporalExtent">
                <xsl:comment>Need to implement spatialExtent import</xsl:comment>
        <!-- <gmd:EX_SpatialTemporalExtent>
	        <gmd:extent>
	            <gml:TimePeriod gml:id="{util:randomId()}">
	                <xsl:apply-templates mode="TimePeriod" select="extent"/>
	            </gml:TimePeriod>
	        </gmd:extent>
            <xsl:apply-templates mode="Temporal" select="GM03Core.Core.spatialExtentEX_SpatialTemporalExtent"/>
        </gmd:EX_SpatialTemporalExtent> -->
    </xsl:template>

	<xsl:template mode="TemporalExtent"
		match="GM03Core.Core.spatialExtentEX_SpatialTemporalExtent">
        <xsl:comment>Need to implement spatialExtent import</xsl:comment>
		<xsl:choose>
			<xsl:when test="spatialExtent[.//GM03Core.Core.EX_BoundingPolygon]">
				<gmd:spatialExtent>
					<xsl:comment>
						spatialExtent[.//GM03Core.Core.EX_BoundingPolygon]
					</xsl:comment>
					<xsl:apply-templates mode="Extent" select="description" />

					<xsl:variable name="polygon">
						<xsl:if test="spatialExtent[.//GM03Core.Core.EX_BoundingPolygon]">
							<xsl:apply-templates mode="Extent"
								select="spatialExtent[.//GM03Core.Core.EX_BoundingPolygon]" />
						</xsl:if>
					</xsl:variable>
					<xsl:if test="normalize-space($polygon) != ''">
							<xsl:copy-of select="util:multipolygon(string(description), $polygon)" />
						<xsl:if test="spatialExtent[.//GM03Core.Core.EX_GeographicBoundingBox]">
								<xsl:copy-of select="util:bbox(string(description), $polygon)" />
						</xsl:if>
					</xsl:if>
					<xsl:apply-templates mode="Extent"
						select="spatialExtent[not(.//GM03Core.Core.EX_BoundingPolygon or .//GM03Core.Core.EX_GeographicBoundingBox)]" />
				</gmd:spatialExtent>
			</xsl:when>
			<xsl:when test="spatialExtent[.//GM03Core.Core.EX_GeographicBoundingBox]">
				<xsl:for-each
					select="spatialExtent//GM03Core.Core.EX_GeographicBoundingBox">
					<gmd:spatialExtent>
						<xsl:comment>
							spatialExtent//GM03Core.Core.EX_GeographicBoundingBox
						</xsl:comment>
						<xsl:apply-templates mode="Extent"
							select="ancestor::GM03Core.Core.EX_Extent/description" />
						<gmd:geographicElement>
							<xsl:apply-templates mode="Extent" select="." />
						</gmd:geographicElement>
						<xsl:apply-templates mode="Extent"
							select="GM03Core.Core.EX_ExtentgeographicElement[not(.//GM03Core.Core.EX_BoundingPolygon or .//GM03Core.Core.EX_GeographicBoundingBox)]|spatialExtent[not(.//GM03Core.Core.EX_BoundingPolygon or .//GM03Core.Core.EX_GeographicBoundingBox)]" />
					</gmd:spatialExtent>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<gmd:spatialExtent>
					<xsl:comment>
						otherwise
					</xsl:comment>
					<xsl:apply-templates mode="Extent" select="description" />
					<xsl:apply-templates mode="Extent"
						select="GM03Core.Core.EX_ExtentgeographicElement[not(.//GM03Core.Core.EX_BoundingPolygon or .//GM03Core.Core.EX_GeographicBoundingBox)]|spatialExtent[not(.//GM03Core.Core.EX_BoundingPolygon or .//GM03Core.Core.EX_GeographicBoundingBox)]" />
				</gmd:spatialExtent>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:for-each select=".//GM03Core.Core.EX_ExtenttemporalElement">
			<gmd:spatialExtent>
				<xsl:comment>
					.//GM03Core.Core.EX_ExtenttemporalElement
				</xsl:comment>
				<xsl:apply-templates mode="Temporal" />
			</gmd:spatialExtent>
		</xsl:for-each>
		<xsl:for-each select=".//GM03Core.Core.EX_VerticalExtent">
			<gmd:spatialExtent>
				<xsl:comment>
					.//GM03Core.Core.EX_VerticalExtent
				</xsl:comment>
				<xsl:apply-templates mode="Extent" />
			</gmd:spatialExtent>
		</xsl:for-each>
	</xsl:template>

    <xsl:template mode="Temporal" match="GM03Core.Core.spatialExtentEX_SpatialTemporalExtent">
        <xsl:apply-templates mode="TemporalExtent" select="."/>
    </xsl:template>

    <xsl:template mode="Temporal" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">Temporal</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template mode="TimePeriod" match="begin">
        <gml:begin>
            <xsl:apply-templates mode="TimeInstant" select="."/>
        </gml:begin>
    </xsl:template>
    <xsl:template mode="TimePeriod" match="end">
        <gml:end>
            <xsl:apply-templates mode="TimeInstant" select="."/>
        </gml:end>
    </xsl:template>
    
    <xsl:template mode="TimeInstant" match="*">
        <xsl:variable name="time">
            <xsl:apply-templates mode="dateTime"/>
        </xsl:variable>
		<gml:TimeInstant gml:id="{util:randomId()}">
		    <gml:timePosition>
		        <xsl:value-of select="normalize-space($time//text())"/>
		    </gml:timePosition>
		</gml:TimeInstant>
    </xsl:template>
    <!-- ================================================================================= -->

    <xsl:template mode="BoundingPoly" match="extentTypeCode">
        <gmd:extentTypeCode>
            <gco:Boolean>
                <xsl:value-of select="."/>
            </gco:Boolean>
        </gmd:extentTypeCode>
    </xsl:template>

    <xsl:template mode="BoundingPoly" match="polygon">
        <gmd:polygon>
            <xsl:apply-templates mode="GM_Object" select="."/>
        </gmd:polygon>
    </xsl:template>

    <!-- ================================================================================= -->

    <xsl:template mode="GM_Object" match="polygon">
        <gml:Polygon gml:id="{generate-id(.)}">
            <xsl:apply-templates mode="GM_Object"/>
        </gml:Polygon>
    </xsl:template>

    <xsl:template mode="GM_Object" match="SURFACE">
        <xsl:for-each select="BOUNDARY">
            <xsl:choose>
                <xsl:when test="position() = 1">
                    <gml:exterior>
                        <xsl:apply-templates mode="GM_Object" select="."/>
                    </gml:exterior>
                </xsl:when>
                <xsl:otherwise>
                    <gml:interior>
                        <xsl:apply-templates mode="GM_Object" select="."/>
                    </gml:interior>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <xsl:template mode="GM_Object" match="BOUNDARY">
        <gml:LinearRing>
                <xsl:apply-templates mode="GM_Object"  select="POLYLINE"/>
        </gml:LinearRing>
    </xsl:template>

    <xsl:template mode="GM_Object" match="POLYLINE">
            <gml:posList ><xsl:apply-templates mode="GM_Object"/></gml:posList>
    </xsl:template>

    <xsl:template match="GM03Core.Core.GM_Point_" mode="GM_Object">
        <gml:Point gml:id="{util:randomId()}">
            <gml:coordinates>
                <xsl:apply-templates mode="GM_Object" select="value"/>
            </gml:coordinates>
        </gml:Point>
    </xsl:template>

    <xsl:template mode="GM_Object" match="COORD"><xsl:value-of select="C1"/><xsl:text> </xsl:text><xsl:value-of select="C2"/><xsl:text> </xsl:text></xsl:template>

    <!-- ================================================================================= -->
    <xsl:template mode="BboxToPolygon" match="GM03Core.Core.EX_GeographicBoundingBox">

        <xsl:variable name="north" select="northBoundLatitude"/>
        <xsl:variable name="east" select="eastBoundLongitude"/>
        <xsl:variable name="south" select="southBoundLatitude"/>
        <xsl:variable name="west" select="westBoundLongitude"/>

        <xsl:variable name="ul">
            <xsl:value-of select="$west"/><xsl:text> </xsl:text><xsl:value-of select="$north"/>
        </xsl:variable>
        <xsl:variable name="ur">
            <xsl:value-of select="$east"/><xsl:text> </xsl:text><xsl:value-of select="$north"/>
        </xsl:variable>
        <xsl:variable name="lr">
            <xsl:value-of select="$east"/><xsl:text> </xsl:text><xsl:value-of select="$south"/>
        </xsl:variable>
        <xsl:variable name="ll">
            <xsl:value-of select="$west"/><xsl:text> </xsl:text><xsl:value-of select="$south"/>
        </xsl:variable>

        <gmd:geographicElement>
            <gmd:EX_BoundingPolygon>
                <gmd:extentTypeCode>
                    <gco:Boolean><xsl:value-of select="extentTypeCode"/></gco:Boolean>
                </gmd:extentTypeCode>
                <gmd:polygon>
                    <gml:MultiSurface gml:id="NN29">
                        <gml:surfaceMember>
                            <gml:Polygon gml:id="NN30">
                                <gml:exterior>
                                    <gml:LinearRing>
                                            <gml:posList><xsl:value-of select="$ul"/><xsl:text> </xsl:text><xsl:value-of select="$ur"/><xsl:text> </xsl:text><xsl:value-of select="$lr"/><xsl:text> </xsl:text><xsl:value-of select="$ll"/><xsl:text> </xsl:text><xsl:value-of select="$ul"/></gml:posList>
                                    </gml:LinearRing>
                                </gml:exterior>
                            </gml:Polygon>
                        </gml:surfaceMember>
                    </gml:MultiSurface>
                </gmd:polygon>
            </gmd:EX_BoundingPolygon>
        </gmd:geographicElement>
    </xsl:template>

    <!-- ================================================================================= -->

    <xsl:template mode="Extent" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">Extent</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
