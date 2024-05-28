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
import {requireNativeComponent, NativeModules} from 'react-native';

const {ARModule} = NativeModules;
const ARView =
  Platform.OS === 'android' ? requireNativeComponent('ARViewManager') : null;

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
    if (Platform.OS === 'android' && arViewRef.current) {
      setView(true);
      console.log(arViewRef);
      UIManager.dispatchViewManagerCommand(
        findNodeHandle(arViewRef.current),
        UIManager.getViewManagerConfig('ARViewManager').Commands.create,
        [findNodeHandle(arViewRef.current)],
      );
    }
  }, []);

  if (Platform.OS === 'ios') {
    useEffect(() => {
      ARModule.startARSession();
    }, []);
    return (
      <View style={styles.sectionContainer}>
        <Text>iOS AR Session Started</Text>
      </View>
    );
  }

  return (
    <View style={styles.sectionContainer}>
      {ARView && <ARView ref={arViewRef} style={styles.arView} />}
      <Text>Hello</Text>
    </View>
  );
};

const App: React.FC = () => {
  const isDarkMode = useColorScheme() === 'dark';

  useEffect(() => {
    if (Platform.OS === 'android') {
      requestCameraPermission();
    }
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
