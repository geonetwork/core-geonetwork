//=====================================================================================
//===
//=== Model (type:CGP)
//===
//=====================================================================================

cgp.Model = function(xmlLoader)
{
	HarvesterModel.call(this);

	var loader = xmlLoader;
	var callBackF = null;

	this.retrieveIcons = retrieveIcons;
	this.retrieveGroups    = retrieveGroups;
	this.retrieveCategories= retrieveCategories;
	this.getUpdateRequest = getUpdateRequest;

	//=====================================================================================

	function retrieveIcons(callBack)
	{
		callBackF = callBack;

		var request = ker.createRequest('type', 'icons');

		ker.send('xml.harvesting.info', request, ker.wrap(this, retrieveIcons_OK));
	}

	//-------------------------------------------------------------------------------------

	function retrieveIcons_OK(xmlRes)
	{
		if (xmlRes.nodeName == 'error')
			ker.showError(loader.getText('cannotRetrieve'), xmlRes);
		else
		{
			var data = [];
			var list = xml.children(xml.children(xmlRes)[0]);

			for (var i = 0; i < list.length; i++)
				data.push(xml.textContent(list[i]));

			callBackF(data);
		}
	}

//=====================================================================================

	function retrieveGroups(callBack)
	{
		new InfoService(loader, 'groups', callBack);
	}

	//=====================================================================================

	function retrieveCategories(callBack)
	{
		new InfoService(loader, 'categories', callBack);
	}

	//=====================================================================================

	function getUpdateRequest(data)
	{
		var request = str.substitute(updateTemp, data);

		var list = data.SEARCH_LIST;
		var text = '';

		for (var i = 0; i < list.length; i++)
			text += str.substitute(searchTemp, list[i]);

		request = str.replace(request, '{SEARCH_LIST}', text);

		return this.substituteCommon(data, request);
	}

	//=====================================================================================

	var updateTemp =
			' <node id="{ID}" type="{TYPE}">' +
			'    <site>' +
			'      <name>{NAME}</name>' +
			'      <url>{URL}</url>' +
			'      <account>' +
			'        <use>{USE_ACCOUNT}</use>' +
			'        <username>{USERNAME}</username>' +
			'        <password>{PASSWORD}</password>' +
			'      </account>' +
			'      <icon>{ICON}</icon>' +
			'    </site>' +
			'    <options>' +
			'      <every>{EVERY}</every>' +
			'      <oneRunOnly>{ONE_RUN_ONLY}</oneRunOnly>' +
			'      <validate>{VALIDATE}</validate>'+
			'    </options>' +
			'    <searches>' +
			'       {SEARCH_LIST}' +
			'    </searches>' +
			'    <privileges>'+
			'       {PRIVIL_LIST}'+
			'    </privileges>'+
			'    <categories>'+
			'       {CATEG_LIST}'+
			'    </categories>'+
			'  </node>';

	//=====================================================================================

	var searchTemp =
			'    <search>' +
			'      <freeText>{ANY_TEXT}</freeText>' +
			'      <from>{FROM}</from>'+
			'      <until>{UNTIL}</until>'+
			'      <latNorth>{LAT_NORTH}</latNorth>'+
			'      <latSouth>{LAT_SOUTH}</latSouth>'+
			'      <lonEast>{LON_EAST}</lonEast>'+
			'      <lonWest>{LON_WEST}</lonWest>'+
			'    </search>';

	//=====================================================================================

	var groupTemp = '<group name="{NAME}" policy="{POLICY}"/>';

	//=====================================================================================
}
