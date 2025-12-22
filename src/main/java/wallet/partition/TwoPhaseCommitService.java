package wallet.partition;

public class TwoPhaseCommitService {

    public static synchronized void transfer(
            WalletPartition sourceAccountPartition,
            WalletPartition destinationAccountPartition,
            long from,
            long to,
            double amount) {

        try {
            // Phase 1: Prepare
            if (sourceAccountPartition.getAccount(from).getBalance() < amount)
                throw new IllegalStateException("Insufficient funds");

            // Phase 2: Commit
            sourceAccountPartition.getAccount(from).debit(amount);
            destinationAccountPartition.getAccount(to).credit(amount);

        } catch (Exception e) {
            throw new RuntimeException("Transaction aborted");
        }
    }
}

