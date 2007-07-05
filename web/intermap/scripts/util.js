function getMouseX(e)
{
	var posx = 0;
	if (!e) var e = window.event;
	if (e.pageX)
		posx = e.pageX;
	else if (e.clientX)
		posx = e.clientX + document.body.scrollLeft;
	
	return posx;
}

function getMouseY(e)
{
	var posy = 0;
	if (!e) var e = window.event;
	if (e.pageY)
		posy = e.pageY;
	else if (e.clientY)
		posy = e.clientY + document.body.scrollTop;
	
	return posy;
}

function openGeoNetwork(what)
{
	gnWindow=window.open(what,"GeoNetwork")
	gnWindow.focus()
}

// Didn't find a way to do it with prototype
function getWindowSize()
{
	var width = 0, weight = 0;
	if(typeof(window.innerWidth) == 'number')
	{
		// non IE
		width = window.innerWidth;
		weight = window.innerHeight;
	} 
	else if (document.documentElement && (document.documentElement.clientWidth || document.documentElement.clientHeight))
	{
		// IE 6+ in 'standards compliant mode'
		width = document.documentElement.clientWidth;
		weight = document.documentElement.clientHeight;
	}
	else if (document.body && (document.body.clientWidth || document.body.clientHeight))
	{
		// IE 4 compatible
		width = document.body.clientWidth;
		weight = document.body.clientHeight;
	}
	
	return [width, weight];
}
