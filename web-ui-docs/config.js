module.exports = function(grunt) {

  grunt.loadNpmTasks('grunt-ngdocs');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-connect');
  grunt.loadNpmTasks('grunt-contrib-clean');

  grunt.initConfig({
    ngdocs : {
      options : {
        dest: 'target/jsdocs',
        scripts : [ 'angular.js', '../src.js' ],
        html5Mode : true,
        startPage : '/api'
      },
      api : {
        src : [ '../web-ui/src/main/resources/catalog/components/**/*.js' ],
        title : 'JS API Documentation'
      },
      service: {
        src : ['target/generated-resources/*.ngdoc'],
        title: 'Services Documentation'
      }
    },
    connect : {
      options : {
        keepalive : true
      },
      server : {}
    },
    clean : [ 'target/jsdocs' ]
  });

//  grunt.registerTask('default', [ 'clean', 'ngdocs', 'connect' ]);
  grunt.registerTask('default', [ 'clean', 'ngdocs']);

};
