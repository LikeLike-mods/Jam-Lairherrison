package net.ussm.theshell.client.mask;

import java.util.ArrayList;
import java.util.List;

public final class MaskClipper {
    private MaskClipper() {}

    public record Vertex(double x, double y, double z, double u, double v) {
        Vertex lerp(Vertex other, double t) {
            if (t <= 0) return this;
            if (t >= 1) return other;
            return new Vertex(x + (other.x - x) * t, y + (other.y - y) * t,
                    z + (other.z - z) * t, u + (other.u - u) * t, v + (other.v - v) * t);
        }

        boolean samePosition(Vertex other) {
            return x == other.x && y == other.y && z == other.z;
        }
    }

    private record Plane(double a, double b, double c, double d) {
        double distance(Vertex vertex) {
            return a * vertex.x + b * vertex.y + c * vertex.z + d;
        }
    }

    public static final class Volume {
        private final Plane[] planes;

        private Volume(Plane[] planes) {
            this.planes = planes;
        }

        public static Volume fromAffine(float[] m, double minX, double minY, double minZ,
                                         double maxX, double maxY, double maxZ) {
            if (m.length != 16) throw new IllegalArgumentException("Expected 16 matrix coefficients");
            for (float value : m) if (!Float.isFinite(value)) return null;
            for (double value : new double[] {minX, minY, minZ, maxX, maxY, maxZ}) {
                if (!Double.isFinite(value)) return null;
            }
            if (maxX <= minX || maxY <= minY || maxZ <= minZ ||
                    m[3] != 0 || m[7] != 0 || m[11] != 0 || m[15] != 1) return null;

            double a = m[0], b = m[4], c = m[8];
            double d = m[1], e = m[5], f = m[9];
            double g = m[2], h = m[6], i = m[10];
            double det = a * (e * i - f * h) - b * (d * i - f * g) + c * (d * h - e * g);
            if (det == 0 || !Double.isFinite(det)) return null;
            double[][] rows = {
                    {(e * i - f * h) / det, (c * h - b * i) / det, (b * f - c * e) / det},
                    {(f * g - d * i) / det, (a * i - c * g) / det, (c * d - a * f) / det},
                    {(d * h - e * g) / det, (b * g - a * h) / det, (a * e - b * d) / det}
            };
            double[] low = {minX, minY, minZ}, high = {maxX, maxY, maxZ};
            Plane[] planes = new Plane[6];
            for (int axis = 0; axis < 3; axis++) {
                double[] row = rows[axis];
                double translation = -row[0] * m[12] - row[1] * m[13] - row[2] * m[14];
                if (!Double.isFinite(translation)) return null;
                for (double value : row) if (!Double.isFinite(value)) return null;
                planes[axis * 2] = new Plane(row[0], row[1], row[2], translation - low[axis]);
                planes[axis * 2 + 1] = new Plane(-row[0], -row[1], -row[2], high[axis] - translation);
            }
            return new Volume(planes);
        }

        public boolean contains(Vertex point) {
            for (Plane plane : planes) if (plane.distance(point) < 0) return false;
            return true;
        }
    }

    public static List<List<Vertex>> subtract(List<Vertex> polygon, List<Volume> volumes) {
        if (polygon.size() < 3) return List.of();
        List<List<Vertex>> pieces = List.of(polygon);
        for (Volume volume : volumes) {
            List<List<Vertex>> next = new ArrayList<>();
            for (List<Vertex> piece : pieces) subtractOne(piece, volume, next);
            pieces = next;
            if (pieces.isEmpty()) break;
        }
        return pieces;
    }

    private static void subtractOne(List<Vertex> polygon, Volume volume, List<List<Vertex>> output) {
        boolean allInside = true;
        for (Plane plane : volume.planes) {
            int inside = 0;
            for (Vertex vertex : polygon) if (plane.distance(vertex) >= 0) inside++;
            if (inside == 0) {
                output.add(polygon);
                return;
            }
            if (inside != polygon.size()) allInside = false;
        }
        if (allInside) return;

        List<Vertex> remainder = polygon;
        for (Plane plane : volume.planes) {
            if (remainder.isEmpty()) break;
            List<Vertex> inside = new ArrayList<>(remainder.size() + 1);
            List<Vertex> outside = new ArrayList<>(remainder.size() + 1);
            Vertex previous = remainder.getLast();
            double previousDistance = plane.distance(previous);
            for (Vertex current : remainder) {
                double currentDistance = plane.distance(current);
                boolean previousInside = previousDistance >= 0;
                boolean currentInside = currentDistance >= 0;
                if (previousInside != currentInside) {
                    double t = previousDistance / (previousDistance - currentDistance);
                    Vertex intersection = previous.lerp(current, t);
                    appendUnique(inside, intersection);
                    appendUnique(outside, intersection);
                }
                appendUnique(currentInside ? inside : outside, current);
                previous = current;
                previousDistance = currentDistance;
            }
            closePolygon(inside);
            closePolygon(outside);
            if (hasArea(outside)) output.add(outside);
            remainder = inside;
        }
    }

    private static void appendUnique(List<Vertex> vertices, Vertex vertex) {
        if (vertices.isEmpty() || !vertices.getLast().samePosition(vertex)) vertices.add(vertex);
    }

    private static void closePolygon(List<Vertex> vertices) {
        if (vertices.size() > 1 && vertices.getFirst().samePosition(vertices.getLast())) vertices.removeLast();
    }

    public static boolean hasArea(List<Vertex> polygon) {
        if (polygon.size() < 3) return false;
        Vertex a = polygon.getFirst();
        for (int j = 1; j + 1 < polygon.size(); j++) {
            Vertex b = polygon.get(j), c = polygon.get(j + 1);
            double abX = b.x - a.x, abY = b.y - a.y, abZ = b.z - a.z;
            double acX = c.x - a.x, acY = c.y - a.y, acZ = c.z - a.z;
            double x = abY * acZ - abZ * acY, y = abZ * acX - abX * acZ, z = abX * acY - abY * acX;
            if (x != 0 || y != 0 || z != 0) return true;
        }
        return false;
    }
}
