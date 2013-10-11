SharedObjectsDev.run(function ($httpBackend) {
    $httpBackend.whenGET('partials/shared.html').passThrough();
    $httpBackend.whenGET('partials/menubar.html').passThrough();
    $httpBackend.whenGET('partials/modal-confirm-delete.html').passThrough();
    $httpBackend.whenGET('partials/modal-confirm-validate.html').passThrough();
    $httpBackend.whenGET('partials/modal-edit-common.html').passThrough();
    $httpBackend.whenGET('partials/modal-edit-contacts.html').passThrough();
    $httpBackend.whenGET('partials/modal-edit-extents.html').passThrough();
    $httpBackend.whenGET('partials/modal-edit-formats.html').passThrough();
    $httpBackend.whenGET('partials/modal-edit-keywords.html').passThrough();
    $httpBackend.whenGET('partials/modal-executing-operation.html').passThrough();
    $httpBackend.whenGET('partials/modal-reject.html').passThrough();
    $httpBackend.whenGET('../../../srv/eng/header-template.xml').passThrough();

    $httpBackend.whenGET('../../../srv/eng/reusable.list.js?validated=false&type=contacts').respond([{
        "url":"local://shared.user.edit?closeOnSave&id=5&validated=n&operation=fullupdate",
        "id":"5",
        "type":"contact",
        "xlink":"local://xml.user.get?id=5*",
        "desc":"Non-Validated Contact",
        "search":"5 Non-Validated Contact"
    }]);
    $httpBackend.whenGET('../../../srv/eng/reusable.list.js?validated=true&type=contacts').respond([{
        "url":"local://shared.user.edit?closeOnSave&id=5&validated=n&operation=fullupdate",
        "id":"6",
        "type":"contact",
        "xlink":"local://xml.user.get?id=6*",
        "desc":"Validated Contact",
        "search":"6 Validated Contact"
    }]);
    $httpBackend.whenGET('../../../srv/eng/reusable.references?id=5&type=contacts&validated=false').respond([{
        "id":"886",
        "title":"Non-validated Metadata",
        "name":"nvowner",
        "email":"nvemail"
    }]);
    $httpBackend.whenGET('../../../srv/eng/reusable.references?id=6&type=contacts&validated=true').respond([{
        "id":"887",
        "title":"Validated Metadata",
        "name":"owner",
        "email":"email"
    }]);

});