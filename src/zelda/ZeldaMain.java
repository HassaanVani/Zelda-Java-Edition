package zelda;

import engine.KeyHandler;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ZeldaMain {
    public static void main(String[] args) {
        JFrame window = new JFrame("The Legend of Zelda");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        
        ZeldaGamePanel gamePanel = new ZeldaGamePanel();
        window.add(gamePanel);
        window.pack();
        
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gamePanel.cleanup();
            }
        });
        
        gamePanel.startGame();
    }
}

class ZeldaGamePanel extends JPanel implements Runnable {
    public static final int NATIVE_WIDTH = 256;
    public static final int NATIVE_HEIGHT = 240;
    public static final int SCALE = 3;
    public static final int SCREEN_WIDTH = NATIVE_WIDTH * SCALE;
    public static final int SCREEN_HEIGHT = NATIVE_HEIGHT * SCALE;
    
    private Thread gameThread;
    private final int FPS = 60;
    
    private KeyHandler keyHandler;
    private ZeldaGame game;
    
    public ZeldaGamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        
        keyHandler = new KeyHandler();
        this.addKeyListener(keyHandler);
        
        game = new ZeldaGame(keyHandler);
    }
    
    public void startGame() {
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        
        while (gameThread != null) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }
    
    private void update() {
        game.update();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g;
        
        g2.scale(SCALE, SCALE);
        
        game.render(g2);
        
        g2.dispose();
    }
    
    public void cleanup() {
        if (game != null) {
            game.getAudioManager().cleanup();
        }
    }
}
