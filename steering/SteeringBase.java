package steering;

import apw3.DriverCons;

import java.util.ArrayList;
import java.util.List;

public abstract class SteeringBase implements Steerable {
    public Point steerPoint = new Point(0, 0);

    public List<Point> leftPoints = new ArrayList<>();
    public List<Point> rightPoints = new ArrayList<>();
    public List<Point> midPoints = new ArrayList<>();
    public int cameraWidth = 640;
    public int screenHeight = DriverCons.D_ImHi;
    private double integral, previousError;

    public int startTarget = 0;
    public int endTarget = 0;


    public Point origin = new Point(cameraWidth / 2, screenHeight);

    @Override
    public double curveSteepness(double turnAngle) {
        return Math.abs(turnAngle) / (45);
    }

    public int getDegreeOffset() {
        int xOffset = origin.x - steerPoint.x;
        int yOffset = Math.abs(origin.y - steerPoint.y);
        System.out.println("\n\n\n" + myPID() + " " + xOffset + "\n\n\n");

        return (int) Math.round((Math.atan2(-xOffset, yOffset)) * (180 / Math.PI));
    }

    public double myPID() {

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

    public void findPoints(int[] pixels) {

    }
}
