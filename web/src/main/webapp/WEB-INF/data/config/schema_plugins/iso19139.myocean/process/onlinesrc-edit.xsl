<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to update metadata for a service and 
attached it to the metadata for data.
-->
<xsl:stylesheet version="2.0" xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gts="http://www.isotc211.org/2005/gts"
    xmlns:gml="http://www.opengis.net/gml" xmlns:srv="http://www.isotc211.org/2005/srv"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:date="http://exslt.org/dates-and-times">

    <!-- ============================================================================= -->

    <xsl:param name="uuidref"/>
    <xsl:param name="extra_metadata_uuid"/>
    <xsl:param name="protocol" select="'OGC:WMS-1.1.1-http-get-map'"/>
    <xsl:param name="url"/>
    <xsl:param name="name"/>
    <xsl:param name="desc"/>
    <xsl:param name="serviceDescr"/>
    <xsl:param name="serviceName"/>
    <xsl:param name="oldUrl"/>
    <xsl:param name="oldName"/>
    <xsl:param name="oldProtocol"/>
    
    <!-- ============================================================================= -->

    <xsl:template match="/gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of
                select="gmd:fileIdentifier|
            gmd:language|
            gmd:characterSet|
            gmd:parentIdentifier|
            gmd:hierarchyLevel|
            gmd:hierarchyLevelName|
            gmd:contact|
            gmd:dateStamp|
            gmd:metadataStandardName|
            gmd:metadataStandardVersion|
            gmd:dataSetURI|
            gmd:locale|
            gmd:spatialRepresentationInfo|
            gmd:referenceSystemInfo|
            gmd:metadataExtensionInfo|
            gmd:identificationInfo|
            gmd:contentInfo"/>

            <xsl:choose>
                <xsl:when
                    test="gmd:identificationInfo/srv:SV_ServiceIdentification|
                gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']">
                    <xsl:copy-of select="gmd:distributionInfo"/>
                </xsl:when>
                <!-- In a dataset add a link in the distribution section -->
                <xsl:otherwise>
                    <gmd:distributionInfo>
                        <gmd:MD_Distribution>
                            <xsl:copy-of
                                select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat"/>
                            
                            <!-- Copy only resource with a URL different from one sent (to replace the value) -->
                            <xsl:copy-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor[gmd:MD_Distributor//gmd:onLine[1]/gmd:CI_OnlineResource/gmd:linkage/gmd:URL != $oldUrl 
                                    or gmd:MD_Distributor//gmd:onLine[1]/gmd:CI_OnlineResource/gmd:name/gco:CharacterString != $oldName
                                    or gmd:MD_Distributor//gmd:onLine[1]/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString != $oldProtocol]"/>
                            <!-- Copy non existing ressource attached to one responsible party  -->
                            <xsl:for-each select="//extra/gmd:MD_Metadata">
                                <xsl:call-template name="onlinecopy"/>
                            </xsl:for-each>
                
                            <xsl:if test="$url">
                                <xsl:choose>
                					<xsl:when test="$protocol != 'OGC:WMS'">
                                    <gmd:distributor>
                                        <gmd:MD_Distributor>
                                        	<xsl:copy-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact[..//gmd:onLine[1]/gmd:CI_OnlineResource/gmd:linkage/gmd:URL = $oldUrl 
                                        		and ..//gmd:onLine[1]/gmd:CI_OnlineResource/gmd:name/gco:CharacterString = $oldName
                                        		and ..//gmd:onLine[1]/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString = $oldProtocol]"/>
                                        	<xsl:copy-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorFormat[..//gmd:onLine[1]/gmd:CI_OnlineResource/gmd:linkage/gmd:URL = $oldUrl 
                                        		and ..//gmd:onLine[1]/gmd:CI_OnlineResource/gmd:name/gco:CharacterString = $oldName
                                        		and ..//gmd:onLine[1]/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString = $oldProtocol]"/>
                                        	
                                            <gmd:distributorTransferOptions>
                                                <gmd:MD_DigitalTransferOptions>
                                                    <gmd:onLine>
                                                        <xsl:if test="$uuidref">
                                                            <xsl:attribute name="uuidref" select="$uuidref"/>
                                                        </xsl:if>
                                                        <gmd:CI_OnlineResource>
                                                            <gmd:linkage>
                                                                <gmd:URL>
                                                                <xsl:value-of select="$url"/>
                                                                </gmd:URL>
                                                            </gmd:linkage>
                                                            <gmd:protocol>
                                                                <gco:CharacterString>
                                                                <xsl:value-of select="$protocol"/>
                                                                </gco:CharacterString>
                                                            </gmd:protocol>
                                                            <gmd:name>
                                                                <gco:CharacterString>
                                                                <xsl:value-of select="$name"/>
                                                                </gco:CharacterString>
                                                            </gmd:name>
                                                            <gmd:description>
                                                                <gco:CharacterString>
                                                                <xsl:value-of select="$desc"/>
                                                                </gco:CharacterString>
                                                            </gmd:description>
                                                            <gmd:function>
                                                                <gmd:CI_OnLineFunctionCode
                                                                codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#CI_OnL
                                                                ineFunctionCode" codeListValue=""/>
                                                            </gmd:function>
                                                        </gmd:CI_OnlineResource>
                                                    </gmd:onLine>
                                                  </gmd:MD_DigitalTransferOptions>
                                            </gmd:distributorTransferOptions>
                                        </gmd:MD_Distributor>
                                    </gmd:distributor>
                                </xsl:when>
                                <xsl:otherwise>
                                    <gmd:distributor>
                                        <gmd:MD_Distributor>
                                        	<xsl:copy-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact[..//gmd:onLine[1]/gmd:CI_OnlineResource/gmd:linkage/gmd:URL = $oldUrl 
                                        		and ..//gmd:onLine[1]/gmd:CI_OnlineResource/gmd:name/gco:CharacterString = $oldName
                                        		and ..//gmd:onLine[1]/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString = $oldProtocol]"/>
                                        	<xsl:copy-of select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorFormat[..//gmd:onLine[1]/gmd:CI_OnlineResource/gmd:linkage/gmd:URL = $oldUrl 
                                        		and ..//gmd:onLine[1]/gmd:CI_OnlineResource/gmd:name/gco:CharacterString = $oldName
                                        		and ..//gmd:onLine[1]/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString = $oldProtocol]"/>
                                            <gmd:distributorTransferOptions>
                                                <gmd:MD_DigitalTransferOptions>
                                                    <gmd:onLine>
                                                        <xsl:if test="$uuidref">
                                                            <xsl:attribute name="uuidref" select="$uuidref"/>
                                                        </xsl:if>
                                                        <gmd:CI_OnlineResource>
                                                            <gmd:linkage>
                                                                <gmd:URL>
                                                                <xsl:value-of select="$url"/>
                                                                </gmd:URL>
                                                            </gmd:linkage>
                                                            <gmd:protocol>
                                                                <gco:CharacterString>
                                                                <xsl:value-of select="'OGC:WMS:getCapabilities'"/>
                                                                </gco:CharacterString>
                                                            </gmd:protocol>
                                                            <gmd:name>
                                                                <gco:CharacterString>
                                                                <xsl:value-of select="$serviceName"/>
                                                                </gco:CharacterString>
                                                            </gmd:name>
                                                            <gmd:description>
                                                                <gco:CharacterString>
                                                                <xsl:value-of select="$serviceDescr"/>
                                                                </gco:CharacterString>
                                                            </gmd:description>
                                                            <gmd:function>
                                                                <gmd:CI_OnLineFunctionCode
                                                                codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#CI_OnL
                                                                ineFunctionCode" codeListValue=""/>
                                                            </gmd:function>
                                                        </gmd:CI_OnlineResource>
                                                    </gmd:onLine>
                                                    
                                                    <xsl:for-each select="tokenize($name, ',,')">
                                                        <xsl:variable name="pos" select="position()" />
                                                        <gmd:onLine>
                                                            <xsl:if test="$uuidref">
                                                                <xsl:attribute name="uuidref" select="$uuidref" />
                                                            </xsl:if>
                                                            <gmd:CI_OnlineResource>
                                                                <gmd:linkage>
                                                                    <gmd:URL>
                                                                        <xsl:value-of select="$url" />
                                                                    </gmd:URL>
                                                                </gmd:linkage>
                                                                <gmd:protocol>
                                                                    <gco:CharacterString>
                                                                        <xsl:value-of select="$protocol" />
                                                                    </gco:CharacterString>
                                                                </gmd:protocol>
                                                                <gmd:name>
                                                                    <gco:CharacterString>
                                                                        <xsl:value-of select="." />
                                                                    </gco:CharacterString>
                                                                </gmd:name>
                                                                <gmd:description>
                                                                    <gco:CharacterString>
                                                                        <xsl:value-of select="tokenize($desc, ',,')[position() = $pos]" />
                                                                    </gco:CharacterString>
                                                                </gmd:description>
                                                                <gmd:function>
                                                                    <gmd:CI_OnLineFunctionCode
                                                                    codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#CI_OnL
                                                                    ineFunctionCode" codeListValue=""/>
                                                                </gmd:function>
                                                                
                                                            </gmd:CI_OnlineResource>
                                                        </gmd:onLine>
                                                    </xsl:for-each>
                                                </gmd:MD_DigitalTransferOptions>
                                            </gmd:distributorTransferOptions>
                                        </gmd:MD_Distributor>
                                    </gmd:distributor>
                                </xsl:otherwise>
                                </xsl:choose>
                            </xsl:if>
                            <xsl:copy-of
                                select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions"
                            />
                        </gmd:MD_Distribution>
                        
                    </gmd:distributionInfo>
                </xsl:otherwise>
            </xsl:choose>
            
            <xsl:copy-of
                select="gmd:dataQualityInfo|
            gmd:portrayalCatalogueInfo|
            gmd:metadataConstraints|
            gmd:applicationSchemaInfo|
            gmd:metadataMaintenance|
            gmd:series|
            gmd:describes|
            gmd:propertyType|
            gmd:featureType|
            gmd:featureAttribute"/>
            
        </xsl:copy>
    </xsl:template>
    
    
    <!-- Copy dataset distributor -->
    <xsl:template name="onlinecopy">
    
        <gmd:distributor>
            <gmd:MD_Distributor>
                <gmd:distributorContact>
                    <xsl:copy-of select="
                        gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/
                        gmd:citedResponsibleParty/gmd:CI_ResponsibleParty"/>
                </gmd:distributorContact>
                <gmd:distributorFormat/>
                <gmd:distributorTransferOptions>
                    <gmd:MD_DigitalTransferOptions>
                        <xsl:apply-templates mode="onlinecopy" select="
                            gmd:distributionInfo//gmd:onLine"/>
                    </gmd:MD_DigitalTransferOptions>
                </gmd:distributorTransferOptions>
            </gmd:MD_Distributor>
        </gmd:distributor>
    </xsl:template>
    
    
    <xsl:template match="gmd:onLine" mode="onlinecopy">
      <xsl:copy>
          <xsl:choose>
              <xsl:when test="../../../../../gmd:fileIdentifier/gco:CharacterString">
                  <xsl:attribute name="uuidref" select="../../../../../gmd:fileIdentifier/gco:CharacterString"/>
              </xsl:when>
              <xsl:when test="$extra_metadata_uuid">
                  <xsl:attribute name="uuidref" select="$extra_metadata_uuid"/>
              </xsl:when>
          </xsl:choose>
          <gmd:CI_OnlineResource>
              <xsl:copy-of select="gmd:CI_OnlineResource/gmd:linkage|gmd:CI_OnlineResource/gmd:protocol|
                  gmd:CI_OnlineResource/gmd:applicationProfile|gmd:CI_OnlineResource/gmd:name"/>
              <gmd:description>
                  <xsl:copy-of select="../../../../../gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/*"/>
              </gmd:description>
              <xsl:copy-of select="gmd:CI_OnlineResource/gmd:function"/>
          </gmd:CI_OnlineResource>
      </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
