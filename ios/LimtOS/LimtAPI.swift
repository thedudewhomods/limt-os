import Foundation
import UIKit

struct LimtChatMessage: Identifiable {
    let id = UUID()
    let role: String
    let content: String
    var files: [GeneratedFile] = []
}
struct GeneratedFile: Identifiable, Decodable {
    var id: String { name }
    let name: String
    let content: String
}
struct ModelsResponse: Decodable {
    let models: [String]
    let defaultModel: String?
    enum CodingKeys: String, CodingKey { case models; case defaultModel = "default" }
}
struct ChatRequest: Encodable {
    let message: String
    let model: String
    let device_info: [String: String]
    let history: [[String: String]]
}
struct ChatResponse: Decodable {
    let reply: String
    let files: [GeneratedFile]?
    let model: String?
}
enum LimtAPIError: LocalizedError {
    case invalidURL
    case server(String)
    var errorDescription: String? {
        switch self {
        case .invalidURL: return "Limt OS is not connected yet. This build has no backend URL. Deploy the FastAPI backend, set the LIMT_API_BASE_URL GitHub Actions secret to its HTTPS address, and rebuild the app."
        case .server(let value): return value
        }
    }
}
final class LimtAPI {
    static let shared = LimtAPI()
    private let baseURL: URL?
    private let session: URLSession
    private init() {
        let raw = Bundle.main.object(forInfoDictionaryKey: "LimtAPIBaseURL") as? String ?? ""
        baseURL = raw.contains("YOUR_BACKEND_HOST") || raw.isEmpty ? nil : URL(string: raw)
        let config = URLSessionConfiguration.ephemeral
        config.timeoutIntervalForRequest = 45
        config.timeoutIntervalForResource = 90
        session = URLSession(configuration: config)
    }
    func loadModels() async throws -> ModelsResponse {
        let data = try await request(path: "/models", method: "GET")
        return try JSONDecoder().decode(ModelsResponse.self, from: data)
    }
    func send(message: String, model: String, history: [LimtChatMessage]) async throws -> ChatResponse {
        let payload = ChatRequest(message: message, model: model, device_info: LimtDeviceInfo.snapshot,
            history: history.suffix(12).map { ["role": $0.role, "content": $0.content] })
        let data = try JSONEncoder().encode(payload)
        let responseData = try await request(path: "/chat", method: "POST", body: data)
        return try JSONDecoder().decode(ChatResponse.self, from: responseData)
    }
    private func request(path: String, method: String, body: Data? = nil) async throws -> Data {
        guard let baseURL, let url = URL(string: path, relativeTo: baseURL) else { throw LimtAPIError.invalidURL }
        var req = URLRequest(url: url)
        req.httpMethod = method
        req.httpBody = body
        req.setValue("application/json", forHTTPHeaderField: "Accept")
        if body != nil { req.setValue("application/json", forHTTPHeaderField: "Content-Type") }
        let (data, response) = try await session.data(for: req)
        guard let http = response as? HTTPURLResponse else { throw LimtAPIError.server("Invalid server response.") }
        guard (200..<300).contains(http.statusCode) else {
            let detail = (try? JSONSerialization.jsonObject(with: data) as? [String: Any])?["detail"] as? String
            throw LimtAPIError.server(detail ?? "Server error HTTP \(http.statusCode).")
        }
        return data
    }
}
enum LimtDeviceInfo {
    static var snapshot: [String: String] {
        ["platform": "iOS", "device_model": UIDevice.current.model,
         "system_name": UIDevice.current.systemName, "system_version": UIDevice.current.systemVersion,
         "device_name": UIDevice.current.name, "processor_count": String(ProcessInfo.processInfo.processorCount),
         "physical_memory_bytes": String(ProcessInfo.processInfo.physicalMemory),
         "bootloader_status": "Not accessible to ordinary iOS apps", "serial_number": "Not collected"]
    }
}
