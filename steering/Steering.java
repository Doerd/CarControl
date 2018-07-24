package steering;

import apw3.DriverCons;
import apw3.TrakSim;

import java.util.ArrayList;
import java.util.List;

public class Steering {

	
	public int startingPoint = 0;
	
	//767 is white

    private long averageLuminance = 0;

	public int heightOfArea = 32;
	public int startingHeight = 272;

	private int screenWidth = 912;
	private int cameraWidth = 640;
	private int screenHeight = DriverCons.D_ImHi;
	public Point steerPoint = new Point(0, 0);

	public List<Point> leadingMidPoints = new ArrayList<Point>();
	public List<Point> leftPoints = new ArrayList<Point>();
	public List<Point> rightPoints = new ArrayList<Point>();
	
	public List<Point> midPoints = new ArrayList<Point>();
	Point origin = new Point(cameraWidth/2, screenHeight);
	
	Boolean found = false;
	Boolean leftSideFound = false;
	Boolean rightSideFound = false;
	
	private TrakSim theSim;
	
	private double integral, derivative, previousError;
	private double kP = 0.65;
	private double kI = 1;
	private double kD = 1;
	private boolean usePID = true;

	public Steering(TrakSim theSim) {
		for (int i = 0; i<leadingMidPoints.size(); i++) {
			leadingMidPoints.add(new Point(0, 0));
		}
		this.theSim = theSim;
	}
	
	public List<Point> findPoints(int[] pixels) {
	    int count = 0;
        clearArrays();

        averageLuminance = 0;
        for (int pixel = 0; pixel < pixels.length; pixel++) {
            if (pixel % screenWidth < cameraWidth) {
                averageLuminance += pixels[pixel];
            }
            count++;
        }
        averageLuminance = (long)(averageLuminance / count * 1.5);

	    int midX = cameraWidth / 2;
	    for (int cameraRow = screenHeight - 20; cameraRow > screenHeight / 2; cameraRow--) {
	        for (int cameraColumn = midX; cameraColumn < cameraWidth; cameraColumn++) {
	            if (!rightSideFound && pixels[(screenWidth * (cameraRow - 1)) + cameraColumn] >= averageLuminance) {
	                rightSideFound = true;
	                rightPoints.add(new Point(cameraColumn, cameraRow));
                }
                if (!leftSideFound && pixels[(screenWidth * (cameraRow)) - cameraColumn] >= averageLuminance) {
	                leftSideFound = true;
                    leftPoints.add(new Point(cameraColumn, cameraRow));
                }
            }
            if (rightSideFound && leftSideFound) {
	            midX = (rightPoints.get(rightPoints.size() - 1).x + leftPoints.get(leftPoints.size() - 1).x) / 2;
                midPoints.add(new Point(midX, cameraRow));
            } else if (rightSideFound || leftSideFound) {
	            //FINISH LATER
            }

            rightSideFound = false;
	        leftSideFound = false;
        }

        /*
		int roadMiddle = cameraWidth;
		int leftSideTemp = 0;
		int rightSideTemp = 0;
		startingPoint = 0;
		
		//first, find where road starts on both sides
		leftSideFound = false;
		rightSideFound = false;
		for (int row = screenHeight - 22; row>startingHeight + heightOfArea; row--) {
			
			for (int column = roadMiddle/2; column>=0; column--) {
				if (pixels[(screenWidth * (row)) + column] == 16777215) {
					leftSideFound = true;
					break;
				}
			}
			for (int column = roadMiddle/2; column<cameraWidth; column++) {
				if (pixels[(screenWidth * (row)) + column] == 16777215) {
					rightSideFound = true;
					break;
				}
			}

			if (leftSideFound && rightSideFound) {
				startingPoint = row;
				leftSideFound = false;
				rightSideFound = false;
				break;
			}
			leftSideFound = false;
			rightSideFound = false;
		}
		
		//Next, calculate the roadpoint 
		
		int count = 0;
		
		for (int i = startingPoint; i > startingHeight + heightOfArea; i--) {
			for (int j = roadMiddle/2; j>=0; j--) {
				if (pixels[screenWidth * i + j] == 16777215) {
					leftSideTemp = j;
					break;
				}
			}
			for (int j = roadMiddle/2; j<cameraWidth; j++) {
				if (pixels[screenWidth * i + j] == 16777215) {
					rightSideTemp = j;
					break;
				}
			}
			
			leadingMidPoints.get(count).x = roadMiddle / 2;
			leadingMidPoints.get(count).y = i;
			count++;
			roadMiddle = leftSideTemp + rightSideTemp;
		}

		count = 0;
		for (int i = startingHeight + heightOfArea; i>startingHeight; i--) {
			//center to left
			found = false;
			leftPoints.get(count).y = i;
			
			for (int j = roadMiddle/2; j>=0; j--) {
				if (pixels[screenWidth * i + j] == 16777215) {
					leftPoints.get(count).x = j;
					found = true;
					break;
				}
				
			}
			if (!found) {
				leftPoints.get(count).x = 0;
			}
			
			
			//center to right
			found = false;
			rightPoints.get(count).y = leftPoints.get(count).y;
			for (int j = roadMiddle/2; j<cameraWidth; j++) {
				if (pixels[screenWidth * i + j] == 16777215) {
					rightPoints.get(count).x = j;
					found = true;
					break;
				}
			}
			if (!found) {
				rightPoints.get(count).x = cameraWidth;
			}
			
			midPoints.get(count).x = roadMiddle/2;
			midPoints.get(count).y = (leftPoints.get(count).y);
			roadMiddle = (leftPoints.get(count).x + rightPoints.get(count).x);
			count++;
		}
		*/
		return midPoints;
		
	}
	
	public double curveSteepness(double turnAngle) {
		return Math.abs(turnAngle)/(45);
	}


	/*
	find the average point from the midpoints array
	 */
	public void averageMidpoints() {
        double tempY = 0;
        double tempX = 0;
        int tempCount = 0;

        // Sum the x's and the y's
	    for (int i = 0; i<startingPoint - (startingHeight + heightOfArea); i++) {
	    		Point point = new Point (leadingMidPoints.get(i).x, leadingMidPoints.get(i).y);
            tempX += point.x;
            tempY += point.y;
            tempCount++;
        }
	    if (tempCount == 0) {
		    for (int i = 0; i<midPoints.size(); i++) {
                Point point = new Point (midPoints.get(i).x, midPoints.get(i).y);
                tempX += point.x;
                tempY += point.y;
                tempCount++;
		    }
	    }

        steerPoint.x = (int)(tempX / tempCount);
	    steerPoint.y = (int)(tempY / tempCount);

    }


    public int getDegreeOffset() {
	    int xOffset = origin.x - steerPoint.x;
	    int yOffset = Math.abs(origin.y - steerPoint.y);
	    System.out.println("\n\n\n" + myPID() + " " + xOffset + "\n\n\n");

	    return (int)Math.round((Math.atan2(-xOffset, yOffset)) * (180 / Math.PI));
    }

    private void clearArrays() {
	    leftPoints.clear();
	    rightPoints.clear();
	    midPoints.clear();
    }
    
    public double myPID() {
 
    		int error = origin.x - steerPoint.x;
    		
    		integral += error * DriverCons.D_FrameTime;
    		if (error == 0 || (Math.abs(error-previousError)==(Math.abs(error)+Math.abs(previousError)))) {
    			integral = 0;
    		}
    		if (Math.abs(integral) > 100) {
    			integral = 0;
    		}

    		derivative = (error - previousError)/DriverCons.D_FrameTime;
    		previousError = error;
    		return error*kP + integral*kI + derivative*kD;
 
    }
}

