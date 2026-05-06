# Script to publish the OCI Artifact (Docker Compose)
$DOCKER_USER = "nietodiazdaniel"

Write-Host "--- Publishing Docker Compose as OCI Artifact ---" -ForegroundColor Cyan

# Ensuring the images are tagged correctly for the compose push
# Note: The docker-compose.yml must use the ${DOCKER_USER} variable or be updated
docker compose -f docker-compose.yml push

Write-Host "OCI Artifact publication process finished." -ForegroundColor Green
