package assignments.Ex3;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for Ex3Game server logic.
 * Focuses on map parsing, entity placement and board initialization.
 */
class Ex3GameTest {

    /**
     * Verifies that the map dimensions are parsed correctly from the input string.
     * Checks if the width and height of the generated board match the string structure.
     */
    @Test
    void testLoadMapDimensions() {
        Ex3Game game = new Ex3Game();
        String simpleMap =
                "WWWW\n" +
                        "W.PW\n" +
                        "WWWW";

        game.loadMap(simpleMap);

        int[][] board = game.getGame(0);
        assertEquals(4, board.length, "Width should be 4");
        assertEquals(3, board[0].length, "Height should be 3");
    }

    /**
     * Verifies that Pacman's starting position is correctly parsed.
     * Ensures the coordinate conversion (flipping Y-axis) is handled properly.
     */
    @Test
    void testPacmanPosition() {
        Ex3Game game = new Ex3Game();
        String map =
                "WWWWW\n" +
                        "W...W\n" +
                        "W.P.W\n" +
                        "WWWWW";

        game.loadMap(map);
        String pos = game.getPos(0);
        assertEquals("2,1", pos, "Pacman position should be parsed correctly");
    }

    /**
     * Verifies that ghost entities are correctly detected and loaded.
     * Checks the ghost count and their initial coordinates.
     */
    @Test
    void testGhostLoading() {
        Ex3Game game = new Ex3Game();
        String map =
                "WGW\n" +
                        "W.W";

        game.loadMap(map);
        String[] ghosts = game.getGhosts();

        assertNotNull(ghosts);
        assertEquals(1, ghosts.length, "Should detect exactly 1 ghost");
        assertTrue(ghosts[0].startsWith("1,1"), "Ghost position should be correct");
    }

    /**
     * Verifies that specific tile types (Walls and Food) are correctly identified
     * and stored in the board array with their corresponding integer constants.
     */
    @Test
    void testWallAndFoodParsing() {
        Ex3Game game = new Ex3Game();
        String map = "W.";

        game.loadMap(map);
        int[][] board = game.getGame(0);

        assertEquals(Ex3Game.WALL, board[0][0], "Should be a wall");
        assertEquals(Ex3Game.FOOD, board[1][0], "Should be food");
    }
}