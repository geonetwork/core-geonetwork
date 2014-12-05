<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                xmlns:exslt="http://exslt.org/common" exclude-result-prefixes="exslt geonet java">

    <xsl:include href="banner.xsl"/>
    <xsl:include href="utils.xsl"/>
    <xsl:include href="metadata.xsl"/>

    <xsl:output omit-xml-declaration="yes" method="html"
                doctype-public="html" indent="yes" encoding="UTF-8" />


    <xsl:variable name="baseUrl">
        <xsl:value-of select="/root/gui/env/server/protocol" />://<xsl:value-of select="/root/gui/env/server/host" />:<xsl:value-of select="/root/gui/env/server/port" /><xsl:value-of select="/root/gui/url" />
    </xsl:variable>

    <xsl:variable name="appBaseUrl"><xsl:value-of select="$baseUrl"/>/apps/geocatch</xsl:variable>

    <!-- main page -->
    <xsl:template match="/">
    <html class="no-js">
        <head>

            <xsl:attribute name="lang">
                <xsl:value-of select="/root/gui/language" />
            </xsl:attribute>

            <meta http-equiv="Content-type" content="text/html;charset=UTF-8"></meta>
            <meta http-equiv="X-UA-Compatible" content="IE=9"/>

            <!-- Recent updates newsfeed -->
            <link href="{/root/gui/locService}/rss.latest?georss=gml" rel="alternate" type="application/rss+xml" title="GeoNetwork opensource GeoRSS | {/root/gui/strings/recentAdditions}" />
            <link href="{/root/gui/locService}/portal.opensearch" rel="search" type="application/opensearchdescription+xml">
                <xsl:attribute name="title"><xsl:value-of select="//site/name"/> (GeoNetwork)</xsl:attribute>
            </link>

            <!-- meta tags -->
            <xsl:copy-of select="/root/gui/strings/header_meta/meta"/>
            <META HTTP-EQUIV="Pragma"  CONTENT="no-cache"/>
            <META HTTP-EQUIV="Expires" CONTENT="-1"/>

            <!-- title -->
            <title><xsl:value-of select="/root/gui/strings/title"/></title>
            <link rel="shortcut icon" type="image/x-icon" href="{/root/gui/url}/images/logos/favicon.gif"/>
            <link rel="icon" type="image/x-icon" href="{/root/gui/url}/images/logos/favicon.gif"/>


            <meta name="description" content="" ></meta>
            <meta name="viewport" content="width=device-width"></meta>


            <link rel="alternate" type="application/rss+xml"  href="http://www.website.com/rss.xml"
                  title="RSS feed">
                <xsl:attribute name="href"><xsl:value-of
                        select="$baseUrl" />/srv/<xsl:value-of
                        select="/root/gui/language" />/rss.search</xsl:attribute>
            </link>

            <!-- CSS for Ext -->
            <link rel="stylesheet" type="text/css">
                <xsl:attribute name="href"><xsl:value-of
                        select="$baseUrl" />/apps/js/ext/resources/css/ext-all.css</xsl:attribute>
            </link>
            <!--<link rel="stylesheet" type="text/css">
                <xsl:attribute name="href"><xsl:value-of
                        select="$baseUrl" />/apps/js/ext/resources/css/xtheme-gray.css</xsl:attribute>
            </link>-->

            <link rel="stylesheet" type="text/css">
                <xsl:attribute name="href"><xsl:value-of
                        select="$baseUrl" />/apps/js/ext-ux/Rating/rating.css</xsl:attribute>
            </link>
            <link rel="stylesheet" type="text/css">
                <xsl:attribute name="href"><xsl:value-of
                        select="$baseUrl" />/apps/js/ext-ux/SuperBoxSelect/superboxselect.css</xsl:attribute>
            </link>
            <link rel="stylesheet" type="text/css">
                <xsl:attribute name="href"><xsl:value-of
                        select="$baseUrl" />/apps/js/ext-ux/LightBox/lightbox.css</xsl:attribute>
            </link>
            <link rel="stylesheet" type="text/css">
                <xsl:attribute name="href"><xsl:value-of
                        select="$baseUrl" />/apps/js/ext-ux/FileUploadField/file-upload.css</xsl:attribute>
            </link>
            <link rel="stylesheet" type="text/css">
                <xsl:attribute name="href"><xsl:value-of
                        select="$baseUrl" />/apps/js/ext-ux/MultiselectItemSelector-3.0/Multiselect.css</xsl:attribute>
            </link>


            <link rel="stylesheet">
                <xsl:attribute name="href"><xsl:value-of
                        select="$appBaseUrl" />/css/normalize.min.css</xsl:attribute>
            </link>
            <!--<link rel="stylesheet">
                <xsl:attribute name="href"><xsl:value-of
                        select="$appBaseUrl" />/css/gndefault.css</xsl:attribute>
            </link>-->
            <link rel="stylesheet">
                <xsl:attribute name="href"><xsl:value-of
                        select="$appBaseUrl" />/css/gnmetadatadefault.css</xsl:attribute>
            </link>
            <link rel="stylesheet">
                <xsl:attribute name="href"><xsl:value-of
                        select="$appBaseUrl" />/css/gnmapdefault.css</xsl:attribute>
            </link>

            <!--<link rel="stylesheet">
                <xsl:attribute name="href"><xsl:value-of
                        select="$appBaseUrl" />/css/geonetwork.css</xsl:attribute>
            </link>-->

            <link rel="stylesheet">
                <xsl:attribute name="href"><xsl:value-of
                        select="$appBaseUrl" />/css/metadata-view.css</xsl:attribute>
            </link>
                 <link rel="stylesheet">
                <xsl:attribute name="href"><xsl:value-of
                        select="$appBaseUrl" />/css/main.css</xsl:attribute>
            </link>


            <!--[if lt IE 7]> <link rel="stylesheet"> <xsl:attribute name="href"><xsl:value-of
                          select="$baseUrl" />/apps/ngr2/css/ltie7.css"/></xsl:attribute> </link> <![endif] -->

            <script type="text/javascript">
                <xsl:attribute name="src"><xsl:value-of
                        select="$baseUrl" />/apps/js/ext/adapter/ext/ext-base.js</xsl:attribute>
            </script>

            <script type="text/javascript">
                <xsl:attribute name="src"><xsl:value-of
                        select="$baseUrl" />/apps/js/ext/ext-all.js</xsl:attribute>
            </script>

            <script type="text/javascript">
                <xsl:attribute name="src"><xsl:value-of
                        select="$appBaseUrl" />/js/vendor/modernizr-2.6.1-respond-1.1.0.min.js</xsl:attribute>
            </script>

            
            <script>
                /**
                * Geocat specific stuff
                */
                 var geoserverUrl = '<xsl:value-of select="/root/gui/config/geoserver.url"/>';

                 var geoNetworkStores = {
                     'topicCat': [['', '<xsl:value-of select="/root/gui/strings/any"/>']<xsl:apply-templates select="/root/gui/schemas/iso19139/codelists/codelist[@name='gmd:MD_TopicCategoryCode']/entry" mode="js-translations-topicCat"/>],
                     'sources_groups': [<xsl:apply-templates select="/root/gui/groups/record" mode="js-translations-sources-groups"><xsl:sort select="label/*[name()=/root/gui/language]"/><xsl:sort select="name"/></xsl:apply-templates><xsl:if
                         test="count(/root/gui/groups/record) > 0 and count(/root/gui/sources/record) > 0">,</xsl:if><xsl:apply-templates select="/root/gui/sources/record[not(./siteid = preceding::record/siteid)]" mode="js-translations-sources-groups"><xsl:sort select="label/*[name()=/root/gui/language]"/><xsl:sort select="name"/></xsl:apply-templates>],
                     'formats': [['', '<xsl:value-of select="/root/gui/strings/any"/>']<xsl:apply-templates select="/root/gui/formats/record" mode="js-translations-formats"/>]
                 };
                function permlink(url) {
                    Ext.MessageBox.show({
                        title: '<xsl:value-of select="/root/gui/strings/permlink"/>',
                        msg: '<a href="'+url+'" target="_newtab">'+url+'</a>',
                        animEl: 'mb7'
                    });
                }
            </script>


        </head>
        <body>
            
            <script>
                var geonetMask = Ext.getBody().mask("<xsl:value-of select="/root/gui/strings/loading"/>");
                geonetMask.setStyle("opacity", "1");
            </script>
                    <!--[if lt IE 7]> <p class="chromeframe">You are using an outdated browser.
                             <a href="http://browsehappy.com/">Upgrade your browser today</a> or <a href="http://www.google.com/chromeframe/?redirect=true">install
                             Google Chrome Frame</a> to better experience this site.</p> <![endif] -->

            <div id="header">
                <div style="height: 100px; overflow:hidden;">
                    <img src="/geonetwork/images/geocat_logo_li.gif" alt="geocat.ch logo"  />
                    <img src="/geonetwork/images/bg_kopf_geocat.gif" alt="geocat.ch logo" style="float:right;" />
                </div>

                <header class="wrapper clearfix">
                    <div class="header_middle">
                        <nav id="navTop">
                            <ul id="top-navigation">
                                <li>
                                    <a href="http://www.geocat.ch/geonetwork/srv/{/root/gui/language}/geocat">
                                        <xsl:value-of select="/root/gui/strings/nav/home" />
                                    </a>
                                </li>
                                <li>
                                    <a class="selected" href="http://www.geocat.ch/internet/geocat/{/root/gui/strings/language}/tools/sitemap.html">
                                        <xsl:value-of select="/root/gui/strings/nav/overview" />
                                    </a>
                                </li>
                                <xsl:if test="string(/root/gui/config/serverStage) != 'production'">
                                <li>
                                    <a onclick="javascript:alert('You are not on the production server.  You are using the \'{/root/gui/config/serverStage}\' server.  http://www.geocat.ch is the production/real server')" class="banner"><xsl:value-of select="/root/gui/config/serverStage"/></a>
                                </li>
                                </xsl:if>
                            </ul>
                        </nav>
                        <div id="lang-form"></div>
                        <div style="clear: both; display: hidden"></div>
                    </div>

                    <xsl:variable name="user">
                        <xsl:value-of select="/root/response/user" />
                    </xsl:variable>
                    <div class="header_bottom">
                        <nav id="nav">
                            <ul id="main-navigation">
                                <li>
                                    <a href="http://www.geocat.ch/internet/geocat/{/root/gui/strings/language}/home/news.html"><xsl:value-of select="/root/gui/strings/nav/news"/></a>
                                </li>
                                <li>
                                    <xsl:choose>
                                        <xsl:when test="/root/gui/reqService='geocat' or /root/gui/reqService='geocatui' or /root/gui/reqService='user.login' or /root/gui/reqService='user.logout'">
                                            <a class="banner-active" href="geocat"><xsl:value-of select="/root/gui/strings/nav/metasearch"/></a>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <a class="banner" href="geocat"><xsl:value-of select="/root/gui/strings/nav/metasearch"/></a>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </li>
                                <li id="adminMenu">
                                    <xsl:attribute name="style">
                                        <xsl:if test="string(/root/gui/session/userId)=''">display:none</xsl:if>
                                    </xsl:attribute>

                                    <xsl:choose>
                                        <xsl:when test="/root/gui/reqService='admin'">
                                            <a class="banner-active" href="admin"><xsl:value-of select="/root/gui/strings/nav/metainput"/></a>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <a class="banner" href="admin"><xsl:value-of select="/root/gui/strings/nav/metainput"/></a>
                                        </xsl:otherwise>
                                    </xsl:choose>

                                </li>
                                <li>
                                    <a href="http://www.geocat.ch/internet/geocat/{/root/gui/strings/language}/home/documentation.html"><xsl:value-of select="/root/gui/strings/nav/doc"/></a>
                                </li>
                                <li>
                                    <a href="http://www.geocat.ch/internet/geocat/{/root/gui/strings/language}/home/about.html"><xsl:value-of select="/root/gui/strings/nav/about"/></a>
                                </li>
                            </ul>
                        </nav>
                        
                        <script>
                            function false_(){
                                return false;
                            }
                        
                        </script>
                        
                        <form id="login-form" action="{/root/gui/locService}/j_spring_security_check" onsubmit="return false_();">
                        
	                       <xsl:choose>
	                           <xsl:when test="string(/root/gui/session/userId)!=''">
	                               <xsl:attribute name="style">display:none;</xsl:attribute>
	                           </xsl:when>
	                       </xsl:choose> 
                       
                            <div id="login_div">
                                <label>
                                    <xsl:value-of select="/root/gui/strings/username" />:
                                </label>
                                <input type="text" id="username" class="banner" name="username" />
                                <label>
                                    <xsl:value-of select="/root/gui/strings/password" />:
                                </label>
                                <input type="password" id="password" class="banner" name="password" />
                                <input type="button" class="banner"  id="login_button">
                                
                                    <xsl:attribute name="value">
                                        <xsl:value-of
                                                select="/root/gui/strings/login" />
                                    </xsl:attribute>
                                </input>
                            </div>
                       </form>
                        
                       <form id="logout-div">
                           <xsl:choose>
                               <xsl:when test="not(string(/root/gui/session/userId)!='')">
                                <xsl:attribute name="style">display:none;</xsl:attribute>
                               </xsl:when>
                           </xsl:choose>
                           <form name="logout" onsubmit="return false_();">
                                <label id="username_label">
                                    <xsl:value-of select="/root/gui/strings/username"/>
                                </label>
                                <label id="name_label">
                                    <xsl:text>: </xsl:text>
                                    <xsl:value-of select="/root/gui/session/name"/><xsl:text> </xsl:text><xsl:value-of select="/root/gui/session/surname"/>
                                </label>
                                <label id="profile_label">
                                    <xsl:text> (</xsl:text>
                                    <xsl:value-of select="/root/gui/session/profile"/>
                                    <xsl:text>) </xsl:text>
                                </label>
                                <button class="banner" onclick="javascript:catalogue.logout();"><xsl:value-of select="/root/gui/strings/logout"/></button>
                            </form>
                        </form>
                        
                        <div style="clear: both; display: hidden"></div>
                    </div>
                </header>
            </div>

            <div id="search-results" style="height: 100%">
                <div id="results-main">
                    <h2><xsl:value-of
                            select="/root/gui/strings/mainpageTitle" /></h2>
                    <img src="{/root/gui/url}/images/geocatII-web.jpg" alt="Geocat cat" width="100px"/>

                    <fieldset id="latestChanges">
                       <legend><xsl:value-of select="/root/gui/strings/recentAdditions"/>&#160;<a href="../../srv/en/rss.latest?georss=simplepoint" alt="Latest news" title="Latest news"><img src="../../apps/images/default/feed.png"/></a></legend>
                        <div id="latest-metadata">
                           <xsl:for-each select="/root/gui/latestUpdated/*">
								<xsl:variable name="md">
									<xsl:apply-templates mode="brief" select="."/>
								</xsl:variable>
								<xsl:variable name="metadata" select="$md/*[1]"/>
								<div><a class="arrow" href="javascript:catalogue.metadataShow('{geonet:info/uuid}');" title="{$metadata/title}">
                                    <xsl:value-of select="$metadata/title"/>
								</a></div>
							</xsl:for-each>
                        </div>
                    </fieldset>
                </div>
                
                <div id="result-panel"></div>
            </div>

             <div id="search-filter">
                <div id="bread-crumb-div"></div>
                <div id="facets-panel-div"></div>
            </div>

            <div id="map-div"></div>
            
            <script type="text/javascript">
                GeoNetwork_URL = '<xsl:value-of select="$baseUrl" />';
            </script>

            <xsl:choose>
                <xsl:when test="/root/request/debug">

                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/ext-ux/Rating/RatingItem.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/ext-ux/FileUploadField/FileUploadField.js</xsl:attribute>
                    </script>

                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/ext-ux/TwinTriggerComboBox/TwinTriggerComboBox.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/ext-ux/DateTime/DateTime.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/ext-ux/RowExpander/RowExpander.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/ext-ux/MultiselectItemSelector-3.0/DDView.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/ext-ux/MultiselectItemSelector-3.0/Multiselect.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/ext-ux/SuperBoxSelect/SuperBoxSelect.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/ext-ux/LightBox/lightbox.js</xsl:attribute>
                    </script>

                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/proj4js-compressed.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/OpenLayers/lib/OpenLayers.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/GeoExt/lib/overrides/override-ext-ajax.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/GeoExt/lib/GeoExt.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/GeoExt-ux/LayerOpacitySliderPlugin/LayerOpacitySliderPlugin.js</xsl:attribute>
                    </script>

                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/GeoNetwork/lib/GeoNetwork.js</xsl:attribute>
                    </script>
                    
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/GeoNetwork/lib/GeoNetwork/widgets/FacetsPanel.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/GeoNetwork/lib/GeoNetwork/Message.js</xsl:attribute>
                    </script>

                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$appBaseUrl" />/js/lang/en.js</xsl:attribute>
                    </script>

                     <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$appBaseUrl" />/js/lang/de.js</xsl:attribute>
                    </script>

                     <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$appBaseUrl" />/js/lang/fr.js</xsl:attribute>
                    </script>

                                         <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$appBaseUrl" />/js/lang/it.js</xsl:attribute>
                    </script>

                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$appBaseUrl" />/js/GlobalFunctions.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$appBaseUrl" />/js/Settings.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$appBaseUrl" />/js/Shortcuts.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$appBaseUrl" />/js/map/Settings.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$appBaseUrl" />/js/map/MapApp.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$appBaseUrl" />/js/BreadCrumb.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$appBaseUrl" />/js/Templates.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$appBaseUrl" />/js/search/SearchApp.js</xsl:attribute>
                    </script>
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$appBaseUrl" />/js/user/LoginApp.js</xsl:attribute>
                    </script>

                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$appBaseUrl" />/js/App.js</xsl:attribute>
                    </script>
                    
                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$appBaseUrl" />/js/MetadataTooltip.js</xsl:attribute>
                    </script>

                    <script type="text/javascript">
                        <xsl:attribute name="src"><xsl:value-of
                                select="$appBaseUrl" />/js/NotifyByEmail.js</xsl:attribute>
                    </script>
                    
                </xsl:when>
                <xsl:otherwise>
                    <script type="text/javascript" src="{concat($appBaseUrl, '/js/App-mini.js')}"></script>
                </xsl:otherwise>
            </xsl:choose>
        </body>
    </html>
    </xsl:template>

    <xsl:template match="*" mode="js-translations-combo-suite">
        ,["<xsl:value-of select="@value"/><xsl:value-of select="@id"/>", "<xsl:value-of select="."/>"]</xsl:template>

    <xsl:template match="*" mode="js-translations-combo">
        <xsl:if test="position()>1">,</xsl:if>["<xsl:value-of select="@value"/><xsl:value-of select="@id"/>", "<xsl:value-of select="."/>"]</xsl:template>

    <xsl:template match="entry" mode="js-translations-topicCat">
        ,["<xsl:value-of select="code"/>", "<xsl:value-of select="label"/>"]</xsl:template>

    <xsl:template match="record" mode="js-translations-sources-groups">
        <xsl:if test="position()>1">,</xsl:if>
        <xsl:choose>
            <xsl:when test="siteid">["_source/<xsl:value-of select="siteid"/>", "<xsl:value-of select="normalize-space(replace(translate(name,'&quot;', '`'), '[\n\r]+', ' '))"/>"</xsl:when>
            <xsl:otherwise>["_groupOwner/<xsl:value-of select="id"/>", "<xsl:value-of select="normalize-space(replace(translate(label/*[name()=/root/gui/language],'&quot;', '`'), '[\n\r]+', ' '))"/>"
            </xsl:otherwise>
        </xsl:choose>]
    </xsl:template>

    <xsl:template match="record" mode="js-translations-formats">
        ,["<xsl:value-of select="name"/><xsl:if test="version != '-'">_<xsl:value-of select="version"/></xsl:if>", "<xsl:value-of select="name"/> (<xsl:value-of select="version"/>)"]</xsl:template>

</xsl:stylesheet>
