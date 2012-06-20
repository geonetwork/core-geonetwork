<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	>
    <xsl:import  href="../translate-widget.xsl"/>
    <xsl:import  href="../utils.xsl"/>
	<xsl:output method="html"/>
    

    <xsl:variable name="langCode">
    	<xsl:choose>
    		<xsl:when test="string(/root/gui/language) = 'ger'">DE</xsl:when>
    		<xsl:otherwise>
    			<xsl:value-of select="translate(substring(string(/root/gui/language),1,2),$LOWER,$UPPER)"/>
    		</xsl:otherwise>
    	</xsl:choose>
    </xsl:variable>
    
	<!-- Return a list of user to be use
		for search and select user actions
	-->
	<xsl:template match="/">
		<ul>
			<xsl:for-each select="/root/response/record">
                
                <xsl:variable name="org">
                     <xsl:variable name="tmp">
                        <xsl:call-template name="translations" >
                            <xsl:with-param name="root" select="organisation" />
                            <xsl:with-param name="langCode" select="$langCode"/>
                        </xsl:call-template>
                     </xsl:variable>
                     <xsl:value-of select="normalize-space($tmp)"/>
                </xsl:variable>
                
                <xsl:variable name="acronym">
                     <xsl:variable name="tmp">
                         <xsl:call-template name="translations" >
                            <xsl:with-param name="root" select="orgacronym" />
                            <xsl:with-param name="langCode" select="$langCode"/>
                        </xsl:call-template>
                     </xsl:variable>
                     <xsl:value-of select="normalize-space($tmp)"/>
                </xsl:variable>
                
                <xsl:variable name="organization">
                    <xsl:choose>
                        <xsl:when test="$org!=''">
                            [<xsl:value-of select="$org"/>]
                        </xsl:when>
                        <xsl:when test="$org='' and $acronym!=''">
                            [<xsl:value-of select="$acronym"/>]
                        </xsl:when>
                        <xsl:otherwise> <!-- nothing --></xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:variable name="valid">
                    <xsl:call-template name="validIndicator">
                        <xsl:with-param name="indicator" select="normalize-space(translate(validated,$LOWER,$UPPER)) = 'N'" />
                    </xsl:call-template>
                </xsl:variable>

				<li xlink:href="local://xml.user.get?id={id}">
                    <xsl:copy-of select="$valid"/><xsl:text> </xsl:text>
					<xsl:value-of select="name"/><xsl:text> </xsl:text><xsl:value-of select="surname"/> 
					<xsl:text> </xsl:text>
                    <xsl:value-of select="$organization"/>
				</li>
			</xsl:for-each>
		</ul>
	</xsl:template>
	
<!--	-->
<!--    <xsl:template name="doLabel">-->
<!--        <xsl:param name="node"/>-->
<!--        <xsl:variable name="label" select="$node/node()[$langCode=name()]" />-->
<!--        <xsl:choose>-->
<!--        <xsl:when test="$label"><xsl:value-of select="$label"/></xsl:when>-->
<!--        <xsl:when test="text()"><xsl:value-of select="text()"/></xsl:when>-->
<!--        <xsl:otherwise><xsl:value-of select="text()"/></xsl:otherwise>-->
<!--        </xsl:choose>-->
<!--    </xsl:template>-->
</xsl:stylesheet>
