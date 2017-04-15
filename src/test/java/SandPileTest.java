import org.junit.Test;
import static org.junit.Assert.*;

public class SandPileTest {

    private static int[][] inputValue = new int[][] {
            {1, 3, 1},
            {3, 3, 3},
            {1, 3, 1}
    };

    private static int[][] outputValue = new int[][] {
            {2, 2, 2},
            {2, 2, 2},
            {2, 2, 2}
    };

    @Test
    public void testSumSandPilesSync() {
        SandPile sandPileA = SandPile.initAllThrees(3, false);
        SandPile sandPileB = SandPile.initFromInput(inputValue, false);
        SandPile sumPile = SandPile.sumSandPiles(sandPileA, sandPileB, false);
        assertArrayEquals(outputValue, sumPile.toIntArray());
    }

    @Test
    public void testSumSandPileAsync() {
        SandPile sandPileA = SandPile.initAllThrees(3, true);
        SandPile sandPileB = SandPile.initFromInput(inputValue, true);
        SandPile sumPile = SandPile.sumSandPiles(sandPileA, sandPileB, true);
        assertArrayEquals(outputValue, sumPile.toIntArray());
    }
}
