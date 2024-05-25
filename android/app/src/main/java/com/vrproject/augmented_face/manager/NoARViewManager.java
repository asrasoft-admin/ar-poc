package com.vrproject.augmented_face.manager;

import androidx.annotation.NonNull;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.vrproject.augmented_face.NoARViewMain;
import com.vrproject.augmented_face.scene.NoARScene;

public class NoARViewManager extends SimpleViewManager<NoARViewMain> {
  public static final String REACT_CLASS = "SceneformNoARView";

  @Override
  @NonNull
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  @NonNull
  public NoARViewMain createViewInstance(ThemedReactContext reactContext) {
    return new NoARViewMain(reactContext);
  }
}
