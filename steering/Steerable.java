package steering;

public interface Steerable {
    double curveSteepness(double turnAngle);

    int drive(int pixels[]);
}
