import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by travismiller on 2/18/17.
 */
public class SandPileNode {

    private final AtomicInteger amount;
    private SandPileNode topNode;
    private SandPileNode bottomNode;
    private SandPileNode leftNode;
    private SandPileNode rightNode;

    public SandPileNode(int amount) {
        this.amount = new AtomicInteger(amount);
    }

    public void setAmount(int amount) {
        this.amount.set(amount);
    }

    public int getAmount() {
        return amount.get();
    }

    public void setTopNode(SandPileNode topNode) {
        this.topNode = topNode;
    }

    public void setBottomNode(SandPileNode bottomNode) {
        this.bottomNode = bottomNode;
    }

    public void setLeftNode(SandPileNode leftNode) {
        this.leftNode = leftNode;
    }

    public void setRightNode(SandPileNode rightNode) {
        this.rightNode = rightNode;
    }

    public boolean hasOverflow () {
        return amount.get() > 3;
    }

    private void addSand() {
        amount.incrementAndGet();
    }

    private void addAmountOfSand(int sumAmount) {
        amount.addAndGet(sumAmount);
    }

    public Set<SandPileNode> overflowSand() {
        Set<SandPileNode> overflowNodes = new HashSet<>();

        int overflowAmount = amount.get() / 4;
        amount.set(amount.get() % 4);

        if (hasOverflow()) {
            overflowNodes.add(this);
        }

        addSandToNode(overflowAmount, topNode, overflowNodes);
        addSandToNode(overflowAmount, bottomNode, overflowNodes);
        addSandToNode(overflowAmount, leftNode, overflowNodes);
        addSandToNode(overflowAmount, rightNode, overflowNodes);

        return overflowNodes;
    }

    private void addSandToNode(int overflowAmount, SandPileNode node, Set<SandPileNode> overflowNodes) {
        if (node != null) {
            boolean wasOverflow = node.hasOverflow();
            node.addAmountOfSand(overflowAmount);
            if (!wasOverflow && node.hasOverflow()) {
                overflowNodes.add(node);
            }
        }
    }
}
