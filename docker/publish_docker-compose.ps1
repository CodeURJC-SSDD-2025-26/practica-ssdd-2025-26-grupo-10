# Script to publish the Docker Compose YAML as a real OCI Artifact using ORAS
$DOCKER_USER = if ($env:DOCKER_USER) { $env:DOCKER_USER } else { "nietodiazdaniel" }

Write-Host "--- Publishing Docker Compose as OCI Artifact (Native) ---" -ForegroundColor Cyan

# Using native ORAS (it inherits your Windows 'docker login' automatically)
# Requirement: Install ORAS first (winget install ORASProject.ORAS)
oras push "docker.io/$DOCKER_USER/ecomostoles-compose:latest" "docker-compose.yml:application/vnd.docker.compose.project"

Write-Host "OCI Artifact publication process finished." -ForegroundColor Green
