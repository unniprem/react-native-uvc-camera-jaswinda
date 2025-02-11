import * as React from 'react';
import { NavigationContainer, useNavigation } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { useRef, useEffect } from 'react';
import { Button, Dimensions, Image, StyleSheet, View, Alert } from 'react-native';
import { UVCCamera } from '../../src/UVCCamera';
import { PermissionsAndroid } from 'react-native';

const Stack = createNativeStackNavigator();

const windowWidth = Dimensions.get('window').width;
const windowHeight = Dimensions.get('window').height;

export default function App() {
  return (
    <NavigationContainer>
      <Stack.Navigator initialRouteName="HomePage">
        <Stack.Screen name="HomePage" component={HomePage} />
        <Stack.Screen name="CameraPage" component={CameraPage} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

const HomePage = () => {
  const navigation = useNavigation();
  return (
    <View style={styles.root}>
      <Button
        title="Open CameraView"
        onPress={() => navigation.navigate('CameraPage')}
      />
    </View>
  );
};

const CameraPage = () => {
  const camera = useRef<UVCCamera>(null);
  const [picPath, setPicPath] = React.useState<string>();
  
  // Request camera permission when the component mounts
  useEffect(() => {
    const requestCameraPermission = async () => {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.CAMERA,
          {
            title: 'Camera Permission',
            message: 'This app needs access to your camera.',
            buttonNeutral: 'Ask Me Later',
            buttonNegative: 'Cancel',
            buttonPositive: 'OK',
          }
        );

        if (granted !== PermissionsAndroid.RESULTS.GRANTED) {
          Alert.alert("Camera permission denied");
        }
      } catch (err) {
        console.warn(err);
      }
    };

    requestCameraPermission();
  }, []);

  const takePhoto = async () => {
    if (!camera.current) {
      Alert.alert("Camera is not initialized");
      return;
    }
    
    try {
      const photo = await camera.current.takePhoto();
      setPicPath(photo.path);
      console.log('photo', photo);
    } catch (error) {
      console.log('Error taking photo:', error);
      Alert.alert("Failed to take photo", error.message);
    }
  };

  return (
    <View style={styles.root}>
      <UVCCamera ref={camera} style={styles.cameraView} />
      <View style={styles.controlBar}>
        <Button
          title="Open Camera"
          onPress={() => camera.current?.openCamera()}
        />
        <Button
          title="Close Camera"
          onPress={() => camera.current?.closeCamera()}
        />
        <Button title="Take Photo" onPress={takePhoto} />
      </View>
      {picPath && (
        <Image
          source={{ uri: `file:///${picPath}` }}
          style={styles.pic}
          resizeMode="contain"
        />
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  root: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  cameraView: {
    width: windowWidth,
    height: windowHeight,
  },
  controlBar: {
    position: 'absolute',
    top: 10,
    gap: 10,
  },
  pic: {
    position: 'absolute',
    backgroundColor: '#000',
    width: 100,
    height: 100,
    bottom: 20,
    right: 20,
  },
});
