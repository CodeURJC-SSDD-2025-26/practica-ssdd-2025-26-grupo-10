#!/bin/bash

# EcoMóstoles OpenAPI Generator Script
# This script downloads the contract definition from the running service.

# 1. Ensure the docs directory exists
mkdir -p docs

echo "--------------------------------------------------------"
echo "  EcoMóstoles OpenAPI Contract Generator"
echo "--------------------------------------------------------"
echo "[INFO] Requirement: The 'app-service' must be running on https://localhost:8443"
echo "[INFO] Fetching YAML contract..."

# 2. Download the OpenAPI YAML file using curl
# -k: Ignore SSL certificate errors (self-signed)
# -s: Silent mode
# -o: Output file path
curl -k -s https://localhost:8443/v3/api-docs.yaml -o docs/api-docs.yaml

# 3. Verify if the file was created and is not empty
if [ -s "docs/api-docs.yaml" ]; then
    echo "[SUCCESS] File 'docs/api-docs.yaml' has been generated correctly."
    echo "--------------------------------------------------------"
else
    echo "[ERROR] Failed to download the contract. Is the server running on port 8443?"
    echo "--------------------------------------------------------"
    exit 1
fi
