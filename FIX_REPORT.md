# Fix Report - PureLink Android v2.0.0

## 1. Accessibility Service Description & Privacy
- **Issue**: The Accessibility Service description in Android Settings was generic/incorrect.
- **Fix**: Updated `res/xml/service_config.xml` to use the comprehensive `@string/accessibility_service_description` which explains the privacy stance (no data storage, only clipboard cleaning) and functionality.
- **Privacy**: Verified `canRetrieveWindowContent="false"` to ensure the service cannot see screen content, respecting user privacy.

## 2. History Sync (Clipboard Service)
- **Issue**: Links cleaned by the background service were not appearing in the History UI.
- **Fix**: Injected `HistoryRepository` into `ClipboardService`. Added a coroutine scope (`Dispatchers.IO`) to call `historyRepository.addUrl(cleaned)` immediately after cleaning a link. This ensures background operations are persisted.

## 3. Invisible Share Sheet
- **Issue**: Sharing a link to PureLink opened the full app UI, disrupting the user flow.
- **Fix**: 
    - Created `ShareActivity` with a transparent theme (`Theme.PureLink.Translucent`).
    - Configured it to handle `ACTION_SEND` and `PROCESS_TEXT`.
    - It extracts the text, cleans it, saves it to history, copies it back to the clipboard, shows a "Cleaned!" Toast, and finishes immediately.
    - Updated `AndroidManifest.xml` to route `ACTION_SEND` to `ShareActivity` instead of `MainActivity`.

## 4. Onboarding/Status Alert
- **Issue**: Users might not know they need to enable the Accessibility Service for background monitoring.
- **Fix**: Added a prominent `AlertDialog` in `MainScreen`.
    - Checks `uiState.isServiceEnabled`.
    - If disabled, prompts the user with the benefit description and a direct button to "Enable Now" (opens Settings).
    - Includes a "Later" button to dismiss the alert for the session.
