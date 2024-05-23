import Foundation
import SwiftUI
import ARKit
import RealityKit

@objc(ARModule)
class ARModule: NSObject {
    
  @available(iOS 15.0, *)
  @objc func startARSession() {
        DispatchQueue.main.async {
            let rootVC = UIApplication.shared.windows.first?.rootViewController
            let arVC = UIHostingController(rootView: ContentView())
            arVC.modalPresentationStyle = .fullScreen
            rootVC?.present(arVC, animated: true, completion: nil)
        }
    }
    
    @objc static func requiresMainQueueSetup() -> Bool {
        return true
    }
}
