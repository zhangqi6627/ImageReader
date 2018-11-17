package com.china.reader.imagereader.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class NovelUtils {
    public static List<String> getNovelTitles(String urlformat, int beginIndex, int endIndex) {
        List<String> novelTitles = new ArrayList<>();
        for (int i = beginIndex; i <= endIndex; i++) {
            try {
                String novelUrl = String.format(urlformat, i);
                LogUtils.e("NovelUtils->getNovelTitles-> novelUrl:"+novelUrl);
                Document doc = Jsoup.connect(novelUrl).get();
                Elements elements = doc.getElementsByClass("breadcrumb-item");
                Element element = elements.get(1);
                String type = element.getElementsByTag("a").text();
                //System.out.println(type);
                String title = (String.format("%05d", i) + "_" + type + "_" + doc.getElementsByTag("h3").text()).replace(" ", "");
                LogUtils.e("NovelUtils->getNovelTitles->" + title+" novelUrl:"+novelUrl);

                String content = doc.getElementsByClass("shortNovelContent").text().replace("[br]", "\n");
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e("NovelUtils->getNovelTitles->" + e, e);
            }
        }
        return novelTitles;
    }
}
