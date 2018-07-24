package steering;

import java.util.ArrayList;
import java.util.List;

public abstract class SteeringBase implements Steerable {
    public Point steerPoint = new Point(0, 0);

    public List<Point> leftPoints = new ArrayList<>();
    public List<Point> rightPoints = new ArrayList<>();
    public List<Point> midPoints = new ArrayList<>();
}
