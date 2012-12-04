<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:java="java:org.fao.geonet.util.XslUtil"
	exclude-result-prefixes="xsl geonet java">

	<xsl:variable name="mylang" select="/root/gui/language"/>
	<xsl:variable name="mcp" select="/root/gui/schemalist[name='iso19139.mcp']"/>

	<xsl:template name="advanced_search_panel">
		<xsl:param name="remote" select="false()"/>


		<xsl:variable name="formName">
			<xsl:choose>
     		<xsl:when test="$remote">remote_search_form</xsl:when>
      	<xsl:otherwise>advanced_search_form</xsl:otherwise>
    	</xsl:choose>
		</xsl:variable>
			
		<form name="{$formName}" id="{$formName}">
		 <div style="border-bottom: 1px solid;">
			<xsl:comment>ADVANCED SEARCH</xsl:comment>	
			
			<xsl:comment>ADV SEARCH: WHAT?</xsl:comment>
			<xsl:call-template name="adv_what">
				<xsl:with-param name="remote" select="$remote"/>
			</xsl:call-template>
			
			<xsl:comment>ADV SEARCH: WHERE?</xsl:comment>
			<xsl:call-template name="adv_where">
				<xsl:with-param name="remote" select="$remote"/>
			</xsl:call-template>
		
			<xsl:comment>ADV SEARCH: WHEN?/SERVERS?</xsl:comment>
			<xsl:choose>
      	<xsl:when test="$remote">
        	<xsl:call-template name="adv_servers"></xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
        	<xsl:call-template name="adv_when"></xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
		
			<xsl:comment>ADV SEARCH: INSPIRE</xsl:comment>
			<xsl:if test="/root/gui/env/inspire/enable = 'true' and /root/gui/env/inspire/enableSearchPanel = 'true' and not($remote)">
				<xsl:call-template name="adv_inspire"></xsl:call-template>
			</xsl:if>

			<!-- Search button -->
			<div>		
				<table class="advsearchfields" width="100%" border="0" cellspacing="0" cellpadding="0">
					<tr>
						<td style="background: url({/root/gui/url}/images/arrow-bg.gif) repeat-x;" height="29px" width="50%">
						</td>
						<td style="padding:0px; margin:0px;" width="36px">
							<img width="36px" style="padding:0px; margin:0px;"  src="{/root/gui/url}/images/arrow-right.gif" alt="" />
						</td>
						<td style="padding:0px; margin:0px;" width="13px">
							<img width="13px" style="padding:0px; margin:0px;"  src="{/root/gui/url}/images/search-left.gif" alt="" />
						</td>
						<xsl:choose>
							<xsl:when test="$remote">
								<td align="center" style="background: url({/root/gui/url}/images/search-bg.gif) repeat-x; width: auto; white-space: nowrap; padding-bottom: 8px; vertical-align: bottom; cursor:hand;  cursor:pointer;" onclick="runRemoteSearch();" >
									<font color="#FFFFFF"><strong><xsl:value-of select="/root/gui/strings/search"/></strong></font>
								</td>
							</xsl:when>
							<xsl:otherwise>
								<td align="center" style="background: url({/root/gui/url}/images/search-bg.gif) repeat-x; width: auto; white-space: nowrap; padding-bottom: 8px; vertical-align: bottom; cursor:hand;  cursor:pointer;" onclick="runAdvancedSearch();" >
									<font color="#FFFFFF"><strong><xsl:value-of select="/root/gui/strings/search"/></strong></font>
								</td>
							</xsl:otherwise>
						</xsl:choose>
						<td style="padding:0px; margin:0px;" width="12px">
							<img width="12px" style="padding:0px; margin:0px;"  src="{/root/gui/url}/images/search-right.gif" alt="" />
						</td>
					</tr>
				</table>		
			</div>
			
			<!-- Links to Reset fields, Advanced Search and Options panel --> 
			<div style="padding-left:10px;padding-top:5px;" align="right">
				<xsl:choose>
					<xsl:when test="$remote">
						<a onClick="resetRemoteSearch();" style="cursor:pointer; padding-right:10px; padding-left:10px;"><xsl:value-of select="/root/gui/strings/reset"/></a>
					</xsl:when>
					<xsl:otherwise>
						<a onClick="resetAdvancedSearch();" style="cursor:pointer; padding-right:10px; padding-left:10px;"><xsl:value-of select="/root/gui/strings/reset"/></a>
					</xsl:otherwise>
				</xsl:choose>
			</div>
		
			<xsl:if test="not($remote)">
				<div style="padding-left:10px;padding-top:5px;" align="right">
					<a onclick="showFields('restrictions.img','restrictions.table')" style="cursor:pointer;cursor:hand;padding-right:10px;">
						<img id="restrictions.img" src="{/root/gui/url}/images/plus.gif" alt="" />
						<xsl:text> </xsl:text>	
						<xsl:value-of select="/root/gui/strings/restrictTo"/>
					</a>
				
					<a onclick="showFields('advoptions.img','advoptions.table')" style="cursor:pointer;cursor:hand;padding-right:10px;">
						<img id="advoptions.img" src="{/root/gui/url}/images/plus.gif" alt="" />
						<xsl:text> </xsl:text>	
						<xsl:value-of select="/root/gui/strings/options"/>
					</a>
				</div>
			</xsl:if>
			
			<!-- Options panel in advanced search -->
			<div id="advoptions.table" style="display:none; margin-top:5px; margin-bottom:5px">

                <xsl:if test="/root/gui/env/requestedLanguage/ignored = 'false'">
                <!-- language - - - - - - - - - - - - - - - - - - - - -->
                <div class="row" >
                    <span class="labelField">Language</span>
                    <select class="content" name="requestedLanguage" id="requestedLanguage" style="width: 150px"
                            onchange="$('requestedLanguage_simple').value = this.options[this.selectedIndex].value">
                        <option value="">
                            <xsl:value-of select="/root/gui/strings/anyLanguage"/>
                        </option>

                        <xsl:for-each select="/root/gui/isolanguages/record">
                            <xsl:sort select="label/child::*[name() = $lang]" order="ascending"/>
                            <option>
                                <xsl:if test="code = $lang">
                                    <xsl:attribute name="selected">selected</xsl:attribute>
                                </xsl:if>
                                <xsl:attribute name="value">
                                    <xsl:value-of select="code"/>
                                </xsl:attribute>
                                <xsl:value-of select="label/child::*[name() = $lang]"/>
                            </option>
                        </xsl:for-each>
                    </select>
                </div>
                </xsl:if>
			
				<!-- sort by - - - - - - - - - - - - - - - - - - - - -->		
				<div class="row">
					<span class="labelField"><xsl:value-of select="/root/gui/strings/sortBy"/></span>
				  <select id="sortBy" name="sortBy" size="1" class="content" 
						 onChange="if (this.options[this.selectedIndex].value=='title') $('sortOrder').value = 'reverse'; else $('sortOrder').value = ''">
						<xsl:for-each select="/root/gui/strings/sortByType">
							<option value="{@id}">
								<xsl:if test="@id = /root/gui/searchDefaults/sortBy">
									<xsl:attribute name="selected">selected</xsl:attribute>
								</xsl:if>
								<xsl:value-of select="."/>
							</option>
						</xsl:for-each>
					</select>
					<input type="hidden" name="sortOrder" id="sortOrder"/>
				</div>
				
				<!-- hits per page - - - - - - - - - - - - - - - - - - -->
				<div class="row">
					<span class="labelField"><xsl:value-of select="/root/gui/strings/hitsPerPage"/></span>
					<select class="content" id="hitsPerPage" name="hitsPerPage" onchange="$('hitsPerPage_simple').value = this.options[this.selectedIndex].value">
						<xsl:for-each select="/root/gui/strings/hitsPerPageChoice">
						  <xsl:sort select="@value" data-type="number"/>
						  <option>
								<xsl:if
									test="string(@value)=string(/root/gui/searchDefaults/hitsPerPage)">
									<xsl:attribute name="selected">selected</xsl:attribute>
								</xsl:if>
								<xsl:attribute name="value">
									<xsl:value-of select="@value"/>
								</xsl:attribute>
								<xsl:value-of select="."/>
							</option>
						</xsl:for-each>
					</select>
				</div>
				
				<!-- output - - - - - - - - - - - - - - - - - - - - - - -->
				<div class="row">
					<span class="labelField"><xsl:value-of select="/root/gui/strings/output"/></span>

					<select id="output" name="output" size="1" class="content" onchange="$('output_simple').value = this.options[this.selectedIndex].value">
						<xsl:for-each select="/root/gui/strings/outputType">
							<option value="{@id}">
								<xsl:if test="@id = /root/gui/searchDefaults/output">
									<xsl:attribute name="selected">selected</xsl:attribute>
								</xsl:if>
								<xsl:value-of select="."/>
							</option>
						</xsl:for-each>
					</select>
				</div>
			</div>
			
			<!-- Restrictions -->
			<div id="restrictions.table" style="display:none; margin-top:5px; margin-bottom:5px">
				<!-- Source -->
				<div class="row">
					<span class="labelField"><xsl:value-of select="/root/gui/strings/porCatInfoTab"/></span>
					
					<select class="content" name="siteId" id="siteId">
						<option value="">
							<xsl:if test="/root/gui/searchDefaults/siteId=''">
								<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:value-of select="/root/gui/strings/any"/>
						</option>
						<xsl:for-each select="/root/gui/sources/record">
							<!--
								<xsl:sort order="ascending" select="name"/>
							-->
							<xsl:variable name="source" select="siteid/text()"/>
							<xsl:variable name="sourceName" select="name/text()"/>
							<option value="{$source}">
								<xsl:if test="$source=/root/gui/searchDefaults/siteId">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:value-of select="$sourceName"/>
							</option>
						</xsl:for-each>
					</select>
				</div>
				
				<!-- Group -->	
				<xsl:if	test="string(/root/gui/session/userId)!=''">
					<div class="row">
						<span class="labelField"><xsl:value-of select="/root/gui/strings/group"/></span>
						
						<select class="content" name="group" id="group">
							<option value="">
								<xsl:if test="/root/gui/searchDefaults/group=''">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/any"/>
							</option>
							<xsl:for-each select="/root/gui/groups/record">
								<xsl:sort order="ascending" select="name"/>
								<option value="{id}">
									<!-- after a search, many groups are defined in 
									searchDefaults (FIXME ?) and the last group in group list
									was selected by default even if none was
									used in last search. Only set selected one when only one is define in searchDefaults. -->
									<xsl:if test="id=/root/gui/searchDefaults/group and count(/root/gui/searchDefaults/group)=1">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="name"/>
								</option>
							</xsl:for-each>
						</select>
					</div>
				</xsl:if>
					
				<!-- Template -->
				<xsl:if test="string(/root/gui/session/userId)!='' and java:isAccessibleService('metadata.edit')">
					<div class="row">
						<span class="labelField"><xsl:value-of select="/root/gui/strings/kind"/></span>
						
						<select class="content" id="template" name="template" size="1">
							<option value="n">
								<xsl:if test="/root/gui/searchDefaults/template='n'">
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/metadata"/>
							</option>
							<option value="y">
								<xsl:if test="/root/gui/searchDefaults/template='y'">
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/template"/>
							</option>
							<!-- <option value="s">
								<xsl:if test="/root/gui/searchDefaults/template='s'">
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/subtemplate"/>
							</option> -->
						</select>
					</div>
				</xsl:if>
					
				<!-- Category -->
				<xsl:if test="/root/gui/config/category/admin">
					<div class="row">
						<span class="labelField"><xsl:value-of select="/root/gui/strings/category"/></span>
						
						<select class="content" name="category" id="category">
							<option value="">
								<xsl:if test="/root/gui/searchDefaults/category=''">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:value-of select="/root/gui/strings/any"/>
							</option>
							
							<xsl:for-each select="/root/gui/categories/record">
								<xsl:sort select="label/child::*[name() = $mylang]" order="ascending"/>
								
								<option value="{name}">
									<xsl:if test="name = /root/gui/searchDefaults/category">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="label/child::*[name() = $mylang]"/>
								</option>
							</xsl:for-each>
						</select>
					</div>					
				</xsl:if>				

				<!-- Status -->
				<div class="row">
					<span class="labelField"><xsl:value-of select="/root/gui/strings/status"/></span>
						
					<select class="content" name="_status" id="_status">
						<option value="">
							<xsl:if test="/root/gui/searchDefaults/_status=''">
								<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:value-of select="/root/gui/strings/any"/>
						</option>
						
						<xsl:for-each select="/root/gui/status/record">
							<xsl:sort select="label/child::*[name() = $mylang]" order="ascending"/>
							
							<option value="{id}">
								<xsl:if test="id = /root/gui/searchDefaults/_status">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:value-of select="label/child::*[name() = $mylang]"/>
							</option>
						</xsl:for-each>
					</select>
				</div>					

			</div>
			
	 </div>
	</form>					
</xsl:template>

    <!-- ============================================================
        INSPIRE
    ======================================= ===================== -->
<xsl:template name="adv_inspire">
    <h1 style="margin-top:5px;margin-bottom:5px"><a href="#" onclick="toggleInspire()" style="margin-right:2px"><img id="i_inspire" width="9px" height="9px" src="{/root/gui/url}/images/plus.gif" alt="" /></a><xsl:value-of select="/root/gui/strings/inspire/what/l1"/></h1>

    <!-- INSPIRE search elements -->
    <div id="inspiresearchfields" style="display:none">
        <div> <!-- style="float:left;"-->
            <div style="margin-bottom: 10px">  <!-- div row-->
                <input type="checkbox" id="inspire" name="inspire"/><!--Alleen INSPIRE metadata--><xsl:value-of select="/root/gui/strings/inspire/what/l3"/>
            </div>

            <!-- div row-->
            <!--div class="row">
                <span class="labelField"><xsl:value-of select="/root/gui/strings/rtitle"/></span>
                <input type="text" class="content" style="width:200px; !important" id="title" name="title" value=""/>
            </div-->

            <div class="row">  <!-- div row-->
                <span class="labelField"><xsl:value-of select="/root/gui/strings/inspire/what/l6"/></span>
                <select id="inspireannex" name="inspireannex" class="content" style="width:200px; !important" onchange="inspireAnnexChanged(this.value)">
                    <option value="" selected="selected"/>
                    <option value="i"><xsl:value-of select="/root/gui/strings/inspire/what/l6"/> I</option>
                    <option value="ii"><xsl:value-of select="/root/gui/strings/inspire/what/l6"/> II</option>
                    <option value="iii"><xsl:value-of select="/root/gui/strings/inspire/what/l6"/> III</option>
                </select>
            </div>

            <!-- Source type -->
            <div class="row">  <!-- div row-->
                <span class="labelField"><xsl:value-of select="/root/gui/strings/inspire/what/l7"/></span>
                <select id="type" class="content" name="type" style="width:200px; !important" onchange="inspireSourceTypeChanged(this.value)">
                    <option value="" selected="selected"/>
                    <option value="dataset"><!--Datasets en dataset series--><xsl:value-of select="/root/gui/strings/inspire/what/l9"/></option>
                    <option value="service"><!--Services--><xsl:value-of select="/root/gui/strings/inspire/what/l10"/></option>
                </select>
            </div>

            <!--Service type-->
            <div class="row">  <!-- div row-->
                <span class="labelField" id="serviceTypeLabel"><xsl:value-of select="/root/gui/strings/inspire/what/l15"/></span>
                <select id="serviceType" name="serviceType" class="content" style="width:200px; !important">
                   <option value="" ></option>
                   <xsl:for-each select="/root/gui/strings/inspire/serviceType/value">
                       <option value="{@id}">
                           <xsl:if test="@value=/root/gui/searchDefaults/serviceType">
                               <xsl:attribute name="selected"/>
                           </xsl:if>
                           <xsl:value-of select="."/>
                       </option>
                   </xsl:for-each>
                </select>
             </div>
        
            <!-- Classification of spatial data service -->
            <div class="row"> <!-- div row-->
                <input type="hidden" id="keyword"  name="keyword" value="" />

                <span class="labelField" id="classificationDataServiceLabel"><xsl:value-of select="/root/gui/strings/inspire/what/l16"/></span>
                <select id="classificationDataService" class="content" style="width:200px; !important"  onchange="inspireClassificationDataServiceChanged(this.value)">
                    <option value=""></option>
                    <xsl:for-each select="/root/gui/strings/inspire/spatialDataService/group">
                        <optgroup label="{@label}">
                        <xsl:for-each select="value">
                        <option value="{@id}">
                            <xsl:if test="@value=/root/gui/searchDefaults/spatialDataService">
                                <xsl:attribute name="selected"/>
                            </xsl:if>
                           <xsl:value-of select="."/>
                        </option>
                        </xsl:for-each>
                        </optgroup>
                    </xsl:for-each>
                </select>
            </div>


            <!-- INSPIRE Thema -->
             <div>                           <!-- style="float:left; margin-left: 20px;"-->
                <fieldset>
                    <legend><!--INSPIRE Thema--><xsl:value-of select="/root/gui/strings/inspire/what/l14"/></legend>
                    <div id="inspirethemesdiv">
                       <div> <!--style="max-height:170px;height:170px;overflow:auto;"-->
                            <div class="inspireThemeTitle"><xsl:value-of select="/root/gui/strings/inspire/what/l6"/> I</div>

                            <xsl:for-each select="/root/gui/strings/inspire/annex1/theme">
                               <xsl:sort select="." />
                                <div class="inspireThemeElement">
                               <input type="checkbox" value="{.}" name="inspiretheme" id="{@id}"/>

                               <span>
                                   <label for="{@id}"><xsl:value-of select="."/></label>
                               </span>
                               </div>
                            </xsl:for-each>

                            <div class="inspireThemeTitle"><xsl:value-of select="/root/gui/strings/inspire/what/l6"/> II</div>

                            <xsl:for-each select="/root/gui/strings/inspire/annex2/theme">
                                <xsl:sort select="." />

                            <div class="inspireThemeElement">
                               <input type="checkbox" value="{.}" name="inspiretheme" id="{@id}"/>

                               <span>
                                   <label for="{@id}"><xsl:value-of select="."/></label>
                               </span>
                            </div>
                            </xsl:for-each>

                            <div class="inspireThemeTitle"><xsl:value-of select="/root/gui/strings/inspire/what/l6"/> III</div>

                            <xsl:for-each select="/root/gui/strings/inspire/annex3/theme">
                               <xsl:sort select="." />
                            <div class="inspireThemeElement">
                            <input type="checkbox" value="{.}" name="inspiretheme" id="{@id}"/>
                            <span>
                            <label for="{@id}"><xsl:value-of select="."/></label>
                            </span>
                            </div>
                            </xsl:for-each>
                       </div>
                    </div>
                </fieldset>
            </div>
        </div>
    </div>
       <!-- end INSPIRE search elements -->
</xsl:template>
     
	<!-- ============================================================ 
		WHAT
	======================================= ===================== -->
	
<xsl:template name="adv_what">
	<xsl:param name="remote"/>

	<h1 style="margin-bottom:5px"><xsl:value-of select="/root/gui/strings/what"/></h1>
	
	<!-- Any Of The Words -->	
	<div class="row">  <!-- div row-->
		<span class="labelField"><xsl:value-of select="/root/gui/strings/anyWith"/></span>
		<input name="or" id="or" class="content" size="31" value=""/>
		<xsl:if test="not($remote)">
			<a href="#" onclick="toggleMoreFields()" style="margin-left:2px"><img id="i_morefields" width="9px" height="9px" src="{/root/gui/url}/images/plus.gif" title="{/root/gui/strings/showMoreSearchFields}" alt="{/root/gui/strings/showMoreSearchFields}"/></a>
		</xsl:if>
	</div>
	
	<!-- Exact Phrase -->
    <div class="row" id="phrase_search_row" style="display:none">  <!-- div row-->
		<span class="labelField"><xsl:value-of select="/root/gui/strings/anyWithExactPhrase"/></span>
		<input name="phrase" id="phrase" class="content" size="31" value=""/>
	</div>
	
	<!-- All Text -->
    <div class="row" id="all_search_row" style="display:none">  <!-- div row-->
		<span class="labelField"><xsl:value-of select="/root/gui/strings/anyWithAllWords"/></span>
		<input name="all" id="all" class="content" size="31" value=""/>
	</div>
	
	<!-- Without Words -->
	<div class="row" id="without_search_row" style="display:none">  <!-- div row-->
		<span class="labelField"><xsl:value-of select="/root/gui/strings/anyWithoutWords"/></span>
		<input name="without" id="without" class="content" size="31" value=""/>
	</div>
	
	<!-- Title -->	
	<div class="row">  <!-- div row-->
		<span class="labelField"><xsl:value-of select="/root/gui/strings/rtitle"/></span>
		<span title="{/root/gui/strings/searchhelp/rtitle}">
			<input name="title" id="title" class="content"  size="31" value="{/root/gui/searchDefaults/title}"/>
		</span>
		<xsl:if test="not($remote)">
			<div id="titleList" class="keywordList">
				<!-- the titleList for autocompletion will show here -->
			</div>
		</xsl:if>
	</div>
	
	<!-- Abstract -->	
	<div class="row">  <!-- div row-->
		<span class="labelField"><xsl:value-of select="/root/gui/strings/abstract"/></span>
		<span title="{/root/gui/strings/searchhelp/abstract}">
			<input name="abstract" id="abstract" class="content"  size="31" value="{/root/gui/searchDefaults/abstract}"/>
		</span>
	</div>

	<!-- Keywords -->	
	<div class="row">  <!-- div row-->
		<span class="labelField"><xsl:value-of select="/root/gui/strings/keywords"/></span>
		<span title="{/root/gui/strings/searchhelp/keywords}">
			<xsl:choose>
				<xsl:when test="not($remote)">
				  <input id="themekey" name="themekey" onClick="popSelector(this,'keywordSelectorFrame','keywordSelector','portal.search.keywords?mode=selector&amp;keyword','themekey');" class="content" size="31" value="{/root/gui/searchDefaults/themekey}"/>
				</xsl:when>
				<xsl:otherwise>
				  <input id="themekey" name="themekey" class="content" size="31" value="{/root/gui/searchDefaults/themekey}" />
				</xsl:otherwise>
			</xsl:choose>
		</span>

		<xsl:if test="/root/gui/config/search/keyword-selection-panel and not($remote)">
			<a style="cursor:pointer;" onclick="javascript:showSearchKeywordSelectionPanel();">
				<img src="{/root/gui/url}/images/find.png" alt="{/root/gui/strings/searchhelp/thesaurus}" title="{/root/gui/strings/searchhelp/thesaurus}"/>
			</a>
		</xsl:if>
		
		<div id="keywordSelectorFrame" class="keywordSelectorFrame" style="display:none;z-index:1000;">
			<div id="keywordSelector" class="keywordSelector"/>
		</div>
		
		<div id="keywordList" class="keywordList"/>
	</div>

	<xsl:if test="not($remote)">
		<!-- MCP fields -->
		<xsl:call-template name="adv_mcp"/>
	
		<!-- Map type -->
		<div class="row">  <!-- div row-->
			<a onclick="showFields('maptype.img','maptype.table')" style="cursor:pointer;cursor:hand;">
				<img id="maptype.img" src="{/root/gui/url}/images/plus.gif" alt="" />
				<xsl:text> </xsl:text>	
				<xsl:value-of select="/root/gui/strings/mapType"/>
			</a>
		
			<table id="maptype.table" style="display:none;border-color:#2a628f;border-style:solid;width:80%;margin:5px;margin-left:15px">
				<tr>
					<td>
					<input name="digital" id="digital" type="checkbox" value="true">
						<xsl:if test="/root/gui/searchDefaults/digital='true'">
							<xsl:attribute name="checked">CHECKED</xsl:attribute>
						</xsl:if>
						<label for="digital"><xsl:value-of select="/root/gui/strings/digital"/></label>
					</input>
					<br/>
					<input name="paper" id="paper" type="checkbox" value="true">
						<xsl:if test="/root/gui/searchDefaults/paper='true'">
							<xsl:attribute name="checked">CHECKED</xsl:attribute>
						</xsl:if>
						<label for="paper"><xsl:value-of select="/root/gui/strings/paper"/></label>
					</input>
				</td>
				<td>
					<input name="dynamic" id="dynamic" type="checkbox" value="true">
					  <xsl:if test="/root/gui/searchDefaults/dynamic='true'">
						<xsl:attribute name="checked">CHECKED</xsl:attribute>
					</xsl:if>
						<label for="dynamic"><xsl:value-of select="/root/gui/strings/dynamic"/></label>
					</input>
					<br/>
					<input name="download" id="download" type="checkbox" value="true">
						<xsl:if test="/root/gui/searchDefaults/download='true'">
							<xsl:attribute name="checked">CHECKED</xsl:attribute>
						</xsl:if>
						<label for="download"><xsl:value-of select="/root/gui/strings/downloadable"/></label>
					</input>
					</td>
				</tr>
			</table>
		</div>

		<!-- Fuzzy search -->	
		<div class="row">
			<a onclick="showFields('fuzzy.img','fuzzy.td')" style="cursor:pointer;cursor:hand;">
				<img id="fuzzy.img" src="{/root/gui/url}/images/plus.gif" alt="" />
				<xsl:text> </xsl:text>	
				<xsl:value-of select="/root/gui/strings/fuzzy"/>
			</a>
			<table id="fuzzy.td" style="display:none;border-color:#2a628f;border-style:solid;margin:5px;margin-left:10px">
				<tr>
				<td>
					<xsl:value-of select="/root/gui/strings/fuzzyPrecise"/> <input type="radio" id="similarity1" name="similarity" value="1" >
						<xsl:if test="/root/gui/searchDefaults/similarity='1'">
								<xsl:attribute name="checked">CHECKED</xsl:attribute>
						</xsl:if>
					</input>
					<input type="radio" id="similarity08" name="similarity" value=".8">
						<xsl:if test="/root/gui/searchDefaults/similarity='.8'">
								<xsl:attribute name="checked">CHECKED</xsl:attribute>
						</xsl:if>
					</input>
					<input type="radio" id="similarity06" name="similarity" value=".6" >
						<xsl:if test="/root/gui/searchDefaults/similarity='.6'">
								<xsl:attribute name="checked">CHECKED</xsl:attribute>
						</xsl:if>
					</input>
	
					<input type="radio" id="similarity04" name="similarity" value=".4" >
						<xsl:if test="/root/gui/searchDefaults/similarity='.4'">
								<xsl:attribute name="checked">CHECKED</xsl:attribute>
						</xsl:if>
					</input> 
					<input type="radio" id="similarity02" name="similarity" value=".2" >
						<xsl:if test="/root/gui/searchDefaults/similarity='.2'">
								<xsl:attribute name="checked">CHECKED</xsl:attribute>
						</xsl:if>
					</input><xsl:value-of select="/root/gui/strings/fuzzyImprecise"/>
				</td>
				</tr>
			</table>	
	
		</div>

	</xsl:if>


</xsl:template>
	
	
	<!-- ============================================================ 
		WHERE
	============================================================= -->
	
<xsl:template name="adv_where">
	<xsl:param name="remote"/>

	<h1 style="margin-bottom:5px"><xsl:value-of select="/root/gui/strings/where"/></h1>
	
	<xsl:comment>MINIMAP</xsl:comment>
	
	<xsl:variable name="regionSelect">
		<xsl:choose>
			<xsl:when test="$remote">region_remote</xsl:when>
			<xsl:otherwise>region</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
			
	<xsl:variable name="eastBL">
		<xsl:choose>
			<xsl:when test="$remote">eastBL_remote</xsl:when>
			<xsl:otherwise>eastBL</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
			
	<xsl:variable name="westBL">
		<xsl:choose>
			<xsl:when test="$remote">westBL_remote</xsl:when>
			<xsl:otherwise>westBL</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
			
	<xsl:variable name="northBL">
		<xsl:choose>
			<xsl:when test="$remote">northBL_remote</xsl:when>
			<xsl:otherwise>northBL</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
			
	<xsl:variable name="southBL">
		<xsl:choose>
			<xsl:when test="$remote">southBL_remote</xsl:when>
			<xsl:otherwise>southBL</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

	<!-- Map and coordinates container -->
	<table id="minimap_root" width="340px">
		<!-- North -->
		<tr>
			<td colspan="3" align="center" style="padding: 3px;">
				<small><xsl:value-of select="/root/gui/strings/latMax"/></small> <input type="text" class="content" id="{$northBL}" name="northBL"  size="5"
					value="{/root/gui/searchDefaults/northBL}" onChange="javascript:AoIrefresh();"
					alt="{/root/gui/strings/latitude}" title="{/root/gui/strings/latitude}"/>
			</td>
			
		</tr>
	
		<tr>
		
			<td width="52px" style="padding-top: 25px; align: center;">
				<small><xsl:value-of select="/root/gui/strings/longMin"/></small>
				<br />
				<input type="text" class="content" id="{$westBL}" name="westBL" size="5"
					value="{/root/gui/searchDefaults/westBL}" onChange="javascript:AoIrefresh();"
					alt="{/root/gui/strings/longitude}" title="{/root/gui/strings/longitude}"/>
			</td>
	
			<td style="padding: 3px;">
				<xsl:choose>
					<xsl:when test="not($remote)">
						<div id="ol_minimap2" />
					</xsl:when>
					<xsl:otherwise>
						<div id="ol_minimap3" />
					</xsl:otherwise>
				</xsl:choose>
			</td>
		
			<td width="52px" style="padding-top: 25px; align: center;">
				<small><xsl:value-of select="/root/gui/strings/longMax"/></small>
				<br />
				<input type="text" class="content" id="{$eastBL}" name="eastBL" size="5"
					value="{/root/gui/searchDefaults/eastBL}" onChange="javascript:AoIrefresh();"
					alt="{/root/gui/strings/longitude}" title="{/root/gui/strings/longitude}"/>
			</td>
		</tr>
	
		<tr>
			<td />
			<td colspan="2" align="center" style="padding: 3px;">
				<small><xsl:value-of select="/root/gui/strings/latMin"/></small> <input type="text" class="content" id="{$southBL}" name="southBL" size="5"
					value="{/root/gui/searchDefaults/southBL}" onChange="javascript:AoIrefresh();"
					alt="{/root/gui/strings/latitude}" title="{/root/gui/strings/latitude}"/>
			</td>
		</tr>
	</table>
			
	<!-- Bounding box relation -->
	<xsl:variable name="relationSelect">
		<xsl:choose>
			<xsl:when test="$remote">relation_remote</xsl:when>
			<xsl:otherwise>relation</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
			
	<div class="row">
		<span class="labelField"><xsl:value-of select="/root/gui/strings/type"/></span>
		<select class="content" name="relation" id="{$relationSelect}">
			<xsl:for-each select="/root/gui/strings/boundingRelation">
				<option value="{@value}">
					<xsl:if
						test="@value=/root/gui/searchDefaults/relation">
						<xsl:attribute name="selected">selected</xsl:attribute>
					</xsl:if>
					<xsl:value-of select="."/>
				</option>
			</xsl:for-each>
		</select>
	</div>

	<!-- Region -->
	<div class="row">
		<span class="labelField"><xsl:value-of select="/root/gui/strings/region"/></span>
		<select class="content" name="region" id="{$regionSelect}" onchange="doRegionSearch(this.id);">
			<option value="">
				<xsl:if test="/root/gui/searchDefaults/theme='_any_'">
					<xsl:attribute name="selected">selected</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="/root/gui/strings/any"/>
			</option>
			<option value="userdefined">
				<xsl:if test="/root/gui/searchDefaults/theme='_userdefined_'">
					<xsl:attribute name="selected">selected</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="/root/gui/strings/userDefined"/>
			</option>
			
			<xsl:for-each select="/root/gui/regions/record">
				<xsl:sort select="label/child::*[name() = $mylang]" order="ascending"/>
				<option value="{id}">
					<xsl:if test="id=/root/gui/searchDefaults/region">
						<xsl:attribute name="selected">selected</xsl:attribute>
					</xsl:if>
					<xsl:attribute name="value">
							<xsl:value-of select="id"/>
					</xsl:attribute>
					<xsl:value-of select="label/child::*[name() = $mylang]"/>
				</option>
			</xsl:for-each>
		</select>							
	</div>

</xsl:template>

	<!-- ============================================================ 
		WHEN
	============================================================= -->

<xsl:template name="adv_when">
	<h1 style="margin-top:5px;margin-bottom:5px"><a href="#" onclick="toggleWhen()" style="margin-right:2px"><img id="i_when" width="9px" height="9px" src="{/root/gui/url}/images/plus.gif" alt=""/></a><xsl:value-of select="/root/gui/strings//when"/></h1>
	
	<div id="whensearchfields" style="display:none">
		<div class="row">
			<input onclick="setDates(0);" value="" name="radfrom" id="radfrom0" type="radio">
					<xsl:if test="string(/root/gui/searchDefaults/dateFrom)='' and string(/root/gui/searchDefaults/dateTo)=''
							and string(/root/gui/searchDefaults/extFrom)='' and string(/root/gui/searchDefaults/extTo)=''">
						<xsl:attribute name="checked">CHECKED</xsl:attribute>
 					</xsl:if>
					<label for="radfrom0"><xsl:value-of select="/root/gui/strings/anytime"/></label>
			</input>
		</div>
		
		<div class="row">
			<input value="" name="radfrom" id="radfrom1" type="radio" disabled="disabled">
					<xsl:if test="string(/root/gui/searchDefaults/dateFrom)!='' and string(/root/gui/searchDefaults/dateTo)!=''">
						<xsl:attribute name="checked">CHECKED</xsl:attribute>
					</xsl:if>
					<label for="radfrom1"><xsl:value-of select="/root/gui/strings/changeDate"/></label>
			</input>
		</div>
		
	      <!-- Change format to %Y-%m-%dT%H:%M:00 in order to have DateTime field instead of DateField -->
		  <table>
		      <tr>
		          <td><xsl:value-of select="/root/gui/strings/from"/></td>
		          <td>
		            <div class="cal" id="dateFrom" onclick="$('radfrom1').checked=true;$('radfrom1').disabled='';$('radfromext1').disabled='disabled';"></div>
		            <input type="hidden" id="dateFrom_format" value="%Y-%m-%d"/>
		            <input type="hidden" id="dateFrom_cal" value=""/>
		          </td>
		      </tr>
		      <tr>
		          <td><xsl:value-of select="/root/gui/strings/to"/></td>
		          <td>
			        <div class="cal" id="dateTo" onclick="$('radfrom1').checked=true;$('radfrom1').disabled='';$('radfromext1').disabled='disabled';"></div>
		            <input type="hidden" id="dateTo_format" value="%Y-%m-%d"/>
		            <input type="hidden" id="dateTo_cal" value=""/>
		          </td>
		      </tr>
		  </table>
	         
		
		<div class="row">
			<input value="" name="radfrom" id="radfromext1" type="radio" disabled="disabled">
					<xsl:if test="string(/root/gui/searchDefaults/extFrom)!='' and string(/root/gui/searchDefaults/extTo)!=''">
						<xsl:attribute name="checked" />
					</xsl:if>
					<label for="radfromext1"><xsl:value-of select="/root/gui/strings/datasetIssued"/></label>
			</input>
		</div>
		
		 <!-- Change format to %Y-%m-%dT%H:%M:00 in order to have DateTime field instead of DateField -->
          <table>
              <tr>
                  <td><xsl:value-of select="/root/gui/strings/from"/></td>
                  <td>
                    <div class="cal" id="extFrom" onclick="$('radfromext1').checked=true;$('radfromext1').disabled='';$('radfrom1').disabled='disabled';"></div>
		            <input type="hidden" id="extFrom_format" value="%Y-%m-%d"/>
		            <input type="hidden" id="extFrom_cal" value=""/>
                  </td>
              </tr>
              <tr>
                  <td><xsl:value-of select="/root/gui/strings/to"/></td>
                  <td>
		            <div class="cal" id="extTo" onclick="$('radfromext1').checked=true;$('radfromext1').disabled='';$('radfrom1').disabled='disabled';"></div>
		            <input type="hidden" id="extTo_format" value="%Y-%m-%d"/>
		            <input type="hidden" id="extTo_cal" value=""/>
                  </td>
              </tr>
          </table>
		
	</div>


	<!-- restrict to - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<!-- now make sure we open expanded if any restrictions are selected -->
	<xsl:if test="/root/gui/searchDefaults/siteId!='' or
				  /root/gui/searchDefaults/groups/group!='' or
				  /root/gui/searchDefaults/ownergroups='on' or
	              /root/gui/searchDefaults/owner='on' or
	              /root/gui/searchDefaults/notgroups='on' or
 				  ( /root/gui/searchDefaults/template!='' and /root/gui/searchDefaults/template!='n' ) or
				  /root/gui/searchDefaults/category!=''">
		<script type="text/javascript">
			showFields('restrictions.img','restrictions.table');
		</script>
	</xsl:if>

	<!-- options - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<!-- now make sure we open expanded if any options are selected -->
	<xsl:if test="/root/gui/searchDefaults/sortBy!='relevance' or
				  /root/gui/searchDefaults/hitsPerPage!='10' or
				  /root/gui/searchDefaults/output!='full'">
		<script type="text/javascript">
			showFields('advoptions.img','advoptions.fieldset');
		</script>
	</xsl:if>
</xsl:template>
	

<!-- Remote Search - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

<xsl:template name="adv_servers">
	<h1 style="margin-bottom:5px"><xsl:value-of select="concat(/root/gui/strings/from,'?')"/></h1>
	
	<xsl:comment>FROM REMOTE SERVER</xsl:comment>
	
	<!-- Profiles and servers -->
	<div class="row">
		<span class="labelField"><xsl:value-of select="/root/gui/strings/profile"/></span>
		<select class="content" id="profile" name="profile" onchange="profileSelected()">
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
	</div>

	<div class="row">
		<span class="labelField"><xsl:value-of select="/root/gui/strings/server"/></span>
		<select class="content" id="servers" name="servers" size="6" multiple="true">
			<xsl:for-each select="/root/gui/repositories/z3950repositories/repository">
				<xsl:sort select="label" order="ascending"/>
				<xsl:variable name="name" select="id"/>
				<xsl:variable name="description" select="label"/>
				<option>
					<xsl:if test="/root/gui/searchDefaults/servers/server[string(.)=$name]">
						<xsl:attribute name="selected"/>
					</xsl:if>
					<xsl:attribute name="value"><xsl:value-of select="$name"/></xsl:attribute>
					<xsl:value-of select="$description"/>
				</option>
			</xsl:for-each>
		</select>
	</div>
				
	<!-- timeout -->
	<div class="row">
		<span class="labelField"><xsl:apply-templates select="/root/gui/strings/timeout" mode="caption"/></span>
		<select class="content" id="timeout" name="timeout">
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
	</div>

	<!-- server generated html -->
  <div class="row">
		<span class="labelField"><xsl:value-of select="/root/gui/strings/displayRemoteHtml"/></span>
		<input id="serverhtml" name="serverhtml" type="checkbox">
			<xsl:if test="/root/gui/searchDefaults/serverhtml='on'">
				<xsl:attribute name="checked"/>
			</xsl:if>
		</input>
	</div>
			
	<!-- hits per page -->
	<div class="row">
		<span class="labelField"><xsl:value-of select="/root/gui/strings/hitsPerPage"/></span>
		<select class="content" id="hitsPerPage" name="hitsPerPage">
			<xsl:for-each select="/root/gui/strings/hitsPerPageChoice">
			  <xsl:sort select="@value" data-type="number"/>
				<option>
				  <xsl:if test="string(@value)=string(/root/gui/searchDefaults/hitsPerPage)">
						<xsl:attribute name="selected"/>
					</xsl:if>
					<xsl:attribute name="value"><xsl:value-of select="@value"/></xsl:attribute>
					<xsl:value-of select="."/>
				</option>
			</xsl:for-each>
		</select>
	</div>
</xsl:template>


<xsl:template name="adv_mcp">
	<xsl:if test="$mcp">
		<!-- Credit (MCP only) -->	
		<div class="row">
			<span class="labelField"><xsl:value-of select="/root/gui/schemas/iso19139.mcp/strings/credit"/></span>
			<span title="{/root/gui/schemas/iso19139.mcp/strings/creditHelp}">
				<input name="credit" id="credit" class="content"  size="31" value="{/root/gui/searchDefaults/credit}" onclick="popSelector(this,'creditSelectorFrame','creditSelector','portal.search.credits?mode=selector&amp;credit','credit');" />
			</span>
			<div id="creditSelectorFrame" class="keywordSelectorFrame" style="display:none;z-index:1000;">
				<div id="creditSelector" class="keywordSelector"/>
			</div>
		</div>
			
		<!-- Data Parameter (MCP only) -->
		<div class="row">
			<span class="labelField"><xsl:value-of select="/root/gui/schemas/iso19139.mcp/strings/dataparam"/></span>
			<span title="{/root/gui/schemas/iso19139.mcp/strings/dataparamHelp}">
				<input name="dataparam" id="dataparam" class="content"  size="31" value="{/root/gui/searchDefaults/dataparam}" onclick="popSelector(this,'dataparamSelectorFrame','dataparamSelector','portal.search.dataparams?mode=selector&amp;dataparam','dataparam');"/>
			</span>

			<div id="dataparamSelectorFrame" class="keywordSelectorFrame" style="display:none;z-index:1000;">
				<div id="dataparamSelector" class="keywordSelector"/>
			</div>
		</div>

		<!-- Taxonomic info (MCP only) -->
		<div class="row">
			<span class="labelField"><xsl:value-of select="/root/gui/schemas/iso19139.mcp/strings/taxon"/></span>
			<span title="{/root/gui/schemas/iso19139.mcp/strings/taxonHelp}">
				<input name="taxon" id="taxon" class="content"  size="31" value="{/root/gui/searchDefaults/taxon}" onclick="popSelector(this,'taxonSelectorFrame','taxonSelector','portal.search.taxonNames?mode=selector&amp;taxon','taxon');"/>
			</span>

			<div id="taxonSelectorFrame" class="keywordSelectorFrame" style="display:none;z-index:1000;">
				<div id="taxonSelector" class="keywordSelector"/>
			</div>
		</div>
	</xsl:if>
</xsl:template>

</xsl:stylesheet>
