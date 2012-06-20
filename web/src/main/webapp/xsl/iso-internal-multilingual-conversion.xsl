<?xml version="1.0" encoding="UTF-8"?>

<!-- iso-internal-multilingual-conversion-url.xsl was copied from this file 
	so fix bugs there too -->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:che="http://www.geocat.ch/2008/che" xmlns:gml="http://www.opengis.net/gml"
	xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

	<!-- ============================================================================= -->

	<xsl:output method="xml" />

	<xsl:param name="metadataLang" select="'EN'" />

	<!-- Converting the simple format to iso -->
	<xsl:template match="/description">
		<gmd:description xsi:type="gmd:PT_FreeText_PropertyType">
			<xsl:call-template name="composeTranslations">
				<xsl:with-param name="elem" select="." />
			</xsl:call-template>
		</gmd:description>
	</xsl:template>

	<xsl:template name="composeTranslations">
		<xsl:param name="elem" />
		<xsl:choose>
			<xsl:when test="count($elem/child::node()[normalize-space(text())!=''])>0 and normalize-space($elem/text())=''" />
			<xsl:otherwise>
				<gmd:PT_FreeText>
					<gmd:textGroup>
						<gmd:LocalisedCharacterString>
							<xsl:attribute name="locale">
                                <xsl:value-of select="concat('#', $metadataLang)" />
                            </xsl:attribute>
							<xsl:value-of select="normalize-space($elem/text())" />
						</gmd:LocalisedCharacterString>
					</gmd:textGroup>
				</gmd:PT_FreeText>
			</xsl:otherwise>
		</xsl:choose>

		<xsl:choose>
			<xsl:when test="count($elem/child::node()[normalize-space(text())!=''])>0">
				<gmd:PT_FreeText>
					<xsl:apply-templates mode="convert-inner-to-iso"
						select="$elem/*" />
				</gmd:PT_FreeText>
			</xsl:when>
			<xsl:otherwise>

				<xsl:apply-templates mode="convert-inner-to-iso"
					select="$elem/*" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template mode="convert-inner-to-iso" match="text()" />

	<xsl:template mode="convert-inner-to-iso" match="node()"
		priority="1">
		<xsl:variable name="currentName" select="name(.)"/>
		<xsl:variable name="hasPreviousSiblingWithSameName" select="count(preceding-sibling::node()[name() = $currentName]) > 0"/>
		<xsl:if test="normalize-space(string(.))!='' and not($hasPreviousSiblingWithSameName)">
			<gmd:textGroup>
				<gmd:LocalisedCharacterString>
					<xsl:attribute name="locale">
                        <xsl:value-of select="concat('#', normalize-space(name(.)))" />
                    </xsl:attribute>
					<xsl:value-of select="normalize-space(.)" />
				</gmd:LocalisedCharacterString>
			</gmd:textGroup>
		</xsl:if>
	</xsl:template>


	<!-- Converting the iso format to the simple format -->

	<xsl:template match="/root">
		<result>
			<xsl:apply-templates mode="convert-iso-to-inner" />
		</result>
	</xsl:template>

	<xsl:template mode="convert-iso-to-inner"
		match="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
		<xsl:variable name="code" select="substring(string(@locale),2)" />
		<xsl:if
			test="string(text())!='' and ($metadataLang!=$code or string(../../../gco:CharacterString)='')">
			<xsl:element name="{$code}">
				<xsl:value-of select="." />
			</xsl:element>
		</xsl:if>
	</xsl:template>

	<xsl:template mode="convert-iso-to-inner" match="gco:CharacterString">
		<xsl:element name="{$metadataLang}">
			<xsl:value-of select="." />
		</xsl:element>
	</xsl:template>

</xsl:stylesheet>