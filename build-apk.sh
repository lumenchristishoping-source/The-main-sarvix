#!/bin/bash

# Sarvix APK Build Script
# This script automates the APK build process

echo "========================================"
echo "  Sarvix APK Build Script"
echo "========================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if we're in the right directory
if [ ! -f "settings.gradle.kts" ]; then
    echo -e "${RED}Error: Please run this script from the SarvixApp root directory${NC}"
    exit 1
fi

# Check for Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed${NC}"
    exit 1
fi

echo "Java version:"
java -version
echo ""

# Check for google-services.json
if [ ! -f "app/google-services.json" ]; then
    echo -e "${YELLOW}Warning: google-services.json not found!${NC}"
    echo "Please download your Firebase configuration file and place it in app/"
    echo "Continuing with placeholder (app will not work without real config)..."
    echo ""
fi

# Navigate to app directory
cd app

# Create keystore if it doesn't exist
KEYSTORE_FILE="sarvix-test.keystore"
if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "Creating test keystore..."
    keytool -genkey \
        -v \
        -keystore $KEYSTORE_FILE \
        -alias sarvix-test \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -storepass sarvixtest \
        -keypass sarvixtest \
        -dname "CN=Sarvix Test, OU=Development, O=Sarvix, L=Test, ST=Test, C=US"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Keystore created successfully!${NC}"
    else
        echo -e "${RED}Failed to create keystore${NC}"
        exit 1
    fi
else
    echo "Keystore already exists, skipping creation..."
fi

cd ..

# Make gradlew executable
chmod +x gradlew

echo ""
echo "Select build type:"
echo "1) Debug (unsigned, for development)"
echo "2) Preview (signed, recommended for testing)"
echo "3) Release (signed, minified)"
echo ""
read -p "Enter choice [1-3]: " choice

case $choice in
    1)
        echo ""
        echo "Building Debug APK..."
        ./gradlew assembleDebug
        BUILD_TYPE="debug"
        APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
        ;;
    2)
        echo ""
        echo "Building Preview APK..."
        ./gradlew assemblePreview
        BUILD_TYPE="preview"
        APK_PATH="app/build/outputs/apk/preview/app-preview.apk"
        ;;
    3)
        echo ""
        echo "Building Release APK..."
        ./gradlew assembleRelease
        BUILD_TYPE="release"
        APK_PATH="app/build/outputs/apk/release/app-release.apk"
        ;;
    *)
        echo -e "${RED}Invalid choice${NC}"
        exit 1
        ;;
esac

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  Build Successful!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "APK Location: $APK_PATH"
    echo ""
    
    # Show APK size
    if [ -f "$APK_PATH" ]; then
        APK_SIZE=$(ls -lh "$APK_PATH" | awk '{ print $5 }')
        echo "APK Size: $APK_SIZE"
        echo ""
    fi
    
    echo "To install on device:"
    echo "  adb install -r $APK_PATH"
    echo ""
    echo "Or transfer the APK to your device and install directly."
    echo ""
    
    # Option to install immediately
    read -p "Install on connected device now? (y/n): " install_now
    if [ "$install_now" = "y" ] || [ "$install_now" = "Y" ]; then
        echo "Installing..."
        adb install -r "$APK_PATH"
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}Installation successful!${NC}"
        else
            echo -e "${RED}Installation failed. Make sure device is connected and USB debugging is enabled.${NC}"
        fi
    fi
else
    echo ""
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}  Build Failed!${NC}"
    echo -e "${RED}========================================${NC}"
    echo ""
    echo "Check the error messages above for details."
    echo ""
    echo "Common fixes:"
    echo "  - Ensure Android SDK is properly configured"
    echo "  - Run: ./gradlew clean"
    echo "  - Check internet connection for dependency downloads"
    exit 1
fi