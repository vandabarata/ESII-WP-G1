import com.mailslurp.api.api.InboxControllerApi;
import com.mailslurp.api.api.WaitForControllerApi;
import com.mailslurp.client.ApiException;
import com.mailslurp.models.Email;
import com.mailslurp.models.Inbox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests to check login and registration are working properly,
 * as well as permissions.
 *
 * @author Vanda Barata (vsfba1@iscte-iul.pt)
 */
public class LoginAndPermissionsTest extends WebDriverSetup {

    /**
     * Inbox to be used with the random generated email.
     */
    private static Inbox inbox;

    @BeforeAll
    static void generateEmailAndInbox() {
        // create a real, randomized email address with MailSlurp to represent a user
        InboxControllerApi inboxControllerApi = new InboxControllerApi(mailslurpClient);

        try {
            // create random email with inbox
            inbox = inboxControllerApi.createInbox(null,null,null,null, null, null);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        // check if the inbox was created
        assertNotNull(inbox.getId());
        assertTrue(Objects.requireNonNull(inbox.getEmailAddress()).contains("@mailslurp.com"));
    }

    /**
     * Performs a login with an admin user and confirms the admin page appears after login.
     */
    @Test
    public void testLogin() {
        login("admin", "admin");

        // check for the presence of the wp-admin bar on top
        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(By.id("wp-admin-bar-root-default")));

        logout();
    }

    /**
     * Tries to perform a login with an nonexistent user and checks for the error once it fails.
     */
    @Test
    public void testUnregisteredUserCantLogin() {
        login("randomUser", "pass");

        // check for the alert message that shows when an unregistered user tries to login.
        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(By.className("user-registration-error")));
    }

    /**
     * Confirms is user is able to register and get a confirmation email.
     */
    @Test
    public void testUserIsAbleToRegister() {
        final String email = inbox.getEmailAddress();

        register(email);

        // receive a verification email from wordpress using mailslurp
        WaitForControllerApi waitForControllerApi = new WaitForControllerApi(mailslurpClient);

        try {
            // wait for latest unread email in the inbox
            Email mail = waitForControllerApi.waitForLatestEmail(inbox.getId(), 10000L, true);

            // confirm the user received the registration email
            assertTrue(Objects.requireNonNull(mail.getSubject()).contains("Please confirm your registration on ESII-WP-G1"));

            // create a regex for matching the code we expect in the email body
            Pattern p = Pattern.compile("http\\S*?\\?\\S*");
            Matcher matcher = p.matcher(Objects.requireNonNull(mail.getBody()));

            assertTrue(matcher.find());

            String confirmationLink = matcher.group(0).toString();

            assert(confirmationLink.contains("ur_token"));
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs in with a "Member" user and then checks for
     * the presence of the menu item for the website Analytics.
     */
    @Test
    public void testMemberHasAccessToAnalytics() {
        login("member", "Member");

        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(By.id("menu-item-222")));

        driver.findElement(By.cssSelector("#footer-col1 > aside > ul > li > a")).click();

        driver.findElement(By.cssSelector("#user-registration > div > p:nth-child(4) > a")).click();
    }

    /**
     * Logs in with an "Administrator" user and then checks for
     * the presence of the menu item for the Covid Scientific Discoveries Repository.
     */
    @Test
    public void testAdminHasAccessToCovidRepo() {
        login("admin", "admin");

        driver.get(baseUrl);

        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(By.id("menu-item-234")));

        logout();
    }

    /**
     * Performs a login with given username and password.
     *
     * @param username  username to be used to login.
     * @param password  password to be used to login.
     */
    private void login(String username, String password) {
        driver.get(baseUrl);

        driver.findElement(By.id("menu-item-220")).findElement(By.tagName("a")).click();

        WebElement usernameField = new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(By.id("username")));

        WebElement passwordField = new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(By.id("password")));

        usernameField.sendKeys(username);
        passwordField.sendKeys(password);

        driver.findElement(By.name("login")).click();
    }

    /**
     * Method to logout after having logged in.
     */
    private void logout() {
        driver.get(baseUrl);

        driver.findElement(By.cssSelector("#footer-col1 > aside > ul > li > a")).click();

        driver.findElement(By.cssSelector("#user-registration > div > p:nth-child(4) > a")).click();

        WebElement loggedOutWarning = new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#login > p.message")));

        assert(loggedOutWarning.getText().contains("You are now logged out"));
    }

    /**
     * Performs a registration with given email and password.
     *
     *  @param email     email to be used for registration.
     */
    private void register(String email) {
        driver.get(baseUrl);

        driver.findElement(By.cssSelector("#footer-col1 > aside > ul > li.page_item.page-item-37 > a")).click();

        WebElement firstNameField = new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(By.id("first_name")));

        WebElement lastNameField = new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(By.id("last_name")));

        WebElement emailField = new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(By.id("user_email")));

        WebElement passwordField = new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(By.id("user_pass")));

        WebElement confirmationPassowrdField = new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(By.id("user_confirm_password")));

        WebElement privacyCheckButton = new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(By.id("privacy_policy_1591487464")));

        WebElement submitButton = new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"user-registration-form-79\"]/form/div[5]/button")));

        firstNameField.sendKeys("TestName");

        lastNameField.sendKeys("TestLastName");

        emailField.sendKeys(email);

        passwordField.sendKeys("pass");

        confirmationPassowrdField.sendKeys("pass");

        privacyCheckButton.click();

        submitButton.click();

        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(By.id("ur-submit-message-node")));

    }
}
