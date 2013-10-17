'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('navigation', function () {
    beforeEach(function () {
        browser().navigateTo(appDir + 'index-test.html');
    });


    it('clicking on Discovery Service should direct to geocat search page', function () {
        var link = element("#discovery-service-link").attr('href');
        expect(link).toMatch(/.*\/srv\/eng\/geocat/);
    });

    it ('The navigation buttons on menu bar should all change page', function() {

        var  menuNavigateTo = function (from, button, to) {
            browser().navigateTo(from);

            element('#nav-'+button).click()
            expect(browser().location().url()).toBe(to);
        }

        $(['contacts', 'formats', 'keywords', 'extents']).each(function (i, el){
            menuNavigateTo('#/validated/'+el, 'nonvalidated', '/nonvalidated/'+el);
            menuNavigateTo('#/nonvalidated/'+el, 'validated', '/validated/'+el);
            menuNavigateTo('#/nonvalidated/'+el, 'deleted', '/deleted');
            menuNavigateTo('#/validated/'+el, 'deleted', '/deleted');
        });
        menuNavigateTo('#/deleted', 'nonvalidated', '/nonvalidated/contacts');
        menuNavigateTo('#/deleted', 'validated', '/validated/contacts');
    });



});
