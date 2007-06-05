<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:csw="http://www.opengis.net/cat/csw"
										xmlns:dc ="http://purl.org/dc/elements/1.1/"
										xmlns:dct="http://purl.org/dc/terms/"
										xmlns:ows="http://www.opengis.net/ows">

	<!-- ============================================================================= -->

	<xsl:template match="metadata">
		<csw:Record>

			<xsl:for-each select="idinfo/citation/citeinfo/title/@cat_id">
				<dc:identifier><xsl:value-of select="."/></dc:identifier>
			</xsl:for-each>

			<xsl:for-each select="idinfo/citation/citeinfo/title">
				<dc:title><xsl:value-of select="."/></dc:title>
			</xsl:for-each>

			<xsl:for-each select="idinfo/keywords">
				<xsl:for-each select="theme | place | stratum | temporal">
					<dc:subject><xsl:value-of select="."/></dc:subject>
				</xsl:for-each>
			</xsl:for-each>

			<xsl:for-each select="idinfo/descript/abstract">
				<dct:abstract><xsl:value-of select="."/></dct:abstract>
			</xsl:for-each>

			<xsl:for-each select="metainfo/metd">
				<dct:modified><xsl:value-of select="."/></dct:modified>
			</xsl:for-each>

			<xsl:for-each select="spdoinfo">
				<dc:type><xsl:value-of select="."/></dc:type>
			</xsl:for-each>

			<xsl:for-each select="distinfo/distrib/stdorder/digform/digtinfo">
				<dc:format><xsl:value-of select="."/></dc:format>
			</xsl:for-each>

			<!-- extra fields - - - - - - - - - - - - - - - - - - - - - -->

			<xsl:for-each select="idinfo/citation/citeinfo/origin">
				<dc:creator><xsl:value-of select="."/></dc:creator>
			</xsl:for-each>

			<xsl:for-each select="idinfo/citation/citeinfo/pubinfo/publish">
				<dc:publisher><xsl:value-of select="."/></dc:publisher>
			</xsl:for-each>

			<xsl:for-each select="idinfo/ptcontac/cntinfo/cntperp/cntper">
				<dc:contributor><xsl:value-of select="."/></dc:contributor>
			</xsl:for-each>

			<xsl:for-each select="idinfo/useconst | idinfo/accconst">
				<dc:rights><xsl:value-of select="."/></dc:rights>
			</xsl:for-each>

			<!-- bounding box -->

			<xsl:for-each select="idinfo/spdom/bounding">	
				<ows:BoundingBox crs="{spref/horizsys/planar/planci/coordrep}">
					<ows:LowerCorner>
						<xsl:value-of select="eastbc"/> <xsl:value-of select="southbc"/>
					</ows:LowerCorner>
	
					<ows:UpperCorner>
						<xsl:value-of select="westbc"/> <xsl:value-of select="northbc"/>
					</ows:UpperCorner>
				</ows:BoundingBox>
			</xsl:for-each>

		</csw:Record>
	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
