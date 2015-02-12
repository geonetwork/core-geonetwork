<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="2.0" xmlns:gmd="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:gml="http://www.opengis.net/gml"
										xmlns:srv="http://www.isotc211.org/2005/srv"
										xmlns:geonet="http://www.fao.org/geonetwork"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:gmx="http://www.isotc211.org/2005/gmx"
                    xmlns:xlink="http://www.w3.org/1999/xlink"
                    xmlns:util="java:org.fao.geonet.util.XslUtil"
                    xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                    exclude-result-prefixes="#all">

    <xsl:include href="../convert/functions.xsl" />
    <xsl:include href="../../../xsl/utils-fn.xsl" />
    <xsl:include href="index-subtemplate-fields.xsl" />

    <!-- This file defines what parts of the metadata are indexed by Lucene
         Searches can be conducted on indexes defined here.
         The Field@name attribute defines the name of the search variable.
         If a variable has to be maintained in the user session, it needs to be
         added to the GeoNetwork constants in the Java source code.
         Please keep indexes consistent among metadata standards if they should
         work across different metadata resources -->
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

  <!-- Define the way keyword and thesaurus are indexed. If false
  only keyword, thesaurusName and thesaurusType field are created.
  If true, advanced field are created to make more details query
  on keyword type and search by thesaurus. Index size is bigger
  but more detailed facet can be configured based on each thesaurus.
  -->
  <xsl:variable name="indexAllKeywordDetails" select="true()"/>


  <!-- The main metadata language -->
	    <xsl:variable name="isoLangId">
	  	    <xsl:call-template name="langId19139"/>
        </xsl:variable>

  <!-- ========================================================================================= -->
  <xsl:template match="/">
		<Document locale="{$isoLangId}">
			<Field name="_locale" string="{$isoLangId}" store="true" index="true"/>

			<Field name="_docLocale" string="{$isoLangId}" store="true" index="true"/>

			
			<xsl:variable name="_defaultTitle">
				<xsl:call-template name="defaultTitle">
					<xsl:with-param name="isoDocLangId" select="$isoLangId"/>
				</xsl:call-template>
			</xsl:variable>
			<Field name="_defaultTitle" string="{string($_defaultTitle)}" store="true" index="true"/>
			<!-- not tokenized title for sorting, needed for multilingual sorting -->
      <xsl:if test="geonet:info/isTemplate != 's'">
		    <Field name="_title" string="{string($_defaultTitle)}" store="true" index="true" />
      </xsl:if>
		  
			<xsl:apply-templates select="*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']" mode="metadata"/>
		  
			<xsl:apply-templates mode="index" select="*"/>
			
		</Document>
	</xsl:template>
	
	
	<!-- Add index mode template in order to easily add new field in the index (eg. in profiles).
        
        For example, index some keywords from a specific thesaurus in a new field:
        <xsl:template mode="index"
            match="gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/
                        gmd:title/gco:CharacterString='My thesaurus']/
                        gmd:keyword[normalize-space(gco:CharacterString) != '']">
            <Field name="myThesaurusKeyword" string="{string(.)}" store="true" index="true"/>
        </xsl:template>
        
        Note: if more than one template match the same element in a mode, only one will be 
        used (usually the last one).
        
        If matching a upper level element, apply mode to its child to further index deeper level if required:
        <xsl:template mode="index" match="gmd:EX_Extent">
            ... do something
            ... and continue indexing
            <xsl:apply-templates mode="index" select="*"/>
        </xsl:template>
            -->
	<xsl:template mode="index" match="*|@*">
		<xsl:apply-templates mode="index" select="*|@*"/>
	</xsl:template>
	
	
	<xsl:template mode="index"
		match="gmd:extent/gmd:EX_Extent/gmd:description/gco:CharacterString[normalize-space(.) != '']">
		<Field name="extentDesc" string="{string(.)}" store="false" index="true"/>
	</xsl:template>
  
	

	<!-- ========================================================================================= -->

	<xsl:template match="*" mode="metadata">

		<!-- === Data or Service Identification === -->		

		<!-- the double // here seems needed to index MD_DataIdentification when
           it is nested in a SV_ServiceIdentification class -->

		<xsl:for-each select="gmd:identificationInfo//gmd:MD_DataIdentification|
			gmd:identificationInfo//*[contains(@gco:isoType, 'MD_DataIdentification')]|
			gmd:identificationInfo/srv:SV_ServiceIdentification">

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
				<xsl:for-each select="gmd:temporalElement/
				  (gmd:EX_TemporalExtent|gmd:EX_SpatialTemporalExtent)/gmd:extent">
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
        <!-- Index all keywords as text, multilingual text or anchor -->
        <xsl:variable name="listOfKeywords"
                      select="gmd:keyword/gco:CharacterString|
                    gmd:keyword/gmx:Anchor|
                    gmd:keyword/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString"/>
        <xsl:for-each
            select="$listOfKeywords">
                    <Field name="keyword" string="{string(.)}" store="true" index="true"/>
					
          <!-- If INSPIRE is enabled, check if the keyword is one of the 34 themes
          and index annex, theme and theme in english. -->
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
                            <Field name="inspiretheme" string="{string(.)}" store="true" index="true"/>
                <xsl:variable name="englishInspireTheme">
                  <xsl:call-template name="translateInspireThemeToEnglish">
                    <xsl:with-param name="keyword" select="string(.)"/>
                    <xsl:with-param name="inspireThemes" select="$inspire-theme"/>
                  </xsl:call-template>
                </xsl:variable>
                <Field name="inspiretheme_en" string="{$englishInspireTheme}" store="true" index="true"/>
                          	<Field name="inspireannex" string="{$inspireannex}" store="true" index="true"/>
                            <!-- FIXME : inspirecat field will be set multiple time if one record has many themes -->
                          	<Field name="inspirecat" string="true" store="false" index="true"/>
                          </xsl:if>
                        </xsl:if>
                    </xsl:if>
                </xsl:for-each>

        <!-- Index thesaurus name to easily search for records
        using keyword from a thesaurus. -->
        <xsl:for-each select="gmd:thesaurusName/gmd:CI_Citation">
          <xsl:variable name="thesaurusIdentifier"
                        select="gmd:identifier/gmd:MD_Identifier/gmd:code/gmx:Anchor/text()"/>

          <xsl:if test="$thesaurusIdentifier != ''">
            <Field name="thesaurusIdentifier"
                   string="{substring-after(
                              $thesaurusIdentifier,
                              'geonetwork.thesaurus.')}"
                   store="true" index="true"/>
          </xsl:if>
          <xsl:if test="gmd:title/gco:CharacterString/text() != ''">
            <Field name="thesaurusName"
                   string="{gmd:title/gco:CharacterString/text()}"
                   store="true" index="true"/>
          </xsl:if>


          <xsl:if test="$indexAllKeywordDetails and $thesaurusIdentifier != ''">
            <!-- field thesaurus-{{thesaurusIdentifier}}={{keyword}} allows
            to group all keywords of same thesaurus in a field -->
            <xsl:variable name="currentType" select="string(.)"/>
            <xsl:for-each
                select="$listOfKeywords">
              <Field name="thesaurus-{substring-after(
                              $thesaurusIdentifier,
                              'geonetwork.thesaurus.')}"
                     string="{string(.)}"
                     store="true" index="true"/>

            </xsl:for-each>
          </xsl:if>
        </xsl:for-each>

        <!-- Index thesaurus type -->
				<xsl:for-each select="gmd:type/gmd:MD_KeywordTypeCode/@codeListValue">
					<Field name="keywordType" string="{string(.)}" store="true" index="true"/>
          <xsl:if test="$indexAllKeywordDetails">
            <!-- field thesaurusType{{type}}={{keyword}} allows
            to group all keywords of same type in a field -->
            <xsl:variable name="currentType" select="string(.)"/>
            <xsl:for-each
                select="$listOfKeywords">
              <Field name="keywordType-{$currentType}"
                     string="{string(.)}"
                     store="true" index="true"/>
				</xsl:for-each>
          </xsl:if>
			</xsl:for-each>
      </xsl:for-each>
	
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
	
            <xsl:variable name="email" select="/gmd:MD_Metadata/gmd:contact[1]/gmd:CI_ResponsibleParty[1]/gmd:contactInfo[1]/gmd:CI_Contact[1]/gmd:address[1]/gmd:CI_Address[1]/gmd:electronicMailAddress[1]/gco:CharacterString[1]"/>
			<xsl:for-each select="gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString|gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gmx:Anchor">
				<Field name="orgName" string="{string(.)}" store="true" index="true"/>
				
				<xsl:variable name="role" select="../../gmd:role/*/@codeListValue"/>
				<xsl:variable name="logo" select="../..//gmx:FileName/@src"/>
			
				<Field name="responsibleParty" string="{concat($role, '|resource|', ., '|', $logo, '|', $email)}" store="true" index="false"/>
				
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
        <Field name="keyword"
               string="{util:getCodelistTranslation('gmd:MD_TopicCategoryCode', string(.), string($isoLangId))}"
               store="true"
               index="true"/>
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

			<xsl:for-each select="gmd:resourceMaintenance/
				gmd:MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/
				gmd:MD_MaintenanceFrequencyCode/@codeListValue[. != '']">
				<Field name="updateFrequency" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>

			<xsl:for-each select="gmd:status/gmd:MD_ProgressCode/@codeListValue[. != '']">
                <Field name="status" string="{string(.)}" store="true" index="true"/>			
            </xsl:for-each>


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
			
			
			<!-- Index aggregation info and provides option to query by type of association
				and type of initiative
			
			Aggregation info is indexed by adding the following fields to the index:
			 * agg_use: boolean
			 * agg_with_association: {$associationType}
			 * agg_{$associationType}: {$code}
			 * agg_{$associationType}_with_initiative: {$initiativeType}
			 * agg_{$associationType}_{$initiativeType}: {$code}
			 
			Sample queries:
			 * Search for records with siblings: http://localhost:8080/geonetwork/srv/fre/q?agg_use=true
			 * Search for records having a crossReference with another record: 
			 http://localhost:8080/geonetwork/srv/fre/q?agg_crossReference=23f0478a-14ba-4a24-b365-8be88d5e9e8c
			 * Search for records having a crossReference with another record: 
			 http://localhost:8080/geonetwork/srv/fre/q?agg_crossReference=23f0478a-14ba-4a24-b365-8be88d5e9e8c
			 * Search for records having a crossReference of type "study" with another record: 
			 http://localhost:8080/geonetwork/srv/fre/q?agg_crossReference_study=23f0478a-14ba-4a24-b365-8be88d5e9e8c
			 * Search for records having a crossReference of type "study": 
			 http://localhost:8080/geonetwork/srv/fre/q?agg_crossReference_with_initiative=study
			 * Search for records having a "crossReference" : 
			 http://localhost:8080/geonetwork/srv/fre/q?agg_with_association=crossReference
			-->
			<xsl:for-each select="gmd:aggregationInfo/gmd:MD_AggregateInformation">
				<xsl:variable name="code" select="gmd:aggregateDataSetIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString|
												gmd:aggregateDataSetIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString"/>
				<xsl:if test="$code != ''">
					<xsl:variable name="associationType" select="gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue"/>
					<xsl:variable name="initiativeType" select="gmd:initiativeType/gmd:DS_InitiativeTypeCode/@codeListValue"/>
					<Field name="agg_{$associationType}_{$initiativeType}" string="{$code}" store="false" index="true"/>
					<Field name="agg_{$associationType}_with_initiative" string="{$initiativeType}" store="false" index="true"/>
					<Field name="agg_{$associationType}" string="{$code}" store="false" index="true"/>
					<Field name="agg_associated" string="{$code}" store="false" index="true"/>
					<Field name="agg_with_association" string="{$associationType}" store="false" index="true"/>
					<Field name="agg_use" string="true" store="false" index="true"/>
				</xsl:if>
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

      <xsl:for-each select="srv:operatesOn/@xlink:href">
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
							<xsl:choose>
								<xsl:when test="string($fileDescr)='thumbnail'">
									<Field  name="image" string="{concat('thumbnail|', $fileName)}" store="true" index="false"/>
								</xsl:when>
								<xsl:when test="string($fileDescr)='large_thumbnail'">
									<Field  name="image" string="{concat('overview|', $fileName)}" store="true" index="false"/>
								</xsl:when>
								<xsl:otherwise>
									<Field  name="image" string="{concat('unknown|', $fileName)}" store="true" index="false"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:when test="string($fileDescr)='thumbnail'">
							<!-- FIXME : relative path -->
							<Field  name="image" string="{concat($fileDescr, '|', 'resources.get?uuid=', //gmd:fileIdentifier/gco:CharacterString, '&amp;fname=', $fileName, '&amp;access=public')}" store="true" index="false"/>
						</xsl:when>
						<xsl:when test="string($fileDescr)='large_thumbnail'">
							<!-- FIXME : relative path -->
							<Field  name="image" string="{concat('overview', '|', 'resources.get?uuid=', //gmd:fileIdentifier/gco:CharacterString, '&amp;fname=', $fileName, '&amp;access=public')}" store="true" index="false"/>
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

                <!-- If the linkage points to WMS service and no protocol specified, manage as protocol OGC:WMS -->
                <xsl:variable name="wmsLinkNoProtocol" select="contains(lower-case($linkage), 'service=wms') and not(string($protocol))" />

                <!-- ignore empty downloads -->
                <xsl:if test="string($linkage)!='' and not(contains($linkage,$download_check))">
                    <Field name="protocol" string="{string($protocol)}" store="true" index="true"/>
                </xsl:if>

        <xsl:if test="string($title)!='' and string($desc)!='' and not(contains($linkage,$download_check))">
          <Field name="linkage_name_des" string="{string(concat($title, ':::', $desc))}" store="true" index="true"/>
        </xsl:if>

                <xsl:if test="normalize-space($mimetype)!=''">
					<Field name="mimetype" string="{$mimetype}" store="true" index="true"/>
				</xsl:if>
			  
				<xsl:if test="contains($protocol, 'WWW:DOWNLOAD')">
			    	<Field name="download" string="true" store="false" index="true"/>
			  	</xsl:if>

                <xsl:if test="contains($protocol, 'OGC:WMS') or $wmsLinkNoProtocol">
			   	 	<Field name="dynamic" string="true" store="false" index="true"/>
			  	</xsl:if>

                <!-- ignore WMS links without protocol (are indexed below with mimetype application/vnd.ogc.wms_xml) -->
                <xsl:if test="not($wmsLinkNoProtocol)">
                    <Field name="link" string="{concat($title, '|', $desc, '|', $linkage, '|', $protocol, '|', $mimetype)}" store="true" index="false"/>
                </xsl:if>

				<!-- Add KML link if WMS -->
				<xsl:if test="starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-map') and string($linkage)!='' and string($title)!=''">
					<!-- FIXME : relative path -->
					<Field name="link" string="{concat($title, '|', $desc, '|', 
						'../../srv/en/google.kml?uuid=', /gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString, '&amp;layers=', $title, 
						'|application/vnd.google-earth.kml+xml|application/vnd.google-earth.kml+xml')}" store="true" index="false"/>					
				</xsl:if>
				
				<!-- Try to detect Web Map Context by checking protocol or file extension -->
				<xsl:if test="starts-with($protocol,'OGC:WMC') or contains($linkage,'.wmc')">
					<Field name="link" string="{concat($title, '|', $desc, '|', 
						$linkage, '|application/vnd.ogc.wmc|application/vnd.ogc.wmc')}" store="true" index="false"/>
				</xsl:if>

                <xsl:if test="$wmsLinkNoProtocol">
                    <Field name="link" string="{concat($title, '|', $desc, '|',
						$linkage, '|OGC:WMS|application/vnd.ogc.wms_xml')}" store="true" index="false"/>
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
				* INSPIRE Data Specification on <Theme Name> - <version>
				* INSPIRE Specification on <Theme Name> - <version> for CRS and GRID
				
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
		
		<!-- Metadata on maps -->
    <xsl:variable name="isDataset"
                  select="
                  count(gmd:hierarchyLevel[gmd:MD_ScopeCode/@codeListValue='dataset']) > 0 or
                  count(gmd:hierarchyLevel) = 0"/>

		<xsl:variable name="isMapDigital" select="count(gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/
			gmd:presentationForm[gmd:CI_PresentationFormCode/@codeListValue = 'mapDigital']) > 0"/>
		<xsl:variable name="isStatic" select="count(gmd:distributionInfo/gmd:MD_Distribution/
			gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString[contains(., 'PDF') or contains(., 'PNG') or contains(., 'JPEG')]) > 0"/>
		<xsl:variable name="isInteractive" select="count(gmd:distributionInfo/gmd:MD_Distribution/
			gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString[contains(., 'OGC:WMC') or contains(., 'OGC:OWS-C')]) > 0"/>
		<xsl:variable name="isPublishedWithWMCProtocol" select="count(gmd:distributionInfo/gmd:MD_Distribution/
			gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol[starts-with(gco:CharacterString, 'OGC:WMC')]) > 0"/>
		
    <xsl:choose>
      <xsl:when test="$isDataset and $isMapDigital and
        ($isStatic or $isInteractive or $isPublishedWithWMCProtocol)">
			<Field name="type" string="map" store="true" index="true"/>
			<xsl:choose>
				<xsl:when test="$isStatic">
					<Field name="type" string="staticMap" store="true" index="true"/>
				</xsl:when>
				<xsl:when test="$isInteractive or $isPublishedWithWMCProtocol">
					<Field name="type" string="interactiveMap" store="true" index="true"/>
				</xsl:when>
			</xsl:choose>
      </xsl:when>
      <xsl:when test="$isDataset">
        <Field name="type" string="dataset" store="true" index="true"/>
      </xsl:when>
      <xsl:when test="gmd:hierarchyLevel">
        <xsl:for-each select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue[.!='']">
          <Field name="type" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>
      </xsl:when>
    </xsl:choose>
		

		<xsl:choose>
			<!-- Check if metadata is a service metadata record -->
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

    <xsl:for-each select="gmd:metadataStandardName/gco:CharacterString">
      <Field name="standardName" string="{string(.)}" store="true" index="true"/>
    </xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
		
		<xsl:for-each select="gmd:dateStamp/*">
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
				
    <xsl:variable name="identification" select="gmd:identificationInfo//gmd:MD_DataIdentification|
			gmd:identificationInfo//*[contains(@gco:isoType, 'MD_DataIdentification')]|
			gmd:identificationInfo/srv:SV_ServiceIdentification"/>


    <Field name="anylight" store="false" index="true">
      <xsl:attribute name="string">
        <xsl:for-each
            select="$identification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString|
                    $identification/gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString|
                    $identification/gmd:abstract/gco:CharacterString|
                    $identification/gmd:credit/gco:CharacterString|
                    $identification//gmd:organisationName/gco:CharacterString|
                    $identification/gmd:supplementalInformation/gco:CharacterString|
                    $identification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString|
                    $identification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gmx:Anchor">
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
	<!-- This template finds the provided keyword in the skos:prefLabel elements and
	      returns the English one from the same skos:Concept -->
	<xsl:template name="translateInspireThemeToEnglish">
		<xsl:param name="keyword"/>
		<xsl:param name="inspireThemes"/>

    <xsl:value-of select="$inspireThemes/skos:prefLabel[
          @xml:lang='en' and
          ../skos:prefLabel = $keyword]/text()"/>
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
