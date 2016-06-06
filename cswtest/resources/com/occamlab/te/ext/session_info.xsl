<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:txsl="http://www.w3.org/1999/XSL/Transform/target"
  xmlns:ctl="http://www.occamlab.com/ctl"
  xmlns:te="java:com.occamlab.te.TECore"
  version="2.0">

  <xsl:strip-space elements="*"/>
  <xsl:output indent="yes"/>
  <xsl:namespace-alias stylesheet-prefix="txsl" result-prefix="xsl"/>

  <xsl:template match="ctl:get-session-info">
    <ctl:SessionInfo>
      <ctl:SessionId>
        <txsl:value-of select="te:getSessionId()"/>
      </ctl:SessionId>
      <ctl:SessionDir>
        <txsl:value-of select="te:getSessionDir()"/>
      </ctl:SessionDir>
    </ctl:SessionInfo>
  </xsl:template>

</xsl:transform>

