@echo off
echo ========================================
echo VitalTech Chatbot - Clean Build Script
echo ========================================
echo.
echo This script will clean and rebuild your app
echo to ensure the chatbot fix is applied.
echo.
pause

cd /d "C:\Users\arnav\AndroidStudioProjects\AppointementApp"

echo.
echo [1/4] Cleaning project...
call gradlew.bat clean

echo.
echo [2/4] Uninstalling old app from device...
adb uninstall com.example.appointementapp

echo.
echo [3/4] Building app...
call gradlew.bat assembleDebug

echo.
echo [4/4] Installing app on device...
call gradlew.bat installDebug

echo.
echo ========================================
echo BUILD COMPLETE!
echo ========================================
echo.
echo The app has been rebuilt with the chatbot fix.
echo Model: gemini-2.0-flash-exp (Latest & Fastest!)
echo API version: v1beta
echo.
echo Now test the chatbot:
echo 1. Open the app on your device
echo 2. Login as patient
echo 3. Book an appointment
echo 4. Open chatbot
echo 5. Type: "I am having headaches"
echo.
echo You should now get a proper response!
echo.
pause

