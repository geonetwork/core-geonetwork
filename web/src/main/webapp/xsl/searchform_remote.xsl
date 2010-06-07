<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="remote" match="/">
        
        <!-- remote search fields -->
        <xsl:if test="string(/root/gui/searchDefaults/remote)='off'">
            <form name="defsearch" id="defsearch" onsubmit="javascript:runRemoteSearch();">

                <script type="text/javascript" language="JavaScript1.2">
                
                    function profileSelected()
                    {
                        var serverList = document.search.profile.options[document.search.profile.selectedIndex].value;
                        var serverArray = serverList.split(' ');
                        deselectAllServers();
                        for (var i=0; i &lt; serverArray.length; i++)
                            selectServer(serverArray[i]);
                    }
                    
                    function serverSelected()
                    {
                        document.search.profile.options[0].selected = true;
                    }
                    
                    function deselectAllServers()
                    {
                        for (var i=0; i &lt; document.search.servers.length; i++)
                            document.search.servers.options[i].selected = false;
                    }
                    
                    function selectServer(server)
                    {
                        for (var i=0; i &lt; document.search.servers.length; i++)
                            if (document.search.servers.options[i].value == server)
                                document.search.servers.options[i].selected = true;
                    }
                    
                    function checkServers(){
                        if (isWhitespace(document.search.any.value) &amp;&amp;
                            !(document.search.title    &amp;&amp; !isWhitespace(document.search.title.value)) &amp;&amp;
                            !(document.search['abstract'] &amp;&amp; !isWhitespace(document.search['abstract'].value)) &amp;&amp;
                            !(document.search.themekey &amp;&amp; !isWhitespace(document.search.themekey.value)))
                            {
                                alert("Please type some search criteria");
                                return false;
                            }
                        servers = 0;
                        for (var i=0; i &lt; document.search.servers.length; i++)
                            if (document.search.servers.options[i].selected) servers++;
                        if (servers == 0)
                        {
                            alert("Please select a server");
                            return false;
                        }
                    }
                </script>
            
                <table class="geosearchfields" width="211px">
                    <tr>
                        <td rowspan="6" style="vertical-align: bottom;">
                            <img width="9px" height="246px" src="{/root/gui/url}/images/arrow_down_simple.gif"/>
                        </td>
                        <td colspan="2"/>
                    </tr>
                    
                    <!-- Any (free text) -->
                    <tr>
                        <td>
                            <xsl:value-of select="/root/gui/strings/searchText"/>
                        </td>
                        <td class="padded" align="right">
                            <input name="any" id="any" class="content"  size="17"
                                value="{/root/gui/searchDefaults/any}"/>
                            <br/>
                        </td>
                    </tr>
                    
                    
            <tr><td class="dots" colspan="2"/></tr>
            
            <!-- Profiles and servers -->
            <tr>
                <th class="padded"><xsl:value-of select="/root/gui/strings/profile"/></th>
                <td class="padded">
                    <select class="content" name="profile" onchange="profileSelected()">
                        <xsl:for-each select="/root/gui/searchProfiles/profile">
                            <option>
                                <xsl:if test="string(@value)=string(/root/gui/searchDefaults/profile)">
                                    <xsl:attribute name="selected"/>
                                </xsl:if>
                                <xsl:attribute name="value"><xsl:value-of select="@value"/></xsl:attribute>
                                <xsl:value-of select="."/>
                            </option>
                        </xsl:for-each>
                    </select>
                </td>
            </tr>
            
            <tr>
                <th class="padded"><xsl:value-of select="/root/gui/strings/server"/></th>
                <td class="padded">
                    <select class="content" name="servers" size="6" multiple="true" onchange="serverSelected()">
                        <xsl:for-each select="/root/gui/repositories/Instance">
                            <xsl:variable name="name" select="@instance_dn"/>
                            <xsl:variable name="collection" select="@collection_dn"/>
                            <xsl:variable name="description" select="/root/gui/repositories/Collection[@collection_dn=$collection]/@collection_name"/>
                            <option>
                                <xsl:if test="/root/gui/searchDefaults/servers/server[string(.)=$name]">
                                    <xsl:attribute name="selected"/>
                                </xsl:if>
                                <xsl:attribute name="value"><xsl:value-of select="$name"/></xsl:attribute>
                                <xsl:value-of select="$description"/>
                            </option>
                        </xsl:for-each>
                    </select>
                </td>
            </tr>
            
                    
                    <tr>
                        <td colspan="2" style="align: center; padding-top: 7px;">
                            <table class="advsearchfields" width="211px" border="0" cellspacing="0" cellpadding="0">
                                <tr >
                                    <td style="background: url({/root/gui/url}/images/arrow-bg.gif) repeat-x;" height="29px" width="30%">
                                    </td>
                                    <td style="padding:0px; margin:0px;" width="36px">
                                        <img width="36px" style="padding:0px; margin:0px;"  src="{/root/gui/url}/images/arrow-right.gif"/>
                                    </td>
                                    <td style="padding:0px; margin:0px;" width="13px">
                                        <img width="13px" style="padding:0px; margin:0px;"  src="{/root/gui/url}/images/search-left.gif"/>
                                    </td>
                                    <td align="center" style="background: url({/root/gui/url}/images/search-bg.gif) repeat-x; width: auto; white-space: nowrap; padding-bottom: 8px; vertical-align: bottom; cursor:hand;  cursor:pointer;" onclick="runRemoteSearch();" >
                                        <font color="#FFFFFF"><strong><xsl:value-of select="/root/gui/strings/search"/></strong></font>
                                    </td>
                                    <td style="padding:0px; margin:0px;" width="12px">
                                        <img width="12px" style="padding:0px; margin:0px;"  src="{/root/gui/url}/images/search-right.gif"/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    
<!--                    <script language="JavaScript" type="text/javascript">
                        Event.observe('any', 		'keypress',	gn_anyKeyObserver);
                        Event.observe('closeIMBtn', 	'click',  		closeIntermap	 );
                    </script> -->
                    
                    <tr>			
                        <td colspan="3">
                            <div style="padding-left:10px;padding-top:5px;" align="right">
                                <a onClick="resetRemoteSearch();" style="cursor:pointer; padding-right:10px; padding-left:10px;">
                                    <xsl:value-of select="/root/gui/strings/reset"/>
                                </a>
                                <a onClick="showSimpleSearch()" style="cursor:pointer;">
                                    <xsl:value-of select="/root/gui/strings/hideAdvancedOptions"/> <!-- FIXME -->
                                </a>
                                <a onClick="showFields('options.img','options.div')" style="cursor:pointer; padding-left:10px;" title="{/root/gui/strings/options}" alt="{/root/gui/strings/options}">
                                    <img id="options.img" src="{/root/gui/url}/images/configure.png"/>
<!--                                    <xsl:value-of select="/root/gui/strings/options"/> -->
                                </a>
                            </div>
                            <div id="options.div" style="display:none;"> 
                                <table width="100%">
                                    
                                    <!-- timeout for remote servers -->
                                    <tr>
                                        <td><xsl:apply-templates select="/root/gui/strings/timeout" mode="caption"/></td>
                                        <td>
                                            <select class="content" name="timeout">
                                                <xsl:for-each select="/root/gui/strings/timeoutChoice">
                                                    <option>
                                                        <xsl:if test="string(@value)=string(/root/gui/searchDefaults/timeout)">
                                                            <xsl:attribute name="selected"/>
                                                        </xsl:if>
                                                        <xsl:attribute name="value"><xsl:value-of select="@value"/></xsl:attribute>
                                                        <xsl:value-of select="."/>
                                                    </option>
                                                </xsl:for-each>
                                            </select>
                                        </td>
                                    </tr>
                                    
                                    <!-- sort by - - - - - - - - - - - - - - - - - - - - -->
                                    
                                    <!-- <tr>
                                        <td>
                                            <xsl:value-of select="/root/gui/strings/sortBy"/>
                                        </td>
                                        <td>
                                            <select id="sortBy" size="1" class="content">
                                                <xsl:for-each select="/root/gui/strings/sortByType">
                                                    <option value="{@id}">
                                                        <xsl:if test="@id = /root/gui/searchDefaults/sortBy">
                                                            <xsl:attribute name="selected"/>
                                                        </xsl:if>
                                                        <xsl:value-of select="."/>
                                                    </option>
                                                </xsl:for-each>
                                            </select>
                                        </td>
                                    </tr> -->
                                    
                                    <!-- hits per page - - - - - - - - - - - - - - - - - - -->
                                    
                                    <tr>
                                        <td>
                                            <xsl:value-of select="/root/gui/strings/hitsPerPage"/>
                                        </td>
                                        <td>
                                            <select id="hitsPerPage" size="1" class="content">
                                                <xsl:for-each select="/root/gui/strings/hitsPerPageChoice">
                                                    <option value="{@value}">
                                                        <xsl:if test="@value = /root/gui/searchDefaults/hitsPerPage">
                                                            <xsl:attribute name="selected"/>
                                                        </xsl:if>
                                                        <xsl:value-of select="."/>
                                                    </option>
                                                </xsl:for-each>
                                            </select>
                                        </td>
                                    </tr>
                                    
                                    <!-- output - - - - - - - - - - - - - - - - - - - - - - -->
                                    
                                    <tr>
                                        <td>
                                            <xsl:value-of select="/root/gui/strings/output"/>
                                        </td>
                                        <td>
                                            <select id="output" size="1" class="content">
                                                <xsl:for-each select="/root/gui/strings/outputType">
                                                    <option value="{@id}">
                                                        <xsl:if test="@id = /root/gui/searchDefaults/output">
                                                            <xsl:attribute name="selected"/>
                                                        </xsl:if>
                                                        <xsl:value-of select="."/>
                                                    </option>
                                                </xsl:for-each>
                                            </select>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                        </td>
                    </tr>
                </table>
            </form>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>