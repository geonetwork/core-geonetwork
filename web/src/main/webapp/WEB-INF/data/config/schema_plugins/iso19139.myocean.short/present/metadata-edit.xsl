<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
	xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx"
	xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gml="http://www.opengis.net/gml"
	xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:exslt="http://exslt.org/common" exclude-result-prefixes="#all">
	
	<xsl:include href="metadata-markup.xsl"/>
	<!-- main template - the way into processing iso19139.myocean.short -->
	<xsl:template name="metadata-iso19139.myocean.shortview-simple">
		<xsl:call-template name="metadata-iso19139view-simple"/>
	</xsl:template>

	<xsl:template mode="iso19139.myocean.short" match="*|@*"/> 
<!--	<xsl:template mode="iso19139.myocean.short" match="gmd:descriptiveKeywords" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema"  select="$schema"/>
			<xsl:with-param name="edit"    select="$edit"/>
			<xsl:with-param name="content">
				Create custom control for this element
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>-->
	

	<xsl:template mode="iso19139.myocean.short" match="gmd:contact|gmd:pointOfContact|gmd:citedResponsibleParty|gmd:distributorContact" priority="99">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:choose>
			<xsl:when test="contains($currTab, 'myocean')">
				<xsl:for-each select=".">
					<xsl:apply-templates mode="elementEP" select="gmd:CI_ResponsibleParty/gmd:organisationName">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>
					<xsl:apply-templates mode="elementEP" select="gmd:CI_ResponsibleParty/gmd:individualName|
						gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress
						">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="." mode="iso19139">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>
	
	
	<!-- A custom combo for projection list which is based on the helper
	list comming from the localization files and update an hidden input.
	-->
	<xsl:template mode="iso19139.myocean.short" match="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/
		gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:variable name="text">
					
					<xsl:variable name="ref" select="gco:CharacterString/geonet:element/@ref"/>
					<xsl:variable name="value" select="gco:CharacterString"/>
					<input type="hidden" class="md" name="_{$ref}" id="_{$ref}"  
						value="{$value}" size="30"/>
					
					<xsl:variable name="list">
						<xsl:copy-of select="/root/gui/schemas/*[name(.)=$schema]/labels/
							element[@context='gmd:MD_Metadata/gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code']/
							helper"/>
					</xsl:variable>
					
					<select id="s_{$ref}" name="s_{$ref}" size="1" 
						onchange="Ext.getDom('_{$ref}').value=this.options[this.selectedIndex].value; if (Ext.getDom('_{$ref}').onkeyup) Ext.getDom('_{$ref}').onkeyup();"
						class="md" >
						<option/>
						<xsl:for-each select="$list//option">
							<option value="{@value}">
								<xsl:if test="@value = $value">
									<xsl:attribute name="selected">selected</xsl:attribute>
								</xsl:if>
								<xsl:value-of select="."/>
							</option>
						</xsl:for-each>
					</select>
				</xsl:variable>
				
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
					<xsl:with-param name="text"   select="$text"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="iso19139">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit" select="$edit"></xsl:with-param>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>
	
	<!--
		A custom myocean control for distance
		
		As of now the user can chose a unit: we do not want this. Instead the
		user chooses between only two choices: kilometers or degrees. 
		Also we decided to force the user to convert degree fractions into a float. 
		So for both cases (kilometers or degrees) the input type is a float. 
		
		-->
	<xsl:template mode="iso19139.myocean.short" match="gmd:distance" priority="2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:variable name="text">
					<xsl:variable name="ref" select="gco:Distance/geonet:element/@ref"/>
					
					<input type="number" class="md" name="_{$ref}" id="_{$ref}"  
						onkeyup="validateNumber(this,true,true);"
						onchange="validateNumber(this,true,true);"
						value="{gco:Distance}" size="30"/>
					
					<!--&#160;
					<xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name = 'uom']/label"/>
					&#160;-->
					<label><input type="radio" name="ditance_uom_{$ref}" onclick="if (this.checked) document.getElementById('_{$ref}_uom').value = 'degree';">
						<xsl:if test="gco:Distance/@uom = 'degree'">
							<xsl:attribute name="checked">checked</xsl:attribute>
						</xsl:if>
					</input>Degree</label>
					<label><input type="radio" name="ditance_uom_{$ref}" onclick="if (this.checked) document.getElementById('_{$ref}_uom').value = 'km';">
						<xsl:if test="gco:Distance/@uom = 'km' or gco:Distance/@uom = ''">
							<xsl:attribute name="checked">checked</xsl:attribute>
						</xsl:if>
					</input>Kilometers</label>
					<input type="hidden" class="md" name="_{$ref}_uom" id="_{$ref}_uom"  
						value="{gco:Distance/@uom}" style="width:30px;"/>
				</xsl:variable>
				
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
					<xsl:with-param name="text"   select="$text"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="text">
						<xsl:value-of select="gco:Distance"/>
						<xsl:if test="gco:Distance/@uom"><xsl:text>&#160;</xsl:text>
							<xsl:choose>
								<xsl:when test="contains(gco:Distance/@uom, '#')">
									<a href="{gco:Distance/@uom}"><xsl:value-of select="substring-after(gco:Distance/@uom, '#')"/></a>
								</xsl:when>
								<xsl:otherwise><xsl:value-of select="gco:Distance/@uom"/></xsl:otherwise>
							</xsl:choose>
						</xsl:if>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>
	
	
	
	<xsl:template name="view-with-header-iso19139.myocean.short">
		<xsl:param name="tabs"/>

		<xsl:call-template name="view-with-header-iso19139">
			<xsl:with-param name="tabs" select="$tabs"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="metadata-iso19139.myocean.short" match="metadata-iso19139.myocean.short">
		<xsl:param name="schema"/>
		<xsl:param name="edit" select="false()"/>
		<xsl:param name="embedded"/>

		<!-- process in profile mode first -->
		<xsl:variable name="profileElements">
			<xsl:apply-templates mode="iso19139.myocean.short" select=".">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit" select="$edit"/>
				<xsl:with-param name="embedded" select="$embedded"/>
			</xsl:apply-templates>
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



	<xsl:template name="iso19139.myocean.shortCompleteTab">
		<xsl:param name="tabLink"/>
		<xsl:param name="schema"/>
		<xsl:call-template name="iso19139CompleteTab">
			<xsl:with-param name="tabLink" select="$tabLink"/>
			<xsl:with-param name="schema" select="$schema"/>
		</xsl:call-template>
		
		<xsl:call-template name="mainTab">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/myocean.simpleTab"/>
			<xsl:with-param name="default">myocean.simple</xsl:with-param>
			<xsl:with-param name="menu">
				<item label="myocean.simpleTab">myocean.simple</item>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>




	<!--
    Redirection template for profil fra in order to process 
    extraTabs.
  -->
	<xsl:template mode="iso19139.myocean.short" match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']"
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
			<xsl:when test="$currTab='myocean.simple'">
				<xsl:call-template name="myocean.simple">
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
	
	<!-- Simple view mode - apply to upstream products
	
	The most simple way to describe a product is to use the editing for dummy form.
	This form is dedicated to upstream products.
	-->
	<xsl:template name="myocean.simple">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:param name="dataset"/>
		<xsl:param name="core"/>
		
		
		
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/metadataInfoTitle"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/metadataInfoTitle)"/>
			<xsl:with-param name="content">
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/
																gmd:CI_Citation/gmd:title">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:fileIdentifier">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
				
			<!--	<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/
																gmd:CI_Citation/gmd:alternateTitle">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>-->
				
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:status">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
				
				<!--<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/
					gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']">
					<xsl:apply-templates mode="simpleElement" select=".">
						<xsl:with-param name="schema"  select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/creationDate"/>
						<xsl:with-param name="text">
							<xsl:variable name="ref" select="gmd:date/gco:Date/geonet:element/@ref|gmd:date/gco:DateTime/geonet:element/@ref"/>
							<xsl:variable name="format">
								<xsl:choose>
									<xsl:when test="gmd:date/gco:Date"><xsl:text>%Y-%m-%d</xsl:text></xsl:when>
									<xsl:otherwise><xsl:text>%Y-%m-%dT%H:%M:00</xsl:text></xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							
							<xsl:call-template name="calendar">
								<xsl:with-param name="ref" select="$ref"/>
								<xsl:with-param name="date" select="gmd:date"/>
								<xsl:with-param name="format" select="$format"/>
							</xsl:call-template>
							
						</xsl:with-param>
					</xsl:apply-templates>
				</xsl:for-each>-->
				
				
				<!--<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/
					gmd:CI_Citation/gmd:editionDate">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>-->
				
			</xsl:with-param>
		</xsl:call-template>
		
		
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/geoCoverage"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/geoCoverage)"/>
			<xsl:with-param name="content">
				
				<xsl:apply-templates mode="iso19139" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/
																gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
				
			</xsl:with-param>
		</xsl:call-template>
		
		
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/org"/>
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/org)"/>
			<xsl:with-param name="content">
		<!--<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/
																gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>-->
				
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/
					gmd:pointOfContact[gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue='pointOfContact']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
				
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
</xsl:stylesheet>
