# Limt OS for iOS

Native SwiftUI client using the existing FastAPI backend.

1. On a Mac with Xcode 15+, install XcodeGen with Homebrew.
2. Run xcodegen generate from this directory.
3. Open LimtOS.xcodeproj, select an iPhone simulator, and run.
4. Set LimtAPIBaseURL in the generated app Info settings to the deployed HTTPS backend URL.

The client uses GET /models and POST /chat with the same JSON contract as Android.

iOS cannot inspect Android bootloader state, Android ABI details, or privileged firmware information. The iOS device sheet reports public iOS device information only. This is not an iBoot replacement, firmware flasher, or bootable OS installer.

A distributable IPA requires an Apple signing certificate and provisioning profile. The workflow documents the required GitHub secrets: IOS_CERTIFICATE_P12_BASE64, IOS_CERTIFICATE_PASSWORD, IOS_PROVISIONING_PROFILE_BASE64, IOS_TEAM_ID. Never commit signing credentials.
