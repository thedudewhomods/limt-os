import Foundation

/// Swift counterpart of Android module LIMT-039 (FileNameSanitizer).
/// The Android source currently defines this module as foundation metadata only.
public enum LimtFileNameSanitizerModule {
    public static let moduleID = "LIMT-039"
    public static let moduleName = "FileNameSanitizer"
    public static let description = "File Name Sanitizer"
    public static let implementationStatus = "foundation"
}
