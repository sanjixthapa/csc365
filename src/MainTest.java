import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainTest {
    @Test
    public void testLoadPages() throws IOException{

        File testUrls = File.createTempFile("test_urls", ".txt");
        try (FileWriter writer = new FileWriter(testUrls)) {
            writer.write("https://en.wikipedia.org/wiki/Test_automation\n" + //
                         "https://en.wikipedia.org/wiki/JUnit\n" + //
                         "https://en.wikipedia.org/wiki/Regression_testing");
        }
    }

    @Test
    public void testCountWords() {
        Main main = new Main();
        HT result = main.countWords("hello world Hello hello world");
        assertEquals(3, result.getCount("hello"));
        assertEquals(2, result.getCount("world"));
    }
    @Test
    public void testCountWordsIgnoreSymbols() {
        Main main = new Main();
        HT result = main.countWords("Hello! hello, world. Hello world, world!");
        assertEquals(3, result.getCount("world"));
        assertEquals(3, result.getCount("hello"));

    }
}
