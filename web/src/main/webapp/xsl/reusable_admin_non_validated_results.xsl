<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:util="xalan://org.fao.geonet.util.XslUtil"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <xsl:include href="main.xsl"/>

    <xsl:template mode="css" match="/">
        <script type="text/javascript">
            window.gMfLocation = '<xsl:value-of select="/root/gui/url"/>/scripts/mapfish/';
        </script>
        <xsl:call-template name="geoCssHeader"/>
    </xsl:template>
            
    <xsl:template mode="script" match="/">
    
        <!-- To avoid an interaction with prototype and ExtJs.Tooltip, should be loaded before ExtJs -->
        <xsl:choose>
            <xsl:when test="/root/request/debug">
                <script type="text/javascript" src="{/root/gui/url}/scripts/prototype.js"></script>
            </xsl:when>
            <xsl:otherwise>
              <script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.libs.js"></script>      
            </xsl:otherwise>
        </xsl:choose>
    
        <xsl:call-template name="geoHeader"/>
        <script src="{/root/gui/url}/scripts/RowExpander.js" type="text/javascript"/>
         
         <script language="JavaScript1.2" type="text/javascript">
                         
            locUrl = '<xsl:value-of select="/root/gui/locService"/>';
            function refresh(){
                msgWindow.close();  
                var page = Ext.query("tr.blue-content").first().id.substring(4);
                show(page);
            }             
            
            function pageInit(){
                rejectBtnDefaultTxt = '<xsl:value-of select="/root/gui/strings/reject" />';
                var page = 'contacts';
                <xsl:if test="/root/request/page">
                page = '<xsl:value-of select="/root/request/page"/>';
                </xsl:if>
                
                show(page, '<xsl:value-of select="/root/gui/strings/delete"/>');
            }
         </script>

         <script src="{/root/gui/url}/scripts/mapfishIntegration/MapComponent.js" type="text/javascript"/>
         <script src="{/root/gui/url}/scripts/mapfishIntegration/MapDrawComponent.js" type="text/javascript"/>
         <script src="{/root/gui/url}/scripts/mapfishIntegration/geocat.js" type="text/javascript"/>
         <script src="{/root/gui/url}/scripts/reusable-validate.js" type="text/javascript"/>
    </xsl:template>
    
	<xsl:template name="content">
	   <xsl:call-template name="formLayout">
            <xsl:with-param name="title" select="/root/gui/strings/reusable/nonValidTitle"/>
            <xsl:with-param name="indent">0
            </xsl:with-param>
            <xsl:with-param name="content">
                <xsl:call-template name="nav"/>
            
                <xsl:call-template name="form"/>
            </xsl:with-param>
            <xsl:with-param name="buttons">
                <xsl:call-template name="buttons"/>
            </xsl:with-param>
            
       </xsl:call-template>
    </xsl:template>
    
    <xsl:template name="buttons">
        <button class="content" id='edit' onclick="edit()">
            <xsl:value-of select="/root/gui/strings/edit"/>
        </button>
                            &#160;
        <button class="content" id='validate' onclick="validate()">
            <xsl:value-of select="/root/gui/strings/validate"/>
        </button>
                            &#160;
        <button class="content" id="reject" onclick="reject('reject', '{/root/gui/strings/submit}', '{/root/gui/strings/cancel}')">
            <xsl:value-of select="/root/gui/strings/reject"/>
        </button>
                            &#160;
        <button class="content" onclick="load('{/root/gui/locService}/admin')">
            <xsl:value-of select="/root/gui/strings/back"/>
        </button>
    </xsl:template>
	
	<xsl:template name="nav">
        <ul class="nav" style="padding-left: 60px;">
            <li id="nav_contacts"> <a href="javascript:show('contacts')"><xsl:value-of select="/root/gui/strings/user"/></a></li>
            <li id="nav_formats"> <a href="javascript:show('formats')"><xsl:value-of select="/root/gui/strings/formats"/></a></li>
            <li id="nav_extents"> <a href="javascript:show('extents')"><xsl:value-of select="/root/gui/strings/extents"/></a></li>
            <li id="nav_keywords"> <a href="javascript:show('keywords')"><xsl:value-of select="/root/gui/strings/keywords"/></a></li>
            <li id="nav_deleted"> <a href="javascript:showDeletePage('{/root/gui/strings/delete}')"><xsl:value-of select="/root/gui/strings/deleted"/></a></li>
       </ul><br/>
	</xsl:template>
	
	   
    <xsl:template name="form">
    
        <div id="msg_win" class="x-hidden">
            <div class="x-window-header"><xsl:value-of select="/root/gui/strings/reusable/rejectTitle"/></div>
               <div id="msg-panel">
                    <textarea style="width: 100%; height: 300px;" id="reusable_msg"><xsl:value-of select="/root/gui/strings/reusable/rejectDefaultMsg"/></textarea>
            </div>
        </div>

        <div id="grid-panel"/>        
    </xsl:template>

    <xsl:template match="text()"></xsl:template>
    
</xsl:stylesheet>
