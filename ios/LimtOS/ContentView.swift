import SwiftUI

struct ContentView: View {
    @State private var messages = [LimtChatMessage(role: "assistant", content: "Hi! I'm Limt OS. What are we building today?")]
    @State private var draft = ""
    @State private var models = ["gpt-5.5"]
    @State private var selectedModel = "gpt-5.5"
    @State private var sending = false
    @State private var showDeviceInfo = false
    @State private var showSidebar = false
    @FocusState private var focused: Bool

    private let accent = Color(red: 0.56, green: 0.38, blue: 0.98)
    private let background = Color(red: 0.055, green: 0.055, blue: 0.065)
    private let panel = Color(red: 0.105, green: 0.105, blue: 0.12)

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
                                ProgressView().tint(accent)
                                Text("Limt OS is thinking…").font(.subheadline).foregroundStyle(.secondary)
                            }
                        }
                    }
                    .padding(.horizontal, 18).padding(.top, 24).padding(.bottom, 20)
                    .frame(maxWidth: 760)
                    .frame(maxWidth: .infinity)
                }
                .onChange(of: messages.count) { _ in
                    if let last = messages.last { withAnimation { proxy.scrollTo(last.id, anchor: .bottom) } }
                }
            }
            composer
        }
        .background(background.ignoresSafeArea())
        .preferredColorScheme(.dark)
        .task { await refreshModels() }
        .sheet(isPresented: $showDeviceInfo) { deviceSheet }
        .sheet(isPresented: $showSidebar) { sidebarSheet }
    }

    private var header: some View {
        HStack(spacing: 12) {
            Button { showSidebar = true } label: {
                Image(systemName: "line.3.horizontal").font(.system(size: 19, weight: .medium))
                    .foregroundStyle(.white).frame(width: 40, height: 40)
                    .background(Color.white.opacity(0.07), in: Circle())
            }
            .accessibilityLabel("Open sidebar")
            VStack(alignment: .leading, spacing: 2) {
                Text("Limt OS").font(.headline.weight(.semibold))
                Text("AI project builder").font(.caption).foregroundStyle(.secondary)
            }
            Spacer()
            Menu {
                ForEach(models, id: \.self) { model in
                    Button(model) { selectedModel = model }
                }
                Divider()
                Button { showDeviceInfo = true } label: { Label("Device information", systemImage: "iphone") }
            } label: {
                HStack(spacing: 5) {
                    Text(selectedModel).font(.caption.weight(.medium)).foregroundStyle(accent)
                    Image(systemName: "chevron.down").font(.system(size: 9, weight: .bold)).foregroundStyle(.secondary)
                }
                .padding(.horizontal, 12).padding(.vertical, 9)
                .background(panel, in: Capsule())
            }
            Button {
                startNewChat()
            } label: {
                Image(systemName: "square.and.pencil").font(.system(size: 18))
                    .foregroundStyle(.white).frame(width: 40, height: 40)
                    .background(Color.white.opacity(0.07), in: Circle())
            }
            .accessibilityLabel("New chat")
        }
        .padding(.horizontal, 14).padding(.vertical, 10)
        .background(background)
        .overlay(alignment: .bottom) { Rectangle().fill(Color.white.opacity(0.08)).frame(height: 1) }
    }

    private var composer: some View {
        VStack(spacing: 7) {
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
            .background(panel, in: RoundedRectangle(cornerRadius: 25))
            .overlay(RoundedRectangle(cornerRadius: 25).stroke(Color.white.opacity(0.13), lineWidth: 1))
            Text("Limt OS can make mistakes. Check generated code before using it.")
                .font(.system(size: 10)).foregroundStyle(.tertiary)
        }
        .padding(.horizontal, 14).padding(.top, 10).padding(.bottom, 8)
        .background(background)
    }

    @ViewBuilder private func messageView(_ message: LimtChatMessage) -> some View {
        let user = message.role == "user"
        HStack(alignment: .top, spacing: 10) {
            if !user {
                Image(systemName: "sparkle").font(.system(size: 15, weight: .semibold))
                    .foregroundStyle(accent).frame(width: 28, height: 28)
                    .background(accent.opacity(0.15), in: RoundedRectangle(cornerRadius: 9))
            } else {
                Spacer(minLength: 34)
            }
            VStack(alignment: .leading, spacing: 10) {
                if !user {
                    Text("LIMT OS").font(.caption.weight(.bold)).foregroundStyle(accent)
                }
                Text(message.content).font(.body).textSelection(.enabled)
                    .foregroundStyle(user ? .white : Color.white.opacity(0.94))
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
                if user { RoundedRectangle(cornerRadius: 20).fill(Color(red: 0.25, green: 0.22, blue: 0.34)) }
            }
            if !user { Spacer(minLength: 18) }
        }
    }

    private var sidebarSheet: some View {
        NavigationStack {
            List {
                Button {
                    startNewChat()
                    showSidebar = false
                } label: {
                    Label("New chat", systemImage: "square.and.pencil").foregroundStyle(.primary)
                }
                Section("Recent") {
                    Button {
                        showSidebar = false
                    } label: {
                        Label(messages.last(where: { $0.role == "user" })?.content.prefix(32).description ?? "Welcome to Limt OS",
                              systemImage: "bubble.left").foregroundStyle(.primary)
                    }
                }
                Section {
                    Label("Limt OS", systemImage: "sparkle")
                    Text("AI project builder").font(.caption).foregroundStyle(.secondary)
                }
            }
            .scrollContentBackground(.hidden)
            .background(background)
            .navigationTitle("Limt OS")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .topBarTrailing) { Button("Done") { showSidebar = false } } }
        }
        .presentationDetents([.medium, .large])
        .preferredColorScheme(.dark)
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

    private func startNewChat() {
        messages = [LimtChatMessage(role: "assistant", content: "New conversation. What would you like to build?")]
        draft = ""
    }

    @MainActor private func refreshModels() async {
        do {
            let result = try await LimtAPI.shared.loadModels()
            if !result.models.isEmpty {
                models = result.models
                selectedModel = result.defaultModel.flatMap { result.models.contains($0) ? $0 : nil } ?? result.models[0]
            }
        } catch {
            // Keep the fallback model; chat displays the actionable backend configuration error.
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
            messages.append(LimtChatMessage(role: "assistant", content: error.localizedDescription))
        }
    }
}
