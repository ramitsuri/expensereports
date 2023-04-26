import Combine
import SwiftUI
import shared

class ObservableTransactionsModel: ObservableObject {
    private var viewModel: TransactionsCallbackViewModel?
    
    private var cancellables = [AnyCancellable]()
    
    @Published
    private (set) var state: TransactionsViewState?
    
    func activate() {
        let viewModel = Dependencies.shared.getTransactionsViewModel()

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
    
    func onFilterUpdated(
            startDate: LocalDateComponents?,
            endDate: LocalDateComponents?,
            minAmount: String?,
            maxAmount: String?
    ) {
    }
    
}
