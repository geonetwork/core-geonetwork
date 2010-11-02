//=====================================================================================
//===
//=== Tooltip : shows a tooltip for an element.
//===
//=== Needs:	prototype.js
//===				kernel.js
//=====================================================================================

/* 'el' can be a DOM element or its id. 
   'message' is a string containing simple text or escaped HTML code
*/

function Tooltip(el, message)
{
	var elem = $(el);
	var shown= false;
	var msg  = message;
	var tip  = null;
	var timer= null;
	
	var initDelay = 1000;
	
	Event.observe(elem, 'mouseover', ker.wrap(this, mouseIn));
	Event.observe(elem, 'mouseout',  ker.wrap(this, mouseOut));	

//=====================================================================================

function mouseIn(event)
{
	if (shown)
		return;
		
	if (tip == null)
	{
		tip = document.createElement('div');
		tip.className     = 'tooltip';
		tip.innerHTML     = msg;
		tip.style.display = 'none';
		tip.style.zIndex  = 32000;

		document.body.appendChild(tip);	
	}
		
	tip.style.left = Event.pointerX(event) +12 + "px";
	tip.style.top  = Event.pointerY(event) +12 + "px";
	
	timer = setTimeout(ker.wrap(this, mouseIn_CB), initDelay);	
}

//-------------------------------------------------------------------------------------

function mouseIn_CB()
{
	Element.show(tip);
	
	shown = true;
	timer = null;
}

//=====================================================================================

function mouseOut(event)
{
	if (timer)
	{
		clearTimeout(timer);
		timer = null;
	}
	
	if (!shown)
		return;
		
	Element.hide(tip);	
	
	shown = false;
}

//=====================================================================================
}