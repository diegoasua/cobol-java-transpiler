import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

class Cbtrn01c {
    /* working storage section */
    static DalytranRecord dalytranRecord = new DalytranRecord();
    static CardXrefRecord cardXrefRecord = new CardXrefRecord();
    static AccountRecord accountRecord = new AccountRecord();
    static String endOfDailyTransFile = "N";
    static int returnCode = 0;
    static DalytranFile dalytranFile = new DalytranFile();
    static CustomerFile customerFile = new CustomerFile();
    static XrefFile xrefFile = new XrefFile();
    static CardFile cardFile = new CardFile();
    static AccountFile accountFile = new AccountFile();
    static TransactFile transactFile = new TransactFile();
    static int applEof = 16;
    static int applAok = 0;

    private static void run() {
        mainPara();
    }

    private static void mainPara() {
        System.out.println("START OF EXECUTION OF PROGRAM CBTRN01C");
        _0000dalytranOpen();
        _0100custfileOpen();
        _0200xreffileOpen();
        _0300cardfileOpen();
        _0400acctfileOpen();
        _0500tranfileOpen();
        while ((!endOfDailyTransFile.equals("Y"))) {
            if (endOfDailyTransFile.equals("N")) {
                WsMiscVariables wsMiscVariables = new WsMiscVariables();
                _1000dalytranGetNext();
                if (endOfDailyTransFile.equals("N")) {
                    System.out.println(dalytranRecord);
                }
                wsMiscVariables.wsXrefReadStatus = 0;
                cardXrefRecord.xrefCardNum = dalytranRecord.dalytranCardNum;
                _2000lookupXref();
                if ((wsMiscVariables.wsXrefReadStatus == 0)) {
                    wsMiscVariables.wsAcctReadStatus = 0;
                    accountRecord.acctId = cardXrefRecord.xrefAcctId;
                    _3000readAccount();
                    if ((wsMiscVariables.wsAcctReadStatus != 0)) {
                        System.out.println((("ACCOUNT " + accountRecord.acctId) + " NOT FOUND"));
                    }
                } else {
                    System.out.println(((("CARD NUMBER " + dalytranRecord.dalytranCardNum)
                            + " COULD NOT BE VERIFIED. SKIPPING TRANSACTION ID-") + dalytranRecord.dalytranId));
                }
            }
        }
        _9000dalytranClose();
        _9100custfileClose();
        _9200xreffileClose();
        _9300cardfileClose();
        _9400acctfileClose();
        _9500tranfileClose();
        System.out.println("END OF EXECUTION OF PROGRAM CBTRN01C");
        System.exit(returnCode);
        // ****************************************************************
        // READS FILE *
        // ****************************************************************
    }

    private static void _1000dalytranGetNext() {
        int applResult;
        if (dalytranFile.read()) {
            dalytranRecord = new DalytranRecord(dalytranFile.record.toString());
        }
        if (dalytranFile.fileStatus.equals("0".repeat(2))) {
            applResult = 0;
        } else if (dalytranFile.fileStatus.equals("10")) {
            applResult = 16;
        } else {
            applResult = 12;
        }
        if ((applResult == applAok)) {

        } else if ((applResult == applEof)) {
            endOfDailyTransFile = "Y";
        } else {
            System.out.println("ERROR READING DAILY TRANSACTION FILE");
            IoStatus04 ioStatus04 = zDisplayIoStatus(new IoStatus(dalytranFile.fileStatus.toString()));
            zAbendProgram();
        }

        // ---------------------------------------------------------------*
    }

    private static void _2000lookupXref() {
        xrefFile.record.fdXrefCardNum = cardXrefRecord.xrefCardNum;
        if (xrefFile.read()) {
            cardXrefRecord = new CardXrefRecord(xrefFile.record.toString());
            System.out.println("SUCCESSFUL READ OF XREF");
            System.out.println(("CARD NUMBER: " + cardXrefRecord.xrefCardNum));
            System.out.println(("ACCOUNT ID : " + cardXrefRecord.xrefAcctId));
            System.out.println(("CUSTOMER ID: " + cardXrefRecord.xrefCustId));
        } else {
            WsMiscVariables wsMiscVariables = new WsMiscVariables();
            System.out.println("INVALID CARD NUMBER FOR XREF");
            wsMiscVariables.wsXrefReadStatus = 4;
        }
        // ---------------------------------------------------------------*
    }

    private static void _3000readAccount() {
        accountFile.record.fdAcctId = accountRecord.acctId;
        if (accountFile.read()) {
            accountRecord = new AccountRecord(accountFile.record.toString());
            System.out.println("SUCCESSFUL READ OF ACCOUNT FILE");
        } else {
            WsMiscVariables wsMiscVariables = new WsMiscVariables();
            System.out.println("INVALID ACCOUNT NUMBER FOUND");
            wsMiscVariables.wsAcctReadStatus = 4;
        }
        // ---------------------------------------------------------------*
    }

    private static void _0000dalytranOpen() {
        int applResult = 8;
        dalytranFile.open(StandardOpenOption.READ);
        if (dalytranFile.fileStatus.equals("0".repeat(2))) {
            applResult = 0;
        } else {
            applResult = 12;
        }
        if ((applResult == applAok)) {

        } else {
            System.out.println("ERROR OPENING DAILY TRANSACTION FILE");
            IoStatus04 ioStatus04 = zDisplayIoStatus(new IoStatus(dalytranFile.fileStatus.toString()));
            zAbendProgram();
        }

        // ---------------------------------------------------------------*
    }

    private static void _0100custfileOpen() {
        int applResult = 8;
        customerFile.open(StandardOpenOption.READ);
        if (customerFile.fileStatus.equals("0".repeat(2))) {
            applResult = 0;
        } else {
            applResult = 12;
        }
        if ((applResult == applAok)) {

        } else {
            System.out.println("ERROR OPENING CUSTOMER FILE");
            IoStatus04 ioStatus04 = zDisplayIoStatus(new IoStatus(customerFile.fileStatus.toString()));
            zAbendProgram();
        }

        // ---------------------------------------------------------------*
    }

    private static void _0200xreffileOpen() {
        int applResult = 8;
        xrefFile.open(StandardOpenOption.READ);
        if (xrefFile.fileStatus.equals("0".repeat(2))) {
            applResult = 0;
        } else {
            applResult = 12;
        }
        if ((applResult == applAok)) {

        } else {
            System.out.println("ERROR OPENING CROSS REF FILE");
            IoStatus04 ioStatus04 = zDisplayIoStatus(new IoStatus(xrefFile.fileStatus.toString()));
            zAbendProgram();
        }

        // ---------------------------------------------------------------*
    }

    private static void _0300cardfileOpen() {
        int applResult = 8;
        cardFile.open(StandardOpenOption.READ);
        if (cardFile.fileStatus.equals("0".repeat(2))) {
            applResult = 0;
        } else {
            applResult = 12;
        }
        if ((applResult == applAok)) {

        } else {
            System.out.println("ERROR OPENING CARD FILE");
            IoStatus04 ioStatus04 = zDisplayIoStatus(new IoStatus(cardFile.fileStatus.toString()));
            zAbendProgram();
        }

        // ---------------------------------------------------------------*
    }

    private static void _0400acctfileOpen() {
        int applResult = 8;
        accountFile.open(StandardOpenOption.READ);
        if (accountFile.fileStatus.equals("0".repeat(2))) {
            applResult = 0;
        } else {
            applResult = 12;
        }
        if ((applResult == applAok)) {

        } else {
            System.out.println("ERROR OPENING ACCOUNT FILE");
            IoStatus04 ioStatus04 = zDisplayIoStatus(new IoStatus(accountFile.fileStatus.toString()));
            zAbendProgram();
        }

        // ---------------------------------------------------------------*
    }

    private static void _0500tranfileOpen() {
        int applResult = 8;
        transactFile.open(StandardOpenOption.READ);
        if (transactFile.fileStatus.equals("0".repeat(2))) {
            applResult = 0;
        } else {
            applResult = 12;
        }
        if ((applResult == applAok)) {

        } else {
            System.out.println("ERROR OPENING TRANSACTION FILE");
            IoStatus04 ioStatus04 = zDisplayIoStatus(new IoStatus(transactFile.fileStatus.toString()));
            zAbendProgram();
        }

        // ---------------------------------------------------------------*
    }

    private static void _9000dalytranClose() {
        int applResult = 8;
        dalytranFile.close();
        if (dalytranFile.fileStatus.equals("0".repeat(2))) {
            applResult = 0;
        } else {
            applResult = 12;
        }
        if ((applResult == applAok)) {

        } else {
            System.out.println("ERROR CLOSING CUSTOMER FILE");
            IoStatus04 ioStatus04 = zDisplayIoStatus(new IoStatus(customerFile.fileStatus.toString()));
            zAbendProgram();
        }

        // ---------------------------------------------------------------*
    }

    private static void _9100custfileClose() {
        int applResult = 8;
        customerFile.close();
        if (customerFile.fileStatus.equals("0".repeat(2))) {
            applResult = 0;
        } else {
            applResult = 12;
        }
        if ((applResult == applAok)) {

        } else {
            System.out.println("ERROR CLOSING CUSTOMER FILE");
            IoStatus04 ioStatus04 = zDisplayIoStatus(new IoStatus(customerFile.fileStatus.toString()));
            zAbendProgram();
        }

        // ---------------------------------------------------------------*
    }

    private static void _9200xreffileClose() {
        int applResult = 8;
        xrefFile.close();
        if (xrefFile.fileStatus.equals("0".repeat(2))) {
            applResult = 0;
        } else {
            applResult = 12;
        }
        if ((applResult == applAok)) {

        } else {
            System.out.println("ERROR CLOSING CROSS REF FILE");
            IoStatus04 ioStatus04 = zDisplayIoStatus(new IoStatus(xrefFile.fileStatus.toString()));
            zAbendProgram();
        }

        // ---------------------------------------------------------------*
    }

    private static void _9300cardfileClose() {
        int applResult = 8;
        cardFile.close();
        if (cardFile.fileStatus.equals("0".repeat(2))) {
            applResult = 0;
        } else {
            applResult = 12;
        }
        if ((applResult == applAok)) {

        } else {
            System.out.println("ERROR CLOSING CARD FILE");
            IoStatus04 ioStatus04 = zDisplayIoStatus(new IoStatus(cardFile.fileStatus.toString()));
            zAbendProgram();
        }

        // ---------------------------------------------------------------*
    }

    private static void _9400acctfileClose() {
        int applResult = 8;
        accountFile.close();
        if (accountFile.fileStatus.equals("0".repeat(2))) {
            applResult = 0;
        } else {
            applResult = 12;
        }
        if ((applResult == applAok)) {

        } else {
            System.out.println("ERROR CLOSING ACCOUNT FILE");
            IoStatus04 ioStatus04 = zDisplayIoStatus(new IoStatus(accountFile.fileStatus.toString()));
            zAbendProgram();
        }

        // ---------------------------------------------------------------*
    }

    private static void _9500tranfileClose() {
        int applResult = 8;
        transactFile.close();
        if (transactFile.fileStatus.equals("0".repeat(2))) {
            applResult = 0;
        } else {
            applResult = 12;
        }
        if ((applResult == applAok)) {

        } else {
            System.out.println("ERROR CLOSING TRANSACTION FILE");
            IoStatus04 ioStatus04 = zDisplayIoStatus(new IoStatus(transactFile.fileStatus.toString()));
            zAbendProgram();
        }

    }

    private static void zAbendProgram() {
        int abcode;
        int timing;
        System.out.println("ABENDING PROGRAM");
        timing = 0;
        abcode = 999;
        System.exit(returnCode);
        // ****************************************************************
    }

    private static IoStatus04 zDisplayIoStatus(IoStatus ioStatus) {
        IoStatus04 ioStatus04 = new IoStatus04();
        if ((((!ioStatus.ioStat1.chars().allMatch(Character::isDigit))
                && (!ioStatus.ioStat2.chars().allMatch(Character::isDigit))) || ioStatus.ioStat1.equals("9"))) {
            int twoBytesBinary;
            {
                StringBuilder stringBuilder = new StringBuilder(String.format("%01d", ioStatus04.ioStatus0401));
                stringBuilder.replace(0, 1, String.format("%-1s", ioStatus.ioStat1));
                ioStatus04.ioStatus0401 = Integer.parseInt(stringBuilder.toString());
            }
            twoBytesBinary = 0;
            {
                StringBuilder stringBuilder = new StringBuilder(String.format("%04d", twoBytesBinary));
                stringBuilder.replace(1, 2, ioStatus.ioStat2);
                twoBytesBinary = Integer.parseInt(stringBuilder.toString());
            }
            ioStatus04.ioStatus0403 = twoBytesBinary;
            System.out.println(("FILE STATUS IS: NNNN" + ioStatus04));
        } else {
            ioStatus04 = new IoStatus04("0".repeat(4));
            {
                StringBuilder stringBuilder = new StringBuilder(String.format("%03d", ioStatus04.ioStatus0403));
                stringBuilder.replace(1, 3, ioStatus.toString());
                ioStatus04.ioStatus0403 = Integer.parseInt(stringBuilder.toString());
            }
            System.out.println(("FILE STATUS IS: NNNN" + ioStatus04));
        }

        return ioStatus04;
    }

    public static class FdTranRecord {
        String fdTranId = " ".repeat(16);
        String fdCustData = " ".repeat(334);

        @Override
        public String toString() {
            return new StringBuilder().append(String.format("%-16s", this.fdTranId))
                    .append(String.format("%-334s", this.fdCustData)).toString();
        }

        FdTranRecord(String s) {
            this.fdTranId = s.substring(0, 16);
            this.fdCustData = s.substring(16, 350);
        }

        FdTranRecord() {
        }
    }

    public static class DalytranFile {
        FdTranRecord record = new FdTranRecord();
        Path filePath;
        SeekableByteChannel fileChannel;
        String fileStatus = " ".repeat(2);

        public DalytranFile() {
            filePath = Paths.get("./data/ascii/dailytran.txt");
        }

        public void open(OpenOption... options) {
            try {
                fileChannel = FileChannel.open(filePath, options);
                fileStatus = "0".repeat(2);
            } catch (java.io.IOException e) {
                fileStatus = "30";
            }
        }

        public boolean read() {
            try {
                if ((fileChannel.position() == fileChannel.size())) {
                    fileStatus = "10";
                    return false;
                }
                ByteBuffer buf = ByteBuffer.allocate(350);
                fileChannel.read(buf);
                buf.flip();
                record = new FdTranRecord(StandardCharsets.US_ASCII.decode(buf).toString());

                fileStatus = "0".repeat(2);
                return true;
            } catch (java.io.IOException e) {
                fileStatus = "30";
            }
            return false;
        }

        public void write() {
            try {
                ByteBuffer buf = ByteBuffer.allocate(350);
                buf.put(record.toString().getBytes(StandardCharsets.US_ASCII));
                buf.flip();
                fileChannel.write(buf);

            } catch (java.io.IOException e) {
                fileStatus = "30";
            }
        }

        public void close() {
            try {
                fileChannel.close();
                fileStatus = "0".repeat(2);
            } catch (java.io.IOException e) {
                fileStatus = "30";
            }
        }
    }

    public static class CustomerFile {
        FdCustfileRec record = new FdCustfileRec();
        SortedMap<Integer, FdCustfileRec> allItems;
        Path filePath;
        String fileStatus = " ".repeat(2);

        public CustomerFile() {
            filePath = Paths.get("./data/ascii/custdata.txt");
        }

        public void open(OpenOption... options) {
            boolean read = Arrays.asList(options).contains(StandardOpenOption.READ);
            allItems = new TreeMap<>();
            if (read) {
                try {
                    List<String> lines = Files.readAllLines(filePath, StandardCharsets.US_ASCII);
                    fileStatus = "0".repeat(2);
                    for (String l : lines) {
                        FdCustfileRec i = new FdCustfileRec(l);
                        allItems.put(i.fdCustId, i);
                    }
                } catch (java.io.IOException e) {
                    fileStatus = "30";
                }
            } else {
                fileStatus = "0".repeat(2);
            }
        }

        public boolean read() {
            FdCustfileRec i = this.allItems.get(this.record.fdCustId);
            if ((i != null)) {
                this.record = new FdCustfileRec(i.toString());
                return true;
            }
            return false;
        }

        public void write() {
            try {
                this.allItems.put(this.record.fdCustId, new FdCustfileRec(this.record.toString()));
                List<String> lines = this.allItems.values().stream().map(FdCustfileRec::toString)
                        .collect(Collectors.toList());
                Files.write(filePath, lines);
                fileStatus = "0".repeat(2);
                return;
            } catch (java.io.IOException e) {
                fileStatus = "30";
            }
        }

        public boolean rewrite() {
            try {
                if (this.allItems.containsKey(this.record.fdCustId)) {
                    this.allItems.put(this.record.fdCustId, new FdCustfileRec(this.record.toString()));
                    List<String> lines = this.allItems.values().stream().map(FdCustfileRec::toString)
                            .collect(Collectors.toList());
                    Files.write(filePath, lines);
                    fileStatus = "0".repeat(2);
                    return true;
                }
            } catch (java.io.IOException e) {
                fileStatus = "30";
            }
            return false;
        }

        public void close() {
        }
    }

    public static class FdCustfileRec {
        int fdCustId;
        String fdCustData = " ".repeat(491);

        @Override
        public String toString() {
            return new StringBuilder().append(String.format("%09d", this.fdCustId))
                    .append(String.format("%-491s", this.fdCustData)).toString();
        }

        FdCustfileRec(String s) {
            this.fdCustId = Integer.parseInt(s.substring(0, 9));
            this.fdCustData = s.substring(9, 500);
        }

        FdCustfileRec() {
        }
    }

    public static class XrefFile {
        FdXreffileRec record = new FdXreffileRec();
        SortedMap<String, FdXreffileRec> allItems;
        Path filePath;
        String fileStatus = " ".repeat(2);

        public XrefFile() {
            filePath = Paths.get("./data/ascii/cardxref.txt");
        }

        public void open(OpenOption... options) {
            boolean read = Arrays.asList(options).contains(StandardOpenOption.READ);
            allItems = new TreeMap<>();
            if (read) {
                try {
                    List<String> lines = Files.readAllLines(filePath, StandardCharsets.US_ASCII);
                    fileStatus = "0".repeat(2);
                    for (String l : lines) {
                        FdXreffileRec i = new FdXreffileRec(l);
                        allItems.put(i.fdXrefCardNum, i);
                    }
                } catch (java.io.IOException e) {
                    fileStatus = "30";
                }
            } else {
                fileStatus = "0".repeat(2);
            }
        }

        public boolean read() {
            FdXreffileRec i = this.allItems.get(this.record.fdXrefCardNum);
            if ((i != null)) {
                this.record = new FdXreffileRec(i.toString());
                return true;
            }
            return false;
        }

        public void write() {
            try {
                this.allItems.put(this.record.fdXrefCardNum, new FdXreffileRec(this.record.toString()));
                List<String> lines = this.allItems.values().stream().map(FdXreffileRec::toString)
                        .collect(Collectors.toList());
                Files.write(filePath, lines);
                fileStatus = "0".repeat(2);
                return;
            } catch (java.io.IOException e) {
                fileStatus = "30";
            }
        }

        public boolean rewrite() {
            try {
                if (this.allItems.containsKey(this.record.fdXrefCardNum)) {
                    this.allItems.put(this.record.fdXrefCardNum, new FdXreffileRec(this.record.toString()));
                    List<String> lines = this.allItems.values().stream().map(FdXreffileRec::toString)
                            .collect(Collectors.toList());
                    Files.write(filePath, lines);
                    fileStatus = "0".repeat(2);
                    return true;
                }
            } catch (java.io.IOException e) {
                fileStatus = "30";
            }
            return false;
        }

        public void close() {
        }
    }

    public static class FdXreffileRec {
        String fdXrefCardNum = " ".repeat(16);
        String fdXrefData = " ".repeat(34);

        @Override
        public String toString() {
            return new StringBuilder().append(String.format("%-16s", this.fdXrefCardNum))
                    .append(String.format("%-34s", this.fdXrefData)).toString();
        }

        FdXreffileRec(String s) {
            this.fdXrefCardNum = s.substring(0, 16);
            this.fdXrefData = s.substring(16, 50);
        }

        FdXreffileRec() {
        }
    }

    public static class CardFile {
        FdCardfileRec record = new FdCardfileRec();
        SortedMap<String, FdCardfileRec> allItems;
        Path filePath;
        String fileStatus = " ".repeat(2);

        public CardFile() {
            filePath = Paths.get("./data/ascii/carddata.txt");
        }

        public void open(OpenOption... options) {
            boolean read = Arrays.asList(options).contains(StandardOpenOption.READ);
            allItems = new TreeMap<>();
            if (read) {
                try {
                    List<String> lines = Files.readAllLines(filePath, StandardCharsets.US_ASCII);
                    fileStatus = "0".repeat(2);
                    for (String l : lines) {
                        FdCardfileRec i = new FdCardfileRec(l);
                        allItems.put(i.fdCardNum, i);
                    }
                } catch (java.io.IOException e) {
                    fileStatus = "30";
                }
            } else {
                fileStatus = "0".repeat(2);
            }
        }

        public boolean read() {
            FdCardfileRec i = this.allItems.get(this.record.fdCardNum);
            if ((i != null)) {
                this.record = new FdCardfileRec(i.toString());
                return true;
            }
            return false;
        }

        public void write() {
            try {
                this.allItems.put(this.record.fdCardNum, new FdCardfileRec(this.record.toString()));
                List<String> lines = this.allItems.values().stream().map(FdCardfileRec::toString)
                        .collect(Collectors.toList());
                Files.write(filePath, lines);
                fileStatus = "0".repeat(2);
                return;
            } catch (java.io.IOException e) {
                fileStatus = "30";
            }
        }

        public boolean rewrite() {
            try {
                if (this.allItems.containsKey(this.record.fdCardNum)) {
                    this.allItems.put(this.record.fdCardNum, new FdCardfileRec(this.record.toString()));
                    List<String> lines = this.allItems.values().stream().map(FdCardfileRec::toString)
                            .collect(Collectors.toList());
                    Files.write(filePath, lines);
                    fileStatus = "0".repeat(2);
                    return true;
                }
            } catch (java.io.IOException e) {
                fileStatus = "30";
            }
            return false;
        }

        public void close() {
        }
    }

    public static class FdCardfileRec {
        String fdCardNum = " ".repeat(16);
        String fdCardData = " ".repeat(134);

        @Override
        public String toString() {
            return new StringBuilder().append(String.format("%-16s", this.fdCardNum))
                    .append(String.format("%-134s", this.fdCardData)).toString();
        }

        FdCardfileRec(String s) {
            this.fdCardNum = s.substring(0, 16);
            this.fdCardData = s.substring(16, 150);
        }

        FdCardfileRec() {
        }
    }

    public static class AccountFile {
        FdAcctfileRec record = new FdAcctfileRec();
        SortedMap<Long, FdAcctfileRec> allItems;
        Path filePath;
        String fileStatus = " ".repeat(2);

        public AccountFile() {
            filePath = Paths.get("./data/ascii/acctdata.txt");
        }

        public void open(OpenOption... options) {
            boolean read = Arrays.asList(options).contains(StandardOpenOption.READ);
            allItems = new TreeMap<>();
            if (read) {
                try {
                    List<String> lines = Files.readAllLines(filePath, StandardCharsets.US_ASCII);
                    fileStatus = "0".repeat(2);
                    for (String l : lines) {
                        FdAcctfileRec i = new FdAcctfileRec(l);
                        allItems.put(i.fdAcctId, i);
                    }
                } catch (java.io.IOException e) {
                    fileStatus = "30";
                }
            } else {
                fileStatus = "0".repeat(2);
            }
        }

        public boolean read() {
            FdAcctfileRec i = this.allItems.get(this.record.fdAcctId);
            if ((i != null)) {
                this.record = new FdAcctfileRec(i.toString());
                return true;
            }
            return false;
        }

        public void write() {
            try {
                this.allItems.put(this.record.fdAcctId, new FdAcctfileRec(this.record.toString()));
                List<String> lines = this.allItems.values().stream().map(FdAcctfileRec::toString)
                        .collect(Collectors.toList());
                Files.write(filePath, lines);
                fileStatus = "0".repeat(2);
                return;
            } catch (java.io.IOException e) {
                fileStatus = "30";
            }
        }

        public boolean rewrite() {
            try {
                if (this.allItems.containsKey(this.record.fdAcctId)) {
                    this.allItems.put(this.record.fdAcctId, new FdAcctfileRec(this.record.toString()));
                    List<String> lines = this.allItems.values().stream().map(FdAcctfileRec::toString)
                            .collect(Collectors.toList());
                    Files.write(filePath, lines);
                    fileStatus = "0".repeat(2);
                    return true;
                }
            } catch (java.io.IOException e) {
                fileStatus = "30";
            }
            return false;
        }

        public void close() {
        }
    }

    public static class FdAcctfileRec {
        long fdAcctId;
        String fdAcctData = " ".repeat(289);

        @Override
        public String toString() {
            return new StringBuilder().append(String.format("%011d", this.fdAcctId))
                    .append(String.format("%-289s", this.fdAcctData)).toString();
        }

        FdAcctfileRec(String s) {
            this.fdAcctId = Long.parseLong(s.substring(0, 11));
            this.fdAcctData = s.substring(11, 300);
        }

        FdAcctfileRec() {
        }
    }

    public static class TransactFile {
        FdTranfileRec record = new FdTranfileRec();
        SortedMap<String, FdTranfileRec> allItems;
        Path filePath;
        String fileStatus = " ".repeat(2);

        public TransactFile() {
            filePath = Paths.get("./data/out/transact.txt");
        }

        public void open(OpenOption... options) {
            boolean read = Arrays.asList(options).contains(StandardOpenOption.READ);
            allItems = new TreeMap<>();
            if (read) {
                try {
                    List<String> lines = Files.readAllLines(filePath, StandardCharsets.US_ASCII);
                    fileStatus = "0".repeat(2);
                    for (String l : lines) {
                        FdTranfileRec i = new FdTranfileRec(l);
                        allItems.put(i.fdTransId, i);
                    }
                } catch (java.io.IOException e) {
                    fileStatus = "30";
                }
            } else {
                fileStatus = "0".repeat(2);
            }
        }

        public boolean read() {
            FdTranfileRec i = this.allItems.get(this.record.fdTransId);
            if ((i != null)) {
                this.record = new FdTranfileRec(i.toString());
                return true;
            }
            return false;
        }

        public void write() {
            try {
                this.allItems.put(this.record.fdTransId, new FdTranfileRec(this.record.toString()));
                List<String> lines = this.allItems.values().stream().map(FdTranfileRec::toString)
                        .collect(Collectors.toList());
                Files.write(filePath, lines);
                fileStatus = "0".repeat(2);
                return;
            } catch (java.io.IOException e) {
                fileStatus = "30";
            }
        }

        public boolean rewrite() {
            try {
                if (this.allItems.containsKey(this.record.fdTransId)) {
                    this.allItems.put(this.record.fdTransId, new FdTranfileRec(this.record.toString()));
                    List<String> lines = this.allItems.values().stream().map(FdTranfileRec::toString)
                            .collect(Collectors.toList());
                    Files.write(filePath, lines);
                    fileStatus = "0".repeat(2);
                    return true;
                }
            } catch (java.io.IOException e) {
                fileStatus = "30";
            }
            return false;
        }

        public void close() {
        }
    }

    public static class FdTranfileRec {
        String fdTransId = " ".repeat(16);
        String fdAcctData = " ".repeat(334);

        @Override
        public String toString() {
            return new StringBuilder().append(String.format("%-16s", this.fdTransId))
                    .append(String.format("%-334s", this.fdAcctData)).toString();
        }

        FdTranfileRec(String s) {
            this.fdTransId = s.substring(0, 16);
            this.fdAcctData = s.substring(16, 350);
        }

        FdTranfileRec() {
        }
    }

    public static class DalytranRecord {
        String dalytranId = " ".repeat(16);
        String dalytranTypeCd = " ".repeat(2);
        int dalytranCatCd;
        String dalytranSource = " ".repeat(10);
        String dalytranDesc = " ".repeat(100);
        BigDecimal dalytranAmt = new BigDecimal(0);
        int dalytranMerchantId;
        String dalytranMerchantName = " ".repeat(50);
        String dalytranMerchantCity = " ".repeat(50);
        String dalytranMerchantZip = " ".repeat(10);
        String dalytranCardNum = " ".repeat(16);
        String dalytranOrigTs = " ".repeat(26);
        String dalytranProcTs = " ".repeat(26);
        String filler1 = " ".repeat(20);

        @Override
        public String toString() {
            return new StringBuilder().append(String.format("%-16s", this.dalytranId))
                    .append(String.format("%-2s", this.dalytranTypeCd))
                    .append(String.format("%04d", this.dalytranCatCd))
                    .append(String.format("%-10s", this.dalytranSource))
                    .append(String.format("%-100s", this.dalytranDesc))
                    .append(FormatUtils.formatSignedInt(
                            String.format("%011d", this.dalytranAmt.multiply(new BigDecimal(100)).intValue())))
                    .append(String.format("%09d", this.dalytranMerchantId))
                    .append(String.format("%-50s", this.dalytranMerchantName))
                    .append(String.format("%-50s", this.dalytranMerchantCity))
                    .append(String.format("%-10s", this.dalytranMerchantZip))
                    .append(String.format("%-16s", this.dalytranCardNum))
                    .append(String.format("%-26s", this.dalytranOrigTs))
                    .append(String.format("%-26s", this.dalytranProcTs)).append(String.format("%-20s", this.filler1))
                    .toString();
        }

        DalytranRecord(String s) {
            this.dalytranId = s.substring(0, 16);
            this.dalytranTypeCd = s.substring(16, 18);
            this.dalytranCatCd = Integer.parseInt(s.substring(18, 22));
            this.dalytranSource = s.substring(22, 32);
            this.dalytranDesc = s.substring(32, 132);
            this.dalytranAmt = new BigDecimal(FormatUtils.parseSignedInt(s.substring(132, 143)))
                    .divide(new BigDecimal(100));
            this.dalytranMerchantId = Integer.parseInt(s.substring(143, 152));
            this.dalytranMerchantName = s.substring(152, 202);
            this.dalytranMerchantCity = s.substring(202, 252);
            this.dalytranMerchantZip = s.substring(252, 262);
            this.dalytranCardNum = s.substring(262, 278);
            this.dalytranOrigTs = s.substring(278, 304);
            this.dalytranProcTs = s.substring(304, 330);
            this.filler1 = s.substring(330, 350);
        }

        DalytranRecord() {
        }
    }

    public static class CustomerRecord {
        int custId;
        String custFirstName = " ".repeat(25);
        String custMiddleName = " ".repeat(25);
        String custLastName = " ".repeat(25);
        String custAddrLine1 = " ".repeat(50);
        String custAddrLine2 = " ".repeat(50);
        String custAddrLine3 = " ".repeat(50);
        String custAddrStateCd = " ".repeat(2);
        String custAddrCountryCd = " ".repeat(3);
        String custAddrZip = " ".repeat(10);
        String custPhoneNum1 = " ".repeat(15);
        String custPhoneNum2 = " ".repeat(15);
        int custSsn;
        String custGovtIssuedId = " ".repeat(20);
        String custDobYyyyMmDd = " ".repeat(10);
        String custEftAccountId = " ".repeat(10);
        String custPriCardHolderInd = " ";
        int custFicoCreditScore;
        String filler2 = " ".repeat(168);

        @Override
        public String toString() {
            return new StringBuilder().append(String.format("%09d", this.custId))
                    .append(String.format("%-25s", this.custFirstName))
                    .append(String.format("%-25s", this.custMiddleName))
                    .append(String.format("%-25s", this.custLastName))
                    .append(String.format("%-50s", this.custAddrLine1))
                    .append(String.format("%-50s", this.custAddrLine2))
                    .append(String.format("%-50s", this.custAddrLine3))
                    .append(String.format("%-2s", this.custAddrStateCd))
                    .append(String.format("%-3s", this.custAddrCountryCd))
                    .append(String.format("%-10s", this.custAddrZip)).append(String.format("%-15s", this.custPhoneNum1))
                    .append(String.format("%-15s", this.custPhoneNum2)).append(String.format("%09d", this.custSsn))
                    .append(String.format("%-20s", this.custGovtIssuedId))
                    .append(String.format("%-10s", this.custDobYyyyMmDd))
                    .append(String.format("%-10s", this.custEftAccountId))
                    .append(String.format("%-1s", this.custPriCardHolderInd))
                    .append(String.format("%03d", this.custFicoCreditScore))
                    .append(String.format("%-168s", this.filler2)).toString();
        }

        CustomerRecord(String s) {
            this.custId = Integer.parseInt(s.substring(0, 9));
            this.custFirstName = s.substring(9, 34);
            this.custMiddleName = s.substring(34, 59);
            this.custLastName = s.substring(59, 84);
            this.custAddrLine1 = s.substring(84, 134);
            this.custAddrLine2 = s.substring(134, 184);
            this.custAddrLine3 = s.substring(184, 234);
            this.custAddrStateCd = s.substring(234, 236);
            this.custAddrCountryCd = s.substring(236, 239);
            this.custAddrZip = s.substring(239, 249);
            this.custPhoneNum1 = s.substring(249, 264);
            this.custPhoneNum2 = s.substring(264, 279);
            this.custSsn = Integer.parseInt(s.substring(279, 288));
            this.custGovtIssuedId = s.substring(288, 308);
            this.custDobYyyyMmDd = s.substring(308, 318);
            this.custEftAccountId = s.substring(318, 328);
            this.custPriCardHolderInd = s.substring(328, 329);
            this.custFicoCreditScore = Integer.parseInt(s.substring(329, 332));
            this.filler2 = s.substring(332, 500);
        }

        CustomerRecord() {
        }
    }

    public static class CardXrefRecord {
        String xrefCardNum = " ".repeat(16);
        int xrefCustId;
        long xrefAcctId;
        String filler3 = " ".repeat(14);

        @Override
        public String toString() {
            return new StringBuilder().append(String.format("%-16s", this.xrefCardNum))
                    .append(String.format("%09d", this.xrefCustId)).append(String.format("%011d", this.xrefAcctId))
                    .append(String.format("%-14s", this.filler3)).toString();
        }

        CardXrefRecord(String s) {
            this.xrefCardNum = s.substring(0, 16);
            this.xrefCustId = Integer.parseInt(s.substring(16, 25));
            this.xrefAcctId = Long.parseLong(s.substring(25, 36));
            this.filler3 = s.substring(36, 50);
        }

        CardXrefRecord() {
        }
    }

    public static class CardRecord {
        String cardNum = " ".repeat(16);
        long cardAcctId;
        int cardCvvCd;
        String cardEmbossedName = " ".repeat(50);
        String cardExpiraionDate = " ".repeat(10);
        String cardActiveStatus = " ";
        String filler4 = " ".repeat(59);

        @Override
        public String toString() {
            return new StringBuilder().append(String.format("%-16s", this.cardNum))
                    .append(String.format("%011d", this.cardAcctId)).append(String.format("%03d", this.cardCvvCd))
                    .append(String.format("%-50s", this.cardEmbossedName))
                    .append(String.format("%-10s", this.cardExpiraionDate))
                    .append(String.format("%-1s", this.cardActiveStatus)).append(String.format("%-59s", this.filler4))
                    .toString();
        }

        CardRecord(String s) {
            this.cardNum = s.substring(0, 16);
            this.cardAcctId = Long.parseLong(s.substring(16, 27));
            this.cardCvvCd = Integer.parseInt(s.substring(27, 30));
            this.cardEmbossedName = s.substring(30, 80);
            this.cardExpiraionDate = s.substring(80, 90);
            this.cardActiveStatus = s.substring(90, 91);
            this.filler4 = s.substring(91, 150);
        }

        CardRecord() {
        }
    }

    public static class AccountRecord {
        long acctId;
        String acctActiveStatus = " ";
        BigDecimal acctCurrBal = new BigDecimal(0);
        BigDecimal acctCreditLimit = new BigDecimal(0);
        BigDecimal acctCashCreditLimit = new BigDecimal(0);
        String acctOpenDate = " ".repeat(10);
        String acctExpiraionDate = " ".repeat(10);
        String acctReissueDate = " ".repeat(10);
        BigDecimal acctCurrCycCredit = new BigDecimal(0);
        BigDecimal acctCurrCycDebit = new BigDecimal(0);
        String acctAddrZip = " ".repeat(10);
        String acctGroupId = " ".repeat(10);
        String filler5 = " ".repeat(178);

        @Override
        public String toString() {
            return new StringBuilder().append(String.format("%011d", this.acctId))
                    .append(String.format("%-1s", this.acctActiveStatus))
                    .append(FormatUtils.formatSignedInt(
                            String.format("%012d", this.acctCurrBal.multiply(new BigDecimal(100)).intValue())))
                    .append(FormatUtils.formatSignedInt(
                            String.format("%012d", this.acctCreditLimit.multiply(new BigDecimal(100)).intValue())))
                    .append(FormatUtils.formatSignedInt(
                            String.format("%012d", this.acctCashCreditLimit.multiply(new BigDecimal(100)).intValue())))
                    .append(String.format("%-10s", this.acctOpenDate))
                    .append(String.format("%-10s", this.acctExpiraionDate))
                    .append(String.format("%-10s", this.acctReissueDate))
                    .append(FormatUtils.formatSignedInt(
                            String.format("%012d", this.acctCurrCycCredit.multiply(new BigDecimal(100)).intValue())))
                    .append(FormatUtils.formatSignedInt(
                            String.format("%012d", this.acctCurrCycDebit.multiply(new BigDecimal(100)).intValue())))
                    .append(String.format("%-10s", this.acctAddrZip)).append(String.format("%-10s", this.acctGroupId))
                    .append(String.format("%-178s", this.filler5)).toString();
        }

        AccountRecord(String s) {
            this.acctId = Long.parseLong(s.substring(0, 11));
            this.acctActiveStatus = s.substring(11, 12);
            this.acctCurrBal = new BigDecimal(FormatUtils.parseSignedInt(s.substring(12, 24)))
                    .divide(new BigDecimal(100));
            this.acctCreditLimit = new BigDecimal(FormatUtils.parseSignedInt(s.substring(24, 36)))
                    .divide(new BigDecimal(100));
            this.acctCashCreditLimit = new BigDecimal(FormatUtils.parseSignedInt(s.substring(36, 48)))
                    .divide(new BigDecimal(100));
            this.acctOpenDate = s.substring(48, 58);
            this.acctExpiraionDate = s.substring(58, 68);
            this.acctReissueDate = s.substring(68, 78);
            this.acctCurrCycCredit = new BigDecimal(FormatUtils.parseSignedInt(s.substring(78, 90)))
                    .divide(new BigDecimal(100));
            this.acctCurrCycDebit = new BigDecimal(FormatUtils.parseSignedInt(s.substring(90, 102)))
                    .divide(new BigDecimal(100));
            this.acctAddrZip = s.substring(102, 112);
            this.acctGroupId = s.substring(112, 122);
            this.filler5 = s.substring(122, 300);
        }

        AccountRecord() {
        }
    }

    public static class TranRecord {
        String tranId = " ".repeat(16);
        String tranTypeCd = " ".repeat(2);
        int tranCatCd;
        String tranSource = " ".repeat(10);
        String tranDesc = " ".repeat(100);
        BigDecimal tranAmt = new BigDecimal(0);
        int tranMerchantId;
        String tranMerchantName = " ".repeat(50);
        String tranMerchantCity = " ".repeat(50);
        String tranMerchantZip = " ".repeat(10);
        String tranCardNum = " ".repeat(16);
        String tranOrigTs = " ".repeat(26);
        String tranProcTs = " ".repeat(26);
        String filler6 = " ".repeat(20);

        @Override
        public String toString() {
            return new StringBuilder().append(String.format("%-16s", this.tranId))
                    .append(String.format("%-2s", this.tranTypeCd)).append(String.format("%04d", this.tranCatCd))
                    .append(String.format("%-10s", this.tranSource)).append(String.format("%-100s", this.tranDesc))
                    .append(FormatUtils.formatSignedInt(
                            String.format("%011d", this.tranAmt.multiply(new BigDecimal(100)).intValue())))
                    .append(String.format("%09d", this.tranMerchantId))
                    .append(String.format("%-50s", this.tranMerchantName))
                    .append(String.format("%-50s", this.tranMerchantCity))
                    .append(String.format("%-10s", this.tranMerchantZip))
                    .append(String.format("%-16s", this.tranCardNum)).append(String.format("%-26s", this.tranOrigTs))
                    .append(String.format("%-26s", this.tranProcTs)).append(String.format("%-20s", this.filler6))
                    .toString();
        }

        TranRecord(String s) {
            this.tranId = s.substring(0, 16);
            this.tranTypeCd = s.substring(16, 18);
            this.tranCatCd = Integer.parseInt(s.substring(18, 22));
            this.tranSource = s.substring(22, 32);
            this.tranDesc = s.substring(32, 132);
            this.tranAmt = new BigDecimal(FormatUtils.parseSignedInt(s.substring(132, 143)))
                    .divide(new BigDecimal(100));
            this.tranMerchantId = Integer.parseInt(s.substring(143, 152));
            this.tranMerchantName = s.substring(152, 202);
            this.tranMerchantCity = s.substring(202, 252);
            this.tranMerchantZip = s.substring(252, 262);
            this.tranCardNum = s.substring(262, 278);
            this.tranOrigTs = s.substring(278, 304);
            this.tranProcTs = s.substring(304, 330);
            this.filler6 = s.substring(330, 350);
        }

        TranRecord() {
        }
    }

    public static class IoStatus {
        String ioStat1 = " ";
        String ioStat2 = " ";

        @Override
        public String toString() {
            return new StringBuilder().append(String.format("%-1s", this.ioStat1))
                    .append(String.format("%-1s", this.ioStat2)).toString();
        }

        IoStatus(String s) {
            this.ioStat1 = s.substring(0, 1);
            this.ioStat2 = s.substring(1, 2);
        }

        IoStatus() {
        }
    }

    public static class IoStatus04 {
        int ioStatus0401 = 0;
        int ioStatus0403 = 0;

        @Override
        public String toString() {
            return new StringBuilder().append(String.format("%01d", this.ioStatus0401))
                    .append(String.format("%03d", this.ioStatus0403)).toString();
        }

        IoStatus04(String s) {
            this.ioStatus0401 = Integer.parseInt(s.substring(0, 1));
            this.ioStatus0403 = Integer.parseInt(s.substring(1, 4));
        }

        IoStatus04() {
        }
    }

    public static class WsMiscVariables {
        int wsXrefReadStatus;
        int wsAcctReadStatus;

        @Override
        public String toString() {
            return new StringBuilder().append(String.format("%04d", this.wsXrefReadStatus))
                    .append(String.format("%04d", this.wsAcctReadStatus)).toString();
        }

        WsMiscVariables(String s) {
            this.wsXrefReadStatus = Integer.parseInt(s.substring(0, 4));
            this.wsAcctReadStatus = Integer.parseInt(s.substring(4, 8));
        }

        WsMiscVariables() {
        }
    }

    public static void main(String[] __args) {
        run();
    }
}

class FormatUtils {

    public static int parseSignedInt(String s) {
        char lastChar = s.charAt(s.length() - 1);
        boolean negative = Character.isLetter(lastChar);
        if (negative) {
            char lastDigit = (char) ((int) lastChar - (int) 'p' + (int) '0');
            s = '-' + s.substring(0, s.length() - 1) + lastDigit;
        }
        return Integer.parseInt(s);
    }

    public static String formatSignedInt(String s) {
        boolean negative = s.startsWith("-");
        if (!negative) {
            return s;
        }
        s = s.replace('-', '0');
        char lastDigit = s.charAt(s.length() - 1);
        char lastChar = (char) ((int) lastDigit - (int) '0' + (int) 'p');
        return s.substring(0, s.length() - 1) + lastChar;
    }
}
