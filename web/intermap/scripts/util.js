
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
	gnWindow=window.open(what,"GeoNetwork");
	gnWindow.focus();
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


function clearNode(node)
{
	var enode = $(node);
	while (enode.firstChild) 
	{
		enode.removeChild(enode.firstChild);
	}			
}

//========================================
// Util: Copy a XML tree with HTML format into the current Document
//========================================

function copyTree(src, parentDest)
{		
	var newNode;
		
	if(src.nodeType==Node.TEXT_NODE) 
	{
		newNode= document.createTextNode(src.nodeValue);
		parentDest.appendChild(newNode);
		return;				
	}
	
	if(src.nodeType==Node.COMMENT_NODE) 
	{
		newNode= document.createElement("COMMENT");
		newNode.style.display = "none";
		newNode.textContent= src.nodeValue;
		parentDest.appendChild(newNode);
		return;				
	}
	
//				var li = document.createElement("li"); // DEBUG!!!
//				li.textContent= src.tagName+ " ID:" +src.getAttribute('id');
//				$('ETJ_DEBUG').appendChild(li);
	
	newNode= document.createElement(src.tagName);
//				if(src.className)
//					newNode.className = src.className;
//				newNode.nodeValue = src.nodeValue;
	
	//a("R!!!!!");				
	var tattr = " ";
	//if (src.hasAttributes()) // this line won't work in IE
	if (src.attributes) 
	{				
		var attrs = src.attributes;				
		for(var i=attrs.length-1; i>=0; i--) 
		{
			newNode.setAttribute(attrs[i].name, attrs[i].value);
			tattr += (attrs[i].name+'="'+attrs[i].value+'"');
		}
	}
	
	//a("ADDING  &lt;" + src.tagName + tattr  +'&gt;');	
	//a("Adding to " + parentDest);
	//a("Adding to " + $(parentDest));
	$(parentDest).appendChild(newNode);
	//a("ADDED");
	var child = src.firstChild;				
	while(child)
	{
		copyTree(child, newNode);
		child = child.nextSibling;
	}
}

//========================================
// Util: Transform a XML tree into its related text form
//========================================

function xml2text(src)
{
	if(src.nodeType==Node.TEXT_NODE) 
	{
		return src.nodeValue;
	}
	
	if(src.nodeType==Node.COMMENT_NODE) 
	{
		return "<!-- "+ src.nodeValue +" -->";
	}
	
//	a("R!!!!!");				
	var tattr = "";
	//if (src.hasAttributes()) // this line won't work in IE
	if (src.attributes) 
	{				
            	var attrs = src.attributes;				
            	for(var i=attrs.length-1; i>=0; i--) 
            	{                        	
                        	tattr += (attrs[i].name+'="'+attrs[i].value+'" ');
            	}
	}
	
	var text = "<" + src.tagName +  " " + tattr  +'>\n';
	
	var wrapstart = "";
	
	if(src.tagName == "table")
	{
	    text+= "<tbody>";
	    wrapend= "</tbody>";
	}
		
//	a(text);
	var child = src.firstChild;				
	while(child)
	{
		text += xml2text(child) + "\n";
		child = child.nextSibling;
	}
	
	text += wrapend +  "</"+ src.tagName+ ">";
	
	return text;
}

/**********************************************************
*
* AJAX IFRAME METHOD (AIM)
* http://www.webtoolkit.info/
*
**********************************************************/

AIM = {

    frame : function(c) {

        var n = 'f' + Math.floor(Math.random() * 99999);
        var d = document.createElement('DIV');
        d.innerHTML = '<iframe style="display:none" src="about:blank" id="'+n+'" name="'+n+'" onload="AIM.loaded(\''+n+'\')"></iframe>';
        document.body.appendChild(d);

        var i = document.getElementById(n);
        if (c && typeof(c.onComplete) == 'function') {
            i.onComplete = c.onComplete;
        }

        return n;
    },

    form : function(f, name) {
        f.setAttribute('target', name);
    },

    submit : function(f, c) {
        AIM.form(f, AIM.frame(c));
        if (c && typeof(c.onStart) == 'function') {
            return c.onStart();
        } else {
            return true;
        }
    },

    loaded : function(id) {
        var i = document.getElementById(id);
        if (i.contentDocument) {
            var d = i.contentDocument;
        } else if (i.contentWindow) {
            var d = i.contentWindow.document;
        } else {
            var d = window.frames[id].document;
        }
        if (d.location.href == "about:blank") {
            return;
        }

        if (typeof(i.onComplete) == 'function') {
            i.onComplete(d);
//            i.onComplete(d.body.innerHTML);
        }
    }

};



function getRadioValue(name)
{
    var radioarr = $A(document.getElementsByName(name));
    for(i = 0; i < radioarr.length; i++)
    {
        if(radioarr[i].checked)
            return radioarr[i].value;
    }
    return null;
}


/*******************************************************************
 * Some utility funcs for map handling
 * 
 */
function MapUtils() {}

/**
 * Get the URLized version of the given bbox
 * @param {int} n
 * @param {int} e
 * @param {int} s
 * @param {int} w
 * @return {String} URL 
 */
MapUtils.urlizebb = function (n, e, s, w)
{
	return	"northBL="+n+
			"&eastBL="+e+
			"&southBL="+s+
			"&westBL="+w;    
};

/**
 * Draws the box by setting its style position and size
 * @param {Element} box the DOM Element to resize
 * @param {int} left coord in pixel
 * @param {int} top coord in pixel
 * @param {int} width size in pixel
 * @param {int} height size in pixel
 */
MapUtils.drawBox = function(box, left, top, width, height)
{
	box.style.left = left + 'px';
	box.style.top = top + 'px';
	box.style.width = width + 'px';
	box.style.height = height + 'px';
};

/** 
 * Returns the URLized dezoomed bbox 
 * 
 * @param {number} n
 * @param {number} e
 * @param {number} s
 * @param {number} w
 * @param {number} factor the dezooming factor (opt, default 2) 
 * 
 * @return {string} the URLized dezoomed bbox
 */
MapUtils.dezoom = function(n, e, s, w, factor)
{	
	if(factor===null)
	{
		factor = 2;		
	}
	var dx = (e - w) / factor;
	var dy = (n - s) / factor;
	return MapUtils.urlizebb( n-dy, e+dx, s+dy, w-dx );
};

//==================================================

/**
 * A FakeBox is formed by 4 divs with ids: 
 * <i>name</i>_n, <i>name</i>_e, <i>name</i>_s, <i>name</i>_w.
 * This linear divs are put in a square layout.
 * Such fake box, even when displayed, does not steal mouse events.
 * 
 *  @constructor
 *  
 * @param {string} name The base id name
 */
function FakeBox(name)
{
	this.name = name;
}

FakeBox.prototype.name;

/**
 * Draws a box
 *
 * @param {int} left coord in pixel
 * @param {int} top coord in pixel
 * @param {int} width size in pixel
 * @param {int} height size in pixel
 */
FakeBox.prototype.draw = function(left, top, width, height)
{
    var n = $(this.name + '_n');
    var e = $(this.name + '_e');
    var s = $(this.name + '_s');
    var w = $(this.name + '_w');
	
	if( Prototype.Browser.IE )
	{
		if(width > 0) {width--;}
		if(height>0) {height--;}
	}
		
    n.style.left = left + 'px';
    n.style.top = top + 'px';
    n.style.width = width + 'px';
    n.style.height = '0px';

	// east is shifted 1px left because of 0-width border drawing 
    e.style.left = (left + width - 1 ) + 'px';
    e.style.top = top + 'px';
    e.style.width = '0px';
    e.style.height = height + 'px';
    
	// east is shifted 1px up to left because of 0-height border drawing
    s.style.left = left + 'px';
    s.style.top = (top+height-1) + 'px';
    s.style.width = width + 'px';
    s.style.height = '0px';
    
    w.style.left = left + 'px';
    w.style.top = top + 'px';
    w.style.width = '0px';
    w.style.height = height + 'px';
	
	if( Prototype.Browser.IE )
	{
	    n.style.height = '1px';
	    e.style.width  = '1px';
	    s.style.height = '1px';
	    w.style.width  = '1px';
	}
	
};

FakeBox.prototype.hide = function()
{
    $(this.name + '_n').hide();
    $(this.name + '_e').hide();
    $(this.name + '_s').hide();
    $(this.name + '_w').hide();
};

FakeBox.prototype.show = function()
{
    $(this.name + '_n').show();
    $(this.name + '_e').show();
    $(this.name + '_s').show();
    $(this.name + '_w').show();
};
