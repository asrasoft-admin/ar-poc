package com.vrproject

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.ArFrontFacingFragment
import com.google.ar.sceneform.ux.AugmentedFaceNode
import com.facebook.react.bridge.ReadableArray
import java.util.HashMap
import java.util.HashSet
import java.util.concurrent.CompletableFuture

class ARViewManager : SimpleViewManager<FrameLayout>() {

    companion object {
        const val REACT_CLASS = "ARViewManager"
        const val COMMAND_CREATE = 1
    }

    private lateinit var arFragment: ArFrontFacingFragment
    private lateinit var arSceneView: ArSceneView

    private val loaders = HashSet<CompletableFuture<*>>()

    private var faceTexture: Texture? = null
    private var faceModel: ModelRenderable? = null

    private val facesNodes = HashMap<AugmentedFace, AugmentedFaceNode>()

    override fun getName(): String {
        return REACT_CLASS
    }

    override fun getCommandsMap(): Map<String, Int> {
        return mapOf("create" to COMMAND_CREATE)
    }

    override fun createViewInstance(reactContext: ThemedReactContext): FrameLayout {
        Log.d(REACT_CLASS, "Creating view instance")
        val inflater = LayoutInflater.from(reactContext)
        val layout = inflater.inflate(R.layout.arview, null) as FrameLayout
    
        arFragment = ArFrontFacingFragment()
        val activity = reactContext.currentActivity as AppCompatActivity?
        activity?.let {
            Log.d(REACT_CLASS, "Activity is not null")
            val fragmentManager = it.supportFragmentManager
            val fragmentContainerId = android.R.id.content
    
            val existingFragment = fragmentManager.findFragmentById(fragmentContainerId)
            if (existingFragment == null) {
                Log.d(REACT_CLASS, "No existing fragment, creating new one")
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.add(fragmentContainerId, arFragment, REACT_CLASS)
                fragmentTransaction.commitNow()
            } else {
                Log.d(REACT_CLASS, "Using existing fragment")
                arFragment = existingFragment as ArFrontFacingFragment
            }
    
            arSceneView = arFragment.arSceneView
            initializeArSceneView()
        } ?: run {
            Log.e(REACT_CLASS, "Activity is null")
        }
    
        return layout
    }
    

    private fun initializeArSceneView() {
        if (!::arSceneView.isInitialized) {
            Log.e(REACT_CLASS, "arSceneView is not initialized")
            return
        }

        arSceneView.setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST)
        arFragment.setOnAugmentedFaceUpdateListener(this::onAugmentedFaceTrackingUpdate)
        loadModels()
        loadTextures()
    }

    override fun receiveCommand(root: FrameLayout, commandId: Int, args: ReadableArray?) {
        super.receiveCommand(root, commandId, args)
        when (commandId) {
            COMMAND_CREATE -> initializeArView(root)
        }
    }

    private fun initializeArView(root: FrameLayout) {
        initializeArSceneView()
    }

    private fun loadModels() {
        loaders.add(ModelRenderable.builder()
            .setSource(arSceneView.context, Uri.parse("models/fox.glb"))
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { model -> faceModel = model }
            .exceptionally {
                Toast.makeText(arSceneView.context, "Unable to load renderable", Toast.LENGTH_LONG).show()
                null
            })
    }

    private fun loadTextures() {
        loaders.add(Texture.builder()
            .setSource(arSceneView.context, Uri.parse("textures/freckles.png"))
            .setUsage(Texture.Usage.COLOR_MAP)
            .build()
            .thenAccept { texture -> faceTexture = texture }
            .exceptionally {
                Toast.makeText(arSceneView.context, "Unable to load texture", Toast.LENGTH_LONG).show()
                null
            })
    }

    private fun onAugmentedFaceTrackingUpdate(augmentedFace: AugmentedFace) {
        if (faceModel == null || faceTexture == null) return

        val existingFaceNode = facesNodes[augmentedFace]

        when (augmentedFace.trackingState) {
            TrackingState.TRACKING -> {
                if (existingFaceNode == null) {
                    val faceNode = AugmentedFaceNode(augmentedFace)

                    faceNode.setFaceRegionsRenderable(faceModel)
                    faceNode.setFaceMeshTexture(faceTexture)
                    arSceneView.scene.addChild(faceNode)
                    facesNodes[augmentedFace] = faceNode
                }
            }
            TrackingState.STOPPED -> {
                if (existingFaceNode != null) {
                    arSceneView.scene.removeChild(existingFaceNode)
                    facesNodes.remove(augmentedFace)
                }
            }
            else -> {}
        }
    }
}
