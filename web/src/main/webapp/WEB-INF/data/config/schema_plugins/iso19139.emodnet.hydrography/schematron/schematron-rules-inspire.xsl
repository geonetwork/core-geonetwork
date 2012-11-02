<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:xhtml="http://www.w3.org/1999/xhtml"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:schold="http://www.ascc.net/xml/schematron"
                xmlns:iso="http://purl.oclc.org/dsdl/schematron"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:xlink="http://www.w3.org/1999/xlink"
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
                 select="document(concat('../loc/', $lang, '/', substring-before($rule, '.xsl'), '.xml'))"/>

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
         </xsl:when>
         <xsl:otherwise>
            <xsl:text>*:</xsl:text>
            <xsl:value-of select="local-name()"/>
            <xsl:text>[namespace-uri()='</xsl:text>
            <xsl:value-of select="namespace-uri()"/>
            <xsl:text>']</xsl:text>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="preceding"
                    select="count(preceding-sibling::*[local-name()=local-name(current())                                   and namespace-uri() = namespace-uri(current())])"/>
      <xsl:text>[</xsl:text>
      <xsl:value-of select="1+ $preceding"/>
      <xsl:text>]</xsl:text>
   </xsl:template>
   <xsl:template match="@*" mode="schematron-get-full-path">
      <xsl:apply-templates select="parent::*" mode="schematron-get-full-path"/>
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
                              title="INSPIRE metadata implementing rule validation"
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
         <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/2004/02/skos/core#" prefix="skos"/>
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
         <xsl:apply-templates select="/" mode="M8"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="name">
               <xsl:value-of select="$loc/strings/dataIdentification"/>
            </xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M9"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="name">
               <xsl:value-of select="$loc/strings/serviceIdentification"/>
            </xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M10"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="name">
               <xsl:value-of select="$loc/strings/theme"/>
            </xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M11"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="name">
               <xsl:value-of select="$loc/strings/serviceTaxonomy"/>
            </xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M12"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="name">
               <xsl:value-of select="$loc/strings/geo"/>
            </xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M13"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="name">
               <xsl:value-of select="$loc/strings/temporal"/>
            </xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M14"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="name">
               <xsl:value-of select="$loc/strings/quality"/>
            </xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M15"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="name">
               <xsl:value-of select="$loc/strings/conformity"/>
            </xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M16"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="name">
               <xsl:value-of select="$loc/strings/constraints"/>
            </xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M17"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="name">
               <xsl:value-of select="$loc/strings/org"/>
            </xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M18"/>
         <svrl:active-pattern>
            <xsl:attribute name="document">
               <xsl:value-of select="document-uri(/)"/>
            </xsl:attribute>
            <xsl:attribute name="name">
               <xsl:value-of select="$loc/strings/metadata"/>
            </xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M19"/>
      </svrl:schematron-output>
   </xsl:template>

   <!--SCHEMATRON PATTERNS-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">INSPIRE metadata implementing rule validation</svrl:text>

   <!--PATTERN $loc/strings/identification-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/identification"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="//gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation" priority="1003"
                 mode="M8">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation"/>
      <xsl:variable name="noResourceTitle"
                    select="not(gmd:title) or gmd:title/@gco:nilReason='missing'"/>
      <xsl:variable name="resourceTitle" select="gmd:title/*/text()"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not($noResourceTitle)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="not($noResourceTitle)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M35/div"/>
                  <xsl:text/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="not($noResourceTitle)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="not($noResourceTitle)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M35/div"/>
               <xsl:text/>
               <xsl:text/>
               <xsl:copy-of select="$resourceTitle"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M8"/>
   </xsl:template>

	  <!--RULE -->
<xsl:template match="//gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine/gmd:CI_OnlineResource"
                 priority="1002"
                 mode="M8">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine/gmd:CI_OnlineResource"/>
      <xsl:variable name="resourceLocator" select="gmd:linkage/*/text()"/>
      <xsl:variable name="noResourceLocator"
                    select="normalize-space(gmd:linkage/gmd:URL)=''       or not(gmd:linkage)"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not($noResourceLocator)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="not($noResourceLocator)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				              <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M52/div"/>
                  <xsl:text/>
			            </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="not($noResourceLocator)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="not($noResourceLocator)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M52/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$resourceLocator"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M8"/>
   </xsl:template>

	  <!--RULE -->
<xsl:template match="//gmd:MD_Metadata" priority="1001" mode="M8">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="//gmd:MD_Metadata"/>
      <xsl:variable name="resourceType_present"
                    select="gmd:hierarchyLevel/*/@codeListValue='dataset'     or gmd:hierarchyLevel/*/@codeListValue='series'     or gmd:hierarchyLevel/*/@codeListValue='service'"/>
      <xsl:variable name="resourceType"
                    select="string-join(gmd:hierarchyLevel/*/@codeListValue, ',')"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$resourceType_present"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$resourceType_present">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				              <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M37/div"/>
                  <xsl:text/>
			            </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$resourceType_present">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$resourceType_present">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M37/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$resourceType"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M8"/>
   </xsl:template>

	  <!--RULE -->
<xsl:template match="//gmd:MD_DataIdentification|    //*[@gco:isoType='gmd:MD_DataIdentification']|    //srv:SV_ServiceIdentification|    //*[@gco:isoType='srv:SV_ServiceIdentification']"
                 priority="1000"
                 mode="M8">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:MD_DataIdentification|    //*[@gco:isoType='gmd:MD_DataIdentification']|    //srv:SV_ServiceIdentification|    //*[@gco:isoType='srv:SV_ServiceIdentification']"/>
      <xsl:variable name="resourceAbstract" select="gmd:abstract/*/text()"/>
      <xsl:variable name="noResourceAbstract"
                    select="not(gmd:abstract) or gmd:abstract/@gco:nilReason='missing'"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not($noResourceAbstract)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="not($noResourceAbstract)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				              <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M36/div"/>
                  <xsl:text/>				
			            </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="not($noResourceAbstract)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="not($noResourceAbstract)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M36/div"/>
               <xsl:text/>				
				           <xsl:text/>
               <xsl:copy-of select="$resourceAbstract"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M8"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M8"/>
   <xsl:template match="@*|node()" priority="-2" mode="M8">
      <xsl:apply-templates select="*" mode="M8"/>
   </xsl:template>

   <!--PATTERN $loc/strings/dataIdentification-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/dataIdentification"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="//gmd:MD_DataIdentification[    ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'series'    or ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'dataset'    or ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = '']|    //*[@gco:isoType='gmd:MD_DataIdentification' and (    ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'series'    or ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'dataset'    or ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = '')]"
                 priority="1000"
                 mode="M9">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:MD_DataIdentification[    ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'series'    or ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'dataset'    or ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = '']|    //*[@gco:isoType='gmd:MD_DataIdentification' and (    ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'series'    or ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'dataset'    or ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = '')]"/>
      <xsl:variable name="resourceLanguage"
                    select="string-join(gmd:language/gco:CharacterString|gmd:language/gmd:LanguageCode/@codeListValue, ', ')"/>
      <xsl:variable name="euLanguage"
                    select="     not(gmd:language/@gco:nilReason='missing') and      geonet:contains-any-of($resourceLanguage,      ('eng', 'fre', 'ger', 'spa', 'dut', 'ita', 'cze', 'lav', 'dan', 'lit', 'mlt',      'pol', 'est', 'por', 'fin', 'rum', 'slo', 'slv', 'gre', 'bul',      'hun', 'swe', 'gle'))"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$euLanguage"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$euLanguage">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				              <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M55/div"/>
                  <xsl:text/>
			            </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$euLanguage">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$euLanguage">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M55/div"/>
               <xsl:text/>
               <xsl:text/>
               <xsl:copy-of select="$resourceLanguage"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="topic" select="gmd:topicCategory/gmd:MD_TopicCategoryCode"/>
      <xsl:variable name="noTopic"
                    select="not(gmd:topicCategory)  or      gmd:topicCategory/gmd:MD_TopicCategoryCode/text() = ''"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not($noTopic)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="not($noTopic)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				              <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M39/div"/>
                  <xsl:text/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="not($noTopic)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="not($noTopic)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M39/div"/>
               <xsl:text/>
               <xsl:text/>
               <xsl:copy-of select="$topic"/>
               <xsl:text/>
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="resourceIdentifier"
                    select="gmd:citation/gmd:CI_Citation/gmd:identifier      and not(gmd:citation/gmd:CI_Citation/gmd:identifier[*/gmd:code/@gco:nilReason='missing'])"/>
      <xsl:variable name="resourceIdentifier_code"
                    select="gmd:citation/gmd:CI_Citation/gmd:identifier/*/gmd:code/*/text()"/>
      <xsl:variable name="resourceIdentifier_codeSpace"
                    select="gmd:citation/gmd:CI_Citation/gmd:identifier/*/gmd:codeSpace/*/text()"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$resourceIdentifier"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$resourceIdentifier">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M38/div"/>
                  <xsl:text/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$resourceIdentifier_code">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$resourceIdentifier_code">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M38/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$resourceIdentifier_code"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--REPORT -->
<xsl:if test="$resourceIdentifier_codeSpace">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$resourceIdentifier_codeSpace">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M38.codespace/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$resourceIdentifier_codeSpace"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M9"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M9"/>
   <xsl:template match="@*|node()" priority="-2" mode="M9">
      <xsl:apply-templates select="*" mode="M9"/>
   </xsl:template>

   <!--PATTERN $loc/strings/serviceIdentification-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/serviceIdentification"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="//srv:SV_ServiceIdentification|    //*[@gco:isoType='srv:SV_ServiceIdentification']"
                 priority="1000"
                 mode="M10">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//srv:SV_ServiceIdentification|    //*[@gco:isoType='srv:SV_ServiceIdentification']"/>
      <xsl:variable name="coupledResourceHref"
                    select="string-join(srv:operatesOn/@xlink:href, ', ')"/>
      <xsl:variable name="coupledResourceUUID" select="string-join(srv:operatesOn/@uuidref, ', ')"/>
      <xsl:variable name="coupledResource"
                    select="../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='service'     and //srv:operatesOn"/>

		    <!--REPORT -->
<xsl:if test="$coupledResource and $coupledResourceHref!=''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$coupledResource and $coupledResourceHref!=''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M51/div"/>
               <xsl:text/>
               <xsl:text/>
               <xsl:copy-of select="$coupledResourceHref"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--REPORT -->
<xsl:if test="$coupledResource and $coupledResourceUUID!=''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$coupledResource and $coupledResourceUUID!=''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M51/div"/>
               <xsl:text/>
               <xsl:text/>
               <xsl:copy-of select="$coupledResourceUUID"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="serviceType" select="srv:serviceType/gco:LocalName"/>
      <xsl:variable name="noServiceType"
                    select="geonet:contains-any-of(srv:serviceType/gco:LocalName,      ('view', 'discovery', 'download', 'transformation', 'invoke', 'other'))"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$noServiceType"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$noServiceType">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				              <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M60/div"/>
                  <xsl:text/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$noServiceType">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$noServiceType">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M60/div"/>
               <xsl:text/>
               <xsl:text/>
               <xsl:copy-of select="$serviceType"/>
               <xsl:text/>
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M10"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M10"/>
   <xsl:template match="@*|node()" priority="-2" mode="M10">
      <xsl:apply-templates select="*" mode="M10"/>
   </xsl:template>

   <!--PATTERN $loc/strings/theme-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/theme"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="//gmd:MD_DataIdentification|    //*[@gco:isoType='gmd:MD_DataIdentification']"
                 priority="1000"
                 mode="M11">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:MD_DataIdentification|    //*[@gco:isoType='gmd:MD_DataIdentification']"/>
      <xsl:variable name="inspire-thesaurus"
                    select="document(concat('file:///', $thesaurusDir, '/external/thesauri/theme/inspire-theme.rdf'))"/>
      <xsl:variable name="inspire-theme" select="$inspire-thesaurus//skos:Concept"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="count($inspire-theme) &gt; 0"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="count($inspire-theme) &gt; 0">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				INSPIRE Theme thesaurus not found. Check installation in codelist/external/thesauri/theme.
				Download thesaurus from https://geonetwork.svn.sourceforge.net/svnroot/geonetwork/utilities/gemet/thesauri/.
			</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="thesaurus_name"
                    select="gmd:descriptiveKeywords/*/gmd:thesaurusName/*/gmd:title/*/text()"/>
      <xsl:variable name="thesaurus_date"
                    select="gmd:descriptiveKeywords/*/gmd:thesaurusName/*/gmd:date/*/gmd:date/*/text()"/>
      <xsl:variable name="thesaurus_dateType"
                    select="gmd:descriptiveKeywords/*/gmd:thesaurusName/*/gmd:date/*/gmd:dateType/*/@codeListValue/text()"/>
      <xsl:variable name="keyword"
                    select="gmd:descriptiveKeywords/*/gmd:keyword/gco:CharacterString"/>
      <xsl:variable name="inspire-theme-found"
                    select="count($inspire-thesaurus//skos:Concept[skos:prefLabel = $keyword])"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$inspire-theme-found &gt; 0"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$inspire-theme-found &gt; 0">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				              <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M40/div"/>
                  <xsl:text/>
			            </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$inspire-theme-found &gt; 0">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$inspire-theme-found &gt; 0">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$inspire-theme-found"/>
               <xsl:text/> 
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M40/div"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--REPORT -->
<xsl:if test="$thesaurus_name">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$thesaurus_name">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>Thesaurus:
				<xsl:text/>
               <xsl:copy-of select="$thesaurus_name"/>
               <xsl:text/>, <xsl:text/>
               <xsl:copy-of select="$thesaurus_date"/>
               <xsl:text/> (<xsl:text/>
               <xsl:copy-of select="$thesaurus_dateType"/>
               <xsl:text/>)
			</svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M11"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M11"/>
   <xsl:template match="@*|node()" priority="-2" mode="M11">
      <xsl:apply-templates select="*" mode="M11"/>
   </xsl:template>

   <!--PATTERN $loc/strings/serviceTaxonomy-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/serviceTaxonomy"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="//srv:SV_ServiceIdentification|//*[@gco:isoType='srv:SV_ServiceIdentification']"
                 priority="1000"
                 mode="M12">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//srv:SV_ServiceIdentification|//*[@gco:isoType='srv:SV_ServiceIdentification']"/>
      <xsl:variable name="inspire-thesaurus"
                    select="document(concat('file:///', $thesaurusDir, '/external/thesauri/theme/inspire-service-taxonomy.rdf'))"/>
      <xsl:variable name="inspire-st" select="$inspire-thesaurus//skos:Concept"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="count($inspire-st) &gt; 0"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="count($inspire-st) &gt; 0">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				INSPIRE service taxonomy thesaurus not found. Check installation in codelist/external/thesauri/theme.
				Download thesaurus from https://geonetwork.svn.sourceforge.net/svnroot/geonetwork/utilities/gemet/thesauri/.
			</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="keyword"
                    select="gmd:descriptiveKeywords/*/gmd:keyword/gco:CharacterString"/>
      <xsl:variable name="inspire-theme-found"
                    select="count($inspire-thesaurus//skos:Concept[skos:prefLabel = $keyword])"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$inspire-theme-found &gt; 0"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$inspire-theme-found &gt; 0">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				              <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M58/div"/>
                  <xsl:text/>
			            </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$inspire-theme-found &gt; 0">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$inspire-theme-found &gt; 0">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$inspire-theme-found"/>
               <xsl:text/> 
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M58/div"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M12"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M12"/>
   <xsl:template match="@*|node()" priority="-2" mode="M12">
      <xsl:apply-templates select="*" mode="M12"/>
   </xsl:template>

   <!--PATTERN $loc/strings/geo-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/geo"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="//gmd:MD_DataIdentification[    ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'series'    or ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'dataset'    or ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = '']    /gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox    |    //*[@gco:isoType='gmd:MD_DataIdentification' and (    ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'series'    or ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'dataset'    or ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = '')]    /gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox    "
                 priority="1001"
                 mode="M13">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:MD_DataIdentification[    ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'series'    or ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'dataset'    or ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = '']    /gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox    |    //*[@gco:isoType='gmd:MD_DataIdentification' and (    ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'series'    or ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'dataset'    or ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = '')]    /gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox    "/>
      <xsl:variable name="west" select="number(gmd:westBoundLongitude/gco:Decimal/text())"/>
      <xsl:variable name="east" select="number(gmd:eastBoundLongitude/gco:Decimal/text())"/>
      <xsl:variable name="north" select="number(gmd:northBoundLatitude/gco:Decimal/text())"/>
      <xsl:variable name="south" select="number(gmd:southBoundLatitude/gco:Decimal/text())"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="(-180.00 &lt;= $west) and ( $west &lt;= 180.00)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="(-180.00 &lt;= $west) and ( $west &lt;= 180.00)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M41.W/div"/>
                  <xsl:text/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="(-180.00 &lt;= $west) and ( $west &lt;= 180.00)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="(-180.00 &lt;= $west) and ( $west &lt;= 180.00)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M41.W/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$west"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="(-180.00 &lt;= $east) and ($east &lt;= 180.00)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="(-180.00 &lt;= $east) and ($east &lt;= 180.00)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M41.E/div"/>
                  <xsl:text/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="(-180.00 &lt;= $east) and ($east &lt;= 180.00)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="(-180.00 &lt;= $east) and ($east &lt;= 180.00)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M41.E/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$east"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="(-90.00 &lt;= $south) and ($south &lt;= $north)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="(-90.00 &lt;= $south) and ($south &lt;= $north)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M41.S/div"/>
                  <xsl:text/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="(-90.00 &lt;= $south) and ($south &lt;= $north)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="(-90.00 &lt;= $south) and ($south &lt;= $north)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M41.S/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$south"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="($south &lt;= $north) and ($north &lt;= 90.00)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="($south &lt;= $north) and ($north &lt;= 90.00)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M41.N/div"/>
                  <xsl:text/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="($south &lt;= $north) and ($north &lt;= 90.00)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="($south &lt;= $north) and ($north &lt;= 90.00)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M41.N/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$north"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M13"/>
   </xsl:template>

	  <!--RULE -->
<xsl:template match="//srv:SV_ServiceIdentification[    ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'service']    /srv:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox"
                 priority="1000"
                 mode="M13">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//srv:SV_ServiceIdentification[    ../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue/normalize-space(.) = 'service']    /srv:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox"/>
      <xsl:variable name="west" select="number(gmd:westBoundLongitude/gco:Decimal/text())"/>
      <xsl:variable name="east" select="number(gmd:eastBoundLongitude/gco:Decimal/text())"/>
      <xsl:variable name="north" select="number(gmd:northBoundLatitude/gco:Decimal/text())"/>
      <xsl:variable name="south" select="number(gmd:southBoundLatitude/gco:Decimal/text())"/>

		    <!--REPORT -->
<xsl:if test="(-180.00 &lt;= $west) and ( $west &lt;= 180.00)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="(-180.00 &lt;= $west) and ( $west &lt;= 180.00)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M41.W/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$west"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--REPORT -->
<xsl:if test="(-180.00 &lt;= $east) and ($east &lt;= 180.00)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="(-180.00 &lt;= $east) and ($east &lt;= 180.00)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M41.E/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$east"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--REPORT -->
<xsl:if test="(-90.00 &lt;= $south) and ($south &lt;= $north)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="(-90.00 &lt;= $south) and ($south &lt;= $north)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M41.S/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$south"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--REPORT -->
<xsl:if test="($south &lt;= $north) and ($north &lt;= 90.00)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="($south &lt;= $north) and ($north &lt;= 90.00)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M41.N/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$north"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M13"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M13"/>
   <xsl:template match="@*|node()" priority="-2" mode="M13">
      <xsl:apply-templates select="*" mode="M13"/>
   </xsl:template>

   <!--PATTERN $loc/strings/temporal-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/temporal"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="//gmd:MD_DataIdentification|    //*[@gco:isoType='gmd:MD_DataIdentification']|    //srv:SV_ServiceIdentification|    //*[@gco:isoType='srv:SV_ServiceIdentification']"
                 priority="1000"
                 mode="M14">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:MD_DataIdentification|    //*[@gco:isoType='gmd:MD_DataIdentification']|    //srv:SV_ServiceIdentification|    //*[@gco:isoType='srv:SV_ServiceIdentification']"/>
      <xsl:variable name="temporalExtentBegin"
                    select="gmd:extent/*/gmd:temporalElement/*/gmd:extent/*/gml:beginPosition/text()"/>
      <xsl:variable name="temporalExtentEnd"
                    select="gmd:extent/*/gmd:temporalElement/*/gmd:extent/*/gml:endPosition/text()"/>
      <xsl:variable name="publicationDate"
                    select="gmd:citation/*/gmd:date[./*/gmd:dateType/*/@codeListValue='publication']/*/gmd:date/*"/>
      <xsl:variable name="creationDate"
                    select="gmd:citation/*/gmd:date[./*/gmd:dateType/*/@codeListValue='creation']/*/gmd:date/*"/>
      <xsl:variable name="no_creationDate"
                    select="count(gmd:citation/*/gmd:date[./*/gmd:dateType/*/@codeListValue='creation'])"/>
      <xsl:variable name="revisionDate"
                    select="gmd:citation/*/gmd:date[./*/gmd:dateType/*/@codeListValue='revision']/*/gmd:date/*"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$no_creationDate &lt;= 1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$no_creationDate &lt;= 1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				              <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M42.creation/div"/>
                  <xsl:text/>
				           </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$publicationDate or $creationDate or $revisionDate or $temporalExtentBegin or $temporalExtentEnd"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$publicationDate or $creationDate or $revisionDate or $temporalExtentBegin or $temporalExtentEnd">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				              <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M42/div"/>
                  <xsl:text/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$temporalExtentBegin">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$temporalExtentBegin">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M42.begin/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$temporalExtentBegin"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--REPORT -->
<xsl:if test="$temporalExtentEnd">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$temporalExtentEnd">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M42.end/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$temporalExtentEnd"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--REPORT -->
<xsl:if test="$publicationDate">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$publicationDate">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M42.publication/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$publicationDate"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--REPORT -->
<xsl:if test="$revisionDate">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$revisionDate">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M42.revision/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$revisionDate"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--REPORT -->
<xsl:if test="$creationDate">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$creationDate">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M42.creation/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$creationDate"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M14"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M14"/>
   <xsl:template match="@*|node()" priority="-2" mode="M14">
      <xsl:apply-templates select="*" mode="M14"/>
   </xsl:template>

   <!--PATTERN $loc/strings/quality-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/quality"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="//gmd:DQ_DataQuality[../../gmd:identificationInfo/gmd:MD_DataIdentification    or ../../gmd:identificationInfo/*/@gco:isoType = 'gmd:MD_DataIdentification']"
                 priority="1001"
                 mode="M15">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:DQ_DataQuality[../../gmd:identificationInfo/gmd:MD_DataIdentification    or ../../gmd:identificationInfo/*/@gco:isoType = 'gmd:MD_DataIdentification']"/>
      <xsl:variable name="lineage"
                    select="not(gmd:lineage/gmd:LI_Lineage/gmd:statement) or (gmd:lineage//gmd:statement/@gco:nilReason)"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not($lineage)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="not($lineage)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.M43/div"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="not($lineage)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="not($lineage)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:copy-of select="$loc/strings/report.M43/div"/>
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M15"/>
   </xsl:template>

	  <!--RULE -->
<xsl:template match="//gmd:MD_DataIdentification/gmd:spatialResolution|//*[@gco:isoType='gmd:MD_DataIdentification']/gmd:spatialResolution"
                 priority="1000"
                 mode="M15">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:MD_DataIdentification/gmd:spatialResolution|//*[@gco:isoType='gmd:MD_DataIdentification']/gmd:spatialResolution"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="*/gmd:equivalentScale or */gmd:distance"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="*/gmd:equivalentScale or */gmd:distance">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:copy-of select="$loc/strings/alert.M56/div"/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="*/gmd:equivalentScale or */gmd:distance">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="*/gmd:equivalentScale or */gmd:distance">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:copy-of select="$loc/strings/report.M56/div"/>
            </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M15"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M15"/>
   <xsl:template match="@*|node()" priority="-2" mode="M15">
      <xsl:apply-templates select="*" mode="M15"/>
   </xsl:template>

   <!--PATTERN $loc/strings/conformity-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/conformity"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="/gmd:MD_Metadata" priority="1001" mode="M16">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="/gmd:MD_Metadata"/>
      <xsl:variable name="degree"
                    select="count(gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:pass)"/>

		    <!--REPORT -->
<xsl:if test="$degree = 0">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$degree = 0">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M44.nonev/div"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M16"/>
   </xsl:template>

	  <!--RULE -->
<xsl:template match="//gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*" priority="1000"
                 mode="M16">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*"/>
      <xsl:variable name="degree" select="gmd:pass/*/text()"/>
      <xsl:variable name="specification_title" select="gmd:specification/*/gmd:title/*/text()"/>
      <xsl:variable name="specification_date"
                    select="gmd:specification/*/gmd:date/*/gmd:date/*/text()"/>
      <xsl:variable name="specification_dateType"
                    select="normalize-space(gmd:specification/*/gmd:date/*/gmd:dateType/*/@codeListValue)"/>

		    <!--REPORT -->
<xsl:if test="$specification_title">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$specification_title">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M44.spec/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$specification_title"/>
               <xsl:text/>, (<xsl:text/>
               <xsl:copy-of select="$specification_date"/>
               <xsl:text/>, <xsl:text/>
               <xsl:copy-of select="$specification_dateType"/>
               <xsl:text/>)
			</svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--REPORT -->
<xsl:if test="$degree">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$degree">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M44.degree/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$degree"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M16"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M16"/>
   <xsl:template match="@*|node()" priority="-2" mode="M16">
      <xsl:apply-templates select="*" mode="M16"/>
   </xsl:template>

   <!--PATTERN $loc/strings/constraints-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/constraints"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="//gmd:MD_DataIdentification|    //*[@gco:isoType='gmd:MD_DataIdentification']|    //srv:SV_ServiceIdentification|    //*[@gco:isoType='srv:SV_ServiceIdentification']"
                 priority="1002"
                 mode="M17">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:MD_DataIdentification|    //*[@gco:isoType='gmd:MD_DataIdentification']|    //srv:SV_ServiceIdentification|    //*[@gco:isoType='srv:SV_ServiceIdentification']"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="count(gmd:resourceConstraints/*) &gt; 0"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="count(gmd:resourceConstraints/*) &gt; 0">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				              <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M45.rc/div"/>
                  <xsl:text/>
			            </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="accessConstraints"
                    select="     count(gmd:resourceConstraints/*/gmd:accessConstraints/gmd:MD_RestrictionCode[@codeListValue='otherRestrictions'])&gt;0      and (     not(gmd:resourceConstraints/*/gmd:otherConstraints)          or gmd:resourceConstraints/*/gmd:otherConstraints[@gco:nilReason='missing']     )"/>
      <xsl:variable name="otherConstraints"
                    select="     gmd:resourceConstraints/*/gmd:otherConstraints and     gmd:resourceConstraints/*/gmd:otherConstraints/gco:CharacterString!='' and      count(gmd:resourceConstraints/*/gmd:accessConstraints/gmd:MD_RestrictionCode[@codeListValue='otherRestrictions'])=0     "/>
      <xsl:variable name="otherConstraintInfo"
                    select="gmd:resourceConstraints/*/gmd:otherConstraints/gco:CharacterString"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not($accessConstraints)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="not($accessConstraints)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				              <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M45.or/div"/>
                  <xsl:text/>
			            </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not($otherConstraints)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="not($otherConstraints)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				              <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M45.or/div"/>
                  <xsl:text/>
			            </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$otherConstraintInfo!='' and not($accessConstraints) and not($otherConstraints)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$otherConstraintInfo!='' and not($accessConstraints) and not($otherConstraints)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M45.or/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$otherConstraintInfo"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M17"/>
   </xsl:template>

	  <!--RULE -->
<xsl:template match="//gmd:MD_DataIdentification/gmd:resourceConstraints/*|    //*[@gco:isoType='gmd:MD_DataIdentification']/gmd:resourceConstraints/*|    //srv:SV_ServiceIdentification/gmd:resourceConstraints/*|    //*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:resourceConstraints/*"
                 priority="1001"
                 mode="M17">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:MD_DataIdentification/gmd:resourceConstraints/*|    //*[@gco:isoType='gmd:MD_DataIdentification']/gmd:resourceConstraints/*|    //srv:SV_ServiceIdentification/gmd:resourceConstraints/*|    //*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:resourceConstraints/*"/>
      <xsl:variable name="accessConstraints"
                    select="string-join(gmd:accessConstraints/*/@codeListValue, ', ')"/>
      <xsl:variable name="classification"
                    select="string-join(gmd:classification/*/@codeListValue, ', ')"/>
      <xsl:variable name="otherConstraints"
                    select="gmd:otherConstraints/gco:CharacterString/text()"/>

		    <!--REPORT -->
<xsl:if test="$accessConstraints!=''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$accessConstraints!=''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M45.ac/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$accessConstraints"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>

		    <!--REPORT -->
<xsl:if test="$classification!=''">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$classification!=''">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M45.class/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$classification"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M17"/>
   </xsl:template>

	  <!--RULE -->
<xsl:template match="//gmd:MD_DataIdentification|    //*[@gco:isoType='gmd:MD_DataIdentification']|    //srv:SV_ServiceIdentification|    //*[@gco:isoType='srv:SV_ServiceIdentification']"
                 priority="1000"
                 mode="M17">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:MD_DataIdentification|    //*[@gco:isoType='gmd:MD_DataIdentification']|    //srv:SV_ServiceIdentification|    //*[@gco:isoType='srv:SV_ServiceIdentification']"/>
      <xsl:variable name="useLimitation"
                    select="gmd:resourceConstraints/*/gmd:useLimitation/*/text()"/>
      <xsl:variable name="useLimitation_count"
                    select="count(gmd:resourceConstraints/*/gmd:useLimitation/*/text())"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$useLimitation_count"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$useLimitation_count">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				              <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M45.ul/div"/>
                  <xsl:text/>
			            </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$useLimitation_count">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$useLimitation_count">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M45.ul/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$useLimitation"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M17"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M17"/>
   <xsl:template match="@*|node()" priority="-2" mode="M17">
      <xsl:apply-templates select="*" mode="M17"/>
   </xsl:template>

   <!--PATTERN $loc/strings/org-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/org"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="//gmd:identificationInfo" priority="1001" mode="M18">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="//gmd:identificationInfo"/>
      <xsl:variable name="missing" select="not(*/gmd:pointOfContact)"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not($missing)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="not($missing)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M47/div"/>
                  <xsl:text/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="not($missing)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="not($missing)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M47/div"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M18"/>
   </xsl:template>

	  <!--RULE -->
<xsl:template match="//gmd:identificationInfo/*/gmd:pointOfContact    |//*[@gco:isoType='gmd:MD_DataIdentification']/gmd:pointOfContact    |//*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact"
                 priority="1000"
                 mode="M18">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:identificationInfo/*/gmd:pointOfContact    |//*[@gco:isoType='gmd:MD_DataIdentification']/gmd:pointOfContact    |//*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact"/>
      <xsl:variable name="missing"
                    select="not(*/gmd:organisationName)      or (*/gmd:organisationName/@gco:nilReason)      or not(*/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress)      or (*/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/@gco:nilReason)"/>
      <xsl:variable name="organisationName" select="*/gmd:organisationName/*/text()"/>
      <xsl:variable name="role" select="normalize-space(*/gmd:role/*/@codeListValue)"/>
      <xsl:variable name="emptyRole" select="$role=''"/>
      <xsl:variable name="emailAddress"
                    select="*/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/*/text()"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not($missing)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="not($missing)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M47.info/div"/>
                  <xsl:text/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not($emptyRole)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="not($emptyRole)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M48.role/div"/>
                  <xsl:text/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="not($missing)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="not($missing)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M47.info/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$organisationName"/>
               <xsl:text/> 
				(<xsl:text/>
               <xsl:copy-of select="$role"/>
               <xsl:text/>)
			</svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M18"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M18"/>
   <xsl:template match="@*|node()" priority="-2" mode="M18">
      <xsl:apply-templates select="*" mode="M18"/>
   </xsl:template>

   <!--PATTERN $loc/strings/metadata-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
      <xsl:copy-of select="$loc/strings/metadata"/>
   </svrl:text>

	  <!--RULE -->
<xsl:template match="//gmd:MD_Metadata" priority="1001" mode="M19">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="//gmd:MD_Metadata"/>
      <xsl:variable name="dateStamp" select="gmd:dateStamp/*/text()"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$dateStamp"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$dateStamp">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				              <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M50/div"/>
                  <xsl:text/>				
			            </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$dateStamp">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$dateStamp">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M50/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$dateStamp"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="language"
                    select="gmd:language/gco:CharacterString|gmd:language/gmd:LanguageCode/@codeListValue"/>
      <xsl:variable name="language_present"
                    select="geonet:contains-any-of($language,      ('eng', 'fre', 'ger', 'spa', 'dut', 'ita', 'cze', 'lav', 'dan', 'lit', 'mlt',      'pol', 'est', 'por', 'fin', 'rum', 'slo', 'slv', 'gre', 'bul',      'hun', 'swe', 'gle'))"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="$language_present"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="$language_present">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
				              <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M49/div"/>
                  <xsl:text/>
			            </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="$language_present">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="$language_present">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
				           <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M49/div"/>
               <xsl:text/>				
				           <xsl:text/>
               <xsl:copy-of select="normalize-space($language)"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:variable name="missing" select="not(gmd:contact)"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not($missing)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="not($missing)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M48/div"/>
                  <xsl:text/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="not($missing)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="not($missing)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M48/div"/>
               <xsl:text/>
			         </svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M19"/>
   </xsl:template>

	  <!--RULE -->
<xsl:template match="//gmd:MD_Metadata/gmd:contact" priority="1000" mode="M19">
      <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:MD_Metadata/gmd:contact"/>
      <xsl:variable name="missing"
                    select="not(gmd:CI_ResponsibleParty/gmd:organisationName)      or (gmd:CI_ResponsibleParty/gmd:organisationName/@gco:nilReason)      or not(gmd:CI_ResponsibleParty/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress)      or (gmd:CI_ResponsibleParty/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/@gco:nilReason)"/>
      <xsl:variable name="organisationName"
                    select="gmd:CI_ResponsibleParty/gmd:organisationName/*/text()"/>
      <xsl:variable name="role"
                    select="normalize-space(gmd:CI_ResponsibleParty/gmd:role/*/@codeListValue)"/>
      <xsl:variable name="emptyRole" select="$role=''"/>
      <xsl:variable name="emailAddress"
                    select="gmd:CI_ResponsibleParty/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/*/text()"/>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not($emptyRole)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="not($emptyRole)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M48.role/div"/>
                  <xsl:text/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--ASSERT -->
<xsl:choose>
         <xsl:when test="not($missing)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                test="not($missing)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-select-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:text/>
                  <xsl:copy-of select="$loc/strings/alert.M48.info/div"/>
                  <xsl:text/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>

		    <!--REPORT -->
<xsl:if test="not($missing)">
         <svrl:successful-report xmlns:svrl="http://purl.oclc.org/dsdl/svrl" ref="#_{geonet:element/@ref}"
                                 test="not($missing)">
            <xsl:attribute name="location">
               <xsl:apply-templates select="." mode="schematron-select-full-path"/>
            </xsl:attribute>
            <svrl:text>
               <xsl:text/>
               <xsl:copy-of select="$loc/strings/report.M48.info/div"/>
               <xsl:text/>
				           <xsl:text/>
               <xsl:copy-of select="$organisationName"/>
               <xsl:text/> 
				(<xsl:text/>
               <xsl:copy-of select="$role"/>
               <xsl:text/>)
			</svrl:text>
         </svrl:successful-report>
      </xsl:if>
      <xsl:apply-templates select="*" mode="M19"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M19"/>
   <xsl:template match="@*|node()" priority="-2" mode="M19">
      <xsl:apply-templates select="*" mode="M19"/>
   </xsl:template>
</xsl:stylesheet>