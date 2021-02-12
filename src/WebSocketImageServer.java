import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class WebSocketImageServer extends WebSocketServer
{
    private HttpServer server;
    private Set<WebSocket> connections = new HashSet<WebSocket>();

    public WebSocketImageServer(int wsport, int webport) throws IOException
    {
        super(new InetSocketAddress("localhost",wsport));
        server = HttpServer.create(new InetSocketAddress("localhost",webport), 0);
    }

    public void start() {
        server.createContext("/", new HttpHandler()
        {
            @Override
            public void handle(HttpExchange exchange) throws IOException
            {
                FileInputStream fin;
                try
                {
                    fin = new FileInputStream("./data/index.html");
                    Headers responseHeaders = exchange.getResponseHeaders();
                    responseHeaders.add("Content-type","text/html");
                    exchange.sendResponseHeaders(200,fin.available());
                    exchange.getResponseBody().write(fin.readAllBytes());
                    fin.close();
                    exchange.getResponseBody().close();
                } catch (FileNotFoundException e)
                {
                    exchange.sendResponseHeaders(400,0);
                }

            }
        });
        server.setExecutor(null);
        server.start();
        super.start();
    }

    public boolean writeImage(BufferedImage img)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] base64 = {};
        try
        {
            ImageIO.write(img, "png", baos);
            base64 = Base64.getEncoder().encode(baos.toByteArray());
        }catch (IOException ex)
        {
            try
            {
                baos.close();
            }
            catch(IOException e)
            {
            }
            return false;
        }
        for(WebSocket con:connections)
        {
            con.send(base64);
        }
        try
        {
            baos.close();
        }
        catch(IOException e)
        {
        }
        return true;
    }

    public void stop()
    {
        server.stop(0);
        try
        {
            super.stop();
        }catch(IOException e)
        {

        }
        catch(InterruptedException e)
        {

        }
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake)
    {
        connections.add(webSocket);
        System.out.println("Client connected: "+clientHandshake.getResourceDescriptor());
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b)
    {
        connections.remove(webSocket);
    }

    @Override
    public void onMessage(WebSocket webSocket, String s)
    {

    }

    @Override
    public void onError(WebSocket webSocket, Exception e)
    {
        if (webSocket != null) {
            connections.remove(webSocket);
        }
        e.printStackTrace();
    }

    @Override
    public void onStart()
    {
        System.out.println("WebSocket server up at "+this.getAddress());
    }
}
