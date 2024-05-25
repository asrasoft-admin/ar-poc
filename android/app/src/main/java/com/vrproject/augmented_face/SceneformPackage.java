package com.vrproject.augmented_face;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.vrproject.augmented_face.manager.AugmentedFacesViewManager;
import com.vrproject.augmented_face.manager.NoARViewManager;
import com.vrproject.augmented_face.manager.SceneformViewManager;
import com.vrproject.augmented_face.module.AugmentedFacesViewModule;
import com.vrproject.augmented_face.module.NoARViewModule;
import com.vrproject.augmented_face.module.SceneformViewModule;

import java.util.ArrayList;
import java.util.List;

public class SceneformPackage implements ReactPackage {
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
      List<NativeModule> modules = new ArrayList<>();
      modules.add(new SceneformViewModule(reactContext));
      modules.add(new AugmentedFacesViewModule(reactContext));
      modules.add(new NoARViewModule(reactContext));
      return modules;
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
      List<ViewManager> managers = new ArrayList<>();
      managers.add(new SceneformViewManager());
      managers.add(new AugmentedFacesViewManager());
      managers.add(new NoARViewManager());
      return managers;
    }
}
