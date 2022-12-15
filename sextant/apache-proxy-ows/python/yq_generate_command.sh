#!/usr/bin/env bash

# Aide
if [ "$1" == "-h" ] ; then
    echo "Utilisation: ./ `basename $0` fichier_de_configuration.yaml"
    echo "This Help File: sh `basename $0` -h"
    echo "Rajouter nom de colonne? `basename $0` -c"
    exit 0
fi
# Verifie que le fichier de conf est bien présent
if [ ! -e "$1" ]; then
    echo "Fichier de configuration introuvable ou non spécifié"
    exit 0
fi
conf_file="$1"

if [ "$2" == "-c" ] ; then
  header='-c'
fi
docker_command='docker run -ti -v $(pwd)/input_output:/input_output '

# récupération des configurations
readarray identityMappings < <(yq eval ".sites[].filename" $1)
for identityMapping in "${identityMappings[@]}"; do
    echo "Travail sur : $identityMapping"
    # transform les chemin relatif en chemin absolu si nécessaire afin de monter les volumes dans docker
    dir_=`echo $identityMapping |xargs dirname`
    # shellcheck disable=SC2164
    dir_absolu="$(cd "$(dirname "$dir_")"; pwd)/$(basename "$dir_")"
    if [ "$dir_absolu" = "$dir_" ]; then
      volume_command="-v ${dir_}:${dir_} "
    else
        volume_command="-v ${dir_absolu}:/app/${dir_} "
    fi
    # command pour monter le volume dans le conteneur
    docker_command+=$volume_command
done

# ajout des volumes, et execution de la commande python dans l'image proxy-apache-sextant
docker_command="${docker_command} proxy-apache-sextant python3 /app/apache_proxy.py /${conf_file} ${header}"

# print de la commande docker
echo "$docker_command"
