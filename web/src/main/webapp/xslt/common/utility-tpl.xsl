<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">

  <xsl:template name="replaceString">
    <xsl:param name="expr"/>
    <xsl:param name="pattern"/>
    <xsl:param name="replacement"/>

    <xsl:variable name="first" select="substring-before($expr,$pattern)"/>
    <xsl:choose>
      <xsl:when test="$first or starts-with($expr, $pattern)">
        <xsl:value-of select="$first"/>
        <xsl:value-of select="$replacement"/>
        <xsl:call-template name="replaceString">
          <xsl:with-param name="expr" select="substring-after($expr,$pattern)"/>
          <xsl:with-param name="pattern" select="$pattern"/>
          <xsl:with-param name="replacement" select="$replacement"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$expr"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!--
        Translates CR-LF sequences into HTML newlines <p/>
        and process current line and next line to add hyperlinks.

        Add new line before hyperlinks because normalize-space
        remove new line information.

    -->
  <xsl:template name="addLineBreaksAndHyperlinks">
    <xsl:param name="txt"/>

    <xsl:choose>
      <xsl:when test="$txt instance of node() and $txt/div">
        <xsl:for-each select="$txt/div">
          <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:call-template name="addLineBreaksAndHyperlinksInternal">
              <xsl:with-param name="txt" select="."/>
            </xsl:call-template>
          </xsl:copy>
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="$txt != ''">
        <xsl:call-template name="addLineBreaksAndHyperlinksInternal">
          <xsl:with-param name="txt" select="$txt"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="addLineBreaksAndHyperlinksInternal">
    <xsl:param name="txt" select="string(.)" />

    <xsl:variable name="txtWithBr">
      <xsl:analyze-string select="$txt"
                          regex="[\r\n]{{2}}">
        <!-- Code the breakline with the char ◿, to be matched later (regular expression with ^ seem doesn't match multiple chars)
             so can't use string that is unlikely to happen like BRBRBR for this and replaced later by <br/>.

             The use of the char ◿ is arbitrary, selected as a very unlikely char to happen in the metadata.
        -->
        <xsl:matching-substring>
          ◿<xsl:value-of select="."/>
        </xsl:matching-substring>
        <xsl:non-matching-substring>
          <xsl:value-of select="."/>
        </xsl:non-matching-substring>
      </xsl:analyze-string>
    </xsl:variable>

    <!-- See previous comment about the usage of the char ◿ -->
    <xsl:analyze-string select="$txtWithBr"
                        regex="[^\n\r◿]+">
      <!-- Surround text without breaklines inside a p element -->
      <xsl:matching-substring>
        <xsl:if test="string(normalize-space(.))">
        <p>
          <xsl:call-template name="hyperlink">
            <xsl:with-param name="string" select="." />
          </xsl:call-template>
        </p>
        </xsl:if>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <xsl:if test="string(normalize-space(.))">
          <xsl:call-template name="hyperlink">
            <xsl:with-param name="string" select="." />
          </xsl:call-template>
        </xsl:if>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:template>


  <xsl:template name="hyperlink">
    <xsl:param name="string" select="." />
    <xsl:analyze-string select="$string"
                        regex="(http|https|ftp)://[^\s()&gt;&lt;]+[^\s`!()\[\]&amp;#123;&amp;#125;;:'&apos;&quot;.,&gt;&lt;?«»“”‘’]">
      <xsl:matching-substring>
        <a href="{.}">
          <xsl:value-of select="." />
        </a>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <xsl:call-template name="hyperlink-mailaddress">
          <xsl:with-param name="string" select="." />
        </xsl:call-template>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:template>

  <xsl:template name="hyperlink-mailaddress">
    <xsl:param name="string" select="." />
    <xsl:analyze-string select="$string"
                        regex="([A-Za-z0-9._%+-]{{1,64}}@([A-Za-z0-9-]{{1,63}}\.)+[A-Za-z]{{2,63}})">
      <xsl:matching-substring>
        <a href="mailto:{.}">
          <xsl:value-of select="." />
        </a>
      </xsl:matching-substring>
      <xsl:non-matching-substring>

        <!-- See previous comment about the usage of the char ◿ -->
        <xsl:analyze-string select="."
                            regex="◿">
          <xsl:matching-substring>
            <br/>
            <xsl:value-of select="replace(., '◿', '')"/>
          </xsl:matching-substring>
          <xsl:non-matching-substring>
            <xsl:value-of select="."/>
          </xsl:non-matching-substring>
        </xsl:analyze-string>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:template>
</xsl:stylesheet>
