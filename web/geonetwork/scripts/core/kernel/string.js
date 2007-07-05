//=====================================================================================
//===
//=== String utility methods
//===
//=== Needs:	xml.js
//=====================================================================================

var str = new Object();

//=====================================================================================

str.replace = function(text, pattern, subst)
{
	var res = '';
	var pos = text.indexOf(pattern);

	while (pos != -1)
	{
		res  = res + text.substring(0, pos) + subst;
		text = text.substring(pos + pattern.length);
		pos  = text.indexOf(pattern);
	}

	return res + text;
}

//=====================================================================================
/* Takes a template and substitutes all constants like {NAME} with the value found
	inside the map. The value is escaped to be xml/html compliant.  
*/

str.substitute = function(template, map)
{
	for (var name in map)
	{
		var value = map[name];
		
		//--- skip arrays and other objects
		if (typeof value == 'object')
			continue;
			
		if (typeof value == 'boolean')
			value = (value) ? 'true' : 'false';
			
		else if (typeof value == 'number')
			value = '' + value;
		
		template = str.replace(template, '{'+name+'}', xml.escape(value));
	}
	
	return template;
}

//=====================================================================================
