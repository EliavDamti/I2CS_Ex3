package assignments.Ex3;

import exe.ex3.game.Game;
import exe.ex3.game.GhostCL;
import exe.ex3.game.PacManAlgo;
import exe.ex3.game.PacmanGame;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;
import java.awt.Color;
import java.util.ArrayList;

/**
 * The {@code Ex3Game} class implements the {@link PacmanGame} interface and serves as the
 * main game engine (Server-Side) for the Pac-Man game.
 * <p>
 * This class is responsible for:
 * <ul>
 * <li>Parsing and loading the map from a String representation.</li>
 * <li>Managing the game loop, including timing and rendering.</li>
 * <li>Handling game entities (Pacman, Ghosts, Walls, Food).</li>
 * <li>Executing movement logic and collision detection.</li>
 * <li>Rendering the game state using {@link StdDraw} with custom images.</li>
 * <li>Playing background music and sound effects.</li>
 * </ul>
 * </p>
 *
 * @author Eliav Damti
 */
public class Ex3Game implements PacmanGame {

    private int[][] board;
    private int width = 22;
    private int height = 22;

    private String pacmanPos = "10,10";
    private String[] ghosts = {};
    private boolean isRunning = true;

    private int score = 0;
    private int totalFood = 0;

    private int lastDir = Game.RIGHT;

    private Clip bgMusicClip;

    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int FOOD = 3;

    /**
     * Default constructor. Initializes a new instance of the game.
     */
    public Ex3Game() {
    }

    /**
     * Plays background music in a continuous loop.
     * Runs in a separate thread to prevent blocking the game loop.
     *
     * @param filePath The relative path to the .wav audio file.
     */
    public void playBackgroundMusic(String filePath) {
        new Thread(() -> {
            try {
                File musicPath = new File(filePath);
                if (musicPath.exists()) {
                    AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                    bgMusicClip = AudioSystem.getClip();
                    bgMusicClip.open(audioInput);

                    try {
                        FloatControl gainControl = (FloatControl) bgMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
                        gainControl.setValue(-15.0f);
                    } catch (Exception e) {
                    }

                    bgMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                    bgMusicClip.start();
                } else {
                    System.out.println("Music file not found: " + filePath);
                }
            } catch (Exception e) {
                System.err.println("Music error: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Stops the currently playing background music if it is running.
     * Should be called when the game ends (Win/Loss).
     */
    public void stopBackgroundMusic() {
        if (bgMusicClip != null && bgMusicClip.isRunning()) {
            bgMusicClip.stop();
            bgMusicClip.close();
        }
    }

    /**
     * Plays a single sound effect (once).
     *
     * @param filePath The relative path to the .wav audio file.
     */
    public void playSoundEffect(String filePath) {
        new Thread(() -> {
            try {
                File musicPath = new File(filePath);
                if (musicPath.exists()) {
                    AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                    Clip fxClip = AudioSystem.getClip();
                    fxClip.open(audioInput);
                    fxClip.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Returns the array of ghosts strings required by the game interface.
     * @return String array representing ghosts.
     */
    public String[] getGhosts() {
        return ghosts;
    }

    /**
     * Parses a string map representation and initializes the game board.
     * Converts characters (W, ., P, G) into the internal integer board representation.
     * Also counts the total food available for win condition.
     *
     * @param mapStr The string representation of the map.
     */
    public void loadMap(String mapStr) {
        String[] lines = mapStr.split("\n");
        height = lines.length;
        width = lines[0].length();
        board = new int[width][height];
        totalFood = 0;

        ArrayList<String> ghostList = new ArrayList<>();

        for (int i = 0; i < height; i++) {
            int y = height - 1 - i;
            String line = lines[i];

            for (int x = 0; x < width; x++) {
                char c = (x < line.length()) ? line.charAt(x) : ' ';
                board[x][y] = EMPTY;

                switch (c) {
                    case 'W':
                    case '#':
                        board[x][y] = WALL;
                        break;
                    case '.':
                        board[x][y] = FOOD;
                        totalFood++;
                        break;
                    case 'P':
                        board[x][y] = EMPTY;
                        pacmanPos = x + "," + y;
                        break;
                    case 'G':
                        board[x][y] = FOOD;
                        totalFood++;
                        ghostList.add(x + "," + y + ",0");
                        break;
                    default:
                        board[x][y] = EMPTY;
                        break;
                }
            }
        }
        ghosts = ghostList.toArray(new String[0]);
    }

    /**
     * Starts the main game loop.
     * Initializes the graphics, handles the logic loop, and renders the game.
     *
     * @param algo The algorithm (Strategy) controlling the Pacman.
     */
    public void play(PacManAlgo algo) {
        StdDraw.setCanvasSize(800, 800);
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height);
        StdDraw.enableDoubleBuffering();

        playBackgroundMusic("pacman-sound.wav");

        while (isRunning) {
            int moveDir = algo.move(this);

            movePacman(moveDir);
            moveGhosts();

            if (score == totalFood) {
                drawGame();
                StdDraw.show();
                StdDraw.pause(300);

                stopBackgroundMusic();
                playSoundEffect("Win sound.WAV");
                drawWinScreen();
                isRunning = false;
                break;
            }

            if (checkCollision()) {
                drawGame();
                StdDraw.show();
                StdDraw.pause(500);

                stopBackgroundMusic();
                playSoundEffect("Death sound.WAV");
                System.out.println("GAME OVER! Score: " + score);
                drawLoseScreen();
                isRunning = false;
                break;
            }

            drawGame();
            StdDraw.show();
            StdDraw.pause(200);
        }
        StdDraw.pause(5000);
        System.exit(0);
    }

    /**
     * Updates Pacman's position based on the chosen direction.
     * Handles wall collisions and cyclic world wrapping.
     *
     * @param dir The direction to move (UP, DOWN, LEFT, RIGHT).
     */
    private void movePacman(int dir) {
        if (dir == Game.STAY) return;

        lastDir = dir;

        String[] xy = pacmanPos.split(",");
        int x = Integer.parseInt(xy[0]);
        int y = Integer.parseInt(xy[1]);
        int nextX = x;
        int nextY = y;

        if (dir == Game.UP) nextY++;
        if (dir == Game.DOWN) nextY--;
        if (dir == Game.RIGHT) nextX++;
        if (dir == Game.LEFT) nextX--;

        if (nextX < 0) nextX = width - 1;
        if (nextX >= width) nextX = 0;
        if (nextY < 0) nextY = height - 1;
        if (nextY >= height) nextY = 0;

        if (board[nextX][nextY] == WALL) return;

        pacmanPos = nextX + "," + nextY;
        if (board[nextX][nextY] == FOOD) {
            board[nextX][nextY] = EMPTY;
            score++;
        }
    }

    /**
     * Moves all ghosts on the board.
     * Ghosts choose a random valid direction (not into a wall).
     */
    private void moveGhosts() {
        for (int i = 0; i < ghosts.length; i++) {
            String[] data = ghosts[i].split(",");
            int x = Integer.parseInt(data[0]);
            int y = Integer.parseInt(data[1]);
            int type = Integer.parseInt(data[2]);

            ArrayList<Integer> validDirections = new ArrayList<>();
            int[] dirs = {Game.UP, Game.DOWN, Game.LEFT, Game.RIGHT};

            for (int dir : dirs) {
                int nextX = x;
                int nextY = y;

                if (dir == Game.UP) nextY++;
                if (dir == Game.DOWN) nextY--;
                if (dir == Game.RIGHT) nextX++;
                if (dir == Game.LEFT) nextX--;

                if (nextX < 0) nextX = width - 1;
                if (nextX >= width) nextX = 0;
                if (nextY < 0) nextY = height - 1;
                if (nextY >= height) nextY = 0;

                if (board[nextX][nextY] != WALL) {
                    validDirections.add(dir);
                }
            }

            if (!validDirections.isEmpty()) {
                int randomIdx = (int) (Math.random() * validDirections.size());
                int chosenDir = validDirections.get(randomIdx);

                int nextX = x;
                int nextY = y;

                if (chosenDir == Game.UP) nextY++;
                if (chosenDir == Game.DOWN) nextY--;
                if (chosenDir == Game.RIGHT) nextX++;
                if (chosenDir == Game.LEFT) nextX--;

                if (nextX < 0) nextX = width - 1;
                if (nextX >= width) nextX = 0;
                if (nextY < 0) nextY = height - 1;
                if (nextY >= height) nextY = 0;

                ghosts[i] = nextX + "," + nextY + "," + type;
            }
        }
    }

    /**
     * Checks if Pacman occupies the same coordinate as any ghost.
     * @return true if collision detected, false otherwise.
     */
    private boolean checkCollision() {
        String[] p = pacmanPos.split(",");
        int px = Integer.parseInt(p[0]);
        int py = Integer.parseInt(p[1]);

        for (String g : ghosts) {
            String[] gData = g.split(",");
            int gx = Integer.parseInt(gData[0]);
            int gy = Integer.parseInt(gData[1]);
            if (px == gx && py == gy) return true;
        }
        return false;
    }

    /**
     * Draws the current game state to the canvas using StdDraw.
     * Handles images for Pacman and Ghosts, and draws walls/food.
     */
    private void drawGame() {
        StdDraw.clear(Color.BLACK);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (board[x][y] == WALL) {
                    StdDraw.setPenColor(Color.BLUE);
                    StdDraw.filledSquare(x + 0.5, y + 0.5, 0.5);
                } else if (board[x][y] == FOOD) {
                    StdDraw.setPenColor(Color.PINK);
                    StdDraw.filledCircle(x + 0.5, y + 0.5, 0.15);
                }
            }
        }

        String[] p = pacmanPos.split(",");
        double pacX = Double.parseDouble(p[0]) + 0.5;
        double pacY = Double.parseDouble(p[1]) + 0.5;

        double angle = 0;
        if (lastDir == Game.UP) angle = 90;
        if (lastDir == Game.LEFT) angle = 180;
        if (lastDir == Game.DOWN) angle = 270;

        try {
            StdDraw.picture(pacX, pacY, "p1.png", 0.8, 0.8, angle);
        } catch (Exception e) {
            StdDraw.setPenColor(Color.YELLOW);
            StdDraw.filledCircle(pacX, pacY, 0.4);
        }

        for (int i = 0; i < ghosts.length; i++) {
            String g = ghosts[i];
            String[] gData = g.split(",");
            double gx = Double.parseDouble(gData[0]) + 0.5;
            double gy = Double.parseDouble(gData[1]) + 0.5;

            int imgNum = i % 4;
            String ghostImg = "g" + imgNum + ".png";

            try {
                StdDraw.picture(gx, gy, ghostImg, 0.8, 0.8);
            } catch (Exception e) {
                StdDraw.setPenColor(Color.RED);
                StdDraw.filledCircle(gx, gy, 0.4);
            }
        }

        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(4, height - 0.5, "Score: " + score + " / " + totalFood);
    }

    /**
     * Draws the 'You Win' screen with the final score.
     */
    private void drawWinScreen() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.GREEN);
        StdDraw.text(width / 2.0, height / 2.0, "You are the Winner!");
        StdDraw.text(width / 2.0, height / 2.0 - 2, "Final Score: " + score + " / " + totalFood);
        StdDraw.show();
    }

    /**
     * Draws the 'Game Over' screen with the final score.
     */
    private void drawLoseScreen() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.RED);
        StdDraw.text(width / 2.0, height / 2.0, "GAME OVER");
        StdDraw.text(width / 2.0, height / 2.0 - 2, "Final Score: " + score + " / " + totalFood);
        StdDraw.show();
    }

    // --- Interface Implementation Methods ---
    @Override public int[][] getGame(int type) { return board; }
    @Override public String getPos(int type) { return pacmanPos; }
    @Override public GhostCL[] getGhosts(int type) { return new GhostCL[ghosts.length]; }
    @Override public String init(int i, String s, boolean b, long l, double v, int i1, int i2) { return ""; }
    @Override public String move(int i) { return ""; }
    @Override public void play() {}
    @Override public String end(int i) { return ""; }
    @Override public String getData(int i) { return ""; }
    @Override public int getStatus() { return 0; }
    @Override public boolean isCyclic() { return true; }
    @Override public Character getKeyChar() { return null; }

    /**
     * Entry point for running the custom game implementation.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Ex3Game game = new Ex3Game();
        String map = "WWWWWWWWWW..WWWWWWWWWW\n" +
                "W.........GG.........W\n" +
                "W.WWWW.WW.WW.WW.WWWW.W\n" +
                "W.W....W..WW..W....W.W\n" +
                "W.W.WW.W.WWWW.W.WW.W.W\n" +
                "W.W.WW..........WW.W.W\n" +
                "W......WW.WW.WW......W\n" +
                "WW.WWW.WW.WW.WW.WWW.WW\n" +
                "W..W..............W..W\n" +
                "W.WW.WWW..P...WWW.WW.W\n" +
                "..WW.WWW......WWW.WW..\n" +
                "...W..............W...\n" +
                "WW.WWW.WW.WW.WW.WWW.WW\n" +
                "W......WW.WW.WW......W\n" +
                "W.W.WW..........WW.W.W\n" +
                "W.W.WW.W.WWWW.W.WW.W.W\n" +
                "W.W....W..WW..W....W.W\n" +
                "W.WWWW.WW.WW.WW.WWWW.W\n" +
                "W.........GG.........W\n" +
                "W.WWWWWWWWWWWWWWWWWW.W\n" +
                "W....................W\n" +
                "WWWWWWWWWW..WWWWWWWWWW";
        game.loadMap(map);
        PacManAlgo myAlgo = new Ex3Algo();
        System.out.println("Starting game with: " + myAlgo.getInfo());
        game.play(myAlgo);
    }
}