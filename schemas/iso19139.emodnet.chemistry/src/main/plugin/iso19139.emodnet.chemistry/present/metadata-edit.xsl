<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
	xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx"
	xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gml="http://www.opengis.net/gml"
	xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:exslt="http://exslt.org/common" exclude-result-prefixes="#all">
	
	<xsl:include href="metadata-markup.xsl"/>
	<!-- main template - the way into processing iso19139.emodnet.chemistry -->
	<xsl:template name="metadata-iso19139.emodnet.chemistryview-simple">
		<xsl:call-template name="metadata-iso19139view-simple"/>
	</xsl:template>

	<!-- EMODNET template / start -->
	<xsl:template mode="iso19139.emodnet.chemistry" match="gmd:distance" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:apply-templates mode="iso19139">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit" select="$edit"/>
		</xsl:apply-templates>
		
	</xsl:template>

	<!-- EMODNET template / end -->
	
	
	<xsl:template name="view-with-header-iso19139.emodnet.chemistry">
		<xsl:param name="tabs"/>

		<xsl:call-template name="view-with-header-iso19139">
			<xsl:with-param name="tabs" select="$tabs"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="metadata-iso19139.emodnet.chemistry">
		<xsl:param name="schema"/>
		<xsl:param name="edit" select="false()"/>
		<xsl:param name="embedded"/>

		<!-- process in profile mode first -->
		<xsl:variable name="profileElements">
			<xsl:if test="$currTab='emodnet.chemistry'">
				<xsl:apply-templates mode="iso19139.emodnet.chemistry" select=".">
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


	<xsl:template name="iso19139.emodnet.chemistryCompleteTab">
		<xsl:param name="tabLink"/>
		<xsl:param name="schema"/>
		<xsl:call-template name="iso19139CompleteTab">
			<xsl:with-param name="tabLink" select="$tabLink"/>
			<xsl:with-param name="schema" select="$schema"/>
		</xsl:call-template>
		
		
		<xsl:call-template name="mainTab">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/tab"/>
			<xsl:with-param name="default">emodnet.chemistry</xsl:with-param>
			<xsl:with-param name="menu">
				<item label="emodnet.chemistryTab">emodnet.chemistry</item>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>






	<xsl:template mode="iso19139.emodnet.chemistry" match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']"
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
			<xsl:when test="$currTab='emodnet.chemistry'">
				
				<xsl:call-template name="emodnet">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="dataset" select="$dataset"/>
					<xsl:with-param name="type" select="'chemistry'"/>
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
	<xsl:template name="emodnet">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:param name="dataset"/>
		<xsl:param name="core"/>
		<xsl:param name="type"/>
		
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/metadataInfoTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/metadataInfoTitle)"/>
			<xsl:with-param name="content">
				<xsl:apply-templates mode="elementEP" select="gmd:fileIdentifier">
					<xsl:with-param name="schema"   select="$schema"/>
					<xsl:with-param name="edit"     select="$edit"/>
				</xsl:apply-templates>
				
				<!--Catalogue type
				is stored in hierarchy level name in a gmx:Anchor.
				-->
				<xsl:choose>
					<xsl:when test="$edit=true()">
						<tr type="metadata" id="catalogueTypeSelect">
							<xsl:variable name="qname">gmd:hierarchyLevelName</xsl:variable>
							<xsl:variable name="codelistProfil">
								<xsl:copy-of
									select="/root/gui/schemas/*[name()=$schema]/codelists/codelist[@name = 'gmd:hierarchyLevelName']/*" />
							</xsl:variable>
							<xsl:variable name="codelist" select="exslt:node-set($codelistProfil)" />
							
							<th  class="main">
								<label><xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/labels/element[@name='gmd:hierarchyLevelName']/label)"/></label>
							</th>
							<td valign="top" class="padded">
								<xsl:variable name="urlPrefix">http://www.seadatanet.org/urnurl/</xsl:variable>
								<xsl:variable name="value"    select="substring-after(gmd:hierarchyLevelName/gmx:Anchor/@xlink:href, $urlPrefix)"/>
								<select class="md" onchange="document.getElementById('catalogueType').getElementsByTagName('input')[1].value='{$urlPrefix}'+this.options[this.selectedIndex].value.substring(0,this.options[this.selectedIndex].value.indexOf('=')).trim();document.getElementById('catalogueType').getElementsByTagName('input')[0].value=this.options[this.selectedIndex].value;" size="1">
									<option name=""/>
									<xsl:for-each select="$codelist/entry[not(@hideInEditMode)]">
										<xsl:sort select="label"/>
										<option>
											<xsl:if test="contains(label, $value)">
												<xsl:attribute name="selected"/>
											</xsl:if>
											<xsl:attribute name="value"><xsl:value-of select="code"/></xsl:attribute>
											<xsl:value-of select="label"/>
										</option>
									</xsl:for-each>
								</select>
							</td>
						</tr>
						<!-- Hide the anchor control -->
						<tr type="metadata" id="catalogueTypeHidden" style="display:none;">
							<td colspan="2">
								<table id="catalogueType" width="100%">
									<xsl:apply-templates mode="elementEP" select="gmd:hierarchyLevelName">
										<xsl:with-param name="schema"  select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
									</xsl:apply-templates>  
								</table>
							</td>
						</tr>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates mode="simpleElement" select="gmd:hierarchyLevelName/gmx:Anchor">
							<xsl:with-param name="schema"  select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
							<xsl:with-param name="title">
								<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/labels/element[@name='gmd:hierarchyLevelName']/label)"/>
							</xsl:with-param>
						</xsl:apply-templates>
					</xsl:otherwise>
				</xsl:choose>
				
				<xsl:apply-templates mode="simpleElement" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='theme' and gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://www.seadatanet.org/urnurl/']">
					<xsl:with-param name="schema"   select="$schema"/>
					<xsl:with-param name="edit"     select="$edit"/>
					<xsl:with-param name="title">
						<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/projectName)"/>
					</xsl:with-param>
					<xsl:with-param name="text">
						<xsl:apply-templates mode="iso19139ComboFromCodelistForCharacterString" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='theme' and gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://www.seadatanet.org/urnurl/']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
					</xsl:with-param>
				</xsl:apply-templates>
				
				
				<!-- FIXME : Multiple instance won't work -->
				<xsl:apply-templates mode="simpleElement" select="gmd:metadataConstraints/gmd:MD_LegalConstraints/gmd:useLimitation">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="title">
						<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/DisseminationLevelTitle)"/>
					</xsl:with-param>
					<xsl:with-param name="text">
						<xsl:apply-templates mode="iso19139ComboFromCodelistForCharacterString" select="gmd:metadataConstraints/gmd:MD_LegalConstraints/gmd:useLimitation">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
					</xsl:with-param>
				</xsl:apply-templates>
				
				
				<xsl:apply-templates mode="iso19139" select="gmd:language">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
				
				
				<xsl:apply-templates mode="elementEP" select="gmd:dateStamp">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:with-param>
		</xsl:call-template>
		
		
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/identificationTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/identificationTitle)"/>
			<xsl:with-param name="content">
				
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:with-param>
		</xsl:call-template>
		
		
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/whatTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/whatTitle)"/>
			<xsl:with-param name="content">
				
				
				<!-- FIXME : Thesaurus or big hack ? big working hack for now. -->
				<xsl:choose>
					<xsl:when test="$edit=true()">
						<tr>
							<td colspan="2">
								<table width="100%" class="gn">
									<xsl:apply-templates mode="iso19139.emodnet.chemistry" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='discipline' and gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/' ]">
										<xsl:with-param name="schema" select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
									</xsl:apply-templates>
								</table>
								
								<table width="100%" name="discipline" style="display:none;">
									<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='discipline' and following-sibling::gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']/gco:CharacterString">
										<tr id="{../geonet:element/@uuid}" type="metadata" > 
											<th class="main">
												<span id="{../geonet:element/@uuid}">
													<xsl:call-template name="getTitle">
														<xsl:with-param name="name"   select="name(..)"/>
														<xsl:with-param name="schema" select="$schema"/>
													</xsl:call-template>
												</span>
											</th>
											<td class="padded" valign="top">
												<xsl:call-template name="getElementText">
													<xsl:with-param name="schema" select="$schema"/>
													<xsl:with-param name="edit"   select="$edit"/>
												</xsl:call-template>
											</td>
										</tr>
									</xsl:for-each>
								</table>
							</td>
						</tr>
					</xsl:when>
					<xsl:otherwise>
						<tr>
							<td colspan="2">
								<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='discipline' and gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']">
									<xsl:apply-templates mode="complexElement" select=".">
										<xsl:with-param name="schema" select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
										<xsl:with-param name="title">
											<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/ParamsMeasuredTitle)"/>
										</xsl:with-param>
										<xsl:with-param name="content">
											<dl>
												<xsl:for-each select="./gmd:keyword">
													<xsl:variable name="value" >
														<xsl:value-of select="gco:CharacterString/text()"/>
													</xsl:variable>
													<xsl:variable name="name" select="local-name(.)"/> 
													<xsl:variable name="qname">disciplineKeyword</xsl:variable>
													<xsl:variable name="codelistProfil" select="/root/gui/schemas/*[name()=$schema]/codelists/codelist[@name = $qname]/*" />
													<xsl:variable name="codelist" select="exslt:node-set($codelistProfil)" />
													
													<!-- codelist in view mode -->
													<xsl:if test="normalize-space($value)!=''">
														<dt><b><xsl:value-of select="$codelist/entry[code = normalize-space($value)]/label"/></b></dt>
														<dd><xsl:value-of select="$codelist/entry[code = normalize-space($value)]/description"/></dd>
													</xsl:if>
												</xsl:for-each>
											</dl>
										</xsl:with-param>
									</xsl:apply-templates>
								</xsl:for-each>
							</td>
						</tr>
					</xsl:otherwise>
				</xsl:choose>
				
				
				
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialRepresentationType">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
				
				
				<xsl:apply-templates mode="complexElement" select="gmd:spatialRepresentationInfo/gmd:MD_Georectified[gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='column'] or gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='row']]">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="title">
						<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/DimensionsTitle)"/>
					</xsl:with-param>
					<xsl:with-param name="content">
						<xsl:for-each select="gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='column']/gmd:dimensionSize/gco:Integer">
							<xsl:call-template name="simpleElementGui">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
								<xsl:with-param name="title">
									<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/nbColumnsTitle)"/>
								</xsl:with-param>
								<xsl:with-param name="text">
									<xsl:call-template name="getElementText">
										<xsl:with-param name="schema" select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
									</xsl:call-template>
								</xsl:with-param>
								<xsl:with-param name="helpLink">
									<xsl:call-template name="getHelpLink">
										<xsl:with-param name="name" select="name(..)"/>
										<xsl:with-param name="schema" select="$schema"/>
									</xsl:call-template>
								</xsl:with-param>
								<xsl:with-param name="validationLink">
									<xsl:variable name="ref" select="concat('#_',./geonet:element/@ref)"/>
									<xsl:call-template name="validationLink">
										<xsl:with-param name="ref" select="$ref"/>
									</xsl:call-template>
								</xsl:with-param>
								<xsl:with-param name="removeLink">
									<xsl:variable name="id" select="../../../geonet:element/@uuid"/>
									<xsl:value-of select="concat('doRemoveElementAction(',$apos,'/metadata.elem.delete',$apos,',',../../../geonet:element/@ref,',',../../../geonet:element/@parent,',',$apos,$id,$apos,',',../../../geonet:element/@min,');')"/>
								</xsl:with-param>
								<xsl:with-param name="id" select="../../../geonet:element/@uuid"/>
							</xsl:call-template>
						</xsl:for-each>
						<xsl:for-each select="gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='row']/gmd:dimensionSize/gco:Integer">
							<xsl:call-template name="simpleElementGui">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="true()"/>
								<xsl:with-param name="title">
									<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/nbLinesTitle)"/>
								</xsl:with-param>
								<xsl:with-param name="text">
									<xsl:call-template name="getElementText">
										<xsl:with-param name="schema" select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
									</xsl:call-template>
								</xsl:with-param>
								<xsl:with-param name="helpLink">
									<xsl:call-template name="getHelpLink">
										<xsl:with-param name="name" select="name(..)"/>
										<xsl:with-param name="schema" select="$schema"/>
									</xsl:call-template>
								</xsl:with-param>
								<xsl:with-param name="validationLink">
									<xsl:variable name="ref" select="concat('#_',./geonet:element/@ref)"/>
									<xsl:call-template name="validationLink">
										<xsl:with-param name="ref" select="$ref"/>
									</xsl:call-template>
								</xsl:with-param>
								<xsl:with-param name="removeLink">
									<xsl:if test="$edit">
										<xsl:variable name="id" select="../../../geonet:element/@uuid"/>
										<xsl:value-of select="concat('doRemoveElementAction(',$apos,'/metadata.elem.delete',$apos,',',../../../geonet:element/@ref,',',../../../geonet:element/@parent,',',$apos,$id,$apos,',',../../../geonet:element/@min,');')"/>
									</xsl:if>
								</xsl:with-param>
								<xsl:with-param name="id" select="../../../geonet:element/@uuid"/>
							</xsl:call-template>
						</xsl:for-each>
					</xsl:with-param>
				</xsl:apply-templates>
				
				<xsl:for-each select="gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:pointInPixel/gmd:MD_PixelOrientationCode">
					<xsl:call-template name="simpleElementGui">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="true()"/>
						<xsl:with-param name="title">
							<xsl:call-template name="getTitle">
								<xsl:with-param name="name"   select="name(..)"/>
								<xsl:with-param name="schema" select="$schema"/>
							</xsl:call-template>
						</xsl:with-param>
						<xsl:with-param name="text">
							<xsl:call-template name="getElementText">
								<xsl:with-param name="schema" select="'iso19139'"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:call-template>
						</xsl:with-param>
						<xsl:with-param name="helpLink">
							<xsl:call-template name="getHelpLink">
								<xsl:with-param name="name" select="name(..)"/>
								<xsl:with-param name="schema" select="$schema"/>
							</xsl:call-template>
						</xsl:with-param>
						<xsl:with-param name="validationLink">
							<xsl:variable name="ref" select="concat('#_',../geonet:element/@ref)"/>
							<xsl:call-template name="validationLink">
								<xsl:with-param name="ref" select="$ref"/>
							</xsl:call-template>
						</xsl:with-param>
						<xsl:with-param name="removeLink">
							<xsl:if test="$edit">
								<xsl:variable name="id" select="../geonet:element/@uuid"/>
								<xsl:value-of select="concat('doRemoveElementAction(',$apos,'/metadata.elem.delete',$apos,',',../geonet:element/@ref,',',../geonet:element/@parent,',',$apos,$id,$apos,',',../geonet:element/@min,');')"/>
							</xsl:if>
						</xsl:with-param>
						<xsl:with-param name="id" select="../geonet:element/@uuid"/>
					</xsl:call-template>
				</xsl:for-each>
				
				
				<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer">
					<xsl:call-template name="simpleElementGui">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="true()"/>
						<xsl:with-param name="title">
							<xsl:call-template name="getTitle">
								<xsl:with-param name="name"   select="name(..)"/>
								<xsl:with-param name="schema" select="$schema"/>
							</xsl:call-template>
						</xsl:with-param>
						<xsl:with-param name="text">
							<xsl:call-template name="getElementText">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:call-template>
						</xsl:with-param>
						<xsl:with-param name="helpLink">
							<xsl:call-template name="getHelpLink">
								<xsl:with-param name="name" select="name(..)"/>
								<xsl:with-param name="schema" select="$schema"/>
							</xsl:call-template>
						</xsl:with-param>
						<xsl:with-param name="validationLink">
							<xsl:variable name="ref" select="concat('#_',../geonet:element/@ref)"/>
							<xsl:call-template name="validationLink">
								<xsl:with-param name="ref" select="$ref"/>
							</xsl:call-template>
						</xsl:with-param>
						<xsl:with-param name="removeLink">
							<xsl:if test="$edit">
								<xsl:variable name="id" select="../geonet:element/@uuid"/>
								<xsl:value-of select="concat('doRemoveElementAction(',$apos,'/metadata.elem.delete',$apos,',',../../../../../geonet:element/@ref,',',../../../../../geonet:element/@parent,',',$apos,$id,$apos,',',../../../../../geonet:element/@min,');')"/>
							</xsl:if>
						</xsl:with-param>
						<xsl:with-param name="id" select="../geonet:element/@uuid"/>
					</xsl:call-template>
				</xsl:for-each>

			</xsl:with-param>
		</xsl:call-template>
		
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/abstractTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/abstractTitle)"/>
			<xsl:with-param name="content">
				
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="complexElement" select="gmd:identificationInfo/gmd:MD_DataIdentification">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="title">
						<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/detailedDescTitle)"/>
					</xsl:with-param>
					<xsl:with-param name="content">
						
						<xsl:call-template name="complexElementGuiWrapper">
							<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/dataSourcesTitle"/>
							<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/dataSourcesTitle)"/>
							<xsl:with-param name="content">
								
								<xsl:choose>
									<xsl:when test="$edit=true()">
										<tr>
											<td colspan="2">
												<table width="100%"> 
													<xsl:apply-templates mode="iso19139.emodnet.chemistry" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='theme' and gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']">
														<xsl:with-param name="schema" select="$schema"/>
														<xsl:with-param name="edit"   select="$edit"/>
													</xsl:apply-templates>
												</table>
												<table width="100%" name="theme" style="display:none">
													<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='theme' and following-sibling::gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']/gco:CharacterString">
														<tr id="{../geonet:element/@uuid}" type="metadata" > 
															<th width="20%" valign="top" class="md">
																<span id="{../geonet:element/@uuid}">
																	<xsl:call-template name="getTitle">
																		<xsl:with-param name="name"   select="name(..)"/>
																		<xsl:with-param name="schema" select="$schema"/>
																	</xsl:call-template>
																</span>
															</th>
															<td class="padded" valign="top">
																<xsl:call-template name="getElementText">
																	<xsl:with-param name="schema" select="$schema"/>
																	<xsl:with-param name="edit"   select="$edit"/>
																</xsl:call-template>
															</td>
														</tr>
													</xsl:for-each>
												</table>
											</td>
										</tr>
									</xsl:when>
									<xsl:otherwise>
										<tr>
											<td colspan="2">
												<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='theme' and gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']">
													<xsl:apply-templates mode="complexElement" select=".">
														<xsl:with-param name="schema" select="$schema"/>
														<xsl:with-param name="edit"   select="$edit"/>
														<xsl:with-param name="title">
															<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/MeasuringInstrsTitle)"/>
														</xsl:with-param>
														<xsl:with-param name="content">
															<dl>
																<xsl:for-each select="./gmd:keyword">
																	<xsl:variable name="value" >
																		<xsl:value-of select="gco:CharacterString/text()"/>
																	</xsl:variable>
																	<xsl:variable name="name" select="local-name(.)"/> 
																	<xsl:variable name="qname">themeKeyword</xsl:variable>
																	<xsl:variable name="codelistProfil" select="/root/gui/schemas/*[name()=$schema]/codelists/codelist[@name = $qname]" />
																	
																	<xsl:variable name="codelist" select="exslt:node-set($codelistProfil)" />
																	<!-- codelist in view mode -->
																	<xsl:if test="normalize-space($value)!=''">
																		<dt><b><xsl:value-of select="$codelist/entry[code = normalize-space($value)]/label"/></b></dt>
																		<dd><xsl:value-of select="$codelist/entry[code = normalize-space($value)]/description"/></dd>
																	</xsl:if>
																</xsl:for-each>
															</dl>
														</xsl:with-param>
													</xsl:apply-templates>
												</xsl:for-each>
											</td>
										</tr>
									</xsl:otherwise>
								</xsl:choose>
								
								
								
								<xsl:choose>
									<xsl:when test="$edit=true()">
										<tr>
											<td colspan="2">
												<table width="100%"> 
													<xsl:apply-templates mode="iso19139.emodnet.chemistry" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='stratum'  and gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']">
														<xsl:with-param name="schema" select="$schema"/>
														<xsl:with-param name="edit"   select="$edit"/>
													</xsl:apply-templates>
												</table>
												<table name="stratum" style="display:none">
													<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='stratum'  and following-sibling::gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']/gco:CharacterString">
														<tr id="{../geonet:element/@uuid}" type="metadata" > 
															<th width="20%" valign="top" class="md">
																<span id="{../geonet:element/@uuid}">
																	<xsl:call-template name="getTitle">
																		<xsl:with-param name="name"   select="name(..)"/>
																		<xsl:with-param name="schema" select="$schema"/>
																	</xsl:call-template>
																</span>
															</th>
															<td class="padded" valign="top">
																<xsl:call-template name="getElementText">
																	<xsl:with-param name="schema" select="$schema"/>
																	<xsl:with-param name="edit"   select="$edit"/>
																</xsl:call-template>
															</td>
														</tr>
													</xsl:for-each>
												</table>
											</td>
										</tr>
									</xsl:when>
									<xsl:otherwise>
										<tr>
											<td colspan="2">
												<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='stratum'  and gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']">
													<xsl:apply-templates mode="complexElement" select=".">
														<xsl:with-param name="schema" select="$schema"/>
														<xsl:with-param name="edit"   select="$edit"/>
														<xsl:with-param name="title">
															<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/PositioningInstrsTitle)"/>
														</xsl:with-param>
														<xsl:with-param name="content">
															<dl>
																<xsl:for-each select="./gmd:keyword">
																	<xsl:variable name="value" >
																		<xsl:value-of select="gco:CharacterString/text()"/>
																	</xsl:variable>
																	<xsl:variable name="name" select="local-name(.)"/> 
																	<xsl:variable name="qname">stratumKeyword</xsl:variable>
																	<!--
																		Get codelist from profil first and use use default one if not
																		available.
																	-->
																	<xsl:variable name="codelistProfil" select="/root/gui/schemas/*[name()=$schema]/codelists/codelist[@name = $qname]/*" />
																	<xsl:variable name="codelist" select="exslt:node-set($codelistProfil)" />
																	
																	<!-- codelist in view mode -->
																	<xsl:if test="normalize-space($value)!=''">
																		<dt><b><xsl:value-of select="$codelist/entry[code = normalize-space($value)]/label"/></b></dt>
																		<dd><xsl:value-of select="$codelist/entry[code = normalize-space($value)]/description"/></dd>
																	</xsl:if>
																</xsl:for-each>
															</dl>
														</xsl:with-param>
													</xsl:apply-templates>
												</xsl:for-each>
											</td>
										</tr>
									</xsl:otherwise>
								</xsl:choose>
								
								<xsl:apply-templates mode="elementEP" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:source/gmd:LI_Source/gmd:description">
									<xsl:with-param name="schema" select="$schema"/>
									<xsl:with-param name="edit"   select="$edit"/>
								</xsl:apply-templates>
								
								<!--
								<xsl:apply-templates mode="simpleElement" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:source/gmd:LI_Source/gmd:description/gco:CharacterString">
									<xsl:with-param name="schema" select="$schema"/>
									<xsl:with-param name="edit"   select="$edit"/>
									<xsl:with-param name="title">
										<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/labels/element[@name='gmd:description'  and @context='LI_Source']/label)"/>
									</xsl:with-param>
									<xsl:with-param name="helpLink">
										<xsl:call-template name="getHelpLink">
											<xsl:with-param name="name"   select="name(gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:source/gmd:LI_Source/gmd:description)"/>
											<xsl:with-param name="schema" select="$schema"/>
										</xsl:call-template>
									</xsl:with-param>
									<xsl:with-param name="text">
										<xsl:for-each select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:source/gmd:LI_Source/gmd:description/gco:CharacterString">
											<xsl:call-template name="getElementText">
												<xsl:with-param name="schema" select="$schema"/>
												<xsl:with-param name="edit"   select="$edit"/>
											</xsl:call-template>
										</xsl:for-each>
									</xsl:with-param>
								</xsl:apply-templates>-->
								
								
								<xsl:call-template name="complexElementGuiWrapper">
									<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/dataProcessingTitle"/>
									<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/dataProcessingTitle)"/>
									<xsl:with-param name="content">
										
										<xsl:apply-templates mode="elementEP" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage[gmd:source]/gmd:statement">
											<xsl:with-param name="schema" select="$schema"/>
											<xsl:with-param name="edit"   select="$edit"/>
										</xsl:apply-templates>
									
										<xsl:call-template name="simpleElementGui">
											<xsl:with-param name="schema" select="$schema"/>
											<xsl:with-param name="edit"   select="true()"/>
											<xsl:with-param name="title">
												<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/processingSoftwareTitle)"/>
											</xsl:with-param>
											<xsl:with-param name="text">
												<xsl:for-each select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope[gmd:level/gmd:MD_ScopeCode/@codeListValue='software']/gmd:extent/gmd:EX_Extent/gmd:description/gco:CharacterString">
													<xsl:call-template name="getElementText">
														<xsl:with-param name="schema" select="$schema"/>
														<xsl:with-param name="edit"   select="$edit"/>
													</xsl:call-template>
													
												</xsl:for-each>
											</xsl:with-param>
											<xsl:with-param name="helpLink">
												<xsl:call-template name="getHelpLink">
													<xsl:with-param name="name" select="name(gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope[gmd:level/gmd:MD_ScopeCode/@codeListValue='software']/gmd:extent/gmd:EX_Extent/gmd:description)"/>
													<xsl:with-param name="schema" select="$schema"/>
												</xsl:call-template>
											</xsl:with-param>
											<xsl:with-param name="validationLink">
												<xsl:variable name="ref" select="concat('#_',gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope[gmd:level/gmd:MD_ScopeCode/@codeListValue='software']/gmd:extent/gmd:EX_Extent/gmd:description/gco:CharacterString/geonet:element/@ref)"/>
												<xsl:call-template name="validationLink">
													<xsl:with-param name="ref" select="$ref"/>
												</xsl:call-template>
											</xsl:with-param>
											<xsl:with-param name="id" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope[gmd:level/gmd:MD_ScopeCode/@codeListValue='software']/gmd:extent/gmd:EX_Extent/gmd:description/gco:CharacterString/geonet:element/@uuid"/>
										</xsl:call-template>
									</xsl:with-param>
									
								</xsl:call-template>
							</xsl:with-param>
						</xsl:call-template>
						
						
						
						<xsl:call-template name="complexElementGuiWrapper">
							<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/qualityTitle"/>
							<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/qualityTitle)"/>
							<xsl:with-param name="content">
								
								<xsl:apply-templates mode="complexElement" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy">
									<xsl:with-param name="schema" select="$schema"/>
									<xsl:with-param name="edit"   select="$edit"/>
									<xsl:with-param name="title">
										<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/horAccuracyTitle)"/>
									</xsl:with-param>
									<xsl:with-param name="content">
										<xsl:apply-templates mode="elementEP" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy/gmd:measureDescription">
											<xsl:with-param name="schema" select="$schema"/>
											<xsl:with-param name="edit"   select="$edit"/>
										</xsl:apply-templates>
										<xsl:for-each select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy/gmd:result/gmd:DQ_QuantitativeResult/gmd:value/gco:Record">
											<xsl:call-template name="simpleElementGui">
												<xsl:with-param name="schema" select="$schema"/>
												<xsl:with-param name="edit"   select="$edit"/>
												<xsl:with-param name="title">
													<xsl:call-template name="getTitle">
														<xsl:with-param name="name"   select="name(.)"/>
														<xsl:with-param name="schema" select="$schema"/>
													</xsl:call-template>
												</xsl:with-param>
												<xsl:with-param name="text">
													<xsl:call-template name="getElementText">
														<xsl:with-param name="schema" select="$schema"/>
														<xsl:with-param name="edit"   select="$edit"/>
													</xsl:call-template>
												</xsl:with-param>
												<xsl:with-param name="helpLink">
													<xsl:call-template name="getHelpLink">
														<xsl:with-param name="name" select="name(.)"/>
														<xsl:with-param name="schema" select="$schema"/>
													</xsl:call-template>
												</xsl:with-param>
												<xsl:with-param name="validationLink">
													<xsl:variable name="ref" select="concat('#_',./geonet:element/@ref)"/>
													<xsl:call-template name="validationLink">
														<xsl:with-param name="ref" select="$ref"/>
													</xsl:call-template>
												</xsl:with-param>
												<xsl:with-param name="removeLink">
													<xsl:variable name="id" select="../../../geonet:element/@uuid"/>
													<xsl:value-of select="concat('doRemoveElementAction(',$apos,'/metadata.elem.delete',$apos,',',../../../geonet:element/@ref,',',../../../geonet:element/@parent,',',$apos,$id,$apos,',',../../../geonet:element/@min,');')"/>
												</xsl:with-param>           
												<xsl:with-param name="id" select="../../../geonet:element/@uuid"/>
											</xsl:call-template>
										</xsl:for-each>
										<xsl:apply-templates mode="elementEP" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy/gmd:evaluationMethodDescription">
											<xsl:with-param name="schema" select="$schema"/>
											<xsl:with-param name="edit"   select="$edit"/>
										</xsl:apply-templates>
										
									</xsl:with-param>
								</xsl:apply-templates>
								
								
								<xsl:apply-templates mode="complexElement" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_QuantitativeAttributeAccuracy">
									<xsl:with-param name="schema" select="$schema"/>
									<xsl:with-param name="edit"   select="$edit"/>
									<xsl:with-param name="title">
										<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/vertAccuracyTitle)"/>
									</xsl:with-param>
									<xsl:with-param name="content">
										<xsl:apply-templates mode="elementEP" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/gmd:measureDescription">
											<xsl:with-param name="schema" select="$schema"/>
											<xsl:with-param name="edit"   select="$edit"/>
										</xsl:apply-templates>
										<xsl:apply-templates mode="elementEP" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/gmd:evaluationMethodDescription">
											<xsl:with-param name="schema" select="$schema"/>
											<xsl:with-param name="edit"   select="$edit"/>
										</xsl:apply-templates>
									</xsl:with-param>
								</xsl:apply-templates>
								
								<xsl:if test="$type='hydrography'">
									<!--Shoal bias (Y/N)
									-->
									<xsl:apply-templates mode="elementEP" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_NonQuantitativeAttributeAccuracy/gmd:measureDescription">
										<xsl:with-param name="schema" select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
									</xsl:apply-templates>
								</xsl:if>
								
							</xsl:with-param>
						</xsl:call-template>
						
						
						
						
						<xsl:call-template name="complexElementGuiWrapper">
							<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/suitabilityTitle"/>
							<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/suitabilityTitle)"/>
							<xsl:with-param name="content">
								<xsl:apply-templates mode="simpleElement" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation">
									<xsl:with-param name="schema" select="$schema"/>
									<xsl:with-param name="edit"   select="$edit"/>
									<xsl:with-param name="text">
										<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString">
											<xsl:call-template name="getElementText">
												<xsl:with-param name="schema" select="$schema"/>
												<xsl:with-param name="edit"   select="$edit"/>
											</xsl:call-template>
										</xsl:for-each>
									</xsl:with-param>
								</xsl:apply-templates>
							</xsl:with-param>
						</xsl:call-template>



						<xsl:call-template name="complexElementGuiWrapper">
							<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/intellectualPropertyTitle"/>
							<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/intellectualPropertyTitle)"/>
							<xsl:with-param name="content">
								
								<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation">
									<xsl:with-param name="schema" select="$schema"/>
									<xsl:with-param name="edit"   select="$edit"/>
								</xsl:apply-templates>
								<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:accessConstraints">
									<xsl:with-param name="schema" select="$schema"/>
									<xsl:with-param name="edit"   select="$edit"/>
								</xsl:apply-templates>
								<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useConstraints">
									<xsl:with-param name="schema" select="$schema"/>
									<xsl:with-param name="edit"   select="$edit"/>
								</xsl:apply-templates>
								
								<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints">
									<xsl:with-param name="schema" select="$schema"/>
									<xsl:with-param name="edit"   select="$edit"/>
								</xsl:apply-templates>
							</xsl:with-param>
						</xsl:call-template>
						
						
						
						<xsl:choose>
							<xsl:when test="$edit=true()">
								<xsl:apply-templates mode="simpleElement" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date">
									<xsl:with-param name="schema"  select="$schema"/>
									<xsl:with-param name="edit"   select="$edit"/>
									<xsl:with-param name="title">
										<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/creationDateTitle)"/>
									</xsl:with-param>
									<xsl:with-param name="text">
										<xsl:variable name="ref" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:Date/geonet:element/@ref|gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:DateTime/geonet:element/@ref"/>
										<xsl:variable name="format">
											<xsl:choose>
												<xsl:when test="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:Date"><xsl:text>%Y-%m-%d</xsl:text></xsl:when>
												<xsl:otherwise><xsl:text>%Y-%m-%dT%H:%M:00</xsl:text></xsl:otherwise>
											</xsl:choose>
										</xsl:variable>
										
										<xsl:call-template name="calendar">
											<xsl:with-param name="ref" select="$ref"/>
											<xsl:with-param name="date" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:DateTime/text()|gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:Date/text()"/>
											<xsl:with-param name="format" select="$format"/>
										</xsl:call-template>
										
									</xsl:with-param>
								</xsl:apply-templates>
							</xsl:when>
							<xsl:otherwise>
								<xsl:apply-templates mode="simpleElement" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date">
									<xsl:with-param name="schema"  select="$schema"/>
									<xsl:with-param name="edit"   select="$edit"/>
									<xsl:with-param name="title">
										<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/creationDateTitle)"/>
									</xsl:with-param>
								</xsl:apply-templates>
							</xsl:otherwise>
						</xsl:choose>
						
						<xsl:choose>
							<xsl:when test="$edit=true()">
								<xsl:apply-templates mode="simpleElement" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date">
									<xsl:with-param name="schema"  select="$schema"/>
									<xsl:with-param name="edit"   select="$edit"/>
									<xsl:with-param name="title">
										<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/revisionDateTitle)"/>
									</xsl:with-param>
									<xsl:with-param name="text">
										<xsl:variable name="ref" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date/geonet:element/@ref|gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:DateTime/geonet:element/@ref"/>
										<xsl:variable name="format">
											<xsl:choose>
												<xsl:when test="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date"><xsl:text>%Y-%m-%d</xsl:text></xsl:when>
												<xsl:otherwise><xsl:text>%Y-%m-%dT%H:%M:00</xsl:text></xsl:otherwise>
											</xsl:choose>
										</xsl:variable>
										
										<xsl:call-template name="calendar">
											<xsl:with-param name="ref" select="$ref"/>
											<xsl:with-param name="date" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:DateTime/text()|gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date/text()"/>
											<xsl:with-param name="format" select="$format"/>
										</xsl:call-template>
									</xsl:with-param>
								</xsl:apply-templates>
							</xsl:when>
							<xsl:otherwise>
								<xsl:apply-templates mode="simpleElement" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date">
									<xsl:with-param name="schema"  select="$schema"/>
									<xsl:with-param name="edit"   select="$edit"/>
									<xsl:with-param name="title">
										<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/revisionDateTitle)"/>
									</xsl:with-param>
								</xsl:apply-templates>
							</xsl:otherwise>
						</xsl:choose>
						
					</xsl:with-param>
				</xsl:apply-templates>
				
			</xsl:with-param>
		</xsl:call-template>
		
		
		
		
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/whereTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/whereTitle)"/>
			<xsl:with-param name="content">
				
				<!-- FIXME -->
				<xsl:apply-templates mode="complexElement" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:distance">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="title">
						<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/spatialResTitle)"/>
					</xsl:with-param>
				</xsl:apply-templates>
				
				
				
				<xsl:apply-templates mode="simpleElement" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier[not(gmd:RS_Identifier/gmd:codeSpace)]/gmd:MD_Identifier/gmd:code">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="title">
						<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/geoAreaNameTitle)"/>
					</xsl:with-param>
					<xsl:with-param name="text">
						<!-- FIXME -->
						<xsl:apply-templates mode="iso19139ComboFromCodelistForCharacterString" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier[not(gmd:RS_Identifier/gmd:codeSpace)]/gmd:MD_Identifier/gmd:code">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
					</xsl:with-param>
					<xsl:with-param name="helpLink"/>
				</xsl:apply-templates>
				
				<!-- FIXME : Add list of region from thesaurus ? -->
				<xsl:apply-templates mode="iso19139" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="complexElement" select="gmd:spatialRepresentationInfo">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="title">
						<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/depthTitle)"/>
					</xsl:with-param>
					<xsl:with-param name="content">
						<xsl:apply-templates mode="simpleElement" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope/gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:minimumValue/gco:Real">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
						<xsl:apply-templates mode="simpleElement" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope/gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:maximumValue/gco:Real">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
					</xsl:with-param>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:referenceSystemInfo">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
				
				<xsl:if test="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier[gmd:RS_Identifier/gmd:codeSpace]/gmd:RS_Identifier/gmd:code">
					<xsl:call-template name="complexElementGui">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="title">
							<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/userCRSTitle)"/>
						</xsl:with-param>
						<xsl:with-param name="content">
							<xsl:apply-templates mode="simpleElement" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier[gmd:RS_Identifier/gmd:codeSpace]/gmd:RS_Identifier/gmd:code/gco:CharacterString">
								<xsl:with-param name="schema"  select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
								<xsl:with-param name="title">
									<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/geodeticSystemAndProjectionTitle)"/>
								</xsl:with-param>
								<xsl:with-param name="helpLink">
									<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier[gmd:RS_Identifier/gmd:codeSpace]/gmd:RS_Identifier">
										<xsl:call-template name="getHelpLink">
											<xsl:with-param name="name"   select="name(.)"/>
											<xsl:with-param name="schema" select="$schema"/>
										</xsl:call-template>
									</xsl:for-each>
								</xsl:with-param>
							</xsl:apply-templates>
							<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:description[../gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:RS_Identifier/gmd:codeSpace]">
								<xsl:with-param name="schema"  select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:apply-templates>
						</xsl:with-param>
						<xsl:with-param name="helpLink">
							<xsl:call-template name="getHelpLink">
								<xsl:with-param name="name"   select="name(gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:RS_Identifier)"/>
								<xsl:with-param name="schema" select="$schema"/>
							</xsl:call-template>
						</xsl:with-param>
						<xsl:with-param name="removeLink">
							<xsl:variable name="id" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:RS_Identifier/gmd:codeSpace]/geonet:element/@uuid"/>
							<xsl:value-of select="concat('doRemoveElementAction(',$apos,'/metadata.elem.delete',$apos,',',gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:RS_Identifier/gmd:codeSpace]/geonet:element/@ref,',',gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:RS_Identifier/gmd:codeSpace]/geonet:element/@parent,',',$apos,$id,$apos,',',gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:RS_Identifier/gmd:codeSpace]/geonet:element/@min,');')"/>
							<xsl:if test="not(gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:RS_Identifier/gmd:codeSpace]/geonet:element/@del='true')">
								<xsl:text>!OPTIONAL</xsl:text>
							</xsl:if>
						</xsl:with-param>
						<xsl:with-param name="id" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:RS_Identifier/gmd:codeSpace]/geonet:element/@uuid"/>
						<xsl:with-param name="validationLink">
							<xsl:variable name="ref" select="concat('#_',gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent[gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:RS_Identifier/gmd:codeSpace]/geonet:element/@ref)"/>
							<xsl:call-template name="validationLink">
								<xsl:with-param name="ref" select="$ref"/>
							</xsl:call-template>
						</xsl:with-param>
					</xsl:call-template>
				</xsl:if>
				
				
				<xsl:apply-templates mode="simpleElement" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope/gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent//gml:VerticalDatum/gml:identifier">
					<xsl:with-param name="schema"   select="$schema"/>
					<xsl:with-param name="edit"     select="$edit"/>
					
					<xsl:with-param name="text">
						<!-- FIXME -->
						<xsl:apply-templates mode="iso19139ComboFromCodelistForCharacterString" select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope/gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent//gml:VerticalDatum/gml:identifier">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
					</xsl:with-param>
				</xsl:apply-templates>
				
			</xsl:with-param>
		</xsl:call-template>
		
		
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/whenTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/whenTitle)"/>
			<xsl:with-param name="content">
				
				<xsl:choose>
					<xsl:when test="$edit=true()">
						<xsl:apply-templates mode="simpleElement" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:*/gml:beginPosition">
							<xsl:with-param name="schema"  select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
							<xsl:with-param name="text">
								<xsl:variable name="ref" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:*/gml:beginPosition/geonet:element/@ref"/>
								<xsl:variable name="format">
									<xsl:choose>
										<xsl:when test="gco:Date"><xsl:text>%Y-%m-%d</xsl:text></xsl:when>
										<xsl:otherwise><xsl:text>%Y-%m-%dT%H:%M:00</xsl:text></xsl:otherwise>
									</xsl:choose>
								</xsl:variable>
								
								<xsl:call-template name="calendar">
									<xsl:with-param name="ref" select="$ref"/>
									<xsl:with-param name="date" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:*/gml:beginPosition"/>
									<xsl:with-param name="format" select="$format"/>
								</xsl:call-template>
								
							</xsl:with-param>
						</xsl:apply-templates>
						
						<xsl:apply-templates mode="simpleElement" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:*/gml:endPosition">
							<xsl:with-param name="schema"  select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
							<xsl:with-param name="text">
								<xsl:variable name="ref" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:*/gml:endPosition/geonet:element/@ref"/>
								<xsl:variable name="format">
									<xsl:choose>
										<xsl:when test="gco:Date"><xsl:text>%Y-%m-%d</xsl:text></xsl:when>
										<xsl:otherwise><xsl:text>%Y-%m-%dT%H:%M:00</xsl:text></xsl:otherwise>
									</xsl:choose>
								</xsl:variable>
								
								<xsl:call-template name="calendar">
									<xsl:with-param name="ref" select="$ref"/>
									<xsl:with-param name="date" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:*/gml:endPosition"/>
									<xsl:with-param name="format" select="$format"/>
								</xsl:call-template>
							</xsl:with-param>
						</xsl:apply-templates>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod">
							<xsl:with-param name="schema"  select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
					</xsl:otherwise>
				</xsl:choose>
				
				<xsl:apply-templates mode="complexElement" select="gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='time']/gmd:resolution">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="title"><xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/tempResolutionTitle)"/></xsl:with-param>
					<xsl:with-param name="content">
						<xsl:call-template name="simpleElementGui">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
							<xsl:with-param name="title">
								<xsl:for-each select="gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='time']/gmd:resolution/gco:Measure">
									<xsl:call-template name="getTitle">
										<xsl:with-param name="name"   select="name(.)"/>
										<xsl:with-param name="schema" select="$schema"/>
									</xsl:call-template>
								</xsl:for-each>
							</xsl:with-param>
							<xsl:with-param name="text">
								<xsl:for-each select="gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='time']/gmd:resolution/gco:Measure">
									<xsl:call-template name="getElementText">
										<xsl:with-param name="schema" select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
									</xsl:call-template>
									
								</xsl:for-each>
							</xsl:with-param>
							<xsl:with-param name="helpLink">
								<xsl:for-each select="gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='time']/gmd:resolution/gco:Measure">
									<xsl:call-template name="getHelpLink">
										<xsl:with-param name="name"   select="name(.)"/>
										<xsl:with-param name="schema" select="$schema"/>
									</xsl:call-template>
								</xsl:for-each>
							</xsl:with-param>
							<xsl:with-param name="validationLink">
								<xsl:variable name="ref" select="concat('#_',gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='time']/gmd:resolution/gco:Measure/geonet:element/@ref)"/>
								<xsl:call-template name="validationLink">
									<xsl:with-param name="ref" select="$ref"/>
								</xsl:call-template>
							</xsl:with-param>
							<xsl:with-param name="id" select="gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='time']/gmd:resolution/gco:Measure/geonet:element/@uuid"/>
						</xsl:call-template>
						<xsl:apply-templates mode="simpleElement" select="gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension[gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='time']/gmd:resolution/gco:Measure/@uom">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>  
						
					</xsl:with-param>
				</xsl:apply-templates>
				
			</xsl:with-param>
		</xsl:call-template>
		
		
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/whoTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/whoTitle)"/>
			<xsl:with-param name="content">
				<xsl:apply-templates mode="complexElement" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='originator']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="title">
						<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/originatorTitle)"/>
					</xsl:with-param>
					<xsl:with-param name="content">
						<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='originator']/gmd:organisationName">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>  
						<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='originator']/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
					</xsl:with-param>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="complexElement" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='custodian']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="title">
						<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/dataHoldingCenterTitle)"/>
					</xsl:with-param>
					<xsl:with-param name="content">
						<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='custodian']/gmd:organisationName">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>  
						<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue='custodian']/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:with-param>
		</xsl:call-template>
		
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/whereToFindTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/whereToFindTitle)"/>
			<xsl:with-param name="content">
				
				<xsl:apply-templates mode="simpleElement" select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="title">
						<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/labels/element[@name='gmd:MD_Distributor']/label)"/>
					</xsl:with-param>
					<xsl:with-param name="helpLink">
						<xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:organisationName">
							<xsl:call-template name="getHelpLink">
								<xsl:with-param name="name"   select="name(.)"/>
								<xsl:with-param name="schema" select="$schema"/>
							</xsl:call-template>
						</xsl:for-each>
					</xsl:with-param>
				</xsl:apply-templates>
				
				
				<xsl:apply-templates mode="complexElement" select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="title">
						<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/distriFormatTitle)"/>
					</xsl:with-param>
				</xsl:apply-templates>
				
				
				<xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:transferSize/gco:Real">
					<xsl:call-template name="simpleElementGui">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="title">
							<xsl:call-template name="getTitle">
								<xsl:with-param name="name"   select="name(..)"/>
								<xsl:with-param name="schema" select="$schema"/>
							</xsl:call-template>
						</xsl:with-param>
						<xsl:with-param name="text">
							<xsl:call-template name="getElementText">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:call-template>
						</xsl:with-param>
						<xsl:with-param name="helpLink">
							<xsl:call-template name="getHelpLink">
								<xsl:with-param name="name" select="name(..)"/>
								<xsl:with-param name="schema" select="$schema"/>
							</xsl:call-template>
						</xsl:with-param>
						<xsl:with-param name="validationLink">
							<xsl:variable name="ref" select="concat('#_',./geonet:element/@ref)"/>
							<xsl:call-template name="validationLink">
								<xsl:with-param name="ref" select="$ref"/>
							</xsl:call-template>
						</xsl:with-param>
						<xsl:with-param name="removeLink">
							<xsl:variable name="id" select="../../../geonet:element/@uuid"/>
							<xsl:value-of select="concat('doRemoveElementAction(',$apos,'/metadata.elem.delete',$apos,',',../../../geonet:element/@ref,',',../../../geonet:element/@parent,',',$apos,$id,$apos,',',../../../geonet:element/@min,');')"/>
						</xsl:with-param>               
						<xsl:with-param name="id" select="../../../geonet:element/@uuid"/>
					</xsl:call-template>
				</xsl:for-each>
				
				
				<xsl:apply-templates mode="complexElement" select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
				
				
				<xsl:for-each select="gmd:contact/gmd:CI_ResponsibleParty">
					<xsl:apply-templates mode="complexElement" select=".">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="title">
							<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/labels/element[@name='gmd:contact']/label)"/>
						</xsl:with-param>
						<xsl:with-param name="content">
							<xsl:apply-templates mode="elementEP" select="./gmd:organisationName">
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
		
	</xsl:template>
	
	
	
	<!-- =================================================================== -->
	<!-- ComboBox using Codelist -->
	<!-- =================================================================== -->
	
	<xsl:template mode="iso19139ComboFromCodelistForCharacterString" match="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope/gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent//gml:VerticalDatum/gml:identifier|
		gmd:metadataConstraints/gmd:MD_LegalConstraints/gmd:useLimitation|
		gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier[not(gmd:RS_Identifier/gmd:codeSpace)]/gmd:MD_Identifier/gmd:code|
		gmd:hierarchyLevelName[gmx:Anchor]|
		gmd:MD_Keywords[gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='theme' and following-sibling::gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://www.seadatanet.org/urnurl/']]">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/> 
		
		<xsl:variable name="name"     select="local-name(.)"/>
		<xsl:variable name="qname" >
			<xsl:choose>
				<xsl:when test="name(.)='gml:identifier'">verticalDatumId</xsl:when>
				<xsl:when test="name(.)='gmd:useLimitation'">metadataDisseminationLevel</xsl:when>
				<xsl:when test="name(.)='gmd:code'">geographicalArea</xsl:when>
				<xsl:when test="name(.)='gmd:MD_Keywords'">projectName</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="name(.)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:variable name="value" >
			<xsl:choose>
				<xsl:when test="name(.)='gmd:useLimitation' or name(.)='gmd:code'">
					<xsl:value-of select="gco:CharacterString/text()"/>
				</xsl:when>
				<xsl:when test="name(.)='gmd:hierarchyLevelName'">
					<xsl:value-of select="gmx:Anchor/text()"/>
				</xsl:when>
				<xsl:when test="name(.)='gml:identifier'">
					<xsl:value-of select="text()"/>
				</xsl:when>
				<xsl:when test="name(.)='gmd:MD_Keywords'">
					<xsl:value-of select="gmd:keyword/gco:CharacterString/text()"/> 
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="."/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<!--
			Get codelist from profil first and use use default one if not
			available.
		-->
		<xsl:variable name="codelistProfil">
			<xsl:choose>
				<xsl:when test="starts-with($schema,'iso19139.')">
					<xsl:copy-of
						select="/root/gui/schemas/*[name(.)=$schema]/codelists/codelist[@name = $qname]/*" />
				</xsl:when>
				<xsl:otherwise />
			</xsl:choose>
		</xsl:variable>
		
		<xsl:variable name="codelistCore">
			<xsl:choose>
				<xsl:when test="normalize-space($codelistProfil)!=''">
					<xsl:copy-of select="$codelistProfil" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of
						select="/root/gui/schemas/*[name(.)='iso19139']/codelists/codelist[@name = $qname]/*" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:variable name="codelist" select="exslt:node-set($codelistCore)" />
		<xsl:variable name="isXLinked" select="count(ancestor-or-self::node()[@xlink:href]) > 0" />
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<!-- codelist in edit mode -->
				
				<xsl:variable name="ref">
					<xsl:choose>
						<xsl:when test="name(.)='gmd:useLimitation' or name(.)='gmd:code'">
							<xsl:value-of select="gco:CharacterString/geonet:element/@ref"/>
						</xsl:when>
						<xsl:when test="name(.)='gmd:hierarchyLevelName'">
							<xsl:value-of select="gmx:Anchor/geonet:element/@ref"/>
						</xsl:when>
						<xsl:when test="name(.)='gml:identifier'">
							<xsl:value-of select="./geonet:element/@ref"/>
						</xsl:when>
						<xsl:when test="name(.)='gmd:MD_Keywords'">
							<xsl:value-of select="gmd:keyword/gco:CharacterString/geonet:element/@ref"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="../geonet:element/@ref"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				
				<select class="md" name="_{$ref}" id="_{$ref}" size="1" 
					>
					<!-- onChange="console.log(document.mainForm._{$ref});document.mainForm._{$ref}.value=this.options[this.selectedIndex].text"
					is not required when the combo is the element -->
					<xsl:if test="../../geonet:element/@min='1' and $edit">
						<xsl:attribute name="onchange">validateNonEmpty(this);</xsl:attribute>
					</xsl:if>
					<xsl:if test="$isXLinked">
						<xsl:attribute name="disabled">disabled</xsl:attribute>
					</xsl:if>
					<option/>
					<!-- Check element is mandatory or not -->
					<xsl:for-each select="$codelist/entry[not(@hideInEditMode)]">
						<xsl:sort select="label"/>
						<option>
							<xsl:if test="code=normalize-space($value)">
								<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:attribute name="value"><xsl:value-of select="code"/></xsl:attribute>
							<xsl:value-of select="label"/>
						</option>
					</xsl:for-each>
				</select>
			</xsl:when>
			<xsl:otherwise>
				<!-- codelist in view mode -->
				<xsl:if test="normalize-space($value)!=''">
					<b><xsl:value-of select="$codelist/entry[code = normalize-space($value)]/label"/></b>
					<xsl:value-of select="concat(': ',$codelist/entry[code = normalize-space($value)]/description)"/>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	<!-- =================================================================== -->
	<!-- List using Codelist -->
	<!-- =================================================================== -->
	
	<xsl:template mode="iso19139ComboFromCodelistForCharacterString" match="gmd:MD_Keywords[gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='discipline' and following-sibling::gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']]|
		gmd:MD_Keywords[gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='theme' and following-sibling::gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']]|
		gmd:MD_Keywords[gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='stratum' and following-sibling::gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']]">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/> 
		
		<xsl:variable name="name"     select="local-name(.)"/> 
		<xsl:variable name="qname" >
			<xsl:choose>
				<xsl:when test=".[gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='discipline']]">disciplineKeyword</xsl:when>
				<xsl:when test=".[gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='theme']]">themeKeyword</xsl:when>
				<xsl:when test=".[gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='stratum']]">stratumKeyword</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="name(.)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:variable name="value" select="string-join(.//gmd:keyword/gco:CharacterString, ' ')"/>
			
		<!--
			Get codelist from profil first and use use default one if not
			available.
		-->
		<xsl:variable name="codelistProfil">
			<xsl:choose>
				<xsl:when test="starts-with($schema,'iso19139.')">
					<xsl:copy-of
						select="/root/gui/schemas/*[name(.)=$schema]/codelists/codelist[@name = $qname]/*" />
				</xsl:when>
				<xsl:otherwise />
			</xsl:choose>
		</xsl:variable>
		
		<xsl:variable name="codelistCore">
			<xsl:choose>
				<xsl:when test="normalize-space($codelistProfil)!=''">
					<xsl:copy-of select="$codelistProfil" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of
						select="/root/gui/schemas/*[name(.)='iso19139']/codelists/codelist[@name = $qname]/*" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:variable name="codelist" select="exslt:node-set($codelistCore)" />
		<xsl:variable name="isXLinked" select="count(ancestor-or-self::node()[@xlink:href]) > 0" />
		
		<xsl:variable name="size">
			<xsl:choose>
				<xsl:when test="not(count($codelist/entry[not(@hideInEditMode)]) > 5)">
					<xsl:value-of select="count($codelist/entry[not(@hideInEditMode)])" />
				</xsl:when>
				<xsl:otherwise>5</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<!-- codelist in edit mode -->
				<xsl:variable name="ref">
					<xsl:choose>
						<xsl:when test="name(.)='gmd:MD_Keywords'">
							<xsl:value-of select="gmd:keyword[1]/gco:CharacterString/geonet:element/@ref"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="./geonet:element/@ref"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:variable name="parentRef">
					<xsl:choose>
						<xsl:when test="name(.)='gmd:MD_Keywords'">
							<xsl:value-of select="./geonet:element/@ref"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="../geonet:element/@ref"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				
				<xsl:variable name="uuid">
					<xsl:choose>
						<xsl:when test="name(.)='gmd:MD_Keywords'">
							<xsl:value-of select="gmd:keyword[1]/geonet:element/@uuid"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="./geonet:element/@uuid"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				
				<xsl:variable name="elemName">
					<xsl:choose>
						<xsl:when test="name(.)='gmd:MD_Keywords'">
							<xsl:value-of select="name(./gmd:keyword[1])"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="name(.)"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				
				<select class="md" id="list_{$uuid}" size="{$size}">
					<xsl:attribute name="multiple"/>
					
					<xsl:choose>
						<xsl:when test=".[gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='discipline']]">
							<xsl:attribute name="onchange">validateNonEmpty(this);if (noDoubleClick()) doAddElementAction('discipline',<xsl:value-of select="$parentRef"/>,'<xsl:value-of select="$elemName"/>','list_<xsl:value-of select="$ref"/>','<xsl:value-of select="$uuid"/>',null,null,true);</xsl:attribute>
						</xsl:when>
						<xsl:when test=".[gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='theme']]">
							<xsl:attribute name="onchange">validateNonEmpty(this);if (noDoubleClick()) doAddElementAction('theme',<xsl:value-of select="$parentRef"/>,'<xsl:value-of select="$elemName"/>','list_<xsl:value-of select="$ref"/>','<xsl:value-of select="$uuid"/>',null,null,true);</xsl:attribute>
						</xsl:when>
						<xsl:when test=".[gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='stratum']]">
							<xsl:attribute name="onchange">validateNonEmpty(this);if (noDoubleClick()) doAddElementAction('stratum',<xsl:value-of select="$parentRef"/>,'<xsl:value-of select="$elemName"/>','list_<xsl:value-of select="$ref"/>','<xsl:value-of select="$uuid"/>',null,null,true);</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:if test="../../geonet:element/@min='1' and $edit">
								<xsl:attribute name="onchange">validateNonEmpty(this);<xsl:value-of select="$elemName"/>','list_<xsl:value-of select="$ref"/>','<xsl:value-of select="$uuid"/>','xlink:href','http://www.myocean.eu.org/2009/resource/vocabulary/delay-mode');</xsl:attribute><!--validateNonEmpty(this);-->
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
					
					<xsl:if test="$isXLinked">
						<xsl:attribute name="disabled">disabled</xsl:attribute>
					</xsl:if>
					<!-- Check element is mandatory or not -->
					<xsl:for-each select="$codelist/entry[not(@hideInEditMode)]">
						<xsl:sort select="label"/>
						<option>
							<xsl:choose>
								<xsl:when test="contains($value, substring-before(code, ' = ')) and $value!=''">
									<xsl:attribute name="selected"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:if test="code=normalize-space($value)">
										<xsl:attribute name="selected"/>
									</xsl:if>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:attribute name="value"><xsl:value-of select="code"/></xsl:attribute>
							<xsl:value-of select="label"/>
						</option>
					</xsl:for-each>
				</select>
			</xsl:when>
			<xsl:otherwise>
				<!-- codelist in view mode -->
				<xsl:if test="normalize-space($value)!=''">
					<b><xsl:value-of select="$codelist/entry[code = normalize-space($value)]/label"/></b>
					<xsl:value-of select="concat(': ',$codelist/entry[code = normalize-space($value)]/description)"/>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	<!-- Hook custom controls for keywords -->
	<xsl:template mode="iso19139.emodnet.chemistry" match="gmd:MD_Keywords[gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='discipline' and following-sibling::gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']]|
		gmd:MD_Keywords[gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='stratum' and following-sibling::gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']]|
		gmd:MD_Keywords[gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='theme' and following-sibling::gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='http://vocab.ndg.nerc.ac.uk/']]" priority="1">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:choose>
			<xsl:when test=".[gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='discipline']]">
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"   select="$schema"/>
					<xsl:with-param name="edit"     select="$edit"/>
					<xsl:with-param name="title">
						<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/ParamsMeasuredTitle)"/>
					</xsl:with-param>
					<xsl:with-param name="text">
						<xsl:apply-templates mode="iso19139ComboFromCodelistForCharacterString" select=".">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test=".[gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='stratum']]">
				<xsl:call-template name="simpleElementGui">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="title">
						<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/PositioningInstrsTitle)"/>
					</xsl:with-param>
					<xsl:with-param name="text">
						<xsl:apply-templates mode="iso19139ComboFromCodelistForCharacterString" select=".">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
					</xsl:with-param>
					<xsl:with-param name="helpLink">
						<xsl:call-template name="getHelpLink">
							<xsl:with-param name="name" select="name(.)"/>
							<xsl:with-param name="schema" select="$schema"/>
						</xsl:call-template>
					</xsl:with-param>
					<xsl:with-param name="removeLink">
						<xsl:variable name="id" select="../geonet:element/@uuid"/>
						doRemoveElementAction('/metadata.elem.delete',<xsl:value-of select="../geonet:element/@ref"/>,<xsl:value-of select="../geonet:element/@parent"/>,'<xsl:value-of select="$id"/>',<xsl:value-of select="../geonet:element/@min"/>);element = document.getElementsByName("stratum")[0]; element.parentNode.removeChild(element);
					</xsl:with-param>
					<xsl:with-param name="id" select="../geonet:element/@uuid"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test=".[gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='theme']]">
				<xsl:call-template name="simpleElementGui">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="title">
						<xsl:value-of select="string(/root/gui/schemas/*[name()=$schema]/strings/MeasuringInstrsTitle)"/>
					</xsl:with-param>
					<xsl:with-param name="text">
						<xsl:apply-templates mode="iso19139ComboFromCodelistForCharacterString" select=".">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="$edit"/>
						</xsl:apply-templates>
					</xsl:with-param>
					<xsl:with-param name="helpLink">
						<xsl:call-template name="getHelpLink">
							<xsl:with-param name="name" select="name(.)"/>
							<xsl:with-param name="schema" select="$schema"/>
						</xsl:call-template>
					</xsl:with-param>
					<xsl:with-param name="removeLink">
						<xsl:variable name="id" select="../geonet:element/@uuid"/>
						doRemoveElementAction('/metadata.elem.delete',<xsl:value-of select="../geonet:element/@ref"/>,<xsl:value-of select="../geonet:element/@parent"/>,'<xsl:value-of select="$id"/>',<xsl:value-of select="../geonet:element/@min"/>);element = document.getElementsByName("theme")[0]; element.parentNode.removeChild(element);
					</xsl:with-param>
					<xsl:with-param name="id" select="../geonet:element/@uuid"/>
				</xsl:call-template>
			</xsl:when> 
			<xsl:otherwise>
				<xsl:call-template name="localizedCharStringField">
					<xsl:with-param name="schema" select="$schema" />
					<xsl:with-param name="edit" select="$edit" />
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>
