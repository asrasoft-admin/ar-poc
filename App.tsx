import React, {useEffect, useRef, useState} from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  View,
  PermissionsAndroid,
  Platform,
  useColorScheme,
  UIManager,
  findNodeHandle,
  Text,
} from 'react-native';
import {Colors} from 'react-native/Libraries/NewAppScreen';
import {requireNativeComponent} from 'react-native';

const ARView = requireNativeComponent('ARViewManager');

const requestCameraPermission = async () => {
  try {
    if (Platform.OS === 'android') {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.CAMERA,
        {
          title: 'Camera Permission',
          message: 'This app needs access to your camera to function properly.',
          buttonNeutral: 'Ask Me Later',
          buttonNegative: 'Cancel',
          buttonPositive: 'OK',
        },
      );
      if (granted === PermissionsAndroid.RESULTS.GRANTED) {
        console.log('You can use the camera');
      } else {
        console.log('Camera permission denied');
      }
    }
  } catch (err) {
    console.warn(err);
  }
};

const Section: React.FC = () => {
  const arViewRef = useRef(null);
  const [view, setView] = useState(false);

  useEffect(() => {
    if (arViewRef.current) {
      setView(true);
      console.log(arViewRef);
      UIManager.dispatchViewManagerCommand(
        findNodeHandle(arViewRef.current),
        UIManager.getViewManagerConfig('ARViewManager').Commands.create,
        [findNodeHandle(arViewRef.current)],
      );
    }
  }, []);

  return (
    <View style={styles.sectionContainer}>
      <ARView ref={arViewRef} />
      <Text>Hello</Text>
    </View>
  );
};

const App: React.FC = () => {
  const isDarkMode = useColorScheme() === 'dark';

  useEffect(() => {
    requestCameraPermission();
  }, []);

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  return (
    <SafeAreaView style={[backgroundStyle, styles.container]}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <ScrollView contentContainerStyle={styles.scrollView}>
        <Section />
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollView: {
    flexGrow: 1,
  },
  sectionContainer: {
    flex: 1,
    marginTop: 32,
    paddingHorizontal: 24,
  },
  arView: {
    flex: 1,
    width: '100%',
    height: '100%',
  },
});

export default App;
