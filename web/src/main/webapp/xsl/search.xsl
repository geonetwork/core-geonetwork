<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:exslt="http://exslt.org/common" exclude-result-prefixes="exslt geonet">


	<xsl:include href="metadata/common.xsl" />

	<xsl:output omit-xml-declaration="no" method="html"
		doctype-public="html" indent="yes" encoding="UTF-8" />


	<xsl:variable name="baseUrl">
		<xsl:value-of select="/root/gui/env/server/protocol" />://<xsl:value-of select="/root/gui/env/server/host" />:<xsl:value-of select="/root/gui/env/server/port" /><xsl:value-of select="/root/gui/url" />
	</xsl:variable>

	<!-- main page -->
	<xsl:template match="/">
    <html class="no-js">
           <xsl:attribute name="lang">
                <xsl:value-of select="/root/gui/language" />
            </xsl:attribute>
    
			<head>
				<meta http-equiv="Content-type" content="text/html;charset=UTF-8"></meta>
				<meta http-equiv="X-UA-Compatible" content="IE=Edge,chrome=1"></meta>
				<title>GeoNetwork</title>
				<meta name="description" content="" ></meta>
                <meta name="viewport" content="width=device-width"></meta>
                <meta name="og:title">
                    <xsl:attribute name="content">GeoNetwork</xsl:attribute>
                </meta>
                
				
                <link rel="alternate" type="application/rss+xml"  href="http://www.website.com/rss.xml"
                         title="RSS feed">
                    <xsl:attribute name="href"><xsl:value-of
                        select="$baseUrl" />/srv/<xsl:value-of
                        select="/root/gui/language" />/rss.search</xsl:attribute>
                </link>

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
						select="$baseUrl" />/apps/html5ui/css/gnmetadatadefault.css</xsl:attribute>
				</link>
				<link rel="stylesheet">
					<xsl:attribute name="href"><xsl:value-of
						select="$baseUrl" />/apps/html5ui/css/gnmetadataview.css</xsl:attribute>
				</link>


				<!--[if lt IE 7]> <link rel="stylesheet"> <xsl:attribute name="href"><xsl:value-of 
					select="$baseUrl" />/apps/html5ui/css/ltie7.css"/></xsl:attribute> </link> <![endif] -->

				<script type="text/javascript">
					<xsl:attribute name="src"><xsl:value-of
						select="$baseUrl" />/apps/js/ext/adapter/ext/ext-base-debug.js</xsl:attribute>
				</script>

				<script type="text/javascript">
					<xsl:attribute name="src"><xsl:value-of
						select="$baseUrl" />/apps/js/ext/ext-all-debug.js</xsl:attribute>
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
                <a href="javascript:window.print();" id="printer-button"><xsl:value-of select="/root/gui/strings/print" /></a> 
                <a href="#" onclick="new GeoNetwork.FeedbackForm().show();" id="feedback-site-button">
                    <xsl:value-of select="/root/gui/strings/feedback" />
                </a>
                <a id="rss-button">
                    <xsl:attribute name="href"><xsl:value-of
                        select="$baseUrl" />/srv/<xsl:value-of
                        select="/root/gui/language" />/rss.search</xsl:attribute>
                    RSS
                </a>
               <!--  <a id="sitemap-button">
                    <xsl:attribute name="href"><xsl:value-of
                        select="$baseUrl" />/srv/<xsl:value-of
                        select="/root/gui/language" />/portal.sitemap</xsl:attribute>
                    SiteMap
                </a> -->
                <div id="lang-form"></div>
             </div>
             <div id="page-container">    
				<div id="container">
					<!--[if lt IE 7]> <p class="chromeframe">You are using an outdated browser. 
						<a href="http://browsehappy.com/">Upgrade your browser today</a> or <a href="http://www.google.com/chromeframe/?redirect=true">install 
						Google Chrome Frame</a> to better experience this site.</p> <![endif] -->

					<div id="header">
                       <xsl:variable name="authenticated">
                           <xsl:value-of select="/root/request/user/authenticated" />
                       </xsl:variable>
                        <span class="user-button">
	                        <a id="user-button">                            
	                            <xsl:choose>
	                                <xsl:when test="$authenticated='false'">
	                                    <xsl:attribute name="href">javascript:toggleLogin();</xsl:attribute>
	                                    <xsl:value-of select="/root/gui/strings/login" />
	                                </xsl:when>
	                                <xsl:otherwise>
	                                     <xsl:attribute name="href">javascript:catalogue.logout();</xsl:attribute>
                                         <xsl:value-of select="/root/gui/strings/logout" />
	                                </xsl:otherwise>
                                </xsl:choose>
                            </a>
                            <label id="username_label">
                                <xsl:value-of select="/root/request/user/username" />
                            </label>
                            <label id="name_label">
                                <xsl:choose>
                                    <xsl:when test="$authenticated='true'">
		                                -
		                                <xsl:value-of select="/root/request/user/name" />
                                    </xsl:when>
                                </xsl:choose>
                            </label>
                            <label id="profile_label">
                                <xsl:choose>
                                    <xsl:when test="$authenticated='true'">
		                                (           
		                                <xsl:value-of select="/root/request/user/profile" />
		                                )
                                    </xsl:when>
                                </xsl:choose>
                            </label>
                                
                            <a href="javascript:catalogue.admin();" id="administration_button">
                                                      
                                <xsl:choose>
                                    <xsl:when test="$authenticated='false'">
                                        <xsl:attribute name="style">display:none</xsl:attribute>
                                    </xsl:when>
                                </xsl:choose>
                                
                                <xsl:value-of select="/root/gui/strings/admin" />
                            </a>
                            <script>
                                function false_(){
                                    return false;
                                }
                            </script>
                            <form id="login-form" style="display: none;" onsubmit="return false_();">
                                    <div id="login_div">
                                        <label>
                                            <xsl:value-of select="/root/gui/strings/username" />:
                                        </label>
                                        <input type="text" id="username" name="username" />
                                        <label>
                                            <xsl:value-of select="/root/gui/strings/password" />:
                                        </label>
                                        <input type="password" id="password" name="password" />
                                        <input type="submit" id="login_button">
                                            <xsl:attribute name="value">
                                                <xsl:value-of
                                                select="/root/gui/strings/login" />
                                            </xsl:attribute>
                                        </input>
                                    </div>
                                </form>
                        </span>
						<header class="wrapper clearfix">
							<div style="width: 100%; margin: 0 auto;">
								<nav id="nav">
									<ul id="main-navigation">
										<li>
											<a id="home-tab" href="javascript:loadPDOK('home-tab', 'nl/ngr/ngr-home');">
												<xsl:value-of select="/root/gui/strings/menu.home" />
											</a>
										</li>
										<li>
											<a id="browse-tab" class="selected" href="javascript:showBrowse();">
												<xsl:value-of select="/root/gui/strings/menu.catalog" />
											</a>
										</li>
                                        <li>
                                            <a id="help-tab" target="docs">
                                                <xsl:attribute name="href"><xsl:value-of select="$baseUrl" />/docs/eng/users/</xsl:attribute>
                                                <xsl:value-of select="/root/gui/strings/help" />
                                            </a>
                                        </li>
                                        <li>
                                            <a id="contact-tab" href="javascript:loadPDOK('contact-tab', 'nl/over-pdok/contact-met-pdok');">
                                                <xsl:value-of select="/root/gui/strings/menu.contact" />
                                            </a>
                                        </li>
										<li>
											<a id="about-tab" href="javascript:loadPDOK('about-tab', 'nl/over-pdok');">
												<xsl:value-of select="/root/gui/strings/menu.about" />
											</a>
										</li>
									</ul>
								</nav>
							</div>
						</header>
					</div>
					
                    <div id="pdok-loads" style="display:none;"></div>
                    <div id="foot-loads" style="display:none;"></div>
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
                        <div id="search-form">
                            <fieldset id="search-form-fieldset">
                                <legend id="legend-search">
                                    <xsl:value-of select="/root/gui/strings/search" />
                                </legend>
                                <span id='fullTextField'></span>
                                <input type="button"
                                    onclick="Ext.getCmp('advanced-search-options-content-form').fireEvent('search');"
                                    id="search-submit" class="form-submit">
                                </input>
                                <div class="form-dummy">
                                    <span><xsl:value-of select="/root/gui/strings/dummySearch" /></span>
	                                <div id="ck1"/>
	                                <div id="ck2"/>
	                                <div id="ck3"/>
                                </div>
                                
                                <div id="show-advanced" onclick="showAdvancedSearch()">
                                    <xsl:value-of select="/root/gui/strings/advancedOptions.show" />
                                </div>
                                <div id="hide-advanced" onclick="hideAdvancedSearch(true)" style="display: none;">
                                    <xsl:value-of select="/root/gui/strings/advancedOptions.hide" />
                                </div>
                                <div id="advanced-search-options" >
                                    <div id="advanced-search-options-content"></div>
                                </div>
                            </fieldset>
                        </div>
					

	                    <div id="browser">
	                        <aside class="main-aside">
	                            <div id="welcome-text">
	                               <xsl:copy-of select="/root/gui/strings/welcome.text" /></div>
	                            <div id="cloud-tag"></div>
	                        </aside>
	                        <section>
	                            <div id="latest-metadata"><header>
                                        <h1><xsl:value-of select="/root/gui/strings/latestDatasets" /></h1></header></div>
	                            <div id="popular-metadata"><header>
                                        <h1><xsl:value-of select="/root/gui/strings/popularDatasets" /></h1></header></div>
	                        </section>
	                    </div>
	                    
						<div id="big-map-container" style="display:none;"/>
                       <div id="metadata-info" style="display:none;"/>
						<div id="search-container" class="main wrapper clearfix">
							<div id="bread-crumb-div"></div>

							<aside id="main-aside" class="main-aside" style="display:none;">
								<header>Filter</header>
								<div id="facets-panel-div"></div>
							</aside>
							<article>
								<aside id="secondary-aside" style="display:none;">
                                    <div id="mini-map"></div>
                                    <div id="recent-viewed-div"><h1><xsl:value-of select="/root/gui/strings/recentViewed" /></h1></div>
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
						<footer class="wrapper">
							<ul class="black">
								<li>
									<a href="javascript:loadFoot('copyright')"><xsl:value-of select="/root/gui/strings/footer.copyright" /></a>
								</li>
								<li>
									<a href="javascript:loadFoot('privacy')"><xsl:value-of select="/root/gui/strings/footer.privacy" /></a>
								</li>
								<li>
									<a href="javascript:loadFoot('cookies')"><xsl:value-of select="/root/gui/strings/footer.cookies" /></a>
								</li>
							</ul>
						</footer>
					</div>
				</div>

				<input type="hidden" id="x-history-field" />
				<iframe id="x-history-frame" height="0" width="0"></iframe>

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
