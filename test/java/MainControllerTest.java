import dom2app.ISingleMeasureRequest;
import engine.MainController;
import org.apache.commons.math3.util.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.Assert.*;

public class MainControllerTest {

    private MainController mainController;
    private static final String DISASTERS_DATA_FILE_PATH = "src/test/resources/input/_ClimateRelatedDisastersFull.tsv";
    private static final String delimiter = "\t";

    @Before
    public void setUp() {
        mainController = new MainController();
    }

    @Test
    public void load_shouldSuccessfullyLoadDataFromFile() throws IOException {
        mainController.load(DISASTERS_DATA_FILE_PATH, delimiter);
    }

    @Test
    public void findSingleCountryIndicator_shouldFindSingleCountryIndicator() throws IOException {
        mainController.load(DISASTERS_DATA_FILE_PATH, delimiter);

        String requestName = "TestRequest";
        String countryName = "Greece";
        String indicatorString = "Storm";

        ISingleMeasureRequest result = mainController.findSingleCountryIndicator(requestName, countryName, indicatorString);

        assertNotNull(result);
        assertTrue(result.isAnsweredFlag());
        assertEquals(requestName, result.getRequestName());
        assertEquals("Greece Storm", result.getRequestFilter());
        assertEquals(countryName, result.getAnswer().getCountryName());
        assertEquals(indicatorString, result.getAnswer().getIndicatorString());
    }

    @Test
    public void findSingleCountryIndicator_shouldNotFindSingleCountryIndicatorWhenDataDoesNotExist() throws IOException {
        mainController.load(DISASTERS_DATA_FILE_PATH, delimiter);

        String requestName = "TestRequest";
        String countryName = "Greece";
        String indicatorString = "someIndicator";

        ISingleMeasureRequest result = mainController.findSingleCountryIndicator(requestName, countryName, indicatorString);

        assertNotNull(result);
        assertFalse(result.isAnsweredFlag());
        assertNull(result.getAnswer());
        assertEquals(requestName, result.getRequestName());
        assertEquals("Greece someIndicator", result.getRequestFilter());
    }

    @Test
    public void findSingleCountryIndicatorYearRange_shouldFindSingleCountryIndicatorInYearRange() throws IOException {
        mainController.load(DISASTERS_DATA_FILE_PATH, delimiter);

        String requestName = "TestRequest";
        String countryName = "Greece";
        String indicatorString = "Storm";
        int startYear = 2000;
        int endYear = 2005;

        ISingleMeasureRequest result = mainController.findSingleCountryIndicatorYearRange(requestName, countryName, indicatorString, startYear, endYear);

        assertNotNull(result);
        assertTrue(result.isAnsweredFlag());
        assertEquals(requestName, result.getRequestName());
        assertEquals("Greece Storm", result.getRequestFilter());
        assertEquals(countryName, result.getAnswer().getCountryName());
        assertEquals(indicatorString, result.getAnswer().getIndicatorString());
        for (Pair<Integer, Integer> pair : result.getAnswer().getMeasurements()) {
            int year = pair.getKey();
            assertTrue(year >= startYear && year <= endYear);
        }
    }

    @Test
    public void findSingleCountryIndicatorYearRange_shouldNotFindSingleCountryIndicatorInYearRangeWhenDataDoesNotExist() throws IOException {
        mainController.load(DISASTERS_DATA_FILE_PATH, delimiter);

        String requestName = "TestRequest";
        String countryName = "Greece";
        String indicatorString = "someIndicator";
        int startYear = 2000;
        int endYear = 2005;

        ISingleMeasureRequest result = mainController.findSingleCountryIndicatorYearRange(requestName, countryName, indicatorString, startYear, endYear);

        assertNotNull(result);
        assertFalse(result.isAnsweredFlag());
        assertNull(result.getAnswer());
        assertEquals(requestName, result.getRequestName());
        assertEquals("Greece someIndicator", result.getRequestFilter());
    }


    @Test
    public void getAllRequestNames_shouldReturnAllRequestNamesWithCorrectContent() throws IOException {
        mainController.load(DISASTERS_DATA_FILE_PATH, delimiter);

        String countryName = "Greece";
        String indicatorString = "Storm";
        String countryName2 = "Germany";
        String indicatorString2 = "Wildfire";
        int startYear = 2000;
        int endYear = 2005;

        mainController.findSingleCountryIndicator("TestRequest1", countryName, indicatorString);
        mainController.findSingleCountryIndicator("TestRequest2", countryName2, indicatorString2);
        mainController.findSingleCountryIndicatorYearRange("TestRequest3", countryName, indicatorString, startYear, endYear);

        Set<String> requestNames = mainController.getAllRequestNames();
        assertEquals(3, requestNames.size());
        assertTrue(requestNames.contains("TestRequest1"));
        assertTrue(requestNames.contains("TestRequest2"));
        assertTrue(requestNames.contains("TestRequest3"));
    }

    @Test
    public void getAllRequestNames_shouldReturnAllRequestNamesCheckForIncorrectContent() throws IOException {
        mainController.load(DISASTERS_DATA_FILE_PATH, delimiter);

        String countryName = "Greece";
        String indicatorString = "Storm";
        int startYear = 2000;
        int endYear = 2005;

        mainController.findSingleCountryIndicator("TestRequest1", countryName, indicatorString);
        mainController.findSingleCountryIndicator("TestRequest2", countryName, indicatorString);
        mainController.findSingleCountryIndicatorYearRange("TestRequest3", countryName, indicatorString, startYear, endYear);

        Set<String> requestNames = mainController.getAllRequestNames();
        assertTrue(requestNames.contains("TestRequest1"));
        assertTrue(requestNames.contains("TestRequest2"));
        assertFalse(requestNames.contains("TestRequest4"));
    }

    @Test
    public void getRequestByName_shouldRetrieveExistingRequest() throws IOException {
        mainController.load(DISASTERS_DATA_FILE_PATH, delimiter);

        String countryName = "Greece";
        String indicatorString = "Storm";
        int startYear = 2000;
        int endYear = 2005;

        mainController.findSingleCountryIndicator("TestRequest1", countryName, indicatorString);
        mainController.findSingleCountryIndicator("TestRequest2", countryName, indicatorString);
        mainController.findSingleCountryIndicatorYearRange("TestRequest3", countryName, indicatorString, startYear, endYear);

        ISingleMeasureRequest request = mainController.getRequestByName("TestRequest3");
        assertEquals("TestRequest3", request.getRequestName());
    }

    @Test
    public void getRequestByName_shouldReturnNullForNotExistingRequest() {
        ISingleMeasureRequest request = mainController.getRequestByName("TestRequest3");
        assertNull(request);
    }

    @Test
    public void getRegression_shouldReturnCorrectRegressionResultForExistingRequest() throws IOException {
        mainController.load(DISASTERS_DATA_FILE_PATH, delimiter);

        String countryName = "Greece";
        String indicatorString = "Storm";
        String expectedRegressionResult = "RegressionResult [intercept=-1.3480821504077318, slope=7.550588945937784E-4, slopeError=0.005379393170971802, Tendency stable]";

        mainController.findSingleCountryIndicator("TestRequest1", countryName, indicatorString);

        ISingleMeasureRequest request = mainController.getRegression("TestRequest1");
        assertNotNull(request);
        assertEquals(expectedRegressionResult, request.getRegressionResultString());
    }

    @Test
    public void getRegression_shouldReturnNullForNonexistentRequest() {
        ISingleMeasureRequest request = mainController.getRegression("TestRequest2");
        assertNull(request);
    }

    @Test
    public void getDescriptiveStats_shouldReturnCorrectDescriptiveStatsForExistingRequest() throws IOException {
        mainController.load(DISASTERS_DATA_FILE_PATH, delimiter);

        String countryName = "Greece";
        String indicatorString = "Storm";
        String expectedDescriptiveStats = "DescriptiveStatsResult [count=43, min=0.0, gMean=0.0, mean=0.1627906976744186, median=0.0, max=2.0, kurtosis=7.654287229472892, stdev=0.43261291166581917, sum=7.0]";

        mainController.findSingleCountryIndicator("TestRequest1", countryName, indicatorString);
        ISingleMeasureRequest request = mainController.getDescriptiveStats("TestRequest1");

        assertNotNull(request);
        assertEquals(expectedDescriptiveStats, request.getDescriptiveStatsString());
    }


    @Test
    public void getDescriptiveStats_shouldReturnNullForNonexistentRequest() {
        ISingleMeasureRequest request = mainController.getDescriptiveStats("TestRequest2");
        assertNull(request);
    }

    @Test
    public void reportToFile_shouldGenerateTextReportFileAndWriteDataSuccessfully() throws IOException {
        mainController.load(DISASTERS_DATA_FILE_PATH, delimiter);

        String requestName = "TestRequest";
        String countryName = "Greece";
        String indicatorString = "Storm";
        String outputFilePath = "reportFile.txt";

        mainController.findSingleCountryIndicator(requestName, countryName, indicatorString);
        int result = mainController.reportToFile(outputFilePath, requestName, "text");

        checkIfFileExists(outputFilePath);
        assertTrue(result > 0);
    }

    @Test
    public void reportToFile_shouldGenerateMarkdownReportFileAndWriteDataSuccessfully() throws IOException {
        mainController.load(DISASTERS_DATA_FILE_PATH, delimiter);

        String requestName = "TestRequest";
        String countryName = "Greece";
        String indicatorString = "Storm";
        String outputFilePath = "reportFile.md";


        mainController.findSingleCountryIndicator(requestName, countryName, indicatorString);
        int result = mainController.reportToFile(outputFilePath, requestName, "md");

        checkIfFileExists(outputFilePath);
        assertTrue(result > 0);
    }

    @Test
    public void reportToFile_shouldGenerateHtmlReportFileAndWriteDataSuccessfully() throws IOException {
        mainController.load(DISASTERS_DATA_FILE_PATH, delimiter);

        String requestName = "TestRequest";
        String countryName = "Greece";
        String indicatorString = "Storm";
        String outputFilePath = "reportFile.html";

        mainController.findSingleCountryIndicator(requestName, countryName, indicatorString);
        int result = mainController.reportToFile(outputFilePath, requestName, "html");

        checkIfFileExists(outputFilePath);
        assertTrue(result > 0);
    }

    @Test(expected = NullPointerException.class)
    public void reportToFile_shouldThrowExceptionForUnsupportedFileType() throws IOException {
        String requestName = "TestRequest";
        String countryName = "Greece";
        String indicatorString = "Storm";
        String filePath = "reportFile.json";

        mainController.findSingleCountryIndicator(requestName, countryName, indicatorString);
        mainController.reportToFile(filePath, requestName, "json");

    }

    public void checkIfFileExists(String filePath) {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            // File exists, do nothing
        } else {
            fail("File does not exist: " + filePath);
        }
    }

    @After
    public void deleteGeneratedReportFiles() {
        File[] files = new File(".").listFiles((dir, name) -> name.startsWith("reportFile"));

        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

}