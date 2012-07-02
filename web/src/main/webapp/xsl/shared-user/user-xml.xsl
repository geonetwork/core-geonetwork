<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:che="http://www.geocat.ch/2008/che"
	xmlns:gco="http://www.isotc211.org/2005/gco"
	 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

	<xsl:output method="xml" indent="yes"/>

    <xsl:include href="../iso-internal-multilingual-conversion.xsl"/>
    <xsl:include href="../iso-internal-multilingual-conversion-url.xsl"/>

	<!-- Return an iso19139 representation of a contact
	stored in the metadata catalogue.
	-->
	<xsl:template match="/">
     <xsl:apply-templates mode="iso19139.che" select="root/response/record"/>
<!--
        See comment by the commented out iso19139 for why this is commented out.
<xsl:choose>
			<xsl:when test="/root/request/schema='iso19139.che'">
				<xsl:apply-templates mode="iso19139.che" select="root/response/record"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="iso19139" select="root/response/record"/>
			</xsl:otherwise>
		</xsl:choose>-->
	</xsl:template>

 <xsl:template match="record" mode="iso19139.che">
		<che:CHE_CI_ResponsibleParty gco:isoType="gmd:CI_ResponsibleParty">
			
            <xsl:if test="normalize-space(organisation)!=''">
				<gmd:organisationName xsi:type="gmd:PT_FreeText_PropertyType">
	                <xsl:call-template name="composeTranslations">
	                    <xsl:with-param name="elem" select="organisation"/>
	                </xsl:call-template>
				</gmd:organisationName>
			</xsl:if>
            <xsl:if test="normalize-space(positionname)!=''">
				<gmd:positionName xsi:type="gmd:PT_FreeText_PropertyType">
	                <xsl:call-template name="composeTranslations">
	                    <xsl:with-param name="elem" select="positionname"/>
	                </xsl:call-template>
				</gmd:positionName>
			</xsl:if>
			<gmd:contactInfo>
				<gmd:CI_Contact>
					<xsl:if test="normalize-space(phone)!='' or normalize-space(phone1)!='' or normalize-space(phone2)!='' or
						normalize-space(facsimile)!='' or normalize-space(facsimile1)!='' or normalize-space(facsimile2)!='' or
						normalize-space(directnumber)!='' or normalize-space(mobile)!='' ">
						<gmd:phone>
							<che:CHE_CI_Telephone gco:isoType="gmd:CI_Telephone">
	                            <xsl:if test="normalize-space(phone)!=''">
		                            <gmd:voice>
		                                <gco:CharacterString>
		                                    <xsl:value-of select="normalize-space(phone)"/>
		                                </gco:CharacterString>
		                            </gmd:voice>
	                            </xsl:if>
	                            <xsl:if test="phone1!=''">
	                                <gmd:voice>
	                                     <xsl:if test="normalize-space(phone1)=''">
	                                         <xsl:attribute name="gco:nilReason">missing</xsl:attribute>
	                                     </xsl:if>
	                                     <gco:CharacterString>
	                                        <xsl:value-of select="normalize-space(phone1)"/>
	                                     </gco:CharacterString>
	                                </gmd:voice>
	                            </xsl:if>
	                            <xsl:if test="phone2!=''">
	                                <gmd:voice>
	                                     <xsl:if test="normalize-space(phone2)=''">
	                                         <xsl:attribute name="gco:nilReason">missing</xsl:attribute>
	                                     </xsl:if>
	                                     <gco:CharacterString>
	                                        <xsl:value-of select="normalize-space(phone2)"/>
	                                     </gco:CharacterString>
	                                </gmd:voice>
	                            </xsl:if>
	                            <xsl:if test="normalize-space(facsimile)!=''">
		                            <gmd:facsimile>
		                                <gco:CharacterString>
		                                    <xsl:value-of select="normalize-space(facsimile)"/>
		                                </gco:CharacterString>
		                            </gmd:facsimile>
	                          	</xsl:if>
	                            <xsl:if test="facsimile1!=''">
	                                <gmd:facsimile>
	                                    <xsl:if test="normalize-space(facsimile1)=''">
	                                        <xsl:attribute name="gco:nilReason">missing</xsl:attribute>
	                                    </xsl:if>
	                                    <gco:CharacterString>
	                                        <xsl:value-of select="normalize-space(facsimile1)"/>
	                                    </gco:CharacterString>
	                                </gmd:facsimile>
	                            </xsl:if>
	                            <xsl:if test="facsimile2!=''">
	                                <gmd:facsimile>
	                                    <xsl:if test="normalize-space(facsimile2)=''">
	                                        <xsl:attribute name="gco:nilReason">missing</xsl:attribute>
	                                    </xsl:if>
	                                    <gco:CharacterString>
	                                        <xsl:value-of select="normalize-space(facsimile2)"/>
	                                    </gco:CharacterString>
	                                </gmd:facsimile>
	                            </xsl:if>
								<che:directNumber>
									<xsl:if test="directnumber=''">
										<xsl:attribute name="gco:nilReason"
											>missing</xsl:attribute>
									</xsl:if>
									<gco:CharacterString>
										<xsl:value-of select="directnumber"/>
									</gco:CharacterString>
								</che:directNumber>
								<che:mobile>
									<xsl:if test="mobile=''">
										<xsl:attribute name="gco:nilReason"
											>missing</xsl:attribute>
									</xsl:if>
									<gco:CharacterString>
										<xsl:value-of select="mobile"/>
									</gco:CharacterString>
								</che:mobile>
							</che:CHE_CI_Telephone>
						</gmd:phone>
					</xsl:if>
					<xsl:if test="normalize-space(city)!='' or normalize-space(state)!='' or normalize-space(zip)!='' or normalize-space(country)!='' or 
								  normalize-space(email)!='' or normalize-space(email1)!='' or normalize-space(email2)!='' or normalize-space(streetname)!='' or 
								  normalize-space(streetnumber)!='' or normalize-space(address)!='' or normalize-space(postbox)!=''">
						<gmd:address>
							<che:CHE_CI_Address gco:isoType="gmd:CI_Address">
								<xsl:if test="normalize-space(city)!=''">
									<gmd:city>
										<gco:CharacterString>
											<xsl:value-of select="city"/>
										</gco:CharacterString>
									</gmd:city>
								</xsl:if>
								<xsl:if test="normalize-space(state)!=''">
									<gmd:administrativeArea>
										<gco:CharacterString>
											<xsl:value-of select="state"/>
										</gco:CharacterString>
									</gmd:administrativeArea>
								</xsl:if>
								<xsl:if test="normalize-space(zip)!=''">
									<gmd:postalCode>
										<gco:CharacterString>
											<xsl:value-of select="zip"/>
										</gco:CharacterString>
									</gmd:postalCode>
								</xsl:if>
	                            <xsl:if test="normalize-space(country)!=''">
									<gmd:country xsi:type="gmd:PT_FreeText_PropertyType">
		                                <gco:CharacterString>
		                                    <xsl:value-of select="country"/>
		                                </gco:CharacterString>
									</gmd:country>
								</xsl:if>
								<xsl:if test="normalize-space(email)!=''">
									<gmd:electronicMailAddress>
										<gco:CharacterString>
											<xsl:value-of select="normalize-space(email)"/>
										</gco:CharacterString>
									</gmd:electronicMailAddress>
								</xsl:if>
	                            <xsl:if test="normalize-space(email1)!=''">
	                                <gmd:electronicMailAddress>
	                                    <gco:CharacterString>
	                                        <xsl:value-of select="normalize-space(email1)"/>
	                                    </gco:CharacterString>
	                                </gmd:electronicMailAddress>
	                            </xsl:if>
	                            <xsl:if test="normalize-space(email2)!=''">
	                                <gmd:electronicMailAddress>
	                                    <gco:CharacterString>
	                                        <xsl:value-of select="normalize-space(email2)"/>
	                                    </gco:CharacterString>
	                                </gmd:electronicMailAddress>
	                            </xsl:if>
								<xsl:if test="normalize-space(streetname)!=''">
									<che:streetName>
										<gco:CharacterString>
											<xsl:value-of select="streetname"/>
										</gco:CharacterString>
									</che:streetName>
								</xsl:if>
								<xsl:if test="normalize-space(streetnumber)!=''">
									<che:streetNumber>
										<gco:CharacterString>
											<xsl:value-of select="streetnumber"/>
										</gco:CharacterString>
									</che:streetNumber>
								</xsl:if>
								<xsl:if test="normalize-space(address)!=''">
									<che:addressLine>
										<gco:CharacterString>
											<xsl:value-of select="address"/>
										</gco:CharacterString>
									</che:addressLine>
								</xsl:if>
								<xsl:if test="normalize-space(postbox)!=''">
									<che:postBox>
										<gco:CharacterString>
											<xsl:value-of select="postbox"/>
										</gco:CharacterString>
									</che:postBox>
								</xsl:if>
							</che:CHE_CI_Address>
						</gmd:address>
						</xsl:if>
						<xsl:if test="onlineresource[*[normalize-space(text())!='']] or normalize-space(onlinedescription)!='' or
									  normalize-space(onlinename)!=''">
							<gmd:onlineResource>
								<gmd:CI_OnlineResource>
								    <xsl:for-each select="onlineresource[*[normalize-space(text())!='']]">
										<gmd:linkage xsi:type="che:PT_FreeURL_PropertyType">
		                                <xsl:call-template name="composeURLTranslations">
		                                    <xsl:with-param name="elem" select="."/>
		                                </xsl:call-template>
										</gmd:linkage>
									</xsl:for-each>
									<gmd:protocol>
										<gco:CharacterString>text/html</gco:CharacterString>
									</gmd:protocol>
	                                <gmd:name xsi:type="gmd:PT_FreeText_PropertyType">
		                                <xsl:call-template name="composeTranslations">
		                                    <xsl:with-param name="elem" select="onlinename"/>
		                                </xsl:call-template>
	                                </gmd:name>
	                                <gmd:description xsi:type="gmd:PT_FreeText_PropertyType">
		                                <xsl:call-template name="composeTranslations">
		                                    <xsl:with-param name="elem" select="onlinedescription"/>
		                                </xsl:call-template>
	                                </gmd:description>
								</gmd:CI_OnlineResource>
							</gmd:onlineResource>
						</xsl:if>
                        <xsl:if test="normalize-space(hoursofservice)!=''">
							<gmd:hoursOfService>
								<gco:CharacterString>
									<xsl:value-of select="hoursofservice"/>
								</gco:CharacterString>
							</gmd:hoursOfService>
						</xsl:if>
                        <xsl:if test="normalize-space(contactinstructions)!=''">
							<gmd:contactInstructions xsi:type="gmd:PT_FreeText_PropertyType">
	                            <xsl:call-template name="composeTranslations">
	                                <xsl:with-param name="elem" select="contactinstructions"/>
	                            </xsl:call-template>
							</gmd:contactInstructions>
						</xsl:if>
				</gmd:CI_Contact>
			</gmd:contactInfo>
			<gmd:role>
				<gmd:CI_RoleCode codeListValue="{/root/request/role}" codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode"/>
			</gmd:role>
			<xsl:if test="normalize-space(name)!=''">
				<che:individualFirstName>
					<gco:CharacterString>
						<xsl:value-of select="name"/>
					</gco:CharacterString>
				</che:individualFirstName>
			</xsl:if>
			<xsl:if test="normalize-space(surname)!=''">
				<che:individualLastName>
					<gco:CharacterString>
						<xsl:value-of select="surname"/>
					</gco:CharacterString>
				</che:individualLastName>
			</xsl:if>
			<xsl:if test="normalize-space(orgacronym)!=''">
				<che:organisationAcronym xsi:type="gmd:PT_FreeText_PropertyType">
	                <xsl:call-template name="composeTranslations">
	                    <xsl:with-param name="elem" select="orgacronym"/>
	                </xsl:call-template>
	            </che:organisationAcronym>
            </xsl:if>
            <xsl:if test="normalize-space(parentinfo) != ''">
				<che:parentResponsibleParty xmlns:xlink="http://www.w3.org/1999/xlink"
					    xlink:href="local://xml.user.get?id={parentinfo}&amp;schema=iso19139.che&amp;role=distributor"
					    xlink:show="embed">
					  <xsl:if test="string(../parentValidated)='n'">
					    <xsl:attribute name="xlink:role">http://www.geonetwork.org/non_valid_obj</xsl:attribute>
					  </xsl:if>
                </che:parentResponsibleParty>
            </xsl:if>
		</che:CHE_CI_ResponsibleParty>
	</xsl:template>
</xsl:stylesheet>
