import org.apache.log4j.Logger;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TestLog4j {
    public static void main(String[] args) {
        Robot r = null;
        try {
            r = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
//        String fileName = "2018-12-14.log";
//        String suffix = fileName.substring(fileName.lastIndexOf(".log.") + 5, fileName.length() - 4);
//        System.out.println(suffix);
        while (true) {
            r.delay(2000);
            Logger logger = Logger.getLogger(TestLog4j.class);
            logger.fatal("FATAL：" + new Date().toString());
            logger.info("INFO：" + new Date().toString());
            logger.warn("WARN：" + new Date().toString());
            logger.error("ERROR：" + new Date().toString());
            logger.debug("DEBUG：" + new Date().toString());
        }

    }
}
