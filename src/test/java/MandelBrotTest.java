import fr.diguiet.grpc.common.utils.ImageUtils;
import fr.diguiet.grpc.image.generator.MandelBrot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MandelBrotTest {
    @Test
    public void testMandelBrot() throws IOException {
        final MandelBrot mandelBrot = MandelBrot.newInstance().build(); //16384
        Assertions.assertNotNull(mandelBrot);
        final BufferedImage image = mandelBrot.generateImage();
        Assertions.assertNotNull(image);
        final long[] imageIntensityMap = ImageUtils.getLevels(image);
        Assertions.assertNotNull(imageIntensityMap);

        System.out.println(Arrays.toString(imageIntensityMap));

        final File file = new File("image." + ImageUtils.getDefaultImgExt());
        ImageUtils.save(image, file);
        file.deleteOnExit();
    }
}
