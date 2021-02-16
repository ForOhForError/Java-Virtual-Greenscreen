import javax.swing.*;

import com.github.sarxos.webcam.Webcam;
import org.json.*;

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

    public ConfigPanel()
    {
        super();
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
    }

    private void initGUI()
    {
        camList = new JComboBox<>();
        dimList = new JComboBox<>();


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
        add(camList);

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
        add(dimList);

        add(webPortSpinner);
        add(wsPortSpinner);

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
