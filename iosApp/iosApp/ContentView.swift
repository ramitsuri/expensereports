import SwiftUI

struct MainScreen: View {
    
	var body: some View {
        TabView {
//            ForEach(BottomNav.allCases, id: \.self) { nav in
//                nav.self.route.tabItem {
//                        Label(nav.self.labelKey.local(), systemImage: nav.imageKey)
//                    }
//            }
            HomeScreen()
                .tabItem {
                    BottomNav.home.label
                }
            ReportsScreen()
                .tabItem {
                    BottomNav.reports.label
                }
            TransactionsScreen()
                .tabItem {
                    BottomNav.transactions.label
                }
            SettingsScreen()
                .tabItem {
                    BottomNav.misc.label
                }
        }
	}
}

enum BottomNav:CaseIterable {
    case home
    case reports
    case transactions
    case misc
    
    var label: Label<Text, Image> {
        switch self {
        case .home:
            return Label(self.labelKey.local(), systemImage: self.imageKey)
        case .reports:
            return Label(self.labelKey.local(), systemImage: self.imageKey)
        case .transactions:
            return Label(self.labelKey.local(), systemImage: self.imageKey)
        case .misc:
            return Label(self.labelKey.local(), systemImage: self.imageKey)
        }
    }
    
    private var labelKey: String {
        switch self {
        case .home:
            return "bottom_nav_home"
        case .reports:
            return "bottom_nav_reports"
        case .transactions:
            return "bottom_nav_transactions"
        case .misc:
            return "bottom_nav_misc"
        }
    }
    
    private var imageKey: String {
            switch self {
            case .home:
                return "house.fill"
            case .reports:
                return "doc.on.clipboard.fill"
            case .transactions:
                return "list.bullet.rectangle.portrait.fill"
            case .misc:
                return "person.2.badge.gearshape.fill"
            }
    }
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		MainScreen()
	}
}

extension String {
    func local() -> String {
        NSLocalizedString(self, comment: "")
    }
}
