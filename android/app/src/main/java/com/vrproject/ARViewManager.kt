// ARViewManager.kt
package com.vrproject

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.google.ar.core.AugmentedFace
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Sceneform
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.ArFrontFacingFragment
import com.google.ar.sceneform.ux.AugmentedFaceNode
import java.util.HashMap
import java.util.HashSet
import java.util.concurrent.CompletableFuture

class ARViewManager : SimpleViewManager<ArSceneView>() {
    companion object {
        const val REACT_CLASS = "ARView"
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

    override fun createViewInstance(reactContext: ThemedReactContext): ArSceneView {
        arFragment = ArFrontFacingFragment()
        val activity = reactContext.currentActivity as AppCompatActivity?
        if (activity != null) {
            activity.supportFragmentManager.beginTransaction()
                .add(android.R.id.content, arFragment)
                .commitNow()
            arSceneView = arFragment.arSceneView
            initializeArSceneView()
        }
        return arSceneView
    }

    private fun initializeArSceneView() {
        arSceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST
        arFragment.setOnAugmentedFaceUpdateListener(this::onAugmentedFaceTrackingUpdate)
        loadModels()
        loadTextures()
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
            AugmentedFace.TrackingState.TRACKING -> {
                if (existingFaceNode == null) {
                    val faceNode = AugmentedFaceNode(augmentedFace)
                    val modelInstance = faceNode.setFaceRegionsRenderable(faceModel)
                    modelInstance.setShadowCaster(false)
                    modelInstance.setShadowReceiver(true)
                    faceNode.setFaceMeshTexture(faceTexture)
                    arSceneView.scene.addChild(faceNode)
                    facesNodes[augmentedFace] = faceNode
                }
            }
            AugmentedFace.TrackingState.STOPPED -> {
                if (existingFaceNode != null) {
                    arSceneView.scene.removeChild(existingFaceNode)
                    facesNodes.remove(augmentedFace)
                }
            }
            else -> {}
        }
    }
}
