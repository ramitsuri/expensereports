import Combine
import shared

func createPublisher<T>(_ flowAdapter: FlowAdapter<T>) -> AnyPublisher<T, KotlinError> {
    return Deferred<Publishers.HandleEvents<PassthroughSubject<T, KotlinError>>> {
        let subject = PassthroughSubject<T, KotlinError>()
        let canceller = flowAdapter.subscribe(
            onEach: { item in subject.send(item) },
            onComplete: { subject.send(completion: .finished) },
            onThrow: { error in subject.send(completion: .failure(KotlinError(error))) }
        )
        return subject.handleEvents(receiveCancel: { canceller.cancel() })
    }.eraseToAnyPublisher()
}

func doPublish<T>(_ flowAdapter: FlowAdapter<T>, onEach: @escaping (T) -> Void) -> Cancellable {
    return createPublisher(flowAdapter)
        .assertNoFailure()
        .compactMap { $0 }
        .receive(on: DispatchQueue.main)
        .sink { onEach($0) }
}

class KotlinError: LocalizedError {
    let throwable: KotlinThrowable

    init(_ throwable: KotlinThrowable) {
        self.throwable = throwable
    }
    var errorDescription: String? {
        throwable.message
    }
}
