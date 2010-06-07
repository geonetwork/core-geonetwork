<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns    ="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:gml="http://www.opengis.net/gml"
										xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Extent">

		<xsl:for-each select="exDesc">
			<description>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</description>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="geoEle">
			<geographicElement>
				<xsl:apply-templates select="." mode="GeoExtentTypes"/>
			</geographicElement>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="tempEle">
			<temporalElement>
				<xsl:apply-templates select="." mode="TempExtentTypes"/>
			</temporalElement>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="vertEle">
			<verticalElement>
				<EX_VerticalExtent>
					<xsl:apply-templates select="." mode="VertExtent"/>
				</EX_VerticalExtent>
			</verticalElement>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === GeoExtentTypes === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="GeoExtentTypes">

		<xsl:for-each select="BoundPoly">
			<EX_BoundingPolygon>
				<xsl:apply-templates select="." mode="BoundPoly"/>
			</EX_BoundingPolygon>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="GeoDesc">
			<EX_GeographicDescription>
				<xsl:apply-templates select="." mode="GeoDesc"/>
			</EX_GeographicDescription>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="BoundPoly">

		<xsl:for-each select="exTypeCode">
			<extentTypeCode>
				<gco:Boolean><xsl:value-of select="."/></gco:Boolean>
			</extentTypeCode>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="polygon">
			<polygon>
				<xsl:apply-templates select="." mode="GM_Polygon"/>
			</polygon>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="GeoDesc">

		<xsl:for-each select="exTypeCode">
			<extentTypeCode>
				<gco:Boolean><xsl:value-of select="."/></gco:Boolean>
			</extentTypeCode>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="geoId">
			<geographicIdentifier>
				<MD_Identifier>
					<xsl:apply-templates select="." mode="MdIdent"/>
				</MD_Identifier>
			</geographicIdentifier>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="MdIdent">

		<xsl:for-each select="identAuth">
			<authority>
				<CI_Citation>
					<xsl:apply-templates select="." mode="Citation"/>
				</CI_Citation>
			</authority>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<code>
			<gco:CharacterString><xsl:value-of select="identCode"/></gco:CharacterString>
		</code>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="GM_Polygon">

		<xsl:for-each select="GM_Polygon">
			<gml:Polygon>
				<gml:exterior>
					<gml:Ring>
						<gml:curveMember>
							<gml:LineString>
								<xsl:apply-templates select="." mode="PointType"/>
							</gml:LineString>
						</gml:curveMember>
					</gml:Ring>
				</gml:exterior>
			</gml:Polygon>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === TempExtentTypes === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="TempExtentTypes">

		<xsl:for-each select="TempExtent">
			<EX_TemporalExtent>
				<xsl:apply-templates select="." mode="TemporalExtent"/>
			</EX_TemporalExtent>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="SpatTempEx">
			<EX_SpatialTemporalExtent>
				<xsl:apply-templates select="." mode="SpatialTempExtent"/>
			</EX_SpatialTemporalExtent>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="TemporalExtent">

		<extent>
			<xsl:apply-templates select="exTemp/TM_GeometricPrimitive" mode="TM_Primitive"/>
		</extent>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="TM_Primitive">

		<xsl:for-each select="TM_Instant">
			<gml:TimeInstant>
				<gml:timePosition>
					<xsl:apply-templates select="tmPosition" mode="TM_PositionTypes"/>
				</gml:timePosition>
			</gml:TimeInstant>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="TM_Period">
			<gml:TimePeriod>
				<gml:beginPosition><xsl:value-of select="begin"/></gml:beginPosition>
				<gml:endPosition><xsl:value-of select="end"/></gml:endPosition>
			</gml:TimePeriod>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->
	<!-- ATTN: Rough conversion without data loss -->

	<xsl:template match="*" mode="TM_PositionTypes">

		<xsl:for-each select="TM_CalDate/calDate"><xsl:value-of select="."/></xsl:for-each>
		<xsl:for-each select="TM_ClockTime/clkTime"><xsl:value-of select="."/></xsl:for-each>

		<xsl:for-each select="TM_DateAndTime">
			<xsl:value-of select="calDate"/> <xsl:value-of select="clkTime"/>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="SpatialTempExtent">

		<xsl:apply-templates select="." mode="TemporalExtent"/>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="exSpat">
			<spatialExtent>
				<xsl:apply-templates select="." mode="GeoExtentTypes"/>
			</spatialExtent>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->
	<!-- === VertExtend === -->
	<!-- ============================================================================= -->

	<xsl:template match="*" mode="VertExtent">

		<minimumValue>
			<gco:Real><xsl:value-of select="vertMinVal"/></gco:Real>
		</minimumValue>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<maximumValue>
			<gco:Real><xsl:value-of select="vertMaxVal"/></gco:Real>
		</maximumValue>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<verticalCRS></verticalCRS>
	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
