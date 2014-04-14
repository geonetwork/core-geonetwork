var GeoNetworkAdminGroupPage = function() {
  this.service = 'admin.console';
  this.route = this.service + '#/organization';
  this.groups = {
    sample: {
      name: 'sample',
      desc: '',
      email: '',
      eng: 'Sample group',
      fre: 'Groupe exemple'
    },
    groupTest: {
      name: 'groupTest',
      desc: 'Description of test group',
      email: 'email@geonetwork-opensource.org',
      eng: 'E2E Testing group',
      fre: 'Groupe de test E2E'
    }
  };
  // FIXME: Having the find element
  // here trigger UnknownError: angular is not defined
  // this.groupRepeaters =
  //   browser.findElements(by.repeater('g in groups'));

  this.groupNameInput = element(by.model('groupSelected.name'));
  this.groupDescriptionInput = element(by.model('groupSelected.description'));
  this.groupEmailInput = element(by.model('groupSelected.email'));
  this.groupIdInput = element(by.model('groupSelected.id'));
  this.translationENGInput = element(by.css('#translation-eng'));
  this.translationFREInput = element(by.css('#translation-fre'));

  this.newGroupButton = element(by.css('button[id="gn-btn-group-add"]'));
  this.saveGroupButton = element(by.css('button[id="gn-btn-group-save"]'));
  this.deleteGroupButton = element(by.css('button[id="gn-btn-group-delete"]'));

  // TODO: this is shared among all admin pages
  this.statusTitle = element(by.css('.gn-info > h4'));
  this.statusCloseButton = element(by.css('.gn-info > button'));

  this.get = function() {
    browser.get(this.route);
  };
  this.setGroupName = function(name) {
    this.groupNameInput.sendKeys(name);
  };
  this.setGroup = function(group) {
    this.groupNameInput.sendKeys(group.name);
    //        this.groupDescriptionInput.sendKeys(group.desc);
    this.groupEmailInput.sendKeys(group.email);
  };
  this.setGroupTranslation = function(group) {
    this.translationENGInput.clear();
    this.translationFREInput.clear();
    this.translationENGInput.sendKeys(group.eng);
    this.translationFREInput.sendKeys(group.fre);
  };
};


describe('GeoNetwork group administration page', function() {
  var adminGroupPage;

  beforeEach(function() {
    adminGroupPage = new GeoNetworkAdminGroupPage();
  });

  it('should list one group', function() {
    adminGroupPage.get();
    var groupRepeaters = browser.findElements(by.repeater('g in groups'));
    groupRepeaters.then(function(arr) {
      expect(arr.length).toEqual(1);
      expect(arr[0].getText()).toEqual(adminGroupPage.groups.sample.eng);
    });
  });

  it('should display group information when selected (test sample)',
     function() {
       var groupRepeaters = browser.findElements(by.repeater('g in groups'));
       groupRepeaters.then(function(arr) {
         arr[0].click().then(function() {
           //              browser.takeScreenshot().then(function (base64) {
           //              console.log(base64);
           //              };
           expect(adminGroupPage.groupNameInput.getAttribute('value'))
            .toEqual(adminGroupPage.groups.sample.name);
           expect(adminGroupPage.groupEmailInput.getAttribute('value'))
            .toEqual('');
           expect(adminGroupPage.translationENGInput.getAttribute('value'))
            .toEqual(adminGroupPage.groups.sample.eng);
           expect(adminGroupPage.translationFREInput.getAttribute('value'))
            .toEqual(adminGroupPage.groups.sample.fre);
           // Not sure we can retrieve value of hidden field
           // expect(adminGroupPage.groupIdInput.getAttribute('value'))
           //   .toEqual(2);
         });
       });
     });

  it('should add a new empty group when add button is clicked', function() {
    adminGroupPage.newGroupButton.click();
    adminGroupPage.setGroup(adminGroupPage.groups.groupTest);
    adminGroupPage.saveGroupButton.click();

    var groupRepeaters = browser.findElements(by.repeater('g in groups'));
    groupRepeaters.then(function(groups) {
      expect(groups.length).toEqual(2);
      expect(groups[0].getText()).toEqual(adminGroupPage.groups.groupTest.name);
      expect(groups[1].getText()).toEqual(adminGroupPage.groups.sample.eng);
    });
  });

  // FIXME: We shouldn't be able to create groups with same name
  //  it('should not allow to create another group with same name', function() {
  //    adminGroupPage.get();
  //    adminGroupPage.newGroupButton.click();
  //    adminGroupPage.setGroup(adminGroupPage.groups.groupTest);
  //    adminGroupPage.saveGroupButton.click();
  //
  //    expect(adminGroupPage.statusTitle.getText()).not.toEqual("");
  //    adminGroupPage.statusCloseButton.click();
  //  });


  it('should init translation with group name and save translation on update',
     function() {
       var groupRepeaters = browser.findElements(by.repeater('g in groups'));
       groupRepeaters.then(function(groups) {
         expect(groups[0].getText())
            .toEqual(adminGroupPage.groups.groupTest.name);

         groups[0].click().then(function() {
           expect(adminGroupPage.translationENGInput.getAttribute('value'))
            .toEqual(adminGroupPage.groups.groupTest.name);
           expect(adminGroupPage.translationFREInput.getAttribute('value'))
            .toEqual(adminGroupPage.groups.groupTest.name);
           adminGroupPage.setGroupTranslation(adminGroupPage.groups.groupTest);
           expect(adminGroupPage.translationENGInput.getAttribute('value'))
            .toEqual(adminGroupPage.groups.groupTest.eng);
           expect(adminGroupPage.translationFREInput.getAttribute('value'))
            .toEqual(adminGroupPage.groups.groupTest.fre);


           // Reload, to check translation are saved
           adminGroupPage.get();

           var groupRepeaters =
               browser.findElements(by.repeater('g in groups'));
           groupRepeaters.then(function(groups) {
             expect(groups[0].getText())
              .toEqual(adminGroupPage.groups.groupTest.eng);
           });
         });
       });
     });

  it('should delete a group when delete button is clicked', function() {
    var groupRepeaters = browser.findElements(by.repeater('g in groups'));
    groupRepeaters.then(function(groups) {
      expect(groups.length).toEqual(2);
      expect(groups[0].getText()).toEqual(adminGroupPage.groups.groupTest.eng);

      groups[0].click().then(function() {
        adminGroupPage.deleteGroupButton.click().then(function() {
          var updatedGroupRepeaters =
              browser.findElements(by.repeater('g in groups'));
          updatedGroupRepeaters.then(function(arr) {
            expect(arr.length).toEqual(1);
          });
        });
      });
    });
  });

});
