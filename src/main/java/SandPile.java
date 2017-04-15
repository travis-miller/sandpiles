import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by travismiller on 2/18/17.
 */
public class SandPile {

    public static SandPile initAllThrees(int pileSize, boolean async) {
        SandPile sandPile = new SandPile(pileSize, async);
        SandPileNode[][] sandPileNodes = sandPile.getSandPiles();

        for (int i = 0; i < pileSize; i++) {
            for (int j = 0; j < pileSize; j++) {
                SandPileNode newNode = new SandPileNode(3);
                sandPileNodes[i][j] = newNode;
                if (i > 0) {
                    SandPileNode topNode = sandPileNodes[i-1][j];
                    newNode.setTopNode(topNode);
                    topNode.setBottomNode(newNode);
                }

                if (j > 0) {
                    SandPileNode leftNode = sandPileNodes[i][j-1];
                    newNode.setLeftNode(leftNode);
                    leftNode.setRightNode(newNode);
                }
            }
        }

        return sandPile;
    }

    public static SandPile initFromInput(int[][] inputValues, boolean async) {
        int pileSize = inputValues.length;
        SandPile sandPile = new SandPile(pileSize, async);
        SandPileNode[][] sandPileNodes = sandPile.getSandPiles();

        for (int i = 0; i < pileSize; i++) {
            for (int j = 0; j < pileSize; j++) {
                SandPileNode newNode = new SandPileNode(inputValues[i][j]);
                sandPileNodes[i][j] = newNode;
                if (i > 0) {
                    SandPileNode topNode = sandPileNodes[i-1][j];
                    newNode.setTopNode(topNode);
                    topNode.setBottomNode(newNode);
                }

                if (j > 0) {
                    SandPileNode leftNode = sandPileNodes[i][j-1];
                    newNode.setLeftNode(leftNode);
                    leftNode.setRightNode(newNode);
                }
            }
        }

        sandPile.resolvePile();

        return sandPile;
    }

    public static SandPile initWithLargeCenter(int pileSize, int middleValue, boolean async) {
        SandPile sandPile = new SandPile(pileSize, async);
        SandPileNode[][] sandPileNodes = sandPile.getSandPiles();

        for (int i = 0; i < pileSize; i++) {
            for (int j = 0; j < pileSize; j++) {
                SandPileNode newNode = new SandPileNode(0);
                sandPileNodes[i][j] = newNode;
                if (i > 0) {
                    SandPileNode topNode = sandPileNodes[i-1][j];
                    newNode.setTopNode(topNode);
                    topNode.setBottomNode(newNode);
                }

                if (j > 0) {
                    SandPileNode leftNode = sandPileNodes[i][j-1];
                    newNode.setLeftNode(leftNode);
                    leftNode.setRightNode(newNode);
                }
            }
        }

        sandPileNodes[pileSize/2][pileSize/2].setAmount(middleValue);
        sandPile.resolvePile();

        return sandPile;
    }

    public static SandPile sumSandPiles(SandPile firstPile, SandPile secondPile, boolean async) {
        if (firstPile.pileSize != secondPile.pileSize) {
            throw new IllegalArgumentException("The piles being summed must be the same size");
        }

        SandPileNode[][] firstSandPile = firstPile.getSandPiles();
        SandPileNode[][] secondSandPile = secondPile.getSandPiles();

        int pileSize = firstSandPile.length;
        int[][] resultSandPile = new int[pileSize][pileSize];

        for (int i = 0; i < firstSandPile.length; i++) {
            for (int j = 0; j < firstSandPile[i].length; j++) {
                resultSandPile[i][j] = firstSandPile[i][j].getAmount() + secondSandPile[i][j].getAmount();
            }
        }

        SandPile sumPile = initFromInput(resultSandPile, async);
        sumPile.resolvePile();

        return sumPile;
    }

    private final SandPileNode[][] sandPiles;
    private Set<SandPileNode> overflowNodes;
    private final int pileSize;
    private final int processorCount;
    private final CountDownLatch latch;
    private final CyclicBarrier barrier;
    private final Resolver[] resolvers;
    private final boolean async;
    private volatile boolean resolveDone = false;

    private SandPile(int pileSize, boolean async) {
        sandPiles = new SandPileNode[pileSize][pileSize];
        this.pileSize = pileSize;
        this.async = async;
        processorCount = Runtime.getRuntime().availableProcessors();
        latch = new CountDownLatch(processorCount);
        barrier = new CyclicBarrier(processorCount, () -> {
            if (overflowNodes.isEmpty()) {
                resolveDone = true;
            } else {
                addNodesToResolvers();
                overflowNodes.clear();
            }
        });

        resolvers = new Resolver[processorCount];
        for (int i = 0; i < processorCount; i++) {
            resolvers[i] = new Resolver();
        }
    }

    public SandPileNode[][] getSandPiles() {
        return sandPiles;
    }

    public int getPileSize() {
        return pileSize;
    }

    private void resolvePile() {
        if (async) {
            resolvePileAsync();
        } else {
            resolvePileSync();
        }
    }

    private void resolvePileSync() {
        System.out.println("Starting resolve pile sync");
        long startTime = System.nanoTime();
        overflowNodes = new HashSet<>();
        getOverFlowNodes(overflowNodes);

        while (!overflowNodes.isEmpty()) {
            Set<SandPileNode> newOverflowNodes = new HashSet<>();
            for (SandPileNode node : overflowNodes) {
                newOverflowNodes.addAll(node.overflowSand());
            }

            overflowNodes = newOverflowNodes;
        }

        long endTime = System.nanoTime();
        long timeInMilliSeconds = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        System.out.println("Resolve sync took " + timeInMilliSeconds);
    }

    private void resolvePileAsync() {
        System.out.println("Starting resolve pile async");
        long startTime = System.nanoTime();
        overflowNodes = Collections.newSetFromMap(new ConcurrentHashMap<SandPileNode, Boolean>());
        getOverFlowNodes(overflowNodes);

        for (Resolver resolver : resolvers) {
            new Thread(resolver).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        long timeInMilliSeconds = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        System.out.println("Resolve async took " + timeInMilliSeconds);
    }

    private void getOverFlowNodes(Set<SandPileNode> overflowNodes) {
        for (int i = 0; i < pileSize; i++) {
            for (int j = 0; j < pileSize; j++) {
                SandPileNode node = sandPiles[i][j];;
                if (node.hasOverflow()) {
                    overflowNodes.add(node);
                }
            }
        }
    }

    private void addNodesToResolvers() {
        SandPileNode[] sandPileNodes = overflowNodes.toArray(new SandPileNode[overflowNodes.size()]);

        for (Resolver resolver : resolvers) {
            resolver.clearNodes();
        }

        for (int i = 0; i < sandPileNodes.length; i++) {
            resolvers[i % processorCount].addNode(sandPileNodes[i]);
        }
    }

    public int[][] toIntArray() {
        int [][] intArray = new int[pileSize][pileSize];

        for (int i = 0; i < pileSize; i++) {
            for (int j = 0; j < pileSize; j++) {
                intArray[i][j] = sandPiles[i][j].getAmount();
            }
        }

        return intArray;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pileSize; i++) {
            for (int j = 0; j < pileSize; j++) {
                sb.append(sandPiles[i][j].getAmount());
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private class Resolver implements Runnable {

        private Set<SandPileNode> resolverNodes = new HashSet<>();

        public void addNode(SandPileNode node) {
            resolverNodes.add(node);
        }

        public void clearNodes() {
            resolverNodes.clear();
        }

        @Override
        public void run() {
            while (!resolveDone) {
                Set<SandPileNode> newOverflowNodes = new HashSet<>();
                for (SandPileNode node : resolverNodes) {
                    newOverflowNodes.addAll(node.overflowSand());
                }

                overflowNodes.addAll(newOverflowNodes);

                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    return;
                }
            }

            latch.countDown();
        }
    }
}
