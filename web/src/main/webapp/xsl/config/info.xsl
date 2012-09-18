<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!-- 
	Display system information panel.
	-->
	<xsl:include href="../main.xsl"/>

	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/systemInfo"/>

			<xsl:with-param name="content">
                <xsl:if test="count(//info) > 1">
                    <div style="text-align:left;padding:5px;">
                        <h2>Retrieved system configuration of <xsl:value-of select="count(//info)"/> nodes</h2>

                        <xsl:for-each select="//info">
                            <xsl:sort select="system/children/nodeId/value" />
                            <fieldset style="text-align:left; margin:40px 0px;color:#0963F4;border-color:#2C7DF7;border-width:1px;">
                                <legend style="font-weight:bold;">Node identifier: <xsl:value-of select="system/children/nodeId/value"/></legend>
                                <div style="margin:5px;color:#064377;">
                                    <xsl:apply-templates mode="block" select="*"/>
                                </div>
                            </fieldset>
                        </xsl:for-each>
                    </div>
                </xsl:if>
                <fieldset style="text-align:left;">
                    <legend>Monitoring</legend>
                    <ul>
                        <li>
                            <a href="{/root/gui/url}/monitor/metrics?pretty=true">
                                <label style="text-size:140%;font-weight:bold">Metrics</label>
                            </a>
                        </li>
                        <li>
                            <a href="{/root/gui/url}/monitor/healthcheck">
                                <label style="text-size:140%;font-weight:bold">Health Check</label>
                            </a>
                        </li>
                        <li>
                            <a href="{/root/gui/url}/monitor/threads">
                                <label style="text-size:140%;font-weight:bold">Threads</label>
                            </a>
                        </li>
                        <li>
                            <a href="{/root/gui/locService}/debug.filehandles?max=100&amp;filter=^((?!\.jar).)*$">
                                <label style="text-size:140%;font-weight:bold">Open File Descriptors</label>
                            </a>
                        </li>
                        <li>
                            <a href="{/root/gui/locService}/debug.openconnection.accessors">
                                <label style="text-size:140%;font-weight:bold">Open Connections</label>
                            </a>
                        </li>
                    </ul>
                </fieldset>

			</xsl:with-param>
			<xsl:with-param name="buttons"></xsl:with-param>
		</xsl:call-template>
    </xsl:template>
	
	<xsl:template mode="block" match="system|catalogue|main|index|database">
		<fieldset style="text-align:left;">
			<xsl:variable name="tag" select="name(.)"/>
			<legend><xsl:value-of select="/root/gui/config/*[name()=$tag]"/></legend>
			<xsl:for-each select="*">
				<xsl:sort order="ascending" select="name(.)"/>
				<xsl:apply-templates mode="info" select="."/>
			</xsl:for-each>
            <xsl:if test="name(.) = 'main'">
                <xsl:variable name="value">
                    <xsl:choose>
                        <xsl:when test="system-property('geonetwork.sequential.execution') != ''">
                            <xsl:value-of select="system-property('geonetwork.sequential.execution')"/>
                        </xsl:when>
                        <xsl:otherwise>false</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <label style="text-size:140%;font-weight:bold"><xsl:value-of select="/root/gui/config/*[name()='sequentialExecution']"/></label> <span class="info"><xsl:value-of select="$value"/></span><br/>
            </xsl:if>
		</fieldset>
	</xsl:template>
	
	
	<xsl:template mode="info" match="main/*|index/*|catalogue/*|database/*|siteId|version|subVersion">
		<xsl:variable name="tag" select="name(.)"/>
		<label style="text-size:140%;font-weight:bold"><xsl:value-of select="/root/gui/config/*[name()=$tag]"/></label> <span class="info"><xsl:value-of select=".|value"/></span><br/>
	</xsl:template>
	
	<xsl:template mode="info" match="index/index.lucene.config">
		<xsl:variable name="tag" select="name(.)"/>
		<label style="text-size:140%;font-weight:bold"><xsl:value-of select="/root/gui/config/*[name()=$tag]"/></label> 
		<div class="info" style="overflow:auto;width:600px !important;">
			<pre><xsl:value-of select="."/></pre></div><br/>
	</xsl:template>
	
	<xsl:template mode="info" match="*">
		<xsl:apply-templates mode="info" select="*"/>
	</xsl:template>
</xsl:stylesheet>
