<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to add a reference to a related record using aggregation info.
-->
<xsl:stylesheet version="2.0" xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- The uuid of the target record -->
	<xsl:param name="uuidref"/>
	
	<!-- A list of uuids of the target records -->
	<xsl:param name="uuids"/>
	
	<!-- (optional) The association type. Default: crossReference. -->
	<xsl:param name="associationType" select="'crossReference'"/>
	
	<!-- (optional) The initiative type. -->
	<xsl:param name="initiativeType" select="''"/>
	
	
	<xsl:template match="gmd:MD_DataIdentification|*[@gco:isoType='gmd:MD_DataIdentification']">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="gmd:citation"/>
			<xsl:apply-templates select="gmd:abstract"/>
			<xsl:apply-templates select="gmd:purpose"/>
			<xsl:apply-templates select="gmd:credit"/>
			<xsl:apply-templates select="gmd:status"/>
			<xsl:apply-templates select="gmd:pointOfContact"/>
			<xsl:apply-templates select="gmd:resourceMaintenance"/>
			<xsl:apply-templates select="gmd:graphicOverview"/>
			<xsl:apply-templates select="gmd:resourceFormat"/>
			<xsl:apply-templates select="gmd:descriptiveKeywords"/>
			<xsl:apply-templates select="gmd:resourceSpecificUsage"/>
			<xsl:apply-templates select="gmd:resourceConstraints"/>
			
			<xsl:apply-templates select="gmd:aggregationInfo"/>
			
			<xsl:call-template name="fill"/>
			
			<xsl:apply-templates select="gmd:spatialRepresentationType"/>
			<xsl:apply-templates select="gmd:spatialResolution"/>
			<xsl:apply-templates select="gmd:language"/>
			<xsl:apply-templates select="gmd:characterSet"/>
			<xsl:apply-templates select="gmd:topicCategory"/>
			<xsl:apply-templates select="gmd:environmentDescription"/>
			<xsl:apply-templates select="gmd:extent"/>
			<xsl:apply-templates select="gmd:supplementalInformation"/>
			
		</xsl:copy>
	</xsl:template>
	
	<xsl:template name="fill">
		<xsl:variable name="context" select="."/>
		
		<xsl:if test="$uuidref != ''">
			<xsl:call-template name="make-aggregate">
				<xsl:with-param name="uuid" select="$uuidref"/>
				<xsl:with-param name="context" select="$context"/>
			</xsl:call-template>
		</xsl:if>
		
		<xsl:if test="$uuids != ''">
			<xsl:for-each select="tokenize($uuids, ',')">
				<xsl:choose>
					<xsl:when test="contains(., '#')">
						<xsl:variable name="tokens" select="tokenize(., '#')"/>
						<xsl:call-template name="make-aggregate">
							<xsl:with-param name="uuid" select="$tokens[1]"/>
							<xsl:with-param name="context" select="$context"/>
							<xsl:with-param name="associationType" select="$tokens[2]"/>
							<xsl:with-param name="initiativeType" select="$tokens[3]"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<!-- Same initiative type and association type
						for all siblings -->
						<xsl:call-template name="make-aggregate">
							<xsl:with-param name="uuid" select="."/>
							<xsl:with-param name="context" select="$context"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
				
			</xsl:for-each>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="make-aggregate">
		<xsl:param name="uuid"/>
		<xsl:param name="context"/>
		<xsl:param name="initiativeType" select="$initiativeType" required="no"/>
		<xsl:param name="associationType" select="$associationType" required="no"/>
		
		<xsl:variable name="notExist" select="count($context/gmd:aggregationInfo/gmd:MD_AggregateInformation[
			gmd:aggregateDataSetIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString = $uuid
			and gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue = $associationType
			and gmd:initiativeType/gmd:DS_InitiativeTypeCode/@codeListValue = $initiativeType
			]) = 0"/>
		<xsl:if test="$notExist">
			<gmd:aggregationInfo>
				<gmd:MD_AggregateInformation>
					<gmd:aggregateDataSetIdentifier>
						<gmd:MD_Identifier>
							<gmd:code>
								<gco:CharacterString><xsl:value-of select="$uuid"/></gco:CharacterString>
							</gmd:code>
						</gmd:MD_Identifier>
					</gmd:aggregateDataSetIdentifier>
					<gmd:associationType>
						<gmd:DS_AssociationTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#DS_AssociationTypeCode" codeListValue="{$associationType}"/>
					</gmd:associationType>
					<xsl:if test="$initiativeType != ''">
						<gmd:initiativeType>
							<gmd:DS_InitiativeTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#DS_InitiativeTypeCode" codeListValue="{$initiativeType}"/>
						</gmd:initiativeType>
					</xsl:if>
				</gmd:MD_AggregateInformation>
			</gmd:aggregationInfo>
		</xsl:if>
	</xsl:template>
	
	<!-- Do a copy of every nodes and attributes -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- Remove geonet:* elements. -->
	<xsl:template match="geonet:*|gmd:aggregationInfo[*/gmd:aggregateDataSetIdentifier/*/gmd:code/* = $uuidref]" priority="2"/>
</xsl:stylesheet>
