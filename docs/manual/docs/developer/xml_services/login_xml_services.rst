.. _login_xml_services:

Login and Logout services
=========================

Login services
--------------

.. index:: xml.user.login

.. _xml.user.login:

GeoNetwork standard login (xml.user.login)
``````````````````````````````````````````

The **xml.user.login** service is used to
authenticate the user in GeoNetwork. Authenticated users can use XML services
that require authentication such as those used to maintain
group or user information.

Request
^^^^^^^

Parameters:

- **username** (mandatory): Login for the user to authenticate

- **password** (mandatory): Password for the user to authenticate

Login request example::

  Url:
  http://localhost:8080/geonetwork/srv/en/xml.user.login

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request>
      <username>admin</username>
      <password>admin</password>
  </request>

Response
^^^^^^^^

When user authentication is successful HTTP status code 200 is returned along with  an XML response as follows::

 <ok/>

If the response headers are examined, they will look something like the following:::

  Expires: Thu, 01 Jan 1970 00:00:00 GMT
  Set-Cookie: JSESSIONID=1xh3kpownhmjh;Path=/geonetwork
  Content-Type: application/xml; charset=UTF-8
  Pragma: no-cache
  Cache-Control: no-cache
  Transfer-Encoding: chunked

The authentication process sets the **JSESSIONID** cookie with the authentication token. This token should be sent as part of the request to all services that 
need authentication.

If the execution of the login request is not successful then an HTTP 500 status code error is returned along with an XML document that describes the exception/what went wrong. An example of such a response is:::
 
  <error id="user-login">
    <message>User login failed</message>
    <class>UserLoginEx</class>
    <stack>
      <at class="org.fao.geonet.services.login.Login" file="Login.java" line="90" method="exec" />
      <at class="jeeves.server.dispatchers.ServiceInfo" file="ServiceInfo.java" line="238" method="execService" />
      <at class="jeeves.server.dispatchers.ServiceInfo" file="ServiceInfo.java" line="141" method="execServices" />
      <at class="jeeves.server.dispatchers.ServiceManager" file="ServiceManager.java" line="377" method="dispatch" />
      <at class="jeeves.server.JeevesEngine" file="JeevesEngine.java" line="621" method="dispatch" />
      <at class="jeeves.server.sources.http.JeevesServlet" file="JeevesServlet.java" line="174" method="execute" />
      <at class="jeeves.server.sources.http.JeevesServlet" file="JeevesServlet.java" line="99" method="doPost" />
      <at class="javax.servlet.http.HttpServlet" file="HttpServlet.java" line="727" method="service" />
      <at class="javax.servlet.http.HttpServlet" file="HttpServlet.java" line="820" method="service" />
      <at class="org.mortbay.jetty.servlet.ServletHolder" file="ServletHolder.java" line="502" method="handle" />
    </stack>
    <object>admin2</object>
    <request>
      <language>en</language>
      <service>user.login</service>
    </request>
  </error>

See :ref:`exception_handling` for more details. 

Errors
^^^^^^

- **Missing parameter (error id: missing-parameter)**, when
  mandatory parameters are not send. Returns 500 HTTP code

- **bad-parameter XXXX**, when an empty username or password
  is provided. Returns 500 HTTP code

- **User login failed (error id: user-login)**, when login
  information is not valid. Returns 500 HTTP code

Logout service
--------------

.. index:: xml.user.logout

Logout (xml.user.logout)
````````````````````````

The **xml.user.logout** service clears the user authentication session, removing the **JSESSIONID** cookie.

Request
^^^^^^^

Parameters:

- **None**:This request requires no parameters however the **JSESSIONID** token obtained from ``xml.user.login`` should be included as this is the session that will be cleared..

Logout request example::

  Url:
  http://localhost:8080/geonetwork/srv/en/xml.user.logout

  Mime-type:
  application/xml

  Post request:
  <?xml version="1.0" encoding="UTF-8"?>
  <request/>

Response
^^^^^^^^

Logout response example::

  <ok />


