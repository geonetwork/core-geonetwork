<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:geonet="http://www.fao.org/geonetwork">

  <xsl:include href="main.xsl"/>

  <!--
	page content
	-->
  <xsl:template name="content">
    <xsl:call-template name="formLayout">
      <xsl:with-param name="title" select="/root/gui/strings/metadataInsertResults"/>
      <xsl:with-param name="content">

        <table width="100%">
          <tr>
            <td align="left">
              <xsl:choose>
                <xsl:when test="/root/response/id">
                  <xsl:value-of
                    select="concat(/root/gui/strings/metadataAdded,' ',/root/response/id)"/>
                  <br/>
                  <br/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:variable name="errors" select="count(/root/response/exceptions/exception)"/>
                  <xsl:value-of
                    select="concat(/root/gui/strings/metadataRecordsProcessed,' ',/root/response/records)"/>
                  <br/>
                  <xsl:value-of
                    select="concat(/root/gui/strings/metadataRecordsAdded,' ', (/root/response/records - $errors))"/>
                  <br/>
                  <xsl:value-of select="concat($errors, ' ', /root/gui/strings/errors)"/>
                  <br/>
                  <ul>
                    <xsl:for-each select="/root/response/exceptions/exception">
                      <li>
                        <xsl:value-of select="."/>
                      </li>
                    </xsl:for-each>
                  </ul>
                </xsl:otherwise>
              </xsl:choose>
            </td>
          </tr>
        </table>
      </xsl:with-param>

      <xsl:with-param name="buttons">
        <xsl:if test="/root/response/id">
          
          <xsl:choose>
            <xsl:when test="/root/gui/config/client/@widget='true'">
              <button class="content" onclick="goBack()" id="back"><xsl:value-of
                select="/root/gui/strings/back"/></button> &#160; <button class="content"
                  onclick="load('{/root/gui/config/client/@url}?hl={/root/gui/language}#uuid={/root/response/uuid}')"
                  ><xsl:value-of select="/root/gui/strings/show"/></button> &#160; <button
                    class="content"
                    onclick="load('{/root/gui/config/client/@url}?hl={/root/gui/language}#edit={/root/response/id}')"
                    ><xsl:value-of select="/root/gui/strings/edit"/></button>
            </xsl:when>
            <xsl:otherwise>
              <button class="content" onclick="goBack()" id="back"><xsl:value-of
                select="/root/gui/strings/back"/></button> &#160; <button class="content"
                  onclick="load('{/root/gui/locService}/metadata.show?id={/root/response/id}')"
                  ><xsl:value-of select="/root/gui/strings/show"/></button> &#160; <button
                    class="content"
                    onclick="load('{/root/gui/locService}/metadata.edit?id={/root/response/id}')"
                    ><xsl:value-of select="/root/gui/strings/edit"/></button>
            </xsl:otherwise>
          </xsl:choose>
          
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>
