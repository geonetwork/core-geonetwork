<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:util="java:org.fao.geonet.util.XslUtil"
	xmlns:exslt="http://exslt.org/common" exclude-result-prefixes="exslt">
    <xsl:include href="../utils.xsl"/>
    <xsl:include href="../metadata.xsl"/>

    <xsl:output method="xml"/>

    <xsl:template match="/">
        <table border="1">
            <xsl:choose>
                <xsl:when test="not(node())">
                    <tr>
                        <td colspan="2">
                            <i>
                                <xsl:value-of select="/root/gui/strings/stat.noValue"/>
                            </i>
                        </td>
                    </tr>
                </xsl:when>
                <xsl:otherwise>
                    <tr>
                        <th>
                            <b>
                                <xsl:value-of select="/root/gui/strings/stat.md"/>
                            </b>
                        </th>
                        <th>
                            <b>
                                <xsl:value-of select="/root/gui/strings/stat.popularity"/>
                            </b>
                        </th>
                    </tr>
                    <xsl:variable name="locService" select="root/gui/locService"/>
                    <xsl:variable name="serverName" select="root/gui/env/server/host"/>
                    <xsl:variable name="serverPort" select="root/gui/env/server/port"/>
                    <xsl:variable name="limit" select="root/gui//mdPopularity/limit"/>
                    <xsl:for-each select="root/gui/mdPopularity/record[position() &lt;= $limit]">
                        <tr>
                            <td>
                                <a href="metadata.show?id={id}">
                            <!-- fixme: not a smarter/simpler way ? -->
                                    <xsl:variable name="id">
                                        <xsl:value-of select="id"/>
                                    </xsl:variable>
                                    
                                    <!-- Get title from index in current language or in default one. -->
                                    <xsl:variable name="metadataTitle" select="util:getIndexField(string(/root/gui/app/path), string(uuid), '_title', string(/root/gui/language))"/>
                                    <xsl:variable name="title">
                                        <xsl:choose>
                                            <xsl:when test="$metadataTitle!=''"><xsl:value-of select="$metadataTitle"/></xsl:when>
                                            <xsl:otherwise><xsl:value-of select="util:getIndexField(string(/root/gui/app/path), string(uuid), '_defaultTitle', string(/root/gui/language))"/></xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:variable>
                                    
                                    <xsl:choose>
                                        <xsl:when test="$title!=''"><xsl:value-of select="$title"/></xsl:when>
                                        <xsl:otherwise>(<xsl:value-of select="uuid"/>)</xsl:otherwise>
                                    </xsl:choose>
                                    
                                </a>
                            </td>
                            <td>
                                <xsl:value-of select="popularity"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </xsl:otherwise>
            </xsl:choose>
        </table>
    </xsl:template>
</xsl:stylesheet>
<!--  select simple, count(*) as cnt from requests group by simple -->
