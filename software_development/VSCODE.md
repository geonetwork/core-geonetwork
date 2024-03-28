# VS Code

In the 2023 StackOverflow
[developer survey](https://survey.stackoverflow.co/2023/#section-most-popular-technologies-integrated-development-environment),
VS Code was the most popular IDE.

This document describes the VS Code development setup to ease contributions using VS Code.

## Setting up

1. *Open Folder...* and select the local git clone. This creates a `.vscode` folder (which is covered by `.gitignore`)

The Java extensions should be installed by default. If not, install the "Extension Pack for Java" extension.

To get more insights on Spring Boot, install the extension
[Spring Boot Extension Pack](https://marketplace.visualstudio.com/items?itemName=vmware.vscode-boot-dev-pack)

To interact with the Jetty server, install the extension
[Community Server Connectors](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-community-server-connector).

In the sidebar Explorer > Servers > Community Server Connector right-click and "Create New Server..." > Yes (Download
server?) and select the Jetty 9.4.x server.

## Building

Follow the [build
instructions](https://github.com/geonetwork/core-geonetwork/blob/main/software_development/SOURCE.md#source-code)

## Running

Right-click on web/target/geonetwork.war and "Run on Server" > select the Jetty 9.4.x server > project name "gn-web-app".

## Debugging

Right-click on web/target/geonetwork.war and "Debug on Server" > select the Jetty 9.4.x server > project name "gn-web-app".
