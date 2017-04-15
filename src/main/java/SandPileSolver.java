import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by travismiller on 2/18/17.
 */
public class SandPileSolver {

    public static void main(String[] args) {
        SandPileSolver sandPileSolver = new SandPileSolver();
        SandPile solution = sandPileSolver.solve();
        sandPileSolver.createImage(solution);
    }

    private SandPile solve() {
        int initialMidValue = new Double(Math.pow(2, 18)).intValue();
        SandPile middlePile = SandPile.initWithLargeCenter(401, initialMidValue, true);
        return middlePile;
    }

    private void createImage(SandPile sandPile) {
        int pileSize = sandPile.getPileSize();
        BufferedImage sandPileImage = new BufferedImage(pileSize, pileSize, BufferedImage.TYPE_INT_RGB);

        SandPileNode[][] sandPiles = sandPile.getSandPiles();
        for (int i = 0; i < pileSize; i++) {
            for (int j = 0; j < pileSize; j++) {
                Color nodeColor = getNodeColor(sandPiles[i][j].getAmount());
                sandPileImage.setRGB(i, j, nodeColor.getRGB());
            }
        }

        try {
            File output = new File("sandpile.bmp");
            ImageIO.write(sandPileImage, "bmp", output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Color getNodeColor(long amount) {
        if (amount == 0) {
            return new Color(16, 54, 255);
        } else if (amount == 1) {
            return new Color(134, 191, 255);
        } else if (amount == 2) {
            return new Color(243, 221, 8);
        } else {
            return new Color(90, 0, 0);
        }
    }
}
