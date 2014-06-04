<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx"
  xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gml="http://www.opengis.net/gml"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:geonet="http://www.fao.org/geonetwork"
  xmlns:sdn-product="http://myocean.org/geonetwork" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:exslt="http://exslt.org/common"
  exclude-result-prefixes="gmd gco gml gts srv xlink exslt geonet">

  <xsl:include href="metadata-markup.xsl"/>

  <!-- Simple views is ISO19139 -->
  <xsl:template name="metadata-iso19139.sdn-productview-simple">
    <xsl:call-template name="metadata-iso19139view-simple"/>
  </xsl:template>

  <xsl:template name="view-with-header-iso19139.sdn-product">
    <xsl:param name="tabs"/>
    <xsl:call-template name="view-with-header-iso19139">
      <xsl:with-param name="tabs" select="$tabs"/>
    </xsl:call-template>
  </xsl:template>

	<!--  custom templates for seadatanet-product -->

    <!-- Gegraphical extent selector -->
 
	<xsl:template mode="iso19139.sdn-product" match="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/
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

	<!--  custom temporal resolution -->

	<xsl:template mode="iso19139.sdn-product" match="gmd:resolution" priority="2">
		<xsl:param name="schema" />
		<xsl:param name="edit" />
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:variable name="text">
					<xsl:variable name="ref" select="gco:Measure/geonet:element/@ref" />
					<div>
						<input type="text" class="md" name="_{$ref}" id="_{$ref}"
	        			    style="width: inherit;"
							onkeyup="validateNumber(this,true,true);"
							onchange="validateNumber(this,true,true);"
							value="{gco:Measure}"/>


            <xsl:variable name="options" as="xs:string*" select="'day', 'month', 'year'"/>
            <xsl:variable name="uom" select="gco:Measure/@uom"/>

						<select  class="md" name="_{$ref}_uom" id="_{$ref}_uom" style="width: inherit; margin-top: 0;">
              <xsl:for-each select="$options">
                <option value="{.}">
                  <xsl:if test=". = $uom">
                    <xsl:attribute name="selected">selected</xsl:attribute>
                  </xsl:if>
                  <xsl:value-of select="."/></option>
              </xsl:for-each>
					  	</select>
				  	</div>
				</xsl:variable>
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/temporalResolution" />
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
					<xsl:with-param name="text"   select="$text"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="text">
						<xsl:value-of select="gco:Measure"/>
						<xsl:if test="gco:Measure/@uom"><xsl:text>&#160;</xsl:text>
							<xsl:choose>
								<xsl:when test="contains(gco:Measure/@uom, '#')">
									<a href="{gco:Measure/@uom}"><xsl:value-of select="substring-after(gco:Measure/@uom, '#')"/></a>
								</xsl:when>
								<xsl:otherwise><xsl:value-of select="gco:Measure/@uom"/></xsl:otherwise>
							</xsl:choose>
						</xsl:if>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

    <!-- custom distance / spatial resolution form -->

	<xsl:template mode="iso19139.sdn-product" match="gmd:distance" priority="2">
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

	<!-- Custom contact form -->

	<xsl:template mode="iso19139.sdn-product"
		match="gmd:contact|gmd:pointOfContact|gmd:citedResponsibleParty|gmd:distributorContact" priority="99">
		
		<xsl:param name="schema" />
		<xsl:param name="edit" />

		<xsl:for-each select=".">
			<xsl:apply-templates mode="iso19139"
				select="gmd:CI_ResponsibleParty/gmd:organisationName">
				<xsl:with-param name="schema" select="$schema" />
				<xsl:with-param name="edit" select="$edit" />
			</xsl:apply-templates>
			<xsl:apply-templates mode="iso19139"
				select="gmd:CI_ResponsibleParty/gmd:individualName|
						gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress">
				<xsl:with-param name="schema" select="$schema" />
				<xsl:with-param name="edit" select="$edit" />
			</xsl:apply-templates>
		</xsl:for-each>

	</xsl:template>



  <!-- Check if any elements are overriden here in iso19139.sdn-product mode
  if not fallback to iso19139 -->
  <xsl:template name="metadata-iso19139.sdn-product" match="metadata-iso19139.sdn-product">
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    <xsl:param name="embedded"/>

    <!-- process in profile mode first -->
    <xsl:variable name="profileElements">
      <xsl:apply-templates mode="iso19139.sdn-product" select=".">
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
        <xsl:apply-templates mode="iso19139" select=".">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
          <xsl:with-param name="embedded" select="$embedded"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Tab configuration -->
  <xsl:template name="iso19139.sdn-productCompleteTab">
    <xsl:param name="tabLink"/>
    <xsl:param name="schema"/>

    <!-- Add custom tab if a custom view is needed -->
    <xsl:call-template name="mainTab">
       <xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/sdnProductTab"/>
      <xsl:with-param name="default">sdnProduct</xsl:with-param>
      <xsl:with-param name="menu">
        <item label="sdnProductTab">sdnProduct</item>
      </xsl:with-param>
    </xsl:call-template>


    <!-- Preserve iso19139 complete tabs -->
    <xsl:call-template name="iso19139CompleteTab">
      <xsl:with-param name="tabLink" select="$tabLink"/>
      <xsl:with-param name="schema" select="$schema"/>
    </xsl:call-template>

  </xsl:template>

  <!-- Based template for dispatching each tabs -->
  <xsl:template mode="iso19139.sdn-product" match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']"
    priority="3">
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

        <xsl:when test="$currTab='sdnProduct'">
        <xsl:call-template name="sdnProduct">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
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

	<!-- SeaDatanet custom tab -->
	<xsl:template name="sdnProduct">
		<xsl:param name="schema" />
		<xsl:param name="edit" />
		<xsl:param name="dataset" />
		<xsl:param name="core" />

		<!-- 1.1 Product identification -->
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/productIdentification" />
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/metadataInfoTitle)" />

			<xsl:with-param name="content">

				<!-- title -->
				<xsl:apply-templates mode="elementEP"
					select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title">
					<xsl:with-param name="schema" select="$schema" />
					<xsl:with-param name="edit" select="$edit" />
				</xsl:apply-templates>

				<!-- Internal permanent shortname -->
				<xsl:apply-templates mode="elementEP"
					select="gmd:fileIdentifier">
					<xsl:with-param name="schema" select="$schema" />
					<xsl:with-param name="edit" select="false" />
				</xsl:apply-templates>

				<!-- External shortname -->
				<xsl:apply-templates mode="elementEP"
					select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:alternateTitle">
					<xsl:with-param name="schema" select="$schema" />
					<xsl:with-param name="edit" select="$edit" />
				</xsl:apply-templates>

				<!-- DOI -->
				<xsl:apply-templates mode="simpleElement" select="gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorTransferOptions/
					gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:protocol/gco:CharacterString='WWW:LINK-1.0-http--metadata-URL']/gmd:linkage/gmd:URL">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="title" select="'DOI'"/>
				</xsl:apply-templates>




				<!-- Overview -->
				<xsl:apply-templates mode="elementEP"
					select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract">
					<xsl:with-param name="schema" select="$schema" />
					<xsl:with-param name="edit" select="$edit" />
				</xsl:apply-templates>
				
				<!-- creation date -->
				<xsl:apply-templates mode="elementEP" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/
                                                                 gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date">
                    <xsl:with-param name="schema" select="$schema"/>
                    <xsl:with-param name="edit"   select="$edit"/>
                </xsl:apply-templates>
                
				<!-- update date -->
				<xsl:apply-templates mode="elementEP"
					select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/
 					gmd:CI_Citation/gmd:editionDate">
					<xsl:with-param name="schema" select="$schema" />
					<xsl:with-param name="edit" select="$edit" />
				</xsl:apply-templates>
				
				<!-- version -->
				<xsl:apply-templates mode="elementEP"
					select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:edition">
					<xsl:with-param name="schema" select="$schema" />
					<xsl:with-param name="edit" select="$edit" />
				</xsl:apply-templates>
				
			</xsl:with-param>
		</xsl:call-template>

		<!-- 1.2 Descriptive keywords -->

		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/descriptiveKeywords" />
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/descriptiveKeywords)" />

			<xsl:with-param name="content">		

			<!-- Feature type -->
			
				<xsl:for-each select="gmd:contentInfo/gmd:MD_FeatureCatalogueDescription[gmd:featureTypes/gco:LocalName]">
					<xsl:apply-templates mode="simpleElement" select=".">
						<xsl:with-param name="schema"  select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/featureType"/>
						<xsl:with-param name="text">
							<xsl:call-template name="snippet-editor">
								<xsl:with-param name="elementRef" select="../geonet:element/@ref"/>
								<xsl:with-param name="widgetMode" select="'multiplelist'"/>
								<xsl:with-param name="thesaurusId" select="'local.feature-type.seadatanet.feature-type'"/>
								<xsl:with-param name="listOfKeywords" select="replace(replace(string-join(gmd:featureTypes/gco:LocalName/@codeSpace, '!,!'), '''', '\\'''), '!', '''')"/>
								<xsl:with-param name="listOfTransformations" select="'''to-iso19139.myocean-feature-type'''"/>
								<xsl:with-param name="transformation" select="'to-iso19139.myocean-feature-type'"/>
								<xsl:with-param name="identificationMode" select="'uri'"/>
							</xsl:call-template>
						</xsl:with-param>
					</xsl:apply-templates>
				</xsl:for-each>

				<!-- sea areas -->
				<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords
					[gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='reference-geographical-area']/gmd:MD_Keywords">
					<xsl:variable name="refGeoAreaKeywords" select="."/>
					
					<xsl:apply-templates mode="simpleElement" select=".">
						<xsl:with-param name="schema"  select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/seaAreas"/>
						<xsl:with-param name="text">
							<xsl:call-template name="snippet-editor">
								<xsl:with-param name="elementRef" select="$refGeoAreaKeywords/../geonet:element/@ref"/>
								<xsl:with-param name="thesaurusId" select="'local.reference-geographical-area.seadatanet.reference-geographical-area'"/>
								<xsl:with-param name="listOfKeywords" select="replace(replace(string-join($refGeoAreaKeywords/gmd:keyword/*[1], '!,!'), '''', '\\'''), '!', '''')"/>
								<xsl:with-param name="listOfTransformations" select="'''to-iso19139-keyword'''"/>
								<xsl:with-param name="transformation" select="'to-iso19139-keyword'"/>
							</xsl:call-template>
						</xsl:with-param>
					</xsl:apply-templates>
					
				</xsl:for-each>

			<!-- ocean discovery parameters -->
			
				<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords
					[gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='parameter']/gmd:MD_Keywords">
					<xsl:variable name="oceanDP" select="."/>

					<xsl:if test="position()=1">

					<xsl:apply-templates mode="simpleElement" select=".">
						<xsl:with-param name="schema"  select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/oceanDiscoveryParameters"/>
						<xsl:with-param name="text">
							<xsl:call-template name="snippet-editor">
								<xsl:with-param name="elementRef" select="$oceanDP/../geonet:element/@ref"/>
								<xsl:with-param name="thesaurusId" select="'local.parameter.seadatanet-ocean-discovery-parameter'"/>
								<xsl:with-param name="listOfKeywords" select="replace(replace(string-join($oceanDP/gmd:keyword/*[1], '!,!'), '''', '\\'''), '!', '''')"/>
								<xsl:with-param name="listOfTransformations" select="'''to-iso19139-keyword'''"/>
								<xsl:with-param name="transformation" select="'to-iso19139-keyword'"/>
								<xsl:with-param name="identificationMode" select="''"/>
							</xsl:call-template>
						</xsl:with-param>
					</xsl:apply-templates>

					</xsl:if>
					<xsl:if test="position()=last()">
					<xsl:apply-templates mode="simpleElement" select=".">
						<xsl:with-param name="schema"  select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
						<xsl:with-param name="title" select="'Ocean chemistry variable'"/>
						<xsl:with-param name="text">
							<xsl:call-template name="snippet-editor">
								<xsl:with-param name="elementRef" select="$oceanDP/../geonet:element/@ref"/>
								<xsl:with-param name="thesaurusId" select="'local.parameter.seadatanet-ocean-chemistry-variable'"/>
								<xsl:with-param name="listOfKeywords" select="replace(replace(string-join($oceanDP/gmd:keyword/*[1], '!,!'), '''', '\\'''), '!', '''')"/>
								<xsl:with-param name="listOfTransformations" select="'''to-iso19139-keyword'''"/>
								<xsl:with-param name="transformation" select="'to-iso19139-keyword'"/>
								<xsl:with-param name="identificationMode" select="''"/>
							</xsl:call-template>
						</xsl:with-param>
					</xsl:apply-templates>


					</xsl:if>


				</xsl:for-each>




			<!-- Usage license -->
			
			<xsl:apply-templates mode="iso19139.sdn-product" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation">
					<xsl:with-param name="schema" select="$schema" />
					<xsl:with-param name="edit" select="$edit" />
		    </xsl:apply-templates>


			</xsl:with-param>
			
		</xsl:call-template>

		<!-- 1.3 Spatio-temporal extent -->
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/spatioTemporalExtent" />
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/spatioTemporalExtent)" />

			<xsl:with-param name="content">		

					<!-- geographical extent -->
					<xsl:call-template name="complexElementGuiWrapper">
						<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/geographicalExtent" />
						<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/geographicalExtent)" />
						<xsl:with-param name="content">
							<!-- geographic bounding box -->
							<xsl:apply-templates mode="iso19139"
								select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/
												gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
								<xsl:with-param name="schema" select="$schema" />
								<xsl:with-param name="edit" select="$edit" />
							</xsl:apply-templates>
							<!-- spatial resolution -->
							<xsl:apply-templates mode="iso19139.sdn-product"
								select="gmd:identificationInfo/gmd:MD_DataIdentification/
																gmd:spatialResolution/gmd:MD_Resolution/gmd:distance">
								<xsl:with-param name="schema" select="$schema" />
								<xsl:with-param name="edit" select="$edit" />
							</xsl:apply-templates>
		
							<!-- Coordinate reference system -->
							<xsl:apply-templates mode="iso19139.sdn-product"
								select="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/
																gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code">
								<xsl:with-param name="schema" select="$schema" />
								<xsl:with-param name="edit" select="$edit" />
							</xsl:apply-templates>
						</xsl:with-param>
					</xsl:call-template>
									
					<!-- vertical extent -->
					
					<xsl:call-template name="complexElementGuiWrapper">
						<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/verticalExtent" />
						<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/verticalExtent)" />
						<xsl:with-param name="content">
							<xsl:apply-templates mode="iso19139" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/
								gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:minimumValue|gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/
								gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:maximumValue">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:apply-templates>

							<!-- Number of vertical levels (card. 0..1) -->
							<xsl:apply-templates mode="iso19139" select="gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/
                                gmd:MD_Dimension[./gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='vertical']/gmd:dimensionSize">
								<xsl:with-param name="schema"   select="$schema"/>
								<xsl:with-param name="edit"     select="$edit"/>
								<xsl:with-param name="title"    select="/root/gui/schemas/*[name()=$schema]/strings/nbVerticalLevels" />
								<xsl:with-param name="text"    select="gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/
    	 						gmd:MD_Dimension[./gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='vertical']/gmd:dimensionSize/gco:Integer" />
							</xsl:apply-templates>
							
						</xsl:with-param>
						
					</xsl:call-template>
					<!-- end of vertical extent -->
					
					<!-- temporal extent -->

					<xsl:call-template name="complexElementGuiWrapper">
						<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/temporalExtent" />
						<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/temporalExtent)" />
						<xsl:with-param name="content">			
							<xsl:apply-templates mode="iso19139" select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/
									gmd:EX_Extent/gmd:temporalElement/
									gmd:EX_TemporalExtent/gmd:extent//*[gml:beginPosition]">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
								<xsl:with-param name="title"  select="/root/gui/schemas/*[name()=$schema]/strings/startDate"/>
							</xsl:apply-templates>
							<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/
								gmd:EX_Extent/gmd:temporalElement/
								gmd:EX_TemporalExtent/gmd:extent//*[gml:endPosition]">
								<xsl:for-each select="*">
									<xsl:apply-templates mode="simpleElement" select=".">
										<xsl:with-param name="schema"  select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
										<xsl:with-param name="title"  select="/root/gui/schemas/*[name()=$schema]/strings/endDate"/>
										<xsl:with-param name="editAttributes" select="false()"/>
										<xsl:with-param name="text">
											<xsl:variable name="ref" select="geonet:element/@ref"/>
											<xsl:variable name="format">
												<xsl:choose>
													<xsl:when test="string-length(text()) = 10"><xsl:text>%Y-%m-%d</xsl:text></xsl:when>
													<xsl:when test="string-length(text()) > 10"><xsl:text>%Y-%m-%dT%H:%M:00</xsl:text></xsl:when>
													<xsl:otherwise><xsl:text>%Y-%m-%d</xsl:text></xsl:otherwise>
												</xsl:choose>
											</xsl:variable>
											<xsl:call-template name="calendar">
												<xsl:with-param name="ref" select="$ref"/>
												<xsl:with-param name="date" select="text()"/>
												<xsl:with-param name="format" select="$format"/>
												<xsl:with-param name="disabled" select="@indeterminatePosition = 'unknown'"/>
											</xsl:call-template>
											<xsl:variable name="indeterminatePositionId" select="concat('_', $ref ,'_indeterminatePosition')"/>
											<div style="margin-left: 168px;">
												<!-- When form field is disabled, they are not posted. Add an empty field 
													to enable when the calendar field is disabled. -->
												<input type="hidden" id="_{$ref}_disabled_field" name="_{$ref}" value="">
													<xsl:if test="@indeterminatePosition != 'unknown'">
														<xsl:attribute name="disabled">disabled</xsl:attribute>
													</xsl:if>
												</input>
												<input type="hidden" id="{$indeterminatePositionId}" name="{$indeterminatePositionId}" value="{@indeterminatePosition}"/>
												<input type="checkbox"
													onclick="indeterminatePositionCheck(this.checked, '_{$ref}', '{$indeterminatePositionId}');"
													id="{$indeterminatePositionId}_ck">
													<xsl:if test="$edit = false()">
														<xsl:attribute name="disabled">disabled</xsl:attribute>
													</xsl:if>
													<xsl:if test="@indeterminatePosition = 'unknown'">
														<xsl:attribute name="checked">checked</xsl:attribute>
													</xsl:if>
												</input>											
												<label for="{$indeterminatePositionId}_ck">
													<xsl:value-of select="/root/gui/schemas/*[name()=$schema]/strings/unknown" />
												</label>
											</div>
										</xsl:with-param>
									</xsl:apply-templates>
								</xsl:for-each>
							</xsl:for-each>
							
							<!-- Temporal resolution -->
							<xsl:for-each select="gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension
										[./gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue='time']/gmd:resolution">
									<xsl:apply-templates mode="iso19139.sdn-product" select=".">
										<xsl:with-param name="schema" select="$schema" />
										<xsl:with-param name="edit" select="$edit" />
									</xsl:apply-templates>
							</xsl:for-each>

						</xsl:with-param>
					</xsl:call-template>
					<!-- end of temporal extent -->
			</xsl:with-param>
			
		</xsl:call-template>
		
		<!-- 1.4 Organization responsible for management ... -->
		<xsl:call-template name="complexElementGuiWrapper">
			<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/pointOfContact" />
			<xsl:with-param name="id" select="generate-id(/root/gui/schemas/*[name()=$schema]/strings/pointOfContact)" />

			<xsl:with-param name="content">		
				<xsl:if test="$edit">
					<xsl:copy-of select="geonet:makeSubTemplateButton(gmd:identificationInfo/gmd:MD_DataIdentification/geonet:element/@ref, 
						'gmd:pointOfContact', 
						'gmd:CI_ResponsibleParty', 
						/root/gui/schemas/*[name()=$schema]/strings/orgAdd,
						/root/gui/schemas/*[name()=$schema]/strings/orgAdd, 
						/root/gui/schemalist/name[text()=$schema]/@namespaces)"/>
				</xsl:if>
				
				<!-- custodian -->
				<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/
					gmd:pointOfContact[gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue='custodian']">
					
					<xsl:variable name="id" select="concat('seadatanet-product-', generate-id(.))"/>
					
					<xsl:call-template name="complexElementGuiWrapper">
					<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/custodian"/>
					<xsl:with-param name="id" select="$id"/>
						<xsl:with-param name="content">
							<span class="buttons">
								<a class="small del" href="javascript:void(0);" 
									onclick="doRemoveElementActionSimple('metadata.elem.delete.new', {geonet:element/@ref}, {../geonet:element/@ref}, '{$id}');"><span>  Remove</span></a>
							</span>
							<!-- Org name -->							
							<xsl:apply-templates mode="iso19139.sdn-product" select=".">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:apply-templates>

						</xsl:with-param>
				</xsl:call-template>
			</xsl:for-each>

			<!-- originator -->
			<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/
					gmd:pointOfContact[gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue='originator']">
					
					<xsl:variable name="id" select="concat('seadatanet-product-', generate-id(.))"/>
					
					<xsl:call-template name="complexElementGuiWrapper">
					<xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/originator"/>
					<xsl:with-param name="id" select="$id"/>
						<xsl:with-param name="content">
							<span class="buttons">
								<a class="small del" href="javascript:void(0);" 
									onclick="doRemoveElementActionSimple('metadata.elem.delete.new', {geonet:element/@ref}, {../geonet:element/@ref}, '{$id}');"><span>  Remove</span></a>
							</span>
							<!-- Org name -->							
							<xsl:apply-templates mode="iso19139.sdn-product" select=".">
								<xsl:with-param name="schema" select="$schema"/>
								<xsl:with-param name="edit"   select="$edit"/>
							</xsl:apply-templates>
							<!-- Indiv. name -->
<!-- 							<xsl:apply-templates mode="iso19139" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress"> -->
<!-- 								<xsl:with-param name="schema" select="$schema"/> -->
<!-- 								<xsl:with-param name="edit"   select="$edit"/> -->
<!-- 							</xsl:apply-templates> -->
							<!-- Electronic mail address -->
<!-- 							<xsl:apply-templates mode="iso19139" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress"> -->
<!-- 								<xsl:with-param name="schema" select="$schema"/> -->
<!-- 								<xsl:with-param name="edit"   select="$edit"/> -->
<!-- 							</xsl:apply-templates> -->
						</xsl:with-param>
				</xsl:call-template>
			</xsl:for-each>	
		 </xsl:with-param>
	  </xsl:call-template>
    </xsl:template>

	
	<!-- Match all elements in schema mode and return nothing in 
	order to delegate to iso19139 mode. See metadata-iso19139.sdn-product
	dispatcher template. -->
	<xsl:template mode="iso19139.sdn-product" match="*|@*"/>
	
	<!-- Override useLimitation having anchor to not display the xlink:href information. -->
	<xsl:template mode="iso19139.sdn-product" match="gmd:useLimitation[gmx:Anchor]" priority="99">
		<xsl:param name="schema" />
		<xsl:param name="edit" />
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
        <xsl:variable name="ref" select="geonet:element/@ref"/>
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema" select="$schema" />
          <xsl:with-param name="edit" select="$edit" />
          <xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/usageLicense" />
          <xsl:with-param name="text">
            <xsl:call-template name="snippet-editor">
              <xsl:with-param name="elementRef" select="../geonet:element/@ref" />
              <xsl:with-param name="widgetMode" select="'combo'" />
              <xsl:with-param name="thesaurusId"
                select="'local.use-limitation.seadatanet.use-limitation'" />
              <xsl:with-param name="listOfKeywords"
                select="replace(replace(string-join(gmx:Anchor, '!,!'), '''', '\\'''), '!', '''')" />
              <xsl:with-param name="listOfTransformations"
                select="'''to-iso19139.sdn-use-limitation'''" />
              <xsl:with-param name="transformation"
                select="'to-iso19139.sdn-use-limitation'" />
              <xsl:with-param name="identificationMode" select="'uri'" />
            </xsl:call-template>
          </xsl:with-param>
        </xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<a href="{gmx:Anchor/@xlink:href}"><xsl:value-of select="gmx:Anchor"/></a>
			</xsl:otherwise> 
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
