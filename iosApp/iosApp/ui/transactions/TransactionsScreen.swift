import SwiftUI

import class shared.Transaction
import class shared.TransactionsFilter
import class shared.LocalDateComponents

struct TransactionsScreen: View {
    @StateObject
    private var model = ObservableTransactionsModel()

    var body: some View {
        VStack {
            if let state = model.state {
                TransactionsContent(transactions: state.transactions, filter: state.filter) { startDate, endDate, minAmount, maxAmount in
                    model.onFilterUpdated(startDate: startDate, endDate: endDate, minAmount: minAmount, maxAmount: maxAmount)
                }
            }
        } .onAppear(perform: {
            model.activate()
        })
            .onDisappear(perform: {
            model.deactivate()
        })

    }
}

struct TransactionsContent: View {
    let transactions: [String: [Transaction]]
    let filter: TransactionsFilter
    let onFilterUpdated: (LocalDateComponents?, LocalDateComponents?, String?, String?) -> Void

    var body: some View {
        VStack {
            //FilterRow(filter: filter, onFilterUpdated: onFilterUpdated)
            Transactions(transactionGroups: transactions)
        }
    }
}

struct FilterRow: View {
    let filter: TransactionsFilter
    let onFilterUpdated: (LocalDateComponents?, LocalDateComponents?, String?, String?) -> Void

    var body: some View {
        Text("Filter")
    }
}


struct Transactions: View {
    let transactionGroups: [String: [Transaction]]
    init(transactionGroups: [String: [Transaction]]) {
        self.transactionGroups = transactionGroups
    }
//    init(transactions: [Transaction]) {
//        self.transactions = transactions.map { TransactionIdentifiable(transaction: $0) }
//    }
//    let columns = [
//        GridItem(.flexible(), alignment: .topLeading),
//        GridItem(.flexible(minimum: 150), alignment: .topLeading),
//        GridItem(.flexible(), alignment: .topLeading),
//        GridItem(.flexible(), alignment: .topLeading),
//        GridItem(.flexible(), alignment: .topLeading)
//    ]
//    var body: some View {
//        LazyVGrid(columns: columns) {
//
//            // headers
//            Group {
//                Text("transactions_date_header".local())
//                Text("transactions_description_header".local())
//                Text("transactions_amount_header".local())
//                Text("transactions_from_accounts_header".local())
//                Text("transactions_to_accounts_header".local())
//            }
//                .font(.headline)
//
//            // content
//            ForEach(transactions) { transaction in
//                Text(transaction.date)
//                Text(transaction.description)
//                Text(transaction.amount)
//                Text(transaction.fromAccounts)
//                Text(transaction.toAccounts)
//            }
//        }
//            .padding()
//    }

    var body: some View {
        NavigationView {
            List {
                ForEach(transactionGroups.keys.sorted().reversed(), id: \.self) { date in
                    if let transactions = transactionGroups[date] {
                        Section(header: HStack {
                            TransactionItemHeader(date: date)
                        }) {
                            ForEach(transactions, id: \.self) { transaction in
                                TransactionItem(transaction: transaction)
                            }
                        }
                    }
                }
            }
                .listStyle(GroupedListStyle())
        }
    }
}

struct TransactionItemHeader: View {
    let date: String

    var body: some View {
        Text(date)
            .font(.body)
    }
}

struct TransactionItem: View {
    let transaction: Transaction

    @State
    private var showDetails: Bool = false

    var body: some View {
        VStack(spacing: 8) {
            HStack {
                Text(transaction.description_)
                    .font(.callout)
                    .lineLimit(1)
                Spacer()
                Text(transaction.total.format())
                    .font(.callout)
                    .fontWeight(.bold)
            }
            Button {
                showDetails = !showDetails
            } label: {
                Image(systemName: "chevron.down")
            }
                .frame(maxWidth: .infinity)

            if (showDetails) {
                GeometryReader { geo in
                    HStack {
                        VStack {
                            ForEach(transaction.debitSplits, id: \.self) { split in
                                HStack {
                                    Text(split.account.split(separator: ":").last ?? "")
                                        .font(.caption)
                                    Spacer()
                                    Text(split.amount.abs().format())
                                        .font(.caption)
                                }
                            }
                        }
                            .frame(width: geo.size.width * 0.4)

                        Image(systemName: "arrow.right")
                            .frame(width: geo.size.width * 0.2)

                        VStack {
                            ForEach(transaction.creditSplits, id: \.self) { split in
                                HStack {
                                    Text(split.account.split(separator: ":").last ?? "")
                                        .font(.caption)
                                    Spacer()
                                    Text(split.amount.abs().format())
                                        .font(.caption)
                                }
                            }
                        }
                            .frame(width: geo.size.width * 0.4)
                    }
                }
            }
        }
            .padding(8)
    }
}

//struct TransactionIdentifiable: Identifiable {
//    let id = UUID()
//
//    let date: String
//    let amount: String
//    let description: String
//    let fromAccounts: String
//    let toAccounts: String
//
//    init (transaction: Transaction) {
//        self.date = transaction.date.monthDateYear()
//        self.amount = transaction.amount.format()
//        self.description = transaction.description_
//        self.fromAccounts = transaction.fromAccounts.joined(separator: ", ")
//        self.toAccounts = transaction.toAccounts.joined(separator: ", ")
//    }
//}
