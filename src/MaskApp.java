import ai.onnxruntime.OrtException;

import java.io.IOException;

class MaskApp
{
    public static void main(String[] args) throws OrtException
	{
        TinkerApp app = new TinkerApp("Virtual Green Screen");
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