<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns    ="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
										xmlns:xlink="http://www.w3.org/1999/xlink"
										xmlns:wfs="http://www.opengis.net/wfs"
										xmlns:ows="http://www.opengis.net/ows"
										xmlns:wcs="http://www.opengis.net/wcs"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										extension-element-prefixes="wcs ows wfs">

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="RespParty">

		<xsl:for-each select="ContactPersonPrimary/ContactPerson|wcs:individualName|ows:ProviderName">
			<individualName>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</individualName>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="ContactPersonPrimary/ContactOrganization|wcs:organisationName">
			<organisationName>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</organisationName>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="ContactPosition|wcs:positionName">
			<positionName>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</positionName>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<contactInfo>
			<CI_Contact>
				<xsl:apply-templates select="." mode="Contact"/>
			</CI_Contact>
		</contactInfo>
		
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<role>
			<CI_RoleCode codeList="./resources/codeList.xml#CI_RoleCode" codeListValue="pointOfContact" />
		</role>

	</xsl:template>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Contact">

		<phone>
			<CI_Telephone>
				<xsl:for-each select="ContactVoiceTelephone">
					<voice>
						<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
					</voice>
				</xsl:for-each>
	
				<xsl:for-each select="ContactFacsimileTelephone">
					<facsimile>
						<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
					</facsimile>
				</xsl:for-each>
			</CI_Telephone>
		</phone>
	
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="ContactAddress|wcs:contactInfo">
			<address>
				<CI_Address>
					<xsl:apply-templates select="." mode="Address"/>
				</CI_Address>
			</address>
		</xsl:for-each>

		<!--cntOnLineRes-->
		<!--cntHours -->
		<!--cntInstr -->
		<onlineResource>
			<CI_OnlineResource>
				<linkage>
					<URL><xsl:value-of select="//Service/OnlineResource/@xlink:href"/></URL>
				</linkage>
			</CI_OnlineResource>
		</onlineResource>
	</xsl:template>


	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Address">

		<xsl:for-each select="Address">
			<deliveryPoint>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</deliveryPoint>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="City|wcs:address/wcs:city">
			<city>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</city>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="StateOrProvince">
			<administrativeArea>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</administrativeArea>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="PostCode">
			<postalCode>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</postalCode>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="Country|wcs:address/wcs:country">
			<country>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</country>
		</xsl:for-each>

		<!-- TODO - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="eMailAdd|wcs:address/wcs:electronicMailAddress">
			<electronicMailAddress>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</electronicMailAddress>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
