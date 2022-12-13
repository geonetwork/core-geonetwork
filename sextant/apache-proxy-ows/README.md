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

docker build . -t proxy-apache-sextant
```

### Utilisation

Déplacer le fichier de configuration dans le dossier input_output/
```bash 
bash apache_proxy.sh input_output/sample_conf.yml
```

`-c ` si besoin des headers


Avec `sample_conf.yaml` la configuration.
Le fichier de sortie se trouve dans le dossier input/output
