<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:geonet="http://www.fao.org/geonetwork">


    <xsl:template match="/">
        <html>
            <head>
                <link href="../../catalog/lib/style/bootstrap-3.0.1/bootstrap.less" rel="stylesheet/less" type="text/css"/>
                <script src="../../catalog/lib/less-1.4.1.min.js"></script>
            </head>
            <body class="container">
                <h1 class="text-danger">
                    <xsl:value-of select="/root/gui/startupError/error/Error"/>
                </h1>
                <div class="alert alert-danger">
                    <table>
                    <xsl:apply-templates mode="showError" select="/root/gui/startupError/error/*[name()!='Error' and name()!='Stack']"/>
                    <xsl:apply-templates mode="showError" select="/root/gui/startupError/error/Stack"/>
                    </table>
                </div>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template mode="showError" match="*">
        <tr>
            <td>
                <strong><xsl:value-of select="name(.)"/></strong>
            </td>
            <td>
                <pre><xsl:value-of select="string(.)"/></pre>
            </td>
        </tr>
    </xsl:template>
    
</xsl:stylesheet>
