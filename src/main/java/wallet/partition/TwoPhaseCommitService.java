package wallet.partition;

public class TwoPhaseCommitService {

    public static synchronized void transfer(
            WalletPartition src,
            WalletPartition dst,
            long from,
            long to,
            double amount) {

        try {
            // Phase 1: Prepare
            if (src.getAccount(from).getBalance() < amount)
                throw new IllegalStateException("Insufficient funds");

            // Phase 2: Commit
            src.getAccount(from).debit(amount);
            dst.getAccount(to).credit(amount);

        } catch (Exception e) {
            throw new RuntimeException("Transaction aborted");
        }
    }
}

