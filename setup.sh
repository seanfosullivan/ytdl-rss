#!/usr/bin/env bash
set -euo pipefail

# This setup script is now focused on Docker usage.
# It builds the image and prepares a local data directory for downloads.

APP_NAME="ytdl-rss"
IMAGE_TAG="${APP_NAME}:local"
DATA_DIR="$(pwd)/data/videos"

echo "Creating data directory at: ${DATA_DIR}"
mkdir -p "${DATA_DIR}"

echo "Building Docker image: ${IMAGE_TAG}"
docker build -t "${IMAGE_TAG}" .

cat <<EOF

Build complete.

Run with:
  export YOUTUBE_API_KEY=your_key_here
  docker run --rm -p 8080:8080 \
    -e YOUTUBE_API_KEY=\"$YOUTUBE_API_KEY\" \
    -e DOWNLOADER_OUTPUT_DIR=/data/videos \
    -e DOWNLOADER_YTDLP_PATH=/usr/local/bin/yt-dlp \
    -v ${DATA_DIR}:/data/videos \
    ${IMAGE_TAG}

Or using docker compose:
  export YOUTUBE_API_KEY=your_key_here
  docker compose up --build

Note: The application process starts automatically when the container starts; no extra setup is required inside the container.
EOF