var builder = {
    url: null, // to be set layout.html
    modules: null, // to be set in builder-vX.Y.js
    assets: null, // to be set by callback
    setAssets: function(assets) {
        this.assets = assets;
    }
};

Ext.onReady(function() {
    
    var assets = builder.assets;
    var modules = builder.modules;

    var checks = {};
    var prefs = {};
    
    // uncheck components that have not been specifically checked by the user
    var updateDeps = function() {
        for (var path in assets) {
            var check = checks[path];
            if (check) {
                var checked = check.getValue();
                if (checked && !prefs[path]) {
                    // keep checked if required or included
                    var depended = false;
                    for (var p in assets) {
                        var c = checks[p];
                        if (c && c.getValue() && ((path in assets[p].require) || (path in assets[p].include))) {
                            depended = true;
                            break;
                        }
                    }
                    if (!depended) {
                        check.setValue(false);
                        check.enable();
                    }
                }
            }
        }
    };
    
    // check additional requires and includes
    var checkDeps = function(path) {
        var check, asset = assets[path];
        for (var include in asset.include) {
            check = checks[include];
            if (check && !check.getValue()) {
                checks[include].setValue(true);
            }
        }
        for (var require in asset.require) {
            check = checks[require];
            if (check && !check.getValue()) {
                checks[require].setValue(true);
                checks[require].disable();
            }
        }
    };
    
    // check form data before submitting
    var validate = function() {
        // for now, we just want to make sure something is checked
        var valid = false;
        for (var path in checks) {
            if (checks[path].getValue()) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            Ext.Msg.show({
                msg: "You must select at least one component for your build.",
                icon: Ext.MessageBox.WARNING
            });
        }
        return valid;
    };


    var items = [];
    var module, set, sets = {};
    for (var path in modules) {
        module = modules[path];
        set = new Ext.form.FieldSet({
            title: module.title,
            autoHeight: true,
            collapsible: true,
            layout: "column",
            autoWidth: true,
            html: "<div class='module-desc'>" + module.description + "</div>",
            defaults: {width: 300}
        });
        sets[path] = set;
        items.push(set);
    }
    
    var checkbox, parts, prefix, base, name;
    var updating = false;
    for (var path in assets) {
        parts = path.split("/");
        name = parts.pop().replace(".js", "");
        var base = parts.join("/");
        var set = sets[base];
        if (set) {
            prefix = modules[base].prefix;
            checkbox = new Ext.form.Checkbox({
                boxLabel: prefix ? (prefix + "." + name) : name,
                name: "include[]",
                inputValue: path,
                ctCls: "component",
                handler: function(box, checked) {
                    var path = box.getRawValue();
                    var old = updating;
                    if (checked) {
                        updating = true;
                        checkDeps(path);
                        updating = old;
                    } else {
                        updating = true;
                        updateDeps();
                        updating = old;
                    }
                    if (!updating) {
                        // check represents user preferene
                        prefs[path] = checked;
                    }
                }
            });
            checks[path] = checkbox;
            set.add(checkbox);
        }
    }
    
    var dh = Ext.DomHelper;
    
    var formContainer = dh.insertAfter(
        Ext.get(Ext.select(".builder-form-text").first()),
        {tag: "div", cls: "builder-form"}
    );

    var form = new Ext.FormPanel({
        method: "POST",
        standardSubmit: true,
        border: false,
        bodyStyle: {padding: 10},
        renderTo: formContainer,
        labelWidth: 100,
        autoWidth: true,
        items: items
    });
    
    var buttonContainer = dh.insertAfter(
        Ext.get(Ext.select(".download-button-text").first()),
        {tag: "div", cls: "download-button"}
    );
    
    var download = new Ext.Button({
        renderTo: buttonContainer,
        text: "download",
        iconCls: "download",
        handler: function() {
            if (validate()) {
                var el = form.getForm().getEl().dom;
                el.action = builder.url,
                el.submit();
            }
        }
    });
    
});
