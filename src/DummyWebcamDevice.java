import com.github.sarxos.webcam.WebcamDevice;

import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Dimension;

class DummyWebcamDevice implements WebcamDevice {

    private BufferedImage buffer;
    private BufferedImage display;
    Graphics2D g;
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;

    private static final int BOUNCE_SIZE = 50;
    private static final int BOUNCE_SIZE2 = BOUNCE_SIZE * 2;

    private static final double MAX_VEL = 100;

    private Image bounce;
    private double bounceX = WIDTH / 2;
    private double bounceY = HEIGHT / 2;

    private double xvel;
    private double yvel;

    private long time;

    private Dimension[] dimensions;

    boolean open;

    public DummyWebcamDevice() {
        time = System.currentTimeMillis();
        buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        display = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = buffer.createGraphics();
        try {
            bounce = ImageIO.read(new File("./data/img/bounce.png"))
                .getScaledInstance(BOUNCE_SIZE2, BOUNCE_SIZE2, BufferedImage.SCALE_SMOOTH);
        } catch (IOException e) {
            bounce = null;
        }
        Random r = new Random();
        xvel = (r.nextDouble()-0.5)*MAX_VEL*2;
        yvel = (r.nextDouble()-0.5)*MAX_VEL*2;
        dimensions = new Dimension[1];
        dimensions[0] = new Dimension(display.getWidth(),display.getHeight());
        open = false;
    }

    public String getName()
    {
        return "Simulated Webcam";
    }

    @Override
    public Dimension getResolution()
    {
        return dimensions[0];
    }

    @Override
    public Dimension[] getResolutions()
    {
        return dimensions;
    }

    @Override
    public void setResolution(Dimension size)
    {
        return;
    }

    @Override
    public void open()
    {
        open = true;
    }

    @Override
    public void close()
    {
        open = false;
    }

    private void draw()
    {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(bounce, (int)bounceX-BOUNCE_SIZE, (int)bounceY-BOUNCE_SIZE, null);

        display.getGraphics().drawImage(buffer, 0, 0, null);
    }

    private void update()
    {
        long now = System.currentTimeMillis();
        double delta = (now-time)/1000.0;
        time = now;
        bounceX += xvel*delta;
        bounceY += yvel*delta;
        if(bounceX > WIDTH - BOUNCE_SIZE)
        {
            bounceX = WIDTH - BOUNCE_SIZE;
            xvel = -xvel;
        }
        if(bounceX < BOUNCE_SIZE)
        {
            bounceX = BOUNCE_SIZE;
            xvel = -xvel;
        }
        if(bounceY > HEIGHT - BOUNCE_SIZE)
        {
            bounceY = HEIGHT - BOUNCE_SIZE;
            yvel = -yvel;
        }
        if(bounceY < BOUNCE_SIZE)
        {
            bounceY = BOUNCE_SIZE;
            yvel = -yvel;
        }
    }

    @Override
    public BufferedImage getImage()
    {
        draw();
        update();
        return display;
    }

    @Override
    public void dispose() {
        return;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

}