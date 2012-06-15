Ext.namespace("geocat.edit.Contact");

geocat.edit.Contact = {
    searchWindow:null,
    roles:null,
    searchCombo:null,
    submit: function(xlink) {
        var self = geocat.edit.Contact;
        xlinks[0].href = xlink+'&role='+self.roles.getValue();
        self.searchWindow.hide();
        geocat.edit.submitXLink();
    },
    openWindow: function(name) {
        var edit = geocat.edit;
        var self = edit.Contact;
        var id = 'contactSearchCombo';
        if(self.searchWindow === null) {
            var addButton = edit.addButton(function(){self.submit(self.searchCombo.getValue());});
            var newButton = edit.createButton(function(){self.submit('local://xml.user.get?schema=iso19139.che');});
            self.searchCombo = edit.createSearchCombo({
                id: id,
                addButton: addButton,
                service:"shared.user.list", 
                fieldLabel: translate('popXlink.contact.search'),
                queryParam:'name', 
                baseParams:{sortByValidated:true}
            });
            self.roles = edit.createArrayCombo({
                fieldLabel: translate('popXlink.contact.role'),
                value: 'pointOfContact', 
                elements: edit.contactRoles
            });

            self.searchWindow = edit.createWindow(self, {
                title: translate('ContactSelectionTitle'),
                items: [self.searchCombo,self.roles],
                buttons: [addButton, newButton]
            });
        }

        geocat.edit.showWindow(self);
    },
    accepts: function(name) {
        var ucName = name.toUpperCase();
        return ucName.indexOf("RESPONSIBLEPARTY") != -1 || ucName.indexOf("CONTACT") != -1;
    }
};
