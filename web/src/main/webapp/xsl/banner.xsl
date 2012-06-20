<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:variable name="modal" select="count(/root/gui/config/search/use-modal-box-for-banner-functions)"/>

    <xsl:template name="banner">

        <table width="100%">
            <!-- print banner -->
            <tr id="banner-img1" class="banner doprint" style="display:none;white-space:nowrap">
                <td class="banner" colspan="2" width="100%"><div style="width:1024px">
                    <img src="{/root/gui/url}/images/geocat_logo_li.gif" alt="geocat.ch logo"/>
                    <img src="{/root/gui/url}/images/header-background-print.jpg" alt="geocat.ch logo"/>
                    <img src="{/root/gui/url}/images/bg_kopf_geocat.gif" alt="geocat.ch logo"/>
                </div></td>
            </tr>

            <!-- title -->
            <tr id="banner-img2" class="banner noprint">
                <td class="banner" colspan="2" width="100%">
                    <div style="width:100%; height:103; background-image:url('{/root/gui/url}/images/header-background.jpg');">
                        <img src="{/root/gui/url}/images/bg_kopf_geocat.gif" alt="geocat.ch logo" style="float: right;"/>
                        <img src="{/root/gui/url}/images/geocat_logo_li.gif" alt="geocat.ch logo"/>
                    </div>
                </td>
            </tr>

            <!-- buttons -->
            <tr class="banner noprint">
                <td class="banner-menu">
                    <a class="banner" href="http://www.geocat.ch/geonetwork/srv/{/root/gui/language}/geocat">
                        <xsl:value-of select="/root/gui/strings/nav/home"/>
                    </a> |
                    <a class="banner" href="http://www.geocat.ch/internet/geocat/{/root/gui/strings/language}/tools/sitemap.html">
                        <xsl:value-of select="/root/gui/strings/nav/overview"/>
                    </a> |
                    <xsl:if test="string(/root/gui/config/serverStage) != 'production'">
						<a onclick="javascript:alert('You are not on the production server.  You are using the \'{/root/gui/config/serverStage}\' server.  http://www.geocat.ch is the production/real server')" class="banner"><xsl:value-of select="/root/gui/config/serverStage"/></a> |
					</xsl:if>
                </td>
                <td align="right" class="banner-menu">
                    <xsl:choose>
                        <xsl:when test="/root/gui/language='eng'">
                        </xsl:when>
                        <xsl:otherwise>
                            <a class="banner" href="../eng/geocat"><xsl:value-of select="/root/gui/strings/eng"/></a> |
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:choose>
                        <xsl:when test="/root/gui/language='fra' or /root/gui/language='fre'">
                        </xsl:when>
                        <xsl:otherwise>
                            <a class="banner" href="../fra/geocat"><xsl:value-of select="/root/gui/strings/fre"/></a>
                            <xsl:choose><xsl:when test="not(/root/gui/language='deu' or /root/gui/language='ger')">
                                |
                            </xsl:when></xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:choose>
                        <xsl:when test="/root/gui/language='deu' or /root/gui/language='ger'">
                        </xsl:when>
                        <xsl:otherwise>
                            <a class="banner" href="../deu/geocat"><xsl:value-of select="/root/gui/strings/ger"/></a>
                            <!--<xsl:choose><xsl:when test="not(/root/gui/language='ita')">
                                |
                            </xsl:when></xsl:choose>-->
                        </xsl:otherwise>
                    </xsl:choose>
                    <!--<xsl:choose>
                        <xsl:when test="/root/gui/language='ita'">
                        </xsl:when>
                        <xsl:otherwise>
                            <a class="banner" href="../ita/geocat"><xsl:value-of select="/root/gui/strings/it"/></a>
                        </xsl:otherwise>
                    </xsl:choose>-->
                </td>
            </tr>

            <!-- buttons -->
            <tr class="banner noprint">
                <td class="banner-nav">
                    <table><tr><td class="first">
                        <a href="http://www.geocat.ch/internet/geocat/{/root/gui/strings/language}/home/news.html"><xsl:value-of select="/root/gui/strings/nav/news"/></a>
                    </td><td>
                        <xsl:choose>
                            <xsl:when test="/root/gui/reqService='geocat' or /root/gui/reqService='user.login' or /root/gui/reqService='user.logout'">
                                <a class="banner-active" href="geocat"><xsl:value-of select="/root/gui/strings/nav/metasearch"/></a>
                            </xsl:when>
                            <xsl:otherwise>
                                <a class="banner" href="geocat"><xsl:value-of select="/root/gui/strings/nav/metasearch"/></a>
                            </xsl:otherwise>
                        </xsl:choose>
                    </td><td>
                        <xsl:if test="string(/root/gui/session/userId)!=''">
                            <xsl:choose>
                                <xsl:when test="/root/gui/reqService='admin'">
                                    <a class="banner-active" href="admin"><xsl:value-of select="/root/gui/strings/nav/metainput"/></a>
                                </xsl:when>
                                <xsl:otherwise>
                                    <a class="banner" href="admin"><xsl:value-of select="/root/gui/strings/nav/metainput"/></a>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:if>
                    </td><td>
                        <a href="http://www.geocat.ch/internet/geocat/{/root/gui/strings/language}/home/documentation.html"><xsl:value-of select="/root/gui/strings/nav/doc"/></a>
                    </td><td>
                        <a href="http://www.geocat.ch/internet/geocat/{/root/gui/strings/language}/home/about.html"><xsl:value-of select="/root/gui/strings/nav/about"/></a>
                    </td></tr></table>
                </td>
                <xsl:choose>
                    <xsl:when test="string(/root/gui/session/userId)!=''">
                        <td align="right" class="banner-login">
                            <form name="logout" action="user.logout" method="post">
                                <xsl:value-of select="/root/gui/strings/user"/>
                                <xsl:text>: </xsl:text>
                                <xsl:value-of select="/root/gui/session/name"/>
                                <xsl:text> </xsl:text>
                                <xsl:value-of select="/root/gui/session/surname"/>
                                <xsl:text> </xsl:text>
                                <button id="logoutButton" class="banner" onclick="goSubmit('logout')"><xsl:value-of select="/root/gui/strings/logout"/></button>
                            </form>
                        </td>
                    </xsl:when>
                    <xsl:otherwise>
                        <td align="right" class="banner-login">
                            <form name="login" action="user.login" method="post">
                                <input type="submit" style="display: none;" />
                                <xsl:value-of select="/root/gui/strings/username"/>
                                <input class="banner" type="text" id="username" name="username" size="10" onkeypress="return entSub('login')"/>
                                <xsl:value-of select="/root/gui/strings/password"/>
                                <input class="banner" type="password" id="password" name="password" size="10" onkeypress="return entSub('login')"/>
                                <button id="loginButton" class="banner" onclick="goSubmit('login')"><xsl:value-of select="/root/gui/strings/login"/></button>
                            </form>
                        </td>
                    </xsl:otherwise>
                </xsl:choose>
            </tr>

            <!-- FIXME: should also contain links to last results and metadata -->

            <!-- login
            <tr class="banner">
                <td class="banner-login">
            -->
                    <!-- FIXME
                    <button class="banner" onclick="goSubmit('{/root/gui/service}/es/main.present')">Last search results (11-20 of 73)</button>
                    <a class="banner" href="{/root/gui/service}/es/main.present">Last search results (11-20 of 73)<xsl:value-of select="/root/gui/strings/results"/></a>
                    -->
            <!-- login
                </td>
            </tr>
            -->
        </table>
    </xsl:template>

    <!--
    main html banner in a popup window
    -->
    <xsl:template name="bannerPopup">
        <table width="100%">

            <!-- title -->
            <tr class="banner noprint">
                <td class="banner" colspan="2" width="100%">
                    <div style="width:100%; height:103; background-image:url('{/root/gui/url}/images/header-background.jpg');">
                        <img src="{/root/gui/url}/images/header-cat.jpg" alt="GeoNetwork opensource logo" style="float: right;"/>
                        <img src="{/root/gui/url}/images/header-logo.jpg" alt="World picture"/>
                    </div>
                </td>
            </tr>           

            <!-- buttons -->
            <tr class="banner">
                <td class="banner-menu" colspan="2">
                </td>
            </tr>
        </table>
    </xsl:template>


</xsl:stylesheet>

