
package my.fin;

import android.net.Uri;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
//import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Promise;
import android.graphics.Bitmap;


import java.io.IOException;

public class RNScanDocModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext mReactContext;

  public RNScanDocModule(ReactApplicationContext reactContext) {
    super(reactContext);
    mReactContext = reactContext;
    reactContext.addActivityEventListener(this);
  }

  @Override
  public String getName() {
    return "RNScanDoc";
  }

  @ReactMethod
  public void scan(String imagePath, String outputPath, Promise promise) {
    try {
      Activity currentActivity = getCurrentActivity();

      if (currentActivity == null) {
//        response = Arguments.createMap();
//        response.putString("path", Uri.fromFile().toString());
//        promise.resolve(response);
        promise.reject("no activity found.")
        return;
      }

      if (imagePath == null || imagePath.isEmpty() || outputPath == null || outputPath.isEmpty()) {
        promise.reject("no imagePath or outputPath was set.")
        return;
      }

      Uri uri = Uri.parse(imagePath)

      bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
      getContentResolver().delete(uri, null, null);


    } catch (IOException e) {
      failureCb.invoke(e.getMessage());
    }

  }

  private void scanWithExceptions()
}