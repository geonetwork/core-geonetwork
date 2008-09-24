<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:sch="http://www.ascc.net/xml/schematron"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                version="1.0"
                geonet:dummy-for-xmlns=""
                xlink:dummy-for-xmlns="">
   <xsl:output method="html"/>
   <xsl:template match="*|@*" mode="schematron-get-full-path">
      <xsl:apply-templates select="parent::*" mode="schematron-get-full-path"/>
      <xsl:text>/</xsl:text>
      <xsl:if test="count(. | ../@*) = count(../@*)">@</xsl:if>
      <xsl:value-of select="name()"/>
      <xsl:text>[</xsl:text>
      <xsl:value-of select="1+count(preceding-sibling::*[name()=name(current())])"/>
      <xsl:text>]</xsl:text>
   </xsl:template>
   <xsl:template match="/">
      <html>
      	<head>
			<link type="image/x-icon" rel="shortcut icon" href="/geonetwork/favicon.ico" />
			<link type="image/x-icon" rel="icon" href="/geonetwork/favicon.ico" />
			<link href="/geonetwork/geonetwork.css" type="text/css" rel="stylesheet" />
		</head>
		<body>
         <h2 title="Schematron contact-information is at the end of                   this page">
            <font color="#FF0080">Schematron</font> Report
      </h2>
         <h1 title=" ">No schematron rules defined</h1>
         <div class="errors">
            <ul/>
         </div>
         <hr color="#FF0080"/>
         <p>
            <font size="2">Schematron Report by David Carlisle.
      <a href="http://www.ascc.net/xml/resource/schematron/schematron.html"
                  title="Link to the home page of the Schematron,                  a tree-pattern schema language">
                  <font color="#FF0080">The Schematron</font>
               </a> by
      <a href="mailto:ricko@gate.sinica.edu.tw"
                  title="Email to Rick Jelliffe (pronounced RIK JELIF)">Rick Jelliffe</a>,
      <a href="http://www.sinica.edu.tw" title="Link to home page of Academia Sinica">Academia Sinica Computing Centre</a>.
      </font>
         </p>
         </body>
      </html>
   </xsl:template>
   <xsl:template match="text()" priority="-1"/>
</xsl:stylesheet>