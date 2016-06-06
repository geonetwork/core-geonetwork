<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method='html' encoding='UTF-8' indent='yes'/>

  <xsl:template match="/">
    <xsl:variable name="in" select="/root/request/keyword"/>
    <xsl:variable name="mode" select="/root/request/mode"/>

    <xsl:choose>
      <xsl:when test="$mode = 'selector'">
        <img align="right" src="{/root/gui/url}/images/del.gif"
             onclick="$('keywordSelectorFrame').style.display = 'none'"/>
        <br/>
        <xsl:if test="count(/root/response/summary/keywords/keyword)=0">0 <xsl:value-of
          select="/root/gui/strings/keyword"/>.
        </xsl:if>

        <xsl:for-each select="/root/response/summary/keywords/keyword">
          <input type="checkbox" id="keyword{position()}" name="" value=""
                 onclick="keywordCheck(this.value, this.checked);">
            <xsl:attribute name="value">
              <xsl:value-of select="@name"/>
            </xsl:attribute>

            <xsl:if test="contains($in, @name)=1">
              <xsl:attribute name="checked">checked</xsl:attribute>
            </xsl:if>
          </input>
          <label for="keyword{position()}">
            <xsl:value-of select="@name"/>
            <xsl:text> </xsl:text>
            (<xsl:value-of select="@count"/>
            <xsl:text> </xsl:text>
            <xsl:choose>
              <xsl:when test="@count &gt; 2">
                <xsl:value-of select="/root/gui/strings/res"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="/root/gui/strings/ress"/>
              </xsl:otherwise>
            </xsl:choose>
            )
          </label>
          <br/>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <ul>
          <xsl:for-each select="/root/response/summary/keywords/keyword[contains(@name,$in)=1]">
            <li>
              <xsl:value-of select="@name"/>
            </li>
          </xsl:for-each>
        </ul>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


</xsl:stylesheet>
