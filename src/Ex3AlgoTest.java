package assignments.Ex3;

import exe.ex3.game.Game;
import exe.ex3.game.PacManAlgo;
import exe.ex3.game.PacmanGame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class for Ex3Algo logic.
 * Uses Dynamic Proxy to mock the PacmanGame interface for isolated testing.
 */
class Ex3AlgoTest {

    private static final int WALL = 1;
    private static final int FOOD = 3;

    /**
     * Internal interface used ONLY for testing.
     * Must be public so the Proxy can implement it alongside PacmanGame.
     * Allows retrieving ghost data during tests.
     */
    public interface GhostSource {
        String[] getGhosts();
    }

    /**
     * Helper method to create a Mock/Stub of PacmanGame.
     * Creates a dynamic object that returns specific values for getGame, getPos, and getGhosts.
     * This allows testing the algorithm without running the actual GUI/Game engine.
     *
     * @param board The 2D array representing the map.
     * @param pos The Pacman position string (e.g., "5,5").
     * @param ghosts The array of ghost strings.
     * @return A mock PacmanGame instance.
     */
    private static PacmanGame stubGame(int[][] board, String pos, String[] ghosts) {
        return (PacmanGame) Proxy.newProxyInstance(
                Ex3AlgoTest.class.getClassLoader(),
                new Class<?>[]{PacmanGame.class, GhostSource.class},
                (proxy, method, args) -> {
                    String name = method.getName();

                    if (name.equals("getGame")) {
                        return board;
                    }
                    if (name.equals("getPos")) {
                        return pos;
                    }
                    if (name.equals("getGhosts")) {
                        return ghosts;
                    }

                    Class<?> rt = method.getReturnType();
                    if (rt.equals(int.class)) return 0;
                    if (rt.equals(boolean.class)) return false;
                    if (rt.equals(char.class)) return '\0';
                    return null;
                }
        );
    }

    /**
     * Tests that the getInfo() method returns a valid, non-empty string.
     * This is required for identifying the student/algorithm.
     */
    @Test
    void testGetInfoNotEmpty() {
        PacManAlgo algo = new Ex3Algo();
        String info = algo.getInfo();
        assertNotNull(info);
        assertFalse(info.trim().isEmpty());
    }

    /**
     * Tests that the move() method runs efficiently (under 2 seconds).
     * Also verifies that it returns a valid direction constant (UP, DOWN, LEFT, RIGHT, STAY).
     */
    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testMoveReturnsLegalDirection() {
        int w = 10, h = 10;
        int[][] board = new int[w][h];
        PacmanGame game = stubGame(board, "5,5", new String[]{});
        PacManAlgo algo = new Ex3Algo();

        int dir = algo.move(game);

        // Correct assertion logic: Check if dir is any valid Game constant
        boolean isValid = (dir == Game.UP || dir == Game.RIGHT || dir == Game.DOWN ||
                dir == Game.LEFT || dir == Game.STAY);
        assertTrue(isValid, "Direction should be a valid Game constant (0-4)");
    }

    /**
     * Tests basic pathfinding logic.
     * Verifies that Pacman moves towards the nearest food source on a clear board.
     */
    @Test
    void testMoveTowardsFoodSimple() {
        int[][] board = new int[10][10];
        board[6][5] = FOOD; // Food is to the right

        PacmanGame game = stubGame(board, "5,5", new String[]{});
        PacManAlgo algo = new Ex3Algo();

        int dir = algo.move(game);
        assertEquals(Game.RIGHT, dir, "Pacman should move RIGHT towards the food");
    }

    /**
     * Tests the algorithm's awareness of cyclic (torus) topology.
     * Verifies that Pacman chooses the wrap-around path if it is shorter than the direct path.
     */
    @Test
    void testCyclicMovement() {
        int w = 10, h = 10;
        int[][] board = new int[w][h];
        board[9][5] = FOOD; // Food at far right

        // Pacman at far left (0,5). Shortest path is LEFT (wrap around).
        PacmanGame game = stubGame(board, "0,5", new String[]{});
        PacManAlgo algo = new Ex3Algo();

        int dir = algo.move(game);
        assertEquals(Game.LEFT, dir, "Pacman should move LEFT to wrap around");
    }

    /**
     * Tests basic evasion logic.
     * Verifies that Pacman avoids moving directly towards a ghost that is nearby.
     */
    @Test
    void testRunAwayFromGhost() {
        int[][] board = new int[10][10];
        String[] ghosts = {"6,5,0"}; // Ghost at 6,5 (Right)

        PacmanGame game = stubGame(board, "5,5", ghosts);
        PacManAlgo algo = new Ex3Algo();

        int dir = algo.move(game);

        assertNotEquals(Game.RIGHT, dir, "Pacman should NOT move towards the ghost");
        // Depending on implementation, UP/DOWN/LEFT are all safer than RIGHT.
        // But LEFT is the direct opposite.
        assertNotEquals(Game.STAY, dir, "Pacman should not stay when ghost is near");
    }

    /**
     * Tests the "Virtual Wall" safety mechanism.
     * Verifies that Pacman treats tiles adjacent to ghosts as walls and refuses to move
     * towards food if it puts him in immediate danger (distance 1 from ghost).
     */
    @Test
    void testVirtualWallGhost() {
        int[][] board = new int[10][10];
        board[5][6] = FOOD; // Food UP

        // Ghost at 4,6. Distance to food is 1. Food is dangerous.
        String[] ghosts = {"4,6,0"};

        PacmanGame game = stubGame(board, "5,5", ghosts);
        PacManAlgo algo = new Ex3Algo();

        int dir = algo.move(game);

        assertNotEquals(Game.UP, dir, "Pacman should NOT go UP towards dangerous food");
    }
}