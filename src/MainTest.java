import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.swing.*;
import java.lang.reflect.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class MainTest {
    //helper to get private fields
    private <T> T getField(Main m, String name, Class<T> type) throws Exception {
        Field f = Main.class.getDeclaredField(name);
        f.setAccessible(true);
        return type.cast(f.get(m));
    }
    //write to urlfile
    private void writeUrlsFile(String content) throws IOException {
        File urlsFile = new File("src/urls");
        urlsFile.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(urlsFile)) {
            writer.write(content);
        }
    }
    //for L3 test case
    private void deleteUrlsFile() {
        File urlsFile = new File("src/urls");
        if (urlsFile.exists()) {
            urlsFile.delete();
        }
    }
    @Test
    //test loadPages
    public void L1() throws Exception{
        //test num of urls
        writeUrlsFile("https://en.wikipedia.org/wiki/Test_automation\n" +
                "https://en.wikipedia.org/wiki/JUnit\n" +
                "https://en.wikipedia.org/wiki/Regression_testing");
        
        Main main = new Main();
        JComboBox<String> dropdown = getField(main, "dropdown", JComboBox.class);
        List<?> pages = getField(main, "pages", List.class);
        JTextArea results = getField(main,"results",JTextArea.class);
        //assert that wiki pages are there. Can change w respect to num of URLS
        assertEquals(3, dropdown.getItemCount());
        assertEquals(3, pages.size());
        assertEquals(" Select page and click Find Similar", results.getText());
    }
    @Test
    //empty url file test
    public void L2() throws Exception {
        writeUrlsFile("");
        Main main = new Main();
        JComboBox<String> dropdown = getField(main, "dropdown", JComboBox.class);
        List<?> pages = getField(main, "pages", List.class);
        JTextArea results = getField(main,"results",JTextArea.class);
        //asert there is no urls on file
        assertEquals(0, dropdown.getItemCount());
        assertEquals(0, pages.size());
        assertEquals(" Select page and click Find Similar", results.getText());
    }
    @Test
    //no url file present
    public void L3() throws Exception {
        deleteUrlsFile();
        Main main = new Main();
        JComboBox<String> dropdown = getField(main, "dropdown", JComboBox.class);
        List<?> pages = getField(main, "pages", List.class);
        JTextArea results = getField(main,"results",JTextArea.class);
        assertEquals(0, dropdown.getItemCount());
        assertEquals(0, pages.size());
        assertEquals("Error reading urls file", results.getText());
    }
    
    @Test
    //file with invalid URL
    public void L4() throws Exception {
        //mix of 2 valid urls and 1 invalid; only 2 should show
        writeUrlsFile("https://en.wikipedia.org/wiki/Test_automation\n" +
                "https://en.wikipedia.org/wiki/invalidurl\n" +
                "https://en.wikipedia.org/wiki/Regression_testing");

        //capture the error msg
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        PrintStream origErr = System.err;
        System.setErr(new PrintStream(result));

        Main main = new Main();
        JComboBox<String> dropdown = getField(main, "dropdown", JComboBox.class);
        List<?> pages = getField(main, "pages", List.class);
        assertEquals(2, dropdown.getItemCount());
        assertEquals(2, pages.size());
        String output = result.toString();
        assertTrue("Prints error to console", output.contains("Failed: https://en.wikipedia.org/wiki/invalidurl"));     
        System.setErr(origErr);
    }
    

    //tests for countWords method
    @Test
    //should ignore numeric chars too
    public void CW1() {
        Main main = new Main();
        HT result = main.countWords("hello world 23 Hello hello 999 world");
        assertEquals(3, result.getCount("hello"));
        assertEquals(2, result.getCount("world"));
    }
    @Test
    public void CW2() {
        //empty string (boundary value)
        Main main = new Main();
        HT result = main.countWords("");
        assertEquals(0, result.getCount("anystring"));
    }
    @Test
    public void CW3() {
        Main main = new Main();
        HT result = main.countWords("The 123 and an 579");
        assertEquals(0, result.getCount("empty"));
    }
    @Test
    public void CW4() {
        //mixed cases and punctuation
        Main main = new Main();
        HT result = main.countWords("HeLLo! hello, world. ?HELLO world, wOrld!");
        assertEquals(3, result.getCount("world"));
        assertEquals(3, result.getCount("hello"));
    }
    
    
}
