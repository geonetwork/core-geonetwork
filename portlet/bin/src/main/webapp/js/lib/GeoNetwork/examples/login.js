var catalogue;

Ext.onReady(function(){
    catalogue = new GeoNetwork.Catalogue({
        servlet: GeoNetwork.URL
    });
    
    var loginForm = new GeoNetwork.LoginForm({
        renderTo: 'login-form',
        catalogue: catalogue
    });
    
    this.catalogue.on('afterLogin', connected, this);
    this.catalogue.on('afterLogout', disconnected, this);
});


function connected(cat, user){
    Ext.Msg.alert('Login operation successful', user.firstName);
}

function disconnected(cat, user){
    Ext.Msg.alert('Logout operation successful', user);
}
