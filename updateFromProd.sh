#!/bin/sh



# Run script from ssh0.bgdi.admin.ch


set -x

DUMP_FILE="dump-`date '+%y%m%d-%H%M'`.sql.gz"
DB_SERVER=admin@db1.bgdi.admin.ch
PROD_SERVER=admin@ec2-176-34-163-138.eu-west-1.compute.amazonaws.com
DEV_SERVER=ec2-46-137-20-48.eu-west-1.compute.amazonaws.com
DEPLOY_DIR=/var/www/vhosts/tc-geocat/private/geocat.ch.deploy

ssh $DB_SERVER "export PGPASSWORD="www-data"; pg_dump -T uback -T basicdata -T harvester -T backup -U www-data -E UTF-8 -Z 9 geocat >  $DUMP_FILE"

scp $DB_SERVER:$DUMP_FILE .
scp $DUMP_FILE $DEV_SERVER:$DEPLOY_DIR/hooks/geocat/initialDump.sql.gz
ssh "rm $DUMP_FILE"
rm $DUMP_FILE

GEOCAT_DATA_ZIP=geocat-data.zip
FILTER="--exclude=*/.svn/*"
ssh $PROD_SERVER "rm /tmp/$GEOCAT_DATA_ZIP; cd /srv/tomcat/geocat/private/geocat; zip $FILTER -r /tmp/$GEOCAT_DATA_ZIP config; zip $FILTER -r /tmp/$GEOCAT_DATA_ZIP index; zip $FILTER -r /tmp/$GEOCAT_DATA_ZIP data"

scp -r $PROD_SERVER:/tmp/$GEOCAT_DATA_ZIP .
scp -r $GEOCAT_DATA_ZIP $DEV_SERVER:/tmp/
ssh $DEV_SERVER "rm -rf $DEPLOY_DIR/hooks/geocat/data/*; unzip -d $DEPLOY_DIR/hooks/geocat/data /tmp/$GEOCAT_DATA_ZIP; rm /tmp/$GEOCAT_DATA_ZIP"

ssh $PROD_SERVER "rm -f /tmp/$GEOCAT_DATA_ZIP"
rm -f $GEOCAT_DATA_ZIP

