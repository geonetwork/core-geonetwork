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
Soit utiliser le script bash:

```bash 
bash yq_generate_command.sh input_output/sample_conf.yml
```

`-c ` si besoin des headers

soit directement lance la commande docker run

```bash
docker run -ti -v $(pwd)/input_output:/input_output
-v /home/jwaddle/Documents/sextant/sextant-geonetwork-7/sextant-geonetwork/sextant/apache-proxy-ows/python/tests:/app/./tests
proxy-apache-sextant python3 /app/apache_proxy.py /input_output/sample_conf.yml -c
```

Avec `sample_conf.yaml` la configuration.
Le fichier de sortie se trouve dans le dossier input/output
