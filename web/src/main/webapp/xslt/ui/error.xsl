<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:exslt="http://exslt.org/common">


  <xsl:include href="../base-layout.xsl"/>

  <xsl:template mode="content" match="/">
    <div class="jumbotron">
      <div class="container">
        <h1 class="text-danger">
          <xsl:choose>
            <xsl:when test="/root/gui/startupError/error">
              <xsl:value-of select="$i18n/initializationFailure"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$i18n/initializationSuccess"/>
            </xsl:otherwise>
          </xsl:choose>
        </h1>
        <p>
          <ul class="text-danger">
            <xsl:apply-templates mode="showError" select="/root/gui/startupError/error/Error"/>
            <xsl:apply-templates mode="showError"
              select="/root/gui/startupError/error/*[name()!='Error' and name()!='Stack']"/>
            <xsl:apply-templates mode="showError" select="/root/gui/startupError/error/Stack"/>
          </ul>
        </p>
      </div>
    </div>
  </xsl:template>

  <xsl:template mode="showError" match="*">
    <li>
      <xsl:value-of select="name(.)"/>
      <pre>
        <xsl:value-of select="string(.)"/>
      </pre>
    </li>
  </xsl:template>

</xsl:stylesheet>
