import ai.onnxruntime.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.HashMap;

import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.alg.misc.GPixelMath;
import boofcv.core.image.ConvertImage;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.ConvertRaster;
import boofcv.struct.border.BorderType;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.InterleavedF32;
import boofcv.struct.image.Planar;

import boofcv.abst.distort.FDistort;
import georegression.struct.affine.Affine2D_F64;

public class MaskStep extends ProcessStep
{
    private static String MODEL_PATH = "./data/deconv_bnoptimized_munet.onnx";
    private static long[] TENSOR_DIMS = {1, 128, 128, 3};
    private static int BUFFER_LEN = 128*128*3;

    private OrtEnvironment env;
    private OrtSession session;

    private Planar<GrayF32> inFrame;
    private Planar<GrayF32> netInput;
    private GrayF32 netOutput;
    private GrayF32 outFrame;

    private FloatBuffer fb;

    private final FDistort scaler;
    private final ConvertRaster raster;

    private BufferedImage bufferOut;

    private WebSocketImageServer server;

    public MaskStep() throws OrtException, IOException
    {
        env = OrtEnvironment.getEnvironment();
        session = env.createSession(MODEL_PATH,new OrtSession.SessionOptions());

        netInput = new Planar<GrayF32>(GrayF32.class, 128, 128, 3);

        inFrame = null;

        netOutput = new GrayF32(128,128);

        fb = FloatBuffer.allocate(BUFFER_LEN);

        outFrame = null;

        scaler = new FDistort();
        raster = new ConvertRaster();
        //server = new BrowserSourceServer(this, "localhost", 7777);

        server = new WebSocketImageServer(7778,7777);
        server.start();
    }

    @Override
    protected Image process(BufferedImage in)
    {
        //Convert to R,G,B Planar Image for Manipulation with BoofCV
        inFrame = ConvertBufferedImage.convertFromPlanar(
                in, inFrame, true, GrayF32.class
        );

        //Create buffered output
        if(bufferOut == null)
        {
            bufferOut = new BufferedImage(inFrame.width, inFrame.height, BufferedImage.TYPE_INT_ARGB);
        }

        //Create scaling transforms
        Affine2D_F64 transformIn = new Affine2D_F64();
        Affine2D_F64 transformOut = new Affine2D_F64();

        transformIn.a11 = 128.0/inFrame.width;
        transformIn.a22 = 128.0/inFrame.height;

        transformOut.a11 = inFrame.width/128.0;
        transformOut.a22 = inFrame.height/128.0;

        scaler.init(inFrame,netInput);
        scaler.border(BorderType.EXTENDED);
        scaler.affine(transformIn);
        scaler.apply();

        //Write band data to float buffer in the order the neural net expects
        fb.position(0);
        for(int x=0; x<128; x++)
        {
            for(int y=0; y<128; y++)
            {
                int ix = 128*x+y;
                for (int b = 0; b < 3; b++)
                {
                    fb.put(netInput.bands[b].data[ix]/255);
                }
            }
        }

        float[][][][] resArr;

        try
        {
            //Convert FloatBuffer to a Tensor object
            fb.position(0);
            OnnxTensor tensor = OnnxTensor.createTensor(env, fb, TENSOR_DIMS);

            //Run neural net and store results
            HashMap<String,OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_1",tensor);
            OrtSession.Result result = session.run(inputs);
            tensor.close();
            OnnxValue res = result.get("op").get();
            resArr = (float[][][][])res.getValue();
        }
        catch(OrtException ex)
        {
            return in;
        }

        //Parse result back into BoofCV format
        for(int x=0; x<128; x++)
        {
            for(int y=0; y<128; y++)
            {
                float val = resArr[0][y][x][0]*255;
                netOutput.set(x,y,val);
            }
        }
        if(outFrame == null)
        {
            outFrame = new GrayF32(inFrame.width, inFrame.height);
        }

        //Rescale network output to the size of the input frame
        scaler.init(netOutput,outFrame);
        scaler.border(BorderType.EXTENDED);
        scaler.affine(transformOut);
        scaler.apply();

        //Post process the neural network output into a binary mask
        //Gaussian blur -> Otsu binary threshold and invert -> Gaussian blur
        //Additionally convert to the float datatype, and scale the values to 0-255
        BlurImageOps.gaussian(outFrame,outFrame,-1,3,null);
        double threshold = GThresholdImageOps.computeOtsu(outFrame, 0, 255);
        GrayU8 bin = ThresholdImageOps.threshold(outFrame,null,(float)threshold,true);
        BinaryImageOps.invert(bin,bin);
        GrayF32 binFloat = ConvertImage.convert(bin, new GrayF32(bin.width, bin.height));
        BlurImageOps.gaussian(binFloat,binFloat,-1,3,null);
        GPixelMath.multiply(binFloat, 255,binFloat);

        //Add mask as alpha channel to the input frame
        inFrame.setNumberOfBands(4);
        inFrame.reorderBands(3,0,1,2);
        inFrame.bands[0].setData(binFloat.data);

        //Convert to ARGB BufferedImage output
        InterleavedF32 inter = ConvertImage.convert(inFrame, new InterleavedF32(inFrame.width, inFrame.height, 4));
        synchronized (bufferOut)
        {
            ConvertRaster.interleavedToBuffered(inter, (DataBufferInt) bufferOut.getRaster().getDataBuffer(), bufferOut.getRaster());
        }
        server.writeImage(bufferOut);
        return bufferOut;
    }

    @Override
    protected boolean handleClick(MouseEvent e)
    {
        return false;
    }

    public BufferedImage getFrame()
    {
        synchronized (bufferOut)
        {
            return bufferOut;
        }
    }
}
