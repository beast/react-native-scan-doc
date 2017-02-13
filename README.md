
# react-native-scan-doc (WIP)

## Getting started

`$ npm install react-native-scan-doc --save`

### Mostly automatic installation

`$ react-native link react-native-scan-doc`

##Features
### Android
- [x] Scan document.
- [x] Perspective Transform.
  
### iOS
- [x] Scan document.
- [x] Perspective Transform.

##Requirements
### Android
API 16+
### iOS
iOS 8+
### React Native
RN 0.38+

### Manual installation

#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-scan-doc` and add `RNScanDoc.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNScanDoc.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import my.fin.RNScanDocPackage;` to the imports at the top of the file
  - Add `new RNScanDocPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-scan-doc'
  	project(':react-native-scan-doc').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-scan-doc/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-scan-doc')
  	```

## Usage
```javascript
import RNScanDoc from 'react-native-scan-doc';

// TODO: What do with the module?
RNScanDoc;
```
  