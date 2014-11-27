<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:xhtml="http://www.w3.org/1999/xhtml"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:schold="http://www.ascc.net/xml/schematron"
                xmlns:iso="http://purl.oclc.org/dsdl/schematron"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="#all"
                version="2.0"><!--Implementers: please note that overriding process-prolog or process-root is 
    the preferred method for meta-stylesheets to use where possible. -->
<xsl:param name="archiveDirParameter"/>
   <xsl:param name="archiveNameParameter"/>
   <xsl:param name="fileNameParameter"/>
   <xsl:param name="fileDirParameter"/>
   <xsl:variable name="document-uri">
      <xsl:value-of select="document-uri(/)"/>
   </xsl:variable>

   <!--PHASES-->


<!--PROLOG-->
<xsl:output xmlns:svrl="http://purl.oclc.org/dsdl/svrl" method="xml"
               omit-xml-declaration="no"
               standalone="yes"
               indent="yes"/>
   <xsl:include xmlns:svrl="http://purl.oclc.org/dsdl/svrl" href="../../../xsl/utils-fn.xsl"/>
   <xsl:param xmlns:svrl="http://purl.oclc.org/dsdl/svrl" name="lang"/>
   <xsl:param xmlns:svrl="http://purl.oclc.org/dsdl/svrl" name="thesaurusDir"/>
   <xsl:param xmlns:svrl="http://purl.oclc.org/dsdl/svrl" name="rule"/>
   <xsl:variable xmlns:svrl="http://purl.oclc.org/dsdl/svrl" name="loc"
                 select="document(concat('../loc/', $lang, '/', $rule, '.xml'))"/>

   <!--XSD TYPES FOR XSLT2-->


<!--KEYS AND FUNCTIONS-->


<!--DEFAULT RULES-->


<!--MODE: SCHEMATRON-SELECT-FULL-PATH-->
<!--This mode can be used to generate an ugly though full XPath for locators-->
<xsl:template match="*" mode="schematron-select-full-path">
      <xsl:apply-templates select="." mode="schematron-get-full-path"/>
   </xsl:template>

   <!--MODE: SCHEMATRON-FULL-PATH-->
<!--This mode can be used to generate an ugly though full XPath for locators-->
<xsl:template match="*" mode="schematron-get-full-path">
      <xsl:apply-templates select="parent::*" mode="schematron-get-full-path"/>
      <xsl:text>/</xsl:text>
      <xsl:choose>
         <xsl:when test="namespace-uri()=''">
            <xsl:value-of select="name()"/>
            <xsl:variable name="p_1" select="1+    count(preceding-sibling::*[name()=name(current())])"/>
            <xsl:if test="$p_1&gt;1 or following-sibling::*[name()=name(current())]">[<xsl:value-of select="$p_1"/>]</xsl:if>
         </xsl:when>
         <xsl:otherwise>
            <xsl:text>*[local-name()='</xsl:text>
            <xsl:value-of select="local-name()"/>
            <xsl:text>']</xsl:text>
            <xsl:variable name="p_2"
                          select="1+   count(preceding-sibling::*[local-name()=local-name(current())])"/>
            <xsl:if test="$p_2&gt;1 or following-sibling::*[local-name()=local-name(current())]">[<xsl:value-of select="$p_2"/>]</xsl:if>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   <xsl:template match="@*" mode="schematron-get-full-path">
      <xsl:text>/</xsl:text>
      <xsl:choose>
         <xsl:when test="namespace-uri()=''">@<xsl:value-of select="name()"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:text>@*[local-name()='</xsl:text>
            <xsl:value-of select="local-name()"/>
            <xsl:text>' and namespace-uri()='</xsl:text>
            <xsl:value-of select="namespace-uri()"/>
            <xsl:text>']</xsl:text>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--MODE: SCHEMATRON-FULL-PATH-2-->
<!--This mode can be used to generate prefixed XPath for humans-->
<xsl:template match="node() | @*" mode="schematron-get-full-path-2">
      <xsl:for-each select="ancestor-or-self::*">
         <xsl:text>/</xsl:text>
         <xsl:value-of select="name(.)"/>
         <xsl:if test="preceding-sibling::*[name(.)=name(current())]">
            <xsl:text>[</xsl:text>
            <xsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1"/>
            <xsl:text>]</xsl:text>
         </xsl:if>
      </xsl:for-each>
      <xsl:if test="not(self::*)">
         <xsl:text/>/@<xsl:value-of select="name(.)"/>
      </xsl:if>
   </xsl:template>
   <!--MODE: SCHEMATRON-FULL-PATH-3-->
<!--This mode can be used to generate prefixed XPath for humans 
	(Top-level element has index)-->
<xsl:template match="node() | @*" mode="schematron-get-full-path-3">
      <xsl:for-each select="ancestor-or-self::*">
         <xsl:text>/</xsl:text>
         <xsl:value-of select="name(.)"/>
         <xsl:if test="parent::*">
            <xsl:text>[</xsl:text>
            <xsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1"/>
            <xsl:text>]</xsl:text>
         </xsl:if>
      </xsl:for-each>
      <xsl:if test="not(self::*)">
         <xsl:text/>/@<xsl:value-of select="name(.)"/>
      </xsl:if>
   </xsl:template>

   <!--MODE: GENERATE-ID-FROM-PATH -->
<xsl:template match="/" mode="generate-id-from-path"/>
   <xsl:template match="text()" mode="generate-id-from-path">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:value-of select="concat('.text-', 1+count(preceding-sibling::text()), '-')"/>
   </xsl:template>
   <xsl:template match="comment()" mode="generate-id-from-path">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:value-of select="concat('.comment-', 1+count(preceding-sibling::comment()), '-')"/>
   </xsl:template>
   <xsl:template match="processing-instruction()" mode="generate-id-from-path">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:value-of select="concat('.processing-instruction-', 1+count(preceding-sibling::processing-instruction()), '-')"/>
   </xsl:template>
   <xsl:template match="@*" mode="generate-id-from-path">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:value-of select="concat('.@', name())"/>
   </xsl:template>
   <xsl:template match="*" mode="generate-id-from-path" priority="-0.5">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:text>.</xsl:text>
      <xsl:value-of select="concat('.',name(),'-',1+count(preceding-sibling::*[name()=name(current())]),'-')"/>
   </xsl:template>

   <!--MODE: GENERATE-ID-2 -->
<xsl:template match="/" mode="generate-id-2">U</xsl:template>
   <xsl:template match="*" mode="generate-id-2" priority="2">
      <xsl:text>U</xsl:text>
      <xsl:number level="multiple" count="*"/>
   </xsl:template>
   <xsl:template match="node()" mode="generate-id-2">
      <xsl:text>U.</xsl:text>
      <xsl:number level="multiple" count="*"/>
      <xsl:text>n</xsl:text>
      <xsl:number count="node()"/>
   </xsl:template>
   <xsl:template match="@*" mode="generate-id-2">
      <xsl:text>U.</xsl:text>
      <xsl:number level="multiple" count="*"/>
      <xsl:text>_</xsl:text>
      <xsl:value-of select="string-length(local-name(.))"/>
      <xsl:text>_</xsl:text>
      <xsl:value-of select="translate(name(),':','.')"/>
   </xsl:template>
   <!--Strip characters--><xsl:template match="text()" priority="-1"/>

   <!--SCHEMA SETUP-->
<xsl:template match="/">
      <svrl:schematron-output xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                              title="Schematron validation / MyOcean recommendations"
                              schemaVersion="">
         <xsl:comment>
            <xsl:value-of select="$archiveDirParameter"/>   
		 <xsl:value-of select="$archiveNameParameter"/>  
		 <xsl:value-of select="$fileNameParameter"/>  
		 <xsl:value-of select="$fileDirParameter"/>
         </xsl:comment>
         <svrl:ns-prefix-in-attribute-values uri="http://www.opengis.net/gml" prefix="gml"/>
         <svrl:ns-prefix-in-attribute-values uri="http://www.isotc211.org/2005/gmd" prefix="gmd"/>
         <svrl:ns-prefix-in-attribute-values uri="http://www.isotc211.org/2005/srv" prefix="srv"/>
         <svrl:ns-prefix-in-attribute-values uri="http://www.isotc211.org/2005/gco" prefix="gco"/>
         <svrl:ns-prefix-in-attribute-values uri="http://www.fao.org/geonetwork" prefix="geonet"/>
         <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/1999/xlink" prefix="xlink"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="name">
               <xsl:value-of select="$loc/strings/identification"/>
            </xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M7"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="name">
               <xsl:value-of select="$loc/strings/processing"/>
            </xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M8"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="name">
               <xsl:value-of select="$loc/strings/crs"/>
            </xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M9"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="name">
               <xsl:value-of select="$loc/strings/keywords"/>
            </xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M10"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="name">
               <xsl:value-of select="$loc/strings/spatiotemp"/>
            </xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M11"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="name">
               <xsl:value-of select="$loc/strings/contact"/>
            </xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M12"/>
      </svrl:schematron-output>
   </xsl:template>

   <!--SCHEMATRON PATTERNS-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">Schematron validation / MyOcean recommendations</svrl:text>

   <!--PATTERN $loc/strings/identification-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/identification"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="//gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification|             //*[@gco:isoType='gmd:MD_Metadata']/gmd:identificationInfo/gmd:MD_DataIdentification"
                 priority="1000"
                 mode="M7">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification|             //*[@gco:isoType='gmd:MD_Metadata']/gmd:identificationInfo/gmd:MD_DataIdentification"/>
      <xsl:variable name="title" select="gmd:citation/gmd:CI_Citation/gmd:title"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="normalize-space($title) != ''"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="normalize-space($title) != ''">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R1"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="normalize-space($title) != ''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="normalize-space($title) != ''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R1"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="normalize-space($title)"/>
               <xsl:text/>"</svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="interShortname"
                    select="gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="normalize-space($interShortname) != ''"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="normalize-space($interShortname) != ''">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R2"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="normalize-space($interShortname) != ''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="normalize-space($interShortname) != ''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R2"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="normalize-space($interShortname)"/>
               <xsl:text/>"</svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="altTitle" select="gmd:citation/gmd:CI_Citation/gmd:alternateTitle"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="normalize-space($altTitle) != ''"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="normalize-space($altTitle) != ''">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R3"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="normalize-space($altTitle) != ''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="normalize-space($altTitle) != ''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R3"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="normalize-space($altTitle)"/>
               <xsl:text/>"</svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="creationDate"
                    select="gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="normalize-space($creationDate) != ''"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="normalize-space($creationDate) != ''">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R4"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="normalize-space($creationDate) != ''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="normalize-space($creationDate) != ''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R4"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="normalize-space($creationDate)"/>
               <xsl:text/>"</svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="updateDate" select="gmd:citation/gmd:CI_Citation/gmd:editionDate"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="normalize-space($updateDate) != ''"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="normalize-space($updateDate) != ''">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R5"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="normalize-space($updateDate) != ''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="normalize-space($updateDate) != ''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R5"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="normalize-space($updateDate)"/>
               <xsl:text/>"</svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="abstract" select="gmd:abstract"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="normalize-space($abstract) != ''"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="normalize-space($abstract) != ''">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R6"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="normalize-space($abstract) != ''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="normalize-space($abstract) != ''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R6"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="normalize-space($abstract)"/>
               <xsl:text/>"</svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="overview" select="gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="normalize-space($overview) != ''"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="normalize-space($overview) != ''">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R7"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="normalize-space($overview) != ''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="normalize-space($overview) != ''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R7"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="normalize-space($overview)"/>
               <xsl:text/>"</svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M7"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M7"/>
   <xsl:template match="@*|node()" priority="-2" mode="M7">
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M7"/>
   </xsl:template>

   <!--PATTERN $loc/strings/processing-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/processing"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="//gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification|             //*[@gco:isoType='gmd:MD_Metadata']/gmd:identificationInfo/gmd:MD_DataIdentification"
                 priority="1000"
                 mode="M8">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification|             //*[@gco:isoType='gmd:MD_Metadata']/gmd:identificationInfo/gmd:MD_DataIdentification"/>
      <xsl:variable name="level"
                    select="gmd:descriptiveKeywords                 [gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='processing-level']/gmd:MD_Keywords[1]/gmd:keyword[1]"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="string-join($level, '') != ''"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="string-join($level, '') != ''">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R17"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="string-join($level, '') != ''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="string-join($level, '') != ''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R17"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="string-join($level, ', ')"/>
               <xsl:text/>"
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="period"
                    select="gmd:resourceMaintenance[1]/gmd:MD_MaintenanceInformation[1]/gmd:updateScopeDescription[1]/gmd:MD_ScopeDescription[1]/gmd:other"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="normalize-space($period) != '' and normalize-space($period) != 'P0M0D0H/P0M0D0H'"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="normalize-space($period) != '' and normalize-space($period) != 'P0M0D0H/P0M0D0H'">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R19"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="normalize-space($period) != '' and normalize-space($period) != 'P0M0D0H/P0M0D0H'">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="normalize-space($period) != '' and normalize-space($period) != 'P0M0D0H/P0M0D0H'">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R19"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="normalize-space($period)"/>
               <xsl:text/>"
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M8"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M8"/>
   <xsl:template match="@*|node()" priority="-2" mode="M8">
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M8"/>
   </xsl:template>

   <!--PATTERN $loc/strings/crs-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/crs"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']" priority="1000"
                 mode="M9">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']"/>
      <xsl:variable name="coord"
                    select="gmd:referenceSystemInfo[1]/gmd:MD_ReferenceSystem[1]/gmd:referenceSystemIdentifier[1]/gmd:RS_Identifier[1]/gmd:code"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="normalize-space($coord) != ''"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="normalize-space($coord) != ''">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R24"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="normalize-space($coord) != ''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="normalize-space($coord) != ''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
            	  <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R24"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="normalize-space($coord)"/>
               <xsl:text/>"
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M9"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M9"/>
   <xsl:template match="@*|node()" priority="-2" mode="M9">
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M9"/>
   </xsl:template>

   <!--PATTERN $loc/strings/keywords-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/keywords"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="//gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification|             //*[@gco:isoType='gmd:MD_Metadata']/gmd:identificationInfo/gmd:MD_DataIdentification"
                 priority="1000"
                 mode="M10">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification|             //*[@gco:isoType='gmd:MD_Metadata']/gmd:identificationInfo/gmd:MD_DataIdentification"/>
      <xsl:variable name="mission"
                    select="gmd:descriptiveKeywords                 [gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='discipline']/gmd:MD_Keywords/gmd:keyword[* != '']"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="string-join($mission, '') != ''"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="string-join($mission, '') != ''">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R8"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="string-join($mission, '') != ''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="string-join($mission, '') != ''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R8"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="string-join($mission, ', ')"/>
               <xsl:text/>"</svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="benefit"
                    select="count(gmd:descriptiveKeywords                 [gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='area-of-benefit']/gmd:MD_Keywords/gmd:keyword[* != ''])"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$benefit &gt; 0"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$benefit &gt; 0">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R9"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$benefit &gt; 0">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$benefit &gt; 0">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R9"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="string-join(gmd:descriptiveKeywords                     [gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='area-of-benefit']/gmd:MD_Keywords/gmd:keyword, ', ')"/>
               <xsl:text/>"
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="ocean"
                    select="count(gmd:descriptiveKeywords                 [gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='parameter']/gmd:MD_Keywords/gmd:keyword[* != ''])"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$ocean &gt; 0"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$ocean &gt; 0">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R10"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$ocean &gt; 0">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$ocean &gt; 0">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R10"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="string-join(gmd:descriptiveKeywords                     [gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='parameter']/gmd:MD_Keywords/gmd:keyword, ', ')"/>
               <xsl:text/>"
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="feature"
                    select="count(../../gmd:contentInfo[2]/gmd:MD_FeatureCatalogueDescription/gmd:featureTypes[gco:LocalName!=''])"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$feature &gt; 0"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$feature &gt; 0">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R23"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$feature &gt; 0">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$feature &gt; 0">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R23"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="../../gmd:contentInfo[2]/gmd:MD_FeatureCatalogueDescription/gmd:featureTypes/gco:LocalName"/>
               <xsl:text/>"
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="credit" select="gmd:supplementalInformation"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="normalize-space($credit) != 'display priority:'"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="normalize-space($credit) != 'display priority:'">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R11"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="normalize-space($credit) != 'display priority:'">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="normalize-space($credit) != 'display priority:'">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R11"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="normalize-space($credit)"/>
               <xsl:text/>"
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M10"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M10"/>
   <xsl:template match="@*|node()" priority="-2" mode="M10">
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M10"/>
   </xsl:template>

   <!--PATTERN $loc/strings/spatiotemp-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/spatiotemp"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']" priority="1001"
                 mode="M11">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']"/>
      <xsl:variable name="vert"
                    select="gmd:contentInfo[1]/gmd:MD_CoverageDescription[1]/gmd:dimension[2]/gmd:MD_RangeDimension[1]/gmd:descriptor"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="normalize-space($vert) != '' and normalize-space($vert) != 'vertical level number:'"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="normalize-space($vert) != '' and normalize-space($vert) != 'vertical level number:'">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R25"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="normalize-space($vert) != '' and normalize-space($vert) != 'vertical level number:'">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="normalize-space($vert) != '' and normalize-space($vert) != 'vertical level number:'">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R25"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="normalize-space($vert)"/>
               <xsl:text/>"
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="resolu"
                    select="gmd:contentInfo[1]/gmd:MD_CoverageDescription[1]/gmd:dimension[1]/gmd:MD_RangeDimension[1]/gmd:descriptor"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="normalize-space($resolu) != '' and normalize-space($resolu) != 'temporal resolution:'"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="normalize-space($resolu) != '' and normalize-space($resolu) != 'temporal resolution:'">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R26"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="normalize-space($resolu) != '' and normalize-space($resolu) != 'temporal resolution:'">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="normalize-space($resolu) != '' and normalize-space($resolu) != 'temporal resolution:'">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R26"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="normalize-space($resolu)"/>
               <xsl:text/>"
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M11"/>
   </xsl:template>

	  <!--RULE -->
<xsl:template match="//gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification|             //*[@gco:isoType='gmd:MD_Metadata']/gmd:identificationInfo/gmd:MD_DataIdentification"
                 priority="1000"
                 mode="M11">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification|             //*[@gco:isoType='gmd:MD_Metadata']/gmd:identificationInfo/gmd:MD_DataIdentification"/>
      <xsl:variable name="west" select="gmd:extent[1]//gmd:westBoundLongitude[gco:Decimal!='NaN']"/>
      <xsl:variable name="east" select="gmd:extent[1]//gmd:eastBoundLongitude[gco:Decimal!='NaN']"/>
      <xsl:variable name="north" select="gmd:extent[1]//gmd:northBoundLatitude[gco:Decimal!='NaN']"/>
      <xsl:variable name="south" select="gmd:extent[1]//gmd:southBoundLatitude[gco:Decimal!='NaN']"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="normalize-space($west) != '' and normalize-space($east) != '' and                 normalize-space($north) != '' and normalize-space($south) != ''"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="normalize-space($west) != '' and normalize-space($east) != '' and normalize-space($north) != '' and normalize-space($south) != ''">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R12&#xA;            "/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="normalize-space($west) != '' and normalize-space($east) != '' and                 normalize-space($north) != '' and normalize-space($south) != ''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="normalize-space($west) != '' and normalize-space($east) != '' and normalize-space($north) != '' and normalize-space($south) != ''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R12"/>
               <xsl:text/> "
                <xsl:text/>
               <xsl:copy-of select="normalize-space($west)"/>
               <xsl:text/>
                <xsl:text/>
               <xsl:copy-of select="normalize-space($east)"/>
               <xsl:text/>
                <xsl:text/>
               <xsl:copy-of select="normalize-space($north)"/>
               <xsl:text/>
                <xsl:text/>
               <xsl:copy-of select="normalize-space($south)"/>
               <xsl:text/>"
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="area"
                    select="count(gmd:descriptiveKeywords                 [gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='reference-geographical-area']/gmd:MD_Keywords/gmd:keyword[* != ''])"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$area &gt; 0"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$area &gt; 0">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R13"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$area &gt; 0">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$area &gt; 0">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R13"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="string-join(gmd:descriptiveKeywords                     [gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='reference-geographical-area']/gmd:MD_Keywords/gmd:keyword[* != ''], ', ')"/>
               <xsl:text/>"
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="minValue"
                    select="gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:minimumValue"/>
      <xsl:variable name="maxValue"
                    select="gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:maximumValue"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="normalize-space($minValue) != '' and normalize-space($maxValue) != ''"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="normalize-space($minValue) != '' and normalize-space($maxValue) != ''">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R14"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="normalize-space($minValue) != '' and normalize-space($maxValue) != ''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="normalize-space($minValue) != '' and normalize-space($maxValue) != ''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R14"/>
               <xsl:text/> "
                <xsl:text/>
               <xsl:copy-of select="normalize-space(concat(concat($minValue,' - '), $maxValue))"/>
               <xsl:text/>"
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="startDate"
                    select="gmd:extent/gmd:EX_Extent/gmd:temporalElement[1]/gmd:EX_TemporalExtent[1]/gmd:extent[1]/gml:TimePeriod/descendant-or-self::*[name() = 'gml:beginPosition']"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="normalize-space($startDate) != ''"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="normalize-space($startDate) != ''">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R15"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="normalize-space($startDate) != ''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="normalize-space($startDate) != ''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R15"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="normalize-space($startDate)"/>
               <xsl:text/>"
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="endDate"
                    select="gmd:extent/gmd:EX_Extent/gmd:temporalElement[1]/gmd:EX_TemporalExtent[1]/gmd:extent[1]/gml:TimePeriod/descendant-or-self::*[name() = 'gml:endPosition']"/>
      <xsl:variable name="isIndeterminate"
                    select="gmd:extent/gmd:EX_Extent/gmd:temporalElement[1]/gmd:EX_TemporalExtent[1]/gmd:extent[1]/gml:TimePeriod/descendant-or-self::*[name() = 'gml:endPosition']/@indeterminatePosition='unknown'"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="normalize-space($endDate) != '' or $isIndeterminate"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="normalize-space($endDate) != '' or $isIndeterminate">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R15.end"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="normalize-space($endDate) != ''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="normalize-space($endDate) != ''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R15.end"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="normalize-space($endDate)"/>
               <xsl:text/>"
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--REPORT -->
<xsl:if test=" $isIndeterminate">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$isIndeterminate">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R15.endIndeterminate"/>
               <xsl:text/>
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="scale"
                    select="gmd:descriptiveKeywords                 [gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='temporal-scale']/gmd:MD_Keywords[1]/gmd:keyword[1]"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="string-join($scale, '') != ''"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="string-join($scale, '') != ''">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R16"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="string-join($scale, '') != ''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="string-join($scale, '') != ''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R16"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="string-join($scale, ', ')"/>
               <xsl:text/>"
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="freq"
                    select="gmd:resourceMaintenance[1]/gmd:MD_MaintenanceInformation[1]/gmd:maintenanceAndUpdateFrequency[1]/gmd:MD_MaintenanceFrequencyCode[1]/@codeListValue"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="normalize-space($freq) != ''"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="normalize-space($freq) != ''">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R18"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="normalize-space($freq) != ''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="normalize-space($freq) != ''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R18"/>
               <xsl:text/> "<xsl:text/>
               <xsl:copy-of select="normalize-space($freq)"/>
               <xsl:text/>"
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M11"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M11"/>
   <xsl:template match="@*|node()" priority="-2" mode="M11">
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M11"/>
   </xsl:template>

   <!--PATTERN $loc/strings/contact-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/contact"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']" priority="1000"
                 mode="M12">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']"/>
      <xsl:variable name="manager"
                    select="count(gmd:identificationInfo/gmd:MD_DataIdentification/                 gmd:pointOfContact[gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue='originator'])"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$manager &gt; 0"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$manager &gt; 0">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R21"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$manager &gt; 0">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$manager &gt; 0">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$manager"/>
               <xsl:text/> 
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R21"/>
               <xsl:text/>
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="center"
                    select="count(gmd:identificationInfo/gmd:MD_DataIdentification/                 gmd:pointOfContact[gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue='custodian'])"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$center &gt; 0"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$center &gt; 0">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R20"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$center &gt; 0">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$center &gt; 0">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$center"/>
               <xsl:text/> 
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R20"/>
               <xsl:text/>
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="desk"
                    select="count(gmd:identificationInfo/gmd:MD_DataIdentification/                 gmd:pointOfContact[gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue='pointOfContact'])"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$desk &gt; 0"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$desk &gt; 0">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.R22"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$desk &gt; 0">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$desk &gt; 0">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
                <xsl:text/>
               <xsl:copy-of select="$desk"/>
               <xsl:text/> 
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.R22"/>
               <xsl:text/>
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M12"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M12"/>
   <xsl:template match="@*|node()" priority="-2" mode="M12">
      <xsl:apply-templates select="*|comment()|processing-instruction()" mode="M12"/>
   </xsl:template>
</xsl:stylesheet>