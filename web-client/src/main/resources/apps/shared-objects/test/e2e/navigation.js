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

});
