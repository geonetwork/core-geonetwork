<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gco="http://www.isotc211.org/2005/gco"
   xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:xslt="http://xml.apache.org/xslt">
	<xsl:output method="xml" indent="yes" encoding="UTF-8" xslt:indent-amount="4"/>


	<xsl:template match="/">
		<xsl:for-each select="/Organisations/Organisation">
			<xsl:variable name="output" select="concat('data/SDN-EDMO-',n_code/text(),'.xml')"/>
			<xsl:result-document href="{$output}" method="xml">

				<gmd:CI_ResponsibleParty>
					<xsl:attribute name="uuid" select="concat('https://edmo.seadatanet.org/report/',n_code/text())"/>

					<xsl:element name="gmd:organisationName">
						<xsl:element name="gco:CharacterString">
							<xsl:value-of select="name/text()"/>
						</xsl:element>
					</xsl:element>
					<xsl:element name="gmd:contactInfo">
						<xsl:element name="gmd:CI_Contact">
							<xsl:element name="gmd:phone">
								<xsl:element name="gmd:CI_Telephone">
									<xsl:element name="gmd:voice">
										<xsl:element name="gco:CharacterString">
											<xsl:value-of select="phone/text()"/>
										</xsl:element>
									</xsl:element>
									<xsl:element name="gmd:facsimile">
										<xsl:element name="gco:CharacterString">
											<xsl:value-of select="fax/text()"/>
										</xsl:element>
									</xsl:element>
								</xsl:element>
							</xsl:element>
							<xsl:element name="gmd:address">
								<xsl:element name="gmd:CI_Address">
									<xsl:element name="gmd:deliveryPoint">
										<xsl:element name="gco:CharacterString">
											<xsl:value-of select="address/text()"/>
										</xsl:element>
									</xsl:element>
									<xsl:element name="gmd:city">
										<xsl:element name="gco:CharacterString">
											<xsl:value-of select="city/text()"/>
										</xsl:element>
									</xsl:element>
									<xsl:element name="gmd:administrativeArea">
										<xsl:element name="gco:CharacterString">
										</xsl:element>
									</xsl:element>
									<xsl:element name="gmd:postalCode">
										<xsl:element name="gco:CharacterString">
											<xsl:value-of select="zipcode/text()"/>
										</xsl:element>
									</xsl:element>
									<xsl:element name="gmd:country">
										<xsl:element name="gco:CharacterString">
											<xsl:value-of select="c_country/text()"/>
										</xsl:element>
									</xsl:element>
									<xsl:element name="gmd:electronicMailAddress">
										<xsl:element name="gco:CharacterString">
											<xsl:value-of select="email/text()"/>
										</xsl:element>
									</xsl:element>
								</xsl:element>
							</xsl:element>

							<xsl:element name="gmd:onlineResource">
								<xsl:element name="gmd:CI_OnlineResource">
									<xsl:element name="gmd:linkage">
										<xsl:element name="gmd:URL">
											<xsl:value-of select="website/text()"/>
										</xsl:element>
									</xsl:element>
									<xsl:element name="gmd:name">
										<xsl:element name="gco:CharacterString">
											<xsl:text>web site</xsl:text>
										</xsl:element>
									</xsl:element>
									<xsl:element name="gmd:function">
										<xsl:element name="gmd:CI_OnLineFunctionCode">
											<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#CI_OnLineFunctionCode</xsl:attribute>
											<xsl:attribute name="codeListValue">information</xsl:attribute>
										</xsl:element>
									</xsl:element>
								</xsl:element>
							</xsl:element>

						</xsl:element>
					</xsl:element>


					<xsl:element name="gmd:role">
						<xsl:element name="gmd:CI_RoleCode">
							<xsl:attribute name="codeList">http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#CI_RoleCode</xsl:attribute>
							<xsl:attribute name="codeListValue">pointOfContact</xsl:attribute>
						</xsl:element>
					</xsl:element>
				</gmd:CI_ResponsibleParty>

			</xsl:result-document>
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>