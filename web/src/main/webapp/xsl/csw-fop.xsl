<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xalan"
	exclude-result-prefixes="xalan"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:dct="http://purl.org/dc/terms/"
    xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:fo="http://www.w3.org/1999/XSL/Format">


	<!--
		metadata result to fop
	-->
	<xsl:template name="fo">
		<xsl:param name="res" />
		<xsl:param name="server" />
		<xsl:param name="gui" />
		<xsl:param name="remote" />

		<xsl:for-each select="$res/*">
            <xsl:if test="geonet:info/id != ''">
			<fo:table-row border-bottom="1pt solid gray">
				<fo:table-cell padding-bottom="10pt" padding-top="10pt">

				    <xsl:variable name="source" select="string(geonet:info/source)"/>
					<xsl:variable name="groupLogoUuid" select="string(geonet:info/groupLogoUuid)"/>
					<xsl:variable name="groupWebsite" select="string(geonet:info/groupWebsite)"/>




                    <fo:block font-weight="bold" font-size="14pt"
						border-top="2pt solid black">
                    	<xsl:choose>
                    		<xsl:when test="$groupWebsite != '' and $groupLogoUuid != ''">
                    			<fo:external-graphic content-width="35pt">
                    				<xsl:attribute name="src">
                    					url('<xsl:value-of
                    						select="concat('http://', $server/host,':', $server/port, $gui/url, '/images/logos/', $groupLogoUuid , '.png')" />')"
                    				</xsl:attribute>
                    			</fo:external-graphic>
                    		</xsl:when>
                    		<xsl:when test="$groupLogoUuid != ''">
                    			<fo:external-graphic content-width="35pt">
                    				<xsl:attribute name="src">
                    					url('<xsl:value-of
                    						select="concat('http://', $server/host,':', $server/port, $gui/url, '/images/logos/', $groupLogoUuid , '.png')" />')"
                    				</xsl:attribute>
                    			</fo:external-graphic>
                    		</xsl:when>
                    		<xsl:when test="/root/gui/sources/record[string(siteid)=$source]">
                    			<fo:external-graphic content-width="35pt">
                    				<xsl:attribute name="src">
                    					url('<xsl:value-of
                    						select="concat('http://', $server/host,':', $server/port, $gui/url, '/images/logos/', $source , '.png')" />')"
                    				</xsl:attribute>
                    			</fo:external-graphic>
                    		</xsl:when>
                    		<xsl:otherwise>
                    			<fo:external-graphic content-width="35pt">
                    				<xsl:attribute name="src">
                    					url('<xsl:value-of
                    						select="concat('http://', $server/host,':', $server/port, $gui/url, '/images/logos/', $source , '.gif')" />')"
                    				</xsl:attribute>
                    			</fo:external-graphic>
                    		</xsl:otherwise>
                    	</xsl:choose>

                    	<xsl:value-of select="dc:title" />
					</fo:block>
					<fo:block text-align="left" padding-bottom="3pt">
						<fo:inline font-weight="bold"><xsl:value-of select="$gui/strings/abstract" /></fo:inline>
						:
						<xsl:value-of select="dct:abstract" />
					</fo:block>
					<!-- keywords -->
					<xsl:if test="dc:subject">
						<fo:block text-align="left" padding-bottom="3pt">
							<fo:inline font-weight="bold"><xsl:value-of select="$gui/strings/keywords" /></fo:inline>
							:
							<xsl:for-each select="dc:subject">
								<xsl:if test="position() &gt; 1">
									,
								</xsl:if>
								<xsl:value-of select="." />
							</xsl:for-each>
						</fo:block>
					</xsl:if>

					<fo:block text-align="left">
						|<fo:basic-link text-decoration="underline"	color="blue">
							<xsl:attribute
								name="external-destination">
                                   url('<xsl:value-of
									select="concat('http://', $server/host,':', $server/port, $gui/locService,'/metadata.show?id=', geonet:info/id, '&amp;currTab=simple')" />')
                               </xsl:attribute>
							<xsl:value-of select="$gui/strings/showPdf" />
						</fo:basic-link>|

					</fo:block>
				</fo:table-cell>
			</fo:table-row>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>


	<!--
		main pdf banner
	-->
	<xsl:template name="banner">
		<fo:table table-layout="fixed" width="100%">
			<fo:table-column
				column-width="proportional-column-width(1)" />
			<fo:table-body>
				<fo:table-row>
					<fo:table-cell display-align="left">
						<fo:block text-align="left" background-image="url('')">
							<!-- FIXME: put the geocat header background as block bg -->
							<fo:external-graphic content-width="515pt">
								<xsl:attribute name="src">
                                url('<xsl:value-of
                                	select="concat('http://', //server/host,':', //server/port, /root/gui/url,'/images/Banner_print.png')" />')"
                                    </xsl:attribute>
							</fo:external-graphic>
					   </fo:block>
<!--
					</fo:table-cell><fo:table-cell display-align="right" background-color="#064377">
					   <fo:block text-align="right">
                    		<fo:external-graphic>
								<xsl:attribute name="src">
                                url('<xsl:value-of
                                	select="concat('http://', //server/host,':', //server/port, /root/gui/url,'/images/header-logo.jpg')" />')"
                                    </xsl:attribute>
							</fo:external-graphic>
						</fo:block>
-->
					</fo:table-cell>
				</fo:table-row>
			</fo:table-body>
		</fo:table>
	</xsl:template>
</xsl:stylesheet>
