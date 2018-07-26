/*import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import com.aparapi.Kernel;
import com.aparapi.Range;

public class AparapiSolution{

   public static class AparapiConvolution extends Kernel{

      private byte[] inputData;

      private byte[] outputData;

      private int width;

      private int height;

      private Range range;

      float[] convMatrix3x3;

      public AparapiConvolution(BufferedImage _imageIn, BufferedImage _imageOut) {
         inputData = ((DataBufferByte) _imageIn.getRaster().getDataBuffer()).getData();
         outputData = ((DataBufferByte) _imageOut.getRaster().getDataBuffer()).getData();
         width = _imageIn.getWidth();
         height = _imageIn.getHeight();
         range = Range.create2D(width * 3, height);
         setExplicit(true);

      }

      public void processPixel(int x, int y, int w, int h) {
         float accum = 0;
         int count = 0;
         for (int dx = -3; dx < 6; dx += 3) {
            for (int dy = -1; dy < 2; dy += 1) {
               int rgb = 0xff & inputData[((y + dy) * w) + (x + dx)];
               accum += rgb * convMatrix3x3[count++];
            }
         }
         outputData[y * w + x] = (byte) Math.max(0, Math.min((int) accum, 255));
      }

      public void run() {
         int x = getGlobalId(0);
         int y = getGlobalId(1);
         int w = getGlobalSize(0);
         int h = getGlobalSize(1);
         if (x > 3 && x < (w - 3) && y > 1 && y < (h - 1)) {
            processPixel(x, y, w, h);
         } else {
            outputData[y * w + x] = inputData[(y * w) + x];
         }
      }

      public void apply(float[] _convMatrix3x3) {
         convMatrix3x3 = _convMatrix3x3;
         put(convMatrix3x3).put(inputData).execute(range).get(outputData);
      }

   }

   public static void main(final String[] _args) {
      String fileName = _args.length == 1 ? _args[0] : "Leo720p.wmv";

      float[] convMatrix3x3 = new float[] {
            0f,
            -10f,
            0f,
            -10f,
            41f,
            -10f,
            0f,
            -10f,
            0f
      };
      new JJMPEGPlayer("Aparapi - Solution", fileName, convMatrix3x3){
         AparapiConvolution kernel = null;

         @Override protected void processFrame(Graphics2D gc, float[] _convMatrix3x3, BufferedImage in, BufferedImage out) {
            if (kernel == null) {
               kernel = new AparapiConvolution(in, out);
            }
            kernel.apply(_convMatrix3x3);
         }
      };

   }
}*/