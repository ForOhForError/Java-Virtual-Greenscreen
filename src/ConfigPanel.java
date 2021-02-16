import javax.swing.*;

import com.github.sarxos.webcam.Webcam;
import org.json.*;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class ConfigPanel extends JPanel
{
    private JSONObject configObject;
    private final File CONFIG_FILE = new File("./config.json");

    private LinkedList<PrettyWebcam> pcams = new LinkedList<PrettyWebcam>();
    private Webcam currentCam;
    private PrettyDimension[] dims;

    private JComboBox<PrettyWebcam> camList;
    private JComboBox<PrettyDimension> dimList;

    private ActionListener dimListener;
    private ActionListener camListener;

    private JSpinner wsPortSpinner;
    private JSpinner webPortSpinner;
    private JTextField hostField;

    private JButton serverToggle;
    private boolean serverRunning = false;

    public ConfigPanel()
    {
        super();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        for(Webcam cam:Webcam.getWebcams())
        {
            pcams.add(new PrettyWebcam(cam));
        }
        pcams.add(new PrettyWebcam(new DummyWebcam()));
        if(CONFIG_FILE.exists() && CONFIG_FILE.isFile())
        {
            try
            {
                BufferedReader configInput = new BufferedReader(new FileReader(CONFIG_FILE));
                String jsonString = configInput.lines().collect(Collectors.joining());
                configObject = new JSONObject(jsonString);
                configInput.close();
            }
            catch (FileNotFoundException e)
            {
                configObject = new JSONObject();
            } catch (IOException ignored)
            {
            }
        }
        else
        {
            configObject = new JSONObject();
        }
        initDefaults();
        initGUI();
        write();
    }

    private void initGUI()
    {
        camList = new JComboBox<>();
        dimList = new JComboBox<>();

        serverToggle = new JButton("Start Server");
        serverToggle.addActionListener(event -> {
            MaskApp app = MaskApp.INSTANCE;
            if(serverRunning)
            {
                app.stopServer();
                serverToggle.setText("Start Server");
                wsPortSpinner.setEnabled(true);
                webPortSpinner.setEnabled(true);
                hostField.setEnabled(true);
                serverRunning=false;
            }
            else
            {
                boolean res = app.startServer(
                    (int)webPortSpinner.getValue(),
                    (int)wsPortSpinner.getValue(),
                    hostField.getText()
                );
                if(res)
                {
                    serverToggle.setText("Stop Server");
                    wsPortSpinner.setEnabled(false);
                    webPortSpinner.setEnabled(false);
                    hostField.setEnabled(false);
                    serverRunning = true;
                    updateFromGUI();
                    write();
                }
            }
        });

        wsPortSpinner = new JSpinner(new SpinnerNumberModel(
                1,0,65535,1)
        );
        JSpinner.NumberEditor wsEditor = new JSpinner.NumberEditor( wsPortSpinner, "#" );
        wsPortSpinner.setEditor(wsEditor);
        webPortSpinner = new JSpinner(new SpinnerNumberModel(
                1,0,65535,1)
        );
        JSpinner.NumberEditor webEditor = new JSpinner.NumberEditor( webPortSpinner, "#" );
        webPortSpinner.setEditor(webEditor);

        hostField = new JTextField();
        hostField.setText((String)configObject.get("host"));

        JButton copyAddress = new JButton("Copy Web Address");
        copyAddress.addActionListener(event -> {
            String content = String.format("http://%s:%d/",hostField.getText(),webPortSpinner.getValue());
            StringSelection stringSelection = new StringSelection(content);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });

        wsPortSpinner.setValue(configObject.get("web_socket_port"));
        webPortSpinner.setValue(configObject.get("web_port"));


        for(PrettyWebcam w:pcams)
        {
            camList.addItem(w);
        }

        camListener = event ->
        {
            Webcam sel = ((PrettyWebcam) camList.getSelectedItem()).get();
            setCam(sel);
        };
        camList.addActionListener(camListener);

        dimListener = event ->
        {
            synchronized (currentCam)
            {
                PrettyDimension sel = (PrettyDimension) dimList.getSelectedItem();
                System.out.println(event);
                if (sel != null)
                {
                    currentCam.close();
                    currentCam.setViewSize(sel);
                    currentCam.open();
                }
            }
        };
        dimList.addActionListener(dimListener);

        add(new JLabel("Camera"));
        add(camList);
        add(new JLabel("Resolution"));
        add(dimList);
        add(new JLabel("Web Port"));
        add(webPortSpinner);
        add(new JLabel("Websocket Port"));
        add(wsPortSpinner);
        add(new JLabel("Host"));
        add(hostField);
        add(serverToggle);
        add(copyAddress);

        setCam(Webcam.getDefault());
    }

    private void setCam(Webcam sel)
    {
        if(sel != currentCam)
        {
            camList.removeActionListener(camListener);
            for(int i=0; i<camList.getItemCount(); i++)
            {
                PrettyWebcam pCam = camList.getItemAt(i);
                if(sel.equals(pCam.get()))
                {
                    camList.setSelectedItem(pCam);
                    break;
                }
            }
            camList.addActionListener(camListener);
            currentCam = sel;

            dims = new PrettyDimension[sel.getViewSizes().length];
            for(int i=0;i<dims.length;i++)
            {
                dims[i] = new PrettyDimension(sel.getViewSizes()[i]);
            }

            dimList.removeActionListener(dimListener);
            dimList.removeAllItems();
            for(PrettyDimension dim:dims)
            {
                dimList.addItem(dim);
            }
            dimList.setSelectedItem(dims[dims.length-1]);
            dimList.addActionListener(dimListener);
            sel.setViewSize(dims[dims.length-1]);

            MaskApp.INSTANCE.setWebcam(sel);
        }
    }

    private void initDefaults()
    {
        if(!configObject.has("web_port"))
        {
            configObject.put("web_port",7777);
        }
        if(!configObject.has("web_socket_port"))
        {
            configObject.put("web_socket_port",7778);
        }
        if(!configObject.has("hostname"))
        {
            configObject.put("host","localhost");
        }
    }

    private void updateFromGUI()
    {
        configObject.put("web_port",webPortSpinner.getValue());
        configObject.put("web_socket_port",wsPortSpinner.getValue());
        configObject.put("host", hostField.getText());
    }

    private void write()
    {
        try
        {
            FileWriter writer = new FileWriter(CONFIG_FILE);
            writer.write(configObject.toString(4));
            writer.flush();
            writer.close();
        } catch (IOException e)
        {
            System.err.println("Config file write failed; is ./config.json writable?");
            e.printStackTrace();
        }
    }
}
