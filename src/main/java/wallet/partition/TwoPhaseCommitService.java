package wallet.partition;

public class TwoPhaseCommitService {

    public static synchronized void transfer(
            WalletPartition sourceAccountPartition,
            WalletPartition destinationAccountPartition,
            long fromAcc,
            long toAcc,
            double amount) {

        try {
            // Phase 1: Prepare
            if (sourceAccountPartition.getAccount(fromAcc).getBalance() < amount)
                throw new IllegalStateException("Insufficient funds");

            // Phase 2: Commit
            sourceAccountPartition.getAccount(fromAcc).debit(amount);
            destinationAccountPartition.getAccount(toAcc).credit(amount);

        } catch (Exception e) {
            throw new RuntimeException("Transaction aborted");
        }
    }
}

