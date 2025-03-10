import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U D L R
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize/4;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize/4;
            }
            else if (this.direction == 'L') {
                this.velocityX = -tileSize/4;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = tileSize/4;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    // X = wall, O = skip, P = pac man, ' ' = food
    // Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXrXX X XXXX",
            "O       bpo       O",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'}; // up down left right
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;
    boolean gamePaused = false;

    // Enhancement: Restart button
    JButton restartButton;
    JPanel buttonPanel;

    // Constructor for the game panel
    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);
        setLayout(new BorderLayout());

        // Initialize button panel
        buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        restartButton = new JButton("Restart Game");
        restartButton.setFocusable(false); // Prevent taking focus from game
        restartButton.addActionListener(e -> restartGame());
        buttonPanel.add(restartButton);

        // Add button panel to the bottom of the game panel
        add(buttonPanel, BorderLayout.SOUTH);

        // Load images
        loadImages();
        loadMap();

        // Initialize ghost directions
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }

        // How long it takes to start timer, milliseconds gone between frames
        gameLoop = new Timer(50, this); // 20fps (1000/50)
        gameLoop.start();
    }

    // Enhancement: Separate method for loading images
    private void loadImages() {
        try {
            wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
            blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
            orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
            pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
            redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();

            pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
            pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
            pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
            pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();
        } catch (Exception e) {
            System.out.println("Error loading images: " + e.getMessage());
            // Use colored rectangles as fallback if images don't load
            createFallbackImages();
        }
    }

    // Enhancement: Fallback images if resource loading fails
    private void createFallbackImages() {
        // Create blank images of the right size
        wallImage = createColoredImage(Color.BLUE, tileSize, tileSize);
        blueGhostImage = createColoredImage(Color.CYAN, tileSize, tileSize);
        orangeGhostImage = createColoredImage(Color.ORANGE, tileSize, tileSize);
        pinkGhostImage = createColoredImage(Color.PINK, tileSize, tileSize);
        redGhostImage = createColoredImage(Color.RED, tileSize, tileSize);

        pacmanUpImage = createColoredImage(Color.YELLOW, tileSize, tileSize);
        pacmanDownImage = createColoredImage(Color.YELLOW, tileSize, tileSize);
        pacmanLeftImage = createColoredImage(Color.YELLOW, tileSize, tileSize);
        pacmanRightImage = createColoredImage(Color.YELLOW, tileSize, tileSize);
    }

    // Helper method to create a colored image
    private Image createColoredImage(Color color, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(color);
        g2d.fillOval(0, 0, width, height);
        g2d.dispose();
        return img;
    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c*tileSize;
                int y = r*tileSize;

                if (tileMapChar == 'X') { // Block wall
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') { // Blue ghost
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') { // Orange ghost
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') { // Pink ghost
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'r') { // Red ghost
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') { // Pacman
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') { // Food
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }
    }

    // Enhancement: Restart game method (used by button)
    public void restartGame() {
        loadMap();
        resetPositions();
        lives = 3;
        score = 0;
        gameOver = false;
        gamePaused = false;
        if (!gameLoop.isRunning()) {
            gameLoop.start();
        }
        this.requestFocus(); // Give focus back to the game panel
    }

    // Enhancement: Toggle pause method
    public void togglePause() {
        gamePaused = !gamePaused;
        if (gamePaused) {
            gameLoop.stop();
        } else {
            gameLoop.start();
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Enhancement: Adjust drawing for button panel placement
        int gameAreaHeight = getHeight() - buttonPanel.getHeight();

        // Draw game elements
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        // Score and game state display
        g.setFont(new Font("Arial", Font.BOLD, 18));
        if (gameOver) {
            // Enhancement: More visible game over message
            g.setColor(Color.RED);
            g.drawString("Game Over: " + String.valueOf(score), tileSize/2, tileSize/2);
            g.setColor(Color.YELLOW);
            g.drawString("Press any key or Restart button to play again", tileSize/2, tileSize);
        }
        else if (gamePaused) {
            g.setColor(Color.YELLOW);
            g.drawString("GAME PAUSED - Press P to resume", tileSize/2, tileSize/2);
            g.setColor(Color.WHITE);
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize/2, tileSize);
        }
        else {
            g.setColor(Color.WHITE);
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize/2, tileSize/2);
            // Enhancement: Display controls
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("Arrow Keys: Move | P: Pause | R: Restart", tileSize/2, gameAreaHeight - 10);
        }
    }

    public void move() {
        if (gamePaused || gameOver) {
            return;
        }

        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // Check wall collisions
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        // Enhancement: Teleport through edges (tunnel)
        if (pacman.x < 0) {
            pacman.x = boardWidth - pacman.width;
        } else if (pacman.x > boardWidth - pacman.width) {
            pacman.x = 0;
        }

        // Check ghost collisions
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    return;
                }
                resetPositions();
            }

            if (ghost.y == tileSize*9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }

        // Check food collision
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        // Level completed
        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
            // Enhancement: Increase score for completing level
            score += 100;
        }
    }

    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
            return;
        }

        // Enhancement: Add keyboard commands for pause and restart
        if (e.getKeyCode() == KeyEvent.VK_P) {
            togglePause();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_R) {
            restartGame();
            return;
        }

        if (gamePaused) {
            return;
        }

        // Movement controls
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
        }

        // Update Pacman image based on direction
        if (pacman.direction == 'U') {
            pacman.image = pacmanUpImage;
        }
        else if (pacman.direction == 'D') {
            pacman.image = pacmanDownImage;
        }
        else if (pacman.direction == 'L') {
            pacman.image = pacmanLeftImage;
        }
        else if (pacman.direction == 'R') {
            pacman.image = pacmanRightImage;
        }
    }

    // Main method to start the game (combines App.java and PacMan.java)
    public static void main(String[] args) {
        int rowCount = 21;
        int columnCount = 19;
        int tileSize = 32;
        int boardWidth = columnCount * tileSize;
        int boardHeight = rowCount * tileSize;

        JFrame frame = new JFrame("Pac Man");
        frame.setSize(boardWidth, boardHeight + 40); // Extra height for restart button
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        PacMan pacmanGame = new PacMan();
        frame.add(pacmanGame);
        frame.pack();
        pacmanGame.requestFocus();
        frame.setVisible(true);
    }
}