import Combine
import SwiftUI
import shared

class ObservableSettingsModel: ObservableObject {
    private var viewModel: SettingsCallbackViewModel?

    private var cancellables = [AnyCancellable]()

    @Published
    private(set) var state: SettingsViewState?

    func activate() {
        let viewModel = Dependencies.shared.getSettingsViewModel()

        doPublish(viewModel.state) { [weak self] state in
            self?.state = state
        }.store(in: &cancellables)

        self.viewModel = viewModel
    }

    func deactivate() {
        cancellables.forEach { $0.cancel() }
        cancellables.removeAll()

        viewModel?.clear()
        viewModel = nil
    }

    func setServerUrl(url: String) {
        viewModel?.setServerUrl(url: url)
    }

    func downloadReports() {
        viewModel?.downloadReports()
    }

    func setShouldDownloadRecentData() {
        viewModel?.setShouldDownloadRecentData()
    }
}
