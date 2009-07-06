This dir contains the actual services protocol tests.
Each file contains a test to be executed by the test framework.
A test file contains two XML-fragments:
<request> : the request to be dispatched
<response> : the response expected

Symbolic vars in the form ${var} may be used to capture and
reuse important values such asIds and Uuid's. These can also
be used to cater for variant response values such as date/time etc.
