<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="main.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/info/heading"/>
			<xsl:with-param name="content">

                <div style="text-align:left">
                    <!-- Database status -->
                    <h1 class="monitoring_title"><xsl:value-of select="/root/gui/strings/monitoring.service.db"/></h1>
                    <div style="margin-left:20px">
                        <xsl:choose>
                            <xsl:when test="/root/monitoringReport/db/status='ok'">
                                <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.status"/>: </span> <xsl:value-of select="/root/gui/strings/monitoring.connectionsuccess"/></div>
                                <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.responsetime"/>: </span><xsl:value-of select="/root/monitoringReport/db/responseTime"/> ms.</div>

                                <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.metadatarecords"/>: </span> <xsl:value-of select="/root/monitoringReport/db/metadataCount"/></div>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:call-template name="error_info">
                                    <xsl:with-param name="error_code"><xsl:value-of select="/root/monitoringReport/db/errorCode"/></xsl:with-param>
                                    <xsl:with-param name="error_description"><xsl:value-of select="/root/monitoringReport/db/errorDescription"/></xsl:with-param>
                                </xsl:call-template>
                            </xsl:otherwise>

                        </xsl:choose>                        
                    </div>

                    <!-- CSW status -->
                    <h1 class="monitoring_title"><xsl:value-of select="/root/gui/strings/monitoring.service.csw"/></h1>
                    <div style="margin-left:20px">
                        <xsl:choose>
                            <!-- CSW enabled -->
                            <xsl:when test="/root/monitoringReport/cswService/enabled='true'">
                                <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.status"/>: </span> <xsl:value-of select="/root/gui/strings/monitoring.enabled"/></div>

                               <!-- Capabilities -->
                                <h2 class="monitoring_title"><xsl:value-of select="/root/gui/strings/monitoring.service.csw.capabilities"/></h2>
                                <div style="margin-left:20px">
                                    <xsl:choose>
                                        <xsl:when test="/root/monitoringReport/cswService_capabilities/status='ok'">
                                            <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.status"/>: </span> <xsl:value-of select="/root/gui/strings/monitoring.connectionsuccess"/></div>
                                            <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.responsetime"/>: </span> <xsl:value-of select="/root/monitoringReport/cswService_capabilities/responseTime"/> ms.</div>

                                        </xsl:when>
                                        <xsl:otherwise>
                                             <xsl:call-template name="error_info">
                                                <xsl:with-param name="error_code"><xsl:value-of select="/root/monitoringReport/cswService_capabilities/errorCode"/></xsl:with-param>
                                                <xsl:with-param name="error_description"><xsl:value-of select="/root/monitoringReport/cswService_capabilities/errorDescription"/></xsl:with-param>
                                            </xsl:call-template>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:variable name="capabServiceUrl" select="/root/monitoringReport/cswService_capabilities/url"/>

                                    <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.url"/>: </span> <a href="{$capabServiceUrl}" target="_blank"><xsl:value-of select="$capabServiceUrl" /> </a></div>                                    
                                </div>

                                <!-- GetRecords -->
                                <xsl:if test="/root/monitoringReport/cswService_getrecords">
                                <h2 class="monitoring_title"><xsl:value-of select="/root/gui/strings/monitoring.service.csw.getrecords"/></h2>
                                <div style="margin-left:20px">
                                    <xsl:choose>

                                        <xsl:when test="/root/monitoringReport/cswService_getrecords/status='ok'">
                                            <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.status"/>: </span> <xsl:value-of select="/root/gui/strings/monitoring.connectionsuccess"/></div>
                                            <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.responsetime"/>: </span> <xsl:value-of select="/root/monitoringReport/cswService_getrecords/responseTime"/> ms.</div>
                                        </xsl:when>
                                        <xsl:otherwise>
                                             <xsl:call-template name="error_info">

                                                <xsl:with-param name="error_code"><xsl:value-of select="/root/monitoringReport/cswService_getrecords/errorCode"/></xsl:with-param>
                                                <xsl:with-param name="error_description"><xsl:value-of select="/root/monitoringReport/cswService_getrecords/errorDescription"/></xsl:with-param>
                                            </xsl:call-template>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:variable name="getrecordsServiceUrl" select="/root/monitoringReport/cswService_getrecords/url"/>
                                    <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.url"/>: </span> <a href="{$getrecordsServiceUrl}" target="_blank"><xsl:value-of select="substring($getrecordsServiceUrl, 1, 90)" />... </a></div>                                   
                                </div>

                                </xsl:if>
                            </xsl:when>

                            <!-- CSW disabled -->
                            <xsl:otherwise>
                                <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.status"/> </span> <xsl:value-of select="/root/gui/strings/monitoring.disabled"/></div>
                            </xsl:otherwise>
                        </xsl:choose>                    
                    </div>

                    <!-- Print service status
                    C2C PMT geocat2 : check is disabled for now on this service
                    <h1 class="monitoring_title"><xsl:value-of select="/root/gui/strings/monitoring.service.print"/></h1>
                    <div style="margin-left:20px">
                        <xsl:choose>
                            <xsl:when test="/root/monitoringReport/printService/status='ok'">
                                <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.status"/>: </span> <xsl:value-of select="/root/gui/strings/monitoring.connectionsuccess"/></div>
                                <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.responsetime"/>: </span> <xsl:value-of select="/root/monitoringReport/printService/responseTime"/> ms.</div>

                            </xsl:when>
                            <xsl:otherwise>
                                 <xsl:call-template name="error_info">
                                    <xsl:with-param name="error_code"><xsl:value-of select="/root/monitoringReport/printService/errorCode"/></xsl:with-param>
                                    <xsl:with-param name="error_description"><xsl:value-of select="/root/monitoringReport/printService/errorDescription"/></xsl:with-param>
                                </xsl:call-template>
                             </xsl:otherwise>
                        </xsl:choose>
                        <xsl:variable name="printServiceUrl" select="/root/monitoringReport/printService/url"/>

                        <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.url"/>: </span> <a href="{$printServiceUrl}" target="_blank"><xsl:value-of select="$printServiceUrl" /> </a></div>
                    </div>
 					-->

                    <!-- Index service status -->
                    <h1 class="monitoring_title"><xsl:value-of select="/root/gui/strings/monitoring.service.index"/></h1>
                    <div style="margin-left:20px">
                        <xsl:choose>

                            <xsl:when test="/root/monitoringReport/indexService/status='indexing'">
                                <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.status"/>: </span> <xsl:value-of select="/root/gui/strings/monitoring.indexing"/></div>
                            </xsl:when>
                            <xsl:when test="/root/monitoringReport/indexService/status='idle'">
                                <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.status"/>: </span> <xsl:value-of select="/root/gui/strings/monitoring.idle"/></div>
                            </xsl:when>
                            <xsl:otherwise>

                                <xsl:call-template name="error_info">
                                    <xsl:with-param name="error_code"><xsl:value-of select="/root/monitoringReport/indexService/errorCode"/></xsl:with-param>
                                    <xsl:with-param name="error_description"><xsl:value-of select="/root/monitoringReport/indexService/errorDescription"/></xsl:with-param>
                                </xsl:call-template>                                
                            </xsl:otherwise>
                        </xsl:choose>
                    </div>

                    <!-- Disk service status -->
                    <h1 class="monitoring_title"><xsl:value-of select="/root/gui/strings/monitoring.service.disk"/></h1>

                    <div style="margin-left:20px">
                        <xsl:choose>
                            <xsl:when test="/root/monitoringReport/freediskService/status='ok'">
                                <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.freespace"/>: </span> <xsl:value-of select="/root/monitoringReport/freediskService/freespace"/></div>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:if test="/root/gui/strings/monitoring.freespace">
                                    <div><span class="monitoring_label" style="color:red"><xsl:value-of select="/root/gui/strings/monitoring.freespace"/>: </span>

                                         <span style="color:red;font-weight:bold"><xsl:value-of select="/root/monitoringReport/freediskService/freespace"/></span>
                                    </div>
                                </xsl:if>
                                <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.error.code"/>: </span> <xsl:value-of select="/root/monitoringReport/freediskService/errorCode"/></div>
                                <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.error.description"/>: </span> <xsl:value-of select="/root/monitoringReport/freediskService/errorDescription"/></div>
                             </xsl:otherwise>
                        </xsl:choose>

                    </div>
                </div>
            </xsl:with-param>

            <xsl:with-param name="buttons">
				<xsl:call-template name="buttons"/>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

    <!-- ============================================================================================= -->
    <!-- === Error info -->
    <!-- ============================================================================================= -->

    <xsl:template name="error_info">
        <xsl:param name="error_code" />
        <xsl:param name="error_description" />

        <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.status"/>: </span> <xsl:value-of select="/root/gui/strings/monitoring.connectionerror"/></div>

        <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.error.code"/>: </span> <xsl:value-of select="$error_code"/></div>
        <div><span class="monitoring_label"><xsl:value-of select="/root/gui/strings/monitoring.error.description"/>: </span> <xsl:value-of select="$error_description"/></div>
    </xsl:template>


    <!-- ============================================================================================= -->
    <!-- === Buttons -->
    <!-- ============================================================================================= -->

    <xsl:template name="buttons">
        <button class="content" onclick="load('{/root/gui/locService}/admin')">
            <xsl:value-of select="/root/gui/strings/back"/>
        </button>
        &#160;
        <button class="content" onclick="load('{/root/gui/locService}/monitoring.report')">
            <xsl:value-of select="/root/gui/strings/monitoring.refresh"/>
        </button>

    </xsl:template>

</xsl:stylesheet>
