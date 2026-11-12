import SwiftUI

struct ContentView: View {
    @State private var messages = [LimtChatMessage(role: "assistant", content: "Welcome to Limt OS. Ask me to explain device information or generate an Android OS project.")]
    @State private var draft = ""
    @State private var models = ["gpt-5.5"]
    @State private var selectedModel = "gpt-5.5"
    @State private var sending = false
    @State private var showDeviceInfo = false
    @FocusState private var focused: Bool

    private let accent = Color(red: 0.48, green: 0.35, blue: 0.96)

    var body: some View {
        VStack(spacing: 0) {
            header
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 22) {
                        ForEach(messages) { message in
                            messageView(message).id(message.id)
                        }
                        if sending {
                            HStack(spacing: 10) {
                                ProgressView().tint(.white)
                                Text("Limt OS is thinking…").font(.subheadline).foregroundStyle(.secondary)
                            }
                        }
                    }
                    .padding(.horizontal, 18).padding(.top, 24).padding(.bottom, 20)
                }
                .onChange(of: messages.count) { _ in
                    if let last = messages.last { withAnimation { proxy.scrollTo(last.id, anchor: .bottom) } }
                }
            }
            composer
        }
        .background(Color(red: 0.055, green: 0.055, blue: 0.065).ignoresSafeArea())
        .preferredColorScheme(.dark)
        .task { await refreshModels() }
        .sheet(isPresented: $showDeviceInfo) { deviceSheet }
    }

    private var header: some View {
        HStack(spacing: 12) {
            Image(systemName: "sparkle").font(.system(size: 19, weight: .semibold))
                .foregroundStyle(accent).frame(width: 38, height: 38)
                .background(accent.opacity(0.16), in: RoundedRectangle(cornerRadius: 13))
            VStack(alignment: .leading, spacing: 2) {
                Text("Limt OS").font(.headline.weight(.semibold))
                Menu {
                    ForEach(models, id: \.self) { model in
                        Button(model) { selectedModel = model }
                    }
                    Divider()
                    Button { showDeviceInfo = true } label: { Label("Device information", systemImage: "iphone.gen3") }
                } label: {
                    HStack(spacing: 5) {
                        Text(selectedModel).font(.caption).foregroundStyle(.secondary)
                        Image(systemName: "chevron.down").font(.system(size: 9, weight: .bold)).foregroundStyle(.secondary)
                    }
                }
            }
            Spacer()
            Button {
                messages = [LimtChatMessage(role: "assistant", content: "New conversation. What would you like to build?")]
            } label: {
                Image(systemName: "square.and.pencil").font(.system(size: 18))
                    .foregroundStyle(.white).frame(width: 40, height: 40)
                    .background(Color.white.opacity(0.08), in: Circle())
            }
            .accessibilityLabel("New chat")
        }
        .padding(.horizontal, 18).padding(.vertical, 12)
        .background(.ultraThinMaterial)
        .overlay(alignment: .bottom) { Rectangle().fill(Color.white.opacity(0.08)).frame(height: 1) }
    }

    private var composer: some View {
        HStack(alignment: .bottom, spacing: 10) {
            TextField("Message Limt OS…", text: $draft, axis: .vertical)
                .lineLimit(1...5).focused($focused).submitLabel(.send)
                .padding(.vertical, 12).padding(.leading, 14)
                .onSubmit { Task { await send() } }.disabled(sending)
            Button { Task { await send() } } label: {
                Image(systemName: "arrow.up").font(.system(size: 16, weight: .bold)).foregroundStyle(.white)
                    .frame(width: 38, height: 38)
                    .background(draft.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || sending ? Color.gray.opacity(0.35) : accent, in: Circle())
            }
            .disabled(draft.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || sending)
            .padding(6)
        }
        .background(Color(white: 0.12), in: RoundedRectangle(cornerRadius: 25))
        .overlay(RoundedRectangle(cornerRadius: 25).stroke(Color.white.opacity(0.13), lineWidth: 1))
        .padding(.horizontal, 14).padding(.top, 10).padding(.bottom, 8)
        .background(Color(red: 0.055, green: 0.055, blue: 0.065))
    }

    @ViewBuilder private func messageView(_ message: LimtChatMessage) -> some View {
        let user = message.role == "user"
        HStack {
            if user { Spacer(minLength: 44) }
            VStack(alignment: .leading, spacing: 10) {
                if !user {
                    Label("LIMT OS", systemImage: "sparkle").font(.caption.weight(.bold)).foregroundStyle(accent)
                }
                Text(message.content).font(.body).textSelection(.enabled)
                    .foregroundStyle(user ? .white : Color.white.opacity(0.92))
                ForEach(message.files) { file in
                    ShareLink(item: file.content, subject: Text(file.name)) {
                        Label("Export \(file.name)", systemImage: "square.and.arrow.up")
                            .font(.caption.weight(.semibold)).padding(.horizontal, 12).padding(.vertical, 9)
                            .background(accent.opacity(0.2), in: Capsule())
                    }
                }
            }
            .padding(user ? 13 : 2)
            .background {
                if user { RoundedRectangle(cornerRadius: 20).fill(Color(red: 0.30, green: 0.22, blue: 0.75)) }
            }
            if !user { Spacer(minLength: 18) }
        }
    }

    private var deviceSheet: some View {
        NavigationStack {
            List(LimtDeviceInfo.snapshot.keys.sorted(), id: \.self) { key in
                VStack(alignment: .leading, spacing: 4) {
                    Text(key.replacingOccurrences(of: "_", with: " ").capitalized).font(.caption).foregroundStyle(.secondary)
                    Text(LimtDeviceInfo.snapshot[key] ?? "")
                }.padding(.vertical, 3)
            }
            .navigationTitle("Device information")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .topBarTrailing) { Button("Done") { showDeviceInfo = false } } }
        }
        .preferredColorScheme(.dark)
    }

    @MainActor private func refreshModels() async {
        do {
            let result = try await LimtAPI.shared.loadModels()
            if !result.models.isEmpty {
                models = result.models
                selectedModel = result.defaultModel.flatMap { result.models.contains($0) ? $0 : nil } ?? result.models[0]
            }
        } catch {
            // Leave the fallback model visible; the next chat request reports the actionable error.
        }
    }

    @MainActor private func send() async {
        let prompt = draft.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !prompt.isEmpty, !sending else { return }
        draft = ""
        focused = false
        let history = messages
        messages.append(LimtChatMessage(role: "user", content: prompt))
        sending = true
        defer { sending = false }
        do {
            let result = try await LimtAPI.shared.send(message: prompt, model: selectedModel, history: history)
            messages.append(LimtChatMessage(role: "assistant", content: result.reply, files: result.files ?? []))
        } catch {
            messages.append(LimtChatMessage(role: "assistant", content: "I couldn't connect to the Limt OS backend. \(error.localizedDescription)"))
        }
    }
}
