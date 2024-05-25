/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */
import React, {Component, useEffect, useRef, useState} from 'react';
import {
    SafeAreaView,
    View,
    Text,
    StyleSheet,
    PermissionsAndroid,
    FlatList,
    TouchableOpacity,
} from 'react-native';
import {
    AugmentedFacesView,
} from './augmented_face';
import {it} from "@jest/globals";

const ExampleSet = [
    {title: 'Mesh', model: 'models/face.glb', texture: 'textures/face.png'},
    {title: 'Fox', model: 'models/fox.glb', texture: 'textures/freckles.png'},
    {title: 'Mario hat', model: 'models/mario-hat.glb'},
    {title: 'Hat', model: 'models/hat.glb'},
    {title: 'Rabbit', model: 'models/Rabbit.glb'},
    {title: 'rabbit', model: 'models/rabbit.glb'},
    {title: 'sunglasses', model: 'models/sunglasses.fbx'},
];

const App = () => {
    const [cameraPermissionGranted, setcameraPermissionGranted] = useState(false)
    const [augmentedFaceIndex, setaugmentedFaceIndex] = useState(-1)
    const [augmentedFaces, setaugmentedFaces] = useState([])

    const augmentedFacesView = useRef(null);

    useEffect(() => {
        checkPermissions()
    }, [])
    const checkPermissions = async () => {
        const actual = await PermissionsAndroid.check(
            PermissionsAndroid.PERMISSIONS.CAMERA,
        );
        if (actual) {
            setcameraPermissionGranted(true);
            loadAssets()
        } else {
            const permission = await PermissionsAndroid.request(
                PermissionsAndroid.PERMISSIONS.CAMERA,
            );
            if (permission == 'granted') {
                setcameraPermissionGranted(true)
                loadAssets()
            }
        }
    };

    const loadAssets = () => {
        ExampleSet.forEach(item => {
            augmentedFacesView?.current?.addAugmentedFace?.(item).then(index => {
                const faces = [...augmentedFaces, {...item, index}];
                setaugmentedFaces(faces)
            });
        });
    };

    const takePicture = () => {
        if (!augmentedFacesView) {
            console.error('AugmentedFacesView ref is not set.');
            return;
        }

        augmentedFacesView?.current?.takeScreenshot()
            .then(data => {
                console.log('Screenshot taken successfully.', data);
            })
            .catch(err => {
                console.error('Error while taking screenshot:', err);
            });
    };

    return (
        <SafeAreaView style={styles.container}>
            <TouchableOpacity onPress={takePicture}>
                <Text> Test </Text>
            </TouchableOpacity>
            {cameraPermissionGranted && (
                <View style={styles.container}>
                    <AugmentedFacesView
                        style={styles.camera}
                        setAugmentedFace={augmentedFaceIndex}
                        ref={augmentedFacesView}
                    />
                    <View style={styles.overlay}>
                        <FlatList
                            horizontal={true}
                            data={augmentedFaces}
                            renderItem={({item}) => {
                                return (
                                    <TouchableOpacity
                                        style={styles.touchable}
                                        onPress={() => {
                                            setaugmentedFaceIndex(item.index);
                                        }}>
                                        <View style={styles.touchable_body}>
                                            <Text style={styles.text}>{item.title}</Text>
                                        </View>
                                    </TouchableOpacity>
                                );
                            }}
                            style={styles.flatlist}
                            contentContainerStyle={styles.flatlist_container}
                        />
                    </View>
                </View>
            )}
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    container: {
        width: '100%',
        height: '100%',
        backgroundColor: 'white',
    },
    camera: {
        flex: 1,
    },
    overlay: {
        zIndex: 2,
        width: '100%',
        height: '100%',
        position: 'absolute',
        top: 0,
    },
    flatlist: {
        width: '100%',
        height: 60,
        position: 'absolute',
        bottom: 0,
        backgroundColor: 'rgba(0,0,0,0.25)',
    },
    flatlist_container: {
        alignItems: 'center',
    },
    touchable: {
        width: 100,
        height: 50,
        padding: 5,
    },
    touchable_body: {
        width: '100%',
        height: '100%',
        backgroundColor: 'white',
        borderRadius: 5,
        elevation: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
    text: {
        textAlign: 'center',
        textAlignVertical: 'center',
    },
});

export default App;
