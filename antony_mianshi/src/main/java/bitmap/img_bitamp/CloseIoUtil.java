package bitmap.img_bitamp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CloseIoUtil {

    public static void close(Object stream) {
        if ((stream instanceof InputStream)) {
            try {
                ((InputStream) stream).close();
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if ((stream instanceof OutputStream)) {
            try {
                ((OutputStream) stream).close();
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
