package assignments.Ex3;

import exe.ex3.game.Game;
import exe.ex3.game.PacManAlgo;
import exe.ex3.game.PacmanGame;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Ex3Algo implements PacManAlgo {

    private static final int WALL = 1;
    private static final int FOOD = 3;
    private static final int POWER = 5;

    private static final int PANIC_DIST = 4;
    private static final int MIN_SAFE_AREA = 80;
    private static final int CHECK_DEPTH = 100;

    private int lastX = -1, lastY = -1;
    private int stuckCounter = 0;
    private Random rand = new Random();

    @Override
    public String getInfo() {
        return "Ex3Algo: Cyclic Survivor";
    }

    /**
     * Main game loop. Decides whether to escape, eat, or roam based on danger level.
     * Handles cyclic board dimensions.
     */
    @Override
    public int move(PacmanGame game) {
        int[][] board = game.getGame(0);
        String pos = game.getPos(0);

        if (board == null || pos == null) return Game.STAY;

        int[] p = pos(pos);
        int pX = p[0];
        int pY = p[1];

        int w = board.length;
        int h = board[0].length;

        if (pX == lastX && pY == lastY) stuckCounter++;
        else stuckCounter = 0;
        lastX = pX; lastY = pY;

        if (stuckCounter >= 5) {
            stuckCounter = 0;
            return randomSafeMove(board, pX, pY, getGhostsSafe(game));
        }

        ArrayList<int[]> ghosts = getGhostsSafe(game);

        int distToGhost = getMinDistanceCyclic(pX, pY, ghosts, w, h);

        if (distToGhost <= PANIC_DIST) {
            return bestEscapeMove(board, pX, pY, ghosts);
        }

        int dir = bfsToFoodSecure(board, pX, pY, ghosts);

        if (dir != -1) return dir;

        return bestEscapeMove(board, pX, pY, ghosts);
    }

    /**
     * Calculates the best move to escape ghosts.
     * Considers cyclic borders (torus) and maximizes open space (Flood Fill).
     */
    private int bestEscapeMove(int[][] board, int x, int y, ArrayList<int[]> ghosts) {
        int w = board.length;
        int h = board[0].length;
        int[] dirs = {Game.UP, Game.RIGHT, Game.DOWN, Game.LEFT};
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};

        int bestDir = -1;
        double bestScore = -Double.MAX_VALUE;

        for (int i = 0; i < 4; i++) {
            int nx = (x + dx[i] + w) % w;
            int ny = (y + dy[i] + h) % h;

            if (board[nx][ny] != WALL) {

                int minGhostDist = getMinDistToGhostCyclic(nx, ny, ghosts, w, h);

                if (minGhostDist <= 1) continue;

                double score = 0;
                score += (minGhostDist * 10);

                int openSpace = countReachableTilesCyclic(board, nx, ny, ghosts, CHECK_DEPTH);

                if (openSpace < 20) score -= 100000;
                else if (openSpace < 50) score -= 5000;
                else score += (openSpace * 5);

                if (board[nx][ny] == FOOD) score += 5;

                if (score > bestScore) {
                    bestScore = score;
                    bestDir = dirs[i];
                }
            }
        }

        if (bestDir != -1) return bestDir;
        return randomSafeMove(board, x, y, ghosts);
    }

    /**
     * Finds the nearest food using BFS.
     * Ensures the path leads to a safe area with enough open space.
     */
    private int bfsToFoodSecure(int[][] board, int startX, int startY, ArrayList<int[]> ghosts) {
        int w = board.length;
        int h = board[0].length;
        boolean[][] visited = new boolean[w][h];
        int[][] firstMove = new int[w][h];
        Queue<int[]> queue = new LinkedList<>();

        queue.add(new int[]{startX, startY});
        visited[startX][startY] = true;

        while(!queue.isEmpty()) {
            int[] curr = queue.poll();
            int cx = curr[0];
            int cy = curr[1];

            if ((board[cx][cy] == FOOD || board[cx][cy] == POWER) && !(cx==startX && cy==startY)) {
                int dir = firstMove[cx][cy];
                int[] dxy = dirToDelta(dir);
                int nx = (startX + dxy[0] + w) % w;
                int ny = (startY + dxy[1] + h) % h;

                int safetyArea = countReachableTilesCyclic(board, nx, ny, ghosts, CHECK_DEPTH);

                if (safetyArea >= MIN_SAFE_AREA) {
                    return dir;
                }
            }

            int[] dx = {0, 1, 0, -1};
            int[] dy = {1, 0, -1, 0};
            int[] moves = {Game.UP, Game.RIGHT, Game.DOWN, Game.LEFT};

            for (int i = 0; i < 4; i++) {
                int nx = (cx + dx[i] + w) % w;
                int ny = (cy + dy[i] + h) % h;

                if (board[nx][ny] != WALL && !visited[nx][ny]) {
                    if (isSafeFromGhostsCyclic(nx, ny, ghosts, w, h)) {
                        visited[nx][ny] = true;
                        if (cx == startX && cy == startY) firstMove[nx][ny] = moves[i];
                        else firstMove[nx][ny] = firstMove[cx][cy];
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Counts reachable tiles using BFS (Flood Fill).
     * Handles cyclic borders and treats ghosts as walls.
     */
    private int countReachableTilesCyclic(int[][] board, int startX, int startY, ArrayList<int[]> ghosts, int limit) {
        int w = board.length;
        int h = board[0].length;
        boolean[][] visited = new boolean[w][h];
        Queue<int[]> q = new LinkedList<>();

        q.add(new int[]{startX, startY});
        visited[startX][startY] = true;

        int count = 0;
        while(!q.isEmpty() && count < limit) {
            int[] curr = q.poll();
            count++;

            int[] dx = {0, 1, 0, -1};
            int[] dy = {1, 0, -1, 0};

            for(int i=0; i<4; i++) {
                int nx = (curr[0] + dx[i] + w) % w;
                int ny = (curr[1] + dy[i] + h) % h;

                if (!visited[nx][ny] && board[nx][ny] != WALL) {
                    if (isSafeFromGhostsCyclic(nx, ny, ghosts, w, h)) {
                        visited[nx][ny] = true;
                        q.add(new int[]{nx, ny});
                    }
                }
            }
        }
        return count;
    }

    /**
     * Checks if a position is safe from ghosts, considering cyclic distance.
     */
    private boolean isSafeFromGhostsCyclic(int x, int y, ArrayList<int[]> ghosts, int w, int h) {
        if (ghosts == null) return true;
        for(int[] g : ghosts) {
            int d = cyclicDist(x, y, g[0], g[1], w, h);
            if (d <= 1) return false;
        }
        return true;
    }

    /**
     * Calculates the minimum cyclic distance to any ghost.
     */
    private int getMinDistToGhostCyclic(int x, int y, ArrayList<int[]> ghosts, int w, int h) {
        int min = Integer.MAX_VALUE;
        if (ghosts == null || ghosts.isEmpty()) return 100;
        for (int[] g : ghosts) {
            int d = cyclicDist(x, y, g[0], g[1], w, h);
            if (d < min) min = d;
        }
        return min;
    }

    /**
     * Wrapper for minimum distance (unused in cyclic version).
     */
    private int getMinDistance(int x, int y, ArrayList<int[]> ghosts) {
        return 0;
    }

    /**
     * Wrapper for cyclic minimum distance.
     */
    private int getMinDistanceCyclic(int x, int y, ArrayList<int[]> ghosts, int w, int h) {
        return getMinDistToGhostCyclic(x, y, ghosts, w, h);
    }

    /**
     * Calculates distance between two points on a torus (cyclic board).
     */
    private int cyclicDist(int x1, int y1, int x2, int y2, int w, int h) {
        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);

        dx = Math.min(dx, w - dx);
        dy = Math.min(dy, h - dy);

        return dx + dy;
    }

    /**
     * Converts a direction constant to X and Y changes.
     */
    private int[] dirToDelta(int dir) {
        if (dir == Game.UP) return new int[]{0, 1};
        if (dir == Game.RIGHT) return new int[]{1, 0};
        if (dir == Game.DOWN) return new int[]{0, -1};
        if (dir == Game.LEFT) return new int[]{-1, 0};
        return new int[]{0, 0};
    }

    /**
     * Returns a random valid move, preferably one that is safe from ghosts.
     */
    private int randomSafeMove(int[][] board, int x, int y, ArrayList<int[]> ghosts) {
        int w = board.length;
        int h = board[0].length;
        int[] dirs = {Game.UP, Game.RIGHT, Game.DOWN, Game.LEFT};
        ArrayList<Integer> safeMoves = new ArrayList<>();
        ArrayList<Integer> legalMoves = new ArrayList<>();
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};

        for(int i=0; i<4; i++) {
            int nx = (x + dx[i] + w) % w;
            int ny = (y + dy[i] + h) % h;

            if(board[nx][ny] != WALL) {
                legalMoves.add(dirs[i]);
                if (isSafeFromGhostsCyclic(nx, ny, ghosts, w, h)) {
                    safeMoves.add(dirs[i]);
                }
            }
        }
        if (!safeMoves.isEmpty()) return safeMoves.get(rand.nextInt(safeMoves.size()));
        if (!legalMoves.isEmpty()) return legalMoves.get(rand.nextInt(legalMoves.size()));
        return Game.STAY;
    }

    /**
     * Retrieves ghost positions using reflection to support different API versions.
     */
    private ArrayList<int[]> getGhostsSafe(PacmanGame game) {
        ArrayList<int[]> list = new ArrayList<>();
        try {
            Method m = game.getClass().getMethod("getGhosts", int.class);
            String[] ghosts = (String[]) m.invoke(game, 0);
            if (ghosts != null) for (String g : ghosts) { int[] xy = pos(g); if (xy != null) list.add(xy); }
        } catch (Exception e) {
            try {
                Method m = game.getClass().getMethod("getGhosts");
                String[] ghosts = (String[]) m.invoke(game);
                if (ghosts != null) for (String g : ghosts) { int[] xy = pos(g); if (xy != null) list.add(xy); }
            } catch (Exception ignored) {}
        }
        return list;
    }

    /**
     * Parses a position string into an integer array [x, y].
     */
    private static int[] pos(String p) {
        if (p == null) return null;
        try { String[] a = p.split(","); return new int[]{Integer.parseInt(a[0]), Integer.parseInt(a[1])}; } catch (Exception e) { return null; }
    }
}