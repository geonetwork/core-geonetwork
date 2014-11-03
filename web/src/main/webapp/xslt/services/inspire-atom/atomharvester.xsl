<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:util="xalan://org.fao.geonet.util.XslUtil">

    <!-- ============================================================================================= -->

    <xsl:include href="../modal.xsl"/>

    <!-- ============================================================================================= -->

    <xsl:variable name="style" select="'margin-left:50px;text-align:left'"/>
    <xsl:variable name="width" select="'70px'"/>

    <!-- ============================================================================================= -->

    <!-- ============================================================================================= -->
    <!-- === page content -->
    <!-- ============================================================================================= -->

    <xsl:template name="content">
        Processed (<xsl:value-of select="count(/root/response/feed[string(@feed)])" />):
        <ul>
            <xsl:for-each select="/root/response/feed[string(@feed)]">
                <xsl:variable name="title" select="if (@uuid) then util:getIndexField(string(/root/gui/app/path), string(@uuid), '_title', string(/root/gui/language)) else ''"/>

                <li><xsl:value-of select="$title" /> (<xsl:value-of select="@uuid" />): <xsl:value-of select="@feed" /></li>
            </xsl:for-each>
        </ul>

        <xsl:if test="count(/root/response/feed[string(@error)]) > 0">

            Errors (<xsl:value-of select="count(/root/response/feed[string(@error)])" />):
            <ul>
                <xsl:for-each select="/root/response/feed[string(@error)]">
                    <xsl:variable name="title" select="if (../@uuidref) then util:getIndexField(string(/root/gui/app/path), string(@uuid), '_title', string(/root/gui/language)) else ''"/>

                    <li><xsl:value-of select="$title" /> (<xsl:value-of select="@uuid" />): <xsl:value-of select="@error" /></li>
                </xsl:for-each>
            </ul>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>