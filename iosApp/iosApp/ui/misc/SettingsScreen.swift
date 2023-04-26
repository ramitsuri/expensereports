import SwiftUI
import class shared.DownloadViewState

struct SettingsScreen: View {
    @StateObject
    private var model = ObservableSettingsModel()

    var body: some View {
        Form {
            if let state = model.state {
                ServerUrlItem(serverUrl: state.serverUrl.url) { serverUrl in
                    model.setServerUrl(url: serverUrl)
                }
                DownloadReportsItem(downloadViewState: state.downloadViewState) {
                    model.downloadReports()
                }
                ShouldDownloadRecentItem(shouldDownloadRecent: state.shouldDownloadRecentData) {
                    model.setShouldDownloadRecentData()
                }
            }
        }
            .onAppear(perform: {
            model.activate()
        })
            .onDisappear(perform: {
            model.deactivate()
        })
    }
}

struct ServerUrlItem: View {
    let serverUrl: String
    let onServerUrlSet: (String) -> Void

    @State
    private var serverSet: Bool = false

    @State
    private var dialogState = false

    init(serverUrl: String, onServerUrlSet: @escaping (String) -> Void) {
        self.serverUrl = serverUrl
        self.onServerUrlSet = onServerUrlSet
    }

    var body: some View {
        SettingsItem(title: "settings_server_url_title".local(), subtitle: subtitle) {
            dialogState = true
        }.sheet(isPresented: $dialogState, onDismiss: { }) {
            SetApiUrlDialog(serverUrl, onServerUrlSet: onServerUrlSet)
        }
    }

    private var subtitle: String {
        if (serverSet) {
            return "settings_server_url_restart".local()
        } else if (serverUrl.isEmpty) {
            return "settings_server_url_server_not_set".local()
        } else {
            return serverUrl
        }
    }
}

struct SetApiUrlDialog: View {
    @Environment(\.presentationMode) var presentation

    @State
    private var url: String

    private let onServerUrlSet: (String) -> Void

    init(_ previousUrl: String, onServerUrlSet: @escaping (String) -> Void) {
        _url = State(initialValue: previousUrl.isEmpty ? "settings_server_url_server_not_set".local() : previousUrl)
        self.onServerUrlSet = onServerUrlSet
    }

    var body: some View {
        VStack {
            TextField(
                "",
                text: $url
            )
                .textFieldStyle(.roundedBorder)
                .padding(10)
            HStack(spacing: 10) {
                Button("cancel".local()) {
                    self.presentation.wrappedValue.dismiss()
                }
                Button("ok".local()) {
                    self.presentation.wrappedValue.dismiss()
                    onServerUrlSet(url)
                }
            }
        }
            .padding(10)
    }
}

struct DownloadReportsItem: View {
    let downloadViewState: DownloadViewState?
    let onTap: () -> Void

    init(downloadViewState: DownloadViewState?, onTap: @escaping () -> Void) {
        self.downloadViewState = downloadViewState
        self.onTap = onTap
    }

    var body: some View {
        SettingsItem(title: "settings_download_title".local(), subtitle: subtitle) {
            onTap()
        }
    }

    private var subtitle: String {
        let lastDownloadTime = downloadViewState?.lastDownloadTime
        if (lastDownloadTime == nil) {
            return "settings_download_never_downloaded".local()
        } else {
            return String(format: "settings_download_last_download_time_format".local(), lastDownloadTime?.timeDateMonthYear() ?? "")
        }
    }
}

struct ShouldDownloadRecentItem: View {
    let shouldDownloadRecent: Bool
    let onTap: () -> Void

    init(shouldDownloadRecent: Bool, onTap: @escaping () -> Void) {
        self.shouldDownloadRecent = shouldDownloadRecent
        self.onTap = onTap
    }

    var body: some View {
        SettingsItemWithToggle(title: "settings_should_download_recent_title".local(), subtitle: "settings_should_download_recent_subtitle".local(), isOn: shouldDownloadRecent) {
            onTap()
        }
    }
}

struct SettingsItem: View {
    let title: String
    let subtitle: String
    let onTap: () -> Void

    init(title: String, subtitle: String, onTap: @escaping () -> Void) {
        self.title = title
        self.subtitle = subtitle
        self.onTap = onTap
    }

    var body: some View {
        Section {
            VStack (alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.body)
                    .frame(maxWidth: .infinity, alignment: Alignment.leading)
                Text(subtitle)
                    .font(.body)
                    .foregroundColor(Color.gray)
            }
                .background(Color.blue.opacity(0.0001))
                .onTapGesture { onTap() }
        }
    }
}

struct SettingsItemWithToggle: View {
    let title: String
    let subtitle: String
    let onTap: () -> Void
    @State
    var isToggleOn: Bool

    init(title: String, subtitle: String, isOn: Bool, onTap: @escaping () -> Void) {
        self.title = title
        self.subtitle = subtitle
        self.isToggleOn = isOn
        self.onTap = onTap
    }

    var body: some View {
        Section {
            VStack (alignment: .leading, spacing: 4) {
                Toggle(isOn: $isToggleOn) {
                    Text(title)
                        .font(.body)
                        .frame(maxWidth: .infinity, alignment: Alignment.leading)
                }
                    .onChange(of: self.isToggleOn, perform: { _ in
                    onTap()
                })
                Text(subtitle)
                    .font(.body)
                    .foregroundColor(Color.gray)
            }

        }
    }
}
