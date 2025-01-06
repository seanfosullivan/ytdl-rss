#!/bin/bash

# Install Python on a Debian-based system.
# Update package list and upgrade system packages
echo "Updating package list..."
sudo apt update

# Install prerequisite packages
echo "Installing prerequisites..."
sudo apt install -y software-properties-common

# Add deadsnakes PPA for more Python versions (optional)
# Uncomment the next line if you want to install a specific version of Python
# sudo add-apt-repository ppa:deadsnakes/ppa

# Install Python 3
echo "Installing Python 3..."
sudo apt install -y python3 python3-pip

# Verify installation
echo "Python version:"
python3 --version

echo "Pip version:"
pip3 --version

# Optional: Install additional Python packages or tools
# sudo pip3 install [package-name]

echo "Python installation complete."

# Install java
echo "Installing OpenJDK 17..."
sudo apt install -y openjdk-17-jdk

# Download ytdlp
# Define the download URL and output directory
DOWNLOAD_URL="https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp"
OUTPUT_DIR="$HOME/Downloads"
OUTPUT_FILE="${OUTPUT_DIR}/yt-dlp"

# Create the output directory if it doesn't exist
if [ ! -d "$OUTPUT_DIR" ]; then
  echo "Creating output directory: $OUTPUT_DIR"
  sudo mkdir -p "$OUTPUT_DIR"
fi

# Use curl to download the file and save it to the output directory
echo "Downloading yt-dlp..."
curl -L "$DOWNLOAD_URL" -o "$OUTPUT_FILE"

# Check if the download was successful
if [ $? -eq 0 ]; then
  echo "Download completed successfully. Saved to $OUTPUT_FILE"
  # make the file executable
  chmod +x "$OUTPUT_FILE"
else
  echo "Download failed."
fi