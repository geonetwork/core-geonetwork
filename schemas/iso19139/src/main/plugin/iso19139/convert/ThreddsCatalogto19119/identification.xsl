<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:wms="http://www.opengis.net/wms"
                xmlns:math="http://exslt.org/math"
                xmlns:date="http://exslt.org/dates-and-times"
                xmlns:exslt="http://exslt.org/common"
                version="2.0"
		exclude-result-prefixes="wms xsl date exslt math"
                extension-element-prefixes="math exslt date">

  <!-- =================================================================== -->

  <xsl:template match="*" mode="SrvDataIdentification">
    <xsl:param name="topic"/>
    <xsl:param name="name"/>
    <xsl:param name="type"/>
    <xsl:param name="desc"/>
    <xsl:param name="props"/>
    <xsl:param name="version"/>
    <xsl:param name="serverops"/>
    <xsl:param name="bbox"/>
    <xsl:param name="textent"/>


    <gmd:citation>
      <gmd:CI_Citation>
        <gmd:title>
          <gco:CharacterString>
            <xsl:value-of select="$name"/>
          </gco:CharacterString>
        </gmd:title>
        <gmd:date>
          <gmd:CI_Date>
            <xsl:variable name="df">[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]</xsl:variable>
            <gmd:date>
              <gco:DateTime>
                <xsl:value-of select="format-dateTime(current-dateTime(),$df)"/>
              </gco:DateTime>
            </gmd:date>
            <gmd:dateType>
              <gmd:CI_DateTypeCode codeList="./resources/codeList.xml#CI_DateTypeCode"
                                   codeListValue="revision"/>
            </gmd:dateType>
          </gmd:CI_Date>
        </gmd:date>
      </gmd:CI_Citation>
    </gmd:citation>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <gmd:abstract>
      <gco:CharacterString>
        <xsl:value-of select="concat('Description: ',$desc,' Properties : ',$props)"/>
      </gco:CharacterString>
    </gmd:abstract>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <gmd:status>
      <gmd:MD_ProgressCode codeList="./resources/codeList.xml#MD_ProgressCode"
                           codeListValue="completed"/>
    </gmd:status>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="//wms:ContactInformation">
      <gmd:pointOfContact>
        <gmd:CI_ResponsibleParty>
          <xsl:apply-templates select="." mode="RespParty"/>
        </gmd:CI_ResponsibleParty>
      </gmd:pointOfContact>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <srv:serviceType>
      <gco:LocalName codeSpace="www.w3c.org">
        <xsl:value-of select="$type"/>
      </gco:LocalName>
    </srv:serviceType>
    <srv:serviceTypeVersion>
      <gco:CharacterString>
        <xsl:value-of select="$version"/>
      </gco:CharacterString>
    </srv:serviceTypeVersion>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <srv:accessProperties>
      <gmd:MD_StandardOrderProcess>
        <gmd:fees>
          <gco:CharacterString>Free</gco:CharacterString>
        </gmd:fees>
      </gmd:MD_StandardOrderProcess>
    </srv:accessProperties>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:variable name="bboxtokens" select="tokenize($bbox,'\^\^\^')"/>
    <srv:extent>
      <gmd:EX_Extent>
        <gmd:geographicElement>
          <gmd:EX_GeographicBoundingBox>
            <gmd:westBoundLongitude>
              <gco:Decimal><xsl:value-of select="$bboxtokens[3]"/></gco:Decimal>
            </gmd:westBoundLongitude>
            <gmd:eastBoundLongitude>
              <gco:Decimal><xsl:value-of select="$bboxtokens[4]"/></gco:Decimal>
            </gmd:eastBoundLongitude>
            <gmd:southBoundLatitude>
              <gco:Decimal><xsl:value-of select="$bboxtokens[1]"/></gco:Decimal>
            </gmd:southBoundLatitude>
            <gmd:northBoundLatitude>
              <gco:Decimal><xsl:value-of select="$bboxtokens[2]"/></gco:Decimal>
            </gmd:northBoundLatitude>
          </gmd:EX_GeographicBoundingBox>
        </gmd:geographicElement>
      </gmd:EX_Extent>
    </srv:extent>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:variable name="textenttokens" select="tokenize($textent,'\^\^\^')"/>
		<srv:extent>
			<gmd:EX_Extent>
				<gmd:temporalElement>
					<gmd:EX_TemporalExtent>
						<gmd:extent>
							<gml:TimePeriod gml:id="TP1">
								<gml:beginPosition><xsl:value-of select="$textenttokens[1]"/></gml:beginPosition>
								<gml:endPosition><xsl:value-of select="$textenttokens[2]"/></gml:endPosition>
							</gml:TimePeriod>
						</gmd:extent>
					</gmd:EX_TemporalExtent>
				</gmd:temporalElement>
			</gmd:EX_Extent>
		</srv:extent>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <srv:couplingType>
      <srv:SV_CouplingType codeList="#SV_CouplingType" codeListValue="tight">
        tight
      </srv:SV_CouplingType>
    </srv:couplingType>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <srv:containsOperations>
      <srv:SV_OperationMetadata>

        <xsl:choose>
          <xsl:when test="contains($type,'OPENDAP')">
            <xsl:if test="contains($serverops,'das')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'das'"/>
                <xsl:with-param name="title" select="'dataset attribute structure'"/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="contains($serverops,'dds')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'dds'"/>
                <xsl:with-param name="title" select="'dataset descriptor structure'"/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="contains($serverops,'dods')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'dods'"/>
                <xsl:with-param name="title"
                                select="'dataset descriptor structure populated with data using a constraint'"/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="contains($serverops,'ddx')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'ddx'"/>
                <xsl:with-param name="title"
                                select="'XML version of dataset descriptor structure (dds) and dataset attribute structure (das)'"/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="contains($serverops,'blob')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'blob'"/>
                <xsl:with-param name="title"
                                select="'Serialized binary data content for requested data set, with a constraint expression applied'"/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="contains($serverops,'info')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'info'"/>
                <xsl:with-param name="title"
                                select="'info object (attributes, types and other information)'"/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="contains($serverops,'html')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'html'"/>
                <xsl:with-param name="title" select="'html input form for a dataset'"/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="contains($serverops,'ver')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'ver'"/>
                <xsl:with-param name="title" select="'return the version number of the server'"/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="contains($serverops,'help')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'help'"/>
                <xsl:with-param name="title" select="'return help from server'"/>
              </xsl:call-template>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <srv:operationName>
              <gco:CharacterString>
                <xsl:value-of select="$name"/>
              </gco:CharacterString>
            </srv:operationName>
            <srv:DCP>
              <srv:DCPList codeList="#DCPList">
                <xsl:attribute name="codeListValue">
                  <xsl:value-of select="http"/>
                </xsl:attribute>
              </srv:DCPList>
            </srv:DCP>
            <!-- URL will be a string of urls separated by ^^^ -->
            <xsl:for-each select="tokenize($url,'\^\^\^')"> 
            	<srv:connectPoint>
              	<gmd:CI_OnlineResource>
                	<gmd:linkage>
                  	<gmd:URL>
                    	<xsl:value-of select="."/>
                  	</gmd:URL>
                	</gmd:linkage>
                	<gmd:protocol>
										<xsl:choose>
                    	<xsl:when test="contains($type,'WMS')">
                  			<gco:CharacterString>OGC:WMS</gco:CharacterString>
											</xsl:when>
                    	<xsl:when test="contains($type,'NETCDFSUBSET')">
                  			<gco:CharacterString>"WWW:LINK-1.0-http--netcdfsubset</gco:CharacterString>
											</xsl:when>
                    	<xsl:when test="contains($type,'OPENDAP')">
                  			<gco:CharacterString>"WWW:LINK-1.0-http--opendap</gco:CharacterString>
											</xsl:when>
											<xsl:otherwise>
                  			<gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>
											</xsl:otherwise>
										</xsl:choose>
                	</gmd:protocol>
                	<gmd:description>
                  	<gco:CharacterString>
                    	<xsl:value-of select="$desc"/>
                  	</gco:CharacterString>
                	</gmd:description>
                	<gmd:function>
                  	<gmd:CI_OnLineFunctionCode
                    	codeList="./resources/codeList.xml#CI_OnLineFunctionCode"
                    	codeListValue="information"/>
                	</gmd:function>
              	</gmd:CI_OnlineResource>
            	</srv:connectPoint>
					  </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>
      </srv:SV_OperationMetadata>
    </srv:containsOperations>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        <srv:operatesOn> is done by harvester after data metadata creation
          -->

  </xsl:template>

  <!-- =================================================================== -->
  <!-- === opendapOps === -->
  <!-- =================================================================== -->

  <xsl:template name="opendapOps">
    <xsl:param name="op"/>
    <xsl:param name="title"/>

    <srv:operationName>
      <gco:CharacterString>
        <xsl:value-of select="concat($op,' - ',$title)"/>
      </gco:CharacterString>
    </srv:operationName>
    <srv:DCP>
      <srv:DCPList codeList="#DCPList">
        <xsl:attribute name="codeListValue">
          <xsl:value-of select="http"/>
        </xsl:attribute>
      </srv:DCPList>
    </srv:DCP>
    <srv:connectPoint>
      <gmd:CI_OnlineResource>
        <gmd:linkage>
          <gmd:URL>
            <xsl:value-of select="concat($url,'/help')"/>
          </gmd:URL>
        </gmd:linkage>
        <gmd:protocol>
          <gco:CharacterString>
            WWW:LINK-1.0-http--link
          </gco:CharacterString>
        </gmd:protocol>
        <gmd:description>
          <gco:CharacterString>
            <xsl:value-of select="concat('Help on ',$op,' - ',$title)"/>
          </gco:CharacterString>
        </gmd:description>
        <gmd:function>
          <gmd:CI_OnLineFunctionCode codeList="./resources/codeList.xml#CI_OnLineFunctionCode"
                                     codeListValue="information"/>
        </gmd:function>
      </gmd:CI_OnlineResource>
    </srv:connectPoint>
  </xsl:template>


  <!-- =================================================================== -->
  <!-- === DataIdentification === -->
  <!-- =================================================================== -->

  <xsl:template match="*" mode="DataIdentification">
    <xsl:param name="topic"/>
    <xsl:param name="name"/>
    <xsl:param name="desc"/>
    <xsl:param name="bbox"/>
    <xsl:param name="textent"/>
    <xsl:param name="modificationdate"/>

    <gmd:citation>
      <gmd:CI_Citation>
        <gmd:title>
          <gco:CharacterString><xsl:value-of select="$name"/></gco:CharacterString>
        </gmd:title>
				<xsl:if test="normalize-space($modificationdate)!=''">
        	<gmd:date>
          	<gmd:CI_Date>
            	<gmd:date>
              	<gco:DateTime><xsl:value-of select="$modificationdate"/></gco:DateTime>
            	</gmd:date>
            	<gmd:dateType>
              	<gmd:CI_DateTypeCode codeList="./resources/codeList.xml#CI_DateTypeCode"
                                   	codeListValue="revision"/>
            	</gmd:dateType>
          	</gmd:CI_Date>
        	</gmd:date>
				</xsl:if>
      </gmd:CI_Citation>
    </gmd:citation>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <gmd:abstract>
      <gco:CharacterString><xsl:value-of select="$desc"/></gco:CharacterString>
    </gmd:abstract>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <gmd:status>
      <gmd:MD_ProgressCode codeList="./resources/codeList.xml#MD_ProgressCode"
                           codeListValue="completed"/>
    </gmd:status>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="//wms:ContactInformation">
      <gmd:pointOfContact>
        <gmd:CI_ResponsibleParty>
          <xsl:apply-templates select="." mode="RespParty"/>
        </gmd:CI_ResponsibleParty>
      </gmd:pointOfContact>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="//wms:KeywordList">
    	<gmd:descriptiveKeywords>
      		<gmd:MD_Keywords>
	 					<xsl:apply-templates select="." mode="Keywords"/>
      		</gmd:MD_Keywords>
    	</gmd:descriptiveKeywords>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <gmd:spatialRepresentationType>
      <gmd:MD_SpatialRepresentationTypeCode
        codeList="./resources/codeList.xml#MD_SpatialRepresentationTypeCode" codeListValue="grid"/>
    </gmd:spatialRepresentationType>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <!-- English is default -->
    <gmd:language>
      <gco:CharacterString>eng</gco:CharacterString>
    </gmd:language>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <gmd:characterSet>
      <gmd:MD_CharacterSetCode codeList="./resources/codeList.xml#MD_CharacterSetCode"
                               codeListValue="utf8"/>
    </gmd:characterSet>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <gmd:topicCategory>
      <gmd:MD_TopicCategoryCode><xsl:value-of select="$topic"/></gmd:MD_TopicCategoryCode>
    </gmd:topicCategory>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:choose>
			<xsl:when test="normalize-space($bbox)!=''">

    <xsl:variable name="bboxtokens" select="tokenize($bbox,'\^\^\^')"/>

    <gmd:extent>
      <gmd:EX_Extent>
        <gmd:geographicElement>
          <gmd:EX_GeographicBoundingBox>
            <gmd:westBoundLongitude>
              <gco:Decimal><xsl:value-of select="$bboxtokens[3]"/></gco:Decimal>
            </gmd:westBoundLongitude>
            <gmd:eastBoundLongitude>
              <gco:Decimal><xsl:value-of select="$bboxtokens[4]"/></gco:Decimal>
            </gmd:eastBoundLongitude>
            <gmd:southBoundLatitude>
              <gco:Decimal><xsl:value-of select="$bboxtokens[1]"/></gco:Decimal>
            </gmd:southBoundLatitude>
            <gmd:northBoundLatitude>
              <gco:Decimal><xsl:value-of select="$bboxtokens[2]"/></gco:Decimal>
            </gmd:northBoundLatitude>
          </gmd:EX_GeographicBoundingBox>
        </gmd:geographicElement>
      </gmd:EX_Extent>
    </gmd:extent>
	
			</xsl:when>
			<xsl:otherwise>  <!-- Use the stuff provided by the wms -->

		<xsl:variable name="wmsBBOX" select="//wms:Layer/wms:BoundingBox"/>

    <gmd:extent>
      <gmd:EX_Extent>
        <gmd:geographicElement>
          <gmd:EX_GeographicBoundingBox>
            <gmd:westBoundLongitude>
              <gco:Decimal><xsl:value-of select="$wmsBBOX[1]/@minx"/></gco:Decimal>
            </gmd:westBoundLongitude>
            <gmd:eastBoundLongitude>
              <gco:Decimal><xsl:value-of select="$wmsBBOX[1]/@maxx"/></gco:Decimal>
            </gmd:eastBoundLongitude>
            <gmd:southBoundLatitude>
              <gco:Decimal><xsl:value-of select="$wmsBBOX[1]/@miny"/></gco:Decimal>
            </gmd:southBoundLatitude>
            <gmd:northBoundLatitude>
              <gco:Decimal><xsl:value-of select="$wmsBBOX[1]/@maxy"/></gco:Decimal>
            </gmd:northBoundLatitude>
          </gmd:EX_GeographicBoundingBox>
        </gmd:geographicElement>
      </gmd:EX_Extent>
    </gmd:extent>

			</xsl:otherwise>
		</xsl:choose>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<xsl:choose>
			<xsl:when test="normalize-space($textent)!=''">

    <xsl:variable name="textenttokens" select="tokenize($textent,'\^\^\^')"/>
		<gmd:extent>
			<gmd:EX_Extent>
				<gmd:temporalElement>
					<gmd:EX_TemporalExtent>
						<gmd:extent>
							<gml:TimePeriod gml:id="TP1">
								<gml:beginPosition><xsl:value-of select="$textenttokens[1]"/></gml:beginPosition>
								<gml:endPosition><xsl:value-of select="$textenttokens[2]"/></gml:endPosition>
							</gml:TimePeriod>
						</gmd:extent>
					</gmd:EX_TemporalExtent>
				</gmd:temporalElement>
			</gmd:EX_Extent>
		</gmd:extent>

			</xsl:when>
			<xsl:otherwise>

		<xsl:variable name="wmsTextent" select="//wms:Layer[@queryable='1']/wms:Dimension"/>
		<xsl:variable name="wTokens" select="tokenize($wmsTextent[1]/text(),',')"/>

		<gmd:extent>
			<gmd:EX_Extent>
				<gmd:temporalElement>
					<gmd:EX_TemporalExtent>
						<gmd:extent>
							<gml:TimePeriod gml:id="TP1">
								<gml:beginPosition><xsl:value-of select="$wTokens[1]"/></gml:beginPosition>
								<gml:endPosition><xsl:value-of select="$wTokens[last()]"/></gml:endPosition>
							</gml:TimePeriod>
						</gmd:extent>
					</gmd:EX_TemporalExtent>
				</gmd:temporalElement>
			</gmd:EX_Extent>
		</gmd:extent>

			</xsl:otherwise>
		</xsl:choose>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

  </xsl:template>

  <!-- =================================================================== -->
  <!-- === Keywords === -->
  <!-- =================================================================== -->

  <xsl:template match="*" mode="Keywords">
    <xsl:for-each select="wms:Keyword">
      <gmd:keyword>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:keyword>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <gmd:type>
      <gmd:MD_KeywordTypeCode codeList="./resources/codeList.xml#MD_KeywordTypeCode"
                              codeListValue="theme"/>
    </gmd:type>

  </xsl:template>

  <!-- =================================================================== -->

</xsl:stylesheet>
