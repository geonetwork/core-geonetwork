.. _java_xml_services:

Java development with XML services
==================================

In this chapter are shown some examples to access GeoNetwork XML
services in Java. Apache http commons library is used to send the requests
and retrieve the results.

Retrieve groups list
--------------------

This example shows a simple request, without requiring authentication, to retrieve the GeoNetwork groups.

Source
``````

::

  package org.geonetwork.xmlservices.client;
  
  import org.apache.commons.httpclient.HttpClient;
  import org.apache.commons.httpclient.methods.PostMethod;
  import org.apache.commons.httpclient.methods.StringRequestEntity;
  import org.jdom.Document;
  import org.jdom.Element;
  
  public class GetGroupsClient {
  
    public static void main(String args[]) {
      **// Create request xml**
      Element request = new Element("request");
      **// Create PostMethod specifying service url**
      String serviceUrl = "http://localhost:8080/geonetwork/srv/en/xml.group.list";
      PostMethod post = new PostMethod(serviceUrl);
      
      try {
        String postData = Xml.getString(new Document(request));
        
        **// Set post data, mime-type and encoding**
        post.setRequestEntity(new StringRequestEntity(postData, "application/xml", "UTF8"));
        
        **// Send request**
        HttpClient httpclient = new HttpClient();
        int result = httpclient.executeMethod(post);
        
        **// Display status code**
        System.out.println("Response status code: " + result);
        
        **// Display response**
        System.out.println("Response body: ");
        System.out.println(post.getResponseBodyAsString());
      
      } catch (Exception ex) {
        ex.printStackTrace();
      
      } finally {
        **// Release current connection to the connection pool
        // once you are done**
        post.releaseConnection();
      }
    }
  }

Output
``````

::

  Response status code: 200

  Response body:
  <?xml version="1.0" encoding="UTF-8"?>
    <response>
    <record>
      <id>2</id>
      <name>sample</name>
      <description>Demo group</description>
      <email>group@mail.net</email>
      <referrer />
      <label>
        <en>Sample group</en>
        <fr>Sample group</fr>
        <es>Sample group</es>
        <de>Beispielgruppe</de>
        <nl>Voorbeeldgroep</nl>
      </label>
    </record>
  </response>

Create a new user (exception management)
----------------------------------------

This example show a request to create a new user, that requires
authentication to complete succesfully. The request is executed without
authentication to capture the exception returned by GeoNetwork.

Source
``````

::

  package org.geonetwork.xmlservices.client;
  
  import org.apache.commons.httpclient.HttpClient;
  import org.apache.commons.httpclient.HttpStatus;
  import org.apache.commons.httpclient.methods.PostMethod;
  import org.apache.commons.httpclient.methods.StringRequestEntity;
  import org.jdom.Document;
  import org.jdom.Element;
  
  public class CreateUserClient {
    public static void main(String args[]) {
    
    **// Create request** xml
    Element request = new Element("request")
    .addContent(new Element("operation").setText("newuser"))
    .addContent(new Element("username").setText("samantha"))
    .addContent(new Element("password").setText("editor2"))
    .addContent(new Element("profile").setText("Editor"))
    .addContent(new Element("name").setText("Samantha"))
    .addContent(new Element("city").setText("Amsterdam"))
    .addContent(new Element("country").setText("Netherlands"))
    .addContent(new Element("email").setText("samantha@mail.net"));
    
    **// Create PostMethod specifying service url**
    String serviceUrl = "http://localhost:8080/geonetwork/srv/en/user.update";
    PostMethod post = new PostMethod(serviceUrl);
    
    try {
      String postData = Xml.getString(new Document(request));
      
      **// Set post data, mime-type and encoding**
      post.setRequestEntity(new StringRequestEntity(postData, "application/xml", "UTF8"));
      
      **// Send request**
      HttpClient httpclient = new HttpClient();
      int result = httpclient.executeMethod(post);
      
      **// Display status code**
      System.out.println("Response status code: " + result);
      
      **// Display response**
      System.out.println("Response body: ");
      String responseBody = post.getResponseBodyAsString();
      System.out.println(responseBody);
    
      if (result != HttpStatus.SC_OK) {
        **// Process exception**
        Element response = Xml.loadString(responseBody, false);
        System.out.println("Error code: " +
        response.getAttribute("id").getValue());
        System.out.println("Error message: " +
        response.getChildText("message"));
      }
      
      } catch (Exception ex) {
        ex.printStackTrace();
      
      } finally {
        // Release current connection to the connection pool
        // once you are done
        post.releaseConnection();
      }
    }
  }

Output
``````

::

  Response status code: 401
  
  Response body:
  <?xml version="1.0" encoding="UTF-8"?>
  <error id="service-not-allowed">
    <message>Service not allowed</message>
    <class>ServiceNotAllowedEx</class>
    <stack>
      <at class="jeeves.server.dispatchers.ServiceManager" file="ServiceManager.java" line="374" method="dispatch" />
      <at class="jeeves.server.JeevesEngine" file="JeevesEngine.java" line="621" method="dispatch" />
      <at class="jeeves.server.sources.http.JeevesServlet" file="JeevesServlet.java" line="174" method="execute" />
      <at class="jeeves.server.sources.http.JeevesServlet" file="JeevesServlet.java" line="99" method="doPost" />
      <at class="javax.servlet.http.HttpServlet" file="HttpServlet.java" line="727" method="service" />
      <at class="javax.servlet.http.HttpServlet" file="HttpServlet.java" line="820" method="service" />
      <at class="org.mortbay.jetty.servlet.ServletHolder" file="ServletHolder.java" line="502" method="handle" />
      <at class="org.mortbay.jetty.servlet.ServletHandler" file="ServletHandler.java" line="363" method="handle" />
      <at class="org.mortbay.jetty.security.SecurityHandler" file="SecurityHandler.java" line="216" method="handle" />
      <at class="org.mortbay.jetty.servlet.SessionHandler" file="SessionHandler.java" line="181" method="handle" />
    </stack>
    <object>user.update</object>
    <request>
      <language>en</language>
      <service>user.update</service>
    </request>
  </error>

Error code: service-not-allowed
Error message: Service not allowed

Create a new user (sending credentials)
---------------------------------------

This example show a request to create a new user, that requires
authentication to complete succesfully.

In this example **httpClient** it's used first to
send a login request to GeoNetwork, getting with
**JSESSIONID** cookie. Nexts requests send to
GeoNetwork using **httpClient** send the
**JSESSIONID** cookie, and are managed as authenticated
requests.

Source
``````

::

  package org.geonetwork.xmlservices.client;
  
  import org.apache.commons.httpclient.Credentials;
  import org.apache.commons.httpclient.HttpClient;
  import org.apache.commons.httpclient.HttpStatus;
  import org.apache.commons.httpclient.UsernamePasswordCredentials;
  import org.apache.commons.httpclient.auth.AuthScope;
  import org.apache.commons.httpclient.methods.PostMethod;
  import org.apache.commons.httpclient.methods.StringRequestEntity;
  import org.jdom.Document;
  import org.jdom.Element;
  
  public class CreateUserClientAuth {
    private HttpClient httpclient;
    
    CreateUserClientAuth() {
      httpclient = new HttpClient();
    }
    
    **/\**
    * Authenticates the user in GeoNetwork and send a request
    * that needs authentication to create a new user
    *
    \*/**
    public void sendRequest() {
      **// Authenticate user**
      if (!login()) System.exit(-1);
      
      **// Create request XML**
      Element request = new Element("request")
      .addContent(new Element("operation").setText("newuser"))
      .addContent(new Element("username").setText("samantha"))
      .addContent(new Element("password").setText("editor2"))
      .addContent(new Element("profile").setText("Editor"))
      .addContent(new Element("name").setText("Samantha"))
      .addContent(new Element("city").setText("Amsterdam"))
      .addContent(new Element("country").setText("Netherlands"))
      .addContent(new Element("email").setText("samantha@mail.net"));
      
      **// Create PostMethod specifying service url**
      String serviceUrl = "http://localhost:8080/geonetwork/srv/en/user.update";
      PostMethod post = new PostMethod(serviceUrl);
      
      try {
        String postData = Xml.getString(new Document(request));
        
        **// Set post data, mime-type and encoding**
        post.setRequestEntity(new StringRequestEntity(postData, "application/xml", "UTF8"));
        
        **// Send request**
        **(httpClient has been set in
        // login request with JSESSIONID cookie)**
        int result = httpclient.executeMethod(post);
        
        **// Display status code**
        System.out.println("Create user response status code: " + result);
        
        if (result != HttpStatus.SC_OK) {
          **// Process exception**
          String responseBody = post.getResponseBodyAsString();
          Element response = Xml.loadString(responseBody, false);
          System.out.println("Error code: " +
          response.getAttribute("id").getValue());
          System.out.println("Error message: " +
          response.getChildText("message"));
        }
      
      } catch (Exception ex) {
        ex.printStackTrace();
      
      } finally {
        **// Release current connection to the connection pool
        // once you are done**
        post.releaseConnection();
      }
    }
    
    **/\**
    * Logins a user in GeoNetwork
    *
    * After login **httpClient** gets with JSSESIONID cookie. Using it
    * for nexts requests, these are managed as "authenticated requests"
    *
    * @return  True if login it's ok, false otherwise
    \*/**
    private boolean login() {
      **// Create request XML**
      Element request = new Element("request")
      .addContent(new Element("username").setText("admin"))
      .addContent(new Element("password").setText("admin"));
      
      **// Create PostMethod specifying login service url**
      String loginUrl =
      "http://localhost:8080/geonetwork/srv/en/xml.user.login";
      PostMethod post = new PostMethod(loginUrl);
      
      try {
        String postData = Xml.getString(new Document(request));
        
        **// Set post data, mime-type and encoding**
        post.setRequestEntity(new StringRequestEntity(postData,
        "application/xml", "UTF8"));
        
        **// Send login request**
        int result = httpclient.executeMethod(post);
        
        **// Display status code and authentication session cookie**
        System.out.println("Login response status code: " + result);
        System.out.println("Authentication session cookie: " +
        httpclient.getState().getCookies()[0]);
        
        return (result == HttpStatus.SC_OK);
      
      } catch (Exception ex) {
        ex.printStackTrace();
        return false;
      
      } finally {
        // Release current connection to the connection pool
        // once you are done
        post.releaseConnection();
      }
    
    }
  
    public static void main(String args[]) {
      CreateUserClientAuth request = new CreateUserClientAuth();
      
      request.sendRequest();
    }
  }

Output
``````

::

  Login response status code: 200
  Authentication session cookie: JSESSIONID=ozj8iyva0agv
  Create user response status code: 200

Trying to run again the program, as the user it's just created we get an exception:

::

  Login response status code: 200
  Authentication session cookie: JSESSIONID=1q09kwg0r6fqe
  Create user response status code: 500

Error response::

  <?xml version="1.0" encoding="UTF-8"?>
  <error id="error">
    <message>ERROR: duplicate key violates unique constraint "users_username_key"</message>
    <class>PSQLException</class>
    <stack>
      <at class="org.postgresql.core.v3.QueryExecutorImpl" file="QueryExecutorImpl.java" line="1548" method="receiveErrorResponse" />
      <at class="org.postgresql.core.v3.QueryExecutorImpl" file="QueryExecutorImpl.java" line="1316" method="processResults" />
      <at class="org.postgresql.core.v3.QueryExecutorImpl" file="QueryExecutorImpl.java" line="191" method="execute" />
      <at class="org.postgresql.jdbc2.AbstractJdbc2Statement" file="AbstractJdbc2Statement.java" line="452" method="execute" />
      <at class="org.postgresql.jdbc2.AbstractJdbc2Statement" file="AbstractJdbc2Statement.java" line="351"
      method="executeWithFlags" />
      <at class="org.postgresql.jdbc2.AbstractJdbc2Statement" file="AbstractJdbc2Statement.java" line="305"
      method="executeUpdate" />
      <at class="jeeves.resources.dbms.Dbms" file="Dbms.java" line="261" method="execute" />
      <at class="org.fao.geonet.services.user.Update" file="Update.java" line="134" method="exec" />
      <at class="jeeves.server.dispatchers.ServiceInfo" file="ServiceInfo.java" line="238" method="execService" />
      <at class="jeeves.server.dispatchers.ServiceInfo" file="ServiceInfo.java" line="141" method="execServices" />
    </stack>
    <request>
      <language>en</language>
      <service>user.update</service>
    </request>
  </error>

Error code: error
Error message: ERROR: duplicate key violates unique constraint "users_username_key"


