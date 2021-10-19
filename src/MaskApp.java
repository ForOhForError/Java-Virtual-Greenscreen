import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import ai.onnxruntime.OrtException;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamLockException;

public class MaskApp extends JFrame{
	private static final long serialVersionUID = 1L;
	private static WebcamCanvas wc;
	private Webcam w;
	private Dimension lastSize = null;

	public static MaskApp INSTANCE;

	private JPanel rightSide;
	private ConfigPanel configPanel;
	private MaskStep masker;

	public void addStep(ProcessStep step)
	{
		wc.addStep(step);
	}

	public void run()
	{
		configPanel = new ConfigPanel();
		rightSide.add(configPanel);
		pack();
		setVisible(true);
		setResizable(false);
		while(true)
		{
			wc.draw();
			Dimension size = wc.getSize();
			if(lastSize == null || size.width != lastSize.width || size.height != lastSize.height)
			{
				pack();
				lastSize = size;
			}
		}
	}

	public MaskApp(String appName)
	{
		super(appName);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (Exception e) {}
		BorderLayout bl = new BorderLayout();
		setLayout(bl);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		w = new DummyWebcam();

		if(w==null)
		{
			System.exit(1);
		}

		rightSide = new JPanel();

		wc = new WebcamCanvas(w);

		add(wc,BorderLayout.CENTER);
		add(rightSide,BorderLayout.EAST);

		try
		{
			masker = new MaskStep();
		} catch (OrtException e)
		{
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		addStep(masker);
	}

	public void setWebcam(Webcam w)
	{
		synchronized(wc)
		{
			if(w != null)
			{
				wc.setWebcam(w);
				pack();
			}
		}
	}

	public Dimension getInputDim()
	{
		return w.getViewSize();
	}

	public Webcam getWebcam()
	{
		return w;
	}

	public boolean startServer(int webPort, int wsPort, String host)
	{
		return masker.startServer(webPort, wsPort, host);
	}

	public void stopServer()
	{
		masker.stopServer();
	}

	public void setFrameAverage(int n)
	{
		masker.setFrameAverage(n);
	}

	public void setBlurRadius(int n)
	{
		masker.setBlurRadius(n);
	}

	public void setShrinkEdges(int n)
	{
		masker.setErodeBorders(n);
	}

	public static void main(String[] args) throws OrtException
	{
		MaskApp app = new MaskApp("Virtual Green Screen");
		INSTANCE = app;
		app.run();
	}
}
