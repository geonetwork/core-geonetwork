Ext.namespace("geocat.edit.Format");

geocat.edit.Format = {
    searchWindow:null,
    searchCombo:null,
    submit: function(xlink) {
        xlinks[0].href = xlink;
        geocat.edit.Format.searchWindow.hide();
        geocat.edit.submitXLink();
    },
    openWindow: function(name) {
        var edit = geocat.edit;
        var self = edit.Format;
        var id = 'formatSearchCombo';
        if(self.searchWindow === null) {
            var addButton = edit.addButton(function(){self.submit(self.searchCombo.getValue());});
            var newButton = edit.createButton(function(){self.submit('local://xml.format.get');});
            self.searchCombo = edit.createSearchCombo({
                id: id,
                addButton: addButton,
                service:"xml.format.list", 
                fieldLabel: translate('popXlink.format.search'),
                queryParam:'name', 
                baseParams:{order:'validated'}
            });
            self.searchWindow = edit.createWindow(self, {
                title: translate('FormatSelectionTitle'),
                items: [self.searchCombo],
                buttons: [addButton, newButton]
            });
        }

       geocat.edit.showWindow(self);
    },
    accepts: function(name) {
        var ucName = name.toUpperCase();
        return ucName.indexOf("FORMAT") != -1;
    }
};
