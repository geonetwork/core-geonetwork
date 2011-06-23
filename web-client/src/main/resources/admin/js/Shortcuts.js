/**
 * Init shortcut for admin mode
 * @return
 */
function initShortcut() {
	// TODO : for each mode

	// Define editor shortcut here.
	var searchConfig = [ 
//	                     {
//		key : "s",
//		ctrl : true,
//		shift : true,
//		stopEvent : true,
//		label : 'Run a search.',
//		fn : function() {
//			Ext.getCmp('searchBt').fireEvent('click');
//		}
//	}, {
		];
	var map = new Ext.KeyMap(document, searchConfig);
	map.enable();

	var help = '';
	for ( var i = 0; i < searchConfig.length; i++) {
		var c = searchConfig[i];
		help += '<li><span class="label">'
				+ (c.ctrl == true ? '&lt;Ctrl&gt;' : '') + '+'
				+ (c.shift == true ? '&lt;Shift&gt;' : '') + '+' + c.key
				+ ': </span>' + c.label + '</li>';
	}
	Ext.getDom('shortcutHelp').innerHTML = help;

	// Launch search when enter key press
	var formMap = new Ext.KeyMap("searchForm", [ {
		key : [ 10, 13 ],
		fn : function() {
			Ext.getCmp('searchBt').fireEvent('click');
		}
	} ]);

	formMap.enable();
};
