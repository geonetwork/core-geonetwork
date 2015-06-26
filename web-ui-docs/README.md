# JS documentation

This module generate documentation for the web-ui module.

## Dependencies

* [node](http://nodejs.org)
* [grunt-cli](https://github.com/gruntjs/grunt-cli)
* [ngdoc](https://github.com/m7r/grunt-ngdocs)

## Generate the documentation

Use maven to build the documentation:
```
mvn clean install
```

The documentation is generated in target/docs folder and could be published on the website.

Another option is to use grunt directly from the command line.
For that, you need to edit the `config.js` file and uncomment the line

    grunt.registerTask('default', [ 'clean', 'ngdocs', 'connect' ]);

Then run `grunt --gruntfile config.js` and open http://localhost:8000/docs in your Browser.
