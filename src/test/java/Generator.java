import java.time.LocalDateTime;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * This class is used to generate sample data.
 */
public class Generator {

    private static final String[] levels = {"VERBOSE", "DEBUG", "INFO", "WARN", "ERROR"};

    private static final String[] classes =
            {"com.amadeus.DataMap", "org.bigfoot.Toe", "java.util.Map", "com.fizzy.Twizzer", "org.whynot.Question",
                    "com.enterprise.Business"};

    private static final String[] nouns = {"file", "message", "id", "PNR", "reservation", "service", "class", "object"};

    private static final String[] participates = {"found", "deleted", "inserted", "retrieved", "read", "downloaded", "accessed", "returned"};

    private static final String[] verbs = {"cannot be", "was", "has been", "could not be"};

    private final Random random = new Random();

    public static void main(String[] args) {
        new Generator().generate();
    }

    private void generate() {
        String format = "<%s> <%s> <%s> <> <%s;jsessionid=%s>%n";

        for (int i = 0; i < 600; i++) {
            System.out.printf(format, LocalDateTime.now(), rand(levels), rand(classes), randMsg(), randSID());
        }
    }

    private String rand(String[] array) {
        return array[random.nextInt(array.length)];
    }

    private String randMsg() {
        return String.format("The %s %s %s", rand(nouns), rand(verbs), rand(participates));
    }

    private String randSID() {
        return random.ints(16, 0, 0xFFFF).mapToObj(Integer::toHexString).collect(Collectors.joining());
    }
}
