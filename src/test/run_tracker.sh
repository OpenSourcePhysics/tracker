#!/bin/bash
# --- Configuration ---

# 1. Define the full path to the JAR file
JAR_PATH="/Applications/Tracker.app/Contents/app/tracker.jar"

# 2. Define the Java executable path (Standard on macOS)
JAVA_CMD="/usr/bin/java"

# --- Execution Logic ---

echo "Checking for tracker.jar at: $JAR_PATH"

# Check if the JAR file exists
if [[ ! -f "$JAR_PATH" ]]; then
    echo "Error: The JAR file was not found at the specified path."
    echo "Please verify the path /Applications/Tracker.app/Contents/app/tracker.jar"
    exit 1
fi

echo "Starting Tracker application..."

# Execute the JAR file using the Java command
# The -jar flag is essential for running the application
"$JAVA_CMD" -jar "$JAR_PATH"

# Check the exit status of the java command
if [ $? -eq 0 ]; then
    echo "Tracker application finished successfully (or is running in the background)."
else
    echo "Error: The Tracker application failed to start or encountered an error."
fi

exit 0