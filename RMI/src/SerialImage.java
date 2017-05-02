import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerialImage implements Serializable{

    private transient BufferedImage bufferedImage;

    public SerialImage(BufferedImage bi) {
        bufferedImage = bi;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ImageIO.write(bufferedImage, "png", out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        bufferedImage = ImageIO.read(in);
    }
}
