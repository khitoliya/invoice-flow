# Invoice Flow

An offline-first Android application designed for streamlined GST-compliant billing, purchase ledger management, and automatic document processing.

Invoice Flow bridges the gap between local record-keeping and cloud databases, incorporating an on-device OCR pipeline to convert vendor PDFs into structured local database records and a session-isolated portal automation interface to assist with filing.

---

## 🏗️ Architecture

The project is built on **Clean Architecture** principles, separating concerns into discrete layers:

```text
       ┌────────────────────────────────────────────────────────┐
       │                       Presentation                     │
       │           (Jetpack Compose, Material 3, MVVM)          │
       └───────────────────────────┬────────────────────────────┘
                                   │
                                   ▼
       ┌────────────────────────────────────────────────────────┐
       │                          Domain                        │
       │      (Use Cases, Entities, Validation, OCR Parsing)    │
       └───────────────────────────┬────────────────────────────┘
                                   │
                                   ▼
       ┌────────────────────────────────────────────────────────┐
       │                          Data                          │
       │       (Room Database, Cloud Firestore, Firebase Auth)  │
       └────────────────────────────────────────────────────────┘
```

*   **Presentation Layer**: Built entirely using **Jetpack Compose** and **Material 3**, conforming to the MVVM pattern. ViewModels handle state management using Kotlin StateFlows, ensuring lifecycle-aware UI updates.
*   **Domain Layer**: Pure business logic containing entity models (Invoices, Firms, Parties, Items), validators, and use cases. It operates independently of databases and UI frameworks.
*   **Data Layer**: Manages data persistence. It coordinates local caching (Room SQLite) and remote storage (Cloud Firestore) through repositories, implementing an offline-first strategy.

---

## 📄 Intelligent Document Processing (OCR Pipeline)

To eliminate manual data entry, Invoice Flow features an on-device Intelligent Document Processing (IDP) engine that parses and structure-extracts details from vendor PDF invoices.

### Extraction Pipeline Flow

```text
       ┌──────────────────────┐
       │   Vendor PDF File    │
       └──────────┬───────────┘
                  │
                  ▼
       ┌──────────────────────┐
       │     PdfRenderer      │  ◄── Renders PDF pages to high-resolution Bitmaps
       └──────────┬───────────┘
                  │
                  ▼
       ┌──────────────────────┐
       │  Google ML Kit OCR   │  ◄── Processes images to detect text lines and blocks
       └──────────┬───────────┘
                  │
                  ▼
       ┌──────────────────────┐
       │    Text Normalizer   │  ◄── Standardizes spacing, character encoding, and case
       └──────────┬───────────┘
                  │
                  ▼
       ┌──────────────────────┐
       │  Entity Extraction   │  ◄── Extracts raw tokens (GSTINs, Dates, Totals) using Regex
       └──────────┬───────────┘
                  │
                  ▼
       ┌──────────────────────┐
       │  Contextual Mapping  │  ◄── Resolves buyer/seller entities and locates names
       └──────────┬───────────┘          using spatial heuristics relative to GSTINs
                  │
                  ▼
       ┌──────────────────────┐
       │  Business Validation │  ◄── Performs arithmetic checks, HSN parsing, and binds
       └──────────┬───────────┘          data to local SQLite entities
                  │
                  ▼
       ┌──────────────────────┐
       │  Structured Invoice  │
       └──────────────────────┘
```

### Technical Implementation Details

1.  **On-Device Text Recognition**: Utilizing Android's native `PdfRenderer`, PDF pages are converted into `Bitmap` instances at double scale (to ensure OCR accuracy for fine-print tables). These bitmaps are then parsed asynchronously using **Google's ML Kit Vision Text Recognition API**.
2.  **Entity Resolution Heuristics**:
    *   **GSTIN Matching**: The parser extracts all GST Identification Numbers (GSTINs) using strict regex matching. It determines the seller (Firm) by matching extracted GSTINs against registered database configurations. The buyer (Party) is identified as the remaining unique GSTIN.
    *   **Spatial Heuristics for Addresses**: Since address structures vary, the engine uses a sliding index window centered on the buyer's GSTIN. It scans lines immediately preceding the GSTIN (ignoring keywords like *Buyer* or *Consignee*) to accurately isolate the business name and billing address.
3.  **Line Item Parsing**: To extract tables of items, the engine identifies line-end currency patterns (denoting taxable values) and works backward to isolate the corresponding HSN (Harmonized System of Nomenclature) code, item description, and units of measurement.
4.  **Database Binding**: Extracted parties and inventory items are cross-referenced with the local SQLite database. If a party or item is new, the application alerts the user, allowing them to verify and register the entity in a single workflow.

---

## 🔒 Enterprise-Grade Features

### 1. Webview Session Isolation & Portal Automation
The application includes a custom-engineered `PortalWebView` to streamline interaction with government tax portals (GST & E-Way Bill portals):
*   **Cookie Separation**: To prevent session data leaks when managing accounts for multiple firms, the app isolates cookie spaces. Upon switching firm contexts, it wipes the browser cookie manager and restores only the target firm's cookies.
*   **Secure Auto-Fill**: Integrates credential injection scripts that identify login forms and safely autofill credentials stored in hardware-backed `EncryptedSharedPreferences`.
*   **DOM Injection & CSS Overrides**: Government portals frequently block text selection and right-clicks. The app injects styling rules and JavaScript event listener overrides to re-enable standard copy-paste functionality, saving users time during form verification.
*   **Native Printing Redirection**: Overrides the window-level browser `print()` API using a Javascript interface, redirecting portal page captures straight to the native Android `PrintManager`.

### 2. Offline-First Synchronization
*   **Room + Firestore**: Read and write operations occur instantly against the local SQLite database via Room. A synchronization manager coordinates background data transfers with Cloud Firestore.
*   **Kotlin Flow-based Listeners**: Subscribes to database changes in real-time, pushing sync states to ViewModels seamlessly.
*   **Android WorkManager**: Executes background synchronizations, handles retry constraints (requiring unmetered networks or battery charging), and manages backup tasks.

### 3. Verification & Compliance Engines
*   **Local Validations**: Runs schema and validation checks for GST formats, Interstate vs. Intrastate splits, tax allocations (CGST, SGST, IGST), and document field length limits before network submission.
*   **Pincode Distance Caching**: Includes a shared database table on Firestore mapping pincode pairs to road distances. The client checks this cache first to pre-fill E-Way Bill drafts, eliminating redundant API distance queries.

---

## 🛠️ Technical Stack

*   **Language**: Kotlin (utilizing Coroutines, Flows, and Serialization)
*   **UI Framework**: Jetpack Compose (Material 3 components)
*   **Dependency Injection**: Dagger Hilt
*   **Database & Storage**: Room SQLite database, Cloud Firestore, Firebase Storage
*   **Auth**: Firebase Authentication, Google Play Services Auth
*   **Background Jobs**: Android WorkManager
*   **Document Engines**: Google ML Kit (Text Recognition), iText7 (PDF rendering/merging), Apache POI (Excel integrations)
*   **Security**: Android Keystore, EncryptedSharedPreferences, AndroidX Biometrics (Fingerprint & Face verification)

---

## 🚀 Getting Started

### Prerequisites

*   Android Studio Ladybug (or newer)
*   Android SDK 26 (Android 8.0) or higher
*   A Firebase Project configuration

### Installation

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/yourusername/invoice-flow.git
    cd invoice-flow
    ```

2.  **Add Firebase Configuration**:
    Add your `google-services.json` file in the `app/` directory.

3.  **Build the Project**:
    Open the directory in Android Studio, let Gradle sync complete, and click **Run**.

---

## 📄 License

This repository is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.
