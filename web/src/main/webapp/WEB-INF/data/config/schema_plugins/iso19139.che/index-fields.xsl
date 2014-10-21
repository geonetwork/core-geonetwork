<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="2.0" xmlns:gmd="http://www.isotc211.org/2005/gmd"
                                        xmlns:gco="http://www.isotc211.org/2005/gco"
                                        xmlns:gml="http://www.opengis.net/gml"
                                        xmlns:srv="http://www.isotc211.org/2005/srv"
                                        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                                        xmlns:gmx="http://www.isotc211.org/2005/gmx"
                                        xmlns:che="http://www.geocat.ch/2008/che"
										xmlns:geonet="http://www.fao.org/geonetwork"
                                        xmlns:xlink="http://www.w3.org/1999/xlink"
                                        xmlns:java="java:org.fao.geonet.util.XslUtil"
                                        xmlns:skos="http://www.w3.org/2004/02/skos/core#">

    <xsl:include href="../iso19139/convert/functions.xsl"/>
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

		<xsl:variable name="poundLangId" select="concat('#', upper-case(java:twoCharLangCode(normalize-space(string($isoLangId)))))" />
        <Document locale="{$isoLangId}">
            <xsl:apply-templates mode="xlinks"/>
            <xsl:apply-templates mode="broken-xlinks"/>
            <Field name="_locale" string="{$isoLangId}" store="true" index="true" token="false"/>

            <Field name="_docLocale" string="{$isoLangId}" store="true" index="true" token="false"/>

            <xsl:variable name="_defaultTitle">
                <xsl:call-template name="defaultTitle">
                    <xsl:with-param name="isoDocLangId" select="$isoLangId"/>
                </xsl:call-template>
            </xsl:variable>

            <!-- not tokenized title for sorting -->
            <Field name="_defaultTitle" string="{string($_defaultTitle)}" store="true" index="true" token="false" />
            <Field name="_title" string="{string($_defaultTitle)}" store="true" index="true" token="false" />

            <xsl:variable name="_defaultAbstract">
                <xsl:call-template name="defaultAbstract">
                    <xsl:with-param name="isoDocLangId" select="$isoLangId"/>
                </xsl:call-template>
            </xsl:variable>

            <Field name="_defaultAbstract" string="{string($_defaultAbstract)}" store="true" index="true" token="false" />


            <xsl:apply-templates select="*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']" mode="metadata">
					<xsl:with-param name="langId" select="$poundLangId" />
            </xsl:apply-templates>
            <xsl:call-template name="hasLinkageURL" />
        </Document>
    </xsl:template>
    
	<!-- ========================================================================================= -->

	<xsl:template match="*" mode="metadata">
		<xsl:param name="langId" />

		<!-- === Data or Service Identification === -->		

		<!-- the double // here seems needed to index MD_DataIdentification when
           it is nested in a SV_ServiceIdentification class -->

		<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification|
							  gmd:identificationInfo/*[@gco:isoType='gmd:MD_DataIdentification']|
							  gmd:identificationInfo/srv:SV_ServiceIdentification|
							  gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']">

			<xsl:for-each select="gmd:citation/gmd:CI_Citation">
				<xsl:for-each select="gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString">
					<Field name="identifier" string="{string(.)}" store="true" index="true" token="false"/>
				</xsl:for-each>

				<xsl:for-each select="gmd:title/gco:CharacterString">
					<Field name="title" string="{string(.)}" store="true" index="true" token="true"/>
				</xsl:for-each>
	
				<xsl:for-each select="gmd:alternateTitle/gco:CharacterString">
					<Field name="altTitle" string="{string(.)}" store="true" index="true" token="true"/>
				</xsl:for-each>

				<xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date">
					<Field name="revisionDate" string="{string(.)}" store="true" index="true" token="false"/>
				</xsl:for-each>

				<xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:Date">
					<Field name="createDate" string="{string(.)}" store="true" index="true" token="false"/>
					<Field name="tempExtentBegin" string="{string(.)}" store="true" index="true" token="false"/>
					<Field name="tempExtentEnd" string="{string(.)}" store="true" index="true" token="false"/>					
				</xsl:for-each>

				<xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='publication']/gmd:date/gco:Date">
					<Field name="publicationDate" string="{string(.)}" store="true" index="true" token="false"/>
				</xsl:for-each>

				<xsl:choose>
					<xsl:when test="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date">
						<xsl:variable name="date" select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date"/>
						<Field name="_revisionDate" string="{$date[1]}" store="true" index="true" token="false"/>
					</xsl:when>
					<xsl:when test="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:Date">
						<xsl:variable name="date" select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:Date"/>
						<Field name="_revisionDate" string="{$date[1]}" store="true" index="true" token="false"/>
					</xsl:when>
					<xsl:when test="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='publication']/gmd:date/gco:Date">
						<xsl:variable name="date" select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='publication']/gmd:date/gco:Date"/>
						<Field name="_revisionDate" string="{$date[1]}" store="true" index="true" token="false"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="date" select="gmd:date/gmd:CI_Date/gmd:date/gco:Date"/>
						<Field name="_revisionDate" string="{$date[1]}" store="true" index="true" token="false"/>
					</xsl:otherwise>
				</xsl:choose>

				<!-- fields used to search for metadata in paper or digital format -->

				<xsl:for-each select="gmd:presentationForm">
					<xsl:if test="contains(gmd:CI_PresentationFormCode/@codeListValue, 'Digital')">
						<Field name="digital" string="true" store="true" index="true" token="false"/>
					</xsl:if>
				
					<xsl:if test="contains(gmd:CI_PresentationFormCode/@codeListValue, 'Hardcopy')">
						<Field name="paper" string="true" store="true" index="true" token="false"/>
					</xsl:if>
				</xsl:for-each>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
	
			<xsl:for-each select="gmd:abstract/gco:CharacterString">
				<Field name="abstract" string="{string(.)}" store="true" index="true" token="true"/>
			</xsl:for-each>
			
			<xsl:choose>
            	<xsl:when test="count(gmd:status[gmd:MD_ProgressCode/@codeListValue = 'historicalArchive']) > 0">
            		<Field name="historicalArchive" string="y" store="true" index="true"/>
            	</xsl:when>
            	<xsl:otherwise>
            		<Field name="historicalArchive" string="n" store="true" index="true" />
            	</xsl:otherwise>
            </xsl:choose>
	            
            <xsl:for-each select="gmd:status/gmd:MD_ProgressCode/@codeListValue">
				<Field name="statusProgressCode" string="{string(.)}" store="true" index="true" token="false"/>
			</xsl:for-each>

			<xsl:for-each select="che:basicGeodataID/gco:CharacterString">
				<Field name="basicgeodataid" string="{string(.)}" store="true" index="true" token="false"/>
				<Field name="type" string="basicgeodata" store="true" index="true" token="false"/>
			</xsl:for-each>
			<xsl:for-each select="che:basicGeodataIDType/che:basicGeodataIDTypeCode[@codeListValue!='']">
				<Field name="type" string="basicgeodata-{@codeListValue}" store="true" index="true" token="false"/>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		

			<xsl:for-each select="*/gmd:EX_Extent">
				<xsl:apply-templates select="gmd:geographicElement/gmd:EX_GeographicBoundingBox" mode="northBLn"/>

				<xsl:for-each select="gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString">
					<Field name="geoDescCode" string="{string(.)}" store="true" index="true" token="false"/>
				</xsl:for-each>

				<xsl:for-each select="gmd:description//gco:CharacterString">
					<Field name="extentDesc" string="{string(.)}" store="true" index="true" token="true"/>
				</xsl:for-each>

				<xsl:for-each select="gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent|
					gmd:temporalElement/gmd:EX_SpatialTemporalExtent/gmd:extent">
					<xsl:for-each select="gml:TimePeriod/gml:beginPosition">
						<Field name="tempExtentBegin" string="{string(.)}" store="true" index="true" token="false"/>
					</xsl:for-each>

					<xsl:for-each select="gml:TimePeriod/gml:endPosition">
						<Field name="tempExtentEnd" string="{string(.)}" store="true" index="true" token="false"/>
					</xsl:for-each>

					<xsl:for-each select="gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition">
						<Field name="tempExtentBegin" string="{string(.)}" store="true" index="true" token="false"/>
					</xsl:for-each>

					<xsl:for-each select="gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition">
						<Field name="tempExtentEnd" string="{string(.)}" store="true" index="true" token="false"/>
					</xsl:for-each>
					
					<xsl:for-each select="gml:TimeInstant/gml:timePosition">
						<Field name="tempExtentBegin" string="{string(.)}" store="true" index="true" token="false"/>
						<Field name="tempExtentEnd" string="{string(.)}" store="true" index="true" token="false"/>
					</xsl:for-each>
					
				</xsl:for-each>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		

			<xsl:for-each select="*/gmd:MD_Keywords">
				<xsl:for-each select="gmd:keyword/gco:CharacterString">
					<Field name="keyword" string="{string(.)}" store="true" index="true" token="false"/>
					<Field name="subject" string="{string(.)}" store="true" index="true" token="false"/>					
				</xsl:for-each>

				<xsl:for-each select="gmd:type/gmd:MD_KeywordTypeCode/@codeListValue">
					<Field name="keywordType" string="{string(.)}" store="true" index="true" token="true"/>
				</xsl:for-each>
			</xsl:for-each>
	
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
	
			<xsl:for-each select="//gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString |
				//che:CHE_CI_ResponsibleParty/gmd:organisationName/gco:CharacterString |
				//che:CHE_CI_ResponsibleParty/che:organisationAcronym/gco:CharacterString">
                <Field name="orgName" string="{string(.)}" store="true" index="true" token="true"/>
                <Field name="_orgName" string="{string(.)}" store="true" index="true" token="false"/>
			</xsl:for-each>
			<xsl:for-each select="//gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString|
				//che:CHE_CI_ResponsibleParty/che:individualFirstName/gco:CharacterString|
				//che:CHE_CI_ResponsibleParty/che:individualLastName/gco:CharacterString">
				<Field name="creator" string="{string(.)}" store="true" index="true" token="true"/>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
	
			<xsl:choose>
				<xsl:when test="gmd:resourceConstraints/gmd:MD_SecurityConstraints">
					<Field name="secConstr" string="true" store="true" index="true" token="false"/>
				</xsl:when>
				<xsl:otherwise>
					<Field name="secConstr" string="false" store="true" index="true" token="false"/>
				</xsl:otherwise>
			</xsl:choose>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
	
			<xsl:for-each select="gmd:topicCategory/gmd:MD_TopicCategoryCode">
				<Field name="topicCat" string="{string(.)}" store="true" index="true" token="false"/>
				<Field name="subject" string="{string(.)}" store="true" index="true" token="false"/>				
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
	
			<xsl:for-each select="gmd:language/gco:CharacterString">
				<Field name="datasetLang" string="{string(.)}" store="true" index="true" token="false"/>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
			
			<xsl:for-each select="gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode/@codeListValue">
				<Field name="spatialRepresentation" string="{string(.)}" store="true" index="true" token="false"/>
			</xsl:for-each>
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->      
			
			<xsl:for-each select="gmd:spatialResolution/gmd:MD_Resolution">
				<xsl:for-each select="gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer">
					<Field name="denominator" string="{string(.)}" store="true" index="true" token="false"/>
				</xsl:for-each>

				<xsl:for-each select="gmd:distance/gco:Distance">
					<Field name="distanceVal" string="{string(.)}" store="true" index="true" token="false"/>
				</xsl:for-each>

				<xsl:for-each select="gmd:distance/gco:Distance/@uom">
					<Field name="distanceUom" string="{string(.)}" store="true" index="true" token="false"/>
				</xsl:for-each>
			</xsl:for-each>



			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<!--  Fields use to search on Service -->
			
			<xsl:for-each select="srv:serviceType/gco:LocalName">
				<Field  name="serviceType" string="{string(.)}" store="true" index="true" token="false"/>
				<Field  name="type" string="service-{string(.)}" store="true" index="true" token="false"/>
			</xsl:for-each>
			
			<xsl:for-each select="srv:serviceTypeVersion/gco:CharacterString">
				<Field  name="serviceTypeVersion" string="{string(.)}" store="true" index="true" token="false"/>
			</xsl:for-each>
			
			<xsl:for-each select="//srv:SV_OperationMetadata/srv:operationName/gco:CharacterString">
				<Field  name="operation" string="{string(.)}" store="true" index="true" token="false"/>
			</xsl:for-each>
			
			<xsl:for-each select="srv:operatesOn/@uuidref">
				<Field  name="operatesOn" string="{string(.)}" store="true" index="true" token="false"/>
			</xsl:for-each>
			
			<xsl:for-each select="srv:coupledResource">
				<xsl:for-each select="srv:SV_CoupledResource/srv:identifier/gco:CharacterString">
					<Field  name="operatesOnIdentifier" string="{string(.)}" store="true" index="true" token="false"/>
				</xsl:for-each>
				
				<xsl:for-each select="srv:SV_CoupledResource/srv:operationName/gco:CharacterString">
					<Field  name="operatesOnName" string="{string(.)}" store="true" index="true" token="false"/>
				</xsl:for-each>
			</xsl:for-each>
			
			<xsl:for-each select="//srv:SV_CouplingType/srv:code/@codeListValue">
				<Field  name="couplingType" string="{string(.)}" store="true" index="true" token="false"/>
			</xsl:for-each>
						
			<!-- Sibling relationships -->
			<xsl:for-each select="*/gmd:MD_AggregateInformation">
				<Field name="{gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue}" 
					string="{string(gmd:aggregateDataSetIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString)}" 
					store="true" index="true" token="false"/>					 
			</xsl:for-each>
			
            <xsl:for-each select="gmd:resourceFormat/gmd:MD_Format/gmd:name/gco:CharacterString">
                <Field name="format" string="{string(.)}" store="true" index="true" token="false"/>
            </xsl:for-each>

            <xsl:for-each select="gmd:resourceFormat/gmd:MD_Format/gmd:version/gco:CharacterString">
                <Field name="formatversion" string="{string(.)}" store="true" index="true" token="false"/>
            </xsl:for-each>

        </xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === Distribution === -->		

        <xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution">
            <xsl:for-each select="gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString">
                <Field name="format" string="{string(.)}" store="true" index="true" token="false"/>
            </xsl:for-each>

            <xsl:for-each select="gmd:distributionFormat/gmd:MD_Format/gmd:version/gco:CharacterString">
                <Field name="formatversion" string="{string(.)}" store="true" index="true" token="false"/>
            </xsl:for-each>

            <xsl:for-each select="gmd:distributor/gmd:MD_Distributor/gmd:distributorFormat/gmd:MD_Format/gmd:name/gco:CharacterString">
                <Field name="format" string="{string(.)}" store="true" index="true"/>
            </xsl:for-each>
            <xsl:for-each select="gmd:distributor/gmd:MD_Distributor/gmd:distributorFormat/gmd:MD_Format/gmd:version/gco:CharacterString">
                <Field name="format" string="{string(.)}" store="true" index="true"/>
            </xsl:for-each>

            <!-- index online protocol -->
            
            <xsl:for-each select="gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:linkage !='']">
                <xsl:apply-templates mode="linkage" select=".">
					<xsl:with-param name="langId" select="$langId" />
				</xsl:apply-templates>
            </xsl:for-each>
        </xsl:for-each>



		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
		<!-- === Service stuff ===  -->
		<!-- Service type           -->
		<xsl:for-each select="gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName|
			gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/srv:serviceType/gco:LocalName">
			<Field name="serviceType" string="{string(.)}" store="true" index="true" token="false"/>
		</xsl:for-each>
		
		<!-- Service version        -->
		<xsl:for-each select="gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceTypeVersion/gco:CharacterString|
			gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']/srv:serviceTypeVersion/gco:CharacterString">
			<Field name="serviceTypeVersion" string="{string(.)}" store="true" index="true" token="false"/>
		</xsl:for-each>

		<xsl:for-each
			select="gmd:identificationInfo/(*[@gco:isoType='srv:SV_ServiceIdentification']|srv:SV_ServiceIdentification)/srv:coupledResource/srv:SV_CoupledResource/gco:ScopedName">
			<xsl:variable name="layerName" select="string(.)" />
			<xsl:variable name="uuid" select="string(../srv:identifier/gco:CharacterString)" />
			<xsl:variable name="allConnectPoint" 
				select="../../../srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/(gmd:URL|che:LocalisedURL|.//che:LocalisedURL)" />
		    <xsl:variable name="connectPoint" select="$allConnectPoint[1]"/>
			<xsl:variable name="serviceUrl">
				<xsl:choose>
					<xsl:when test="$connectPoint=''">
						<xsl:value-of
							select="//gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$connectPoint" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:if test="string-length($layerName) > 0 and string-length($serviceUrl) > 0">
		    	<Field name="wms_uri" string="{$uuid}###{$layerName}###{$serviceUrl}" store="true" index="true" token="false"/>
		    </xsl:if>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === General stuff === -->		

		<xsl:choose>
			<xsl:when test="gmd:hierarchyLevel">
				<xsl:for-each select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue">
					<Field name="type" string="{string(.)}" store="true" index="true" token="false"/>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<Field name="type" string="dataset" store="true" index="true" token="false"/>
			</xsl:otherwise>
		</xsl:choose>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		

		<xsl:for-each select="gmd:hierarchyLevelName/gco:CharacterString">
			<Field name="levelName" string="{string(.)}" store="true" index="true" token="true"/>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		

		<xsl:for-each select="gmd:language/gco:CharacterString">
			<Field name="language" string="{string(.)}" store="true" index="true" token="false"/>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		

		<xsl:for-each select="gmd:fileIdentifier/gco:CharacterString">
			<Field name="fileId" string="{string(.)}" store="true" index="true" token="false"/>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		

		<xsl:for-each select="gmd:parentIdentifier/gco:CharacterString">
			<Field name="parentUuid" string="{string(.)}" store="true" index="true" token="false"/>
		</xsl:for-each>

		<xsl:for-each select="gmd:dateStamp/gco:DateTime">
			<Field name="changeDate" string="{string(.)}" store="true" index="true"/>
		</xsl:for-each>
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === Reference system info === -->		

		<xsl:for-each select="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem">
			<xsl:for-each select="gmd:referenceSystemIdentifier/gmd:RS_Identifier">
				<xsl:variable name="crs" select="concat(string(gmd:codeSpace/gco:CharacterString),'::',string(gmd:code/gco:CharacterString))"/>

				<xsl:if test="$crs != '::'">
					<Field name="crs" string="{$crs}" store="true" index="true" token="false"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === Free text search === -->		

		<Field name="any" store="false" index="true" token="true">
			<xsl:attribute name="string">
				<xsl:apply-templates select="." mode="allText"/>
			</xsl:attribute>
		</Field>

		<xsl:apply-templates select="." mode="codeList"/>
		
	</xsl:template>

	<!-- ========================================================================================= -->
	<!-- codelist element, indexed, not stored nor tokenized -->
	
	<xsl:template match="*" mode="linkage">
		<xsl:param name="langId" />

		<xsl:variable name="download_check"><xsl:text>&amp;fname=&amp;access</xsl:text></xsl:variable>
		<xsl:variable name="linkage" select="gmd:linkage/gmd:URL |
			gmd:linkage//che:LocalisedURL[not(ancestor::gmd:linkage/gmd:URL) and @locale=$langId] |
			gmd:linkage//che:LocalisedURL[not(ancestor::gmd:linkage//che:LocalisedURL[@locale=$langId]) and @locale!=$langId]" />

		<xsl:variable name="title" select="normalize-space(gmd:name/gco:CharacterString|gmd:name/gmx:MimeFileType)"/>
		<xsl:variable name="desc" select="normalize-space(gmd:description/gco:CharacterString)"/>
		<xsl:variable name="protocol" select="normalize-space(gmd:protocol/gco:CharacterString)"/>

        <xsl:variable name="mimetype">
            <xsl:choose>
                <xsl:when test="count($linkage) > 0">
                    <xsl:value-of select="geonet:protocolMimeType($linkage[1], $protocol, gmd:name/gmx:MimeFileType/@type)"/>
                </xsl:when>
                <xsl:otherwise>n/a</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

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
		<Field name="link" string="{concat($title, '|', $desc, '|', $linkage[1], '|', $protocol, '|', $mimetype)}" store="true" index="false"/>
		<Field name="linkage" string="{$linkage[1]}" store="true" index="true" token="false"/>
		
		<!-- Add KML link if WMS -->
		<xsl:if test="starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-map') and string($linkage)!='' and string($title)!=''">

	    	<Field name="wms_uri" string="{/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString}###{$title}###{$linkage[1]}" store="true" index="true" token="false"/>

			<!-- FIXME : relative path -->
			<Field name="link" string="{concat($title, '|', $desc, '|', 
				'../../srv/en/google.kml?uuid=', /gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString, '&amp;layers=', $title, 
				'|application/vnd.google-earth.kml+xml|application/vnd.google-earth.kml+xml')}" store="true" index="false"/>					
		</xsl:if>
	</xsl:template>

	<!-- ========================================================================================= -->
	<!-- codelist element, indexed, not stored nor tokenized -->
	
	<xsl:template match="*[./*/@codeListValue]" mode="codeList">
		<xsl:param name="name" select="name(.)"/>
		
		<Field name="{$name}" string="{*/@codeListValue}" store="false" index="true" token="false"/>		
	</xsl:template>

	<!-- ========================================================================================= -->
	
	<xsl:template match="*" mode="codeList">
		<xsl:apply-templates select="*" mode="codeList"/>
	</xsl:template>

	<!-- ========================================================================================= -->
	<!--allText -->
	
	<xsl:template match="*" mode="allText">
		<xsl:for-each select="@*">
			<xsl:if test="name(.) != 'codeList' ">
				<xsl:value-of select="concat(string(.),' ')"/>
			</xsl:if>	
		</xsl:for-each>

		<xsl:choose>
			<!-- Index all elements in default metadata language (having no locale attribute)
			other terms will go in language specific indices -->
			<xsl:when test="*[@locale]"/>
			<xsl:when test="*"><xsl:apply-templates select="*" mode="allText"/></xsl:when>
			<xsl:otherwise><xsl:value-of select="concat(string(.),' ')"/></xsl:otherwise>			
		</xsl:choose>
	</xsl:template>

	<!-- ========================================================================================= -->
	<!-- xlinks -->

	<xsl:template match="*[contains(string(@xlink:href),'xml.reusable.deleted')]" mode="xlinks" priority="100">
		<Field name="xlink_deleted" string="{@xlink:href}" store="true" index="true" token="false"/>
	</xsl:template>

	<xsl:template match="*[@xlink:href and @xlink:role = 'http://www.geonetwork.org/non_valid_obj']" mode="xlinks">
		<xsl:apply-templates select="." mode="non-valid-xlink"/>
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="*[@xlink:href and not(@xlink:role = 'http://www.geonetwork.org/non_valid_obj')]" mode="xlinks">
		<xsl:apply-templates select="." mode="valid-xlink"/>
		<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="*" mode="xlinks">
		<xsl:apply-templates mode="xlinks"/>
	</xsl:template>
	
	<xsl:template match="text()" mode="xlinks">
	</xsl:template>	

	<xsl:template mode="non-valid-xlink" match="gmd:extent|srv:extent">
		<Field name="invalid_xlink_extent" string="{@xlink:href}" store="true" index="true" token="false"/>
	</xsl:template>
	<xsl:template mode="valid-xlink" match="gmd:extent|srv:extent">
		<Field name="valid_xlink_extent" string="{@xlink:href}" store="true" index="true" token="false"/>
	</xsl:template>

	<xsl:template mode="non-valid-xlink" match="gmd:distributorFormat|gmd:distributionFormat|gmd:resourceFormat">
		<Field name="invalid_xlink_format" string="{@xlink:href}" store="true" index="true" token="false"/>
	</xsl:template>
	<xsl:template mode="valid-xlink" match="gmd:distributorFormat|gmd:distributionFormat|gmd:resourceFormat">
		<Field name="valid_xlink_format" string="{@xlink:href}" store="true" index="true" token="false"/>	
	</xsl:template>

	<xsl:template mode="non-valid-xlink" match="gmd:descriptiveKeywords">
		<Field name="invalid_xlink_keyword" string="{@xlink:href}" store="true" index="true" token="false"/>
	</xsl:template>
	<xsl:template mode="valid-xlink" match="gmd:descriptiveKeywords">
		<Field name="valid_xlink_keyword" string="{@xlink:href}" store="true" index="true" token="false"/>	
	</xsl:template>

	<xsl:template mode="non-valid-xlink" match="che:parentResponsibleParty|gmd:citedResponsibleParty|gmd:pointOfContact|gmd:contact|gmd:userContactInfo|gmd:distributorContact">
		<Field name="invalid_xlink_contact" string="{@xlink:href}" store="true" index="true" token="false"/>
	</xsl:template>
	<xsl:template mode="valid-xlink" match="che:parentResponsibleParty|gmd:citedResponsibleParty|gmd:pointOfContact|gmd:contact|gmd:userContactInfo|gmd:distributorContact">
		<Field name="valid_xlink_contact" string="{@xlink:href}" store="true" index="true" token="false"/>	
	</xsl:template>

	<xsl:template match="text()" mode="non-valid-xlink">
	</xsl:template>
	<xsl:template match="text()" mode="valid-xlink">
	</xsl:template>

	<xsl:template match="*[@xlink:href and count(./*) = 0]" mode="broken-xlinks" priority="100">
		<Field name="xlink_unresolved" string="{@xlink:href}" store="true" index="true" token="false"/>
		<Field name="metadata_broken_xlink" string="1" store="true" index="true" token="false"/>
	</xsl:template>

    <xsl:template match="text()" mode="broken-xlinks">
    </xsl:template>

    <xsl:template match="text()">
    </xsl:template>

</xsl:stylesheet>
