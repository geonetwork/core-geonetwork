Ext.namespace("app");

/**
 * Class: app.SimpleKeywordSelectionPanel
 * 
 * Similar to KeywordSelectionPanel but inserting the plain keyword on an input
 * 
 */

app.SimpleKeywordSelectionPanel = Ext.extend(app.KeywordSelectionPanel, {
	input : Ext.get("keyword"),
	initComponent : function() {
		app.SimpleKeywordSelectionPanel.superclass.initComponent.call(this);
	},

	/**
	 * Put data on input
	 */
	buildKeywordXmlList : function() {
		var input = this.input;
		var store = this.itemSelector.toMultiselect.store;
		Ext.each(this.input, function(i) {
			i.dom.value = "";
		});

		Ext.each(store.data.items, function(s) {
			Ext.each(input, function(i) {
				if (i.dom.value != '') {
					i.dom.value = i.dom.value + " and ";
				}
				i.dom.value = i.dom.value + s.id + " " + s.data.value;
			});
		});
		store.clearFilter();
		this.ownerCt.destroy();
	}
});
