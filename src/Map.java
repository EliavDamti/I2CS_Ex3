package assignments.Ex3;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A class representing a 2D map (matrix) of integers.
 * It implements Map2D and supports drawing, filling, pathfinding, and cyclic behavior.
 */
public class Map implements Map2D, Serializable {
    private int[][] _map;
    private int _width;
    private int _height;
    // Default cyclic mode is taken from GameInfo.CYCLIC_MODE
    private boolean _cyclic = GameInfo.CYCLIC_MODE;

    /**
     * Constructs a map of size w*h, filled with value v.
     * @param w width
     * @param h height
     * @param v initial value
     */
    public Map(int w, int h, int v) {
        init(w, h, v);
    }

    /**
     * Constructs a square map of size*size, filled with 0.
     * @param size width and height
     */
    public Map(int size) {
        this(size, size, 0);
    }

    /**
     * Constructs a map from a given 2D array.
     * @param data 2D array of integers
     */
    public Map(int[][] data) {
        init(data);
    }

    /**
     * Initializes the map with dimensions w*h and fills it with v.
     * @param w width
     * @param h height
     * @param v initial value
     */
    @Override
    public void init(int w, int h, int v) {
        _width = w;
        _height = h;
        _map = new int[_width][_height];
        for (int x = 0; x < _width; x++) {
            for (int y = 0; y < _height; y++) {
                _map[x][y] = v;
            }
        }
    }

    /**
     * Initializes the map from a 2D array.
     * Validates that the array is not null, empty, or ragged.
     * @param arr 2D array source
     */
    @Override
    public void init(int[][] arr) {
        if (arr == null || arr.length == 0) {
            throw new RuntimeException("null or empty array");
        }
        int h = arr[0].length;
        if (h == 0) {
            throw new RuntimeException("empty rows");
        }
        for (int x = 0; x < arr.length; x++) {
            if (arr[x] == null || arr[x].length != h) {
                throw new RuntimeException("ragged array");
            }
        }
        _width = arr.length;
        _height = h;
        _map = new int[_width][_height];
        for (int x = 0; x < _width; x++) {
            for (int y = 0; y < _height; y++) {
                _map[x][y] = arr[x][y];
            }
        }
    }

    /**
     * Returns a deep copy of the map as a 2D array.
     * @return 2D integer array
     */
    @Override
    public int[][] getMap() {
        int[][] ans = new int[_width][_height];
        for (int x = 0; x < _width; x++) {
            for (int y = 0; y < _height; y++) {
                ans[x][y] = _map[x][y];
            }
        }
        return ans;
    }

    /**
     * Returns the width of the map.
     */
    @Override
    public int getWidth() {
        return _width;
    }

    /**
     * Returns the height of the map.
     */
    @Override
    public int getHeight() {
        return _height;
    }

    /**
     * Returns the value at (x, y). Returns -1 if out of bounds.
     * @param x x-coordinate
     * @param y y-coordinate
     * @return pixel value
     */
    @Override
    public int getPixel(int x, int y) {
        if (x < 0 || x >= _width || y < 0 || y >= _height) {
            return -1;
        }
        return _map[x][y];
    }

    /**
     * Returns the value at point p. Returns -1 if p is null or out of bounds.
     * @param p pixel coordinate
     * @return pixel value
     */
    @Override
    public int getPixel(Pixel2D p) {
        if (p == null) {
            return -1;
        }
        return getPixel(p.getX(), p.getY());
    }

    /**
     * Sets the value at (x, y) to v. Does nothing if out of bounds.
     * @param x x-coordinate
     * @param y y-coordinate
     * @param v new value
     */
    @Override
    public void setPixel(int x, int y, int v) {
        if (x < 0 || x >= _width || y < 0 || y >= _height) {
            return;
        }
        _map[x][y] = v;
    }

    /**
     * Sets the value at point p to v. Does nothing if p is null or out of bounds.
     * @param p pixel coordinate
     * @param v new value
     */
    @Override
    public void setPixel(Pixel2D p, int v) {
        if (p == null) {
            return;
        }
        setPixel(p.getX(), p.getY(), v);
    }

    /**
     * Checks if point p is inside the map boundaries.
     * @param p pixel coordinate
     * @return true if inside, false otherwise
     */
    @Override
    public boolean isInside(Pixel2D p) {
        if (p == null) {
            return false;
        }
        int x = p.getX();
        int y = p.getY();
        return x >= 0 && x < _width && y >= 0 && y < _height;
    }

    /**
     * Checks if another map has the same dimensions as this one.
     * @param p another Map2D
     * @return true if dimensions match
     */
    public boolean sameDimensions(Map2D p) {
        if (p == null) {
            return false;
        }
        return this.getWidth() == p.getWidth() && this.getHeight() == p.getHeight();
    }

    /**
     * Adds the values of another map to this map, pixel by pixel.
     * The other map must have the same dimensions.
     * @param p another Map2D
     */
    public void addMap2D(Map2D p) {
        if (p == null || !sameDimensions(p)) {
            return;
        }
        int[][] pm = p.getMap();
        for (int x = 0; x < _width; x++) {
            for (int y = 0; y < _height; y++) {
                _map[x][y] = _map[x][y] + pm[x][y];
            }
        }
    }

    /**
     * Multiplies every value in the map by a scalar.
     * @param scalar multiplication factor
     */
    public void mul(double scalar) {
        for (int x = 0; x < _width; x++) {
            for (int y = 0; y < _height; y++) {
                _map[x][y] = (int) (_map[x][y] * scalar);
            }
        }
    }

    /**
     * Rescales the map by factors sx and sy. Uses nearest neighbor.
     * @param sx width scale factor
     * @param sy height scale factor
     */
    public void rescale(double sx, double sy) {
        if (sx <= 0 || sy <= 0) {
            return;
        }
        int newW = (int) Math.round(_width * sx);
        int newH = (int) Math.round(_height * sy);
        if (newW <= 0 || newH <= 0) {
            return;
        }
        int[][] newMap = new int[newW][newH];

        for (int x = 0; x < newW; x++) {
            for (int y = 0; y < newH; y++) {
                double ox = x / sx;
                double oy = y / sy;
                int ix = (int) Math.floor(ox);
                int iy = (int) Math.floor(oy);
                if (ix < 0) ix = 0;
                if (iy < 0) iy = 0;
                if (ix >= _width) ix = _width - 1;
                if (iy >= _height) iy = _height - 1;
                newMap[x][y] = _map[ix][iy];
            }
        }

        _width = newW;
        _height = newH;
        _map = newMap;
    }

    /**
     * Draws a filled circle on the map.
     * @param center center point
     * @param rad radius
     * @param newColor color value to fill
     */
    public void drawCircle(Pixel2D center, double rad, int newColor) {
        if (center == null || rad < 0) {
            return;
        }
        int cx = center.getX();
        int cy = center.getY();
        int r = (int) Math.ceil(rad);

        for (int x = cx - r; x <= cx + r; x++) {
            for (int y = cy - r; y <= cy + r; y++) {
                if (x < 0 || x >= _width || y < 0 || y >= _height) {
                    continue;
                }
                double dx = x - cx;
                double dy = y - cy;
                if (Math.sqrt(dx * dx + dy * dy) <= rad) {
                    _map[x][y] = newColor;
                }
            }
        }
    }

    /**
     * Draws a line segment between p1 and p2.
     * @param p1 start point
     * @param p2 end point
     * @param newColor color value to draw
     */
    public void drawLine(Pixel2D p1, Pixel2D p2, int newColor) {
        if (p1 == null || p2 == null) {
            return;
        }
        int x1 = p1.getX();
        int y1 = p1.getY();
        int x2 = p2.getX();
        int y2 = p2.getY();

        double dx = x2 - x1;
        double dy = y2 - y1;
        int steps = (int) Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) {
            setPixel(x1, y1, newColor);
            return;
        }

        double xInc = dx / steps;
        double yInc = dy / steps;
        double x = x1;
        double y = y1;

        for (int i = 0; i <= steps; i++) {
            int xi = (int) Math.round(x);
            int yi = (int) Math.round(y);
            setPixel(xi, yi, newColor);
            x += xInc;
            y += yInc;
        }
    }

    /**
     * Draws a filled rectangle defined by two opposite corners.
     * @param p1 first corner
     * @param p2 opposite corner
     * @param newColor color value to fill
     */
    public void drawRect(Pixel2D p1, Pixel2D p2, int newColor) {
        if (p1 == null || p2 == null) {
            return;
        }
        int minX = Math.min(p1.getX(), p2.getX());
        int maxX = Math.max(p1.getX(), p2.getX());
        int minY = Math.min(p1.getY(), p2.getY());
        int maxY = Math.max(p1.getY(), p2.getY());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                setPixel(x, y, newColor);
            }
        }
    }

    /**
     * Checks if two maps are equal (same dimensions and same content).
     */
    @Override
    public boolean equals(Object ob) {
        if (this == ob) {
            return true;
        }
        if (!(ob instanceof Map2D)) {
            return false;
        }
        Map2D other = (Map2D) ob;
        if (!sameDimensions(other)) {
            return false;
        }
        int[][] om = other.getMap();
        for (int x = 0; x < _width; x++) {
            for (int y = 0; y < _height; y++) {
                if (_map[x][y] != om[x][y]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Performs a flood fill (BFS) starting from xy with new_v.
     * Uses the 'cyclic' parameter to determine if wrapping is allowed.
     *
     * @param xy start point
     * @param new_v new value to fill
     * @param cyclic allow wrapping around edges
     * @return number of pixels filled
     */
    public int fill(Pixel2D xy, int new_v, boolean cyclic) {
        if (xy == null) {
            return 0;
        }
        int x0 = xy.getX();
        int y0 = xy.getY();
        if (x0 < 0 || x0 >= _width || y0 < 0 || y0 >= _height) {
            return 0;
        }
        int old_v = _map[x0][y0];
        if (old_v == new_v) {
            return 0;
        }

        boolean[][] visited = new boolean[_width][_height];
        Queue<Index2D> q = new LinkedList<>();
        q.add(new Index2D(x0, y0));
        visited[x0][y0] = true;
        _map[x0][y0] = new_v;
        int count = 1;

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        while (!q.isEmpty()) {
            Index2D p = q.remove();
            int cx = p.getX();
            int cy = p.getY();

            for (int k = 0; k < 4; k++) {
                int nx = cx + dx[k];
                int ny = cy + dy[k];

                if (cyclic) {
                    if (nx < 0) nx = _width - 1;
                    if (nx >= _width) nx = 0;
                    if (ny < 0) ny = _height - 1;
                    if (ny >= _height) ny = 0;
                } else {
                    if (nx < 0 || nx >= _width || ny < 0 || ny >= _height) {
                        continue;
                    }
                }

                if (!visited[nx][ny] && _map[nx][ny] == old_v) {
                    visited[nx][ny] = true;
                    _map[nx][ny] = new_v;
                    q.add(new Index2D(nx, ny));
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Computes the shortest path from p1 to p2 using BFS.
     * Avoids obstacles of color 'obsColor'.
     * Uses 'cyclic' parameter to determine if wrapping is allowed.
     *

     [Image of BFS shortest path]

     * @param p1 start point
     * @param p2 end point
     * @param obsColor obstacle color
     * @param cyclic allow wrapping
     * @return array of points representing the path, or null if no path
     */
    public Pixel2D[] shortestPath(Pixel2D p1, Pixel2D p2, int obsColor, boolean cyclic) {
        if (p1 == null || p2 == null) {
            return null;
        }
        if (!isInside(p1) || !isInside(p2)) {
            return null;
        }

        int sx = p1.getX();
        int sy = p1.getY();
        int tx = p2.getX();
        int ty = p2.getY();

        if (_map[sx][sy] == obsColor || _map[tx][ty] == obsColor) {
            return null;
        }

        boolean[][] visited = new boolean[_width][_height];
        Index2D[][] parent = new Index2D[_width][_height];
        Queue<Index2D> q = new LinkedList<>();

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        visited[sx][sy] = true;
        q.add(new Index2D(sx, sy));

        while (!q.isEmpty()) {
            Index2D cur = q.remove();
            int cx = cur.getX();
            int cy = cur.getY();

            if (cx == tx && cy == ty) {
                break;
            }

            for (int k = 0; k < 4; k++) {
                int nx = cx + dx[k];
                int ny = cy + dy[k];

                if (cyclic) {
                    if (nx < 0) nx = _width - 1;
                    if (nx >= _width) nx = 0;
                    if (ny < 0) ny = _height - 1;
                    if (ny >= _height) ny = 0;
                } else {
                    if (nx < 0 || nx >= _width || ny < 0 || ny >= _height) {
                        continue;
                    }
                }

                if (_map[nx][ny] == obsColor) {
                    continue;
                }
                if (!visited[nx][ny]) {
                    visited[nx][ny] = true;
                    parent[nx][ny] = cur;
                    q.add(new Index2D(nx, ny));
                }
            }
        }

        if (!visited[tx][ty]) {
            return null;
        }

        java.util.ArrayList<Pixel2D> path = new java.util.ArrayList<>();
        Index2D step = new Index2D(tx, ty);
        while (step != null) {
            path.add(step);
            step = parent[step.getX()][step.getY()];
        }
        java.util.Collections.reverse(path);
        return path.toArray(new Pixel2D[0]);
    }

    /**
     * Computes the distance from start to all reachable points (BFS).
     * Returns a new Map where each pixel holds its distance from start.
     * Unreachable or obstacle pixels are marked with -1.
     *
     * @param start start point
     * @param obsColor obstacle color
     * @param cyclic allow wrapping
     * @return Map containing distances
     */
    public Map2D allDistance(Pixel2D start, int obsColor, boolean cyclic) {
        if (start == null) {
            return null;
        }
        if (!isInside(start)) {
            return null;
        }

        int sx = start.getX();
        int sy = start.getY();
        if (_map[sx][sy] == obsColor) {
            return null;
        }

        int[][] dist = new int[_width][_height];
        for (int x = 0; x < _width; x++) {
            for (int y = 0; y < _height; y++) {
                dist[x][y] = -1;
            }
        }

        Queue<Index2D> q = new LinkedList<>();
        dist[sx][sy] = 0;
        q.add(new Index2D(sx, sy));

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        while (!q.isEmpty()) {
            Index2D cur = q.remove();
            int cx = cur.getX();
            int cy = cur.getY();
            int cd = dist[cx][cy];

            for (int k = 0; k < 4; k++) {
                int nx = cx + dx[k];
                int ny = cy + dy[k];

                if (cyclic) {
                    if (nx < 0) nx = _width - 1;
                    if (nx >= _width) nx = 0;
                    if (ny < 0) ny = _height - 1;
                    if (ny >= _height) ny = 0;
                } else {
                    if (nx < 0 || nx >= _width || ny < 0 || ny >= _height) {
                        continue;
                    }
                }

                if (_map[nx][ny] == obsColor) {
                    continue;
                }
                if (dist[nx][ny] == -1) {
                    dist[nx][ny] = cd + 1;
                    q.add(new Index2D(nx, ny));
                }
            }
        }

        return new Map(dist);
    }

    /**
     * Fills the map starting from p with new_v. Uses the map's cyclic setting.
     * @param p start point
     * @param new_v new value
     * @return number of pixels filled
     */
    @Override
    public int fill(Pixel2D p, int new_v) {
        return fill(p, new_v, _cyclic);
    }

    /**
     * Computes shortest path from p1 to p2. Uses the map's cyclic setting.
     * @param p1 start point
     * @param p2 end point
     * @param obsColor obstacle color
     * @return path array
     */
    @Override
    public Pixel2D[] shortestPath(Pixel2D p1, Pixel2D p2, int obsColor) {
        return shortestPath(p1, p2, obsColor, _cyclic);
    }

    /**
     * Computes distances from start to all pixels. Uses the map's cyclic setting.
     * @param start start point
     * @param obsColor obstacle color
     * @return Distance map
     */
    @Override
    public Map2D allDistance(Pixel2D start, int obsColor) {
        return allDistance(start, obsColor, _cyclic);
    }

    /**
     * Sets the cyclic flag for this map.
     * @param cyclic true to enable wrapping, false to disable
     */
    @Override
    public void setCyclic(boolean cyclic) {
        _cyclic = cyclic;
    }

    /**
     * Checks if the map is cyclic.
     * @return true if cyclic
     */
    @Override
    public boolean isCyclic() {
        return _cyclic;
    }
}