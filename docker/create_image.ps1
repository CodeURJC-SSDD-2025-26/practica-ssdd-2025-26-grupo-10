# Script to build images locally without host dependencies
$DOCKER_USER = if ($env:DOCKER_USER) { $env:DOCKER_USER } else { "nietodiazdaniel" }

Write-Host "--- Building app-service image ---" -ForegroundColor Cyan
docker build -t "$DOCKER_USER/app-service:latest" -f app-service.Dockerfile ..

Write-Host "--- Building utility-service image ---" -ForegroundColor Cyan
docker build -t "$DOCKER_USER/utility-service:latest" -f utility-service.Dockerfile ..

Write-Host "Build process completed." -ForegroundColor Green
