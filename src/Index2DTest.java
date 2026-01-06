// Index2DTest.java
package assignments.Ex3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Index2DTest {

    // Test default constructor values.
    @Test
    void testDefaultCtor() {
        Pixel2D p = new Index2D();
        assertEquals(0, p.getX());
        assertEquals(0, p.getY());
    }

    // Test (x,y) constructor values.
    @Test
    void testXYCtor() {
        Pixel2D p = new Index2D(7, 9);
        assertEquals(7, p.getX());
        assertEquals(9, p.getY());
    }

    // Test copy constructor values.
    @Test
    void testCopyCtor() {
        Pixel2D a = new Index2D(3, 4);
        Pixel2D b = new Index2D(a);
        assertEquals(3, b.getX());
        assertEquals(4, b.getY());
        assertNotSame(a, b);
    }

    // Test distance2D on identical points.
    @Test
    void testDistanceZero() {
        Pixel2D a = new Index2D(5, 6);
        Pixel2D b = new Index2D(5, 6);
        assertEquals(0.0, a.distance2D(b), 1e-9);
    }

    // Test distance2D basic 3-4-5 triangle.
    @Test
    void testDistance345() {
        Pixel2D a = new Index2D(0, 0);
        Pixel2D b = new Index2D(3, 4);
        assertEquals(5.0, a.distance2D(b), 1e-9);
    }

    // Test distance2D symmetry.
    @Test
    void testDistanceSymmetry() {
        Pixel2D a = new Index2D(2, 10);
        Pixel2D b = new Index2D(7, 1);
        assertEquals(a.distance2D(b), b.distance2D(a), 1e-9);
    }

    // Test equals true for same coordinates.
    @Test
    void testEqualsTrue() {
        Pixel2D a = new Index2D(8, 8);
        Pixel2D b = new Index2D(8, 8);
        assertEquals(a, b);
    }

    // Test equals false for different coordinates.
    @Test
    void testEqualsFalse() {
        Pixel2D a = new Index2D(8, 8);
        Pixel2D b = new Index2D(8, 9);
        assertNotEquals(a, b);
    }

    // Test toString format.
    @Test
    void testToString() {
        Pixel2D a = new Index2D(11, 22);
        assertEquals("11,22", a.toString());
    }

    // Test distance2D throws on null.
    @Test
    void testDistanceNullThrows() {
        Pixel2D a = new Index2D(1, 1);
        assertThrows(RuntimeException.class, () -> a.distance2D(null));
    }
}
