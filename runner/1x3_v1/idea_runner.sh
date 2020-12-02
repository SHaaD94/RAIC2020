./aicup2020 --config 1x3_v1/config.json &

#sleep 1
java -jar 1x3_v1/v1.jar 127.0.0.1 31002 &
java -jar 1x3_v1/v1.jar 127.0.0.1 31003 &
java -jar 1x3_v1/v1.jar 127.0.0.1 31004