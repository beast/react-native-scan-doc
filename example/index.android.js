/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  AppRegistry,
  Dimensions,
  StyleSheet,
  Text,
  View
} from 'react-native';
import Camera from 'react-native-camera';
import Scanner from 'react-native-scan-doc'

export default class example extends Component {
  constructor(props) {
    super(props);
    this.path = '';
  }

  render() {
    return (
      <View style={styles.container}>
        <Camera
          ref={(cam) => {
            this.camera = cam;
          }}
          style={styles.preview}
          aspect={Camera.constants.Aspect.fill}>
          <Text style={styles.capture} onPress={this.takePicture.bind(this)}>[CAPTURE]</Text>
        </Camera>
      </View>
    );
  }

  takePicture() {
    this.camera.capture()
      .then((data) => {
        console.warn(data.path);
        console.warn(data.data);
        Scanner.scan(data.path, 600, 800, 90, 'JPEG', '').then((path) => console.warn(path))
      })
      .catch(err => console.error(err));
    // console.warn(this.path);
    // const path = Scanner.scan(this.path, 600, 800, 90, 'JPEG', '')
    // console.warn(path);
  }
}


const styles = StyleSheet.create({
  container: {
    flex: 1
  },
  preview: {
    flex: 1,
    justifyContent: 'flex-end',
    alignItems: 'center',
    height: Dimensions.get('window').height,
    width: Dimensions.get('window').width
  },
  capture: {
    flex: 0,
    backgroundColor: '#fff',
    borderRadius: 5,
    color: '#000',
    padding: 10,
    margin: 40
  }
});

AppRegistry.registerComponent('example', () => example);
