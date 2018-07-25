package steering;


public class SteeringMk2 extends SteeringBase {



    private Boolean leftSideFound = false;
    private Boolean rightSideFound = false;

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
                double lastY = rightPoints.get(rightPoints.size() - 1).y;
                int lastX = rightPoints.get(rightPoints.size() - 1).x;
                //midPoints.add(new Point((int)Math.round(lastX - ((cameraWidth) * Math.pow((lastY) / (screenHeight), 2))), cameraRow));
            } else if (leftSideFound) {
                double lastY = leftPoints.get(leftPoints.size() - 1).y;
                int lastX = leftPoints.get(leftPoints.size() - 1).x;
                //midPoints.add(new Point((int)Math.round(lastX + ((cameraWidth) * Math.pow((lastY) / (screenHeight), 2))), cameraRow));

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

}
