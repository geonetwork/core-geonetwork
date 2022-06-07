Apache Proxy builder
========================

Le fichier de configuration doit être au format suivant:

``` yaml
sites: 
  - filename : /home/sextant/data_QGIS/_conf/profiles/wms/*.yml
    url_publique : /services/wms/$1
    url_interne : http://vwiz-docker7.ifremer.fr:9080/ows/p/wfs/$1

```


### Installation

```bash
sudo wget -qO /usr/local/bin/yq https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64                                                                                           
sudo chmod a+x /usr/local/bin/yq 
```

### Utilisation

```bash
bash apache-proxy-ows.sh sample_conf.yml
```

Avec `sample_conf.yaml` la configuration.

Le fichier de sortie se trouve à l'endroit ou le script a été lancé `apache_conf.csv`
