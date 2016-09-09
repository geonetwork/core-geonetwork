<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:include href="modal.xsl"/>

  <!--
    page content
    -->
  <xsl:template name="content">
    <xsl:call-template name="formLayout">
      <xsl:with-param name="title" select="/root/gui/strings/download"/>
      <xsl:with-param name="content">
        <h2>
          <xsl:value-of select="/root/gui/strings/messageDownload"/>
        </h2>
        <p/>
        <ul>
          <xsl:for-each select="/root/response/fname">
            <li>
              <xsl:value-of select="."/>
            </li>
          </xsl:for-each>
        </ul>
        <xsl:value-of select="/root/gui/strings/asAZip"/>
        <p/>
        <!-- now display the more info with email -->
        <xsl:if test="normalize-space(/root/gui/env/feedback/email)!=''">
          <xsl:copy-of select="/root/gui/strings/moreinfo"/>
          <a href="mailto:{/root/gui/env/feedback/email}">
            <xsl:value-of select="/root/gui/env/feedback/email"/>
          </a>
        </xsl:if>
        <!-- forward parameters to request using a form with hidden fields -->
        <form name="finalAccept" action="{/root/gui/locService}/resources.get.archive">
          <input type="hidden" name="id" value="{/root/response/id}"/>
          <xsl:for-each select="/root/response/fname">
            <input type="hidden" name="fname" value="{.}"/>
          </xsl:for-each>
          <input type="hidden" name="access" value="private"/>
          <input type="hidden" name="name" value="{/root/response/name}"/>
          <input type="hidden" name="org" value="{/root/response/org}"/>
          <input type="hidden" name="email" value="{/root/response/email}"/>
          <input type="hidden" name="comments" value="{/root/response/comments}"/>
        </form>
      </xsl:with-param>
      <xsl:with-param name="buttons">
        <p/>
        <center>
          <button class="content" onclick="goSubmit('finalAccept')">
            <xsl:value-of select="/root/gui/strings/download"/>
          </button>
        </center>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>
