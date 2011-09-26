Ext.define('Ext.ux.form.SearchField', {
    extend: 'Ext.form.field.Trigger',
    
   	globalTimeout: null,  
   	
    alias: 'widget.searchfield',
    
    trigger1Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
    
    trigger2Cls: Ext.baseCSSPrefix + 'form-search-trigger',
    
    hasSearch : false,
    paramName : 'query',
    
    enableKeyEvents: true,
    
    initComponent: function(){
        this.callParent(arguments);
        
        this.on('specialkey', function(f, e){
            if(e.getKey() == e.ENTER){
				e.stopPropagation();
            	e.preventDefault();
                this.onTrigger2Click();
            }
        }, this);
        
        this.on('keyup', function(f, e){
        	e.stopPropagation();
            e.preventDefault();
            if(this.globalTimeout != null) 
            	clearTimeout(this.globalTimeout);
            var self = this;
            this.globalTimeout = setTimeout(function() {
            	self.onTrigger2Click()
            	},300);
        }, this);
        
    },
    
    afterRender: function(){
        this.callParent();
        this.triggerEl.item(0).setDisplayed('none');  
    },
    
    onTrigger1Click : function(){
        var me = this,
            store = me.store,
            proxy = store.getProxy(),
            val;
            
        if (me.hasSearch) {
        	this.store.clearFilter();
            me.setValue('');
            me.hasSearch = false;
            me.triggerEl.item(0).setDisplayed('none');
            me.doComponentLayout();
        }
    },

    onTrigger2Click : function(){
    	this.globalTimeout = null;  
        var me = this,
            store = me.store,
            proxy = store.getProxy(),
            value = me.getValue();
            
        if (value.length < 1) {
            me.onTrigger1Click();
            return;
        }
        
        this.store.filterBy(function(record){
        	var data = record.data;
        	for(i in data){
        		if(data[i] != null && data[i].toString().toLowerCase().indexOf(value.toLowerCase()) != -1)
        			return true;
        	}
        });
        
        me.hasSearch = true;
        me.triggerEl.item(0).setDisplayed('block');
        me.doComponentLayout();
    }
});