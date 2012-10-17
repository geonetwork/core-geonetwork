<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:xlink="http://www.w3.org/1999/xlink">

	<xsl:include href="../process/process-utility.xsl"/>
	

	<!-- A set of templates use to convert thesaurus concept to ISO19139 fragments. -->

	<xsl:template name="to-iso19139-keyword-with-anchor">
		<xsl:call-template name="to-iso19139-keyword">
			<xsl:with-param name="withAnchor" select="true()"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="to-iso19139-keyword">
		<xsl:param name="withAnchor" select="false()"/>
		<xsl:param name="withXlink" select="false()"/>

		<gmd:MD_Keywords>
			<xsl:variable name="currentThesaurus" select="thesaurus/key"/>
			<!-- Loop on all keyword from the same thesaurus -->
			<xsl:for-each select="//keyword[thesaurus/key = $currentThesaurus]">
				<gmd:keyword>
					<xsl:choose>
						<xsl:when test="$withAnchor">
							<!-- TODO multilingual Anchor ? -->
							<gmx:Anchor
								xlink:href="{$serviceUrl}/xml.keyword.get?thesaurus={thesaurus/key}&amp;amp;id={uri}">
								<xsl:value-of select="value"/>
							</gmx:Anchor>
						</xsl:when>
						<xsl:otherwise>
							<gmx:CharacterString>
								<xsl:value-of select="value"/>
							</gmx:CharacterString>
							<!-- TODO multilingual keywords
							add a lang parameter to only get some of them -->
							<xsl:for-each select="values"> </xsl:for-each>
						</xsl:otherwise>
					</xsl:choose>

				</gmd:keyword>
			</xsl:for-each>

			<!-- TODO : add thesaurus theme -->
			<gmd:type>
				<gmd:MD_KeywordTypeCode
					codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode"
					codeListValue="{thesaurus/type}"/>
			</gmd:type>
			<xsl:if test="not(/root/request/keywordOnly)">
				<gmd:thesaurusName>
					<gmd:CI_Citation>
						<gmd:title>
							<gco:CharacterString>
								<xsl:value-of select="thesaurus/title"/>
							</gco:CharacterString>
						</gmd:title>
						<gmd:date>
							<gmd:CI_Date>
								<gmd:date>
									<gco:Date>
										<xsl:value-of select="thesaurus/date"/>
									</gco:Date>
								</gmd:date>
								<gmd:dateType>
									<gmd:CI_DateTypeCode
										codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#CI_DateTypeCode"
										codeListValue="publication"/>
								</gmd:dateType>
							</gmd:CI_Date>
						</gmd:date>
						<gmd:identifier>
							<gmd:MD_Identifier>
								<gmd:code>
									<gmx:Anchor xlink:href="{thesaurus/url}"
											>geonetwork.thesaurus.<xsl:value-of
											select="thesaurus/key"/></gmx:Anchor>
								</gmd:code>
							</gmd:MD_Identifier>
						</gmd:identifier>
					</gmd:CI_Citation>
				</gmd:thesaurusName>
			</xsl:if>
		</gmd:MD_Keywords>
	</xsl:template>
	
	
	
	
	
	
	<!-- Extent -->
	<xsl:template name="to-iso19139-extent">
		<xsl:param name="isService" select="false()"/>
		
		
		<xsl:variable name="currentThesaurus" select="thesaurus/key"/>
		<!-- Loop on all keyword from the same thesaurus -->
		<xsl:for-each select="//keyword[thesaurus/key = $currentThesaurus]">
			<xsl:choose>
				<xsl:when test="$isService">
					<srv:extent>
						<xsl:copy-of select="geonet:make-iso-extent(geo/west, geo/south, geo/east, geo/north, value)"/>
					</srv:extent>
				</xsl:when>
				<xsl:otherwise>
					<gmd:extent>
						<xsl:copy-of select="geonet:make-iso-extent(geo/west, geo/south, geo/east, geo/north, value)"/>
					</gmd:extent>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
