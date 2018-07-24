package steering;

import apw3.DriverCons;

import java.util.ArrayList;
import java.util.List;


public class SteeringMk2 extends SteeringBase {

    private int cameraWidth = 640;
    private int screenHeight = DriverCons.D_ImHi;

    private Point origin = new Point(cameraWidth / 2, screenHeight);

    private Boolean leftSideFound = false;
    private Boolean rightSideFound = false;

    private double integral, previousError;

    @Override
    public double curveSteepness(double turnAngle) {
        return Math.abs(turnAngle) / (45);
    }

    @Override
    public int drive(int pixels[]) {
        findPoints(pixels);
        averageMidpoints();
        return getDegreeOffset();
    }

    private void findPoints(int[] pixels) {
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
                midX = rightPoints.get(rightPoints.size() - 1).x - 200;
                midPoints.add(new Point(midX, cameraRow));
            } else if (leftSideFound) {
                midX = leftPoints.get(leftPoints.size() - 1).x + 200;
                midPoints.add(new Point(midX, cameraRow));

                // If no lanes are found, route towards found lines.
            } else {
                //FINISH LATER
            }

            rightSideFound = false;
            leftSideFound = false;
        }
    }

    private void clearArrays() {
        leftPoints.clear();
        rightPoints.clear();
        midPoints.clear();
    }

    private void averageMidpoints() {

        double tempY = 0;
        double tempX = 0;

        // Sum the x's and the y's
        for (Point point : midPoints) {
            tempX += point.x;
            tempY += point.y;
        }

        steerPoint.x = (int) (tempX / midPoints.size());
        steerPoint.y = (int) (tempY / midPoints.size());

    }

    private int getDegreeOffset() {
        int xOffset = origin.x - steerPoint.x;
        int yOffset = Math.abs(origin.y - steerPoint.y);
        System.out.println("\n\n\n" + myPID() + " " + xOffset + "\n\n\n");

        return (int) Math.round((Math.atan2(-xOffset, yOffset)) * (180 / Math.PI));
    }

    private double myPID() {

        int error = origin.x - steerPoint.x;
        double kP = 0.65;
        double kI = 1;
        double kD = 1;
        double derivative;


        integral += error * DriverCons.D_FrameTime;
        if (error == 0 || (Math.abs(error - previousError) == (Math.abs(error) + Math.abs(previousError)))) {
            integral = 0;
        }
        if (Math.abs(integral) > 100) {
            integral = 0;
        }

        derivative = (error - previousError) / DriverCons.D_FrameTime;
        previousError = error;
        return error * kP + integral * kI + derivative * kD;

    }

}
