package steering;


import fakefirm.Arduino;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SteeringMk2 extends SteeringBase {

    private Boolean leftSideFound = false;
    private Boolean rightSideFound = false;

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private int initialDelay = 20;
    private int SteerPin;
    private Arduino theServos = null;
    private int averageOffset = 0;
    private int framesCompiled = 0;

    boolean haveNewPixels = false;

    public SteeringMk2(int steerPin, Arduino theServos) {
        this.SteerPin = steerPin;
        this.theServos = theServos;
        executor.scheduleAtFixedRate(() -> makeTurnAdjustment(), initialDelay, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public int drive(int pixels[]) {
        findPoints(pixels);
        averagePoints();
        return getDegreeOffset();
    }

    private void makeTurnAdjustment() {
        if (haveNewPixels) {
            theServos.servoWrite(SteerPin, getDegreeOffset() + 90);
            averageOffset = 0;
            framesCompiled = 0;
        }
    }

    @Override
    public void findPoints(int[] pixels) {
        clearArrays();
        int midX = cameraWidth / 2; // midX is where the car thinks is the middle of the road
        double distanceAhead = 1.8; // how far ahead the car looks for road. (Eventually dynamic?)
        int screenWidth = 912;


        // Iterate through each row in camera
        for (int cameraRow = screenHeight - 50; cameraRow > (int) (screenHeight / distanceAhead); cameraRow--) {

            // Find left point
            for (int cameraColumn = midX; cameraColumn >= 0; cameraColumn--) {
                if (!leftSideFound && pixels[(screenWidth * (cameraRow)) + cameraColumn] >= 16777215) {
                    leftSideFound = true;
                    leftPoints.add(new Point(cameraColumn, cameraRow));
                    break;
                }
            }

            // Find Right point
            for (int cameraColumn = midX; cameraColumn <= cameraWidth; cameraColumn++) {
                if (!rightSideFound && pixels[(screenWidth * (cameraRow - 1)) + cameraColumn] >= 16777215) {
                    rightSideFound = true;
                    rightPoints.add(new Point(cameraColumn, cameraRow));
                    break;
                }
            }

            // If two Lanes are found, average the two
            if (rightSideFound && leftSideFound) {
                midX = (rightPoints.get(rightPoints.size() - 1).x + leftPoints.get(leftPoints.size() - 1).x) / 2;
                midPoints.add(new Point(midX, cameraRow));

                // If One lane is found, add midpoint 100 pixels towards middle.
            } else if (rightSideFound) {
                double lastY = rightPoints.get(rightPoints.size() - 1).y;
                int lastX = rightPoints.get(rightPoints.size() - 1).x;
                midX = (int)Math.round(lastX - ((cameraWidth) * Math.pow((lastY) / (screenHeight), 2)));
                midPoints.add(new Point(midX, cameraRow));
            } else if (leftSideFound) {
                double lastY = leftPoints.get(leftPoints.size() - 1).y;
                int lastX = leftPoints.get(leftPoints.size() - 1).x;
                midX = (int)Math.round(lastX + ((cameraWidth) * Math.pow((lastY) / (screenHeight), 2)));
                midPoints.add(new Point(midX, cameraRow));

                // If no lanes are found, route towards found lines.
            } else {
                midX = cameraWidth / 2;
                midPoints.add(new Point(midX, cameraRow));
            }

            rightSideFound = false;
            leftSideFound = false;
        }
        averagePoints();
        haveNewPixels = true;
    }

    private void clearArrays() {
        leftPoints.clear();
        rightPoints.clear();
        midPoints.clear();
    }

    private void averagePoints() {

        startTarget = (int)(midPoints.size() * 0.5);
        endTarget = (int)(midPoints.size() * 0.7);

        double ySum = 0;
        double xSum = 0;

        // Sum the x's and the y's
        for (int idx = startTarget; idx <= endTarget; idx++) {
            xSum += midPoints.get(idx).x;
            ySum += midPoints.get(idx).y;
        }

        steerPoint.x = (int) (xSum / (endTarget - startTarget));
        steerPoint.y = (int) (ySum / (endTarget - startTarget));

        //averageOffset += getDegreeOffset();
        //framesCompiled++;
        //System.out.println("AVERAGE OFFSET: " + (averageOffset / framesCompiled));
    }

}
