<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:include href="modal.xsl"/>

  <!--
  page content
  -->
  <xsl:template name="content">
    <xsl:call-template name="formLayout">
      <xsl:with-param name="content">
        <xsl:choose>
          <xsl:when test="/root/response/result='errorEmailToAddressFailed'">
            <p class="error">
              <xsl:copy-of select="/root/gui/info/errorEmailToAddressFailed"/>
            </p>
          </xsl:when>
          <xsl:when test="/root/response/result='errorProfileRequestFailed'">
            <p class="error">
              <xsl:copy-of select="/root/gui/info/errorProfileRequestFailed"/>
            </p>
          </xsl:when>
          <xsl:when test="/root/response/result='errorEmailAddressAlreadyRegistered'">
            <p class="error">
              <xsl:copy-of select="/root/gui/info/errorEmailAddressAlreadyRegistered"/>
            </p>
          </xsl:when>
          <xsl:otherwise>
            <p>
              <xsl:value-of select="/root/gui/info/message"/>
            </p>
          </xsl:otherwise>
        </xsl:choose>
        <p>
          <xsl:value-of select="/root/gui/strings/registrationDetails"/>
          <b>
            <xsl:value-of
              select="concat(/root/response/@name,' ',/root/response/@surname,' (',/root/response/@email,')')"/>
          </b>
        </p>
      </xsl:with-param>
      <xsl:with-param name="buttons">
        <xsl:if test="not(starts-with(string(/root/response/result),'error'))">
          <xsl:if test="/root/request/simplemetadata">
            <div align="center">
              <button class="content"
                      onclick="doBannerButton('{/root/gui/locService}/simplemetadata.registration.submit?username={/root/response/@username}&amp;email={/root/response/@email}','{/root/gui/strings/simpleMetadataTitle}',800,600)">
                <xsl:value-of select="/root/gui/strings/submitSimpleRegistration"/>
              </button>
            </div>
          </xsl:if>
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>
