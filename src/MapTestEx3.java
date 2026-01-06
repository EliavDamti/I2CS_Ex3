package assignments.Ex3;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

class MapTestEx3 {

    private static final int WALL = 1;

    private static Pixel2D p(int x, int y) {
        return new Index2D(x, y);
    }

    private static int[][] wrapBoard3x3() {
        return new int[][]{
                {0, 1, 0},
                {1, 1, 1},
                {0, 1, 0}
        };
    }

    private static void assertNeighborStep(Pixel2D a, Pixel2D b, int w, int h, boolean cyclic) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());

        if (cyclic) {
            dx = Math.min(dx, w - dx);
            dy = Math.min(dy, h - dy);
        }

        assertEquals(1, dx + dy, "Consecutive steps in the path are not neighbors");
    }

    private static void assertValidPath(Map2D m, Pixel2D[] path, int obsColor, boolean cyclic) {
        assertNotNull(path);
        assertTrue(path.length >= 1);

        int w = m.getWidth();
        int h = m.getHeight();

        for (Pixel2D step : path) {
            assertNotNull(step);
            assertTrue(m.isInside(step));
            assertNotEquals(obsColor, m.getPixel(step));
        }

        for (int i = 0; i < path.length - 1; i++) {
            assertNeighborStep(path[i], path[i + 1], w, h, cyclic);
        }
    }

    // Verifies setCyclic and isCyclic.
    @Test
    void testSetCyclicAndIsCyclic() {
        Map2D m = new Map(3, 3, 0);

        m.setCyclic(false);
        assertFalse(m.isCyclic());

        m.setCyclic(true);
        assertTrue(m.isCyclic());
    }

    // Verifies fill returns 0 when old color equals new color.
    @Test
    void testFillSameColorReturnsZero() {
        Map2D m = new Map(3, 3, 7);
        int filled = m.fill(p(1, 1), 7);
        assertEquals(0, filled);
        assertEquals(7, m.getPixel(1, 1));
    }

    // Verifies non-cyclic fill does not wrap across borders.
    @Test
    void testFillNonCyclicNoWrap() {
        Map2D m = new Map(wrapBoard3x3());
        m.setCyclic(false);

        int filled = m.fill(p(0, 0), 9);

        assertEquals(1, filled);
        assertEquals(9, m.getPixel(0, 0));
        assertEquals(0, m.getPixel(2, 0));
        assertEquals(0, m.getPixel(0, 2));
        assertEquals(0, m.getPixel(2, 2));
    }

    // Verifies cyclic fill wraps across borders.
    @Test
    void testFillCyclicWrapsEdges() {
        Map2D m = new Map(wrapBoard3x3());
        m.setCyclic(true);

        int filled = m.fill(p(0, 0), 9);

        assertEquals(4, filled);
        assertEquals(9, m.getPixel(0, 0));
        assertEquals(9, m.getPixel(2, 0));
        assertEquals(9, m.getPixel(0, 2));
        assertEquals(9, m.getPixel(2, 2));
    }

    // Verifies shortestPath returns null in non-cyclic mode when border wrap is needed.
    @Test
    @Timeout(value = 2, unit = SECONDS)
    void testShortestPathNonCyclicNoWrapReturnsNull() {
        Map2D m = new Map(wrapBoard3x3());
        m.setCyclic(false);

        Pixel2D[] path = m.shortestPath(p(0, 0), p(2, 0), WALL);

        assertNull(path);
    }

    // Verifies shortestPath uses border wrapping in cyclic mode.
    @Test
    @Timeout(value = 2, unit = SECONDS)
    void testShortestPathCyclicWrapDirectNeighbor() {
        Map2D m = new Map(wrapBoard3x3());
        m.setCyclic(true);

        Pixel2D[] path = m.shortestPath(p(0, 0), p(2, 0), WALL);

        assertNotNull(path);
        assertEquals(2, path.length);
        assertEquals(p(0, 0), path[0]);
        assertEquals(p(2, 0), path[1]);

        assertValidPath(m, path, WALL, true);
    }

    // Verifies shortestPath avoids obstacles and returns a minimal-length path.
    @Test
    @Timeout(value = 2, unit = SECONDS)
    void testShortestPathAvoidsObstaclesAndIsMinimal() {
        int[][] arr = new int[5][5];
        for (int y = 0; y < 5; y++) {
            arr[2][y] = WALL;
        }
        arr[2][2] = 0;

        Map2D m = new Map(arr);
        m.setCyclic(false);

        Pixel2D start = p(0, 2);
        Pixel2D target = p(4, 2);

        Pixel2D[] path = m.shortestPath(start, target, WALL);

        assertNotNull(path);
        assertEquals(start, path[0]);
        assertEquals(target, path[path.length - 1]);

        assertValidPath(m, path, WALL, false);
        assertEquals(5, path.length);
    }

    // Verifies allDistance returns null when the start point is an obstacle.
    @Test
    @Timeout(value = 2, unit = SECONDS)
    void testAllDistanceStartOnObstacleReturnsNull() {
        int[][] arr = new int[][]{
                {0, 0, 0},
                {0, WALL, 0},
                {0, 0, 0}
        };
        Map2D m = new Map(arr);
        m.setCyclic(false);

        Map2D d = m.allDistance(p(1, 1), WALL);
        assertNull(d);
    }

    // Verifies allDistance in non-cyclic mode with a single obstacle.
    @Test
    @Timeout(value = 2, unit = SECONDS)
    void testAllDistanceNonCyclicBasic() {
        int[][] arr = new int[][]{
                {0, 0, 0},
                {0, WALL, 0},
                {0, 0, 0}
        };
        Map2D m = new Map(arr);
        m.setCyclic(false);

        Map2D d = m.allDistance(p(0, 0), WALL);
        assertNotNull(d);

        assertEquals(0, d.getPixel(0, 0));
        assertEquals(1, d.getPixel(1, 0));
        assertEquals(2, d.getPixel(2, 0));

        assertEquals(1, d.getPixel(0, 1));
        assertEquals(-1, d.getPixel(1, 1));

        assertEquals(2, d.getPixel(0, 2));
        assertEquals(4, d.getPixel(2, 2));
    }

    // Verifies allDistance in cyclic mode prefers wrapped shorter routes.
    @Test
    @Timeout(value = 2, unit = SECONDS)
    void testAllDistanceCyclicWrapShorter() {
        Map2D m = new Map(wrapBoard3x3());
        m.setCyclic(true);

        Map2D d = m.allDistance(p(0, 0), WALL);
        assertNotNull(d);

        assertEquals(0, d.getPixel(0, 0));
        assertEquals(1, d.getPixel(2, 0));
        assertEquals(1, d.getPixel(0, 2));
        assertEquals(2, d.getPixel(2, 2));

        assertEquals(-1, d.getPixel(1, 0));
        assertEquals(-1, d.getPixel(1, 1));
        assertEquals(-1, d.getPixel(1, 2));
    }
}
