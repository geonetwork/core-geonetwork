Ext.namespace("geocat.edit.Keyword");

geocat.edit.Keyword = {
    /**
     * Property: searchWindow
     * The window in which we can select keywords
     */
    searchWindow: null,

    /**
     * Display keyword selection panel
     * 
     * @param ref
     * @param name
     * @return
     */
    openWindow: function(ref, name, id) {
        var edit = geocat.edit;
        var self = edit.Keyword;
        if (!self.searchWindow) {
            var port = window.location.port === "" ? "": ':' + window.location.port;
            var keywordSelectionPanel = new app.KeywordSelectionPanel({
                createKeyword: function() {
                    doNewElementAction('/geonetwork/srv/eng/metadata.elem.add', ref, name, id);
                },
                addCreateXLinkButton: true,
                listeners: {
                    keywordselected: function(panel, keywords) {
                        xlinks = [];
                    
                        var count = 2;

                        var store = panel.itemSelector.toMultiselect.store;
                        store.each(function(record) {
                            var uri = record.get("uri");
                            var thesaurus = record.get("thesaurus");
                            var xlink = new XLink();

                            xlink.href = "local://che.keyword.get?"+"thesaurus="+encodeURIComponent(thesaurus)+"&id="+encodeURIComponent(uri)+"&locales=fr,en,de,it";
                            xlinks.push(xlink);
                        });


                        // Save
                        edit.submitXLink();
                    }
                }
            });

            self.searchWindow = new Ext.Window({
                width: geocat.edit.windowWidth,
                height: 300,
                title: translate('keywordSelectionWindowTitle'),
                layout: 'fit',
                items: keywordSelectionPanel,
                closeAction: 'hide'
            });
            geocat.edit.windows.push(self);
        }

        self.searchWindow.items.get(0).setRef(ref);
        geocat.edit.hideOtherWindows(self);
        self.searchWindow.setPosition((document.body.getWidth()/2)-(geocat.edit.windowWidth/2),window.scrollY);
        self.searchWindow.show(undefined, function(){
            self.searchWindow.items.get(0).itemSelector.toMultiselect.store.data.clear();
        });
    },
    accepts: function(name) {
        var ucName = name.toUpperCase();
        return ucName.indexOf("KEYWORD") != -1;
    }
};