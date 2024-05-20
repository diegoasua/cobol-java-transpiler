import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Cbact01c {
    private static final String ACCOUNT_FILE_PATH = "./data/ascii/acctdata.txt";
    private static final int APPL_AOK = 0;
    private static final int APPL_EOF = 16;

    public static void main(String[] args) {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");
        try {
            List<String> lines = Files.readAllLines(Paths.get(ACCOUNT_FILE_PATH), StandardCharsets.US_ASCII);
            for (String line : lines) {
                AccountRecord accountRecord = new AccountRecord(line);
                displayAccountRecord(accountRecord);
            }
        } catch (IOException e) {
            System.out.println("ERROR READING ACCOUNT FILE");
            displayIOStatus(e.getMessage());
            abendProgram();
        }
        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
    }

    private static void displayAccountRecord(AccountRecord accountRecord) {
        System.out.println("ACCT-ID                 : " + accountRecord.getAcctId());
        System.out.println("ACCT-ACTIVE-STATUS      : " + accountRecord.getAcctActiveStatus());
        System.out.println("ACCT-CURR-BAL           : " + accountRecord.getAcctCurrBal());
        System.out.println("ACCT-CREDIT-LIMIT       : " + accountRecord.getAcctCreditLimit());
        System.out.println("ACCT-CASH-CREDIT-LIMIT  : " + accountRecord.getAcctCashCreditLimit());
        System.out.println("ACCT-OPEN-DATE          : " + accountRecord.getAcctOpenDate());
        System.out.println("ACCT-EXPIRATION-DATE    : " + accountRecord.getAcctExpirationDate());
        System.out.println("ACCT-REISSUE-DATE       : " + accountRecord.getAcctReissueDate());
        System.out.println("ACCT-CURR-CYC-CREDIT    : " + accountRecord.getAcctCurrCycCredit());
        System.out.println("ACCT-CURR-CYC-DEBIT     : " + accountRecord.getAcctCurrCycDebit());
        System.out.println("ACCT-GROUP-ID           : " + accountRecord.getAcctGroupId());
        System.out.println("-".repeat(49));
    }

    private static void displayIOStatus(String status) {
        System.out.println("FILE STATUS IS: " + status);
    }

    private static void abendProgram() {
        System.out.println("ABENDING PROGRAM");
        System.exit(1);
    }

    static class AccountRecord {
        private long acctId;
        private String acctActiveStatus;
        private BigDecimal acctCurrBal;
        private BigDecimal acctCreditLimit;
        private BigDecimal acctCashCreditLimit;
        private String acctOpenDate;
        private String acctExpirationDate;
        private String acctReissueDate;
        private BigDecimal acctCurrCycCredit;
        private BigDecimal acctCurrCycDebit;
        private String acctAddrZip;
        private String acctGroupId;

        public AccountRecord(String line) {
            this.acctId = Long.parseLong(line.substring(0, 11).trim());
            this.acctActiveStatus = line.substring(11, 12);
            this.acctCurrBal = parseBigDecimal(line.substring(12, 24));
            this.acctCreditLimit = parseBigDecimal(line.substring(24, 36));
            this.acctCashCreditLimit = parseBigDecimal(line.substring(36, 48));
            this.acctOpenDate = line.substring(48, 58);
            this.acctExpirationDate = line.substring(58, 68);
            this.acctReissueDate = line.substring(68, 78);
            this.acctCurrCycCredit = parseBigDecimal(line.substring(78, 90));
            this.acctCurrCycDebit = parseBigDecimal(line.substring(90, 102));
            this.acctAddrZip = line.substring(102, 112);
            this.acctGroupId = line.substring(112, 122);
        }

        private BigDecimal parseBigDecimal(String value) {
            return new BigDecimal(Integer.parseInt(value.trim())).divide(BigDecimal.valueOf(100));
        }

        // Getters for all fields
        public long getAcctId() {
            return acctId;
        }

        public String getAcctActiveStatus() {
            return acctActiveStatus;
        }

        public BigDecimal getAcctCurrBal() {
            return acctCurrBal;
        }

        public BigDecimal getAcctCreditLimit() {
            return acctCreditLimit;
        }

        public BigDecimal getAcctCashCreditLimit() {
            return acctCashCreditLimit;
        }

        public String getAcctOpenDate() {
            return acctOpenDate;
        }

        public String getAcctExpirationDate() {
            return acctExpirationDate;
        }

        public String getAcctReissueDate() {
            return acctReissueDate;
        }

        public BigDecimal getAcctCurrCycCredit() {
            return acctCurrCycCredit;
        }

        public BigDecimal getAcctCurrCycDebit() {
            return acctCurrCycDebit;
        }

        public String getAcctAddrZip() {
            return acctAddrZip;
        }

        public String getAcctGroupId() {
            return acctGroupId;
        }
    }
}