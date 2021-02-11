import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamLockException;

public class TinkerApp extends JFrame{
	private static final long serialVersionUID = 1L;
	private static WebcamCanvas wc;
	private Webcam w;
	private Dimension lastSize = null;

	public void addStep(ProcessStep step)
	{
		wc.addStep(step);
	}

	public void run()
	{
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

	public TinkerApp(String appName)
	{
		super(appName);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (Exception e) {}
		BorderLayout bl = new BorderLayout();
		setLayout(bl);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		w = WebcamUtils.chooseWebcam();

		if(w==null)
		{
			System.exit(1);
		}

		JPanel right = new JPanel();
		right.setLayout(new GridLayout(2,1));

		wc = new WebcamCanvas(w);

		try{
			w.open(true);
		}catch(WebcamLockException e)
		{
			JOptionPane.showMessageDialog(null, "Webcam already in use. Exiting.");
			System.exit(0);
		}

		add(wc,BorderLayout.CENTER);
		add(right,BorderLayout.EAST);
		pack();
	}

	public void doSetWebcam()
	{
		synchronized(wc)
		{
			Webcam w = WebcamUtils.chooseWebcam();
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
}
