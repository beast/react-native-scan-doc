
# react-native-scan-doc (WIP)

## Read before you proceed:
Most of the scan doc package out there cost a fortune. Last time I looked into one it cost tens of thousands euros a year.

Getting Opencv package to play nicely with RN out-of-box is almost impossible. Part of the reason is also that I try to avoid using install OpenCV manager for Android. Believe me I had tried make it as dummy as possible to the best of my knowledge. But the reality is setting up opencv to work nicely with Android itself is a challenging enough task. 

I suggest you look at a few examples to understand how OpenCV works with Android first then proceed to manual installation.
https://blog.nishtahir.com/2015/11/11/setting-up-for-android-ndk-development/

Try out my [Android Native Scan Doc](https://github.com/beast/android-opencv-scan-doc) sample if you want to first check out how the scan doc helps you to crop a doc in image.

Again, I am open to have some help from the community to improve the package documentation.

I myself unfortunately is tied up with other work of my own. Will look into this seriously once I got time and resource. Thanks.

## Getting started

`$ npm install react-native-scan-doc --save`

### Mostly automatic installation (You still need to setup OpenCV yourself, [read more](https://blog.nishtahir.com/2015/11/11/setting-up-for-android-ndk-development/))

`$ react-native link react-native-scan-doc`

##Features
### Android
- [x] Scan document.
- [x] Perspective Transform.
  
### iOS
- [ ] Scan document.
- [ ] Perspective Transform.

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
  
