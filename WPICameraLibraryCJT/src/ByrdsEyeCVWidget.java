/*
 * Vision Code
 */

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_imgproc.IplConvKernel;
import edu.wpi.first.smartdashboard.camera.WPICameraExtension;
import edu.wpi.first.smartdashboard.robot.Robot;
import edu.wpi.first.wpijavacv.DaisyExtensions;
import edu.wpi.first.wpijavacv.WPIBinaryImage;
import edu.wpi.first.wpijavacv.WPIColor;
import edu.wpi.first.wpijavacv.WPIColorImage;
import edu.wpi.first.wpijavacv.WPIContour;
import edu.wpi.first.wpijavacv.WPIImage;
import edu.wpi.first.wpijavacv.WPIPoint;
import edu.wpi.first.wpijavacv.WPIPolygon;
import java.util.ArrayList;

/**
 *
 * @author PROGRAMING FRC 2013
 */

/* HOW TO GET THIS COMPILING IN NETBEANS:
 *  1. Install the SmartDashboard using the installer (if on Windows)
 *      1a. Verify that the OpenCV libraries are in your PATH (on Windows)
 *  2. Add the following libraries to the project:
 *     SmartDashboard.jar
 *     extensions/WPICameraExtension.jar
 *     lib/NetworkTable_Client.jar
 *     extensions/lib/javacpp.jar
 *     extensions/lib/javacv-*your environment*.jar
 *     extensions/lib/javacv.jar
 *     extensions/lib/WPIJavaCV.jar
 *
 */
/*
 *  CJT Jan 11 2013
 * This class is intended to test the WPICameraExtension class
 * Note that to get this to work we need to include as libraries the jar files 
 * that are currently stored in C:\Program Files\SmartDashboard
 * Specifically include SmartDashboard.jar and all of the jars in extensions/lib directory
 */
public class ByrdsEyeCVWidget extends WPICameraExtension {

    public static final String NAME = "CJT Target Tracker";
    
    private WPIColor targetColor = new WPIColor(0, 255, 0);
    
    // Constants that need to be tuned
    private static final double kNearlyHorizontalSlope = Math.tan(Math.toRadians(20));
    private static final double kNearlyVerticalSlope = Math.tan(Math.toRadians(90 - 20));
    private static final int kMinWidth = 20;
    private static final int kMaxWidth = 200;
    
    // Store JavaCV temporaries as members to reduce memory management during processing
    private CvSize size = null;
    private WPIContour[] contours;
    private ArrayList<WPIPolygon> polygons;
    private IplConvKernel morphKernel;
    private IplImage bin;
    private IplImage hsv;
    private IplImage hue;
    private IplImage sat;
    private IplImage val;
    private WPIPoint linePt1;
    private WPIPoint linePt2;

    public ByrdsEyeCVWidget() {
        morphKernel = IplConvKernel.create(3, 3, 1, 1, opencv_imgproc.CV_SHAPE_RECT, null);
        DaisyExtensions.init();
    }

    @Override
    public WPIImage processImage(WPIColorImage rawImage) {

        if (size == null || size.width() != rawImage.getWidth() || size.height() != rawImage.getHeight()) {

            // Note that the code in here will be executed once the first time through or anytime the image size changes

            size = opencv_core.cvSize(rawImage.getWidth(), rawImage.getHeight());
            bin = IplImage.create(size, 8, 1);
            hsv = IplImage.create(size, 8, 3);
            hue = IplImage.create(size, 8, 1);
            sat = IplImage.create(size, 8, 1);
            val = IplImage.create(size, 8, 1);

            linePt1 = new WPIPoint(size.width() / 2, size.height() - 1);
            linePt2 = new WPIPoint(size.width() / 2, 0);

        }

        // Get the raw IplImages for OpenCV
        IplImage input = DaisyExtensions.getIplImage(rawImage);

        // Convert to HSV color space
        opencv_imgproc.cvCvtColor(input, hsv, opencv_imgproc.CV_BGR2HSV);
        opencv_core.cvSplit(hsv, hue, sat, val, null);

        // Threshold each component separately
        // Hue, Daisy 45..255
        // NOTE: Red is at the end of the color space, so you need to OR together
        // a thresh and inverted thresh in order to get points that are red
        int hueThresholdLow = 0;
        int hueThresholdHigh = 255;
        opencv_imgproc.cvThreshold(hue, bin, hueThresholdLow, hueThresholdHigh, opencv_imgproc.CV_THRESH_BINARY);

        // Saturation, Daisy was 200..255
        // 3929 Blue:  100..255
        int satThresholdLow = 0;
        int satThresholdHigh = 255;
        opencv_imgproc.cvThreshold(sat, sat, satThresholdLow, satThresholdHigh, opencv_imgproc.CV_THRESH_BINARY);

        // Value Daisy was 55..255
        // 3929 Blue:  135..255
        int valThresholdLow = 250;
        int valThresholdHigh = 255;
        opencv_imgproc.cvThreshold(val, val, valThresholdLow, valThresholdHigh, opencv_imgproc.CV_THRESH_BINARY);

        // Combine the results to obtain our binary image which should for the most
        // part only contain pixels that we care about
        opencv_core.cvAnd(hue, bin, bin, null);
        opencv_core.cvAnd(bin, sat, bin, null);
        opencv_core.cvAnd(bin, val, bin, null);

        // Fill in any gaps using binary morphology
        opencv_imgproc.cvMorphologyEx(bin, bin, null, morphKernel, opencv_imgproc.CV_MOP_CLOSE, 9);

        // Find contours
        WPIBinaryImage binWpi = DaisyExtensions.makeWPIBinaryImage(bin);
        contours = DaisyExtensions.findConvexContours(binWpi);

        // polygons = new ArrayList<WPIPolygon>();
        polygons = new ArrayList<>();

        for (WPIContour c : contours) {
            double ratio = ((double) c.getHeight()) / ((double) c.getWidth());
            if (ratio < 1.0 && ratio > 0.5 && c.getWidth() > kMinWidth && c.getWidth() < kMaxWidth) {
                polygons.add(c.approxPolygon(20));
            }
        }

        WPIPolygon square = null;
        int highest = Integer.MAX_VALUE;

        for (WPIPolygon p : polygons) {
            if (p.isConvex() && p.getNumVertices() == 4) {
                // We passed the first test...we fit a rectangle to the polygon
                // Now do some more tests

                WPIPoint[] points = p.getPoints();
                // We expect to see a top line that is nearly horizontal, and two side lines that are nearly vertical
                int numNearlyHorizontal = 0;
                int numNearlyVertical = 0;
                for (int i = 0; i < 4; i++) {
                    double dy = points[i].getY() - points[(i + 1) % 4].getY();
                    double dx = points[i].getX() - points[(i + 1) % 4].getX();
                    double slope = Double.MAX_VALUE;
                    if (dx != 0) {
                        slope = Math.abs(dy / dx);
                    }

                    if (slope < kNearlyHorizontalSlope) {
                        ++numNearlyHorizontal;
                    } else if (slope > kNearlyVerticalSlope) {
                        ++numNearlyVertical;
                    }
                }

                // TODO:  HIGHEST TARGET IS ALWAYS CHOSEN
                // QUESTION WHETHER WE WANT TO PUT OTHER TARGETS
                // ONTO THE FIRING TABLE
                if (numNearlyHorizontal >= 1 && numNearlyVertical == 2) {
                    rawImage.drawPolygon(p, WPIColor.BLUE, 2);

                    int pCenterX = (p.getX() + (p.getWidth() / 2));
                    int pCenterY = (p.getY() + (p.getHeight() / 2));

                    rawImage.drawPoint(new WPIPoint(pCenterX, pCenterY), targetColor, 5);
                    if (pCenterY < highest) // Because coord system is funny
                    {
                        square = p;
                        highest = pCenterY;
                    }
                }
            } else {
                rawImage.drawPolygon(p, WPIColor.YELLOW, 1);
            }
        }

        if (square != null) {
            double x = square.getX() + (square.getWidth() / 2);
            double y = square.getY() + (square.getHeight() / 2);

            // These statements map x and y onto the range -1, +1
            // x = (2 * (x / size.width())) - 1;
            // y = -((2 * (y / size.height())) - 1);

            Robot.getTable().putNumber("x coordinate", x);
            Robot.getTable().putNumber("y coordinate", y);

        }

        // Draw a crosshair
        rawImage.drawLine(linePt1, linePt2, targetColor, 2);

        DaisyExtensions.releaseMemory();

        //System.gc();

        return rawImage;
    }
}
