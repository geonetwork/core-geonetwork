<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 	xmlns    ="http://www.isotc211.org/2005/gmd"
								xmlns:wmc="http://www.opengis.net/context"
								xmlns:wmc11="http://www.opengeospatial.net/context"                              								
								xmlns:gco="http://www.isotc211.org/2005/gco"
								xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
								xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="RespParty">

		<xsl:for-each select="wmc:ContactPersonPrimary/wmc:ContactPerson
			|wmc11:ContactPersonPrimary/wmc11:ContactPerson">
			<individualName>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</individualName>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="wmc:ContactPersonPrimary/wmc:ContactOrganization
			|wmc11:ContactPersonPrimary/wmc11:ContactOrganization">
			<organisationName>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</organisationName>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="wmc:ContactPosition|wmc11:ContactPosition">
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
				<xsl:for-each select="wmc:ContactVoiceTelephone|wmc11:ContactVoiceTelephone">
					<voice>
						<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
					</voice>
				</xsl:for-each>
	
				<xsl:for-each select="wmc:ContactFacsimileTelephone|wmc11:ContactFacsimileTelephone">
					<facsimile>
						<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
					</facsimile>
				</xsl:for-each>
			</CI_Telephone>
		</phone>
	
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="wmc:ContactAddress|wmc11:ContactAddress">
			<address>
				<CI_Address>
					<xsl:apply-templates select="." mode="Address"/>
				</CI_Address>
			</address>
		</xsl:for-each>

		<!--cntOnLineRes-->
		<!--cntHours -->
		<!--cntInstr -->

	</xsl:template>


	<!-- ============================================================================= -->

	<xsl:template match="*" mode="Address">

		<xsl:for-each select="wmc:Address|wmc11:Address">
			<deliveryPoint>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</deliveryPoint>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="wmc:City|wmc11:City">
			<city>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</city>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="wmc:StateOrProvince|wmc11:StateOrProvince">
			<administrativeArea>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</administrativeArea>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="wmc:PostCode|wmc11:PostCode">
			<postalCode>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</postalCode>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="wmc:Country|wmc11:Country">
			<country>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</country>
		</xsl:for-each>

		<!-- TODO - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:for-each select="wmc:eMailAdd|wmc11:eMailAdd">
			<electronicMailAddress>
				<gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
			</electronicMailAddress>
		</xsl:for-each>

	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
