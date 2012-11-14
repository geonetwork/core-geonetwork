<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:variable name="title">CSW Demo Request</xsl:variable>

	<xsl:template match="/">
		<html>
			<head>
				<title>
					<xsl:value-of select="concat(/root/gui/strings/title, '-', $title)"/>
				</title>
			  
			  <link href="../../images/logos/favicon.gif" rel="shortcut icon" type="image/x-icon" />
			  <link href="../../images/logos/favicon.gif" rel="icon" type="image/x-icon" />
			  
				<link rel="stylesheet" type="text/css" href="{/root/gui/url}/geonetwork.css"/>
				<script type="text/javascript" src="../../scripts/openlayers/OpenLayers.js"/>
				<script type="text/javascript" src="../../scripts/test-csw.js"/>
				<style type="text/css">
					div.test {
					    padding:7px 7px 7px 7px;
					}
					div.test-head {
					    border-bottom: 3px solid #0263B2;
					    width:100%;
					    height:80px;
					}
					div.test-head-text {
					    padding-left:10px;
					}
					div.test-head-logo {
					    float:left;
					}
					div.test {
					    vertical-align:top;
					}
					div.test-submit {
					    width:100%;
					    text-align:center;
					}
					div#info {
					    height:35px;
					}
					div.test label {
					    text-align: right;
					    padding-right: 10px;
					    display: block;
					    width: 150px;
					    float: left;
					    padding-bottom: 4px;
					    padding-top: 4px;
					}
					br {
					    clear: left;
					}</style>
			</head>
			<body onload="init();">
				<div class="test-head">
					<div class="test-head-logo">
						<img src="../../images/header-right.gif"/>
					</div>
					<div class="test-head-text">
						<h1>
							<xsl:value-of select="$title"/>
						</h1>
						<p> Example requests for GeoNetwork opensource. Select a request from the
							drop down list, and then hit 'Change'. This will display the request url
							(and body if an xml request). Hit submit to send the request to
							GeoNetwork opensource.<br/>
						</p>
					</div>
				</div>
				<div class="test">
					<label for="request">Request:</label>
					<select id="request" onchange="updateOperation(this);"> </select>
					<br/>
					<label for="url">URL:</label>
					<input id="url" type="text" size="50"
						value="{/root/gui/locService}/csw"/>
					<br/>
					<label for="body">Body:</label>
					<textarea id="body" rows="8" cols="150"/>
					<br/>
					<label for="info">Info:</label>
					<div id="info"/>
					<form action="{/root/gui/locService}/csw" method="POST"
						target="response" name="form">
						<label for="username">username:</label>
						<input id="username" type="text" value="admin"/>
						<br/>
						<label for="password">password:</label>
						<input id="password" type="text" value="admin"/>
						<br/>
					</form>
				</div>
				<div class="test-submit">
					<input type="button" onclick="submit();" name="submit" value="Send request (POST)"/>
					<input type="button" onclick="loginAndRun('{/root/gui/url}');" name="submit" value="Log in and send request (POST)" 
						alt="Use this option to test transaction operation."
						title="Use this option to test transaction operation."/>
				</div>
				<div class="test">
					<label for="response">Response:</label>
					<textarea id="response" name="response" rows="30" cols="150"/>
					<!--<iframe id="response" name="response" style="width:1070px;height:600px"/>-->
				</div>
				
				
				<div class="test-head-text">
					<h1>
						Get request examples:
					</h1>
					<p>
						<ul>
							<li><a href="csw?SERVICE=CSW&amp;VERSION=2.0.2&amp;REQUEST=GetCapabilities">GetCapabilities</a></li>
							<li><a href="csw?SERVICE=CSW&amp;VERSION=2.0.2&amp;REQUEST=GetRecordById&amp;ID=8c74e8c4-701a-4cd1-a988-7f2bf9e891bd">GetRecordById</a></li>
						</ul>
					</p>
				</div>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
