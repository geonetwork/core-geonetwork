<?xml version="1.0" ?>
<!-- Report Generator for the Schematron XML Schema Language.
	http://www.ascc.net/xml/resource/schematron/schematron.html
   
 Copyright (c) 2000,2001 David Calisle, Oliver Becker,
	 Rick Jelliffe and Academia Sinica Computing Center, Taiwan

 This software is provided 'as-is', without any express or implied warranty. 
 In no event will the authors be held liable for any damages arising from 
 the use of this software.

 Permission is granted to anyone to use this software for any purpose, 
 including commercial applications, and to alter it and redistribute it freely,
 subject to the following restrictions:

 1. The origin of this software must not be misrepresented; you must not claim
 that you wrote the original software. If you use this software in a product, 
 an acknowledgment in the product documentation would be appreciated but is 
 not required.

 2. Altered source versions must be plainly marked as such, and must not be 
 misrepresented as being the original software.

 3. This notice may not be removed or altered from any source distribution.

    1999-10-25  Version for David Carlisle's schematron-report error browser
    1999-11-5   Beta for 1.2 DTD
    1999-12-26  Add code for namespace: thanks DC
    1999-12-28  Version fix: thanks Uche Ogbuji
    2000-03-27  Generate version: thanks Oliver Becker
    2000-10-20  Fix '/' in do-all-patterns: thanks Uche Ogbuji
    2001-02-15  Port to 1.5 code
    2001-03-15  Diagnose test thanks Eddie Robertsson

ALTERED by Simon Pigot to remove escaping of quotes necessary for javascript
   alert display - see translate below

-->

<!-- Schematron report -->

<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:axsl="http://www.w3.org/1999/XSL/TransformAlias">

<xsl:import href="skeleton1-5.xsl"/>
<xsl:param name="diagnose">yes</xsl:param>     

<xsl:template name="process-prolog">
   <axsl:output method="html" />

   <axsl:param name="lang"/>
   
   <axsl:variable name="loc" select="document(concat('loc/', $lang, '/schematron.xml'))"/>
   
</xsl:template>

<xsl:template name="process-root">
   <xsl:param name="title" />
   <xsl:param name="icon" />
   <xsl:param name="contents" />
   <html>
      <head>
            <title></title>
            <link rel="stylesheet" type="text/css" href="../../geonetwork.css"/>
      </head>
      <body>
       	   <table width="100%" height="100%">
                <tr class="banner">
                    <td class="banner">
                        <img alt="GeoNetwork opensource" align="top" src="../../images/header-left.jpg"/>
                    </td>
                    <td align="right" class="banner">
                        <img alt="World picture" align="top" src="../../images/header-right.gif"/>
                    </td>
                </tr>
                <tr height="100%">
                    <td class="content" colspan="3">
                     <h1>
                       <axsl:value-of select="$loc/strings/title" />
                     </h1>

                     <h2>
                       <axsl:attribute name="title">
                         <axsl:value-of
                           select="$loc/strings/report.alt" />
                       </axsl:attribute>
                       <axsl:value-of select="$loc/strings/report" />
                     </h2>
                     <xsl:copy-of select="$contents" />

                    </td>
                </tr>
            </table>
      </body>
   </html>
</xsl:template>

<xsl:template name="process-p">
   <xsl:param name="icon" />
<p><xsl:if test="$icon"><img src="{$icon}" /> </xsl:if
><xsl:apply-templates mode="text"/></p>
</xsl:template>

<xsl:template name="process-pattern">
   <xsl:param name="icon" />
   <xsl:param name="name" />
   <xsl:param name="see" />
   <xsl:choose>
      <xsl:when test="$see">
         <a href="{$see}" target="SRDOCO" 
            title="Link to User Documentation:">
            <h3 class="linked">
                <xsl:element name="xsl:value-of">
                  <xsl:attribute name="select">
                      <xsl:value-of select="$name" />
                  </xsl:attribute>
                </xsl:element>
            </h3>
         </a>
      </xsl:when>
      <xsl:otherwise>
         <h3>
            <xsl:element name="xsl:value-of">
              <xsl:attribute name="select">
                  <xsl:value-of select="$name" />
              </xsl:attribute>
            </xsl:element>
        </h3>
      </xsl:otherwise>
   </xsl:choose>
   <xsl:if test="$icon"><img src="{$icon}" /> </xsl:if>
</xsl:template>

<!-- use default rule for process-name: output name -->

<xsl:template name="process-assert">
   <xsl:param name="role" />
   <xsl:param name="id" />
   <xsl:param name="test" />
   <xsl:param name="icon" />
   <xsl:param name="subject" />
   <xsl:param name="diagnostics" />
   <li>
   <xsl:if test="$icon"><img src="{$icon}" /> </xsl:if>
      <a href="schematron-out.html#{{generate-id(.)}}" target="out"
         title="Link to where this pattern was expected">
		 <xsl:element name="xsl:value-of">
            <xsl:attribute name="select">
                  <xsl:apply-templates mode="text"/>
            </xsl:attribute>
          </xsl:element>
         <xsl:if test="$diagnose = 'yes'">
            <b>
                <xsl:call-template name="diagnosticsSplit">
                    <xsl:with-param name="str" select="$diagnostics" />
                </xsl:call-template>
            </b>
         </xsl:if>
      </a>
   </li>                    
</xsl:template>

<xsl:template name="process-report">
   <xsl:param name="role" />
   <xsl:param name="test" />
   <xsl:param name="icon" />
   <xsl:param name="id" />
   <xsl:param name="subject" />
   <xsl:param name="diagnostics" />
   <li>
   <xsl:if test="$icon"><img src="{$icon}" /> </xsl:if>
      <a href="schematron-out.html#{{generate-id(.)}}" target="out"
         title="Link to where this pattern was found">
 	     <xsl:element name="xsl:value-of">
            <xsl:attribute name="select">
                  <xsl:apply-templates mode="text"/>
            </xsl:attribute>
          </xsl:element>
         <b>
            <xsl:call-template name="diagnosticsSplit">
               <xsl:with-param name="str" select="$diagnostics" />
            </xsl:call-template>
         </b>
      </a>
   </li>
</xsl:template>


</xsl:stylesheet>
