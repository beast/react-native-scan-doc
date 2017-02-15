
package my.fin;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
//import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ImageView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import java.io.IOException;

class Line{
  Point _p1;
  Point _p2;
  Point _center;

  Line(Point p1, Point p2) {
    _p1 = p1;
    _p2 = p2;
    _center = new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
  }
}

public class RNScanDocModule extends ReactContextBaseJavaModule {
  Bitmap srcBitmap;
  Bitmap grayBitmap;
  Bitmap cannyBitmap;
  Bitmap linesBitmap;
  Bitmap origBitmap;
  Bitmap dstBitmap;
  Bitmap outputBitmap;

  public final static String BASE64_PREFIX = "data:image/";
  public final static String CONTENT_PREFIX = "content://";
  public final static String FILE_PREFIX = "file:";

  private final ReactApplicationContext mReactContext;

  public RNScanDocModule(ReactApplicationContext reactContext) {
    super(reactContext);
    mReactContext = reactContext;
    OpenCVLoader.initDebug();
  }

  @Override
  public String getName() {
    return "RNScanDoc";
  }

  @ReactMethod
  public void scan(String imagePath, int newWidth, int newHeight, int quality, String compressFormatString, String outputPath, Promise promise) {
    try {
      Bitmap sourceImage;

      // If the BASE64_PREFIX is absent, load bitmap from a file. Otherwise, load from base64.
      if (!imagePath.startsWith(BASE64_PREFIX)) {
        sourceImage = loadBitmapFromFile(mReactContext, imagePath, newWidth, newHeight);
      }
      else {
        sourceImage = loadBitmapFromBase64(imagePath);
      }

      if (sourceImage == null) {
        promise.reject("1","Unable to load source image from path");
      }
      //find edges
      outputBitmap = findEdges(sourceImage);

      // Save the resulting image
      File path = mReactContext.getCacheDir();
      if (outputPath != null) {
        path = new File(outputPath);
      }

      Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.valueOf(compressFormatString);

      File file = saveImage(outputBitmap, path,
              Long.toString(new Date().getTime()), compressFormat, quality);

      WritableMap response = new WritableNativeMap();
      response.putString("path", Uri.fromFile(file).toString());
      promise.resolve(response);

    } catch (Exception e) {
      promise.reject("2",e.getMessage());
    }

  }
  /**
   * Save the given bitmap in a directory. Extension is automatically generated using the bitmap format.
   */
  private static File saveImage(Bitmap bitmap, File saveDirectory, String fileName,
                                  Bitmap.CompressFormat compressFormat, int quality)
          throws IOException {
    if (bitmap == null) {
      throw new IOException("The bitmap couldn't be resized");
    }

    File newFile = new File(saveDirectory, fileName + "." + compressFormat.name());
    if(!newFile.createNewFile()) {
      throw new IOException("The file already exists");
    }

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    bitmap.compress(compressFormat, quality, outputStream);
    byte[] bitmapData = outputStream.toByteArray();

    outputStream.flush();
    outputStream.close();

    FileOutputStream fos = new FileOutputStream(newFile);
    fos.write(bitmapData);
    fos.flush();
    fos.close();

    return newFile;
  }


  /**
   * Loads the bitmap resource from the file specified in imagePath.
   */
  private static Bitmap loadBitmapFromFile(Context context, String imagePath, int newWidth,
                                           int newHeight) throws IOException  {
    // Decode the image bounds to find the size of the source image.
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    loadBitmap(context, imagePath, options);

    // Set a sample size according to the image size to lower memory usage.
    options.inSampleSize = calculateInSampleSize(options, newWidth, newHeight);
    options.inJustDecodeBounds = false;
    System.out.println(options.inSampleSize);
    return loadBitmap(context, imagePath, options);

  }

  /**
   * Compute the inSampleSize value to use to load a bitmap.
   * Adapted from https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
   */
  private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    final int height = options.outHeight;
    final int width = options.outWidth;

    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
      final int halfHeight = height / 2;
      final int halfWidth = width / 2;

      // Calculate the largest inSampleSize value that is a power of 2 and keeps both
      // height and width larger than the requested height and width.
      while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
        inSampleSize *= 2;
      }
    }

    return inSampleSize;
  }

  /**
   * Loads the bitmap resource from a base64 encoded jpg or png.
   * Format is as such:
   * png: 'data:image/png;base64,iVBORw0KGgoAA...'
   * jpg: 'data:image/jpeg;base64,/9j/4AAQSkZJ...'
   */
  private static Bitmap loadBitmapFromBase64(String imagePath) {
    Bitmap sourceImage = null;

    // base64 image.  Convert to a bitmap.
    final int prefixLen = BASE64_PREFIX.length();
    final boolean isJpeg = (imagePath.indexOf("jpeg") == prefixLen);
    final boolean isPng = (!isJpeg) && (imagePath.indexOf("png") == prefixLen);
    int commaLocation = -1;
    if (isJpeg || isPng){
      commaLocation = imagePath.indexOf(',');
    }
    if (commaLocation > 0) {
      final String encodedImage = imagePath.substring(commaLocation+1);
      final byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
      sourceImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    return sourceImage;
  }

  /**
   * Load a bitmap either from a real file or using the {@link ContentResolver} of the current
   * {@link Context} (to read gallery images for example).
   *
   * Note that, when options.inJustDecodeBounds = true, we actually expect sourceImage to remain
   * as null (see https://developer.android.com/training/displaying-bitmaps/load-bitmap.html), so
   * getting null sourceImage at the completion of this method is not always worthy of an error.
   */
  private static Bitmap loadBitmap(Context context, String imagePath, BitmapFactory.Options options) throws IOException {
    Bitmap sourceImage = null;
    if (!imagePath.startsWith(CONTENT_PREFIX)) {
      try {
        sourceImage = BitmapFactory.decodeFile(imagePath, options);
      } catch (Exception e) {
        e.printStackTrace();
        throw new IOException("Error decoding image file");
      }
    } else {
      ContentResolver cr = context.getContentResolver();
      InputStream input = cr.openInputStream(Uri.parse(imagePath));
      if (input != null) {
        sourceImage = BitmapFactory.decodeStream(input, null, options);
        input.close();
      }
    }
    return sourceImage;
  }

  protected Bitmap findEdges(Bitmap bitmap) {

    // https://github.com/daisygao/ScannerLites
    Mat rgbMat = new Mat();
    Mat grayMat = new Mat();
    Mat cannyMat;
    Mat linesMat = new Mat();
    BitmapFactory.Options o=new BitmapFactory.Options();

    // define the destination image size: A4 - 200 PPI
    int w_a4 = 1654, h_a4 = 2339;

    // TODO: 29/08/2016  May need to check sample size https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    o.inSampleSize = 4;
    o.inDither=false;


    // get bitmap
//    origBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.card4, o);
    origBitmap = bitmap;

    int w = origBitmap.getWidth();
    int h = origBitmap.getHeight();
    int min_w = 800;
    double scale = Math.min(10.0, w*1.0/ min_w);
    int w_proc = (int) (w * 1.0 / scale);
    int h_proc = (int) (h * 1.0 / scale);
    srcBitmap = Bitmap.createScaledBitmap(origBitmap, w_proc, h_proc, false);
    grayBitmap = Bitmap.createBitmap(w_proc, h_proc, Bitmap.Config.RGB_565);
    cannyBitmap = Bitmap.createBitmap(w_proc, h_proc, Bitmap.Config.RGB_565);
    linesBitmap = Bitmap.createBitmap(w_proc, h_proc, Bitmap.Config.RGB_565);


    Utils.bitmapToMat(srcBitmap, rgbMat);//convert original bitmap to Mat, R G B.

    // grayscale
    Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//rgbMat to gray grayMat

    // canny
    cannyMat = getCanny(grayMat);

    // HoughLinesP
    Imgproc.HoughLinesP(cannyMat,linesMat, 1, Math.PI/180, w_proc/12, w_proc/12, 20 );

    // Calculate horizontal lines and vertical lines
    Log.e("opencv","lines.cols " + linesMat.cols() + " w_proc/3: " + w_proc/3);
    List<Line> horizontals = new ArrayList<>();
    List<Line> verticals = new ArrayList<>();
    for (int x = 0; x < linesMat.rows(); x++)
    {
      double[] vec = linesMat.get(x, 0);
      double x1 = vec[0],
              y1 = vec[1],
              x2 = vec[2],
              y2 = vec[3];
      Point start = new Point(x1, y1);
      Point end = new Point(x2, y2);
      Line line = new Line(start, end);
      if (Math.abs(x1 - x2) > Math.abs(y1-y2)) {
        horizontals.add(line);
      } else {
        verticals.add(line);
      }

      // for visualization in debug mode
      if (BuildConfig.DEBUG) {
//                Imgproc.line(cannyMat, start, end, new Scalar(255,0,0), 10, Imgproc.LINE_AA, 0);
      }
    }

    Log.e("HoughLines","completed HoughLines");
    Log.e("HoughLines","linesMat size: " + linesMat.size());
    Log.e("HoughLines", "linesBitmap size: " + Integer.toString(linesBitmap.getHeight()) +" x " + Integer.toString(linesBitmap.getWidth()));
    Log.e("Lines Detected", Integer.toString(linesMat.rows()));

    if (linesMat.rows() > 400)
    {
      Context context = getReactApplicationContext();
      int duration = Toast.LENGTH_LONG;
      Toast toast = Toast.makeText(context, "Please use a cleaner background",duration);
      toast.show();
    }

    // if we don't have at least 2 horizontal lines or vertical lines
    if (horizontals.size() < 2) {
      if (horizontals.size() == 0 || horizontals.get(0)._center.y > h_proc /2) {
        horizontals.add(new Line(new Point(0,0),new Point(w_proc-1, 0)));
      }
      if (horizontals.size() == 0 || horizontals.get(0)._center.y <= h_proc /2) {
        horizontals.add(new Line(new Point(0,h_proc-1),new Point(w_proc-1, h_proc-1)));
      }
    }
    if (verticals.size() < 2) {
      if (verticals.size() == 0 || verticals.get(0)._center.x > w_proc / 2) {
        verticals.add(new Line(new Point(0, 0), new Point(h_proc - 1, 0)));
      }
      if (verticals.size() == 0 || verticals.get(0)._center.x <= w_proc / 2) {
        verticals.add(new Line(new Point(w_proc - 1, 0), new Point(w_proc - 1, h_proc - 1)));
      }
    }

    Collections.sort(horizontals, new Comparator<Line>() {
      @Override
      public int compare(Line lhs, Line rhs) {
        return (int)(lhs._center.y - rhs._center.y);
      }
    });

    Collections.sort(verticals, new Comparator<Line>() {
      @Override
      public int compare(Line lhs, Line rhs) {
        return (int)(lhs._center.x - rhs._center.x);
      }
    });


    // for visualization in debug mode
    if (BuildConfig.DEBUG) {
//            Imgproc.line(rgbMat, horizontals.get(0)._p1, horizontals.get(0)._p2, new Scalar(0,255,0), 10, Imgproc.LINE_AA, 0);
//            Imgproc.line(rgbMat, horizontals.get(horizontals.size()-1)._p1, horizontals.get(horizontals.size()-1)._p2, new Scalar(0,255,0), 10, Imgproc.LINE_AA, 0);
//            Imgproc.line(rgbMat, verticals.get(0)._p1, verticals.get(0)._p2, new Scalar(255,0,0), 10, Imgproc.LINE_AA, 0);
//            Imgproc.line(rgbMat, verticals.get(verticals.size()-1)._p1, verticals.get(verticals.size()-1)._p2, new Scalar(255,0,0), 10, Imgproc.LINE_AA, 0);
    }

    // compute intersections
    List<Point> intersections = new ArrayList<>();
    intersections.add(computeIntersection(horizontals.get(0),verticals.get(0)));
    intersections.add(computeIntersection(horizontals.get(0),verticals.get(verticals.size()-1)));
    intersections.add(computeIntersection(horizontals.get(horizontals.size()-1),verticals.get(0)));
    intersections.add(computeIntersection(horizontals.get(horizontals.size()-1),verticals.get(verticals.size()-1)));

    Log.e("Intersections", Double.toString(intersections.get(0).x));

    for (Point point: intersections) {
      // for visualization in debug mode
      if (BuildConfig.DEBUG) {
//                Imgproc.circle(rgbMat, point, 10, new Scalar(255,255,0),3);
      }
//            point.x *= scale;
//            point.y *= scale;
    }

    Log.e("Intersections", Double.toString(intersections.get(0).x));

    // perspective tranform

    // Calculate max width and height
    double w1 = Math.sqrt( Math.pow(intersections.get(3).x - intersections.get(2).x , 2) + Math.pow(intersections.get(3).x - intersections.get(2).x , 2));
    double w2 = Math.sqrt( Math.pow(intersections.get(1).x - intersections.get(0).x , 2) + Math.pow(intersections.get(1).x - intersections.get(0).x , 2));
    double h1 = Math.sqrt( Math.pow(intersections.get(1).y - intersections.get(3).y , 2) + Math.pow(intersections.get(1).y - intersections.get(3).y , 2));
    double h2 = Math.sqrt( Math.pow(intersections.get(0).y - intersections.get(2).y , 2) + Math.pow(intersections.get(0).y - intersections.get(2).y , 2));

    double maxWidth = (w1 < w2) ? w1 : w2;
    double maxHeight = (h1 < h2) ? h1 : h2;

    // source Mat from earlier intersection calculations
    Mat srcMat = new Mat(4,1,CvType.CV_32FC2);
    srcMat.put(0,0,intersections.get(0).x,intersections.get(0).y,intersections.get(1).x,intersections.get(1).y,intersections.get(2).x,intersections.get(2).y,intersections.get(3).x,intersections.get(3).y);

    Mat dstMat = new Mat(4,1,CvType.CV_32FC2);
    dstMat.put(0,0, 0.0,0.0, maxWidth-1,0.0, 0.0,maxHeight-1, maxWidth-1, maxHeight-1);
//        dstMat.put(0,0, 0.0,0.0, w_a4-1,0.0, 0.0,h_a4-1, w_a4-1, h_a4-1);
//        dstMat.put(0,0,intersections.get(0).x,intersections.get(0).y,intersections.get(1).x,intersections.get(1).y,intersections.get(2).x,intersections.get(2).y,intersections.get(3).x,intersections.get(3).y);

    Log.e("FinalDisplay","srcMat: " + srcMat.size());
    Log.e("FinalDisplay","dstMat: " + dstMat.size());

//        // transformation matrix
    Mat transformMatrix = Imgproc.getPerspectiveTransform(srcMat,dstMat);

    Mat finalMat = Mat.zeros((int)maxHeight, (int)maxWidth ,CvType.CV_32FC2);
    Imgproc.warpPerspective(rgbMat, finalMat, transformMatrix, finalMat.size());
    Log.e("FinalDisplay","finalMat: " + finalMat.size());

    // display final results
    dstBitmap = Bitmap.createBitmap(finalMat.width(), finalMat.height(), Bitmap.Config.RGB_565);
    Log.e("FinalDisplay","dstBitmap: " + dstBitmap.getWidth() + " x " + dstBitmap.getHeight());
    Utils.matToBitmap(finalMat, dstBitmap); //convert mat to bitmap
//    img.setImageBitmap(dstBitmap);
    return dstBitmap;
  }

  protected Mat getCanny(Mat gray) {
    Mat threshold = new Mat();
    Mat canny = new Mat();
    // last paramter 8 is using OTSU algorithm
    double high_threshold = Imgproc.threshold(gray, threshold, 0, 255, 8);
    double low_threshold = high_threshold * 0.5;
    Imgproc.Canny(gray, canny, low_threshold, high_threshold);
    return canny;
  }

  protected Point computeIntersection (Line l1, Line l2) {
    double x1 = l1._p1.x, x2= l1._p2.x, y1 = l1._p1.y, y2 = l1._p2.y;
    double x3 = l2._p1.x, x4 = l2._p2.x, y3 = l2._p1.y, y4 = l2._p2.y;
    double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
    Point pt = new Point();
    pt.x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
    pt.y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
    return pt;
  }
}