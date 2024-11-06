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


# Download ytdlp
# Define the download URL and output directory
DOWNLOAD_URL="https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp"
OUTPUT_DIR="/Downloads"
OUTPUT_FILE="${OUTPUT_DIR}/yt-dlp"

# Create the output directory if it doesn't exist
if [ ! -d "$OUTPUT_DIR" ]; then
  echo "Creating output directory: $OUTPUT_DIR"
  mkdir -p "$OUTPUT_DIR"
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


# Install and setup MySQL 
# Update package list
echo "Updating package list..."
sudo apt-get update -y

# Install MySQL Server
echo "Installing MySQL Server..."
sudo apt-get install mysql-server -y

# Enable MySQL to start on boot
echo "Enabling MySQL to start on boot..."
sudo systemctl enable mysql

# Start MySQL service
echo "Starting MySQL service..."
sudo systemctl start mysql

# Wait for MySQL to initialize
sleep 5

# Set up MySQL root password and create a new database
echo "Setting up MySQL database..."
MYSQL_ROOT_PASSWORD="your_root_password"  # Change this to a secure password
MYSQL_DATABASE="your_database_name"       # Change to your desired database name

# Run the SQL commands to set up the database
sudo mysql -u root -e "CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE};"
sudo mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASSWORD}';"
sudo mysql -u root -e "FLUSH PRIVILEGES;"

# Show success message
echo "MySQL installed and database '${MYSQL_DATABASE}' created successfully!"