#!/bin/bash


function getFileName {
  echo $(ls -1 $1$path_to_compressed/$1*.zip | tr '\n' '\0' | xargs -0 -n 1 basename)
}

function builtMessage {
  echo "$1 built in $2"
}

path_to_compressed="/target/universal"
path_to_activator="activator"

if [ -n "$1" ];
then
  path_to_activator=$1
fi

wings_mqtt="wings-mqtt"
wings_clusterseed="wings-clusterseed"
wings_test="wings-test"
wings_http="wings-http"

$path_to_activator $wings_mqtt/universal:packageBin
wings_mqtt_zip=$(getFileName $wings_mqtt)
wings_mqtt_v=${wings_mqtt_zip%.*}
builtMessage $wings_mqtt $wings_mqtt_zip

$path_to_activator $wings_clusterseed/universal:packageBin
wings_clusterseed_zip=$(getFileName $wings_clusterseed)
wings_clusterseed_v=${wings_clusterseed_zip%.*}
builtMessage $wings_clusterseed $wings_clusterseed_zip

$path_to_activator $wings_test/universal:packageBin
wings_test_zip=$(getFileName $wings_test)
wings_test_v=${wings_test_zip%.*}
builtMessage $wings_test $wings_test_zip

$path_to_activator $wings_http/universal:packageBin
wings_http_zip=$(getFileName $wings_http)
wings_http_v=${wings_http_zip%.*}
builtMessage $wings_http $wings_http_zip

echo "Uploading files"

scp -i ~/.ssh/id_rsa $wings_mqtt$path_to_compressed/$wings_mqtt_zip vagrant@172.16.2.211:/home/vagrant/
ssh -i ~/.ssh/id_rsa vagrant@172.16.2.198 << EOF
unzip /home/vagrant/$wings_mqtt_zip;
$wings_mqtt_v/bin/$wings_mqtt;
EOF

scp -i ~/.ssh/id_rsa $wings_test$path_to_compressed/$wings_test_zip vagrant@172.16.2.198:/home/vagrant/

scp -i ~/.ssh/id_rsa $wings_clusterseed$path_to_compressed/$wings_clusterseed_zip vagrant@172.16.2.198:/home/vagrant/
ssh -i ~/.ssh/id_rsa vagrant@172.16.2.198 << EOF
unzip /home/vagrant/$wings_clusterseed_zip;
$wings_clusterseed_v/bin/$wings_clusterseed &;
$! > $wings_clusterseed_v/bin/RUNNING_PID

EOF

scp -i ~/.ssh/id_rsa $wings_http$path_to_compressed/$wings_http_zip vagrant@172.16.2.198:/home/vagrant/
ssh -i ~/.ssh/id_rsa vagrant@172.16.2.198 << EOF
unzip /home/vagrant/$wings_http_zip;
$wings_http_v/bin/$wings_http -Dhttp.port=9000 -Dhttp.address=0.0.0.0 &;
EOF

## declare an array variable
# declare -a projects=("wings-mqtt" "wings-clusterseed" "wings-test")
# http_project="wings-http"
# projects_to_deploy=("${projects[@]}")
# projects_to_deploy+=(http_project)

# now loop through the above array
# for i in "${projects[@]}"
# do
   # $path_to_activator $i/universal:packageBin
   # or do whatever with individual element of the array
# done

# $path_to_activator $http_project/dist

# You can access them using echo "${arr[0]}", "${arr[1]}" also