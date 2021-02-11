import java.awt.*;
import java.awt.image.BufferedImage;

import java.awt.event.MouseEvent;

abstract class ProcessStep
{
    protected BufferedImage buf;
    protected Dimension outputDim;

    protected abstract Image process(BufferedImage in);

    protected abstract boolean handleClick(MouseEvent e);

    public BufferedImage doProcess(BufferedImage input)
    {
        prepBuf();
        Image proc = process(input);
        Graphics g = buf.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0,0, outputDim.width, outputDim.height);
        g.drawImage(proc, 0, 0, null);
        return buf;
    }

    private void prepBuf()
    {
        if(
            buf == null || 
            buf.getWidth() != outputDim.getWidth() || 
            buf.getHeight() != outputDim.getHeight()
        )
        {
            buf = new BufferedImage
            (
                (int)outputDim.getWidth(), 
                (int)outputDim.getHeight(), 
                BufferedImage.TYPE_INT_RGB
            );
        }
    }

    public Dimension getOutputDim()
    {
        return outputDim;
    }

    public void updateOutputDim(Dimension d)
    {
        this.outputDim = new Dimension(d);
    }
}