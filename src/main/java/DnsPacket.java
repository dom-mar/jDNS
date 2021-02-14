import java.util.LinkedList;
import java.util.List;

public class DnsPacket {

    /*
     * Format of DNS packet is:
     *
     *    +---------------------+
     *    |        Header       |
     *    +---------------------+
     *    |       Questions     | the question for the name server
     *    +---------------------+
     *    |        Answers      | RRs answering the question
     *    +---------------------+
     *    |      Authorities    | RRs pointing toward an authority
     *    +---------------------+
     *    |      Additional     | RRs holding additional information
     *    +---------------------+
     *
     * */

    private DnsHeader header;
    private List<DnsQuestion> questions;
    private List<DnsRecord> answers;
    private List<DnsRecord> authorityRecords;
    private List<DnsRecord> resourceRecords;

    public DnsPacket() {
        this.header = new DnsHeader();
        this.questions = new LinkedList<>();
        this.answers = new LinkedList<>();
        this.authorityRecords = new LinkedList<>();
        this.resourceRecords = new LinkedList<>();
    }

    /*
     * Reads required number of question, answers, authorities and resources
     * from given buffer */
    public DnsPacket fromBuffer(BytePacketBuffer buffer) throws Exception {
        this.header.read(buffer);

        // Get questions and put them in list
        for (short i = 0; i < this.header.getQuestionsCnt(); i++) {
            DnsQuestion question = new DnsQuestion("", QueryType.UNKNOWN);
            question.read(buffer);
            this.questions.add(question);
        }

        for (short i = 0; i < this.header.getAnswersCnt(); i++) {
            DnsRecord record = new DnsRecord();
            record.read(buffer);
            this.answers.add(record);
        }

        for (short i = 0; i < this.header.getAuthorityRecordsCnt(); i++) {
            DnsRecord record = new DnsRecord();
            record.read(buffer);
            this.authorityRecords.add(record);
        }

        for (short i = 0; i < this.header.getResourceRecordsCnt(); i++) {
            DnsRecord record = new DnsRecord();
            record.read(buffer);
            this.resourceRecords.add(record);
        }

        return this;
    }

    /*
     * Write DNS packet to buffer */
    public void write(BytePacketBuffer buffer) {
        // Set counter to appropriate size
        this.header.setQuestionsCnt((short) this.questions.size());
        this.header.setAnswersCnt((short) this.answers.size());
        this.header.setAuthorityRecordsCnt((short) this.authorityRecords.size());
        this.header.setResourceRecordsCnt((short) this.resourceRecords.size());

        // Write header to buffer
        this.header.write(buffer);

        // Write other questions and records
        this.questions.forEach(x -> x.write(buffer));
        this.answers.forEach(x -> x.write(buffer));
        this.authorityRecords.forEach(x -> x.write(buffer));
        this.resourceRecords.forEach(x -> x.write(buffer));

    }

    public DnsHeader getHeader() {
        return header;
    }

    public List<DnsQuestion> getQuestions() {
        return questions;
    }

    public List<DnsRecord> getAnswers() {
        return answers;
    }

    public List<DnsRecord> getAuthorityRecords() {
        return authorityRecords;
    }

    public List<DnsRecord> getResourceRecords() {
        return resourceRecords;
    }
}
