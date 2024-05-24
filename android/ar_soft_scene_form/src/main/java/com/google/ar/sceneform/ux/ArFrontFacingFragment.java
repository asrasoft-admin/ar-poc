package com.google.ar.sceneform.ux;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.ar.core.CameraConfig;
import com.google.ar.core.CameraConfigFilter;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Texture;
import com.gorisse.thomas.sceneform.ArSceneViewKt;
import com.gorisse.thomas.sceneform.light.LightEstimationConfig;

/**
 * Implements ArFragment and configures the session for using the augmented faces feature.
 */
public class ArFrontFacingFragment extends ArFragment {

    @Override
    protected Config onCreateSessionConfig(Session session) {
        CameraConfigFilter filter = new CameraConfigFilter(session);
        filter.setFacingDirection(CameraConfig.FacingDirection.FRONT);

        session.setCameraConfig(session.getSupportedCameraConfigs(filter).get(0));

        Config config = super.onCreateSessionConfig(session);
        config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);
        config.setAugmentedFaceMode(Config.AugmentedFaceMode.MESH3D);
        config.setLightEstimationMode(Config.LightEstimationMode.DISABLED);

        return config;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getInstructionsController().setEnabled(false);

        // Disable the light estimation mode because it's not compatible with the front face camera
        ArSceneViewKt.setLightEstimationConfig(getArSceneView(), LightEstimationConfig.DISABLED);

        // Hide plane indicating dots
        getArSceneView().getPlaneRenderer().setVisible(false);
        // Disable the rendering of detected planes.
        getArSceneView().getPlaneRenderer().setEnabled(false);
    }

    public void setBackgroundTexture(int resource) {

        if (resource != -1) {
            Texture.builder().setSource(getContext(), resource).build().thenAccept(texture -> {
                MaterialFactory.makeOpaqueWithTexture(getContext(), texture).thenAccept(material -> {
                    ModelRenderable backgroundRenderable = ShapeFactory.makeCube(new Vector3(10f, 10f, 0.1f), Vector3.zero(), material);

                    Node backgroundNode = new Node();
                    backgroundNode.setParent(getArSceneView().getScene());
                    backgroundNode.setRenderable(backgroundRenderable);

                    // Position the background far enough behind the face mesh
                    backgroundNode.setLocalPosition(new Vector3(0.0f, 0.0f, -5));
                });
            });
        }
    }
}
