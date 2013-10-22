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
    $httpBackend.whenGET('partials/modal-edit-deleted.html').passThrough();
    $httpBackend.whenGET('partials/modal-executing-operation.html').passThrough();
    $httpBackend.whenGET('partials/modal-reject.html').passThrough();
    $httpBackend.whenGET('../../../srv/eng/header-template.xml').passThrough();

    var newMetadata = function(id, valid, type) {
        var title = (valid ? "Validated" : "Non-validated") + " " + type + " Metadata";
        var prefix = valid ? "" : "nv";
        return {
                   "id":id,
                   "title": title,
                   "name": prefix+"owner",
                   "email": prefix+"email"
               }
    }

    var newContact = function(id, title) {
       return {
          "url":"local://shared.user.edit?closeOnSave&id="+id+"&validated=n&operation=fullupdate",
          "id":id,
          "type":"contact",
          "xlink":"local://xml.user.get?id="+id+"*",
          "desc":title+" Contact",
          "search":id+" "+title+" Contact"
       }
    }

    var newFormat = function(id, title) {
       return {
          "url":"local://format.admin?closeOnSave&id="+id+"&dialog=true",
          "id":id,
          "type":"format",
          "xlink":"local://xml.format.get?id="+id,
          "desc":title+" Format",
          "search":id+" "+title+" Format"
       }
    }
    var newKeyword = function(id, title) {
       return {
          "url":'local://thesaurus.admin?thesaurus=local._none_.non_validated&id'+id+'&lang=eng',
          "id":id,
          "type":"keyword",
          "xlink": 'local://che.keyword.get?thesaurus=local._none_.non_validated&id='+id,
          "desc":title+" Keyword",
          "search":id+" "+title+" Keyword"
       }
    }
    var newExtent = function(id, title) {
       return {
          "id":id,
          "type":"keyword",
          "url": "local://extent.edit?closeOnSave&crs=EPSG:21781&wfs=default&typename=gn:non_validated&id="+id,
          "xlink": "local://xml.extent.get?id="+id+"&wfs=default&typename=gn:non_validated&*",
          "desc":title+" Extent",
          "search":id+" "+title+" Extent"
       }
    }
    var newDeleted = function(id, title) {
       return {
            "date": "2013-09-16T10:32:45",
            "desc": "local://xml.user.get?id="+id+"&schema=iso19139.che&role=pointOfContact",
            "id": id,
            "xlink" : 'local://xml.reusable.deleted?id='+id,
            "search" : id+'local://xml.user.get?id="+id+"&schema=iso19139.che&role=pointOfContact2013-09-16T10:32:45',
            "type" : "deleted"
       }
    }
    var id = 1;
    var register = function(type, factory) {
        var id1 = id++;
        var id2 = id++;
        var id3 = id++;
        $httpBackend.whenGET('../../../srv/eng/reusable.list.js?validated=false&type='+type).respond([
            factory(id1,"Non-Validated"),factory(id2,"Non-Validated")
        ]);
        $httpBackend.whenGET('../../../srv/eng/reusable.list.js?validated=true&type='+type).respond([factory(id3,"Validated")]);
        $httpBackend.whenGET('../../../srv/eng/reusable.references?id='+id1+'&type='+type+'&validated=false').respond([newMetadata(100*id1, false, type)]);
        $httpBackend.whenGET('../../../srv/eng/reusable.references?id='+id2+'&type='+type+'&validated=false').respond([]);
        $httpBackend.whenGET('../../../srv/eng/reusable.references?id='+id3+'&type='+type+'&validated=true').respond([newMetadata(100*id3, true, type)]);
    };

    register('contacts', newContact);
    register('formats', newFormat);
    register('keywords', newKeyword);
    register('extents', newExtent);
    register('deleted', newDeleted);

});