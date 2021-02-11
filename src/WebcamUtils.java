import java.awt.Dimension;
import java.net.MalformedURLException;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamCompositeDriver;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;

public class WebcamUtils {
	public static class CompositeDriver extends WebcamCompositeDriver {

		public CompositeDriver() {
			add(new WebcamDefaultDriver());
		}
	}
	
	private static boolean INIT = false;
	
	public static Webcam chooseWebcam()
	{
		LinkedList<PrettyWebcam> pcams = new LinkedList<PrettyWebcam>();
		for(Webcam cam:Webcam.getWebcams())
		{
			pcams.add(new PrettyWebcam(cam));
		}
		pcams.add(new PrettyWebcam(new DummyWebcam()));
		
		PrettyWebcam pw = (PrettyWebcam) JOptionPane.showInputDialog(null, "Choose a webcam", "Select webcam", 
				JOptionPane.PLAIN_MESSAGE, null, 
				pcams.toArray(),Webcam.getDefault());
		if(pw==null)
		{
			return null;
		}
		Webcam w = pw.get();
		
		if(w==null)
		{
			return null;
		}
		PrettyDimension[] dims;
		
		//kill the program if the ip cam isn't up
		Thread t = new Thread()
		{
			public void run()
			{
				try {
					Thread.sleep(1000);
					System.exit(1);
				} catch (InterruptedException e) {
					return;
				}
			}
		};
		t.start();
		dims = new PrettyDimension[w.getViewSizes().length];
		t.interrupt();
		for(int i=0;i<dims.length;i++)
		{
			dims[i] = new PrettyDimension(w.getViewSizes()[i]);
		}
		
		Dimension d = (Dimension) JOptionPane.showInputDialog(null, "Choose a resolution", "Select resolution", 
				JOptionPane.PLAIN_MESSAGE, null, 
				dims,dims[dims.length-1]);
		
		if(d == null)
		{
			return null;
		}

		w.setViewSize(d);
		
		return w;
	}
}
