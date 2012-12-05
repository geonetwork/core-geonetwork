<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="2.0" xmlns:gmd="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:gml="http://www.opengis.net/gml"
										xmlns:srv="http://www.isotc211.org/2005/srv"
										xmlns:geonet="http://www.fao.org/geonetwork"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:gmx="http://www.isotc211.org/2005/gmx"
                                        xmlns:skos="http://www.w3.org/2004/02/skos/core#">

	<xsl:include href="convert/functions.xsl"/>
	<xsl:include href="../../../xsl/utils-fn.xsl"/>
	
	<!-- This file defines what parts of the metadata are indexed by Lucene
	     Searches can be conducted on indexes defined here. 
	     The Field@name attribute defines the name of the search variable.
		 If a variable has to be maintained in the user session, it needs to be 
		 added to the GeoNetwork constants in the Java source code.
		 Please keep indexes consistent among metadata standards if they should
		 work accross different metadata resources -->
	<!-- ========================================================================================= -->
	
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="no" />


	<!-- ========================================================================================= -->

  <xsl:param name="thesauriDir"/>
  <xsl:param name="inspire">false</xsl:param>
  
  <xsl:variable name="inspire-thesaurus" select="if ($inspire!='false') then document(concat('file:///', $thesauriDir, '/external/thesauri/theme/inspire-theme.rdf')) else ''"/>
  <xsl:variable name="inspire-theme" select="if ($inspire!='false') then $inspire-thesaurus//skos:Concept else ''"/>
  
  <!-- If identification creation, publication and revision date
    should be indexed as a temporal extent information (eg. in INSPIRE 
    metadata implementing rules, those elements are defined as part
    of the description of the temporal extent). -->
	<xsl:variable name="useDateAsTemporalExtent" select="false()"/>

        <!-- ========================================================================================= -->

	<xsl:template match="/">
	    <xsl:variable name="isoLangId">
	  	    <xsl:call-template name="langId19139"/>
        </xsl:variable>

		<Document locale="{$isoLangId}">
			<Field name="_locale" string="{$isoLangId}" store="true" index="true"/>

			<Field name="_docLocale" string="{$isoLangId}" store="true" index="true"/>

			<xsl:variable name="_defaultTitle">
				<xsl:call-template name="defaultTitle">
					<xsl:with-param name="isoDocLangId" select="$isoLangId"/>
				</xsl:call-template>
			</xsl:variable>
			<!-- not tokenized title for sorting, needed for multilingual sorting -->
			<Field name="_defaultTitle" string="{string($_defaultTitle)}" store="true" index="true"/>

			<xsl:apply-templates select="*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']" mode="metadata"/>
		</Document>
	</xsl:template>
	
	<!-- ========================================================================================= -->

	<xsl:template match="*" mode="metadata">

		<!-- === Data or Service Identification === -->		

		<!-- the double // here seems needed to index MD_DataIdentification when
           it is nested in a SV_ServiceIdentification class -->

		<xsl:for-each select="gmd:identificationInfo//gmd:MD_DataIdentification|gmd:identificationInfo/srv:SV_ServiceIdentification">

			<xsl:for-each select="gmd:citation/gmd:CI_Citation">
				<xsl:for-each select="gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString">
					<Field name="identifier" string="{string(.)}" store="true" index="true"/>
				</xsl:for-each>

                <xsl:for-each select="gmd:identifier/gmd:RS_Identifier/gmd:code/gco:CharacterString">
                	<Field name="identifier" string="{string(.)}" store="false" index="true"/>
				</xsl:for-each>

	
				<xsl:for-each select="gmd:title/gco:CharacterString">
					<Field name="title" string="{string(.)}" store="true" index="true"/>
                    <!-- not tokenized title for sorting -->
                    <Field name="_title" string="{string(.)}" store="false" index="true"/>
				</xsl:for-each>
	
				<xsl:for-each select="gmd:alternateTitle/gco:CharacterString">
					<Field name="altTitle" string="{string(.)}" store="true" index="true"/>
				</xsl:for-each>

				<xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date">
					<Field name="revisionDate" string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}" store="true" index="true"/>
					<Field name="createDateMonth" string="{substring(gco:Date[.!='']|gco:DateTime[.!=''], 0, 8)}" store="true" index="true"/>
					<Field name="createDateYear" string="{substring(gco:Date[.!='']|gco:DateTime[.!=''], 0, 5)}" store="true" index="true"/>
					<xsl:if test="$useDateAsTemporalExtent">
						<Field name="tempExtentBegin" string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}" store="true" index="true"/>
					</xsl:if>
				</xsl:for-each>

				<xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date">
					<Field name="createDate" string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}" store="true" index="true"/>
					<Field name="createDateMonth" string="{substring(gco:Date[.!='']|gco:DateTime[.!=''], 0, 8)}" store="true" index="true"/>
					<Field name="createDateYear" string="{substring(gco:Date[.!='']|gco:DateTime[.!=''], 0, 5)}" store="true" index="true"/>
					<xsl:if test="$useDateAsTemporalExtent">
						<Field name="tempExtentBegin" string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}" store="true" index="true"/>
					</xsl:if>
				</xsl:for-each>

				<xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='publication']/gmd:date">
					<Field name="publicationDate" string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}" store="true" index="true"/>
					<xsl:if test="$useDateAsTemporalExtent">
						<Field name="tempExtentBegin" string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}" store="true" index="true"/>
					</xsl:if>
				</xsl:for-each>

				<!-- fields used to search for metadata in paper or digital format -->

				<xsl:for-each select="gmd:presentationForm">
					<Field name="presentationForm" string="{gmd:CI_PresentationFormCode/@codeListValue}" store="true" index="true"/>
					
					<xsl:if test="contains(gmd:CI_PresentationFormCode/@codeListValue, 'Digital')">
						<Field name="digital" string="true" store="false" index="true"/>
					</xsl:if>
				
					<xsl:if test="contains(gmd:CI_PresentationFormCode/@codeListValue, 'Hardcopy')">
						<Field name="paper" string="true" store="false" index="true"/>
					</xsl:if>
				</xsl:for-each>
			</xsl:for-each>

            <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

            <xsl:for-each select="gmd:pointOfContact[1]/*/gmd:role/*/@codeListValue">
            	<Field name="responsiblePartyRole" string="{string(.)}" store="true" index="true"/>
            </xsl:for-each>
            
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
	
			<xsl:for-each select="gmd:abstract/gco:CharacterString">
				<Field name="abstract" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
			
			<xsl:for-each select="gmd:credit/gco:CharacterString">
				<Field name="credit" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		

			<xsl:for-each select="*/gmd:EX_Extent">
				<xsl:apply-templates select="gmd:geographicElement/gmd:EX_GeographicBoundingBox" mode="latLon"/>

				<xsl:for-each select="gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString">
					<Field name="geoDescCode" string="{string(.)}" store="true" index="true"/>
				</xsl:for-each>

				<xsl:for-each select="gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent">
					<xsl:for-each select="gml:TimePeriod">
						<xsl:variable name="times">
							<xsl:call-template name="newGmlTime">
								<xsl:with-param name="begin" select="gml:beginPosition|gml:begin/gml:TimeInstant/gml:timePosition"/>
								<xsl:with-param name="end" select="gml:endPosition|gml:end/gml:TimeInstant/gml:timePosition"/>
							</xsl:call-template>
						</xsl:variable>

						<Field name="tempExtentBegin" string="{lower-case(substring-before($times,'|'))}" store="true" index="true"/>
					    <Field name="tempExtentEnd" string="{lower-case(substring-after($times,'|'))}" store="true" index="true"/>
					</xsl:for-each>

				</xsl:for-each>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		

			<xsl:for-each select="//gmd:MD_Keywords">
			  
				<xsl:for-each select="gmd:keyword/gco:CharacterString|gmd:keyword/gmx:Anchor|gmd:keyword/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                    <xsl:variable name="keywordLower" select="lower-case(.)"/>
                    <Field name="keyword" string="{string(.)}" store="true" index="true"/>
					
                    <xsl:if test="$inspire='true'">
                        <xsl:if test="string-length(.) &gt; 0">
                         
                          <xsl:variable name="inspireannex">
                            <xsl:call-template name="determineInspireAnnex">
                              <xsl:with-param name="keyword" select="string(.)"/>
                              <xsl:with-param name="inspireThemes" select="$inspire-theme"/>
                            </xsl:call-template>
                          </xsl:variable>
                          
                          <!-- Add the inspire field if it's one of the 34 themes -->
                          <xsl:if test="normalize-space($inspireannex)!=''">
                            <!-- Maybe we should add the english version to the index to not take the language into account 
                            or create one field in the metadata language and one in english ? -->
                            <Field name="inspiretheme" string="{string(.)}" store="false" index="true"/>
                          	<Field name="inspireannex" string="{$inspireannex}" store="false" index="true"/>
                            <!-- FIXME : inspirecat field will be set multiple time if one record has many themes -->
                          	<Field name="inspirecat" string="true" store="false" index="true"/>
                          </xsl:if>
                        </xsl:if>
                    </xsl:if>
                </xsl:for-each>

				<xsl:for-each select="gmd:type/gmd:MD_KeywordTypeCode/@codeListValue">
					<Field name="keywordType" string="{string(.)}" store="true" index="true"/>
				</xsl:for-each>
			</xsl:for-each>
	
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
	
			<xsl:for-each select="gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString|gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor">
				<Field name="orgName" string="{string(.)}" store="true" index="true"/>
				
				<xsl:variable name="role" select="../../gmd:role/*/@codeListValue"/>
				<xsl:variable name="logo" select="../..//gmx:FileName/@src"/>
			
				<Field name="responsibleParty" string="{concat($role, '|resource|', ., '|', $logo)}" store="true" index="false"/>
				
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
	
			<xsl:choose>
				<xsl:when test="gmd:resourceConstraints/gmd:MD_SecurityConstraints">
					<Field name="secConstr" string="true" store="true" index="true"/>
				</xsl:when>
				<xsl:otherwise>
					<Field name="secConstr" string="false" store="true" index="true"/>
				</xsl:otherwise>
			</xsl:choose>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
			<xsl:for-each select="gmd:topicCategory/gmd:MD_TopicCategoryCode">
				<Field name="topicCat" string="{string(.)}" store="true" index="true"/>
				<Field name="keyword" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
	
			<xsl:for-each select="gmd:language/gco:CharacterString|gmd:language/gmd:LanguageCode/@codeListValue">
				<Field name="datasetLang" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		

			<xsl:for-each select="gmd:spatialResolution/gmd:MD_Resolution">
				<xsl:for-each select="gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer">
					<Field name="denominator" string="{string(.)}" store="true" index="true"/>
				</xsl:for-each>

				<xsl:for-each select="gmd:distance/gco:Distance">
					<Field name="distanceVal" string="{string(.)}" store="true" index="true"/>
				</xsl:for-each>

				<xsl:for-each select="gmd:distance/gco:Distance/@uom">
					<Field name="distanceUom" string="{string(.)}" store="true" index="true"/>
				</xsl:for-each>
			</xsl:for-each>
			
		  <xsl:for-each select="gmd:spatialRepresentationType">
		    <Field name="spatialRepresentationType" string="{gmd:MD_SpatialRepresentationTypeCode/@codeListValue}" store="true" index="true"/>
		  </xsl:for-each>
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			
			<xsl:for-each select="gmd:resourceConstraints">
				<xsl:for-each select="//gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue">
					<Field name="accessConstr" string="{string(.)}" store="true" index="true"/>
				</xsl:for-each>
				<xsl:for-each select="//gmd:otherConstraints/gco:CharacterString">
					<Field name="otherConstr" string="{string(.)}" store="true" index="true"/>
				</xsl:for-each>
				<xsl:for-each select="//gmd:classification/gmd:MD_ClassificationCode/@codeListValue">
					<Field name="classif" string="{string(.)}" store="true" index="true"/>
				</xsl:for-each>
				<xsl:for-each select="//gmd:useLimitation/gco:CharacterString">
					<Field name="conditionApplyingToAccessAndUse" string="{string(.)}" store="true" index="true"/>
				</xsl:for-each>
			</xsl:for-each>
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<!--  Fields use to search on Service -->
			
			<xsl:for-each select="srv:serviceType/gco:LocalName">
				<Field  name="serviceType" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
			
			<xsl:for-each select="srv:serviceTypeVersion/gco:CharacterString">
				<Field  name="serviceTypeVersion" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
			
			<xsl:for-each select="//srv:SV_OperationMetadata/srv:operationName/gco:CharacterString">
				<Field  name="operation" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
			
			<xsl:for-each select="srv:operatesOn/@uuidref">
                <Field  name="operatesOn" string="{string(.)}" store="true" index="true"/>
            </xsl:for-each>
			
			<xsl:for-each select="srv:coupledResource">
				<xsl:for-each select="srv:SV_CoupledResource/srv:identifier/gco:CharacterString">
					<Field  name="operatesOnIdentifier" string="{string(.)}" store="true" index="true"/>
				</xsl:for-each>
				
				<xsl:for-each select="srv:SV_CoupledResource/srv:operationName/gco:CharacterString">
					<Field  name="operatesOnName" string="{string(.)}" store="true" index="true"/>
				</xsl:for-each>
			</xsl:for-each>
			
			<xsl:for-each select="//srv:SV_CouplingType/@codeListValue">
				<Field  name="couplingType" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
			
			
			<xsl:for-each select="gmd:graphicOverview/gmd:MD_BrowseGraphic">
				<xsl:variable name="fileName"  select="gmd:fileName/gco:CharacterString"/>
				<xsl:if test="$fileName != ''">
					<xsl:variable name="fileDescr" select="gmd:fileDescription/gco:CharacterString"/>
					<xsl:choose>
						<xsl:when test="contains($fileName ,'://')">
							<Field  name="image" string="{concat('unknown|', $fileName)}" store="true" index="false"/>
						</xsl:when>
						<xsl:when test="string($fileDescr)='thumbnail'">
							<!-- FIXME : relative path -->
							<Field  name="image" string="{concat($fileDescr, '|', '../../srv/eng/resources.get?uuid=', //gmd:fileIdentifier/gco:CharacterString, '&amp;fname=', $fileName, '&amp;access=public')}" store="true" index="false"/>
						</xsl:when>
					</xsl:choose>
				</xsl:if>
			</xsl:for-each>
			
			
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === Distribution === -->		

		<xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution">
			<xsl:for-each select="gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString">
				<Field name="format" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>

			<!-- index online protocol -->
			
			<xsl:for-each select="gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:linkage/gmd:URL!='']">
				<xsl:variable name="download_check"><xsl:text>&amp;fname=&amp;access</xsl:text></xsl:variable>
				<xsl:variable name="linkage" select="gmd:linkage/gmd:URL" /> 
				<xsl:variable name="title" select="normalize-space(gmd:name/gco:CharacterString|gmd:name/gmx:MimeFileType)"/>
				<xsl:variable name="desc" select="normalize-space(gmd:description/gco:CharacterString)"/>
				<xsl:variable name="protocol" select="normalize-space(gmd:protocol/gco:CharacterString)"/>
				<xsl:variable name="mimetype" select="geonet:protocolMimeType($linkage, $protocol, gmd:name/gmx:MimeFileType/@type)"/>
				
				<!-- ignore empty downloads -->
				<xsl:if test="string($linkage)!='' and not(contains($linkage,$download_check))">  
					<Field name="protocol" string="{string($protocol)}" store="true" index="true"/>
				</xsl:if>  

				<xsl:if test="normalize-space($mimetype)!=''">
					<Field name="mimetype" string="{$mimetype}" store="true" index="true"/>
				</xsl:if>
			  
				<xsl:if test="contains($protocol, 'WWW:DOWNLOAD')">
			    	<Field name="download" string="true" store="false" index="true"/>
			  	</xsl:if>
			  
				<xsl:if test="contains($protocol, 'OGC:WMS')">
			   	 	<Field name="dynamic" string="true" store="false" index="true"/>
			  	</xsl:if>
				<Field name="link" string="{concat($title, '|', $desc, '|', $linkage, '|', $protocol, '|', $mimetype)}" store="true" index="false"/>
				
				<!-- Add KML link if WMS -->
				<xsl:if test="starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-map') and string($linkage)!='' and string($title)!=''">
					<!-- FIXME : relative path -->
					<Field name="link" string="{concat($title, '|', $desc, '|', 
						'../../srv/en/google.kml?uuid=', /gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString, '&amp;layers=', $title, 
						'|application/vnd.google-earth.kml+xml|application/vnd.google-earth.kml+xml')}" store="true" index="false"/>					
				</xsl:if>					
				
			</xsl:for-each>  
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
		<!-- === Content info === -->
		<xsl:for-each select="gmd:contentInfo/*/gmd:featureCatalogueCitation[@uuidref]">
			<Field  name="hasfeaturecat" string="{string(@uuidref)}" store="false" index="true"/>
		</xsl:for-each>
		
		<!-- === Data Quality  === -->
		<xsl:for-each select="gmd:dataQualityInfo/*/gmd:lineage//gmd:source[@uuidref]">
			<Field  name="hassource" string="{string(@uuidref)}" store="false" index="true"/>
		</xsl:for-each>
		
		<xsl:for-each select="gmd:dataQualityInfo/*/gmd:report/*/gmd:result">
			<xsl:if test="$inspire='true'">
				<!-- 
				INSPIRE related dataset could contains a conformity section with:
				* COMMISSION REGULATION (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services
				* INSPIRE Data Specification on <Theme Name> – <version>
				* INSPIRE Specification on <Theme Name> – <version> for CRS and GRID
				
				Index those types of citation title to found dataset related to INSPIRE (which may be better than keyword
				which are often used for other types of datasets).
				
				"1089/2010" is maybe too fuzzy but could work for translated citation like "Règlement n°1089/2010, Annexe II-6" TODO improved
				-->
				<xsl:if test="(
					contains(gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString, '1089/2010') or
					contains(gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString, 'INSPIRE Data Specification') or
					contains(gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString, 'INSPIRE Specification'))">
					<Field name="inspirerelated" string="on" store="false" index="true"/>
				</xsl:if>
			</xsl:if>
			
			<xsl:for-each select="//gmd:pass/gco:Boolean">
				<Field name="degree" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
			
			<xsl:for-each select="//gmd:specification/*/gmd:title/gco:CharacterString">
				<Field name="specificationTitle" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
			
			<xsl:for-each select="//gmd:specification/*/gmd:date/*/gmd:date">
				<Field name="specificationDate" string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}" store="true" index="true"/>
			</xsl:for-each>
			
			<xsl:for-each select="//gmd:specification/*/gmd:date/*/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue">
				<Field name="specificationDateType" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
		</xsl:for-each>
		<xsl:for-each select="gmd:dataQualityInfo/*/gmd:lineage/*/gmd:statement/gco:CharacterString">
			<Field name="lineage" string="{string(.)}" store="true" index="true"/>
		</xsl:for-each>
		
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === General stuff === -->		
		<!-- Metadata type  -->
		<xsl:choose>
			<xsl:when test="gmd:hierarchyLevel">
				<xsl:for-each select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue">
					<Field name="type" string="{string(.)}" store="true" index="true"/>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<Field name="type" string="dataset" store="true" index="true"/>
			</xsl:otherwise>
		</xsl:choose>

	    <xsl:choose>
	     <xsl:when test="gmd:identificationInfo/srv:SV_ServiceIdentification">
	     	<Field name="type" string="service" store="false" index="true"/>
	     </xsl:when>
	     <!-- <xsl:otherwise>
	      ... gmd:*_DataIdentification / hierachicalLevel is used and return dataset, serie, ... 
	      </xsl:otherwise>-->
	    </xsl:choose>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		

		<xsl:for-each select="gmd:hierarchyLevelName/gco:CharacterString">
			<Field name="levelName" string="{string(.)}" store="false" index="true"/>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		

		<xsl:for-each select="gmd:language/gco:CharacterString
			|gmd:language/gmd:LanguageCode/@codeListValue
			|gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode/@codeListValue">
			<Field name="language" string="{string(.)}" store="true" index="true"/>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		

		<xsl:for-each select="gmd:fileIdentifier/gco:CharacterString">
			<Field name="fileId" string="{string(.)}" store="false" index="true"/>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		

		<xsl:for-each select="gmd:parentIdentifier/gco:CharacterString">
			<Field name="parentUuid" string="{string(.)}" store="true" index="true"/>
		</xsl:for-each>
		
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
		
		<xsl:for-each select="gmd:dateStamp/gco:DateTime">
			<Field name="changeDate" string="{string(.)}" store="true" index="true"/>
		</xsl:for-each>
		
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
		
		<xsl:for-each select="gmd:contact/*/gmd:organisationName/gco:CharacterString|gmd:contact/*/gmd:organisationName/gmx:Anchor">
			<Field name="metadataPOC" string="{string(.)}" store="true" index="true"/>
			
			<xsl:variable name="role" select="../../gmd:role/*/@codeListValue"/>
			<xsl:variable name="logo" select="../..//gmx:FileName/@src"/>
			
			<Field name="responsibleParty" string="{concat($role, '|metadata|', ., '|', $logo)}" store="true" index="false"/>			
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === Reference system info === -->		

		<xsl:for-each select="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem">
			<xsl:for-each select="gmd:referenceSystemIdentifier/gmd:RS_Identifier">
				<xsl:variable name="crs" select="concat(string(gmd:codeSpace/gco:CharacterString),'::',string(gmd:code/gco:CharacterString))"/>

				<xsl:if test="$crs != '::'">
					<Field name="crs" string="{$crs}" store="false" index="true"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:for-each>
		
		<xsl:for-each select="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem">
			<xsl:for-each select="gmd:referenceSystemIdentifier/gmd:RS_Identifier">
				<Field name="authority" string="{string(gmd:codeSpace/gco:CharacterString)}" store="false" index="true"/>
				<Field name="crsCode" string="{string(gmd:code/gco:CharacterString)}" store="false" index="true"/>
				<Field name="crsVersion" string="{string(gmd:version/gco:CharacterString)}" store="false" index="true"/>
			</xsl:for-each>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === Free text search === -->		

		<Field name="any" store="false" index="true">
			<xsl:attribute name="string">
				<xsl:value-of select="normalize-space(string(.))"/>
				<xsl:text> </xsl:text>
				<xsl:for-each select="//@codeListValue">
					<xsl:value-of select="concat(., ' ')"/>
				</xsl:for-each>
			</xsl:attribute>
		</Field>
				
		<!--<xsl:apply-templates select="." mode="codeList"/>-->
		
	</xsl:template>

	<!-- ========================================================================================= -->
	<!-- codelist element, indexed, not stored nor tokenized -->
	
	<xsl:template match="*[./*/@codeListValue]" mode="codeList">
		<xsl:param name="name" select="name(.)"/>
		
		<Field name="{$name}" string="{*/@codeListValue}" store="false" index="true"/>		
	</xsl:template>
	
	<!-- ========================================================================================= -->
	
	<xsl:template match="*" mode="codeList">
		<xsl:apply-templates select="*" mode="codeList"/>
	</xsl:template>
	
	
	<!-- ========================================================================================= -->

	<!-- inspireThemes is a nodeset consisting of skos:Concept elements -->
	<!-- each containing a skos:definition and skos:prefLabel for each language -->
	<!-- This template finds the provided keyword in the skos:prefLabel elements and returns the English one from the same skos:Concept -->
	<xsl:template name="translateInspireThemeToEnglish">
		<xsl:param name="keyword"/>
		<xsl:param name="inspireThemes"/>
		<xsl:for-each select="$inspireThemes/skos:prefLabel">
			<!-- if this skos:Concept contains a kos:prefLabel with text value equal to keyword -->
			<xsl:if test="text() = $keyword">
				<xsl:value-of select="../skos:prefLabel[@xml:lang='en']/text()"/>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>	

	<xsl:template name="determineInspireAnnex">
		<xsl:param name="keyword"/>
		<xsl:param name="inspireThemes"/>
		<xsl:variable name="englishKeywordMixedCase">
			<xsl:call-template name="translateInspireThemeToEnglish">
				<xsl:with-param name="keyword" select="$keyword"/>
				<xsl:with-param name="inspireThemes" select="$inspireThemes"/>
			</xsl:call-template>
		</xsl:variable>
	  <xsl:variable name="englishKeyword" select="lower-case($englishKeywordMixedCase)"/>			
	  <!-- Another option could be to add the annex info in the SKOS thesaurus using something
		like a related concept. -->
		<xsl:choose>
			<!-- annex i -->
			<xsl:when test="$englishKeyword='coordinate reference systems' or $englishKeyword='geographical grid systems' 
			            or $englishKeyword='geographical names' or $englishKeyword='administrative units' 
			            or $englishKeyword='addresses' or $englishKeyword='cadastral parcels' 
			            or $englishKeyword='transport networks' or $englishKeyword='hydrography' 
			            or $englishKeyword='protected sites'">
			    <xsl:text>i</xsl:text>
			</xsl:when>
			<!-- annex ii -->
			<xsl:when test="$englishKeyword='elevation' or $englishKeyword='land cover' 
			            or $englishKeyword='orthoimagery' or $englishKeyword='geology'">
				 <xsl:text>ii</xsl:text>
			</xsl:when>
			<!-- annex iii -->
			<xsl:when test="$englishKeyword='statistical units' or $englishKeyword='buildings' 
			            or $englishKeyword='soil' or $englishKeyword='land use' 
			            or $englishKeyword='human health and safety' or $englishKeyword='utility and government services' 
			            or $englishKeyword='environmental monitoring facilities' or $englishKeyword='production and industrial facilities' 
			            or $englishKeyword='agricultural and aquaculture facilities' or $englishKeyword='population distribution - demography' 
			            or $englishKeyword='area management/restriction/regulation zones and reporting units' 
			            or $englishKeyword='natural risk zones' or $englishKeyword='atmospheric conditions' 
			            or $englishKeyword='meteorological geographical features' or $englishKeyword='oceanographic geographical features' 
			            or $englishKeyword='sea regions' or $englishKeyword='bio-geographical regions' 
			            or $englishKeyword='habitats and biotopes' or $englishKeyword='species distribution' 
			            or $englishKeyword='energy resources' or $englishKeyword='mineral resources'">
				 <xsl:text>iii</xsl:text>
			</xsl:when>
			<!-- inspire annex cannot be established: leave empty -->
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
