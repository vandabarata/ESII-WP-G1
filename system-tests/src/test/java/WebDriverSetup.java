import com.mailslurp.client.ApiClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static com.mailslurp.client.Configuration.getDefaultApiClient;

/**
 * The setup for selenium and the chrome webdriver.
 *
 * @author Vanda Barata (vsfba1@iscte-iul.pt)
 */
public class WebDriverSetup {

    /**
     * The browser being used in the docker container.
     */
    public static WebDriver driver;

    /**
     * URL to be used for tests - the localhost and the port for the wordpress container.
     */
    public static final String baseUrl = "http://" + TestProperties.HOSTNAME + ":" + TestProperties.WP_PORT;

    /**
     * The mailslurp client which will allow testing registrations and whatever else requires the use of emails.
     */
    public static ApiClient mailslurpClient;

    /**
     * Method to do the needed setup for selenium and the webdriver, with the needed options.
     */
    @BeforeAll
    public static void setUp() {
        // Chromedriver version 81 - only compatible with linux and chrome version 81.
        System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver");

        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); // to run without graphical mode

        // solution to avoid tests failing due to problems with the chromedriver
        // taken from https://stackoverflow.com/a/52340526
        options.addArguments("start-maximized"); // https://stackoverflow.com/a/26283818/1689770
        options.addArguments("enable-automation"); // https://stackoverflow.com/a/43840128/1689770
        options.addArguments("--no-sandbox"); //https://stackoverflow.com/a/50725918/1689770
        options.addArguments("--disable-infobars"); //https://stackoverflow.com/a/43840128/1689770
        options.addArguments("--disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
        options.addArguments("--disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
        options.addArguments("--disable-gpu"); //https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc

        driver = new ChromeDriver(options);

        // setup mailslurp
        mailslurpClient = getDefaultApiClient();
        mailslurpClient.setApiKey("a5466caef6f3662c8165393d27392bf36c9b07c86d7f4c2918309a5e0bed8f89");
        mailslurpClient.setConnectTimeout(10000);
    }

    /**
     * Method to ensure the browser is closed at the end of the tests.
     */
    @AfterAll
    public static void tearDown() {
        driver.quit();
    }

}
