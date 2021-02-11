import fi.iki.elonen.NanoHTTPD;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class BrowserSourceServer extends NanoHTTPD
{
    private ArrayList<String> allowFiles;
    private String address;
    private MaskStep step;

    public BrowserSourceServer(MaskStep step, String address, int port)
    {
        super(address, port);
        this.step = step;
        this.address = address;
    }

    public boolean startServer()
    {
        try
        {
            super.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException e)
        {
            return false;
        }
        return true;
    }

    public void stop()
    {
        super.stop();
    }

    @Override
    public Response serve(IHTTPSession session)
    {
        String uri = session.getUri();
        if (uri.equalsIgnoreCase("/"))
        {
            FileInputStream fin;
            try
            {
                fin = new FileInputStream("./data/index.html");
            } catch (FileNotFoundException e)
            {
                Response res = newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html", "");
                return res;
            }
            String mime = "text/html";
            return newChunkedResponse(Response.Status.OK, mime, fin);
        }
        if (uri.equalsIgnoreCase("/video_feed"))
        {
            try
            {
                BufferedImage camImage = step.getFrame();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(camImage, "png", baos);
                byte[] buf = baos.toByteArray();
                ByteArrayInputStream bain = new ByteArrayInputStream(buf);
                baos.close();
                Response res = newChunkedResponse(Response.Status.OK, "image/png", bain);
                bain.close();
                return res;
            } catch (Exception e)
            {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html", "");
            }
        }
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html", "");
    }
}
