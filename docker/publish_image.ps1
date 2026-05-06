# Script to push images to DockerHub
$DOCKER_USER = if ($env:DOCKER_USER) { $env:DOCKER_USER } else { "nietodiazdaniel" }
Write-Host "--- Publishing app-service to DockerHub ---" -ForegroundColor Cyan
docker push "$DOCKER_USER/app-service:latest"

Write-Host "--- Publishing utility-service to DockerHub ---" -ForegroundColor Cyan
docker push "$DOCKER_USER/utility-service:latest"

Write-Host "Publishing completed." -ForegroundColor Green
