## Multi-stage build for ytdl-rss

FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Cache dependencies first
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copy sources and build
COPY src ./src
RUN mvn -q -DskipTests package


FROM eclipse-temurin:17-jre
LABEL org.opencontainers.image.title="ytdl-rss" \
      org.opencontainers.image.description="Spring Boot service to download YouTube videos and generate RSS" \
      org.opencontainers.image.source="https://github.com/yourusername/ytdl-rss"

ENV JAVA_OPTS="" \
    YOUTUBE_API_KEY="" \
    DOWNLOADER_YTDLP_PATH="/usr/local/bin/yt-dlp" \
    DOWNLOADER_OUTPUT_DIR="/data/videos"

RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 python3-pip ca-certificates \
    && pip3 install --no-cache-dir yt-dlp \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /workspace/target/*.jar /app/app.jar

# Create data directory for downloads
RUN mkdir -p /data/videos
VOLUME ["/data/videos"]

EXPOSE 8080

ENTRYPOINT ["/bin/sh","-c","java $JAVA_OPTS -jar /app/app.jar"]

