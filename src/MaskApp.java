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
		rightSide.setLayout(new GridLayout(2,1));

		wc = new WebcamCanvas(w);

		add(wc,BorderLayout.CENTER);
		add(rightSide,BorderLayout.EAST);
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

	public static void main(String[] args) throws OrtException
	{
		MaskApp app = new MaskApp("Virtual Green Screen");
		INSTANCE = app;
		try
		{
			app.addStep(new MaskStep());
			app.run();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
