package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesHepler {
  private static Properties properties = null;
  static {
    properties = new Properties();
    InputStream inputStream = null;
    try {
      inputStream = PropertiesHepler.class.getResourceAsStream("/conf/" + "conf.properties");
      properties.load(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (null != inputStream) {
        try {
          inputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static String getValue(String key) {
    return properties.getProperty(key);
  }

}
