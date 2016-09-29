import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;

/**
 * Tool to simulate a running Weblogic server. It streams mock log lines to a file on the disk.
 */
public class MockLogger {

    private static final String[] levels = {"VERBOSE", "DEBUG", "INFO", "WARN", "ERROR"};

    private static final String[] classes = {"com.amadeus.DataMap", "org.bigfoot.Toe", "java.util.Map",
            "com.fizzy.Twizzer", "org.whynot.Question", "com.enterprise.Business"};

    private static final String[] nouns = {"file", "message", "id", "PNR", "reservation", "service", "class", "object"};

    private static final String[] participates = {"found", "deleted", "inserted", "retrieved", "read", "downloaded",
            "accessed", "returned"};

    private static final String[] verbs = {"cannot be", "was", "has been", "could not be"};

    private static final Random random = new Random();

    public static void main(String[] args) {
        // TODO: Use a command line lib? See http://commons.apache.org/proper/commons-cli/index.html
        streamMockLogsToFile("mock.log", 200);
    }

    /**
     * Returns a mock log line. Each part of the line is randomly generated.
     *
     * @return the mock log line
     */
    private static String mockLogLine() {
        String format = "####<%s> <%s> <> <> <> <> <> <> <> <> <> <%s> <%s;jsessionid=%s>%n";
        return String.format(format, LocalDateTime.now(), rand(levels), rand(classes), randMsg(), randSID());
    }

    /**
     * Writes mock lines to the file at the given path. It writes line by line. Speed depends on delayBetweenLines. The
     * file is cleaned at the beginning of each call.
     *
     * @param filename
     *         the path to the target file
     * @param maxDelayBetweenLines
     *         the delay between line writes, in milliseconds
     */
    private static void streamMockLogsToFile(String filename, int maxDelayBetweenLines) {
        Path path = Paths.get(filename);
        try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8, CREATE, TRUNCATE_EXISTING, WRITE, SYNC)) {
            Random rand = new Random();
            while (true) {
                writer.write(mockLogLine());
                writer.flush();
                int delay = rand.nextInt(maxDelayBetweenLines);
                // Thread.sleep(0) is not neutral. See http://stackoverflow.com/a/17494898
                if (delay > 0) {
                    Thread.sleep(delay);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String rand(String[] array) {
        return array[random.nextInt(array.length)];
    }

    private static String randMsg() {
        return String.format("The %s %s %s", rand(nouns), rand(verbs), rand(participates));
    }

    private static String randSID() {
        return random.ints(16, 0, 0xFFFF).mapToObj(Integer::toHexString).collect(Collectors.joining());
    }
}
