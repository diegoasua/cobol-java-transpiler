import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.math.MathContext;

class Cbact01c {
    /* working storage section */
    static AccountRecord accountRecord = new AccountRecord();
    static IoStatus ioStatus = new IoStatus();
    static int twoBytesBinary;
    static IoStatus04 ioStatus04 = new IoStatus04();
    static String endOfFile = "N";
    static int returnCode = 0;
    static AcctfileFile acctfileFile = new AcctfileFile();
    static int applEof = 16;
    static int applAok = 0;

    private static void run() {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");
        _0000acctfileOpen();
        while ((!endOfFile.equals("Y"))) {
            if (endOfFile.equals("N")) {
                _1000acctfileGetNext();
                if (endOfFile.equals("N")) {
                    System.out.println(accountRecord);
                }
            }
        }
        _9000acctfileClose();
        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
        return;
    }

    private static void _1000acctfileGetNext() {
        int applResult;
        if (acctfileFile.read()) {
            accountRecord = new AccountRecord(acctfileFile.record.toString());
        }
        if (acctfileFile.fileStatus.equals("0".repeat(2))) {
            applResult = 0;
            _1100displayAcctRecord();
        } else if (acctfileFile.fileStatus.equals("10")) {
            applResult = 16;
        } else {
            applResult = 12;
        }
        if ((applResult == applAok)) {

        } else if ((applResult == applEof)) {
            endOfFile = "Y";
        } else {
            System.out.println("ERROR READING ACCOUNT FILE");
            ioStatus = new IoStatus(acctfileFile.fileStatus.toString());
            _9910displayIoStatus();
            _9999abendProgram();
        }

        // ---------------------------------------------------------------*
    }

    private static void _1100displayAcctRecord() {
        System.out.println(("ACCT-ID                 :" + accountRecord.acctId));
        System.out.println(("ACCT-ACTIVE-STATUS      :" + accountRecord.acctActiveStatus));
        System.out.println(("ACCT-CURR-BAL           :" + accountRecord.acctCurrBal));
        System.out.println(("ACCT-CREDIT-LIMIT       :" + accountRecord.acctCreditLimit));
        System.out.println(("ACCT-CASH-CREDIT-LIMIT  :" + accountRecord.acctCashCreditLimit));
        System.out.println(("ACCT-OPEN-DATE          :" + accountRecord.acctOpenDate));
        System.out.println(("ACCT-EXPIRAION-DATE     :" + accountRecord.acctExpiraionDate));
        System.out.println(("ACCT-REISSUE-DATE       :" + accountRecord.acctReissueDate));
        System.out.println(("ACCT-CURR-CYC-CREDIT    :" + accountRecord.acctCurrCycCredit));
        System.out.println(("ACCT-CURR-CYC-DEBIT     :" + accountRecord.acctCurrCycDebit));
        System.out.println(("ACCT-GROUP-ID           :" + accountRecord.acctGroupId));
        System.out.println("-".repeat(49));

        // ---------------------------------------------------------------*
    }

    private static void _0000acctfileOpen() {
        int applResult = 8;
        acctfileFile.open(StandardOpenOption.READ);
        if (acctfileFile.fileStatus.equals("0".repeat(2))) {
            applResult = 0;
        } else {
            applResult = 12;
        }
        if ((applResult == applAok)) {

        } else {
            System.out.println("ERROR OPENING ACCTFILE");
            ioStatus = new IoStatus(acctfileFile.fileStatus.toString());
            _9910displayIoStatus();
            _9999abendProgram();
        }

        // ---------------------------------------------------------------*
    }

    private static void _9000acctfileClose() {
        int applResult = 8;
        acctfileFile.close();
        if (acctfileFile.fileStatus.equals("0".repeat(2))) {
            applResult = applResult - applResult;
        } else {
            applResult = 12;
        }
        if ((applResult == applAok)) {

        } else {
            System.out.println("ERROR CLOSING ACCOUNT FILE");
            ioStatus = new IoStatus(acctfileFile.fileStatus.toString());
            _9910displayIoStatus();
            _9999abendProgram();
        }

    }

    private static void _9999abendProgram() {
        int abcode;
        int timing;
        System.out.println("ABENDING PROGRAM");
        timing = 0;
        abcode = 999;
        System.exit(returnCode);
        // ****************************************************************
    }

    private static void _9910displayIoStatus() {
        if ((((!ioStatus.ioStat1.chars().allMatch(Character::isDigit))
                && (!ioStatus.ioStat2.chars().allMatch(Character::isDigit))) || ioStatus.ioStat1.equals("9"))) {
            {
                StringBuilder stringBuilder = new StringBuilder(String.format("%01d", ioStatus04.ioStatus0401));
                stringBuilder.replace(0, 1, String.format("%-1s", ioStatus.ioStat1));
                ioStatus04.ioStatus0401 = Integer.parseInt(stringBuilder.toString().trim());
            }
            twoBytesBinary = 0;
            {
                StringBuilder stringBuilder = new StringBuilder(String.format("%04d", twoBytesBinary));
                stringBuilder.replace(1, 2, String.format("%-1s", ioStatus.ioStat2));
                twoBytesBinary = Integer.parseInt(stringBuilder.toString().trim());
            }
            ioStatus04.ioStatus0403 = twoBytesBinary;
            System.out.println(("FILE STATUS IS: NNNN" + ioStatus04));
        } else {
            ioStatus04 = new IoStatus04(String.format("%-4s", "0".repeat(4)));
            {
                StringBuilder stringBuilder = new StringBuilder(String.format("%03d", ioStatus04.ioStatus0403));
                stringBuilder.replace(1, 3, ioStatus.toString());
                ioStatus04.ioStatus0403 = Integer.parseInt(stringBuilder.toString().trim());
            }
            System.out.println(("FILE STATUS IS: NNNN" + ioStatus04));
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
            initializeFromString(s);
        }

        void initializeFromString(String s) {
            this.fdAcctId = Long.parseLong(s.substring(0, 11).trim());
            this.fdAcctData = s.substring(11, 300);
        }

        void clear() {
            this.fdAcctId = 0;
            this.fdAcctData = " ".repeat(289);
        }

        FdAcctfileRec() {
        }
    }

    public static class AcctfileFile {
        FdAcctfileRec record = new FdAcctfileRec();
        Path filePath;
        SeekableByteChannel fileChannel;
        String fileStatus = " ".repeat(2);

        public AcctfileFile() {
            filePath = Paths.get("./data/ascii/acctdata.txt");
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
                ByteBuffer buf = ByteBuffer.allocate(300);
                fileChannel.read(buf);
                buf.flip();
                record = new FdAcctfileRec(StandardCharsets.US_ASCII.decode(buf).toString());
                fileChannel.read(ByteBuffer.allocate(1));
                fileStatus = "0".repeat(2);
                return true;
            } catch (java.io.IOException e) {
                fileStatus = "30";
            }
            return false;
        }

        public void write() {
            try {
                ByteBuffer buf = ByteBuffer.allocate(300);
                buf.put(record.toString().getBytes(StandardCharsets.US_ASCII));
                buf.flip();
                fileChannel.write(buf);
                fileChannel.write(ByteBuffer.wrap("\n".getBytes()));
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
        String filler1 = " ".repeat(178);

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
                    .append(String.format("%-178s", this.filler1)).toString();
        }

        AccountRecord(String s) {
            initializeFromString(s);
        }

        void initializeFromString(String s) {
            this.acctId = Long.parseLong(s.substring(0, 11).trim());
            this.acctActiveStatus = s.substring(11, 12);
            this.acctCurrBal = new BigDecimal(FormatUtils.parseSignedInt(s.substring(12, 24).trim()))
                    .divide(new BigDecimal(100), MathContext.DECIMAL128);
            this.acctCreditLimit = new BigDecimal(FormatUtils.parseSignedInt(s.substring(24, 36).trim()))
                    .divide(new BigDecimal(100), MathContext.DECIMAL128);
            this.acctCashCreditLimit = new BigDecimal(FormatUtils.parseSignedInt(s.substring(36, 48).trim()))
                    .divide(new BigDecimal(100), MathContext.DECIMAL128);
            this.acctOpenDate = s.substring(48, 58);
            this.acctExpiraionDate = s.substring(58, 68);
            this.acctReissueDate = s.substring(68, 78);
            this.acctCurrCycCredit = new BigDecimal(FormatUtils.parseSignedInt(s.substring(78, 90).trim()))
                    .divide(new BigDecimal(100), MathContext.DECIMAL128);
            this.acctCurrCycDebit = new BigDecimal(FormatUtils.parseSignedInt(s.substring(90, 102).trim()))
                    .divide(new BigDecimal(100), MathContext.DECIMAL128);
            this.acctAddrZip = s.substring(102, 112);
            this.acctGroupId = s.substring(112, 122);
            this.filler1 = s.substring(122, 300);
        }

        void clear() {
            this.acctId = 0;
            this.acctActiveStatus = " ";
            this.acctCurrBal = new BigDecimal(0);
            this.acctCreditLimit = new BigDecimal(0);
            this.acctCashCreditLimit = new BigDecimal(0);
            this.acctOpenDate = " ".repeat(10);
            this.acctExpiraionDate = " ".repeat(10);
            this.acctReissueDate = " ".repeat(10);
            this.acctCurrCycCredit = new BigDecimal(0);
            this.acctCurrCycDebit = new BigDecimal(0);
            this.acctAddrZip = " ".repeat(10);
            this.acctGroupId = " ".repeat(10);
        }

        AccountRecord() {
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
            initializeFromString(s);
        }

        void initializeFromString(String s) {
            this.ioStat1 = s.substring(0, 1);
            this.ioStat2 = s.substring(1, 2);
        }

        void clear() {
            this.ioStat1 = " ";
            this.ioStat2 = " ";
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
            initializeFromString(s);
        }

        void initializeFromString(String s) {
            this.ioStatus0401 = Integer.parseInt(s.substring(0, 1).trim());
            this.ioStatus0403 = Integer.parseInt(s.substring(1, 4).trim());
        }

        void clear() {
            this.ioStatus0401 = 0;
            this.ioStatus0403 = 0;
        }

        IoStatus04() {
        }
    }

    public static void main(String[] __args) {
        run();
    }

    /// Utilities for parsing and formatting COBOL Numerics.
    class FormatUtils {

        /// Parse a signed integer according to the 'p'-thru-'y' sign overpunch
        /// encoding.
        public static int parseSignedInt(String s) {
            char lastChar = s.charAt(s.length() - 1);
            boolean negative = Character.isLetter(lastChar);
            if (negative) {
                char lastDigit = (char) ((int) lastChar - (int) 'p' + (int) '0');
                s = '-' + s.substring(0, s.length() - 1) + lastDigit;
            }
            return Integer.parseInt(s);
        }

        /// Format a signed integer according to the 'p'-thru-'y' sign overpunch
        /// encoding.
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

}