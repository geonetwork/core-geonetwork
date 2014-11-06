<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:j2e="http://java.sun.com/xml/ns/javaee"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output encoding="UTF-8" indent="yes"
    media-type="text/xml"/>

    <xsl:param name="nodeId"/>


    <!-- Add the servlet mapping to the new node -->
    <xsl:template match="/j2e:web-app" priority="2">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:message>Add servlet mappinf for <xsl:value-of select="$nodeId"/></xsl:message>
            <xsl:apply-templates select="*"/>

            <xsl:if test="count(//j2e:servlet-mapping/j2e:url-pattern[text() = concat('/', $nodeId, '/*')]) = 0">
              <j2e:servlet-mapping>
                <j2e:servlet-name>gn-servlet</j2e:servlet-name>
                <j2e:url-pattern>/<xsl:value-of select="$nodeId"/>/*</j2e:url-pattern>
              </j2e:servlet-mapping>
            </xsl:if>
        </xsl:copy>
    </xsl:template>


    <xsl:template match="@*|*|text()|comment()">
        <xsl:copy>
            <xsl:apply-templates select="@*|*|text()|comment()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
