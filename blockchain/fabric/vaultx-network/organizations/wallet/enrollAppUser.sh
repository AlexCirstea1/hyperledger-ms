#!/bin/bash

# Function to print information
function infoln() {
  echo -e "\033[0;32m${1}\033[0m"
}

# Function to print errors
function errorln() {
  echo -e "\033[0;31m${1}\033[0m"
}

# Find the project root directory
PROJECT_ROOT=$(cd $(dirname $0)/../../../..; pwd)

# Create the wallet directory if it doesn't exist
WALLET_DIR="${PROJECT_ROOT}/blockchain/fabric/vaultx-network/organizations/wallet"
mkdir -p ${WALLET_DIR}

infoln "Compiling and running the enrollment program..."

# Set classpath to include the required JAR files
FABRIC_PATH="${PROJECT_ROOT}/target/classes"
FABRIC_SDK_JAR="${PROJECT_ROOT}/target/dependency/fabric-gateway-java-2.2.0.jar"
FABRIC_CA_JAR="${PROJECT_ROOT}/target/dependency/fabric-ca-client-2.2.0.jar"

# If the compiled class file exists, run it
if [ -f "${WALLET_DIR}/EnrollAppUser.class" ]; then
  infoln "Running the enrollment program..."
  cd ${WALLET_DIR}
  java -cp ${FABRIC_PATH}:${FABRIC_SDK_JAR}:${FABRIC_CA_JAR}:. EnrollAppUser
else
  # Otherwise compile the Java program
  infoln "Compiling the enrollment program first..."
  cd ${WALLET_DIR}
  javac -cp ${FABRIC_PATH}:${FABRIC_SDK_JAR}:${FABRIC_CA_JAR} EnrollAppUser.java
  
  if [ $? -eq 0 ]; then
    infoln "Compilation successful. Running the enrollment program..."
    java -cp ${FABRIC_PATH}:${FABRIC_SDK_JAR}:${FABRIC_CA_JAR}:. EnrollAppUser
  else
    errorln "Compilation failed. Please check your Java code."
    exit 1
  fi
fi

infoln "Wallet setup complete!"
