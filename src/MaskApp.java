import ai.onnxruntime.OrtException;

class MaskApp
{
    public static void main(String[] args) throws OrtException
	{
        TinkerApp app = new TinkerApp("Virtual Green Screen");
        app.addStep(new MaskStep());
		app.run();
	}
}