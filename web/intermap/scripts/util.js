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
	if(src.nodeType==Node.TEXT_NODE) 
	{
		var newNode= document.createTextNode(src.nodeValue);
		parentDest.appendChild(newNode);
		return;				
	}
	
	if(src.nodeType==Node.COMMENT_NODE) 
	{
		var newNode= document.createElement("COMMENT");
		newNode.style.display = "none";
		newNode.textContent= src.nodeValue;
		parentDest.appendChild(newNode);
		return;				
	}
	
//				var li = document.createElement("li"); // DEBUG!!!
//				li.textContent= src.tagName+ " ID:" +src.getAttribute('id');
//				$('ETJ_DEBUG').appendChild(li);
	
	var newNode= document.createElement(src.tagName);
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
	};
	
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

}