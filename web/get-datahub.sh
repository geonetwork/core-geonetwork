#!/bin/sh

BRANCH_NAME=$1

REPO_URL="git@github.com:geonetwork/geonetwork-ui.git"
DATAHUB_FOLDER="src/main/datahub"

if [ -z "$BRANCH_NAME" ]; then
  echo "get-datahub.sh Error: BRANCH_NAME is not set."
  exit 1
fi

# Remove datahub directory if it exists
if [ -d "$DATAHUB_FOLDER" ]; then
  rm -rf "$DATAHUB_FOLDER"
fi

mkdir -p "$DATAHUB_FOLDER"

# Create a temporary directory for cloning
TEMP_DIR=$(mktemp -d)

# Get the current directory
CURRENT_DIR=$(pwd)

# Clone the repository into the temporary directory
git clone --branch "$BRANCH_NAME" "$REPO_URL" "$TEMP_DIR"

# Move the contents of the specified folder to the datahub folder
mv "$TEMP_DIR"/* "$CURRENT_DIR/$DATAHUB_FOLDER"

# Clean up the temporary directory
rm -rf "$TEMP_DIR"
