<?xml version="1.0" encoding="UTF-8"?>

<!-- ====================================================== -->
<!-- A translator for DIF (GCMD) to ISO 19115 -->
<!-- Written by Dave Connell (Australian Antarctic Data Centre) and Andy Townsend (Australian Antarctic Data Centre) -->
<!-- Released on the 5th of June, 2008.  Last updated on the 6th of March, 2009 -->
<!-- Version 2.1 -->
<!-- ====================================================== -->

<!-- Trap for young players - name space definitions must match those served out of geoserver -->
<xsl:stylesheet version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gco="http://www.isotc211.org/2005/gco"	
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gml="http://www.opengis.net/gml"
	xmlns:dif="http://gcmd.gsfc.nasa.gov/Aboutus/xml/dif/"
	xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
	xmlns:util="java:java.util.UUID"
	exclude-result-prefixes="dif fn util">

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" media-type="text/plain"/>
  <!--<xsl:strip-space elements="*"/> -->

	<!-- MATCH ROOT DIF -->
	<xsl:template match="dif:DIF">

			<!-- PRINT Dataset HEADER MATERIAL -->
	<gmd:MD_Metadata>
		
		<gmd:fileIdentifier>
			<gco:CharacterString>
				<xsl:value-of select="dif:Entry_ID"/>
			</gco:CharacterString>
		</gmd:fileIdentifier>
	
		<gmd:language>
			<gco:CharacterString>eng</gco:CharacterString>
		</gmd:language>
		
		<gmd:characterSet>
			<gmd:MD_CharacterSetCode codeListValue="utf8" codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#MD_CharacterSetCode"/>
		</gmd:characterSet>
	
		<xsl:if test="dif:Parent_DIF!=''">
      <gmd:parentIdentifier>
        <gco:CharacterString>
          <xsl:value-of select="dif:Parent_DIF"/>
        </gco:CharacterString>
      </gmd:parentIdentifier>
		</xsl:if>
		
		<gmd:hierarchyLevel>
			<gmd:MD_ScopeCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#MD_ScopeCode" codeListValue="dataset"/>
		</gmd:hierarchyLevel>
	
		<xsl:for-each select="dif:Personnel">

			<xsl:choose>
				<xsl:when test="contains(.,'DIF AUTHOR')">
				
					<xsl:for-each select="dif:Role">
						<xsl:if test=".='DIF AUTHOR'">
							<gmd:contact>
								<gmd:CI_ResponsibleParty>
									<gmd:individualName>
										<gco:CharacterString>
											<xsl:value-of select="../dif:Last_Name"></xsl:value-of>, <xsl:value-of select="../dif:First_Name"></xsl:value-of>
										</gco:CharacterString>
									</gmd:individualName>

									<xsl:if test="../dif:Phone!='' or ../dif:Fax!='' or ../dif:Contact_Address/dif:Address!='' or ../dif:Contact_Address/dif:City!='' or ../dif:Contact_Address/dif:Province_or_State!='' or ../dif:Contact_Address/dif:Postal_Code!='' or ../dif:Contact_Address/dif:Country!='' or ../dif:Email!=''">
									
                    <gmd:contactInfo>
                      <gmd:CI_Contact>
                      
                        <xsl:if test="../dif:Phone!='' or ../dif:Fax!=''">
                          <gmd:phone>
                            <gmd:CI_Telephone>
                              <xsl:for-each select="../dif:Phone">
                                <gmd:voice>
                                  <gco:CharacterString>
                                      <xsl:value-of select="."/>
                                  </gco:CharacterString>
                                </gmd:voice>
                               </xsl:for-each>
                               <xsl:for-each select="../dif:Fax">
                                <gmd:facsimile>
                                  <gco:CharacterString>
                                      <xsl:value-of select="."/>
                                  </gco:CharacterString>
                                </gmd:facsimile>
                               </xsl:for-each>                       
                            </gmd:CI_Telephone>
                          </gmd:phone>
                        </xsl:if>
                        
                        <xsl:if test="../dif:Contact_Address/dif:Address!='' or ../dif:Contact_Address/dif:City!='' or ../dif:Contact_Address/dif:Province_or_State!='' or ../dif:Contact_Address/dif:Postal_Code!='' or ../dif:Contact_Address/dif:Country!='' or ../dif:Email!=''">
                          <gmd:address>
                            <gmd:CI_Address>
                              <xsl:for-each select="../dif:Contact_Address/dif:Address">
                                <gmd:deliveryPoint>
                                  <gco:CharacterString>
                                    <xsl:value-of select="."/>
                                  </gco:CharacterString>
                                </gmd:deliveryPoint>
                              </xsl:for-each>
                              <xsl:if test="../dif:Contact_Address/dif:City!=''">
                                <gmd:city>
                                  <gco:CharacterString>
                                    <xsl:value-of select="../dif:Contact_Address/dif:City"/>
                                  </gco:CharacterString>
                                </gmd:city>
                              </xsl:if>
                              <xsl:if test="../dif:Contact_Address/dif:Province_or_State!=''">
                                <gmd:administrativeArea>
                                  <gco:CharacterString>
                                    <xsl:value-of select="../dif:Contact_Address/dif:Province_or_State"/>
                                  </gco:CharacterString>
                                </gmd:administrativeArea>
                              </xsl:if>
                              <xsl:if test="../dif:Contact_Address/dif:Postal_Code!=''">
                                <gmd:postalCode>
                                  <gco:CharacterString>
                                    <xsl:value-of select="../dif:Contact_Address/dif:Postal_Code"/>
                                  </gco:CharacterString>
                                </gmd:postalCode>
                              </xsl:if>
                              <xsl:if test="../dif:Contact_Address/dif:Country!=''">
                                <gmd:country>
                                  <gco:CharacterString>
                                    <xsl:value-of select="../dif:Contact_Address/dif:Country"/>
                                  </gco:CharacterString>
                                </gmd:country>
                              </xsl:if>
                              <xsl:for-each select="../dif:Email">
                               <gmd:electronicMailAddress>
                                 <gco:CharacterString>
                                     <xsl:value-of select="."/>
                                 </gco:CharacterString>
                               </gmd:electronicMailAddress>
                              </xsl:for-each>
                            </gmd:CI_Address>
                          </gmd:address>
                        </xsl:if>
                        
                      </gmd:CI_Contact>
                    </gmd:contactInfo>
									
									</xsl:if>
											
									<gmd:role>
										<gmd:CI_RoleCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_RoleCode" codeListValue="author"/>
									</gmd:role>
								</gmd:CI_ResponsibleParty>
							</gmd:contact>
						</xsl:if>
					</xsl:for-each>

				</xsl:when>
				<xsl:otherwise>

            <gmd:contact>
              <gmd:CI_ResponsibleParty>
                <gmd:individualName gco:nilReason="missing">
                  <gco:CharacterString/>
                </gmd:individualName>
                <gmd:role>
                  <gmd:CI_RoleCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_RoleCode" codeListValue="author"/>
                </gmd:role>
              </gmd:CI_ResponsibleParty>
            </gmd:contact>					

				</xsl:otherwise>
			</xsl:choose>			
		
		</xsl:for-each>
		
		<xsl:if test="dif:Originating_Metadata_Node!=''">
      <gmd:contact>
        <gmd:CI_ResponsibleParty>
          <gmd:organisationName>
            <gco:CharacterString>
              <xsl:value-of select="dif:Originating_Metadata_Node"/>
            </gco:CharacterString>
          </gmd:organisationName>
          <gmd:role>
            <gmd:CI_RoleCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_RoleCode" codeListValue="originator"/>
          </gmd:role>
        </gmd:CI_ResponsibleParty>
      </gmd:contact>
		</xsl:if>

    <xsl:choose>
      <xsl:when test="dif:DIF_Creation_Date!=''">
        <gmd:dateStamp>
          <gco:Date>
            <xsl:value-of select="dif:DIF_Creation_Date"/>
          </gco:Date>
        </gmd:dateStamp>
      </xsl:when>
      <xsl:when test="dif:DIF_Creation_Date=''">
        <gmd:dateStamp>
          <gco:Date>Unknown
          </gco:Date>
        </gmd:dateStamp>
      </xsl:when>
      <xsl:otherwise>
        <gmd:dateStamp>
          <gco:Date>Unknown
          </gco:Date>
        </gmd:dateStamp>          
      </xsl:otherwise>          
    </xsl:choose>
		
		<gmd:metadataStandardName>
			<gco:CharacterString>
				<xsl:value-of select="dif:Metadata_Name"/>
			</gco:CharacterString>
		</gmd:metadataStandardName>
		
		<gmd:metadataStandardVersion>
			<gco:CharacterString>
				<xsl:value-of select="dif:Metadata_Version"/>
			</gco:CharacterString>
		</gmd:metadataStandardVersion>

		<gmd:identificationInfo>
			<gmd:MD_DataIdentification>
				<gmd:citation>
					<gmd:CI_Citation>
					
						<gmd:title>
							<gco:CharacterString>
								<xsl:value-of select="dif:Entry_Title"/>
							</gco:CharacterString>
						</gmd:title>
						
						<xsl:if test="dif:Data_Set_Citation/dif:Dataset_Title!=''">
              <xsl:for-each select="dif:Data_Set_Citation">
                <gmd:alternateTitle>
                  <gco:CharacterString>
                    <xsl:value-of select="./dif:Dataset_Title"/>
                  </gco:CharacterString>
                </gmd:alternateTitle>
              </xsl:for-each>
						</xsl:if>
						
						<xsl:choose>
              <xsl:when test="dif:DIF_Creation_Date!=''">
                <gmd:date>
                  <gmd:CI_Date>
                    <gmd:date>
                      <gco:Date>
                        <xsl:value-of select="dif:DIF_Creation_Date"/>
                      </gco:Date>
                    </gmd:date>
                    <gmd:dateType>
                      <gmd:CI_DateTypeCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_DateTypeCode" codeListValue="publication"/>
                    </gmd:dateType>
                  </gmd:CI_Date>
                </gmd:date>
              </xsl:when>
              <xsl:when test="dif:DIF_Creation_Date=''">
                <gmd:date>
                  <gmd:CI_Date>
                    <gmd:date>
                      <gco:Date>Unknown
                      </gco:Date>
                    </gmd:date>
                    <gmd:dateType>
                      <gmd:CI_DateTypeCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_DateTypeCode" codeListValue="publication"/>
                    </gmd:dateType>
                  </gmd:CI_Date>
                </gmd:date>
              </xsl:when>
              <xsl:otherwise>
                <gmd:date>
                  <gmd:CI_Date>
                    <gmd:date>
                      <gco:Date>Unknown
                      </gco:Date>
                    </gmd:date>
                    <gmd:dateType>
                      <gmd:CI_DateTypeCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_DateTypeCode" codeListValue="publication"/>
                    </gmd:dateType>
                  </gmd:CI_Date>
                </gmd:date>                  
              </xsl:otherwise>                  
            </xsl:choose>
						
						<xsl:if test="dif:Data_Set_Citation/dif:Version!=''">
              <gmd:edition>
                <gco:CharacterString>
                  <xsl:value-of select="dif:Data_Set_Citation/dif:Version"/>
                </gco:CharacterString>
              </gmd:edition>
						</xsl:if>
						
						<xsl:if test="dif:Data_Center/dif:Data_Set_ID!=''">
              <xsl:for-each select="dif:Data_Center/dif:Data_Set_ID">
                <gmd:identifier>
                  <gmd:MD_Identifier>
                    <gmd:code>
                      <gco:CharacterString>
                        <xsl:value-of select="."/>
                      </gco:CharacterString>
                    </gmd:code>
                  </gmd:MD_Identifier>
                </gmd:identifier>
              </xsl:for-each>
						</xsl:if>
						
						<xsl:if test="dif:Personnel!=''">
						
              <xsl:for-each select="dif:Personnel">
                <xsl:for-each select="dif:Role">
                  <xsl:if test=".='INVESTIGATOR'">								
                    <gmd:citedResponsibleParty>
                      <gmd:CI_ResponsibleParty>
                        <gmd:individualName>
                          <gco:CharacterString>
                            <xsl:value-of select="../dif:Last_Name"/>, <xsl:value-of select="../dif:First_Name"/>
                          </gco:CharacterString>
                        </gmd:individualName>

                        <xsl:if test="../dif:Phone!='' or ../dif:Fax!='' or ../dif:Contact_Address/dif:Address!='' or ../dif:Contact_Address/dif:City!='' or ../dif:Contact_Address/dif:Province_or_State!='' or ../dif:Contact_Address/dif:Postal_Code!='' or ../dif:Contact_Address/dif:Country!='' or ../dif:Email!=''">
                        
                          <gmd:contactInfo>
                            <gmd:CI_Contact>
                            
                              <xsl:if test="../dif:Phone!='' or ../dif:Fax!=''">
                                <gmd:phone>
                                  <gmd:CI_Telephone>
                                    <xsl:for-each select="../dif:Phone">
                                      <gmd:voice>
                                        <gco:CharacterString>
                                            <xsl:value-of select="."/>
                                        </gco:CharacterString>
                                      </gmd:voice>
                                     </xsl:for-each>
                                     <xsl:for-each select="../dif:Fax">
                                      <gmd:facsimile>
                                        <gco:CharacterString>
                                            <xsl:value-of select="."/>
                                        </gco:CharacterString>
                                      </gmd:facsimile>
                                     </xsl:for-each>                       
                                  </gmd:CI_Telephone>
                                </gmd:phone>
                              </xsl:if>
                              
                              <xsl:if test="../dif:Contact_Address/dif:Address!='' or ../dif:Contact_Address/dif:City!='' or ../dif:Contact_Address/dif:Province_or_State!='' or ../dif:Contact_Address/dif:Postal_Code!='' or ../dif:Contact_Address/dif:Country!='' or ../dif:Email!=''">
                                <gmd:address>
                                  <gmd:CI_Address>
                                    <xsl:for-each select="../dif:Contact_Address/dif:Address">
                                      <gmd:deliveryPoint>
                                        <gco:CharacterString>
                                          <xsl:value-of select="."/>
                                        </gco:CharacterString>
                                      </gmd:deliveryPoint>
                                    </xsl:for-each>
                                    <xsl:if test="../dif:Contact_Address/dif:City!=''">
                                      <gmd:city>
                                        <gco:CharacterString>
                                          <xsl:value-of select="../dif:Contact_Address/dif:City"/>
                                        </gco:CharacterString>
                                      </gmd:city>
                                    </xsl:if>
                                    <xsl:if test="../dif:Contact_Address/dif:Province_or_State!=''">
                                      <gmd:administrativeArea>
                                        <gco:CharacterString>
                                          <xsl:value-of select="../dif:Contact_Address/dif:Province_or_State"/>
                                        </gco:CharacterString>
                                      </gmd:administrativeArea>
                                    </xsl:if>
                                    <xsl:if test="../dif:Contact_Address/dif:Postal_Code!=''">
                                      <gmd:postalCode>
                                        <gco:CharacterString>
                                          <xsl:value-of select="../dif:Contact_Address/dif:Postal_Code"/>
                                        </gco:CharacterString>
                                      </gmd:postalCode>
                                    </xsl:if>
                                    <xsl:if test="../dif:Contact_Address/dif:Country!=''">
                                      <gmd:country>
                                        <gco:CharacterString>
                                          <xsl:value-of select="../dif:Contact_Address/dif:Country"/>
                                        </gco:CharacterString>
                                      </gmd:country>
                                    </xsl:if>
                                    <xsl:for-each select="../dif:Email">
                                     <gmd:electronicMailAddress>
                                       <gco:CharacterString>
                                           <xsl:value-of select="."/>
                                       </gco:CharacterString>
                                     </gmd:electronicMailAddress>
                                    </xsl:for-each>
                                  </gmd:CI_Address>
                                </gmd:address>
                              </xsl:if>
                              
                            </gmd:CI_Contact>
                          </gmd:contactInfo>
                        
                        </xsl:if>

                        <gmd:role>
                          <gmd:CI_RoleCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_RoleCode" codeListValue="principalInvestigator"/>
                        </gmd:role>
                      </gmd:CI_ResponsibleParty>							
                    </gmd:citedResponsibleParty>
                  </xsl:if>
                </xsl:for-each>									
              </xsl:for-each>							
            
              <xsl:for-each select="dif:Personnel">
                <xsl:for-each select="dif:Role">
                  <xsl:if test=".='TECHNICAL CONTACT'">								
                    <gmd:citedResponsibleParty>
                      <gmd:CI_ResponsibleParty>
                        <gmd:individualName>
                          <gco:CharacterString>
                            <xsl:value-of select="../dif:Last_Name"/>, <xsl:value-of select="../dif:First_Name"/>
                          </gco:CharacterString>
                        </gmd:individualName>

                        <xsl:if test="../dif:Phone!='' or ../dif:Fax!='' or ../dif:Contact_Address/dif:Address!='' or ../dif:Contact_Address/dif:City!='' or ../dif:Contact_Address/dif:Province_or_State!='' or ../dif:Contact_Address/dif:Postal_Code!='' or ../dif:Contact_Address/dif:Country!='' or ../dif:Email!=''">
                        
                          <gmd:contactInfo>
                            <gmd:CI_Contact>
                            
                              <xsl:if test="../dif:Phone!='' or ../dif:Fax!=''">
                                <gmd:phone>
                                  <gmd:CI_Telephone>
                                    <xsl:for-each select="../dif:Phone">
                                      <gmd:voice>
                                        <gco:CharacterString>
                                            <xsl:value-of select="."/>
                                        </gco:CharacterString>
                                      </gmd:voice>
                                     </xsl:for-each>
                                     <xsl:for-each select="../dif:Fax">
                                      <gmd:facsimile>
                                        <gco:CharacterString>
                                            <xsl:value-of select="."/>
                                        </gco:CharacterString>
                                      </gmd:facsimile>
                                     </xsl:for-each>                       
                                  </gmd:CI_Telephone>
                                </gmd:phone>
                              </xsl:if>
                              
                              <xsl:if test="../dif:Contact_Address/dif:Address!='' or ../dif:Contact_Address/dif:City!='' or ../dif:Contact_Address/dif:Province_or_State!='' or ../dif:Contact_Address/dif:Postal_Code!='' or ../dif:Contact_Address/dif:Country!='' or ../dif:Email!=''">
                                <gmd:address>
                                  <gmd:CI_Address>
                                    <xsl:for-each select="../dif:Contact_Address/dif:Address">
                                      <gmd:deliveryPoint>
                                        <gco:CharacterString>
                                          <xsl:value-of select="."/>
                                        </gco:CharacterString>
                                      </gmd:deliveryPoint>
                                    </xsl:for-each>
                                    <xsl:if test="../dif:Contact_Address/dif:City!=''">
                                      <gmd:city>
                                        <gco:CharacterString>
                                          <xsl:value-of select="../dif:Contact_Address/dif:City"/>
                                        </gco:CharacterString>
                                      </gmd:city>
                                    </xsl:if>
                                    <xsl:if test="../dif:Contact_Address/dif:Province_or_State!=''">
                                      <gmd:administrativeArea>
                                        <gco:CharacterString>
                                          <xsl:value-of select="../dif:Contact_Address/dif:Province_or_State"/>
                                        </gco:CharacterString>
                                      </gmd:administrativeArea>
                                    </xsl:if>
                                    <xsl:if test="../dif:Contact_Address/dif:Postal_Code!=''">
                                      <gmd:postalCode>
                                        <gco:CharacterString>
                                          <xsl:value-of select="../dif:Contact_Address/dif:Postal_Code"/>
                                        </gco:CharacterString>
                                      </gmd:postalCode>
                                    </xsl:if>
                                    <xsl:if test="../dif:Contact_Address/dif:Country!=''">
                                      <gmd:country>
                                        <gco:CharacterString>
                                          <xsl:value-of select="../dif:Contact_Address/dif:Country"/>
                                        </gco:CharacterString>
                                      </gmd:country>
                                    </xsl:if>
                                    <xsl:for-each select="../dif:Email">
                                     <gmd:electronicMailAddress>
                                       <gco:CharacterString>
                                           <xsl:value-of select="."/>
                                       </gco:CharacterString>
                                     </gmd:electronicMailAddress>
                                    </xsl:for-each>
                                  </gmd:CI_Address>
                                </gmd:address>
                              </xsl:if>
                              
                            </gmd:CI_Contact>
                          </gmd:contactInfo>
                        
                        </xsl:if>

                        <gmd:role>
                          <gmd:CI_RoleCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_RoleCode" codeListValue="processor"/>
                        </gmd:role>
                      </gmd:CI_ResponsibleParty>							
                    </gmd:citedResponsibleParty>
                  </xsl:if>
                </xsl:for-each>									
              </xsl:for-each>
						
						</xsl:if>
						
						<xsl:if test="dif:Data_Set_Citation/dif:Dataset_Creator!=''">
              <gmd:citedResponsibleParty>
                <xsl:for-each select="dif:Data_Set_Citation">
                  <gmd:CI_ResponsibleParty>
                    <gmd:individualName>
                      <gco:CharacterString>
                        <xsl:value-of select="./dif:Dataset_Creator"/>
                      </gco:CharacterString>
                    </gmd:individualName>
                    <gmd:role>
                      <gmd:CI_RoleCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_RoleCode" codeListValue="originator"/>
                    </gmd:role>
                  </gmd:CI_ResponsibleParty>
                </xsl:for-each>							
              </gmd:citedResponsibleParty>
						</xsl:if>
						
						<xsl:if test="dif:Originating_Center!=''">
              <gmd:citedResponsibleParty>
                <gmd:CI_ResponsibleParty>
                  <gmd:individualName>
                    <gco:CharacterString>
                      <xsl:value-of select="dif:Originating_Center"/>
                    </gco:CharacterString>
                  </gmd:individualName>
                  <gmd:role>
                    <gmd:CI_RoleCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_RoleCode" codeListValue="originator"/>
                  </gmd:role>
                </gmd:CI_ResponsibleParty>
              </gmd:citedResponsibleParty>
						</xsl:if>
						
						<xsl:if test="dif:Data_Set_Citation/dif:Dataset_Publisher!=''">
              <gmd:citedResponsibleParty>
                <xsl:for-each select="dif:Data_Set_Citation">
                  <gmd:CI_ResponsibleParty>
                    <gmd:individualName>
                      <gco:CharacterString>
                        <xsl:value-of select="./dif:Dataset_Publisher"/>
                      </gco:CharacterString>
                    </gmd:individualName>
                    <gmd:role>
                      <gmd:CI_RoleCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_RoleCode" codeListValue="publisher"/>
                    </gmd:role>
                  </gmd:CI_ResponsibleParty>
                </xsl:for-each>							
              </gmd:citedResponsibleParty>
						</xsl:if>							
			
						<xsl:if test="dif:Data_Set_Citation/dif:Dataset_Series_Name!='' or dif:Data_Set_Citation/dif:Issue_Identification!=''">
              <gmd:series>
                <xsl:for-each select="dif:Data_Set_Citation">
                  <gmd:CI_Series>
                    <xsl:if test="dif:Dataset_Series_Name!=''">
                      <gmd:name>
                        <gco:CharacterString>
                          <xsl:value-of select="./dif:Dataset_Series_Name"/>
                        </gco:CharacterString>
                      </gmd:name>
                    </xsl:if>
                    <xsl:if test="dif:Issue_Identification!=''">
                      <gmd:issueIdentification>
                        <gco:CharacterString>
                          <xsl:value-of select="./dif:Issue_Identification"/>
                        </gco:CharacterString>
                      </gmd:issueIdentification>
                    </xsl:if>							
                  </gmd:CI_Series>
                </xsl:for-each>
              </gmd:series>
						</xsl:if>
						
						<xsl:if test="dif:Data_Set_Citation/dif:Other_Citation_Details!=''">
              <xsl:for-each select="dif:Data_Set_Citation">
                <gmd:otherCitationDetails>
                  <gco:CharacterString>
                    <xsl:value-of select="./dif:Other_Citation_Details"/>
                  </gco:CharacterString>
                </gmd:otherCitationDetails>
              </xsl:for-each>
						</xsl:if>
						
					</gmd:CI_Citation>
				</gmd:citation>				
				
				<gmd:abstract>
					<gco:CharacterString>
						<xsl:value-of select="dif:Summary"/>
					</gco:CharacterString>
				</gmd:abstract>
				
				<xsl:if test="dif:Data_Set_Progress!=''">
				
          <xsl:for-each select="dif:Data_Set_Progress">
            <xsl:if test=".='COMPLETE'">
              <gmd:status>
                <gmd:MD_ProgressCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#MD_ProgressCode" codeListValue="completed"/>
              </gmd:status>
            </xsl:if>
          </xsl:for-each>
          
          <xsl:for-each select="dif:Data_Set_Progress">
            <xsl:if test=".='IN WORK'">
              <gmd:status>
                <gmd:MD_ProgressCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#MD_ProgressCode" codeListValue="onGoing"/>
              </gmd:status>
            </xsl:if>
          </xsl:for-each>
          
          <xsl:for-each select="dif:Data_Set_Progress">
            <xsl:if test=".='PLANNED'">
              <gmd:status>
                <gmd:MD_ProgressCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#MD_ProgressCode" codeListValue="planned"/>
              </gmd:status>
            </xsl:if>
          </xsl:for-each>
				
				</xsl:if>
				
				<xsl:for-each select="dif:Data_Center">
          <xsl:for-each select="dif:Personnel/dif:Role">
            <gmd:pointOfContact>
              <gmd:CI_ResponsibleParty>
                <gmd:individualName>
                  <gco:CharacterString>
                    <xsl:value-of select="../dif:First_Name"/>, <xsl:value-of select="../dif:Last_Name"/>
                  </gco:CharacterString>
                </gmd:individualName>
                <gmd:organisationName>
                  <gco:CharacterString>
                    <xsl:value-of select="../../dif:Data_Center_Name/dif:Short_Name"/> | <xsl:value-of select="../../dif:Data_Center_Name/dif:Long_Name"/>
                  </gco:CharacterString>
                </gmd:organisationName>
                <gmd:positionName>
                  <gco:CharacterString>
                    <xsl:value-of select="."/>
                  </gco:CharacterString>
                </gmd:positionName>

                <xsl:if test="../dif:Phone!='' or ../dif:Fax!='' or ../dif:Contact_Address/dif:Address!='' or ../dif:Contact_Address/dif:City!='' or ../dif:Contact_Address/dif:Province_or_State!='' or ../dif:Contact_Address/dif:Postal_Code!='' or ../dif:Contact_Address/dif:Country!='' or ../dif:Email!='' or ../../dif:Data_Center_URL!=''">
						
                  <gmd:contactInfo>
                    <gmd:CI_Contact>

                      <xsl:if test="../dif:Phone!='' or ../dif:Fax!=''">
                        <gmd:phone>
                          <gmd:CI_Telephone>
                            <xsl:for-each select="../dif:Phone">
                              <gmd:voice>
                                <gco:CharacterString>
                                    <xsl:value-of select="."/>
                                </gco:CharacterString>
                              </gmd:voice>
                             </xsl:for-each>
                             <xsl:for-each select="../dif:Fax">
                              <gmd:facsimile>
                                <gco:CharacterString>
                                    <xsl:value-of select="."/>
                                </gco:CharacterString>
                              </gmd:facsimile>
                             </xsl:for-each>                       
                          </gmd:CI_Telephone>
                        </gmd:phone>
                      </xsl:if>
                      
                      <xsl:if test="../dif:Contact_Address/dif:Address!='' or ../dif:Contact_Address/dif:City!='' or ../dif:Contact_Address/dif:Province_or_State!='' or ../dif:Contact_Address/dif:Postal_Code!='' or ../dif:Contact_Address/dif:Country!='' or ../dif:Email!='' or ../../dif:Data_Center_URL!=''">
                        <gmd:address>
                          <gmd:CI_Address>
                            <xsl:for-each select="../dif:Contact_Address/dif:Address">
                              <gmd:deliveryPoint>
                                <gco:CharacterString>
                                  <xsl:value-of select="."/>
                                </gco:CharacterString>
                              </gmd:deliveryPoint>
                            </xsl:for-each>
                            <xsl:if test="../dif:Contact_Address/dif:City!=''">
                              <gmd:city>
                                <gco:CharacterString>
                                  <xsl:value-of select="../dif:Contact_Address/dif:City"/>
                                </gco:CharacterString>
                              </gmd:city>
                            </xsl:if>
                            <xsl:if test="../dif:Contact_Address/dif:Province_or_State!=''">
                              <gmd:administrativeArea>
                                <gco:CharacterString>
                                  <xsl:value-of select="../dif:Contact_Address/dif:Province_or_State"/>
                                </gco:CharacterString>
                              </gmd:administrativeArea>
                            </xsl:if>
                            <xsl:if test="../dif:Contact_Address/dif:Postal_Code!=''">
                              <gmd:postalCode>
                                <gco:CharacterString>
                                  <xsl:value-of select="../dif:Contact_Address/dif:Postal_Code"/>
                                </gco:CharacterString>
                              </gmd:postalCode>
                            </xsl:if>
                            <xsl:if test="../dif:Contact_Address/dif:Country!=''">
                              <gmd:country>
                                <gco:CharacterString>
                                  <xsl:value-of select="../dif:Contact_Address/dif:Country"/>
                                </gco:CharacterString>
                              </gmd:country>
                            </xsl:if>
                            <xsl:for-each select="../dif:Email">
                             <gmd:electronicMailAddress>
                               <gco:CharacterString>
                                   <xsl:value-of select="."/>
                               </gco:CharacterString>
                             </gmd:electronicMailAddress>
                            </xsl:for-each>
                          </gmd:CI_Address>
                        </gmd:address>
                      </xsl:if>
                            
                      <xsl:if test="../../dif:Data_Center_URL!=''">
                        <gmd:onlineResource>
                          <gmd:CI_OnlineResource>
                            <gmd:linkage>
                              <gmd:URL>
                                <xsl:value-of select="../../dif:Data_Center_URL"/>
                              </gmd:URL>
                            </gmd:linkage>
                          </gmd:CI_OnlineResource>
                        </gmd:onlineResource>
                      </xsl:if>
                      
                    </gmd:CI_Contact>
                  </gmd:contactInfo>
								
								</xsl:if>

                <gmd:role>
                  <gmd:CI_RoleCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_RoleCode" codeListValue="resourceProvider"/>
                </gmd:role>
              </gmd:CI_ResponsibleParty>
            </gmd:pointOfContact>
					</xsl:for-each>
				</xsl:for-each>
				
				<xsl:if test="dif:Multimedia_Sample/dif:File!='' or dif:Multimedia_Sample/dif:Description!='' or dif:Multimedia_Sample/dif:Format!=''">
				
          <xsl:for-each select="dif:Multimedia_Sample">
            <gmd:graphicOverview>
              <gmd:MD_BrowseGraphic>
                <xsl:choose>
                  <xsl:when test="dif:File!=''">
                    <gmd:fileName>
                      <gco:CharacterString>
                        <xsl:value-of select="./dif:File"/>
                      </gco:CharacterString>
                    </gmd:fileName>
                  </xsl:when>
                  <xsl:when test="dif:File=''">
                    <gmd:fileName gco:nilReason="missing">
                      <gco:CharacterString/>
                    </gmd:fileName>                     
                  </xsl:when>
                  <xsl:otherwise>
                    <gmd:fileName gco:nilReason="missing">
                      <gco:CharacterString/>
                    </gmd:fileName>                      
                  </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="dif:Description!=''">
                  <gmd:fileDescription>
                    <gco:CharacterString>
                      <xsl:value-of select="./dif:Description"/>
                    </gco:CharacterString>
                  </gmd:fileDescription>
                </xsl:if>
                <xsl:if test="dif:Format!=''">
                  <gmd:fileType>
                    <gco:CharacterString>
                      <xsl:value-of select="./dif:Format"/>
                    </gco:CharacterString>
                  </gmd:fileType>
                </xsl:if>
              </gmd:MD_BrowseGraphic>
            </gmd:graphicOverview>
          </xsl:for-each>
				
				</xsl:if>				
				
				<gmd:descriptiveKeywords>
					<gmd:MD_Keywords>
						<xsl:for-each select="dif:Parameters">
							<gmd:keyword>							
								<gco:CharacterString>
									<xsl:value-of select="dif:Topic"/> | <xsl:value-of select="dif:Term"/> | <xsl:value-of select="dif:Variable_Level_1"/> | <xsl:value-of select="dif:Variable_Level_2"/> | <xsl:value-of select="dif:Variable_Level_3"/>
								</gco:CharacterString>
							</gmd:keyword>
						</xsl:for-each>
						<gmd:thesaurusName>
							<gmd:CI_Citation>
								<gmd:title>
									<gco:CharacterString>GCMD Keywords</gco:CharacterString>
								</gmd:title>
								<gmd:alternateTitle>
									<gco:CharacterString>Science Keywords</gco:CharacterString>
								</gmd:alternateTitle>
								<gmd:date>
									<gmd:CI_Date>
										<gmd:date>
											<gco:Date>2008-02-05</gco:Date>
										</gmd:date>
										<gmd:dateType>
											<gmd:CI_DateTypeCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_DateTypeCode" codeListValue="revision"/>
										</gmd:dateType>
									</gmd:CI_Date>
								</gmd:date>
								<gmd:collectiveTitle>
									<gco:CharacterString>Olsen, L.M., G. Major, K. Shein, J. Scialdone, R. Vogel, S. Leicester, H. Weir, S. Ritz, T. Stevens, M. Meaux, C.Solomon, R. Bilodeau, M. Holland, T. Northcutt, R. A. Restrepo, 2007 .   NASA/Global Change Master Directory (GCMD) Earth Science Keywords. Version  6.0.0.0.0</gco:CharacterString>
								</gmd:collectiveTitle>
							</gmd:CI_Citation>
						</gmd:thesaurusName>								
					</gmd:MD_Keywords>
				</gmd:descriptiveKeywords>
				
				<xsl:if test="dif:Keyword!=''">
          <gmd:descriptiveKeywords>
            <gmd:MD_Keywords>
              <xsl:for-each select="dif:Keyword">
                <gmd:keyword>
                  <gco:CharacterString>
                    <xsl:value-of select="."/>
                  </gco:CharacterString>
                </gmd:keyword>
              </xsl:for-each>
            </gmd:MD_Keywords>
          </gmd:descriptiveKeywords>
				</xsl:if>
									
				<xsl:if test="dif:Sensor_Name!=''">
          <gmd:descriptiveKeywords>
            <gmd:MD_Keywords>
              <xsl:for-each select="dif:Sensor_Name">
                <gmd:keyword>
                  <gco:CharacterString>
                    <xsl:value-of select="dif:Short_Name"/> | <xsl:value-of select="dif:Long_Name"/>
                  </gco:CharacterString>
                </gmd:keyword>
              </xsl:for-each>	
              <gmd:thesaurusName>
                <gmd:CI_Citation>
                  <gmd:title>
                    <gco:CharacterString>GCMD Keywords</gco:CharacterString>
                  </gmd:title>
                  <gmd:alternateTitle>
                    <gco:CharacterString>Instruments</gco:CharacterString>
                  </gmd:alternateTitle>
                  <gmd:date>
                    <gmd:CI_Date>
                      <gmd:date>
                        <gco:Date>2008-01-22</gco:Date>
                      </gmd:date>
                      <gmd:dateType>
                        <gmd:CI_DateTypeCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_DateTypeCode" codeListValue="revision"/>
                      </gmd:dateType>
                    </gmd:CI_Date>
                  </gmd:date>
                  <gmd:collectiveTitle>
                    <gco:CharacterString>Olsen, L.M., G. Major, K. Shein, J. Scialdone, R. Vogel, S. Leicester, H. Weir, S. Ritz, T. Stevens, M. Meaux, C.Solomon, R. Bilodeau, M. Holland, T. Northcutt, R. A. Restrepo, 2007 .   NASA/Global Change Master Directory (GCMD) Earth Science Keywords. Version  6.0.0.0.0</gco:CharacterString>
                  </gmd:collectiveTitle>
                </gmd:CI_Citation>
              </gmd:thesaurusName>								
            </gmd:MD_Keywords>
          </gmd:descriptiveKeywords>
				</xsl:if>
				
				<xsl:if test="dif:Source_Name!=''">
          <gmd:descriptiveKeywords>
            <gmd:MD_Keywords>
              <xsl:for-each select="dif:Source_Name">
                <gmd:keyword>
                  <gco:CharacterString>
                    <xsl:value-of select="dif:Short_Name"/> | <xsl:value-of select="dif:Long_Name"/>
                  </gco:CharacterString>
                </gmd:keyword>
              </xsl:for-each>	
              <gmd:thesaurusName>
                <gmd:CI_Citation>
                  <gmd:title>
                    <gco:CharacterString>GCMD Keywords</gco:CharacterString>
                  </gmd:title>
                  <gmd:alternateTitle>
                    <gco:CharacterString>Platforms</gco:CharacterString>
                  </gmd:alternateTitle>
                  <gmd:date>
                    <gmd:CI_Date>
                      <gmd:date>
                        <gco:Date>2008-02-05</gco:Date>
                      </gmd:date>
                      <gmd:dateType>
                        <gmd:CI_DateTypeCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_DateTypeCode" codeListValue="revision"/>
                      </gmd:dateType>
                    </gmd:CI_Date>
                  </gmd:date>
                  <gmd:collectiveTitle>
                    <gco:CharacterString>Olsen, L.M., G. Major, K. Shein, J. Scialdone, R. Vogel, S. Leicester, H. Weir, S. Ritz, T. Stevens, M. Meaux, C.Solomon, R. Bilodeau, M. Holland, T. Northcutt, R. A. Restrepo, 2007 .   NASA/Global Change Master Directory (GCMD) Earth Science Keywords. Version  6.0.0.0.0</gco:CharacterString>
                  </gmd:collectiveTitle>
                </gmd:CI_Citation>
              </gmd:thesaurusName>								
            </gmd:MD_Keywords>
          </gmd:descriptiveKeywords>
				</xsl:if>
				
          <xsl:if test="dif:Paleo_Temporal_Coverage!=''">
            <gmd:descriptiveKeywords>
              <gmd:MD_Keywords>
                <xsl:for-each select="dif:Paleo_Temporal_Coverage">
                  <gmd:keyword>							
                    <gco:CharacterString>
                      Paleo Start Date <xsl:value-of select="dif:Paleo_Start_Date"/>
                    </gco:CharacterString>
                  </gmd:keyword>
                  <gmd:keyword>							
                    <gco:CharacterString>
                      Paleo Stop Date <xsl:value-of select="dif:Paleo_Stop_Date"/>
                    </gco:CharacterString>
                  </gmd:keyword>
                </xsl:for-each>
                <gmd:thesaurusName>
                  <gmd:CI_Citation>
                    <gmd:title>
                      <gco:CharacterString>GCMD Paleo Temporal Coverage</gco:CharacterString>
                    </gmd:title>
                    <gmd:alternateTitle>
                      <gco:CharacterString>Paleo Start and Stop Dates</gco:CharacterString>
                    </gmd:alternateTitle>
                    <gmd:date>
                      <gmd:CI_Date>
                        <gmd:date>
                          <gco:Date>
                            <xsl:value-of select="dif:Last_DIF_Revision_Date"/>
                          </gco:Date>
                        </gmd:date>
                        <gmd:dateType>
                          <gmd:CI_DateTypeCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_DateTypeCode" codeListValue="revision"/>
                        </gmd:dateType>
                      </gmd:CI_Date>
                    </gmd:date>
                  </gmd:CI_Citation>
                </gmd:thesaurusName>								
              </gmd:MD_Keywords>
            </gmd:descriptiveKeywords>
          </xsl:if>
          
          <xsl:if test="dif:Paleo_Temporal_Coverage/dif:Chronostratigraphic_Unit!=''">
            <gmd:descriptiveKeywords>
              <gmd:MD_Keywords>
                <xsl:for-each select="dif:Paleo_Temporal_Coverage/dif:Chronostratigraphic_Unit">
                  <gmd:keyword>							
                    <gco:CharacterString>
                      <xsl:value-of select="dif:Eon"/> | <xsl:value-of select="dif:Era"/> | <xsl:value-of select="dif:Period"/> | <xsl:value-of select="dif:Epoch"/>
                    </gco:CharacterString>
                  </gmd:keyword>
                </xsl:for-each>
                <gmd:thesaurusName>
                  <gmd:CI_Citation>
                    <gmd:title>
                      <gco:CharacterString>GCMD Keywords</gco:CharacterString>
                    </gmd:title>
                    <gmd:alternateTitle>
                      <gco:CharacterString>Chronostratigraphic Unit</gco:CharacterString>
                    </gmd:alternateTitle>
                    <gmd:date>
                      <gmd:CI_Date>
                        <gmd:date>
                          <gco:Date>2007-04-01</gco:Date>
                        </gmd:date>
                        <gmd:dateType>
                          <gmd:CI_DateTypeCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_DateTypeCode" codeListValue="revision"/>
                        </gmd:dateType>
                      </gmd:CI_Date>
                    </gmd:date>
                    <gmd:collectiveTitle>
                      <gco:CharacterString>Olsen, L.M., G. Major, K. Shein, J. Scialdone, R. Vogel, S. Leicester, H. Weir, S. Ritz, T. Stevens, M. Meaux, C.Solomon, R. Bilodeau, M. Holland, T. Northcutt, R. A. Restrepo, 2007 .   NASA/Global Change Master Directory (GCMD) Earth Science Keywords. Version  6.0.0.0.0</gco:CharacterString>
                    </gmd:collectiveTitle>
                  </gmd:CI_Citation>
                </gmd:thesaurusName>								
              </gmd:MD_Keywords>
            </gmd:descriptiveKeywords>
          </xsl:if>
									
				<xsl:if test="dif:Project!=''">
          <gmd:descriptiveKeywords>
            <gmd:MD_Keywords>
              <xsl:for-each select="dif:Project">
                <gmd:keyword>
                  <gco:CharacterString>
                    <xsl:value-of select="dif:Short_Name"/> | <xsl:value-of select="dif:Long_Name"/>
                  </gco:CharacterString>
                </gmd:keyword>
              </xsl:for-each>	
              <gmd:thesaurusName>
                <gmd:CI_Citation>
                  <gmd:title>
                    <gco:CharacterString>GCMD Keywords</gco:CharacterString>
                  </gmd:title>
                  <gmd:alternateTitle>
                    <gco:CharacterString>Projects</gco:CharacterString>
                  </gmd:alternateTitle>
                  <gmd:date>
                    <gmd:CI_Date>
                      <gmd:date>
                        <gco:Date>2008-01-24</gco:Date>
                      </gmd:date>
                      <gmd:dateType>
                        <gmd:CI_DateTypeCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_DateTypeCode" codeListValue="revision"/>
                      </gmd:dateType>
                    </gmd:CI_Date>
                  </gmd:date>
                  <gmd:collectiveTitle>
                    <gco:CharacterString>Olsen, L.M., G. Major, K. Shein, J. Scialdone, R. Vogel, S. Leicester, H. Weir, S. Ritz, T. Stevens, M. Meaux, C.Solomon, R. Bilodeau, M. Holland, T. Northcutt, R. A. Restrepo, 2007 .   NASA/Global Change Master Directory (GCMD) Earth Science Keywords. Version  6.0.0.0.0</gco:CharacterString>
                  </gmd:collectiveTitle>
                </gmd:CI_Citation>
              </gmd:thesaurusName>								
            </gmd:MD_Keywords>
          </gmd:descriptiveKeywords>
				</xsl:if>	
				
				<xsl:if test="dif:IDN_Node!=''">
          <gmd:descriptiveKeywords>
            <gmd:MD_Keywords>
              <xsl:for-each select="dif:IDN_Node">
                <gmd:keyword>
                  <gco:CharacterString>
                    <xsl:value-of select="dif:Short_Name"/>
                  </gco:CharacterString>
                </gmd:keyword>
              </xsl:for-each>	
              <gmd:thesaurusName>
                <gmd:CI_Citation>
                  <gmd:title>
                    <gco:CharacterString>GCMD Keywords</gco:CharacterString>
                  </gmd:title>
                  <gmd:alternateTitle>
                    <gco:CharacterString>IDN Nodes</gco:CharacterString>
                  </gmd:alternateTitle>
                  <gmd:date>
                    <gmd:CI_Date>
                      <gmd:date>
                        <gco:Date>2007-04-01</gco:Date>
                      </gmd:date>
                      <gmd:dateType>
                        <gmd:CI_DateTypeCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_DateTypeCode" codeListValue="revision"/>
                      </gmd:dateType>
                    </gmd:CI_Date>
                  </gmd:date>
                  <gmd:collectiveTitle>
                    <gco:CharacterString>Olsen, L.M., G. Major, K. Shein, J. Scialdone, R. Vogel, S. Leicester, H. Weir, S. Ritz, T. Stevens, M. Meaux, C.Solomon, R. Bilodeau, M. Holland, T. Northcutt, R. A. Restrepo, 2007 .   NASA/Global Change Master Directory (GCMD) Earth Science Keywords. Version  6.0.0.0.0</gco:CharacterString>
                  </gmd:collectiveTitle>
                </gmd:CI_Citation>
              </gmd:thesaurusName>								
            </gmd:MD_Keywords>
          </gmd:descriptiveKeywords>
				</xsl:if>
				
				<xsl:if test="dif:Location!=''">
          <gmd:descriptiveKeywords>
            <gmd:MD_Keywords>
              <xsl:for-each select="dif:Location">
                <gmd:keyword>
                  <gco:CharacterString>
                    <xsl:value-of select="dif:Location_Category"/> | <xsl:value-of select="dif:Location_Type"/> | <xsl:value-of select="dif:Location_Subregion1"/> | <xsl:value-of select="dif:Location_Subregion2"/> | <xsl:value-of select="dif:Location_Subregion3"/> | <xsl:value-of select="dif:Detailed_Location"/>
                  </gco:CharacterString>
                </gmd:keyword>
              </xsl:for-each>	
              <gmd:thesaurusName>
                <gmd:CI_Citation>
                  <gmd:title>
                    <gco:CharacterString>GCMD Keywords</gco:CharacterString>
                  </gmd:title>
                  <gmd:alternateTitle>
                    <gco:CharacterString>Locations</gco:CharacterString>
                  </gmd:alternateTitle>
                  <gmd:date>
                    <gmd:CI_Date>
                      <gmd:date>
                        <gco:Date>2008-02-05</gco:Date>
                      </gmd:date>
                      <gmd:dateType>
                        <gmd:CI_DateTypeCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_DateTypeCode" codeListValue="revision"/>
                      </gmd:dateType>
                    </gmd:CI_Date>
                  </gmd:date>
                  <gmd:collectiveTitle>
                    <gco:CharacterString>Olsen, L.M., G. Major, K. Shein, J. Scialdone, R. Vogel, S. Leicester, H. Weir, S. Ritz, T. Stevens, M. Meaux, C.Solomon, R. Bilodeau, M. Holland, T. Northcutt, R. A. Restrepo, 2007 .   NASA/Global Change Master Directory (GCMD) Earth Science Keywords. Version  6.0.0.0.0</gco:CharacterString>
                  </gmd:collectiveTitle>
                </gmd:CI_Citation>
              </gmd:thesaurusName>								
            </gmd:MD_Keywords>
          </gmd:descriptiveKeywords>
				</xsl:if>
				
				<xsl:if test="dif:Access_Constraints!=''">
          <xsl:for-each select="dif:Access_Constraints">
            <gmd:resourceConstraints>
              <gmd:MD_LegalConstraints>
                <gmd:accessConstraints>
                  <gmd:MD_RestrictionCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#MD_RestrictionCode" codeListValue="otherRestrictions"/>
                </gmd:accessConstraints>
                <gmd:otherConstraints>
                  <gco:CharacterString>
                    <xsl:value-of select="."/>
                  </gco:CharacterString>
                </gmd:otherConstraints>
              </gmd:MD_LegalConstraints>
            </gmd:resourceConstraints>
          </xsl:for-each>
				</xsl:if>
				
				<xsl:if test="dif:Use_Constraints!=''">
          <xsl:for-each select="dif:Use_Constraints">
            <gmd:resourceConstraints>
              <gmd:MD_LegalConstraints>
                <gmd:useConstraints>
                  <gmd:MD_RestrictionCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#MD_RestrictionCode" codeListValue="otherRestrictions"/>
                </gmd:useConstraints>
                <gmd:otherConstraints>
                  <gco:CharacterString>
                    <xsl:value-of select="."/>
                  </gco:CharacterString>
                </gmd:otherConstraints>
              </gmd:MD_LegalConstraints>
            </gmd:resourceConstraints>
          </xsl:for-each>
				</xsl:if>

				<xsl:choose>
					<xsl:when test="dif:Data_Set_Language">
					
						<xsl:for-each select="dif:Data_Set_Language">
						
							<xsl:if test=".='English' or .='ENGLISH'">
								<gmd:language>
									<gco:CharacterString>eng</gco:CharacterString>
								</gmd:language>
							</xsl:if>

							<xsl:if test=".='Afrikaans' or .='AFRIKAANS'">
								<gmd:language>
									<gco:CharacterString>afr</gco:CharacterString>
								</gmd:language>
							</xsl:if>

							<xsl:if test=".='Arabic' or .='ARABIC'">
								<gmd:language>
									<gco:CharacterString>ara</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Bosnian' or .='BOSNIAN'">
								<gmd:language>
									<gco:CharacterString>bos</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Bulgarian' or .='BULGARIAN'">
								<gmd:language>
									<gco:CharacterString>bul</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Chinese' or .='CHINESE'">
								<gmd:language>
									<gco:CharacterString>chi</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Croatian' or .='CROATIAN'">
								<gmd:language>
									<gco:CharacterString>scr</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Czech' or .='CZECH'">
								<gmd:language>
									<gco:CharacterString>cze</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Danish' or .='DANISH'">
								<gmd:language>
									<gco:CharacterString>dan</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Dutch' or .='DUTCH'">
								<gmd:language>
									<gco:CharacterString>dut</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Estonian' or .='ESTONIAN'">
								<gmd:language>
									<gco:CharacterString>est</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Finnish' or .='FINNISH'">
								<gmd:language>
									<gco:CharacterString>fin</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='French' or .='FRENCH'">
								<gmd:language>
									<gco:CharacterString>fre</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='German' or .='GERMAN'">
								<gmd:language>
									<gco:CharacterString>ger</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Hebrew' or .='HEBREW'">
								<gmd:language>
									<gco:CharacterString>heb</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Hungarian' or .='HUNGARIAN'">
								<gmd:language>
									<gco:CharacterString>hun</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Indonesian' or .='INDONESIAN'">
								<gmd:language>
									<gco:CharacterString>ind</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Italian' or .='ITALIAN'">
								<gmd:language>
									<gco:CharacterString>ita</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Japanese' or .='JAPANESE'">
								<gmd:language>
									<gco:CharacterString>jpn</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Korean' or .='KOREAN'">
								<gmd:language>
									<gco:CharacterString>kor</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Latvian' or .='LATVIAN'">
								<gmd:language>
									<gco:CharacterString>lav</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Lithuanian' or .='LITHUANIAN'">
								<gmd:language>
									<gco:CharacterString>lit</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Norwegian' or .='NORWEGIAN'">
								<gmd:language>
									<gco:CharacterString>nor</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Polish' or .='POLISH'">
								<gmd:language>
									<gco:CharacterString>pol</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Portugese' or .='PORTUGESE'">
								<gmd:language>
									<gco:CharacterString>por</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Romanian' or .='ROMANIAN'">
								<gmd:language>
									<gco:CharacterString>rum</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Russian' or .='RUSSIAN'">
								<gmd:language>
									<gco:CharacterString>rus</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Slovak' or .='SLOVAK'">
								<gmd:language>
									<gco:CharacterString>slo</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Spanish' or .='SPANISH'">
								<gmd:language>
									<gco:CharacterString>spa</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Ukranian' or .='UKRANIAN'">
								<gmd:language>
									<gco:CharacterString>ukr</gco:CharacterString>
								</gmd:language>
							</xsl:if>
							
							<xsl:if test=".='Vietnamese' or .='VIETNAMESE'">
								<gmd:language>
									<gco:CharacterString>vie</gco:CharacterString>
								</gmd:language>
							</xsl:if>
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<gmd:language gco:nilReason="missing">
							<gco:CharacterString/>
						</gmd:language>
					</xsl:otherwise>
				</xsl:choose>
				
				<gmd:characterSet>
					<gmd:MD_CharacterSetCode codeListValue="utf8" codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#MD_CharacterSetCode"/>
				</gmd:characterSet>
				
				<xsl:for-each select="dif:ISO_Topic_Category">
					<gmd:topicCategory>
						<gmd:MD_TopicCategoryCode>
              <xsl:value-of select="lower-case(normalize-space(.))"/>
						</gmd:MD_TopicCategoryCode>
					</gmd:topicCategory>
				</xsl:for-each>
				
				<gmd:extent>
					<gmd:EX_Extent>
					
						<xsl:choose>
              <xsl:when test="dif:Spatial_Coverage/dif:Westernmost_Longitude!='' and dif:Spatial_Coverage/dif:Easternmost_Longitude!='' and dif:Spatial_Coverage/dif:Southernmost_Latitude!='' and dif:Spatial_Coverage/dif:Northernmost_Latitude!=''">
                <gmd:geographicElement>
                  <xsl:for-each select="dif:Spatial_Coverage">
                    <gmd:EX_GeographicBoundingBox>
                      <gmd:westBoundLongitude>
                        <gco:Decimal>
                          <xsl:value-of select="./dif:Westernmost_Longitude"/>
                        </gco:Decimal>
                      </gmd:westBoundLongitude>
                      <gmd:eastBoundLongitude>
                        <gco:Decimal>
                          <xsl:value-of select="./dif:Easternmost_Longitude"/>
                        </gco:Decimal>
                      </gmd:eastBoundLongitude>
                      <gmd:southBoundLatitude>
                        <gco:Decimal>
                          <xsl:value-of select="./dif:Southernmost_Latitude"/>
                        </gco:Decimal>
                      </gmd:southBoundLatitude>
                      <gmd:northBoundLatitude>
                        <gco:Decimal>
                          <xsl:value-of select="./dif:Northernmost_Latitude"/>
                        </gco:Decimal>
                      </gmd:northBoundLatitude>																											
                    </gmd:EX_GeographicBoundingBox>
                  </xsl:for-each>
                </gmd:geographicElement>
              </xsl:when>
              <xsl:when test="dif:Spatial_Coverage/dif:Westernmost_Longitude='' and dif:Spatial_Coverage/dif:Easternmost_Longitude='' and dif:Spatial_Coverage/dif:Southernmost_Latitude='' and dif:Spatial_Coverage/dif:Northernmost_Latitude=''">
                <gmd:geographicElement>
                  <gmd:EX_GeographicDescription>
                    <gmd:geographicIdentifier>
                      <gmd:RS_Identifier>
                        <gmd:code gco:nilReason="missing">
                            <gco:CharacterString/>
                        </gmd:code>
                      </gmd:RS_Identifier>
                    </gmd:geographicIdentifier>
                  </gmd:EX_GeographicDescription>
                </gmd:geographicElement>                  
              </xsl:when>
              <xsl:otherwise>
                <gmd:geographicElement>
                  <gmd:EX_GeographicDescription>
                    <gmd:geographicIdentifier>
                      <gmd:RS_Identifier>
                        <gmd:code gco:nilReason="missing">
                            <gco:CharacterString/>
                        </gmd:code>
                      </gmd:RS_Identifier>
                    </gmd:geographicIdentifier>
                  </gmd:EX_GeographicDescription>
                </gmd:geographicElement>                 
              </xsl:otherwise>
						</xsl:choose>
						
            <xsl:choose>
              <xsl:when test="dif:Temporal_Coverage!=''">
                <xsl:for-each select="dif:Temporal_Coverage">
                  <gmd:temporalElement>
                    <gmd:EX_TemporalExtent>
                      <gmd:extent>
                        <gml:TimePeriod>
                          <xsl:attribute name="gml:id">
                             <xsl:value-of select="generate-id(.)"/>
                          </xsl:attribute>
                          <gml:begin>
                            <gml:TimeInstant>
                              <xsl:attribute name="gml:id">
                                 <xsl:value-of select="generate-id(./dif:Start_Date)"/>
                              </xsl:attribute>
                              <gml:timePosition>
                                <xsl:value-of select="./dif:Start_Date"/>
                              </gml:timePosition>															
                            </gml:TimeInstant>
                          </gml:begin>
                          <gml:end>
                            <gml:TimeInstant>
                              <xsl:choose>
                                <xsl:when test="./dif:Stop_Date!=''">
                                  <xsl:attribute name="gml:id">
                                     <xsl:value-of select="generate-id(./dif:Stop_Date)"/>
                                  </xsl:attribute>
                                </xsl:when>
                                <xsl:when test="./dif:Stop_Date=''">
                                  <xsl:attribute name="gml:id">
                                    <xsl:variable name="uid" select="util:toString(util:randomUUID())"/>
                                    <xsl:value-of select="concat('a',$uid)"/>
                                  </xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:attribute name="gml:id">
                                    <xsl:variable name="uid" select="util:toString(util:randomUUID())"/>
                                    <xsl:value-of select="concat('a',$uid)"/>
                                  </xsl:attribute>                                    
                                </xsl:otherwise>                                    
                              </xsl:choose>
                              <xsl:choose>
                                <xsl:when test="./dif:Stop_Date!=''">
                                  <gml:timePosition>
                                    <xsl:value-of select="./dif:Stop_Date"/>
                                  </gml:timePosition>
                                </xsl:when>
                                <xsl:when test="./dif:Stop_Date=''">
                                   <gml:timePosition>Unknown
                                   </gml:timePosition>
                                </xsl:when>
                                <xsl:otherwise>
                                  <gml:timePosition>Unknown
                                  </gml:timePosition>
                                </xsl:otherwise>
                              </xsl:choose>
                            </gml:TimeInstant>
                          </gml:end>
                        </gml:TimePeriod>
                      </gmd:extent>
                    </gmd:EX_TemporalExtent>
                  </gmd:temporalElement>
                </xsl:for-each>
              </xsl:when>
              <xsl:when test="dif:Temporal_Coverage=''">
                <gmd:temporalElement>
                  <gmd:EX_TemporalExtent>
                    <gmd:extent>
                      <gml:TimePeriod>
                        <xsl:attribute name="gml:id">
                           <xsl:value-of select="generate-id(.)"/>
                        </xsl:attribute>
                        <gml:begin>
                          <gml:TimeInstant>
                            <xsl:attribute name="gml:id">
                              <xsl:variable name="uid" select="util:toString(util:randomUUID())"/>
                              <xsl:value-of select="concat('a',$uid)"/>
                            </xsl:attribute>
                            <gml:timePosition>Unknown
                            </gml:timePosition>															
                          </gml:TimeInstant>
                        </gml:begin>
                        <gml:end>
                          <gml:TimeInstant>
                            <xsl:attribute name="gml:id">
                              <xsl:variable name="uid" select="util:toString(util:randomUUID())"/>
                              <xsl:value-of select="concat('a',$uid)"/>
                            </xsl:attribute>
                            <gml:timePosition>Unknown
                            </gml:timePosition>
                          </gml:TimeInstant>
                        </gml:end>
                      </gml:TimePeriod>
                    </gmd:extent>
                  </gmd:EX_TemporalExtent>
                </gmd:temporalElement>
              </xsl:when>
              <xsl:otherwise>
                <gmd:temporalElement>
                  <gmd:EX_TemporalExtent>
                    <gmd:extent>
                      <gml:TimePeriod>
                        <xsl:attribute name="gml:id">
                           <xsl:value-of select="generate-id(.)"/>
                        </xsl:attribute>
                        <gml:begin>
                          <gml:TimeInstant>
                            <xsl:attribute name="gml:id">
                              <xsl:variable name="uid" select="util:toString(util:randomUUID())"/>
                              <xsl:value-of select="concat('a',$uid)"/>
                            </xsl:attribute>
                            <gml:timePosition>Unknown
                            </gml:timePosition>															
                          </gml:TimeInstant>
                        </gml:begin>
                        <gml:end>
                          <gml:TimeInstant>
                            <xsl:attribute name="gml:id">
                              <xsl:variable name="uid" select="util:toString(util:randomUUID())"/>
                              <xsl:value-of select="concat('a',$uid)"/>
                            </xsl:attribute>
                            <gml:timePosition>Unknown
                            </gml:timePosition>
                          </gml:TimeInstant>
                        </gml:end>
                      </gml:TimePeriod>
                    </gmd:extent>
                  </gmd:EX_TemporalExtent>
                </gmd:temporalElement>
              </xsl:otherwise>                  
            </xsl:choose>              
						
            <xsl:for-each select="dif:Spatial_Coverage">
              <xsl:if test="./dif:Minimum_Altitude!='' or ./dif:Maximum_Altitude!=''">
                <gmd:verticalElement>
                  <gmd:EX_VerticalExtent>
                  
                    <xsl:if test="./dif:Minimum_Altitude!=''">
                      <xsl:for-each select="./dif:Minimum_Altitude">
                        <gmd:minimumValue>
                          <gco:Real>
                            <xsl:value-of select="substring-before(.,' ')"/>
                          </gco:Real>
                        </gmd:minimumValue>
                      </xsl:for-each>
                    </xsl:if>
                    <xsl:if test="./dif:Maximum_Altitude!=''">
                      <xsl:for-each select="./dif:Maximum_Altitude">
                        <gmd:maximumValue>
                          <gco:Real>
                            <xsl:value-of select="substring-before(.,' ')"/>
                          </gco:Real>
                        </gmd:maximumValue>
                      </xsl:for-each>
                    </xsl:if>
                    <xsl:if test="./dif:Minimum_Altitude!=''">
                      <gmd:verticalCRS>
                        <gml:VerticalCRS>
                            <xsl:attribute name="gml:id">
                              <xsl:variable name="uid" select="util:toString(util:randomUUID())"/>
                              <xsl:value-of select="concat('a',$uid)"/>
                            </xsl:attribute>
                          <gml:identifier codeSpace=""/>
                          <gml:scope/>
                          
                            <xsl:if test="substring-after(./dif:Minimum_Altitude,' ')='m' or substring-after(./dif:Minimum_Altitude,' ')='metres' or substring-after(./dif:Minimum_Altitude,' ')='meters'">
                              <gml:verticalCS>
                                <gml:VerticalCS gml:id="epsg-cs-6499">
                                  <gml:identifier codeSpace="EPSG">urn:x-ogc:def:cs:EPSG:6499</gml:identifier>
                                  <gml:name>Vertical CS. Axis: height (H). Orientation: up.  UoM: m.</gml:name>
                                    <gml:axis>                                                        
                                      <gml:CoordinateSystemAxis gml:id="epsg-axis-114" gml:uom="urn:x-ogc:def:uom:EPSG:9001">
                                        <gml:identifier codeSpace="EPSG">urn:x-ogc:def:axis:EPSG:114</gml:identifier>
                                        <gml:name>height</gml:name>
                                        <gml:axisAbbrev>H</gml:axisAbbrev>
                                        <gml:axisDirection codeSpace="EPSG">up</gml:axisDirection>
                                      </gml:CoordinateSystemAxis>
                                     </gml:axis>
                                </gml:VerticalCS>
                              </gml:verticalCS>
                            </xsl:if>
                            
                            <xsl:if test="substring-after(./dif:Minimum_Altitude,' ')='ft' or substring-after(./dif:Minimum_Altitude,' ')='feet'">
                              <gml:verticalCS>
                                <gml:VerticalCS gml:id="epsg-cs-6496">
                                  <gml:identifier codeSpace="EPSG">urn:x-ogc:def:cs:EPSG:6496</gml:identifier>
                                  <gml:name>Vertical CS. Axis: height (H). Orientation: up.  UoM: ft(Br36).</gml:name>
                                    <gml:axis>                                                        
                                      <gml:CoordinateSystemAxis gml:id="epsg-axis-111" gml:uom="urn:x-ogc:def:uom:EPSG:9095">
                                        <gml:identifier codeSpace="EPSG">urn:x-ogc:def:axis:EPSG:111</gml:identifier>
                                        <gml:name>height</gml:name>
                                        <gml:axisAbbrev>H</gml:axisAbbrev>
                                        <gml:axisDirection codeSpace="EPSG">up</gml:axisDirection>
                                      </gml:CoordinateSystemAxis>
                                     </gml:axis>
                                </gml:VerticalCS>
                              </gml:verticalCS>
                            </xsl:if>
                            
                            <gml:verticalDatum>
                              <gml:VerticalDatum>
                                <xsl:attribute name="gml:id">
                                  <xsl:variable name="uid" select="util:toString(util:randomUUID())"/>
                                  <xsl:value-of select="concat('a',$uid)"/>
                                </xsl:attribute>
                                <gml:identifier codeSpace=""/>
                                <gml:scope/>
                              </gml:VerticalDatum>
                            </gml:verticalDatum>
                        </gml:VerticalCRS>
                      </gmd:verticalCRS>
                    </xsl:if>
  
                    <xsl:if test="not(./dif:Minimum_Altitude/text()) and ./dif:Maximum_Altitude!=''">
                      <gmd:verticalCRS>
                        <gml:VerticalCRS>
                            <xsl:attribute name="gml:id">
                              <xsl:variable name="uid" select="util:toString(util:randomUUID())"/>
                              <xsl:value-of select="concat('a',$uid)"/>
                            </xsl:attribute>
                          <gml:identifier codeSpace=""/>
                          <gml:scope/>
                          
                            <xsl:if test="substring-after(./dif:Minimum_Altitude,' ')='m' or substring-after(./dif:Minimum_Altitude,' ')='metres' or substring-after(./dif:Minimum_Altitude,' ')='meters'">
                              <gml:verticalCS>
                                <gml:VerticalCS gml:id="epsg-cs-6499">
                                  <gml:identifier codeSpace="EPSG">urn:x-ogc:def:cs:EPSG:6499</gml:identifier>
                                  <gml:name>Vertical CS. Axis: height (H). Orientation: up.  UoM: m.</gml:name>
                                    <gml:axis>                                                        
                                      <gml:CoordinateSystemAxis gml:id="epsg-axis-114" gml:uom="urn:x-ogc:def:uom:EPSG:9001">
                                        <gml:identifier codeSpace="EPSG">urn:x-ogc:def:axis:EPSG:114</gml:identifier>
                                        <gml:name>height</gml:name>
                                        <gml:axisAbbrev>H</gml:axisAbbrev>
                                        <gml:axisDirection codeSpace="EPSG">up</gml:axisDirection>
                                      </gml:CoordinateSystemAxis>
                                     </gml:axis>
                                </gml:VerticalCS>
                              </gml:verticalCS>
                            </xsl:if>
                            
                            <xsl:if test="substring-after(./dif:Minimum_Altitude,' ')='ft' or substring-after(./dif:Minimum_Altitude,' ')='feet'">
                              <gml:verticalCS>
                                <gml:VerticalCS gml:id="epsg-cs-6496">
                                  <gml:identifier codeSpace="EPSG">urn:x-ogc:def:cs:EPSG:6496</gml:identifier>
                                  <gml:name>Vertical CS. Axis: height (H). Orientation: up.  UoM: ft(Br36).</gml:name>
                                    <gml:axis>                                                        
                                      <gml:CoordinateSystemAxis gml:id="epsg-axis-111" gml:uom="urn:x-ogc:def:uom:EPSG:9095">
                                        <gml:identifier codeSpace="EPSG">urn:x-ogc:def:axis:EPSG:111</gml:identifier>
                                        <gml:name>height</gml:name>
                                        <gml:axisAbbrev>H</gml:axisAbbrev>
                                        <gml:axisDirection codeSpace="EPSG">up</gml:axisDirection>
                                      </gml:CoordinateSystemAxis>
                                     </gml:axis>
                                </gml:VerticalCS>
                              </gml:verticalCS>
                            </xsl:if>
                            
                            <gml:verticalDatum>
                              <gml:VerticalDatum>
                                <xsl:attribute name="gml:id">
                                  <xsl:variable name="uid" select="util:toString(util:randomUUID())"/>
                                  <xsl:value-of select="concat('a',$uid)"/>
                                </xsl:attribute>
                                <gml:identifier codeSpace=""/>
                                <gml:scope/>
                              </gml:VerticalDatum>
                            </gml:verticalDatum>
                        </gml:VerticalCRS>
                      </gmd:verticalCRS>
                    </xsl:if>
                    
                  </gmd:EX_VerticalExtent>
                </gmd:verticalElement>
              </xsl:if>
            </xsl:for-each>
                     
            <xsl:for-each select="dif:Spatial_Coverage">
              <xsl:if test="./dif:Minimum_Depth!='' or ./dif:Maximum_Depth!=''">
                <gmd:verticalElement>
                  <gmd:EX_VerticalExtent>
                 
                    <xsl:if test="./dif:Minimum_Depth!=''">
                      <xsl:for-each select="./dif:Minimum_Depth">
                        <gmd:minimumValue>
                          <gco:Real>
                            <xsl:value-of select="substring-before(.,' ')"/>
                          </gco:Real>
                        </gmd:minimumValue>
                      </xsl:for-each>
                    </xsl:if>
                    <xsl:if test="./dif:Maximum_Depth!=''">
                      <xsl:for-each select="./dif:Maximum_Depth">
                        <gmd:maximumValue>
                          <gco:Real>
                            <xsl:value-of select="substring-before(.,' ')"/>
                          </gco:Real>
                        </gmd:maximumValue>
                      </xsl:for-each>
                    </xsl:if>
                    <xsl:if test="./dif:Minimum_Depth!=''">
                      <gmd:verticalCRS>
                        <gml:VerticalCRS>
                          <xsl:attribute name="gml:id">
                             <xsl:value-of select="generate-id(.)"/>
                          </xsl:attribute>
                          <gml:identifier codeSpace=""/>
                          <gml:scope/>
                          
                            <xsl:if test="substring-after(./dif:Minimum_Depth,' ')='m' or substring-after(./dif:Minimum_Depth,' ')='metres' or substring-after(./dif:Minimum_Depth,' ')='meters'">
                              <gml:verticalCS>
                                <gml:VerticalCS gml:id="epsg-cs-6498">
                                  <gml:identifier codeSpace="EPSG">urn:x-ogc:def:cs:EPSG:6498</gml:identifier>
                                  <gml:name>Vertical CS. Axis: depth (D). Orientation: down. UoM: m.</gml:name>
                                    <gml:axis>                                                        
                                      <gml:CoordinateSystemAxis gml:id="epsg-axis-113" gml:uom="urn:x-ogc:def:uom:EPSG:9001">
                                        <gml:identifier codeSpace="EPSG">urn:x-ogc:def:axis:EPSG:113</gml:identifier>
                                        <gml:name>depth</gml:name>
                                        <gml:axisAbbrev>D</gml:axisAbbrev>
                                        <gml:axisDirection codeSpace="EPSG">down</gml:axisDirection>
                                      </gml:CoordinateSystemAxis>
                                    </gml:axis>
                                </gml:VerticalCS>
                              </gml:verticalCS>
                            </xsl:if>
                            
                            <xsl:if test="substring-after(./dif:Minimum_Depth,' ')='ft' or substring-after(./dif:Minimum_Depth,' ')='feet'">
                              <gml:verticalCS>
                                <gml:VerticalCS gml:id="epsg-cs-6495">
                                  <gml:identifier codeSpace="EPSG">urn:x-ogc:def:cs:EPSG:6495</gml:identifier>
                                  <gml:name>Vertical CS. Axis: depth (D). Orientation: down.  UoM: ft.</gml:name>
                                    <gml:axis>                                                        
                                      <gml:CoordinateSystemAxis gml:id="epsg-axis-214" gml:uom="urn:x-ogc:def:uom:EPSG:9002">
                                        <gml:identifier codeSpace="EPSG">urn:x-ogc:def:axis:EPSG:214</gml:identifier>
                                        <gml:name>depth</gml:name>
                                        <gml:axisAbbrev>D</gml:axisAbbrev>
                                        <gml:axisDirection codeSpace="EPSG">down</gml:axisDirection>
                                      </gml:CoordinateSystemAxis>
                                    </gml:axis>
                                </gml:VerticalCS>
                              </gml:verticalCS>
                            </xsl:if>
                            
                            <gml:verticalDatum>
                              <gml:VerticalDatum>
                                <xsl:attribute name="gml:id">
                                  <xsl:variable name="uid" select="util:toString(util:randomUUID())"/>
                                  <xsl:value-of select="concat('a',$uid)"/>
                                </xsl:attribute>
                                <gml:identifier codeSpace=""/>
                                <gml:scope/>
                              </gml:VerticalDatum>
                            </gml:verticalDatum>
                        </gml:VerticalCRS>
                      </gmd:verticalCRS>
                    </xsl:if>

                    <xsl:if test="not(./dif:Minimum_Depth/text()) and ./dif:Maximum_Depth!=''">
                      <gmd:verticalCRS>
                        <gml:VerticalCRS>
                          <xsl:attribute name="gml:id">
                             <xsl:value-of select="generate-id(.)"/>
                          </xsl:attribute>
                          <gml:identifier codeSpace=""/>
                          <gml:scope/>
                          
                            <xsl:if test="substring-after(./dif:Minimum_Depth,' ')='m' or substring-after(./dif:Minimum_Depth,' ')='metres' or substring-after(./dif:Minimum_Depth,' ')='meters'">
                              <gml:verticalCS>
                                <gml:VerticalCS gml:id="epsg-cs-6498">
                                  <gml:identifier codeSpace="EPSG">urn:x-ogc:def:cs:EPSG:6498</gml:identifier>
                                  <gml:name>Vertical CS. Axis: depth (D). Orientation: down. UoM: m.</gml:name>
                                    <gml:axis>                                                        
                                      <gml:CoordinateSystemAxis gml:id="epsg-axis-113" gml:uom="urn:x-ogc:def:uom:EPSG:9001">
                                        <gml:identifier codeSpace="EPSG">urn:x-ogc:def:axis:EPSG:113</gml:identifier>
                                        <gml:name>depth</gml:name>
                                        <gml:axisAbbrev>D</gml:axisAbbrev>
                                        <gml:axisDirection codeSpace="EPSG">down</gml:axisDirection>
                                      </gml:CoordinateSystemAxis>
                                     </gml:axis>
                                </gml:VerticalCS>
                              </gml:verticalCS>
                            </xsl:if>
                            
                            <xsl:if test="substring-after(./dif:Minimum_Depth,' ')='ft' or substring-after(./dif:Minimum_Depth,' ')='feet'">
                              <gml:verticalCS>
                                <gml:VerticalCS gml:id="epsg-cs-6495">
                                  <gml:identifier codeSpace="EPSG">urn:x-ogc:def:cs:EPSG:6495</gml:identifier>
                                  <gml:name>Vertical CS. Axis: depth (D). Orientation: down.  UoM: ft.</gml:name>
                                    <gml:axis>                                                        
                                      <gml:CoordinateSystemAxis gml:id="epsg-axis-214" gml:uom="urn:x-ogc:def:uom:EPSG:9002">
                                        <gml:identifier codeSpace="EPSG">urn:x-ogc:def:axis:EPSG:214</gml:identifier>
                                        <gml:name>depth</gml:name>
                                        <gml:axisAbbrev>D</gml:axisAbbrev>
                                        <gml:axisDirection codeSpace="EPSG">down</gml:axisDirection>
                                      </gml:CoordinateSystemAxis>
                                     </gml:axis>
                                </gml:VerticalCS>
                              </gml:verticalCS>
                            </xsl:if>
                            
                            <gml:verticalDatum>
                              <gml:VerticalDatum>
                                <xsl:attribute name="gml:id">
                                  <xsl:variable name="uid" select="util:toString(util:randomUUID())"/>
                                  <xsl:value-of select="concat('a',$uid)"/>
                                </xsl:attribute>
                                <gml:identifier codeSpace=""/>
                                <gml:scope/>
                              </gml:VerticalDatum>
                            </gml:verticalDatum>
                        </gml:VerticalCRS>
                      </gmd:verticalCRS>
                    </xsl:if>
                 
                  </gmd:EX_VerticalExtent>
                </gmd:verticalElement>
              </xsl:if>
            </xsl:for-each>
						
					</gmd:EX_Extent>
				</gmd:extent>
				
				<xsl:if test="dif:Reference!=''">
          <gmd:supplementalInformation>
            <gco:CharacterString>
              <xsl:value-of select="dif:Reference"/>
            </gco:CharacterString>
          </gmd:supplementalInformation>
				</xsl:if>				
				
			</gmd:MD_DataIdentification>
		</gmd:identificationInfo>		
		
		<gmd:distributionInfo>
			<gmd:MD_Distribution>
			
        <xsl:choose>
          <xsl:when test="dif:Distribution_Format!=''">
            <xsl:for-each select="dif:Distribution">
              <gmd:distributionFormat>
                <gmd:MD_Format>
                  <gmd:name>
                    <gco:CharacterString>
                      <xsl:value-of select="./dif:Distribution_Format"/>
                    </gco:CharacterString>
                  </gmd:name>
                  <gmd:version gco:nilReason="missing">
                    <gco:CharacterString/>
                  </gmd:version>
                </gmd:MD_Format>
              </gmd:distributionFormat>
            </xsl:for-each>
          </xsl:when>
          <xsl:when test="dif:Distribution_Format=''">
            <gmd:distributionFormat>
              <gmd:MD_Format>
                <gmd:name gco:nilReason="missing">
                  <gco:CharacterString/>
                </gmd:name>
                <gmd:version gco:nilReason="missing">
                  <gco:CharacterString/>
                </gmd:version>
              </gmd:MD_Format>
            </gmd:distributionFormat>              
          </xsl:when>
          <xsl:otherwise>
            <gmd:distributionFormat>
              <gmd:MD_Format>
                <gmd:name gco:nilReason="missing">
                  <gco:CharacterString/>
                </gmd:name>
                <gmd:version gco:nilReason="missing">
                  <gco:CharacterString/>
                </gmd:version>
              </gmd:MD_Format>
            </gmd:distributionFormat>              
          </xsl:otherwise>
        </xsl:choose>					
			
				<xsl:for-each select="dif:Data_Center">
					<xsl:for-each select="dif:Personnel/dif:Role">
              <gmd:distributor>
                <gmd:MD_Distributor>
								<gmd:distributorContact>
									<gmd:CI_ResponsibleParty>
										<gmd:individualName>
											<gco:CharacterString>
												<xsl:value-of select="../dif:First_Name"/>, <xsl:value-of select="../dif:Last_Name"/>
											</gco:CharacterString>
										</gmd:individualName>
										<gmd:organisationName>
											<gco:CharacterString>
												<xsl:value-of select="../../dif:Data_Center_Name/dif:Short_Name"/> | <xsl:value-of select="../../dif:Data_Center_Name/dif:Long_Name"/>
											</gco:CharacterString>
										</gmd:organisationName>
										<gmd:positionName>
											<gco:CharacterString>
												<xsl:value-of select="."/>
											</gco:CharacterString>
										</gmd:positionName>

                    <xsl:if test="../dif:Phone!='' or ../dif:Fax!='' or ../dif:Contact_Address/dif:Address!='' or ../dif:Contact_Address/dif:City!='' or ../dif:Contact_Address/dif:Province_or_State!='' or ../dif:Contact_Address/dif:Postal_Code!='' or ../dif:Contact_Address/dif:Country!='' or ../dif:Email!='' or ../../dif:Data_Center_URL!=''">
                
                      <gmd:contactInfo>
                        <gmd:CI_Contact>

                          <xsl:if test="../dif:Phone!='' or ../dif:Fax!=''">
                            <gmd:phone>
                              <gmd:CI_Telephone>
                                <xsl:for-each select="../dif:Phone">
                                  <gmd:voice>
                                    <gco:CharacterString>
                                        <xsl:value-of select="."/>
                                    </gco:CharacterString>
                                  </gmd:voice>
                                 </xsl:for-each>
                                 <xsl:for-each select="../dif:Fax">
                                  <gmd:facsimile>
                                    <gco:CharacterString>
                                        <xsl:value-of select="."/>
                                    </gco:CharacterString>
                                  </gmd:facsimile>
                                 </xsl:for-each>                       
                              </gmd:CI_Telephone>
                            </gmd:phone>
                          </xsl:if>
                          
                          <xsl:if test="../dif:Contact_Address/dif:Address!='' or ../dif:Contact_Address/dif:City!='' or ../dif:Contact_Address/dif:Province_or_State!='' or ../dif:Contact_Address/dif:Postal_Code!='' or ../dif:Contact_Address/dif:Country!='' or ../dif:Email!='' or ../../dif:Data_Center_URL!=''">
                            <gmd:address>
                              <gmd:CI_Address>
                                <xsl:for-each select="../dif:Contact_Address/dif:Address">
                                  <gmd:deliveryPoint>
                                    <gco:CharacterString>
                                      <xsl:value-of select="."/>
                                    </gco:CharacterString>
                                  </gmd:deliveryPoint>
                                </xsl:for-each>
                                <xsl:if test="../dif:Contact_Address/dif:City!=''">
                                  <gmd:city>
                                    <gco:CharacterString>
                                      <xsl:value-of select="../dif:Contact_Address/dif:City"/>
                                    </gco:CharacterString>
                                  </gmd:city>
                                </xsl:if>
                                <xsl:if test="../dif:Contact_Address/dif:Province_or_State!=''">
                                  <gmd:administrativeArea>
                                    <gco:CharacterString>
                                      <xsl:value-of select="../dif:Contact_Address/dif:Province_or_State"/>
                                    </gco:CharacterString>
                                  </gmd:administrativeArea>
                                </xsl:if>
                                <xsl:if test="../dif:Contact_Address/dif:Postal_Code!=''">
                                  <gmd:postalCode>
                                    <gco:CharacterString>
                                      <xsl:value-of select="../dif:Contact_Address/dif:Postal_Code"/>
                                    </gco:CharacterString>
                                  </gmd:postalCode>
                                </xsl:if>
                                <xsl:if test="../dif:Contact_Address/dif:Country!=''">
                                  <gmd:country>
                                    <gco:CharacterString>
                                      <xsl:value-of select="../dif:Contact_Address/dif:Country"/>
                                    </gco:CharacterString>
                                  </gmd:country>
                                </xsl:if>
                                <xsl:for-each select="../dif:Email">
                                 <gmd:electronicMailAddress>
                                   <gco:CharacterString>
                                       <xsl:value-of select="."/>
                                   </gco:CharacterString>
                                 </gmd:electronicMailAddress>
                                </xsl:for-each>
                              </gmd:CI_Address>
                            </gmd:address>
                          </xsl:if>
                                
                          <xsl:if test="../../dif:Data_Center_URL!=''">
                            <gmd:onlineResource>
                              <gmd:CI_OnlineResource>
                                <gmd:linkage>
                                  <gmd:URL>
                                    <xsl:value-of select="../../dif:Data_Center_URL"/>
                                  </gmd:URL>
                                </gmd:linkage>
                              </gmd:CI_OnlineResource>
                            </gmd:onlineResource>
                          </xsl:if>
                          
                        </gmd:CI_Contact>
                      </gmd:contactInfo>
                    
                    </xsl:if>

										<gmd:role>
											<gmd:CI_RoleCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_RoleCode" codeListValue="distributor"/>
										</gmd:role>
									</gmd:CI_ResponsibleParty>
								</gmd:distributorContact>
						
                <xsl:if test="../../../dif:Distribution/dif:Fees!='' or ../../../dif:Data_Set_Citation/dif:Dataset_Release_Date!=''">                    
                  <xsl:for-each select="../../../dif:Distribution">
                    <gmd:distributionOrderProcess>
                      <gmd:MD_StandardOrderProcess>
                        <xsl:if test="./dif:Fees!=''">
                          <gmd:fees>
                            <gco:CharacterString>
                              <xsl:value-of select="./dif:Fees"/>
                            </gco:CharacterString>
                          </gmd:fees>
                        </xsl:if>
                        <xsl:if test="../dif:Data_Set_Citation/dif:Dataset_Release_Date!=''">
                          <gmd:plannedAvailableDateTime>
                            <gco:DateTime>
                              <xsl:value-of select="../dif:Data_Set_Citation/dif:Dataset_Release_Date"/>T12:00:00
                            </gco:DateTime>
                          </gmd:plannedAvailableDateTime>
                        </xsl:if>
                      </gmd:MD_StandardOrderProcess>
                    </gmd:distributionOrderProcess>
                  </xsl:for-each>
                </xsl:if>
                  
                </gmd:MD_Distributor>
              </gmd:distributor>
					</xsl:for-each>
				</xsl:for-each>
						
				<xsl:if test="dif:Distribution/dif:Distribution_Size!='' or dif:Related_URL!='' or dif:Multimedia_Sample/dif:URL!='' or dif:Data_Set_Citation/dif:Online_Resource!='' or dif:Distribution/dif:Distribution_Media!=''">


          <gmd:transferOptions>
            <gmd:MD_DigitalTransferOptions>
            
              <xsl:if test="dif:Distribution/dif:Distribution_Size!=''">
                <xsl:for-each select="dif:Distribution/dif:Distribution_Size">
                  <gmd:unitsOfDistribution>
                    <gco:CharacterString>
                      <xsl:value-of select="substring-after(.,' ')"/>
                    </gco:CharacterString>
                  </gmd:unitsOfDistribution>
                  <gmd:transferSize>
                    <gco:Real>
                      <xsl:value-of select="substring-before(.,' ')"/>
                    </gco:Real>
                  </gmd:transferSize>
                </xsl:for-each>
              </xsl:if>
              
              <xsl:if test="dif:Related_URL!=''">
              
                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='ECHO'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='EDG'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='EOSDIS DATA POOL'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='GIOVANNI'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='LAADS'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='LAS'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='MIRADOR'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='MODAPS'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='NOMADS'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='OPENDAP DATA (DODS)'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='OPENDAP DIRECTORY (DODS)'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='THREDDS CATALOG'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='THREDDS DATA'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='THREDDS DIRECTORY'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='WHOM'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='WIST'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Type">
                    <xsl:if test=".='GET DATA'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Type">
                    <xsl:if test=".='GET RELATED DATA SET METADATA (DIF)'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Type">
                    <xsl:if test=".='GET RELATED SERVICE METADATA (SERF)'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='ACCESS MAP VIEWER'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>OGC:WMS-1.1.1-http-get-map</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='ACCESS WEB SERVICE'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='GET MAP SERVICE'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>OGC:WMS-1.1.1-http-get-map</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='GET SOFTWARE PACKAGE'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='GET WEB COVERAGE SERVICE (WCS)'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='GET WEB FEATURE SERVICE (WFS)'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='GET WEB MAP SERVICE (WMS)'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>OGC:WMS-1.1.1-http-get-map</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='GET WORKFLOW (SERVICE CHAIN)'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Type">
                    <xsl:if test=".='GET SERVICE'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>													
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Type">
                    <xsl:if test=".='VIEW EXTENDED METADATA'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>													
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Type">
                    <xsl:if test=".='VIEW PROJECT HOME PAGE'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>													
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='PRODUCT HISTORY'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Subtype">
                    <xsl:if test=".='USER''S GUIDE'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/> | <xsl:value-of select="../../dif:URL_Content_Type/dif:Subtype"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="dif:Related_URL/dif:URL_Content_Type">
                  <xsl:for-each select="dif:Type">
                    <xsl:if test=".='VIEW RELATED INFORMATION'">
                      <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage>
                            <gmd:URL>
                              <xsl:value-of select="../../dif:URL"/>
                            </gmd:URL>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:URL_Content_Type/dif:Type"/>
                            </gco:CharacterString>
                          </gmd:name>
                          <gmd:description>
                            <gco:CharacterString>
                              <xsl:value-of select="../../dif:Description"/>
                            </gco:CharacterString>
                          </gmd:description>													
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:for-each>
                
              </xsl:if>
              
              <xsl:choose>
                <xsl:when test="dif:Related_URL">
                </xsl:when>
                <xsl:otherwise>
                  <xsl:if test="dif:Distribution/dif:Distribution_Media!=''">
                    <xsl:if test="dif:Distribution/dif:Distribution_Media='HTTP' or dif:Distribution/dif:Distribution_Media='FTP'">
                     <gmd:onLine>
                        <gmd:CI_OnlineResource>
                          <gmd:linkage gco:nilReason="missing">
                            <gmd:URL/>
                          </gmd:linkage>
                          <gmd:protocol>
                            <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                          </gmd:protocol>
                          <gmd:name gco:nilReason="missing">
                            <gco:CharacterString/>
                          </gmd:name>
                          <gmd:description gco:nilReason="missing">
                            <gco:CharacterString/>
                          </gmd:description>													
                        </gmd:CI_OnlineResource>
                      </gmd:onLine>                        
                    </xsl:if>
                  </xsl:if>
                </xsl:otherwise>
              </xsl:choose>              
              
              <xsl:if test="dif:Multimedia_Sample/dif:URL!=''">
                <xsl:for-each select="dif:Multimedia_Sample">
                  <gmd:onLine>
                    <gmd:CI_OnlineResource>
                      <gmd:linkage>
                        <gmd:URL>
                          <xsl:value-of select="./dif:URL"/>
                        </gmd:URL>
                      </gmd:linkage>
                      <gmd:protocol>
                        <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                      </gmd:protocol>
                      <gmd:name>
                        <gco:CharacterString>Multimedia Sample</gco:CharacterString>
                      </gmd:name>
                      <gmd:description>
                        <gco:CharacterString>
                          <xsl:value-of select="./dif:Description"/>
                        </gco:CharacterString>
                      </gmd:description>													
                    </gmd:CI_OnlineResource>
                  </gmd:onLine>
                </xsl:for-each>
              </xsl:if>
              
              <xsl:if test="dif:Data_Set_Citation/dif:Online_Resource!=''">
                <xsl:for-each select="dif:Data_Set_Citation">
                  <gmd:onLine>
                    <gmd:CI_OnlineResource>
                      <gmd:linkage>
                        <gmd:URL>
                          <xsl:value-of select="./dif:Online_Resource"/>
                        </gmd:URL>
                      </gmd:linkage>
                      <gmd:protocol>
                        <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
                      </gmd:protocol>
                      <gmd:name>
                        <gco:CharacterString>Data Set Citation</gco:CharacterString>
                      </gmd:name>
                      <gmd:description>
                        <gco:CharacterString>
                          <xsl:value-of select="./dif:Dataset_Title"/>
                        </gco:CharacterString>
                      </gmd:description>													
                    </gmd:CI_OnlineResource>
                  </gmd:onLine>
                </xsl:for-each>
              </xsl:if>
              
              <xsl:if test="dif:Distribution/dif:Distribution_Media!=''">
              
                <xsl:for-each select="dif:Distribution/dif:Distribution_Media">
                  <xsl:if test=".='HTTP' or .='FTP'">
                    <gmd:offLine>
                      <gmd:MD_Medium>
                        <gmd:name>
                          <gmd:MD_MediumNameCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#MD_MediumNameCode" codeListValue="onLine"/>
                        </gmd:name>
                      </gmd:MD_Medium>
                    </gmd:offLine>
                  </xsl:if>
                </xsl:for-each>
                
                <xsl:for-each select="dif:Distribution/dif:Distribution_Media">
                  <xsl:if test=".='CD'">
                    <gmd:offLine>
                      <gmd:MD_Medium>
                        <gmd:name>
                          <gmd:MD_MediumNameCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#MD_MediumNameCode" codeListValue="cdRom"/>
                        </gmd:name>
                      </gmd:MD_Medium>
                    </gmd:offLine>
                  </xsl:if>
                </xsl:for-each>
                
                <xsl:for-each select="dif:Distribution/dif:Distribution_Media">
                  <xsl:if test=".='DVD'">
                    <gmd:offLine>
                      <gmd:MD_Medium>
                        <gmd:name>
                          <gmd:MD_MediumNameCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#MD_MediumNameCode" codeListValue="dvd"/>
                        </gmd:name>
                      </gmd:MD_Medium>
                    </gmd:offLine>
                  </xsl:if>
                </xsl:for-each>
              
              </xsl:if>
              
            </gmd:MD_DigitalTransferOptions>
          </gmd:transferOptions>
				
				</xsl:if>												
						
			</gmd:MD_Distribution>
		</gmd:distributionInfo>
		
		<gmd:dataQualityInfo>
			<gmd:DQ_DataQuality>
				<gmd:scope>
					<gmd:DQ_Scope>
						<gmd:level>
							<gmd:MD_ScopeCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#MD_ScopeCode" codeListValue="dataset"/>
						</gmd:level>
					</gmd:DQ_Scope>
				</gmd:scope>
				
				<xsl:if test="dif:Data_Resolution!=''">
				
          <xsl:for-each select="dif:Data_Resolution/dif:Temporal_Resolution">
            <gmd:report>
              <gmd:DQ_AccuracyOfATimeMeasurement>
                <gmd:nameOfMeasure>
                  <gco:CharacterString>Temporal Resolution</gco:CharacterString>
                </gmd:nameOfMeasure>
                <gmd:result>
                  <gmd:DQ_QuantitativeResult>
                    <gmd:valueUnit>
                      <gml:DerivedUnit>
                        <xsl:attribute name="gml:id">
                           <xsl:value-of select="generate-id(.)"/>
                        </xsl:attribute>
                        <gml:identifier codeSpace=""/>
                        <gml:derivationUnitTerm uom="{substring-after(.,' ')}"/>
                      </gml:DerivedUnit>
                    </gmd:valueUnit>
                    <gmd:value>
                      <gco:Record>
                        <xsl:value-of select="substring-before(.,' ')"/>
                      </gco:Record>
                    </gmd:value>
                  </gmd:DQ_QuantitativeResult>
                </gmd:result>
              </gmd:DQ_AccuracyOfATimeMeasurement>
            </gmd:report>
          </xsl:for-each>
          
          <xsl:for-each select="dif:Data_Resolution/dif:Latitude_Resolution">
            <gmd:report>
              <gmd:DQ_AbsoluteExternalPositionalAccuracy>
                <gmd:nameOfMeasure>
                  <gco:CharacterString>Latitude Resolution</gco:CharacterString>
                </gmd:nameOfMeasure>
                <gmd:result>
                  <gmd:DQ_QuantitativeResult>
                    <gmd:valueUnit>
                      <gml:DerivedUnit>
                        <xsl:attribute name="gml:id">
                           <xsl:value-of select="generate-id(.)"/>
                        </xsl:attribute>
                        <gml:identifier codeSpace=""/>
                        <gml:derivationUnitTerm uom="{substring-after(.,' ')}"/>
                      </gml:DerivedUnit>
                    </gmd:valueUnit>
                    <gmd:value>
                      <gco:Record>
                        <xsl:value-of select="substring-before(.,' ')"/>
                      </gco:Record>
                    </gmd:value>
                  </gmd:DQ_QuantitativeResult>
                </gmd:result>
              </gmd:DQ_AbsoluteExternalPositionalAccuracy>
            </gmd:report>
          </xsl:for-each>
          
          <xsl:for-each select="dif:Data_Resolution/dif:Longitude_Resolution">
            <gmd:report>
              <gmd:DQ_AbsoluteExternalPositionalAccuracy>
                <gmd:nameOfMeasure>
                  <gco:CharacterString>Longitude Resolution</gco:CharacterString>
                </gmd:nameOfMeasure>
                <gmd:result>
                  <gmd:DQ_QuantitativeResult>
                    <gmd:valueUnit>
                      <gml:DerivedUnit>
                        <xsl:attribute name="gml:id">
                           <xsl:value-of select="generate-id(.)"/>
                        </xsl:attribute>
                        <gml:identifier codeSpace=""/>
                        <gml:derivationUnitTerm uom="{substring-after(.,' ')}"/>
                      </gml:DerivedUnit>
                    </gmd:valueUnit>
                    <gmd:value>
                      <gco:Record>
                        <xsl:value-of select="substring-before(.,' ')"/>
                      </gco:Record>
                    </gmd:value>
                  </gmd:DQ_QuantitativeResult>
                </gmd:result>
              </gmd:DQ_AbsoluteExternalPositionalAccuracy>
            </gmd:report>
          </xsl:for-each>
          
          <xsl:for-each select="dif:Data_Resolution/dif:Vertical_Resolution">
            <gmd:report>
              <gmd:DQ_AbsoluteExternalPositionalAccuracy>
                <gmd:nameOfMeasure>
                  <gco:CharacterString>Vertical Resolution</gco:CharacterString>
                </gmd:nameOfMeasure>
                <gmd:result>
                  <gmd:DQ_QuantitativeResult>
                    <gmd:valueUnit>
                      <gml:DerivedUnit>
                        <xsl:attribute name="gml:id">
                           <xsl:value-of select="generate-id(.)"/>
                        </xsl:attribute>
                        <gml:identifier codeSpace=""/>
                        <gml:derivationUnitTerm uom="{substring-after(.,' ')}"/>
                      </gml:DerivedUnit>
                    </gmd:valueUnit>
                    <gmd:value>
                      <gco:Record>
                        <xsl:value-of select="substring-before(.,' ')"/>
                      </gco:Record>
                    </gmd:value>
                  </gmd:DQ_QuantitativeResult>
                </gmd:result>
              </gmd:DQ_AbsoluteExternalPositionalAccuracy>
            </gmd:report>
          </xsl:for-each>
				
				</xsl:if>
				
				<xsl:choose>
          <xsl:when test="dif:Quality!=''">
            <gmd:lineage>
              <gmd:LI_Lineage>
                <gmd:statement>
                  <gco:CharacterString>
                    <xsl:value-of select="dif:Quality"/>
                  </gco:CharacterString>
                </gmd:statement>
              </gmd:LI_Lineage>
            </gmd:lineage>
          </xsl:when>
          <xsl:when test="dif:Quality=''">
            <gmd:lineage>
              <gmd:LI_Lineage>
                <gmd:statement gco:nilReason="missing">
                  <gco:CharacterString/>
                </gmd:statement>
              </gmd:LI_Lineage>
            </gmd:lineage>              
          </xsl:when>
          <xsl:otherwise>
            <gmd:lineage>
              <gmd:LI_Lineage>
                <gmd:statement gco:nilReason="missing">
                  <gco:CharacterString/>
                </gmd:statement>
              </gmd:LI_Lineage>
            </gmd:lineage>              
          </xsl:otherwise>
				</xsl:choose>
				
			</gmd:DQ_DataQuality>
		</gmd:dataQualityInfo>
		
		<xsl:choose>
      <xsl:when test="dif:Private">
      
        <xsl:for-each select="dif:Private">
        
          <xsl:if test=".='True' or .='TRUE'">
            <gmd:metadataConstraints>
              <gmd:MD_LegalConstraints>
                <gmd:useLimitation>
                  <gco:CharacterString>This metadata record is publicly available.</gco:CharacterString>
                </gmd:useLimitation>
              </gmd:MD_LegalConstraints>
            </gmd:metadataConstraints>
          </xsl:if>
        
          <xsl:if test=".='False' or .='FALSE'">
            <gmd:metadataConstraints>
              <gmd:MD_LegalConstraints>
                <gmd:useLimitation>
                  <gco:CharacterString>This metadata record is not publicly available.</gco:CharacterString>
                </gmd:useLimitation>
              </gmd:MD_LegalConstraints>
            </gmd:metadataConstraints>
          </xsl:if>
        
        </xsl:for-each>
        
      </xsl:when>
      
      <xsl:otherwise>
        <gmd:metadataConstraints>
          <gmd:MD_LegalConstraints>
            <gmd:useLimitation>
              <gco:CharacterString>This metadata record is publicly available.</gco:CharacterString>
            </gmd:useLimitation>
          </gmd:MD_LegalConstraints>
        </gmd:metadataConstraints>          
      </xsl:otherwise>
		
		</xsl:choose>
		
		<gmd:metadataMaintenance>
			<gmd:MD_MaintenanceInformation>
			
				<gmd:maintenanceAndUpdateFrequency>
					<gmd:MD_MaintenanceFrequencyCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#MD_MaintenanceFrequencyCode" codeListValue="asNeeded"/>
				</gmd:maintenanceAndUpdateFrequency>
				
				<xsl:if test="dif:Future_DIF_Review_Date!=''">
            <gmd:dateOfNextUpdate>
              <gco:Date>
                <xsl:value-of select="dif:Future_DIF_Review_Date"/>
              </gco:Date>
            </gmd:dateOfNextUpdate>
				</xsl:if>
				
				<xsl:if test="dif:DIF_Revision_History!=''">
          <gmd:maintenanceNote>
            <gco:CharacterString>
              <xsl:value-of select="dif:DIF_Revision_History"/>
            </gco:CharacterString>
          </gmd:maintenanceNote>
				</xsl:if>
			
				<xsl:for-each select="dif:Personnel">
					<xsl:for-each select="dif:Role">
						<xsl:if test=".='DIF AUTHOR'">
							<gmd:contact>
								<gmd:CI_ResponsibleParty>
									<gmd:individualName>
										<gco:CharacterString>
											<xsl:value-of select="../dif:Last_Name"></xsl:value-of>, <xsl:value-of select="../dif:First_Name"></xsl:value-of>
										</gco:CharacterString>
									</gmd:individualName>

									<xsl:if test="../dif:Phone!='' or ../dif:Fax!='' or ../dif:Contact_Address/dif:Address!='' or ../dif:Contact_Address/dif:City!='' or ../dif:Contact_Address/dif:Province_or_State!='' or ../dif:Contact_Address/dif:Postal_Code!='' or ../dif:Contact_Address/dif:Country!='' or ../dif:Email!=''">
									
                    <gmd:contactInfo>
                      <gmd:CI_Contact>
                      
                        <xsl:if test="../dif:Phone!='' or ../dif:Fax!=''">
                          <gmd:phone>
                            <gmd:CI_Telephone>
                              <xsl:for-each select="../dif:Phone">
                                <gmd:voice>
                                  <gco:CharacterString>
                                      <xsl:value-of select="."/>
                                  </gco:CharacterString>
                                </gmd:voice>
                               </xsl:for-each>
                               <xsl:for-each select="../dif:Fax">
                                <gmd:facsimile>
                                  <gco:CharacterString>
                                      <xsl:value-of select="."/>
                                  </gco:CharacterString>
                                </gmd:facsimile>
                               </xsl:for-each>                       
                            </gmd:CI_Telephone>
                          </gmd:phone>
                        </xsl:if>
                        
                        <xsl:if test="../dif:Contact_Address/dif:Address!='' or ../dif:Contact_Address/dif:City!='' or ../dif:Contact_Address/dif:Province_or_State!='' or ../dif:Contact_Address/dif:Postal_Code!='' or ../dif:Contact_Address/dif:Country!='' or ../dif:Email!=''">
                          <gmd:address>
                            <gmd:CI_Address>
                              <xsl:for-each select="../dif:Contact_Address/dif:Address">
                                <gmd:deliveryPoint>
                                  <gco:CharacterString>
                                    <xsl:value-of select="."/>
                                  </gco:CharacterString>
                                </gmd:deliveryPoint>
                              </xsl:for-each>
                              <xsl:if test="../dif:Contact_Address/dif:City!=''">
                                <gmd:city>
                                  <gco:CharacterString>
                                    <xsl:value-of select="../dif:Contact_Address/dif:City"/>
                                  </gco:CharacterString>
                                </gmd:city>
                              </xsl:if>
                              <xsl:if test="../dif:Contact_Address/dif:Province_or_State!=''">
                                <gmd:administrativeArea>
                                  <gco:CharacterString>
                                    <xsl:value-of select="../dif:Contact_Address/dif:Province_or_State"/>
                                  </gco:CharacterString>
                                </gmd:administrativeArea>
                              </xsl:if>
                              <xsl:if test="../dif:Contact_Address/dif:Postal_Code!=''">
                                <gmd:postalCode>
                                  <gco:CharacterString>
                                    <xsl:value-of select="../dif:Contact_Address/dif:Postal_Code"/>
                                  </gco:CharacterString>
                                </gmd:postalCode>
                              </xsl:if>
                              <xsl:if test="../dif:Contact_Address/dif:Country!=''">
                                <gmd:country>
                                  <gco:CharacterString>
                                    <xsl:value-of select="../dif:Contact_Address/dif:Country"/>
                                  </gco:CharacterString>
                                </gmd:country>
                              </xsl:if>
                              <xsl:for-each select="../dif:Email">
                               <gmd:electronicMailAddress>
                                 <gco:CharacterString>
                                     <xsl:value-of select="."/>
                                 </gco:CharacterString>
                               </gmd:electronicMailAddress>
                              </xsl:for-each>
                            </gmd:CI_Address>
                          </gmd:address>
                        </xsl:if>
                        
                      </gmd:CI_Contact>
                    </gmd:contactInfo>
									
									</xsl:if>
											
									<gmd:role>
										<gmd:CI_RoleCode codeList="http://asdd.ga.gov.au/asdd/profileinfo/gmxCodelists.xml#CI_RoleCode" codeListValue="author"/>
									</gmd:role>
								</gmd:CI_ResponsibleParty>
							</gmd:contact>
						</xsl:if>
					</xsl:for-each>
				</xsl:for-each>
				
			</gmd:MD_MaintenanceInformation>
		</gmd:metadataMaintenance>
	</gmd:MD_Metadata>
			
			
			
	</xsl:template>
	<!-- ====================================================== -->
  
</xsl:stylesheet>
