import Foundation

/// Swift counterpart of Android module LIMT-008 (BootloaderStatus).
/// The Android source currently defines this module as foundation metadata only.
public enum LimtBootloaderStatusModule {
    public static let moduleID = "LIMT-008"
    public static let moduleName = "BootloaderStatus"
    public static let description = "Bootloader Status"
    public static let implementationStatus = "foundation"
}
