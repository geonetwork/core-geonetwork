Ext.namespace('Ext.ux');

Ext.ux.BoxSelect = Ext.extend(Ext.form.ComboBox, {

	initComponent:function() {
		Ext.apply(this, {
			selectedValues: {},
			boxElements: {},
			current: false,
			options: {
				className: 'bit',
				separator: ','
			},
			hideTrigger: true,
			grow: false
		});
			
		Ext.ux.BoxSelect.superclass.initComponent.call(this);
	},
	
	onRender:function(ct, position) {
		Ext.ux.BoxSelect.superclass.onRender.call(this, ct, position);
		
		this.el.removeClass('x-form-text');
		this.el.className = 'maininput';
		this.el.setWidth(20);

		this.holder = this.el.wrap({
			'tag': 'ul',
			'class':'holder x-form-text'
		});
				
		this.holder.on('click', function(e){
			e.stopEvent();
			if(this.maininput != this.current) this.focus(this.maininput);
		}, this);

		this.maininput = this.el.wrap({
			'tag': 'li', 'class':'bit-input'
		});
		
		
		Ext.apply(this.maininput, {
			'focus': function(){
				this.focus();
			}.createDelegate(this)
		})
		
		this.store.on('datachanged', function(store){
			this.store.each(function(rec){
				if(this.checkValue(rec.data[this.valueField])){
					this.removedRecords[rec.data[this.valueField]] = rec;
					this.store.remove(rec);
				}
			}, this);
		}, this);

		this.on('expand', function(store){
			this.store.each(function(rec){
				if(this.checkValue(rec.data[this.valueField])){
					this.removedRecords[rec.data[this.valueField]] = rec;
					this.store.remove(rec);
				}
			}, this);
		}, this);
		
		this.removedRecords = {};
	},
	
	onResize : function(w, h, rw, rh){
		this._width = w;
		this.holder.setWidth(w-4);
		Ext.ux.BoxSelect.superclass.onResize.call(this, w, h, rw, rh);
		this.autoSize();
	},
	
	onKeyUp : function(e) {
		if(this.editable !== false && !e.isSpecialKey()){
			if(e.getKey() == e.BACKSPACE && this.lastValue.length == 0){
				e.stopEvent();
				this.collapse();
				var el = this.maininput.prev();
				if(el) el.focus();
				return;
			}
			this.dqTask.delay(this.queryDelay);
		}

		this.autoSize();

		Ext.ux.BoxSelect.superclass.onKeyUp.call(this, e);

		this.lastValue = this.el.dom.value;
	},

	onSelect: function(record, index) {
		var val = record.data[this.valueField];
		
		this.selectedValues[val] = val;
		
		if(typeof this.displayFieldTpl === 'string')
			this.displayFieldTpl = new Ext.XTemplate(this.displayFieldTpl);
		
		if(!this.boxElements[val]){
			var caption;
			if(this.displayFieldTpl)
				caption = this.displayFieldTpl.apply(record.data)
			else if(this.displayField)
				caption = record.data[this.displayField];
			
			this.addItem(record.data[this.valueField], caption)
			
		}
		this.collapse();
		this.setRawValue('');
		this.lastSelectionText = '';
		this.applyEmptyText();

		this.autoSize();
	},

	onEnable: function(){
		Ext.ux.BoxSelect.superclass.onEnable.apply(this, arguments);
		for(var k in this.boxElements){
			this.boxElements[k].enable();
		}
	},

	onDisable: function(){
		Ext.ux.BoxSelect.superclass.onDisable.apply(this, arguments);
		for(var k in this.boxElements){
			this.boxElements[k].disable();
		}
	},

	getValue: function(){
		var ret = [];
		for(var k in this.selectedValues){
			if(this.selectedValues[k])
				ret.push(this.selectedValues[k]);
		}
		return ret.join(this.options['separator']);
	},
	
	setValue: function(value){
		this.removeAllItems();
		this.store.clearFilter();
		this.resetStore();
	
		if(Ext.isArray(this.value) && typeof this.value[0]==='object' && this.value[0].data){
			this.setValues(this.value);
		}
		else{
			if(value && typeof value === 'string'){
				value = value.split(',');
			}

			var values = [];
			
			if(this.mode == 'local'){
				Ext.each(value, function(item){
					var index = this.store.find(this.valueField, item.trim());
					if(index > -1){
						values.push(this.store.getAt(index));
					}
				}, this);
			}else{
				this.store.baseParams[this.queryParam] = value;
				this.store.load({
					params: this.getParams(value)
				});
			}
			this.setValues(values);
		}
	},
	
	setValues: function(values){
		if(values){
			Ext.each(values, function(data){
				this.onSelect(data);
			}, this);
		}
		
		this.value = '';
	},
	
	removeAllItems: function(){
		for(var k in this.boxElements){
			this.boxElements[k].dispose(true);
		}
	},
	
	resetStore: function(){
		for(var k in this.removedRecords){
			var rec = this.removedRecords[k];
			this.store.add(rec);
		}
		this.sortStore();
	},
	
	sortStore: function(){
		var si = this.store.getSortState();
		if(si && si.field)
			this.store.sort(si.field, si.direction);
	},
	
	addItem: function(id, caption){
		var box = new Ext.ux.BoxSelect.Item({
			id: 'Box_' + id,
			maininput: this.maininput,
			renderTo: this.holder,
			className: this.options['className'],
			caption: caption,
			disabled: this.disabled,
			'value': id,
			listeners: {
				'remove': function(box){
					delete this.selectedValues[box.value];
					var rec = this.removedRecords[box.value];
					if(rec){
						this.store.add(rec);
						this.sortStore();
						this.view.render();
						//this.removedRecords[box.value] = null;
					}
				},
				scope: this
			}
		});
		box.render();

		box.hidden = this.el.insertSibling({
			'tag':'input', 
			'type':'hidden', 
			'value': id,
			'name': (this.hiddenName || this.name)
		},'before', true);

		this.boxElements['Box_' + id] = box;
	},
	
	autoSize : function(){
		if(!this.rendered){
		return;
		}
		if(!this.metrics){
			this.metrics = Ext.util.TextMetrics.createInstance(this.el);
		}
		var el = this.el;
		var v = el.dom.value;
		var d = document.createElement('div');
		d.appendChild(document.createTextNode(v));
		v = d.innerHTML;
		d = null;
		v += "&#160;";
		var w = Math.max(this.metrics.getWidth(v) +  10, 10);
		if(typeof this._width != 'undefined')
			w = Math.min(this._width, w);
		
		this.el.setWidth(w);
		
		if(Ext.isIE){
			this.el.dom.style.top='0';
		}
	},
	
	onEnable: function(){
		Ext.ux.BoxSelect.superclass.onEnable.apply(this, arguments);

		for(var k in this.boxElements){
			this.boxElements[k].enable();
		}
	},

	onDisable: function(){
		Ext.ux.BoxSelect.superclass.onDisable.apply(this, arguments);

		for(var k in this.boxElements){
			this.boxElements[k].disable();
		}
	},

	checkValue: function (value) {
		return (typeof this.selectedValues[value] != 'undefined');
	}
});

Ext.reg('boxselect', Ext.ux.BoxSelect);

Ext.ux.BoxSelect.Item = Ext.extend(Ext.Component, {

	initComponent : function(){
		Ext.ux.BoxSelect.Item.superclass.initComponent.call(this);
	},

	onElClick : function(e){
		this.focus();
	},

	onLnkClick : function(e){
		e.stopEvent();
		this.fireEvent('remove', this);
		this.dispose();
	},

	onLnkFocus : function(){
		this.el.addClass("bit-box-focus");
	},

	onLnkBlur : function(){
		this.el.removeClass("bit-box-focus");
	},

	enableElListeners : function() {
		this.el.on('click', this.onElClick, this, {stopEvent:true});
	},

	enableLnkListeners : function() {
		this.lnk.on({
			'click': this.onLnkClick,
			'focus': this.onLnkFocus,
			'blur':  this.onLnkBlur,
			scope: this
		});
	},

	enableAllListeners : function() {
		this.enableElListeners();
		this.enableLnkListeners();
	},

	disableAllListeners : function() {
		this.el.un('click', this.onElClick, this);

		this.lnk.un('click', this.onLnkClick, this);
		this.lnk.un('focus', this.onLnkFocus, this);
		this.lnk.un('blur', this.onLnkBlur, this);
	},

	onRender: function(ct, position){
		Ext.ux.BoxSelect.Item.superclass.onRender.call(this, ct, this.maininput);
		
		this.addEvents('remove');

		this.addClass('bit-box');

		this.el = ct.createChild({ tag: 'li' }, this.maininput);
		this.el.addClassOnOver('bit-hover');

		Ext.apply(this.el, {
			'focus': function(){
				this.down('a.closebutton').focus();
			},
			'dispose': function(){
				this.dispose();
			}.createDelegate(this)

		});

		this.enableElListeners();

		this.el.update(this.caption);

		this.lnk = this.el.createChild({
			'tag': 'a',
			'class': 'closebutton',
			'href':'#'
		});

		if(!this.disabled)
			this.enableLnkListeners();
		else
			this.disableAllListeners();

		this.on({
			'disable': this.disableAllListeners,
			'enable': this.enableAllListeners,
			scope: this
		});

		new Ext.KeyMap(this.lnk, [
			{
				key: [Ext.EventObject.BACKSPACE, Ext.EventObject.DELETE],
				fn: function(){
					this.dispose();
				}.createDelegate(this)
			},
			{
				key: Ext.EventObject.RIGHT,
				fn: function(){
					this.move('right');
				}.createDelegate(this)
			},
			{
				key: Ext.EventObject.LEFT,
				fn: function(){
					this.move('left');
				}.createDelegate(this)
			},
			{
				key: Ext.EventObject.TAB,
				fn: function(){
				}.createDelegate(this)
			}
		]).stopEvent = true;

	},
	
	move: function(direction) {
		if(direction == 'left')
			el = this.el.prev();
		else
			el = this.el.next();
		if(el)
			el.focus();
	},
		
	dispose: function(withoutEffect) {
		this.fireEvent('remove', this);

		if(withoutEffect){
			this.destroy();
		}
		else{
			this.el.hide({
				duration: .5,
				callback: function(){
					this.move('right');
					this.destroy()
				}.createDelegate(this)
			});
		}

		return this;
	}

});

