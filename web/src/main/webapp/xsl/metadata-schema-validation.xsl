<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sc="scaling">

	<xsl:include href="banner.xsl"/>

	<!-- page content -->
	<xsl:template match="/">
	<html lang="en" id="ng-app" data-ng-app="SharedObjects">
		<head>
		    <meta charset="utf-8"/>
		    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
		    <title>Shared Object Admin</title>
		    <link rel="stylesheet" href="../../apps/shared-objects/app/lib/bootstrap3/css/bootstrap.css" />
		    <link rel="stylesheet" href="../../geocat.css"/>
		</head>
		<body>
			<div id="header">
				<xsl:call-template name="banner"/>
			</div>
			
			<h3>
				<xsl:value-of select="/root/gui/strings/metadataschemaValidate" />
			</h3>
			<p>
				<xsl:value-of select="/root/gui/strings/metadataschemaValidateDes" />
			</p>
	
			<div class="metadataValidation">
				<form method="post" name="criteria">
					<input type="hidden" name="action" value="add" />
					<div>
						<label>
							<xsl:value-of select="/root/gui/strings/xpathschematron" />
							:
						</label>
						
						<select name="schematron" autofocus="autofocus" required="true">
							<xsl:for-each select="/root/schematron/schematron">
								<option value="id">
									[
									<xsl:value-of select="isoschema" />
									]
									<xsl:value-of select="tokenize(file,'/')[last()]" />
								</option>
							</xsl:for-each>
						</select>
					</div>
					<div>
						<label>
							<xsl:value-of select="/root/gui/strings/xpathlabel" />
							:
						</label>
						<select name="type" id="xpathtype" placeholder="{/root/gui/strings/xpathlabel}"
							required="true">
							<option value="KEYWORD">
								<xsl:value-of select="/root/gui/strings/capitalKeyword" />
							</option>
							<option value="GROUP">
								<xsl:value-of select="/root/gui/strings/group" />
							</option>
						</select>
					</div>
					<div>
						<label>
							<xsl:value-of select="/root/gui/strings/xpathvalue" />
							:
						</label>
						<input style="display:none" id="xpath" type="text" name="value"
							required="true" placeholder="{/root/gui/strings/xpathvalue}" />
						<input type="text" id="keyword" placeholder="{/root/gui/strings/xpathvalue}" ng-controller="KeywordControl" />
						<input style="display:none" type="text" id="group"
							placeholder="{/root/gui/strings/xpathvalue}" />
					</div>
					<input type="submit" onclick="checkErrors()" />
				</form>
	
				<script type="text/javascript">
					function checkErrors() {
						if(criteria.keyword.value == '') {
							jQuery(criteria.keyword).addClass("error");
						}
						if(criteria.group.value == '') {
							jQuery(criteria.group).addClass("error");
						}
					}
				</script>
	
				<div class="xpath list">
	
					<xsl:if test="count(/root/schematron/criteria) > 0">
						<table>
							<tr>
								<th>
									<xsl:value-of select="/root/gui/strings/xpathrequired" />
								</th>
								<th>
									<xsl:value-of select="/root/gui/strings/xpathschematron" />
								</th>
								<th>
									<xsl:value-of select="/root/gui/strings/xpathlabel" />
								</th>
								<th>
									<xsl:value-of select="/root/gui/strings/xpathvalue" />
								</th>
								<td>
								</td>
							</tr>
							<xsl:for-each select="/root/schematron/criteria">
								<xsl:variable name="newSchema"
									select="
										not(string(preceding-sibling::criteria[1]/schematron/@file) != '') 
										or string(preceding-sibling::criteria[1]/schematron/@file) != string(schematron/@file)" />
								<xsl:variable name="schematron_">
									<xsl:if test="$newSchema">
										[
										<xsl:value-of select="schematron/@isoschema" />
										]
										<xsl:value-of select="tokenize(schematron/@file,'/')[last()]" />
									</xsl:if>
								</xsl:variable>
								<tr>
									<xsl:if test="$newSchema">
										<xsl:attribute name="class">newschema</xsl:attribute>
									</xsl:if>
									<td>
										<xsl:if test="$newSchema">
											<xsl:choose>
												<xsl:when test="schematron/@required = 'true'">
													<span class="required" />
												</xsl:when>
												<xsl:otherwise>
													<span class="optional" />
												</xsl:otherwise>
											</xsl:choose>
										</xsl:if>
									</td>
									<td>
										<xsl:value-of select="$schematron_" />
									</td>
									<td>
										<xsl:value-of select="@type" />
									</td>
									<td>
										<xsl:value-of select="@value" />
									</td>
									<td>
										<a class="remove" href="#">
											<xsl:attribute name="onclick">javascript:removeItem(<xsl:value-of
												select="@id" />)</xsl:attribute>
										</a>
									</td>
								</tr>
							</xsl:for-each>
						</table>
					</xsl:if>
				</div>
			</div>
	
	
			<link rel="stylesheet" href="../../apps/shared-objects/app/lib/bootstrap3/css/bootstrap.css" />
			<link rel="stylesheet" href="../../apps/shared-objects/app/css/app.css" />
			<div data-ng-view="">
			</div>
	
	
			<script src="../../apps/shared-objects/app/lib/jquery.js"></script>
			<script id="load-language-script" type="text/javascript">
				changePageLanguage =
					function(lang) {
						window.location.search='?lang='+lang;
					};
		
					var languageHolder='eng';
					var init = function () {
						try {
						var matches = window.location.search.match(/lang=(\w\w\w)/);
						if (matches.length == 2) {
							languageHolder = matches[1];
						}
			
						if(languageHolder === 'deu') {
							languageHolder = 'ger'
						} else if(languageHolder === 'fra') {
							languageHolder = 'fre'
						}
			
						jQuery.ajax('../../srv/'+languageHolder+'/strings.js',
						{
							async:false, cache:true, dataType:'script'});
							Geonet.language = languageHolder;
						} catch (e) {
							if (window.location.href.indexOf('?') === -1) {
								window.location.search = '?lang=eng';
							}
						}
					};
	
				init();
			</script>
	
			<script src="../../apps/shared-objects/app/lib/bootstrap3/js/bootstrap.min.js"></script>
			<script src="../../apps/shared-objects/app/lib/angular/angular.js"></script>
			<script src="../../apps/shared-objects/app/lib/angular/angular-route.js"></script>
			<script type="text/javascript">
			'use strict';
				// Declare app level module which depends on filters, and services
				angular.module('SharedObjects', ['SharedObjects.filters', 'SharedObjects.factories', 'SharedObjects.directives', 'SharedObjects.controllers', 'ngRoute']).
				  config(['$routeProvider', function($routeProvider) {
				      $routeProvider.when('/:validated/contacts', { templateUrl: '../../apps/shared-objects/app/lpartials/shared.html', controller: 'ContactControl' });
				      $routeProvider.when('/:validated/formats', { templateUrl: '../../apps/shared-objects/app/lpartials/shared.html', controller: 'FormatControl' });
				      $routeProvider.when('/:validated/extents', { templateUrl: '../../apps/shared-objects/app/lpartials/shared.html', controller: 'ExtentControl' });
				      $routeProvider.when('/:validated/keywords', { templateUrl: '../../apps/shared-objects/app/lpartials/shared.html', controller: 'KeywordControl' });
				      $routeProvider.when('/deleted', { templateUrl: '../../apps/shared-objects/app/lpartials/shared.html', controller: 'DeletedControl' });
				      $routeProvider.when('/deleted', { templateUrl: '../../apps/shared-objects/app/lpartials/shared.html', controller: 'DeletedControl' });
				      $routeProvider.when('/validated/deleted', { redirectTo: '../../apps/shared-objects/app/l/validated/contacts' });
				      $routeProvider.when('/nonvalidated/deleted', { redirectTo: '../../apps/shared-objects/app/l/nonvalidated/contacts' });
				      $routeProvider.otherwise({ redirectTo: '../../apps/shared-objects/app/l/nonvalidated/contacts' });
				  }]);
			
			</script>
			<script src="../../apps/shared-objects/app/js/factories.js"></script>
			<script src="../../apps/shared-objects/app/js/controllers.js"></script>
			<script src="../../apps/shared-objects/app/js/filters.js"></script>
			<script src="../../apps/shared-objects/app/js/directives.js"></script>
			
			<script type="text/javascript">
				function removeItem(id) {
					if(confirm('<xsl:value-of select="/root/gui/strings/xpathconfirm" />') == true) {
						new Ajax.Request(
							'',
							{
								method: 'post',
								parameters: {action: 'delete', id: id},
								onComplete: function(){location.reload();}});
						}
				}
				  jQuery(function() {
				  	
				  	jQuery("#xpathtype").change(function() {
				  		if(jQuery("#xpathtype").val() == "KEYWORD") {
				  			jQuery("#group").hide();
				  			jQuery("#keyword").show();
				  		} else {
				  			jQuery("#keyword").hide();
				  			jQuery("#group").show();
				  		}
				  	});
				    jQuery( "#keyword" ).change(function( ) {
				  		jQuery("#xpath").val(jQuery("#keyword").val());
				      });
				    
				    jQuery( "#group" ).change(function( ) {
				  		jQuery("#xpath").val(jQuery("#group").val());
				      });
				   });

				function selectKeyword() {
				
					if(!keywordSelectionWindow || keywordSelectionWindow.isDestroyed) {
						var keywordSelectionPanel = new app.SimpleKeywordSelectionPanel({
							input: [Ext.get("keyword"), Ext.get("xpath")]
							});
				
				        var keywordSelectionWindow = new Ext.Window({
				            title: translate('keywordSelectionWindowTitle'),
				            width: 620,
				            height: 300,
				            layout: 'fit',
				            items: keywordSelectionPanel,
				            closeAction: 'hide',
				            constrain: true,
				            iconCls: 'searchIcon'
				        });
				    
					    keywordSelectionWindow.show();
				    }
			    }
			    jQuery( "#keyword" ).click(function() {selectKeyword();})
			</script>
			
		</body>
	</html>
	</xsl:template>

	<!-- ================================================================== -->

</xsl:stylesheet>
