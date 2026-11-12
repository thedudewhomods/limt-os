import Foundation

/// Swift counterpart of Android module LIMT-075 (CrashReporter).
/// The Android source currently defines this module as foundation metadata only.
public enum LimtCrashReporterModule {
    public static let moduleID = "LIMT-075"
    public static let moduleName = "CrashReporter"
    public static let description = "Crash Reporter"
    public static let implementationStatus = "foundation"
}
