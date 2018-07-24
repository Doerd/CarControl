package steering;

import apw3.DriverCons;
import apw3.TrakSim;
import java.util.List;
import java.util.ArrayList;

public class SteeringMk1 extends SteeringBase{


	public int startingPoint = 0;


	//767 is white

	public int heightOfArea = 32;
	public int startingHeight = 272;

	private int screenWidth = 912;
	private int cameraWidth = 640;
	private int screenHeight = DriverCons.D_ImHi;

	public Point[] leadingMidPoints = new Point[startingHeight + heightOfArea];
	public Point[] pointsAhead = new Point[startingHeight-(screenHeight/2)];
	Point origin = new Point(cameraWidth/2, screenHeight);

	Boolean found = false;
	Boolean leftSideFound = false;
	Boolean rightSideFound = false;

	private double integral, derivative, previousError;
	private double kP = 0.85;
	private double kI = 1;
	private double kD = 1;
	private double weight = 1.0; // >1 = right lane, <1 = left lane
	private int tempDeg = 0;
	private boolean weightLane = false;
	private boolean usePID = true;
	private boolean turnAhead = false;
	private boolean turnRightAhead = false;

	private long averageLuminance = 0;


	public SteeringMk1() {
		for (int i = 0; i<heightOfArea; i++) {
			leftPoints.add(new Point(0, 0));
			rightPoints.add(new Point(0, 0));
			midPoints.add(new Point(0, 0));
		}
		for (int i = 0; i<leadingMidPoints.length; i++) {
			leadingMidPoints[i] = new Point(0, 0);
		}
	}

	@Override
	public int drive(int pixels[]) {
		findPoints(pixels);
		averageMidpoints();
		return getDegreeOffset();
	}

	public void findPoints(int[] pixels) {
		int roadMiddle = cameraWidth;
		int leftSideTemp = 0;
		int rightSideTemp = 0;
		startingPoint = 0;
		averageLuminance = 0;
		Boolean first = true;
		int count = 0;
		//first before first, find average luminance
		for (int i = cameraWidth * screenHeight - 1; i > startingHeight * cameraWidth; i--) {
			averageLuminance = averageLuminance + pixels[i];
			count++;
		}
		averageLuminance = (long) (averageLuminance/count * 1.5);
		System.out.println("average luminance " + averageLuminance);
		count = 0;

		//first, find where road starts on both sides
		leftSideFound = false;
		rightSideFound = false;
		for (int i = screenHeight - 22; i>startingHeight + heightOfArea; i--) {
			for (int j = roadMiddle/2; j>=0; j--) {
				if (pixels[(screenWidth * (i)) + j] >= averageLuminance) {
					leftSideFound = true;
					break;
				}
			}
			for (int j = roadMiddle/2; j<cameraWidth; j++) {
				if (pixels[(screenWidth * (i)) + j] >= averageLuminance) {
					rightSideFound = true;
					break;
				}
			}

			if (leftSideFound && rightSideFound) {
				startingPoint = i;
				leftSideFound = false;
				rightSideFound = false;
				break;
			}

			leftSideFound = false;
			rightSideFound = false;
		}

		//Next, calculate the roadpoint

		count = 0;

		for (int i = startingPoint; i > startingHeight + heightOfArea; i--) {
			for (int j = roadMiddle/2; j>=0; j--) {
				if (pixels[screenWidth * i + j]  >= averageLuminance) {
					leftSideTemp = j;
					break;
				}
			}
			for (int j = roadMiddle/2; j<cameraWidth; j++) {
				if (pixels[screenWidth * i + j]  >= averageLuminance) {
					rightSideTemp = j;
					break;
				}
			}

			if(weightLane && midPoints != null){
				averageMidpoints();
				checkPointsAhead(pixels);
				if(turnAhead){
					if(turnRightAhead) weight = 1.25;
					else weight = 0.85;
				}
				else weight = 1;
			}

			leadingMidPoints[count].x = weightLane?( (int) ((double) roadMiddle / 2.0 * weight)):roadMiddle/2;
			leadingMidPoints[count].y = i;
			count++;
			roadMiddle = leftSideTemp + rightSideTemp;
		}
		int tempCount = count;
		count = 0;
		for (int i = startingHeight + heightOfArea; i>startingHeight; i--) {
			//center to left
			found = false;
			leftPoints.get(count).y = i;

			for (int j = roadMiddle/2; j>=0; j--) {
				if (pixels[screenWidth * i + j] >= averageLuminance) {
					leftPoints.get(count).x = j;
					found = true;
					break;
				}

			}
			if (found == false) {
				leftPoints.get(count).x = 0;
			}


			//center to right
			found = false;
			rightPoints.get(count).y = leftPoints.get(count).y;
			for (int j = roadMiddle/2; j<cameraWidth; j++) {
				if (pixels[screenWidth * i + j] >= averageLuminance) {
					rightPoints.get(count).x = j;
					found = true;
					break;
				}

			}
			if (found == false) {
				rightPoints.get(count).x = cameraWidth;
			}

			midPoints.get(count).x = roadMiddle/2;
			midPoints.get(count).y = (leftPoints.get(count).y);
			roadMiddle = (leftPoints.get(count).x + rightPoints.get(count).x);
			count++;
		}
	}
	
	Point avgPointAhead(int[] pixels){ // Look ahead in the road to see if there is a turn (for following the racing curve)
		int roadMiddle = cameraWidth;
		int leftSideTemp = 0;
		int rightSideTemp = 0;
		boolean foundTurn = false;
		startingPoint = 0;

		//Next, calculate the roadpoint

		int count = 0;

		for (int i = startingHeight; i > screenHeight/2; i--) {
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
			pointsAhead[count].x = roadMiddle/2;
			pointsAhead[count].y = i;
			count++;
			roadMiddle = leftSideTemp + rightSideTemp;
		}

		double tempY = 0;
		double tempX = 0;
		int avgX = 0;
		int avgY = 0;
		int tempCount = 0;

		for (int i = 0; i<pointsAhead.length; i++) {
			Point point = new Point (leadingMidPoints[i].x, leadingMidPoints[i].y);
			tempX += point.x;
			tempY += point.y;
			tempCount++;
		}

		avgX = (int)(tempX / tempCount);
		avgY = (int)(tempY / tempCount);

		Point avgPoint = new Point(avgX,avgY);

		return avgPoint;
	}

	void checkPointsAhead(int[] pixels){
		Point ahead = avgPointAhead(pixels);
		if(Math.abs(ahead.x-origin.x) >= Math.abs((steerPoint.x-origin.x)/(steerPoint.y-origin.y)*(ahead.y-origin.y))){
			turnAhead = true;
			if(ahead.x-origin.x > 0) turnRightAhead = true;
			else turnRightAhead = false;
		}
		else turnAhead = false;
	}

	List<Double> posLog = new ArrayList<Double>();

	public void updatePosLog(double x, double y, double heading){ // Reference positions by doing point# * 3 + (0 for x, 1 for y, 2 for heading)
		posLog.add(x);
		posLog.add(y);
		posLog.add(heading);
	}

	public void drawMapArrays(){
		int length = posLog.size()/3;
		double laneWidth = 4; // Needs to be measured
		Point[] leftEdge = new Point[length];
		Point[] rightEdge = new Point[length];
		for(int i = 0; i <= length; i++){
			leftEdge[i].x = (int)(posLog.get(i*3) + laneWidth/2*Math.cos(posLog.get(i*3+2)+(Math.PI/2)));
			leftEdge[i].y = (int)(posLog.get(i*3+1) + laneWidth/2*Math.sin(posLog.get(i*3+2)+(Math.PI/2)));
			rightEdge[i].x = (int)(posLog.get(i*3) + laneWidth/2*Math.cos(posLog.get(i*3+2)-(Math.PI/2)));
			rightEdge[i].y = (int)(posLog.get(i*3+1) + laneWidth/2*Math.sin(posLog.get(i*3+2)-(Math.PI/2)));
		}
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
		int weightCount = 0;

		boolean shouldWeight = false;
		double weightFactor = 1;

		// Sum the x's and the y's
		for (int i = 0; i<startingPoint - (startingHeight + heightOfArea); i++) {
			Point point = new Point (leadingMidPoints[i].x, leadingMidPoints[i].y);
			if(i> (startingPoint-(startingHeight+heightOfArea))/2) shouldWeight = true;
			tempX += shouldWeight?weightFactor*point.x:point.x;
			tempY += point.y;
			tempCount++;
			weightCount += shouldWeight?weightFactor-1:0;
		}
		shouldWeight = false;
		if (tempCount == 0) {
			for (int i = 0; i<midPoints.size(); i++) {
				Point point = new Point (midPoints.get(i).x, midPoints.get(i).y);
				if(i>midPoints.size()/2) shouldWeight = true;
				tempX += shouldWeight?weightFactor*point.x:point.x;
				tempY += point.y;
				tempCount++;
				weightCount += shouldWeight?weightFactor-1:0;
			}
		}

		steerPoint.x = (int)(tempX / tempCount+weightCount);
		steerPoint.y = (int)(tempY / tempCount);

	}


	public int getDegreeOffset() {
		int xOffset = origin.x - steerPoint.x;
		int yOffset = Math.abs(origin.y - steerPoint.y);

		tempDeg = (int)((Math.atan2(-xOffset, yOffset)) * (180 / Math.PI));

		System.out.println("\n\n\n" + tempDeg + " " + myPID() + "\n\n\n");
		return (int)((Math.atan2(-(((usePID) ? (curveSteepness(tempDeg)>0.3) : false) ? myPID() : xOffset), yOffset)) * (180 / Math.PI));
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
