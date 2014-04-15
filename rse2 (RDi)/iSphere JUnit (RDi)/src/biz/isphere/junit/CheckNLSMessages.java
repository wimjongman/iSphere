package biz.isphere.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.Properties;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

/**
 * <b>JUnit 4 Test Case</b>
 * <p>
 * Veryfies that all message constants of the iSphere <i>Messages</i> objects
 * have a corresponding message text entry. Also ensure that there are no dead
 * text entries in the property files.
 * <p>
 * This class uses reflection to get the list of message constants. It
 * instantiates a new dedicated classloader to ensure that the static message
 * constants are properly initialized for each locale.
 * 
 * @author Thomas raddatz
 */
public class CheckNLSMessages {

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(CheckNLSMessages.class);
    }

    private final String defaultLocale = "en";
    private final String[] locales = new String[] { defaultLocale, "de", "nl" };

    /**
     * Verifies the NLS messages of the iSphere project.
     * 
     * @throws Exception
     */
    @Test
    public void testNLSMessages() throws Exception {
        checkMessagesForLocales(biz.isphere.base.Messages.class, "messages");
        checkMessagesForLocales(biz.isphere.core.Messages.class, "messages");
        checkMessagesForLocales(biz.isphere.rse.Messages.class, "messages");
        checkMessagesForLocales(biz.isphere.lpex.tasktags.Messages.class, "messages");
    }

    /**
     * Verifies the message constants of a given NLS object and properties
     * bundle for all supported locales.
     * 
     * @param clazz - NLS messages class that is checked
     * @param nlsResource - resource base name
     * @throws Exception
     */
    private void checkMessagesForLocales(Class<?> clazz, String nlsResource) throws Exception {
        System.out.println("Testing class: " + clazz.getName());
        for (String locale : locales) {
            Locale.setDefault(new Locale(locale));

            Object nlsMessagesObject = getInstance(clazz);
            String resourcePath = getResourcePath(clazz, nlsResource, locale);
            System.out.println("  Locale: " + resourcePath);

            Properties properties = getPropertyResourceBundle(nlsMessagesObject, resourcePath);
            checkMessagesForLocale(nlsMessagesObject, properties);
        }
    }

    /**
     * Verifies the message constants of a given NLS object with a given
     * properties bundle of a specific locale.
     * 
     * @param nlsMessagesObject - NLS messages object
     * @param properties - properties resource bundle
     * @param locale
     * @throws IllegalAccessException
     */
    private void checkMessagesForLocale(Object nlsMessagesObject, Properties properties) throws Exception {
        Field[] fields = nlsMessagesObject.getClass().getFields();
        for (Field field : fields) {

            // Prepare
            String nlsMessageConstant = field.getName();
            String messageText = properties.getProperty(nlsMessageConstant);

            // Check
            assertNotNull("Message text must not be [null]. Missing property: " + nlsMessageConstant, messageText);
            assertTrue("Length of message must be greater than zero. Property: " + nlsMessageConstant, messageText.length() > 0);
            assertEquals("Assigned message text must match text in properties file.", messageText, field.get(null));
            properties.remove(nlsMessageConstant);
        }
        assertEquals("Properties must be empty. Otherwise there are more properties than message constants.", 0, properties.size());
    }

    /**
     * Uses a fresh classloader to create a new object from a fresh class.
     * 
     * @param aClass - class to load
     * @return object created from <i>aClass</i>
     * @throws Exception
     */
    private Object getInstance(Class<?> aClass) throws Exception {

        URL[] urls = ((URLClassLoader)(Thread.currentThread().getContextClassLoader())).getURLs();
        URLClassLoader loader = new URLClassLoader(urls, null);
        Class<?> clazz = loader.loadClass(aClass.getName());

        Constructor<?> constructor = clazz.getDeclaredConstructor(new Class[0]);
        constructor.setAccessible(true);
        Object object = constructor.newInstance(new Object[0]);

        return object;
    }

    /**
     * Produces the NLS resource path.
     * 
     * @param clazz - class used to produce the path
     * @param nlsResource - resource base name
     * @param locale - locale for which the path is produced for
     * @return resource path for the given locale
     */
    private String getResourcePath(Class<?> clazz, String nlsResource, String locale) {
        String resourcePath = "/" + clazz.getPackage().getName().replace('.', '/') + "/";
        resourcePath = resourcePath + nlsResource;
        if (defaultLocale.equalsIgnoreCase(locale)) {
            // do not add locale and use the simple name
        } else {
            resourcePath = resourcePath + "_" + locale;
        }
        resourcePath = resourcePath + ".properties";
        return resourcePath;
    }

    /**
     * Returns the NLS messages of a given NLS messages object and locale
     * specific resource.
     * 
     * @param nlsMessagesObject - NLS messages object whose locale specific
     *        messages are returned
     * @param resourcePath - path to the locale specific messages
     * @return NLS messages
     * @throws IOException
     */
    private Properties getPropertyResourceBundle(Object nlsMessagesObject, String resourcePath) throws IOException {
        Properties properties = new Properties();
        properties.load(nlsMessagesObject.getClass().getResourceAsStream(resourcePath));
        return properties;
    }
}
