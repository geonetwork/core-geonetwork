#/bin/sh

set -x

DUMP_FILE="dump-`date '+%y%m%d-%H%M'`.sql.gz"
DB_SERVER=admin@db1.bgdi.admin.ch
PROD_SERVER=admin@ec2-176-34-163-138.eu-west-1.compute.amazonaws.com
DEV_SERVER=ec2-46-137-20-48.eu-west-1.compute.amazonaws.com
DEPLOY_DIR=/var/www/vhosts/tc-geocat/private/geocat.ch.deploy

copy () {
  ssh -A  ssh0.bgdi.admin.ch "scp -r $PROD_SERVER:/srv/tomcat/geocat/private/geocat/$2 ."
  ssh -A  ssh0.bgdi.admin.ch "scp -r $1 $DEV_SERVER:$DEPLOY_DIR/hooks/geocat/data"
  ssh -A  ssh0.bgdi.admin.ch "rm -rf $1"
}
ssh -A  ssh0.bgdi.admin.ch "ssh $DB_SERVER pg_dump -T uback -T basicdata -T harvester -T backup -U www-data -E UTF-8 -Z 9 geocat > $DUMP_FILE"
ssh -A  ssh0.bgdi.admin.ch "scp $DB_SERVER:$DUMP_FILE ."
ssh -A  ssh0.bgdi.admin.ch "scp $DUMP_FILE $DEV_SERVER:$DEPLOY_DIR/hooks/geocat/initialDump.sql.gz"
ssh -A  ssh0.bgdi.admin.ch "rm $DUMP_FILE"

copy logos data/resources/images/logos
copy data data/metadata_data
copy codelist config/codelist

