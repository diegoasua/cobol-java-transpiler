import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

class Cbtrn01c {
    private static final String DAILY_TRANSACTION_FILE = "./data/ascii/dailytran.txt";
    private static final String CUSTOMER_FILE = "./data/ascii/custdata.txt";
    private static final String CARD_XREF_FILE = "./data/ascii/cardxref.txt";
    private static final String CARD_FILE = "./data/ascii/carddata.txt";
    private static final String ACCOUNT_FILE = "./data/ascii/acctdata.txt";
    private static final String TRANSACTION_FILE = "./data/out/transact.txt";

    private static final int APPL_EOF = 16;
    private static final int APPL_AOK = 0;

    private DailyTransactionRecord dailyTransactionRecord = new DailyTransactionRecord();
    private CardXrefRecord cardXrefRecord = new CardXrefRecord();
    private AccountRecord accountRecord = new AccountRecord();
    private boolean endOfDailyTransFile = false;
    private int returnCode = 0;

    private FileHandler<DailyTransactionRecord> dailyTransactionFile;
    private FileHandler<CustomerRecord> customerFile;
    private FileHandler<CardXrefRecord> cardXrefFile;
    private FileHandler<CardRecord> cardFile;
    private FileHandler<AccountRecord> accountFile;
    private FileHandler<TransactionRecord> transactionFile;

    public static void main(String[] args) {
        Cbtrn01c processor = new Cbtrn01c();
        processor.run();
    }

    private void run() {
        System.out.println("START OF EXECUTION OF PROGRAM CBTRN01C");

        openFiles();

        while (!endOfDailyTransFile) {
            readNextDailyTransaction();

            if (!endOfDailyTransFile) {
                System.out.println(dailyTransactionRecord);
                processTransaction();
            }
        }

        closeFiles();

        System.out.println("END OF EXECUTION OF PROGRAM CBTRN01C");
        System.exit(returnCode);
    }

    private void openFiles() {
        dailyTransactionFile = new FileHandler<>(DAILY_TRANSACTION_FILE, DailyTransactionRecord::fromString);
        customerFile = new FileHandler<>(CUSTOMER_FILE, CustomerRecord::fromString);
        cardXrefFile = new FileHandler<>(CARD_XREF_FILE, CardXrefRecord::fromString);
        cardFile = new FileHandler<>(CARD_FILE, CardRecord::fromString);
        accountFile = new FileHandler<>(ACCOUNT_FILE, AccountRecord::fromString);
        transactionFile = new FileHandler<>(TRANSACTION_FILE, TransactionRecord::fromString);

        dailyTransactionFile.open(StandardOpenOption.READ);
        customerFile.open(StandardOpenOption.READ);
        cardXrefFile.open(StandardOpenOption.READ);
        cardFile.open(StandardOpenOption.READ);
        accountFile.open(StandardOpenOption.READ);
        transactionFile.open(StandardOpenOption.READ);
    }

    private void closeFiles() {
        dailyTransactionFile.close();
        customerFile.close();
        cardXrefFile.close();
        cardFile.close();
        accountFile.close();
        transactionFile.close();
    }

    private void readNextDailyTransaction() {
        if (dailyTransactionFile.read()) {
            dailyTransactionRecord = dailyTransactionFile.getRecord();
        } else {
            endOfDailyTransFile = true;
        }
    }

    private void processTransaction() {
        cardXrefRecord.setCardNumber(dailyTransactionRecord.getCardNumber());
        if (lookupCardXref()) {
            accountRecord.setAccountId(String.valueOf(cardXrefRecord.getAccountId()));
            if (readAccount()) {
                // Process the transaction
                // ...
            } else {
                System.out.println("ACCOUNT " + accountRecord.getAccountId() + " NOT FOUND");
            }
        } else {
            System.out.println("CARD NUMBER " + dailyTransactionRecord.getCardNumber()
                    + " COULD NOT BE VERIFIED. SKIPPING TRANSACTION ID-" + dailyTransactionRecord.getTransactionId());
        }
    }

    private boolean lookupCardXref() {
        cardXrefFile.setKey(cardXrefRecord.getCardNumber());
        if (cardXrefFile.read()) {
            cardXrefRecord = cardXrefFile.getRecord();
            System.out.println("SUCCESSFUL READ OF XREF");
            System.out.println("CARD NUMBER: " + cardXrefRecord.getCardNumber());
            System.out.println("ACCOUNT ID : " + cardXrefRecord.getAccountId());
            System.out.println("CUSTOMER ID: " + cardXrefRecord.getCustomerId());
            return true;
        } else {
            System.out.println("INVALID CARD NUMBER FOR XREF");
            return false;
        }
    }

    private boolean readAccount() {
        accountFile.setKey(accountRecord.getAccountId());
        if (accountFile.read()) {
            accountRecord = accountFile.getRecord();
            System.out.println("SUCCESSFUL READ OF ACCOUNT FILE");
            return true;
        } else {
            System.out.println("INVALID ACCOUNT NUMBER FOUND");
            return false;
        }
    }

    // Other methods and classes...
}

class FileHandler<T extends Record> {
    private Path filePath;
    private RecordParser<T> recordParser;
    private T record;
    private String key;

    public FileHandler(String filePath, RecordParser<T> recordParser) {
        this.filePath = Paths.get(filePath);
        this.recordParser = recordParser;
    }

    public void open(StandardOpenOption... options) {
        // Open the file for reading or writing
        // ...
    }

    public boolean read() {
        // Read a record from the file based on the key
        // ...
        return false;
    }

    public void write() {
        // Write the current record to the file
        // ...
    }

    public void close() {
        // Close the file
        // ...
    }

    public T getRecord() {
        return record;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

interface Record {
    // Common methods for record classes
}

class DailyTransactionRecord implements Record {
    private String transactionId;
    private String cardNumber;
    // Other fields...

    public String getTransactionId() {
        return transactionId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    // Other getters and setters...

    public static DailyTransactionRecord fromString(String line) {
        // Parse the line and create a DailyTransactionRecord object
        // ...
        return new DailyTransactionRecord();
    }
}

class CustomerRecord implements Record {
    private int customerId;
    // Other fields...

    public int getCustomerId() {
        return customerId;
    }

    // Other getters and setters...

    public static CustomerRecord fromString(String line) {
        // Parse the line and create a CustomerRecord object
        // ...
        return new CustomerRecord();
    }
}

class CardXrefRecord implements Record {
    private String cardNumber;
    private int customerId;
    private long accountId;

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public int getCustomerId() {
        return customerId;
    }

    public long getAccountId() {
        return accountId;
    }

    // Other getters and setters...

    public static CardXrefRecord fromString(String line) {
        // Parse the line and create a CardXrefRecord object
        // ...
        return new CardXrefRecord();
    }
}

class CardRecord implements Record {
    private String cardNumber;
    // Other fields...

    public String getCardNumber() {
        return cardNumber;
    }

    // Other getters and setters...

    public static CardRecord fromString(String line) {
        // Parse the line and create a CardRecord object
        // ...
        return new CardRecord();
    }
}

class AccountRecord implements Record {
    private String accountId;
    // Other fields...

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    // Other getters and setters...

    public static AccountRecord fromString(String line) {
        // Parse the line and create an AccountRecord object
        // ...
        return new AccountRecord();
    }
}

class TransactionRecord implements Record {
    private String transactionId;
    // Other fields...

    public String getTransactionId() {
        return transactionId;
    }

    // Other getters and setters...

    public static TransactionRecord fromString(String line) {
        // Parse the line and create a TransactionRecord object
        // ...
        return new TransactionRecord();
    }
}

interface RecordParser<T extends Record> {
    T parse(String line);
}