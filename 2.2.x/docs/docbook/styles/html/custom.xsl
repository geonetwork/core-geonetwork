<?xml version="1.0" encoding="UTF-8"?>

<!--
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.0'>

  <xsl:param name="graphicsize.extension" select="1"/>
  <xsl:param name="tablecolumns.extension" select="0"/>
  <xsl:param name="use.extensions" select="1"/>
  
<!-- Activate Graphics -->
  <xsl:param name="admon.graphics" select="1"/>
  <xsl:param name="admon.graphics.path">images/</xsl:param>
  <xsl:param name="admon.graphics.extension">.png</xsl:param>
  <xsl:param name="callout.graphics" select="1" />
  <xsl:param name="callout.graphics.path">images/callouts/</xsl:param>
  <xsl:param name="callout.graphics.extension">.png</xsl:param>

  <xsl:param name="table.borders.with.css" select="1"/>
  <xsl:param name="html.stylesheet">css/stylesheet.css</xsl:param>
  <xsl:param name="html.stylesheet.type">text/css</xsl:param>         
  <xsl:param name="generate.toc">book toc,title,refentry</xsl:param>  
  
  <!-- Set a target window for URLs -->
  <xsl:param name="ulink.target">_blank</xsl:param>
  
  <!-- Separate title page from index -->
  <xsl:param name="chunk.tocs.and.lots" select="0"/>

  <xsl:template name="user.footer.content">
    <hr />
    <div><p style="text-align: center;">
      Other documents:<xsl:text>    </xsl:text>
      <a href="Manual.pdf" title="Manual in pdf format" target="_blank">The complete manual in pdf format</a><xsl:text>  |  </xsl:text>
      <a href="license.html" title="GPL license" target="_blank">License</a><xsl:text>  |  </xsl:text>
      <a href="readme.html" title="Readme" target="_blank">Readme</a><xsl:text>  |  </xsl:text>
      <a href="changes.txt" title="Changes" target="_blank">Changes</a></p>
    </div>
  </xsl:template>

  <xsl:param name="admonition.title.properties">text-align: left</xsl:param>

  <!-- Label Chapters and Sections (numbering) -->
  <xsl:param name="chapter.autolabel" select="1"/>
  <xsl:param name="section.autolabel" select="1"/>
  <xsl:param name="section.autolabel.max.depth" select="1"/>

  <xsl:param name="section.label.includes.component.label" select="1"/>
  <xsl:param name="table.footnote.number.format" select="'1'"/>

<!-- Remove "Chapter" from the Chapter titles... -->
  <xsl:param name="local.l10n.xml" select="document('')"/>
  <l:i18n xmlns:l="http://docbook.sourceforge.net/xmlns/l10n/1.0">
    <l:l10n language="en">
      <l:context name="title-numbered">
        <l:template name="chapter" text="%n.&#160;%t"/>
        <l:template name="section" text="%n&#160;%t"/>
      </l:context>
    </l:l10n>
  </l:i18n>      
</xsl:stylesheet>
