<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
	xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx"
	xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gml="http://www.opengis.net/gml"
	xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:exslt="http://exslt.org/common" exclude-result-prefixes="#all">
	
	<xsl:include href="metadata-markup.xsl"/>
	<!-- main template - the way into processing iso19139.sextant -->
	<xsl:template name="metadata-iso19139.sextantview-simple">
		<xsl:call-template name="metadata-iso19139view-simple"/>
	</xsl:template>

	<!-- EMODNET template / start -->
	<xsl:template mode="iso19139.sextant" match="gmd:distance" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:apply-templates mode="iso19139">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit" select="$edit"/>
		</xsl:apply-templates>
		
	</xsl:template>

	<!-- Sextant template / end -->
	
	
	<xsl:template name="view-with-header-iso19139.sextant">
		<xsl:param name="tabs"/>

		<xsl:call-template name="view-with-header-iso19139">
			<xsl:with-param name="tabs" select="$tabs"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="metadata-iso19139.sextant">
		<xsl:param name="schema"/>
		<xsl:param name="edit" select="false()"/>
		<xsl:param name="embedded"/>

		<!-- process in profile mode first -->
		<xsl:variable name="profileElements">
			<xsl:if test="$currTab='sextant'">
				<xsl:apply-templates mode="iso19139.sextant" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
					<xsl:with-param name="embedded" select="$embedded"/>
				</xsl:apply-templates>
			</xsl:if>
		</xsl:variable>
		
		<xsl:choose>
			<!-- if we got a match in profile mode then show it -->
			<xsl:when test="count($profileElements/*)>0">
				<xsl:copy-of select="$profileElements"/>
			</xsl:when>
			<!-- otherwise process in base iso19139 mode -->
			<xsl:otherwise> 
				<xsl:apply-templates mode="iso19139" select="." >
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="embedded" select="$embedded" />
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<xsl:template name="iso19139.sextantCompleteTab">
		<xsl:param name="tabLink"/>
		<xsl:param name="schema"/>
		<xsl:call-template name="iso19139CompleteTab">
			<xsl:with-param name="tabLink" select="$tabLink"/>
			<xsl:with-param name="schema" select="$schema"/>
		</xsl:call-template>
		
		<xsl:call-template name="mainTab">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/tab"/>
			<xsl:with-param name="default">sextant</xsl:with-param>
			<xsl:with-param name="menu">
				<item label="sextantTab">sextant</item>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>







	<!--
    Redirection template for profil fra in order to process 
    extraTabs.
  -->
	<xsl:template mode="iso19139.sextant" match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']"
		priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:param name="embedded"/>

		
		<xsl:variable name="dataset"
			select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='dataset' or normalize-space(gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue)=''"/>
		<xsl:choose>

			<!-- metadata tab -->
			<xsl:when test="$currTab='metadata'">
				<xsl:call-template name="iso19139Metadata">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:call-template>
			</xsl:when>

			<!-- identification tab -->
			<xsl:when test="$currTab='identification'">
				<xsl:apply-templates mode="elementEP"
					select="gmd:identificationInfo|geonet:child[string(@name)='identificationInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- maintenance tab -->
			<xsl:when test="$currTab='maintenance'">
				<xsl:apply-templates mode="elementEP"
					select="gmd:metadataMaintenance|geonet:child[string(@name)='metadataMaintenance']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- constraints tab -->
			<xsl:when test="$currTab='constraints'">
				<xsl:apply-templates mode="elementEP"
					select="gmd:metadataConstraints|geonet:child[string(@name)='metadataConstraints']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- spatial tab -->
			<xsl:when test="$currTab='spatial'">
				<xsl:apply-templates mode="elementEP"
					select="gmd:spatialRepresentationInfo|geonet:child[string(@name)='spatialRepresentationInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- refSys tab -->
			<xsl:when test="$currTab='refSys'">
				<xsl:apply-templates mode="elementEP"
					select="gmd:referenceSystemInfo|geonet:child[string(@name)='referenceSystemInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- distribution tab -->
			<xsl:when test="$currTab='distribution'">
				<xsl:apply-templates mode="elementEP"
					select="gmd:distributionInfo|geonet:child[string(@name)='distributionInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- embedded distribution tab -->
			<xsl:when test="$currTab='distribution2'">
				<xsl:apply-templates mode="elementEP"
					select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- dataQuality tab -->
			<xsl:when test="$currTab='dataQuality'">
				<xsl:apply-templates mode="elementEP"
					select="gmd:dataQualityInfo|geonet:child[string(@name)='dataQualityInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- appSchInfo tab -->
			<xsl:when test="$currTab='appSchInfo'">
				<xsl:apply-templates mode="elementEP"
					select="gmd:applicationSchemaInfo|geonet:child[string(@name)='applicationSchemaInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- porCatInfo tab -->
			<xsl:when test="$currTab='porCatInfo'">
				<xsl:apply-templates mode="elementEP"
					select="gmd:portrayalCatalogueInfo|geonet:child[string(@name)='portrayalCatalogueInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- contentInfo tab -->
			<xsl:when test="$currTab='contentInfo'">
				<xsl:apply-templates mode="elementEP"
					select="gmd:contentInfo|geonet:child[string(@name)='contentInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- extensionInfo tab -->
			<xsl:when test="$currTab='extensionInfo'">
				<xsl:apply-templates mode="elementEP"
					select="gmd:metadataExtensionInfo|geonet:child[string(@name)='metadataExtensionInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- ISOMinimum tab -->
			<xsl:when test="$currTab='ISOMinimum'">
				<xsl:call-template name="isotabs">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
					<xsl:with-param name="dataset" select="$dataset"/>
					<xsl:with-param name="core" select="false()"/>
				</xsl:call-template>
			</xsl:when>

			<!-- ISOCore tab -->
			<xsl:when test="$currTab='ISOCore'">
				<xsl:call-template name="isotabs">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
					<xsl:with-param name="dataset" select="$dataset"/>
					<xsl:with-param name="core" select="true()"/>
				</xsl:call-template>
			</xsl:when>

			<!-- ISOAll tab -->
			<xsl:when test="$currTab='ISOAll'">
				<xsl:call-template name="iso19139Complete">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:call-template>
			</xsl:when>

			<!-- INSPIRE tab -->
			<xsl:when test="$currTab='inspire'">
				<xsl:call-template name="inspiretabs">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
					<xsl:with-param name="dataset" select="$dataset"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$currTab='sextant'">
				
				<xsl:call-template name="sextant">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="dataset" select="$dataset"/>
				</xsl:call-template>
				
			</xsl:when>
			<!-- default -->
			<xsl:otherwise>
				<xsl:call-template name="iso19139Simple">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
					<xsl:with-param name="flat"
						select="/root/gui/config/metadata-tab/*[name(.)=$currTab]/@flat"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>


	<!-- The main view -->
	<xsl:template name="sextant">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:param name="dataset"/>
		<xsl:param name="core"/>
		
		
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/metadataInfoTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/metadataInfoTitle)"/>
			<xsl:with-param name="content">
				
				<xsl:apply-templates mode="elementEP" select="gmd:language">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:hierarchyLevel">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:dateStamp">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
				<xsl:for-each select="gmd:contact/gmd:CI_ResponsibleParty">
					<xsl:apply-templates mode="complexElement" select=".">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="title">
							<xsl:value-of select="string(/root/gui/schemas/iso19139/labels/element[@name='gmd:pointOfContact']/label)"/>
						</xsl:with-param>
						<xsl:with-param name="content">
							<xsl:apply-templates mode="elementEP" select="./gmd:organisationName">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:apply-templates>
							<xsl:apply-templates mode="elementEP" select="./gmd:role">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:apply-templates>
							<xsl:apply-templates mode="elementEP" select="./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:apply-templates>
						</xsl:with-param>
					</xsl:apply-templates>
				</xsl:for-each>
				
				
			</xsl:with-param>
		</xsl:call-template>
		
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/respOrgaTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/respOrgaTitle)"/>
			<xsl:with-param name="content">
				
				<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty">
					<xsl:apply-templates mode="complexElement" select=".">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="content">
							<xsl:apply-templates mode="elementEP" select="./gmd:organisationName">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:apply-templates>
							<xsl:apply-templates mode="elementEP" select="./gmd:role">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:apply-templates>
							<xsl:apply-templates mode="elementEP" select="./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:apply-templates>
						</xsl:with-param>
					</xsl:apply-templates>
				</xsl:for-each>
			</xsl:with-param>
		</xsl:call-template>
		
		
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/identificationTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/identificationTitle)"/>
			<xsl:with-param name="content">
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="simpleElement" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="title">
						<xsl:value-of select="string(/root/gui/schemas/iso19139/labels/element[@name='gmd:MD_Identifier']/label)"/>
					</xsl:with-param>
					<xsl:with-param name="helpLink">
						<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code">
							<xsl:call-template name="getHelpLink">
								<xsl:with-param name="name"   select="name(.)"/>
								<xsl:with-param name="schema" select="$schema"/>
							</xsl:call-template>
						</xsl:for-each>
					</xsl:with-param>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:language">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:characterSet">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:credit">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialRepresentationType">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
			</xsl:with-param>
		</xsl:call-template>



		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/tempRefTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/tempRefTitle)"/>
			<xsl:with-param name="content">
				
				
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
				<xsl:choose>
					<xsl:when test="$edit=true()">
						<xsl:call-template name="complexElementGui">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
							<xsl:with-param name="title">
								<xsl:value-of select="string(/root/gui/schemas/iso19139/labels/element[@name='gmd:EX_TemporalExtent']/label)"/>
							</xsl:with-param>
							<xsl:with-param name="content">

                <xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/
                                        gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/
                                          gmd:extent/gml:TimePeriod">


                  <xsl:apply-templates mode="simpleElement"
                                       select="gml:*/gml:beginPosition">
                    <xsl:with-param name="schema"  select="$schema"/>
                    <xsl:with-param name="edit"   select="$edit"/>
                    <xsl:with-param name="text">
                      <xsl:variable name="ref" select="gml:*/gml:beginPosition/geonet:element/@ref"/>
                      <xsl:variable name="format">
                        <xsl:choose>
                          <xsl:when test="gco:Date"><xsl:text>%Y-%m-%d</xsl:text></xsl:when>
                          <xsl:otherwise><xsl:text>%Y-%m-%dT%H:%M:00</xsl:text></xsl:otherwise>
                        </xsl:choose>
                      </xsl:variable>

                      <xsl:call-template name="calendar">
                        <xsl:with-param name="ref" select="$ref"/>
                        <xsl:with-param name="date" select="gml:*/gml:beginPosition"/>
                        <xsl:with-param name="format" select="$format"/>
                      </xsl:call-template>

                    </xsl:with-param>
                  </xsl:apply-templates>

                  <xsl:apply-templates mode="simpleElement" select="gml:*/gml:endPosition">
                    <xsl:with-param name="schema"  select="$schema"/>
                    <xsl:with-param name="edit"   select="$edit"/>
                    <xsl:with-param name="text">
                      <xsl:variable name="ref" select="gml:*/gml:endPosition/geonet:element/@ref"/>
                      <xsl:variable name="format">
                        <xsl:choose>
                          <xsl:when test="gco:Date"><xsl:text>%Y-%m-%d</xsl:text></xsl:when>
                          <xsl:otherwise><xsl:text>%Y-%m-%dT%H:%M:00</xsl:text></xsl:otherwise>
                        </xsl:choose>
                      </xsl:variable>

                      <xsl:call-template name="calendar">
                        <xsl:with-param name="ref" select="$ref"/>
                        <xsl:with-param name="date" select="gml:*/gml:endPosition"/>
                        <xsl:with-param name="format" select="$format"/>
                      </xsl:call-template>
                    </xsl:with-param>
                  </xsl:apply-templates>
                </xsl:for-each>
								
							</xsl:with-param>
							<xsl:with-param name="helpLink">
								<xsl:call-template name="getHelpLink">
									<xsl:with-param name="name"   select="name(gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement[1]/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod)"/>
									<xsl:with-param name="schema" select="$schema"/>
								</xsl:call-template>
							</xsl:with-param>
							<xsl:with-param name="removeLink">
								<xsl:variable name="id" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod]/geonet:element/@uuid"/>
								<xsl:value-of select="concat('doRemoveElementAction(',$apos,'/metadata.elem.delete',$apos,',',gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod]/geonet:element/@ref,',',gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod]/geonet:element/@parent,',',$apos,$id,$apos,',',gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod]/geonet:element/@min,');')"/>
								<xsl:if test="not(gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod]/geonet:element/@del='true')">
									<xsl:text>!OPTIONAL</xsl:text>
								</xsl:if>
							</xsl:with-param>
							<xsl:with-param name="id" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod]/geonet:element/@uuid"/>
							<xsl:with-param name="validationLink">
								<xsl:variable name="ref" select="concat('#_',gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod]/geonet:element/@ref)"/>
								<xsl:call-template name="validationLink">
									<xsl:with-param name="ref" select="$ref"/>
								</xsl:call-template>
							</xsl:with-param>
							
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="complexElementGui">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
							<xsl:with-param name="title">
								<xsl:value-of select="string(/root/gui/schemas/iso19139/labels/element[@name='gmd:EX_TemporalExtent']/label)"/>
							</xsl:with-param>
							<xsl:with-param name="content">
								<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod">
									<xsl:with-param name="schema"  select="$schema"/>
									<xsl:with-param name="edit"   select="$edit"/>
								</xsl:apply-templates>
							</xsl:with-param>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
				
			</xsl:with-param>
		</xsl:call-template>



		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/limitationsTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/limitationsTitle)"/>
			<xsl:with-param name="content">
				
				
				<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints">
					<xsl:apply-templates mode="complexElement" select=".">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="title">
							<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/accessUseConstraints)"/>
						</xsl:with-param>
						<xsl:with-param name="content">

              <xsl:apply-templates mode="elementEP" select="./gmd:useLimitation">
                <xsl:with-param name="schema" select="$schema"/>
                <xsl:with-param name="edit"   select="$edit"/>
                <xsl:with-param name="flat"   select="$flat"/>
              </xsl:apply-templates>

							<xsl:apply-templates mode="elementEP" select="./gmd:accessConstraints">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
								<xsl:with-param name="flat"   select="$flat"/>
							</xsl:apply-templates>
							
							<xsl:apply-templates mode="elementEP" select="./gmd:otherConstraints">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
								<xsl:with-param name="flat"   select="$flat"/>
							</xsl:apply-templates>
							
						</xsl:with-param>
					</xsl:apply-templates>
				</xsl:for-each>
				
				<!--<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_Constraints">
					<xsl:apply-templates mode="complexElement" select=".">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="title">
							<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/publicAccessConstraints)"/>
						</xsl:with-param>
						<xsl:with-param name="content">
							<xsl:apply-templates mode="elementEP" select="./gmd:useLimitation">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
								<xsl:with-param name="flat"   select="$flat"/>
							</xsl:apply-templates>
						</xsl:with-param>
					</xsl:apply-templates>
				</xsl:for-each>-->
				
			</xsl:with-param>
		</xsl:call-template>



		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/keywordTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/keywordTitle)"/>
			<xsl:with-param name="content">
				
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:topicCategory/gmd:MD_TopicCategoryCode">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
			</xsl:with-param>
		</xsl:call-template>
		
		
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/geoLocationTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/geoLocationTitle)"/>
			<xsl:with-param name="content">
				
				
				<xsl:apply-templates mode="iso19139" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:referenceSystemInfo">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
			</xsl:with-param>
		</xsl:call-template>
		
		
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/qualityTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/qualityTitle)"/>
			<xsl:with-param name="content">
				
				<xsl:for-each select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement">
					
					<xsl:apply-templates mode="elementEP" select=".">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="flat"   select="$flat"/>
					</xsl:apply-templates>
				</xsl:for-each>
				
				<xsl:apply-templates mode="elementEP" select="gmd:spatialRepresentationInfo">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
			</xsl:with-param>
		</xsl:call-template>
		
		
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/conformityTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/conformityTitle)"/>
			<xsl:with-param name="content">
				
				<xsl:apply-templates mode="elementEP" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_AbsoluteExternalPositionalAccuracy/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_AbsoluteExternalPositionalAccuracy/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:date">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_AbsoluteExternalPositionalAccuracy/gmd:result/gmd:DQ_ConformanceResult/gmd:explanation">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_AbsoluteExternalPositionalAccuracy/gmd:result/gmd:DQ_ConformanceResult/gmd:pass">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$flat"/>
				</xsl:apply-templates>
				
			</xsl:with-param>
		</xsl:call-template>
		
		
	</xsl:template>
	
</xsl:stylesheet>
