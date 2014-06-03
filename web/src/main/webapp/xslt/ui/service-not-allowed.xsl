<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="../base-layout.xsl"/>

  <xsl:template mode="content" match="/">
    <div class="jumbotron">
      <div class="container">
        <h1 class="text-danger">
          <xsl:value-of select="$i18n/serviceNotAllowedTitle"/>
        </h1>
        <p class="text-danger">
            <xsl:variable name="referer" select="if (normalize-space(/root/request/referer) = 'UNKNOWN')
                        then '' else /root/request/referer"/>

          <xsl:value-of select="replace($i18n/serviceNotAllowed, '\{1\}', concat('&quot;', $referer, '&quot;'))" />
          <xsl:copy-of select="$i18n/linkToHome"/>
        </p>
      </div>
    </div>
  </xsl:template>
</xsl:stylesheet>