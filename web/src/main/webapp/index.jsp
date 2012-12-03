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
			if(userLang == null || userLang.length == 0)
				userLang = (navigator.language) ? navigator.language : navigator.userLanguage; 
			
			if(userLang.match("^en") == "en") userLang = "eng"
			else if(userLang.match("^fr") == "fr") userLang = "fre"
			else userLang = "ger"
	
			var search = window.location.search;
			
		 	if (search && search.indexOf("id") != -1)
				window.location="srv/"+userLang+"/metadata.show" + search;
		 	else if (search && search.indexOf("uuid") != -1)
				window.location="srv/"+userLang+"/metadata.show" + search;
			else
			  	window.location="srv/"+userLang+"/geocat";
		}
		</script>
	</head>
	<body onload="init()">
		<p>&nbsp;&nbsp;Bitte warten...</p>
		<p>&nbsp;&nbsp;Patientez s'il vous plaît...</p>
		<p>&nbsp;&nbsp;Un momento per favore...</p>
		<p>&nbsp;&nbsp;Please wait...</p>

		<noscript>
			<h2>JavaScript warning</h2>
			<p>To use GeoNetwork you need to enable JavaScript in your browser</p>
		</noscript>
	</body>
</html>

