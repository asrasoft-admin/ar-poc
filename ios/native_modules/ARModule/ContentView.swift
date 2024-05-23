import SwiftUI
import RealityKit
import ARKit
import SceneKit
import AVFoundation
import UIKit

@available(iOS 15.0, *)
struct ContentView: View {
    
    @State private var isPresented: Bool = false
    @State private var capturedImage: UIImage?
    @State private var showCapturedImage: Bool = false
    @State private var useFrontCamera: Bool = true
    
    var body: some View {
        ZStack {
            ARViewContainer(capturedImage: $capturedImage, showCapturedImage: $showCapturedImage, useFrontCamera: $useFrontCamera)
                .edgesIgnoringSafeArea(.all)
                .alert("Face Tracking Unavailable", isPresented: $isPresented) {
                    Button {
                        isPresented = false
                    } label: {
                        Text("Okay")
                    }
                } message: {
                    Text("Face tracking requires an iPhone X or later.")
                }
                .onAppear {
                    if !ARFaceTrackingConfiguration.isSupported {
                        isPresented = true
                    }
                }
            
            VStack {
                HStack {
                    Button(action: {
                        removeglasses()
                    }) {
                        Image(systemName: "eye.slash.circle.fill")
                            .resizable()
                            .frame(width: 50, height: 50)
                            .foregroundColor(.white)
                            .background(Color.black.opacity(0.5))
                            .clipShape(Circle())
                            .padding()
                    }
                    Spacer()
                    Button(action: {
                        addRemoveTgt()
                    }) {
                        Image(systemName: "tray.circle.fill")
                            .resizable()
                            .frame(width: 50, height: 50)
                            .foregroundColor(.white)
                            .background(Color.black.opacity(0.5))
                            .clipShape(Circle())
                            .padding()
                    }
                    Spacer()
                    
                    Button(action: {
                        removebg()
                    }) {
                        Image(systemName: "pin.slash")
                            .resizable()
                            .frame(width: 50, height: 50)
                            .foregroundColor(.white)
                            .background(Color.black.opacity(0.5))
                            .clipShape(Circle())
                            .padding()
                    }
                }
                
                Spacer()
                
                HStack {
                    Button(action: {
                        flipCamera()
                    }) {
                        Image(systemName: "arrow.triangle.2.circlepath.camera")
                            .resizable()
                            .frame(width: 50, height: 50)
                            .foregroundColor(.white)
                            .background(Color.black.opacity(0.5))
                            .clipShape(Circle())
                            .padding()
                    }
                    
                    Spacer()
                    
                    Button(action: {
                        capturePhoto()
                    }) {
                        Image(systemName: "camera.circle")
                            .resizable()
                            .frame(width: 70, height: 70)
                            .foregroundColor(.white)
                            .background(Color.black.opacity(0.5))
                            .clipShape(Circle())
                            .padding()
                    }
                }
            }
            
        }
        .sheet(isPresented: $showCapturedImage) {
            if let capturedImage = capturedImage {
                Image(uiImage: capturedImage)
                    .resizable()
                    .scaledToFit()
            }
        }
    }
    
    private func capturePhoto() {
        NotificationCenter.default.post(name: Notification.Name("capturePhoto"), object: nil)
    }
    private func flipCamera() {
        useFrontCamera.toggle()
        NotificationCenter.default.post(name: Notification.Name("flipCamera"), object: nil)
    }
    private func removeglasses() {
        NotificationCenter.default.post(name: Notification.Name("removeglasses"), object: nil)
    }
    private func removebg() {
        NotificationCenter.default.post(name: Notification.Name("removebg"), object: nil)
    }
    private func addRemoveTgt() {
        NotificationCenter.default.post(name: Notification.Name("addRemoveTgt"), object: nil)
    }
}

struct ARViewContainer: UIViewRepresentable {
    
    @Binding var capturedImage: UIImage?
    @Binding var showCapturedImage: Bool
    @Binding var useFrontCamera: Bool
    @State var toggleBench: Bool = false
    @State var toggleGlasses: Bool = false
    @State var toggleTgt: Bool = true
    
    func makeUIView(context: Context) -> ARView {
        let arView = ARView(frame: .zero)
        setupARView(arView: arView)
        
        NotificationCenter.default.addObserver(forName: Notification.Name("capturePhoto"), object: nil, queue: .main) { _ in
            capturePhoto(from: arView)
        }
        
        NotificationCenter.default.addObserver(forName: Notification.Name("flipCamera"), object: nil, queue: .main) { _ in
            setupARView(arView: arView)
        }
        NotificationCenter.default.addObserver(forName: Notification.Name("removeglasses"), object: nil, queue: .main) { _ in
            removeModels(arView: arView, type: 1)
        }
        
        NotificationCenter.default.addObserver(forName: Notification.Name("removebg"), object: nil, queue: .main) { _ in
            removeModels(arView: arView, type: 0)
        }
        
        NotificationCenter.default.addObserver(forName: Notification.Name("addRemoveTgt"), object: nil, queue: .main) { _ in
            removeModels(arView: arView, type: 2)
        }
        
        return arView
    }
    
    @MainActor private func removeModels(arView: ARView, type: Int) {
        if type == 0 {
            if !toggleBench {
                arView.scene.anchors.forEach { anchor in
                    if let entity = anchor.children.first(where: { $0.name == "backgroundEntity" }) {
                        arView.scene.anchors.remove(anchor)
                    }
                }
                toggleBench = true
            } else {
                addBenchBg(arView: arView)
                toggleBench = false
            }
        } else if type == 1 {
            if !toggleGlasses {
                arView.scene.anchors.forEach { anchor in
                    if let entity = anchor.children.first(where: { $0.name == "GlassesEntity" }) {
                        arView.scene.anchors.remove(anchor)
                    }
                }
                toggleGlasses = true
            } else {
                addGlasses(arView: arView)
                toggleGlasses = false
            }
        } else if type == 2 {
            arView.scene.anchors.forEach { anchor in
                if let entity = anchor.children.first(where: { $0.name == "tgtEntity" }) {
                    arView.scene.anchors.remove(anchor)
                }
            }
            arView.scene.anchors.forEach { anchor in
                if let entity = anchor.children.first(where: { $0.name == "GlassesEntity" }) {
                    arView.scene.anchors.remove(anchor)
                }
            }
            if !toggleTgt {
                if useFrontCamera {
                    let arConfig = ARFaceTrackingConfiguration()
                    arView.session.run(arConfig)
                }
                toggleTgt = true
            } else {
                if useFrontCamera {
                    let arConfig = ARFaceTrackingConfiguration()
                    arConfig.frameSemantics = .personSegmentation
                    arView.session.run(arConfig)
                }
                addTgt(arView: arView)
                toggleTgt = false
            }
        }
    }
    
    func addBenchBg(arView: ARView) {
        if let backgroundScene = try? Entity.load(named: "bench") {
            let backgroundAnchor = AnchorEntity(world: SIMD3(x: 0, y: 0, z: -1.0))
            backgroundScene.name = "backgroundEntity"
            backgroundScene.setScale(SIMD3(x: 1, y: 1, z: 1), relativeTo: backgroundAnchor)
            backgroundAnchor.addChild(backgroundScene)
            arView.scene.anchors.append(backgroundAnchor)
        } else {
            print("Failed to load background model")
        }
    }
    
    func addTgt(arView: ARView) {
        if let backgroundScene = try? Entity.load(named: "tgt") {
            let backgroundAnchor = AnchorEntity(world: SIMD3(x: 0, y: 0, z: -1.0))
            backgroundScene.name = "tgtEntity"
            backgroundScene.setScale(SIMD3(x: 1, y: 1, z: 1), relativeTo: backgroundAnchor)
            backgroundAnchor.addChild(backgroundScene)
            arView.scene.anchors.append(backgroundAnchor)
        } else {
            print("Failed to load background model")
        }
    }
    
    @MainActor func addGlasses(arView: ARView) {
        if let faceScene = try? Glasses.loadFace() {
            let faceAnchor = AnchorEntity(world: SIMD3(x: 0, y: 0, z: 0))
            faceScene.setParent(faceAnchor)
            faceScene.name = "GlassesEntity"
            faceScene.setScale(SIMD3(x: 1.5, y: 1.5, z: 1.5), relativeTo: faceAnchor) // Scale to fit
            arView.scene.anchors.append(faceAnchor)
        } else {
            print("Failed to load face tracking model")
        }
    }
    
    @MainActor private func setupARView(arView: ARView) {
        if useFrontCamera {
            let arConfig = ARFaceTrackingConfiguration()
            addGlasses(arView: arView)
            addBenchBg(arView: arView)
            arView.session.run(arConfig)
        } else {
            let arConfig = ARWorldTrackingConfiguration()
            arConfig.planeDetection = [.horizontal, .vertical]
            arView.session.run(arConfig)
        }
    }
    
    func capturePhoto(from arView: ARView) {
        let imageRenderer = UIGraphicsImageRenderer(size: arView.bounds.size)
        let image = imageRenderer.image { context in
            arView.drawHierarchy(in: arView.bounds, afterScreenUpdates: true)
        }
        capturedImage = image
        showCapturedImage = true
    }
    
    func updateUIView(_ uiView: ARView, context: Context) {}
}
