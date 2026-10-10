# Installing with docker

This section describes how to install GeoNetwork with docker.

At the end of the installation process you will end up with the web applications running.

Run with docker

``` shell
docker run --name some-geonetwork -d geonetwork
```

See [docker-library/docs](https://github.com/docker-library/docs/tree/master/geonetwork) for more details.

## Configuration

Settings that a source installation would change in ``WEB-INF/web.xml`` are reached in a container through an environment variable instead, so the image does not have to be rebuilt or its files edited.

The web proxy is the usual case. It ships with a list of hosts it refuses to fetch, and a deployment that has hosts of its own to keep out of reach adds them to that list:

``` shell
docker run --name some-geonetwork -d \
  -e 'GEONETWORK_HTTPPROXY_EXCLUDEHOSTS=^(localhost|...|internal\.example\.org)$' \
  geonetwork
```

The value replaces the shipped list, abbreviated here as ``...``, so start from the value in ``WEB-INF/web.xml`` and add to it. Dropping an entry from it puts that host back within reach of the proxy.

Both settings, the form of their names, and what the default list covers are described in [Hosts and ports the proxy can reach](../maintainer-guide/production-use/index.md#proxy-hosts-and-ports).
