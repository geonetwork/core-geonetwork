//=====================================================================================
//===
//=== TabSwitcher
//===
//=== Given a list of DIVS or other containers with unique ids, allows to show only
//=== one of them at a time, hiding the others. It is like a java tabbed pane
//===
//=== Needs:	prototype.js
//=====================================================================================

function TabSwitcher()
{
	var idLists = new Array();
	
	for (var i=0; i<arguments.length; i++)
		idLists.push(arguments[i]);

	this.add  = add;
	this.show = show;

//=====================================================================================

function add()
{
	for (var i=0; i<arguments.length; i++)
		idLists[i].push(arguments[i]);
}

//=====================================================================================

function show()
{
	for (var i=0; i<arguments.length; i++)
		for (var j=0; j<idLists[i].length; j++)
			if (arguments[i] == idLists[i][j])	Element.show(idLists[i][j]);
				else										Element.hide(idLists[i][j]);
}

//=====================================================================================
}


