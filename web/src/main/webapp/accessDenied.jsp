<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="java.util.Enumeration,org.springframework.security.web.*,org.springframework.security.core.*,java.util.regex.Pattern,java.util.regex.Matcher"%><html xmlns="http://www.w3.org/1999/xhtml">
	<head>
	<% String baseURL = "/geonetwork"; %>
		<meta http-equiv="Pragma" content="no-cache">
		<meta http-equiv="Cache-Control" content="no-cache,no-store">
		<link rel="stylesheet" type="text/css" href="<%= baseURL %>/geonetwork.css">
		<script language="Javascript1.5" type="text/javascript">
		function init() {
			<% 
			Object language = null;
			Object ssLanguage = null;

			String redirectUrl;
			org.springframework.security.web.savedrequest.SavedRequest savedRequest =     new org.springframework.security.web.savedrequest.HttpSessionRequestCache().getRequest(request, response);
			if (savedRequest !=null) {
				redirectUrl=savedRequest.getRedirectUrl();
				Pattern p = Pattern.compile("^.*/srv/([a-z]{3})/.*$");
				Matcher m = p.matcher(redirectUrl);

				if (m.find()) 
					language=m.group(1);
				}
		ssLanguage=language;
				
		if (language==null) {
			String found = null;
				Enumeration names = request.getHeaderNames();
				while (names.hasMoreElements()) {
					String s = (String)names.nextElement();
					if(s.equalsIgnoreCase("Accept-Language")) {
						found = s;
						break;
					}
				}
				if(found != null) {
					language = request.getHeader(found);
					}
				}
			%>
			var userLang = '<%= language %>'
			var ssUserLang = '<%= ssLanguage %>'
			var referer = window.location.pathname

			// Attempt to determine language based on referer if we could not determine it based on spring security.
			refererLang=(referer.match(/^.*\/srv\/([a-z]{3})\/.*$/i)|| [,null])[1];
			if (ssUserLang=="null" && refererLang && refererLang.length==3)
				userLang=refererLang;

			if(!userLang) {
				userLang = (navigator.language) ? navigator.language : navigator.userLanguage;
			} 

			if(!userLang) {
				userLang = "eng";
			} 
			
			userLang = userLang.split('-')[0].toLowerCase();
			if (userLang.match("^en")) {
				userLang = "eng";
			} else if (userLang.match("^fr")) {
				userLang = "fre";
			} else if (userLang.match("^de")) {
				userLang = "ger";
			} else if (userLang.match("^it")) {
				userLang = "ita";
            } else if (userLang.match("^ca")) {
                userLang = "cat";
            } else if (userLang.match("^es")) {
                userLang = "spa";
            } else if (userLang.match("^fi")) {
                userLang = "fin";
            } else if (userLang.match("^pl")) {
                userLang = "pol";
            } else if (userLang.match("^no")) {
                userLang = "nor";
			} else if (userLang.match("^nl")) {
                userLang = "dut";
            } else if (userLang.match("^pt")) {
                userLang = "por";
            } else if (userLang.match("^ar")) {
                userLang = "ara";
            } else if (userLang.match("^zh")) {
                userLang = "chi";
            } else if (userLang.match("^ru")) {
                userLang = "rus";
            } else if (userLang.match("^tr")) {
                userLang = "tur";
            } else {
                userLang = "eng";
            }

		  	window.location="<%= baseURL %>/srv/"+userLang+"/service-not-allowed?referer="+referer;
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

