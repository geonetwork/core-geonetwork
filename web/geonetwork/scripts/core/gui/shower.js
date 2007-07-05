//=====================================================================================
//===
//=== Shower
//===
//=== Needs:	prototype.js
//=====================================================================================

function Shower(sourceId, targetId)
{
	var source = $(sourceId);
	var target = $(targetId);
	
	Event.observe(source, 'change', update);
	
	this.update = update;

//=====================================================================================

function update()
{
	if (source.checked)	Element.show(target);
		else 					Element.hide(target);
}

//=====================================================================================
}