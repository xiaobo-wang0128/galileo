import org.armada.galileo.i18n.I18nUtil;

import java.util.Locale;

/**
 * @author xiaobo
 * @date 2023/1/4 18:28
 */
public class I18nClientTest {

    public static void main(String[] args) {

//        String dd = I18nUtil.get("zh", "aaa", "test111");
//        System.out.println(dd);
//
//        dd = I18nUtil.get("en", "aaa3333", "test11133333");
//        System.out.println(dd);


        for (Locale d : Locale.getAvailableLocales()) {
            System.out.println(d.getLanguage() + "   \t\t" + d.getDisplayName() + ":" + d.getCountry());
        }

    }
}
