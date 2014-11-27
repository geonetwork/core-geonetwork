<?xml version="1.0" encoding="UTF-8"?>
<!--  Mapping between netcdfDatasetInfo and ISO19139 keywords -->
<xsl:stylesheet version="2.0" 
										xmlns:gmd="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:gts="http://www.isotc211.org/2005/gts"
										xmlns:gml="http://www.opengis.net/gml"
										xmlns:srv="http://www.isotc211.org/2005/srv"
										xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
										xmlns:util="java:java.util.UUID"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:xlink="http://www.w3.org/1999/xlink"
										exclude-result-prefixes="util">

	<!-- ==================================================================== -->
	
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
	
	<!-- ==================================================================== -->

	<xsl:template match="*">
	
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
		
		<gmd:descriptiveKeywords>
			<gmd:MD_Keywords>

			<xsl:for-each select="variable">
				<gmd:keyword>
					<gco:CharacterString><xsl:value-of select="concat(@name,' ',@long_name,' ',@decl)"/></gco:CharacterString>
				</gmd:keyword>
			</xsl:for-each>

		  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
		
			<gmd:type>
				<gmd:MD_KeywordTypeCode codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_KeywordTypeCode" codeListValue="theme"/>
			</gmd:type>
		
		  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

			<gmd:thesaurusName>
				<gmd:CI_Citation>
					<gmd:title>
						<gco:CharacterString><xsl:value-of select="concat(convention/@name,' (see http://www.unidata.ucar.edu/software/netcdf/conventions.html for more info on some conventions and adding conventions to the Unidata netcdf-4.0 Java library)')"/></gco:CharacterString>
					</gmd:title>
					<gmd:alternateTitle>
            <gco:CharacterString>Data Parameters/Variables following the <xsl:value-of select="convention/@name"/> conventions</gco:CharacterString>
        	</gmd:alternateTitle>
				</gmd:CI_Citation>
			</gmd:thesaurusName>
		
			</gmd:MD_Keywords>
		</gmd:descriptiveKeywords>

	</xsl:template>
	
	<!-- ============================================================================= -->

</xsl:stylesheet>
