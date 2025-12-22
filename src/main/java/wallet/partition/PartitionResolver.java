package wallet.partition;

public class PartitionResolver {

    private final int partitions;

    public PartitionResolver(int partitions) {
        this.partitions = partitions;
    }

    public int resolvePartitionId(long accountNumber) {
        return (int) (accountNumber % partitions);
    }
}


