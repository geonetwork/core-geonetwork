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
	
	Event.observe(source, 'click', update);
	
	this.update = update;

//=====================================================================================

function update()
{
	if (source.checked)	Element.show(target);
		else 					Element.hide(target);
}

//=====================================================================================
}

function RadioShower(sourceId, targetId, targetIds)
{
	var source = $(sourceId);
	var target = $(targetId);
	var targets = [];
	for (var index = 0, len = targetIds.length; index < len; ++index) {
		var id = targetIds[index];
		targets.push($(id));
	}
	
	Event.observe(source, 'click', update);
	
	this.update = update;

//=====================================================================================

function update()
{
	if (source.checked) {
		for (var index = 0, len = targets.length; index < len; ++index) {
			var elem = targets[index];
			if (elem.id == target.id) Element.show(elem);
			else Element.hide(elem);
		}
	}
}

//=====================================================================================
}
