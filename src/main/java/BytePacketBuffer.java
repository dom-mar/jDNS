import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BytePacketBuffer {
    private byte[] buffer;
    private int position;

    /*
     * Constructor that sets new empty buffer of size 512 B
     * and initializes position to 0 */
    public BytePacketBuffer() {
        this.buffer = new byte[512];
        this.position = 0;
    }

    /*
     * Returns current position inside buffer */
    public int getPosition() {
        return this.position;
    }

    /*
     * Forwards position inside buffer for given number of steps */
    public void forward(int steps) {
        this.position += steps;
    }

    /*
     * Jumps to given position inside buffer */
    public void jump(int position) {
        this.position = position;
    }

    /*
     * Reads one byte and forwards position by one */
    public byte readOne() throws ArrayIndexOutOfBoundsException {
        if (this.position >= 512) throw new ArrayIndexOutOfBoundsException("End of buffer");
        // Returns byte and increments position
        return buffer[this.position++];
    }

    /*
     * Gets current byte without changing position */
    public byte getOne(int position) throws ArrayIndexOutOfBoundsException {
        if (position >= 512) throw new ArrayIndexOutOfBoundsException("End of buffer");
        return buffer[position];
    }

    /*
     * Gets range of bytes from start position to start + length - 1 */
    public byte[] getRange(int start, int length) throws ArrayIndexOutOfBoundsException {
        if (start + length >= 512) throw new ArrayIndexOutOfBoundsException("End of buffer");
        return Arrays.copyOfRange(this.buffer, start, start + length);
    }

    /*
     * Reads two bytes and forwards position by two */
    public short readTwo() {
        short result = (short) readOne();
        // Shift first byte to upper bits
        result <<= 8;
        // Read second byte
        short temp = (short) readOne();
        // Remove first 8 bits in short
        temp &= 0x00FF;
        // Bitwise OR between first byte and second byte
        result |= temp;

        return result;
    }

    /*
     * Reads four bytes and forwards position by four */
    public int readFour() {
        int result = (int) readTwo();
        result <<= 16;
        int temp = (int) readTwo();
        temp &= 0x0000FFFF;
        result |= temp;
        return result;
    }

    /*
     * Reads a query domain name taking labels and their lengths in consideration.
     * Name of domain which is being queried is encoded as sequence of labels
     * and each label is preceded by byte specifying its length. Domain encoding
     * ends with label of 0 length.
     *
     * For example tel.fer.unizg.hr would be represented as:
     * [3]tel[3]fer[5]unizg[2]hr
     *
     * UDP packet has maximum size of 512 bytes and because of that compression is
     * needed. Compression scheme eliminates repetition of domain names by replacing
     * it with a pointer to prior occurrence. If pointer is used first two bits are
     * ones. After that comes OFFSET which specifies offset from the first octet of
     * the header.
     *     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *     | 1  1|                OFFSET                   |
     *     +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     * */
    public String readQName() throws Exception {
        // Saves local position independently
        int position = this.position;
        boolean jumped = false;

        // Allows maximum of 10 jumps because it is possible to craft packet in
        // such a way that would invoke endless loop.
        final int MAX_JUMPS = 10;
        int jumpCnt = 0;

        // Delimiter is empty and it will be set to dot after reading first label.
        String delimiter = "";
        String result = "";

        while (true) {
            // Avoiding endless loop by checking No. of preformed jumps
            if (jumpCnt > MAX_JUMPS) throw new Exception("Jump limit [" + MAX_JUMPS + "] exceeded");

            // Retrieving length of domain label
            byte length = getOne(position);

            // Max length of label is defined as 63 octets which is 0b0011 1111
            // so if label has two most significant bits set it means
            // that jump is necessary.
            // Checking it by doing bitwise AND between length and 0b1100 0000
            // and comparing with 0b1100 0000 aka 0xC0
            if ((length & 0xC0) == 0xC0) {
                // Forward buffer position past the current label
                if (!jumped) jump(position + 2);

                // OFFSET field is 2 octets long so we need another byte
                short nextByte = (short) getOne(position + 1);

                // Bitwise exclusive OR removing first two most significant bits
                short offset = (short) ((short) length ^ 0xC0);

                //Shift length to upper bits
                offset <<= 8;

                //Calculate final offset with bitwise OR using following byte
                offset |= nextByte;

                //Set new local position
                position = (int) offset;

                jumped = true;
                jumpCnt++;

            } else {
                // Forward by one octet past length octet onto start of the label
                position++;

                // Length of zero octets indicates end of domain name
                if (length == 0) break;

                result += delimiter;

                // Retrieve bytes representing current label
                byte[] resultBuffer = getRange(position, (int) length);

                // Append bytes as ASCII chars to result
                result += new String(resultBuffer, StandardCharsets.US_ASCII);
                delimiter = ".";

                // Forward local position for the entire length of the label
                position += (int) length;
            }
        }
        // If no jump was preformed set buffer to current local position
        if (!jumped) jump(position);

        return result;

    }

    /*
     * Writes one byte to buffer and forwards position */
    public void writeOne(byte value) {
        if (this.position >= 512) throw new ArrayIndexOutOfBoundsException("End of buffer");
        // Set value to current position and forward position by one
        this.buffer[this.position++] = value;
    }

    /*
     * Writes two bytes and forwards position by two */
    public void writeTwo(short value) {
        writeOne((byte) (value >>> 8));
        writeOne((byte) (value & 0xFF));
    }

    /*
     *  Writes four bytes to buffer and forwards position by four */
    public void writeFour(int value) {
        writeTwo((short) (value >>> 16));
        writeTwo((short) (value & 0xFFFF));
    }

    /*
     * Writes question name to buffer */
    public void writeQName(String qname) {
        // Split name into labels
        for (String label : qname.split("\\.")) {
            byte length = (byte) label.length();
            // Check if longer than 63 chars
            if (length > 0x3F) throw new IllegalArgumentException("Label " + label + " longer than 63 chars");

            // Write length of label followed by label
            writeOne(length);
            for (byte lb : label.getBytes(StandardCharsets.US_ASCII)) {
                writeOne(lb);
            }
        }

        // Name ends with byte of len 0
        writeOne((byte) 0b0);
    }

    /*
     * Getter for buffer */
    public byte[] getBuffer() {
        return buffer;
    }

    /*
     * Setter for buffer */
    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

}
