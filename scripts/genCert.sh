#!/bin/bash

SERVER=$1
PASSWORD=$2

KEY_OPTS="-keyalg RSA -alias nashorn-ssr -validity 1095 -keysize 2048"
KEYSTORE_OPTS="-keystore ${SERVER}.p12 -storetype pkcs12"
DNAME="CN=${SERVER}, OU=Cloudlake, O=OCC, L=Emerald 4, S=Oz, C=Emerald"
PASSWORD_OPTS="-keypass ${PASSWORD} -storepass ${PASSWORD} -v"


keytool -genkey ${KEY_OPTS} -alias nashorn-ssr ${KEYSTORE_OPTS} -dname "${DNAME} ${PASSWORD_OPTS}"