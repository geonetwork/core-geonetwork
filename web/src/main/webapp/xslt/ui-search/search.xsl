<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="../base-layout.xsl"/>

  <xsl:template mode="content" match="/">
    <div data-ng-include="'{$uiResourcesPath}views/{$searchView}/templates/index.html'" class="gn-full">
    </div>
  </xsl:template>
</xsl:stylesheet>
