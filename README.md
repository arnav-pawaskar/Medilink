<div align="center">

<img src="https://img.shields.io/badge/platform-Android-3ddc84?style=flat-square&logo=android&logoColor=white"/>
<img src="https://img.shields.io/badge/language-Java-f89820?style=flat-square&logo=coffeescript&logoColor=white"/>
<img src="https://img.shields.io/badge/backend-Firebase-ffca28?style=flat-square&logo=firebase&logoColor=black"/>
<img src="https://img.shields.io/badge/AI-Gemini-4285f4?style=flat-square&logo=google&logoColor=white"/>
<img src="https://img.shields.io/badge/build-Gradle-02303a?style=flat-square&logo=gradle&logoColor=white"/>
<img src="https://img.shields.io/badge/license-MIT-blue?style=flat-square"/>

<br/><br/>

# MediLink

**A smart healthcare companion for Android - helping patients book care faster and helping admins manage appointments efficiently.**

[Features](#-features) · [Getting Started](#-getting-started) · [Tech Stack](#-tech-stack) · [Project Structure](#-project-structure)

</div>

---

## Overview

MediLink is a native Android application built in Java with Firebase and Gemini AI integration. It supports secure user authentication, appointment booking, AI-powered specialist recommendation, an in-app healthcare chatbot, and an admin panel for appointment operations.

---

## Features

- **Secure Authentication (Firebase Auth)**
  - User registration with password policy validation
  - Login/logout flows
  - Role-based routing for admin (`admin@gmail.com`) and patient users

- **Patient Appointment Booking**
  - Capture patient profile and health details (name, email, phone, blood group, symptoms, history)
  - Pick appointment date and time using native Android pickers
  - Input validation before submission

- **AI Specialist Recommendation (Gemini API)**
  - Suggests a suitable specialist based on patient symptom/history inputs
  - Displays recommendation directly in booking flow

- **Appointment Confirmation**
  - Loads saved appointment details from Firebase Realtime Database
  - Displays key appointment data after successful booking

- **AI Healthcare Chatbot**
  - Context-aware chat using patient and appointment data
  - Gemini-powered conversational guidance in-app

- **Admin Dashboard**
  - View all appointments in realtime
  - Search by patient name/email/phone
  - Confirm, cancel, and reschedule appointments
  - Delete cancelled appointments (with confirmation)

---

## Getting Started

### Prerequisites

- Android Studio (Hedgehog or newer recommended)
- JDK 11+
- Android SDK installed
- Gemini API key (for AI features)
- Firebase project configured (`google-services.json` already present in this repo)

### Run in Android Studio

1. Open the project folder in Android Studio.
2. Sync Gradle.
3. Run on emulator/device.

### Build from CLI

```powershell
cd C:\Users\arnav\AndroidStudioProjects\AppointementApp
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| UI | Android XML layouts (native views) |
| Backend | Firebase Authentication, Firebase Realtime Database |
| AI | Google Gemini API (gemini-1.5-flash) |
| Network | OkHttp |
| Build System | Gradle (Kotlin DSL) |
| Android SDK | minSdk 27, targetSdk 36 |

---

## Project Structure

```text
AppointementApp/
|- app/
|  |- src/main/
|  |  |- java/com/example/appointementapp/
|  |  |  |- MainActivity.java
|  |  |  |- Login.java
|  |  |  |- Register.java
|  |  |  |- BookAppointment.java
|  |  |  |- AppointmentConfirm.java
|  |  |  |- Chatbot.java
|  |  |  |- Admin.java
|  |  |  |- GeminiApiHelper.java
|  |  |- res/layout/    # Activity and component UI XML files
|- build.gradle.kts
|- settings.gradle.kts
```

---

<div align="center">

Made with care for better healthcare access.

</div>

