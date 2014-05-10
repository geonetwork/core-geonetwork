<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:exslt="http://exslt.org/common" exclude-result-prefixes="exslt geonet">
	<xsl:include href="metadata/common.xsl" />
	<xsl:output omit-xml-declaration="no" method="html"
		doctype-public="html" indent="yes" encoding="UTF-8" />
	<xsl:variable name="hostUrl" select="concat(/root/gui/env/server/protocol, '://', /root/gui/env/server/host, ':', /root/gui/env/server/port)"/>
	<xsl:variable name="baseUrl" select="/root/gui/url" />
	<xsl:variable name="serviceUrl" select="concat($hostUrl, /root/gui/locService)" />
	<xsl:variable name="rssUrl" select="concat($serviceUrl, '/rss.search?sortBy=changeDate')" />
	<xsl:variable name="siteName" select="/root/gui/env/site/name"/>
	
	<!-- main page -->
	<xsl:template match="/">
    <html class="no-js">
           <xsl:attribute name="lang">
                <xsl:value-of select="/root/gui/language" />
            </xsl:attribute>
    
			<head>
				<meta http-equiv="Content-type" content="text/html;charset=UTF-8"></meta>
				<meta http-equiv="X-UA-Compatible" content="IE=9,chrome=1"></meta>
				<title><xsl:value-of select="$siteName" /></title>
				<meta name="description" content="" ></meta>
                <meta name="viewport" content="width=device-width"></meta>
				<meta name="og:title" content="{$siteName}"/>
				
				<link rel="icon" type="image/gif" href="../../images/logos/favicon.gif" />
				<link rel="alternate" type="application/rss+xml" title="{$siteName} - RSS" href="{$rssUrl}"/>
				<link rel="search" href="{$serviceUrl}/portal.opensearch" type="application/opensearchdescription+xml" 
					title="{$siteName}"/>
				

                <!--  CSS for OL -->
                <link rel="stylesheet" type="text/css">
                    <xsl:attribute name="href"><xsl:value-of
                        select="$baseUrl" />/apps/js/OpenLayers/theme/default/style.css</xsl:attribute>
                </link>
                
				<!-- CSS for Ext -->
				<link rel="stylesheet" type="text/css">
					<xsl:attribute name="href"><xsl:value-of
						select="$baseUrl" />/apps/js/ext/resources/css/ext-all.css</xsl:attribute>
				</link>
				<link rel="stylesheet" type="text/css">
					<xsl:attribute name="href"><xsl:value-of
						select="$baseUrl" />/apps/js/ext/resources/css/xtheme-gray.css</xsl:attribute>
				</link>

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
						select="$baseUrl" />/apps/html5ui/css/normalize.min.css</xsl:attribute>
				</link>
                <link rel="stylesheet">
                    <xsl:attribute name="href"><xsl:value-of
                        select="$baseUrl" />/apps/html5ui/css/main.css</xsl:attribute>
                </link>
                <link rel="stylesheet">
                    <xsl:attribute name="href"><xsl:value-of
                        select="$baseUrl" />/apps/html5ui/css/colors.css</xsl:attribute>
                </link>
				<link rel="stylesheet">
					<xsl:attribute name="href"><xsl:value-of
						select="$baseUrl" />/apps/html5ui/css/gnmetadatadefault.css</xsl:attribute>
				</link>
				<link rel="stylesheet">
					<xsl:attribute name="href"><xsl:value-of
						select="$baseUrl" />/apps/html5ui/css/gnmetadataview.css</xsl:attribute>
				</link>
				<link rel="stylesheet">
					<xsl:attribute name="href"><xsl:value-of
						select="$baseUrl" />/apps/html5ui/font-awesome/css/font-awesome.css</xsl:attribute>
				</link>



				<!--[if lt IE 7]> <link rel="stylesheet"> <xsl:attribute name="href"><xsl:value-of 
					select="$baseUrl" />/apps/html5ui/css/ltie7.css"/></xsl:attribute> </link> <![endif] -->

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
						select="$baseUrl" />/apps/html5ui/js/vendor/modernizr-2.6.1-respond-1.1.0.min.js</xsl:attribute>
				</script>

				<script type="text/javascript">
					var _gaq = _gaq || [];
					_gaq.push(['_setAccount', 'UA-36263643-1']);
					_gaq.push(['_trackPageview']);

					(function() {
					var ga = document.createElement('script');
					ga.type = 'text/javascript';
					ga.async = true;
					ga.src = ('https:' == document.location.protocol ? 'https://ssl' :
					'http://www') + '.google-analytics.com/ga.js';
					var s = document.getElementsByTagName('script')[0];
					s.parentNode.insertBefore(ga, s);
					})();

				</script>
			</head>
			<body>

			<div class="grey">
					<a href="javascript:window.print();" id="printer-button">Print</a>
					<a id="rss-button" href="/geonetwork/srv/eng/rss.latest">RSS</a>
					<span class="user-button">
						<a id="user-button">
							<xsl:choose>
								<xsl:when test="string(/root/gui/session/userId)=''">
								  <xsl:attribute name="href">javascript:toggleLogin();</xsl:attribute>
									<xsl:value-of select="/root/gui/strings/signIn"/>
							  </xsl:when>
								<xsl:otherwise>
								  <xsl:attribute name="href">javascript:app.loginApp.logout();</xsl:attribute>
									<xsl:value-of select="/root/gui/strings/signOut"/>
							  </xsl:otherwise>
						  </xsl:choose>
						</a>
						<label id="username_label">
							<xsl:if test="string(/root/gui/session/userId)!=''">
								<xsl:value-of select="concat(/root/gui/session/name,' ')"/>
							</xsl:if>
						</label>
						<label id="name_label">
							<xsl:if test="string(/root/gui/session/userId)!=''">
								<xsl:value-of select="concat(/root/gui/session/surname,' ')"/>
							</xsl:if>
						</label>
						<label id="profile_label">
							<xsl:if test="string(/root/gui/session/userId)!=''">
								<xsl:value-of select="concat('(',/root/gui/session/profile,')')"/>	
							</xsl:if>
						</label>
						<a href="javascript:catalogue.admin();" id="administration_button">
							<xsl:if test="string(/root/gui/session/userId)=''">
								<xsl:attribute name="style">display:none;</xsl:attribute>
							</xsl:if>
							<xsl:value-of select="/root/gui/strings/admin"/>
						</a>
						<script>function false_(){ return false; }</script>
						<form id="login-form" style="display: none;" onsubmit="return false_();">
							<div id="login_div">
								<label>User name:</label>
								<input type="text" id="username" name="username"/><br/>
								<label>Password: </label>
								<input type="password" id="password" name="password"/><br/>
								<input type="submit" id="login_button" value="Login"/>
					  	</div>
						</form>
				  </span>
					<a id="help-tab" target="_blank" href="/geonetwork/docs/eng/users">Help</a>
					<a id="lang-button" href="javascript:toggle('lang-form');">
            <xsl:for-each select="/root/gui/config/languages/*">
              <xsl:variable name="lang" select="name(.)"/>
              <xsl:if test="/root/gui/language=$lang">
                <span id="current-lang"><xsl:value-of select="/root/gui/strings/*[name(.)=$lang]"/></span>&#160;<i class="fa fa-angle-double-down"></i>
              </xsl:if>
            </xsl:for-each>
          </a>
					<div id="lang-form" style="display:none;"></div>
			</div>
				
      <div id="page-container">  
				<div id="container">
					<div id="header">
					  <div id="logo"></div>
						<header class="wrapper clearfix">
							<div style="width: 100%; margin: 0 auto;">
								<nav id="nav">
									<ul id="main-navigation">
										<li>
											<a id="browse-tab" class="selected" href="javascript:showBrowse();">
												<xsl:value-of select="/root/gui/strings/home" />
											</a>
										</li>
										<li>
											<a id="catalog-tab" href="javascript:showSearch();">
												<xsl:value-of select="/root/gui/strings/porCatInfoTab" />
											</a>
										</li>
										<li>
											<a id="map-tab" href="javascript:showBigMap();">
												<xsl:value-of select="/root/gui/strings/map_label" />
											</a>
										</li>
										<li>
											<a id="about-tab" href="javascript:showAbout();">
												<xsl:value-of select="/root/gui/strings/about" />
											</a>
										</li>
									</ul>
								</nav>
							</div>
						</header>
					</div>
					
					<div id="main">
			        <div id="copy-clipboard-ie"></div>
                       <div id="share-capabilities" style="display:none">
                            <a id="custom-tweet-button" href="javascript:void(0);" target="_blank">
                                    <xsl:value-of select="/root/gui/strings/tweet" />
                            </a>
                            <div id="fb-button">
                           </div>
                       </div>
                       <div id="permalink-div" style="display:none"></div>
                        <div id="bread-crumb-app"></div>
                        <div id="search-form" style="display:none;">
                            <fieldset id="search-form-fieldset">
                                <legend id="legend-search">
                                    <xsl:value-of select="/root/gui/strings/search" />
                                </legend>
                                <span id='fullTextField'></span>
                                <input type="button"
                                    onclick="Ext.getCmp('advanced-search-options-content-form').fireEvent('search');"
                                    onmouseover="Ext.get(this).addClass('hover');"
                                    onmouseout="Ext.get(this).removeClass('hover');"
                                    id="search-submit" class="form-submit" value="&#xf002;">
                                </input>
                                <div class="form-dummy">
                                    <span><xsl:value-of select="/root/gui/strings/dummySearch" /></span>
	                                <div id="ck1"/>
	                                <div id="ck2"/>
	                                <div id="ck3"/>
                                </div>
                                
                                <div id="show-advanced" onclick="showAdvancedSearch()">
                                    <span class="button"><xsl:value-of select="/root/gui/strings/advancedOptions.show" />&#160;<i class="fa fa-angle-double-down fa-2x show-advanced-icon"></i></span>
                                </div>
                                <div id="hide-advanced" onclick="hideAdvancedSearch(true)" style="display: none;">
                                    <span class="button"><xsl:value-of select="/root/gui/strings/advancedOptions.hide" />&#160;<i class="fa fa-angle-double-up fa-2x hide-advanced-icon"></i></span>
                                </div>
                                <div id="advanced-search-options" >
                                    <div id="advanced-search-options-content"></div>
                                </div>
                            </fieldset>
                        </div>
					

	                    <div id="browser">
                        <aside class="tag-aside">
	                    	  <div id="welcome-text">
	                      	  <xsl:copy-of select="/root/gui/strings/welcome.text"/>
												  </div>
                          <div id="tags">
                            <header><h1><span><xsl:value-of select="/root/gui/strings/tag_label" /></span></h1></header>
                            <div id="cloud-tag"></div>
                          </div>
                        </aside>
                        <article>
                          <div>
                            <section>
                              <div id="latest-metadata">
                                <header><h1><span><xsl:value-of select="/root/gui/strings/latestDatasets" /></span></h1></header>
                              </div>
                              <div id="popular-metadata">
                                <header><h1><span><xsl:value-of select="/root/gui/strings/popularDatasets" /></span></h1></header>
                              </div>
                            </section>
                          </div>
                        </article>
                      </div>

	                    <div id="about" style="display:none;">
	                    	<div id="about-text">
	                      	<xsl:copy-of select="/root/gui/strings/about.text"/>
                        </div>
                      </div>
	                    
						<div id="big-map-container" style="display:none;"/>
                       <div id="metadata-info" style="display:none;"/>
						<div id="search-container" class="main wrapper clearfix">
							<div id="bread-crumb-div"></div>

							<aside id="main-aside" class="main-aside" style="display:none;">
								<header><xsl:value-of select="/root/gui/strings/filter" /></header>
								<div id="facets-panel-div"></div>
							</aside>
							<article>
								<aside id="secondary-aside" class="secondary-aside" style="display:none;">
									<header><xsl:value-of select="/root/gui/strings/recentlyViewed" /></header>
                  <div id="recent-viewed-div"></div>
                  <div id="mini-map"></div>
								</aside>
								<header>
								</header>
								<section>
									<div id="result-panel"></div>
								</section>
								<footer>
								</footer>
							</article>
						</div>
						<!-- .main .wrapper .clearfix -->
					</div>



					<div id="only_for_spiders">
						<xsl:for-each select="/root/*/record">
							<article>
								<xsl:attribute name="id"><xsl:value-of
									select="uuid" /></xsl:attribute>
								<xsl:apply-templates mode="elementEP"
									select="/root/*[name(.)!='gui' and name(.)!='request']">
									<xsl:with-param name="edit" select="false()" />
									<xsl:with-param name="uuid" select="uuid" />
								</xsl:apply-templates>
							</article>
						</xsl:for-each>
					</div>
					<!-- #main -->

					<div id="footer">
            <xsl:if test="/root/gui/config/html5ui-footer!='true'">
              <xsl:attribute name="style">display:none;</xsl:attribute>
            </xsl:if>
						<footer class="wrapper">
							<ul>
								<li style="float:left">
									<xsl:value-of select="/root/gui/strings/poweredBy"/> 
									<a href="http://geonetwork-opensource.org/">GeoNetwork OpenSource</a>
								</li>
								<li>
                                    <a href="http://www.gnu.org/copyleft/gpl.html">GPL</a>
								</li>
							</ul>
						</footer>
					</div>
				</div>

				<input type="hidden" id="x-history-field" />
				<iframe id="x-history-frame" height="0" width="0"></iframe>

                 <xsl:choose>
                     <xsl:when test="/root/gui/config/map/osm_map = 'true'">
                         <script>
                             var useOSMLayers = true;
                         </script>
                     </xsl:when>

                     <xsl:otherwise>
                         <script>
                             var useOSMLayers = false;
                         </script>
                     </xsl:otherwise>
                 </xsl:choose>

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
								select="$baseUrl" />/apps/js/GeoExt-ux/TabCloseMenu/TabCloseMenu.js</xsl:attribute>
						</script>
						
						<script type="text/javascript">
							<xsl:attribute name="src"><xsl:value-of
								select="$baseUrl" />/apps/js/GeoNetwork/lib/GeoNetwork.js</xsl:attribute>
						</script>
						
						<script type="text/javascript">
							<xsl:attribute name="src"><xsl:value-of
								select="$baseUrl" />/apps/html5ui/js/GlobalFunctions.js</xsl:attribute>
						</script>
						<script type="text/javascript">
							<xsl:attribute name="src"><xsl:value-of
								select="$baseUrl" />/apps/html5ui/js/Settings.js</xsl:attribute>
						</script>
						<script type="text/javascript">
							<xsl:attribute name="src"><xsl:value-of
								select="$baseUrl" />/apps/html5ui/js/Templates.js</xsl:attribute>
						</script>
						<script type="text/javascript">
							<xsl:attribute name="src"><xsl:value-of
								select="$baseUrl" />/apps/html5ui/js/Shortcuts.js</xsl:attribute>
						</script>
						<script type="text/javascript">
							<xsl:attribute name="src"><xsl:value-of
								select="$baseUrl" />/apps/html5ui/js/map/Settings.js</xsl:attribute>
						</script>
						<script type="text/javascript">
							<xsl:attribute name="src"><xsl:value-of
								select="$baseUrl" />/apps/html5ui/js/map/MapApp.js</xsl:attribute>
						</script>
						<script type="text/javascript">
							<xsl:attribute name="src"><xsl:value-of
								select="$baseUrl" />/apps/html5ui/js/search/SearchApp.js</xsl:attribute>
						</script>
						<script type="text/javascript">
							<xsl:attribute name="src"><xsl:value-of
								select="$baseUrl" />/apps/html5ui/js/user/LoginApp.js</xsl:attribute>
						</script>
                        <script type="text/javascript">
                            <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/html5ui/js/state/PermalinkProvider.js</xsl:attribute>
                        </script>
                        <script type="text/javascript">
                            <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/html5ui/js/state/History.js</xsl:attribute>
                        </script>
						<script type="text/javascript">
							<xsl:attribute name="src"><xsl:value-of
								select="$baseUrl" />/apps/html5ui/js/BreadCrumb.js</xsl:attribute>
                        </script>
                        <script type="text/javascript">
                            <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/GeoNetwork/lib/GeoNetwork/map/windows/AddWMTS.js</xsl:attribute>
                        </script>
                        <script type="text/javascript">
                            <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/js/GeoNetwork/lib/GeoNetwork/map/widgets/tree/WMTSTreeGenerator.js</xsl:attribute>
                        </script>
                        <script type="text/javascript">
                            <xsl:attribute name="src"><xsl:value-of
                                select="$baseUrl" />/apps/html5ui/js/App.js</xsl:attribute>
						</script>
						
					</xsl:when>
					<xsl:otherwise>
						<script type="text/javascript" src="{concat($baseUrl, '/apps/html5ui/js/App-mini.js')}"></script>
					</xsl:otherwise>
				</xsl:choose>



            </div>
		</body>
	</html>
	</xsl:template>
</xsl:stylesheet>
