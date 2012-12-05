<%@page import="java.util.Enumeration"%><html xmlns="http://www.w3.org/1999/xhtml" lang="fr" xml:lang="fr">
	<head>
		<meta http-equiv="Pragma" content="no-cache">
		<meta http-equiv="Cache-Control" content="no-cache,no-store">
		<link rel="stylesheet" type="text/css" href="geonetwork.css">
		<script language="Javascript1.5" type="text/javascript">
		function init() {
			<% 
			String found = null;
			Enumeration names = request.getHeaderNames();
			while (names.hasMoreElements()) {
				String s = (String)names.nextElement();
				if(s.equalsIgnoreCase("Accept-Language")) {
					found = s;
					break;
				}
			}
			Object language = null;
			if(found != null) {
				language = request.getHeader(found);
			}
			%>
			var userLang = "<%= language %>"
			if(!userLang) {
				userLang = (navigator.language) ? navigator.language : navigator.userLanguage;
			} 

			if(!userLang) {
				userLang = "eng";
			} 
			
			userLang = userLang.split('-')[0].toLowerCase();
			if (userLang.match("^en")) {
				userLang = "eng";
			}
			if (userLang.match("^fr")) {
				userLang = "fre";
			}
			if (userLang.match("^de")) {
				userLang = "ger";
			}
			if (userLang.match("^it")) {
				userLang = "ita";
			}
	
		  	window.location="srv/"+userLang+"/login.form"+window.location.search;
		}
		</script>
	</head>
	<body onload="init()">
		<p>&nbsp;&nbsp;Please wait...</p>
		<p>&nbsp;&nbsp;Patientez s'il vous plaît...</p>
		<p>&nbsp;&nbsp;Bitte warten...</p>
		<p>&nbsp;&nbsp;Un momento per favore...</p>

		<noscript>
			<h2>JavaScript warning</h2>
			<p>To use GeoNetwork you need to enable JavaScript in your browser</p>
		</noscript>
	</body>
</html>

