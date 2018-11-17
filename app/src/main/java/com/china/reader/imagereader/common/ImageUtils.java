package com.china.reader.imagereader.common;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.china.reader.imagereader.adapter.WidthTransformation;
import com.china.reader.imagereader.bean.ImageBean;
import com.china.reader.imagereader.bean.ImagePage;
import com.squareup.picasso.Picasso;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ImageUtils {

    public final static String DIRECTORY_PATH_FORMAT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ImageReader/%02d/%05d/";

    public static ArrayList<ImageBean> getImageBeans(ImagePage imagePage) {
        int webIndex = imagePage.getWebIndex();
        int pageIndex = imagePage.getPageIndex();
        //load ImageBeans from database first
        ArrayList<ImageBean> mImageBeans = getImageBeansFromDB(webIndex, pageIndex);
        LogUtils.e("ImageUtils->getImageBeans() mImageBeans.size()" + mImageBeans.size());
        if (mImageBeans != null && mImageBeans.size() > 0) {
            return mImageBeans;
        }
        ArrayList<ImageBean> imageBeans = new ArrayList<ImageBean>();
        if (webIndex == BaseActivity.WEB_INDEX_1) {
            //load imagebeans from network
            String pageUrlFormat = "http://www.youzi4.cc/mm/%d/%d_%d.html";
            LogUtils.e("ImageUtils->getImageBeans(3) from internet");
            boolean success = true;
            int i = 1;
            do {
                String pageUrlStr = String.format(pageUrlFormat, pageIndex, pageIndex, i);
                success = true;
                try {
                    pageUrlStr = String.format(pageUrlFormat, pageIndex, pageIndex, i);
                    Document doc = Jsoup.connect(pageUrlStr).get();
                    Elements elements = doc.getElementsByClass("IMG_show");
                    if (elements.size() > 0) {
                        Element imageShowElement = elements.get(0);
                        String imageUrl = imageShowElement.attr("src");
                        ImageBean imageBean = new ImageBean(webIndex, pageIndex, imageUrl);
                        insertImageBean(imageBean);
                        imageBeans.add(imageBean);
                        LogUtils.e("ImageUtils->imageUrl:" + imageUrl);
                        i++;
                    } else {
                        success = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.e("ImageUtils->pageUrl:" + pageUrlStr, e);
                    success = false;
                }
            } while (success);
        } else if (webIndex == BaseActivity.WEB_INDEX_2) {
            //load ImageBeans from network
            String pageUrlFormat = "http://zp2006.com/img_%d.html";
            String pageUrl = String.format(pageUrlFormat, pageIndex);

            LogUtils.e("ImageUtils->getSecretImageUrls(1) from network");
            try {
                LogUtils.e("ImageUtils1:" + pageUrl);
                Document doc = Jsoup.connect(pageUrl).get();
                Elements elements = doc.getElementsByAttributeValue("class", "img lazy");
                for (Element element : elements) {
                    String imageUrl = element.attr("data-original");
                    ImageBean imageBean = new ImageBean(webIndex, pageIndex, imageUrl);
                    insertImageBean(imageBean);
                    imageBeans.add(imageBean);
                    LogUtils.e("ImageUtils2:" + imageUrl);
                }
            } catch (Exception e) {
                LogUtils.e("ImageUtils3:" + pageUrl, e);
                e.printStackTrace();
            }
        } else if (webIndex == BaseActivity.WEB_INDEX_3) {
            //load imagebeans from network
            //http://www.17786.com/1_1.html
            String pageUrlFormat = "http://www.17786.com/%d_%d.html";
            LogUtils.e("ImageUtils->getImageBeans(3) from internet");
            boolean success = true;
            int i = 1;
            do {
                String pageUrlStr = String.format(pageUrlFormat, pageIndex, i);
                success = true;
                try {
                    pageUrlStr = String.format(pageUrlFormat, pageIndex, i);
                    Connection connection = Jsoup.connect(pageUrlStr);
                    LogUtils.e("ImageUtils->Jsoup(1)");
                    Document doc = connection.userAgent("Mozilla/5.0 (X11; U; Linux i586; en-US; rv:1.7.3) Gecko/20040924 Epiphany/1.4.4 (Ubuntu)").get();
                    LogUtils.e("ImageUtils->Jsoup(2)");
                    Elements elements = doc.getElementsByClass("IMG_show");
                    LogUtils.e("ImageUtils->Jsoup(3)");
                    if (elements.size() > 0) {
                        Element imageShowElement = elements.get(0);
                        String imageUrl = imageShowElement.attr("src");
                        ImageBean imageBean = new ImageBean(webIndex, pageIndex, imageUrl);
                        imageBeans.add(imageBean);
                        insertImageBean(imageBean);
                        LogUtils.e("ImageUtils->imageUrl:" + imageUrl);
                        i++;
                    } else {
                        success = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.e("ImageUtils->pageUrl:" + pageUrlStr, e);
                    success = false;
                }
            } while (success);
        } else if (webIndex == BaseActivity.WEB_INDEX_4) {
            LogUtils.e("ImageUtils->getImageBeans(4) from internet");
            String pageUrlFormat = "http://www.mmjpg.com/mm/%d/%d";
            boolean success = true;
            int i = 1;
            do {
                String pageUrlStr = String.format(pageUrlFormat, pageIndex, i);
                success = true;
                try {
                    pageUrlStr = String.format(pageUrlFormat, pageIndex, i);
                    Connection connection = Jsoup.connect(pageUrlStr);
                    Document doc = connection.get();
                    Elements elements = doc.getElementsByAttribute("data-img");
                    if (elements.size() > 0) {
                        Element imageShowElement = elements.get(0);
                        String imageUrl = imageShowElement.attr("src");
                        ImageBean imageBean = new ImageBean(webIndex, pageIndex, imageUrl);
                        imageBeans.add(imageBean);
                        insertImageBean(imageBean);
                        LogUtils.e("ImageUtils->WEB_INDEX_4 imageUrl:" + imageUrl);
                        i++;
                    } else {
                        success = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.e("ImageUtils->WEB_INDEX_4 pageUrl:" + pageUrlStr, e);
                    success = false;
                }
            } while (success);
        }

        return imageBeans;
    }

    private static ArrayList<ImageBean> getImageBeansFromDB(int webIndex, int pageIndex) {
        LogUtils.e("ImageUtils->getImageBeansFromDB(1)");
        ArrayList<ImageBean> imageBeans = new ArrayList<ImageBean>();
        try {
            LogUtils.e("ImageUtils->getImageBeansFromDB(2)");
            List<ImageBean> imageBeansDB = GreenDaoManager.getInstance().getDaoSession().getImageBeanDao().queryRaw("where WEB_INDEX=? and PAGE_INDEX=?", webIndex + "", pageIndex + "");
            if (imageBeansDB.size() > 0) {
                LogUtils.e("ImageUtils->getImageBeansFromDB(3)");
                imageBeans.addAll(imageBeansDB);
                return imageBeans;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("ImageUtils->getImageBeansFromDB(exception):" + e, e);
        }
        LogUtils.e("ImageUtils->getImageBeansFromDB(4)");
        return imageBeans;
    }

    /*获取图片标题列表*/
    public static List<ImagePage> getImagePages(int webIndex, int fromIndex, int toIndex, boolean isFav) {
        if (isFav) {
            // load favorite from database
            return loadFavImagePages(webIndex);
        }
        ArrayList<ImagePage> imagePages = new ArrayList<>();
        try {
            for (int i = fromIndex; i <= toIndex; i++) {
                // load from database
                List<ImagePage> mImagePages = GreenDaoManager.getInstance().getDaoSession().getImagePageDao().queryRaw("where WEB_INDEX=? and PAGE_INDEX=?", String.valueOf(webIndex), String.valueOf(i));
                if (mImagePages.size() > 0) {
                    imagePages.add(mImagePages.get(0));
                    continue;
                }
                String title = "";
                String pageUrl = "";
                String pageUrlFormat = "";
                if (webIndex == BaseActivity.WEB_INDEX_1) {
                    pageUrlFormat = "http://www.youzi4.cc/mm/%d/%d_%d.html";
                    pageUrl = String.format(pageUrlFormat, i, i, 1);
                    Document doc = Jsoup.connect(pageUrl).get();
                    Element imageShowElement = doc.getElementsByClass("IMG_show").get(0);
                    title = imageShowElement.attr("alt");
                } else if (webIndex == BaseActivity.WEB_INDEX_2) {
                    pageUrlFormat = "http://mf94.xyz/img_%d.html";
                    pageUrl = String.format(pageUrlFormat, i);
                    title = Jsoup.connect(pageUrl).get().getElementsByTag("h4").text();
                } else if (webIndex == BaseActivity.WEB_INDEX_3) {
                    pageUrlFormat = "http://www.17786.com/%d_%d.html";
                    pageUrl = String.format(pageUrlFormat, i, 1);
                    Document doc = Jsoup.connect(pageUrl).get();
                    title = doc.getElementsByClass("IMG_show").get(0).attr("alt");
                } else if (webIndex == BaseActivity.WEB_INDEX_4) {
                    pageUrlFormat = "http://www.mmjpg.com/mm/%d/%d";
                    pageUrl = String.format(pageUrlFormat, i, 1);
                    Document doc = Jsoup.connect(pageUrl).get();
                    Element imageShowElement = doc.getElementsByAttribute("data-img").get(0);
                    //String imageUrl = imageShowElement.attr("src");
                    title = imageShowElement.attr("alt");
                    LogUtils.e("ImageUtils->title:" + title);
                }
                ImagePage mImagePage = new ImagePage(webIndex, i, title);
                imagePages.add(mImagePage);
                try {
                    insertImagePage(mImagePage);
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.e("ImageUtils->getImagePages(e)" + e, e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imagePages;
    }

    // download image file to storage
    public static void downloadImage(ImageBean imageBean) {
        int webIndex = imageBean.getWebIndex();
        int pageIndex = imageBean.getPageIndex();
        String imageUrl = imageBean.getImageUrl();
        String imageName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        File directory = new File(String.format(DIRECTORY_PATH_FORMAT, webIndex, pageIndex));
        File imageFile = new File(directory.getAbsolutePath(), imageName);
        try {
            URL url = new URL(imageUrl);
            LogUtils.e("ImageUtils->downloadImage() imageUrl:" + imageUrl);
            InputStream is = getConnection(webIndex, url).getInputStream();
            byte[] bs = new byte[1024];
            int len;
            OutputStream os = new FileOutputStream(imageFile.getPath());
            while ((len = is.read(bs)) != -1) {
                os.write(bs, 0, len);
            }
            os.close();
            is.close();
        } catch (Exception e) {
            LogUtils.e("ImageUtils->downloadImage->exception:" + e, e);
        }
    }

    private static URLConnection getConnection(int webIndex, URL url) throws Exception {
        if (webIndex == 1) {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            con.setRequestProperty("Accept-Encoding", "gzip, deflate");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7");
            con.setRequestProperty("Cache-Control", "max-age=0");
            con.setRequestProperty("Connection", "keep-alive");
            con.setRequestProperty("Cookie", "BUSER=fe2e1477708d4f9b97f45ba54835e008; UM_distinctid=1654dd5f8c03a9-023b2432e39f09-3464790b-fa000-1654dd5f8d641a; CNZZDATA1272874627=747831528-1534606942-null%7C1534606942;Hm_lvt_a5380fe98a4f8ada8d996e42fd889959=1534609193");
            con.setRequestProperty("Host", "www.youzi4.cc");
            con.setRequestProperty("Referer", "http://www.youzi4.cc/mm/1/1_1.html");
            con.setRequestProperty("Upgrade-Insecure-Requests", "1");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.92 Safari/537.36");
            con.setConnectTimeout(5 * 1000);
            con.connect();
            return con;
        } else if (webIndex == 4) {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");
            con.setRequestProperty("Accept-Encoding", "gzip, deflate");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7");
            con.setRequestProperty("Connection", "keep-alive");
            con.setRequestProperty("Host", "fm.shiyunjj.com");
            con.setRequestProperty("Upgrade-Insecure-Requests", "1");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36");
            con.setRequestProperty("Referer", "http://www.mmjpg.com/mm/108/1");
            con.setConnectTimeout(5 * 1000);
            con.connect();
            return con;
        }
        URLConnection con = url.openConnection();
        con.setConnectTimeout(5 * 1000);
        return con;
    }

    // update ImagePage
    public static void updateImagePage(ImagePage imagePage) {
        GreenDaoManager.getInstance().getDaoSession().getImagePageDao().update(imagePage);
    }

    /* update ImageBean */
    public static void updateImageBean(ImageBean imageBean) {
        LogUtils.e("ImageUtils->updateImageBean()" + imageBean.getWidth() + " height:" + imageBean.getHeight());
        GreenDaoManager.getInstance().getDaoSession().getImageBeanDao().update(imageBean);
    }

    // insert ImagePage
    public static long insertImagePage(ImagePage imagePage) {
        return GreenDaoManager.getInstance().getDaoSession().getImagePageDao().insert(imagePage);
    }

    // load favorite ImagePages
    public static List<ImagePage> loadFavImagePages(int webIndex) {
        return GreenDaoManager.getInstance().getDaoSession().getImagePageDao().queryRaw("where WEB_INDEX=? and IS_FAVORITE=?", String.valueOf(webIndex), "1");
    }

    public static long insertImageBean(ImageBean imageBean) {
        return GreenDaoManager.getInstance().getDaoSession().getImageBeanDao().insert(imageBean);
    }

    // download and show image
    public static void downShow(final ImageBean imageBean, final ImageView imageView) {
        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ImageReader/" + String.format("%02d", imageBean.getWebIndex()) + "/" + String.format("%05d", imageBean.getPageIndex()));
        if (!directory.exists()) {
            boolean mkdirSuc = directory.mkdirs();
            LogUtils.e("ImageRecycleAdapter->mkdir:" + mkdirSuc + " dir:" + directory);
        }
        final String imageUrl = imageBean.getImageUrl();
        String imageName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        if (directory.exists()) {
            final File imageFile = new File(directory.getAbsolutePath(), imageName);
            if (imageFile.exists()) {
                LogUtils.e("ImageRecycleAdapter->imageFile exists, load image from file! dir:" + directory);
                //then load image from storage to memory
                Picasso.get().load(imageFile).transform(WidthTransformation.getInstance(imageView.getContext(), imageBean, imageView.getWidth())).into(imageView);
            } else {
                LogUtils.e("ImageRecycleAdapter->imageFile not exists, download image from network");
                final Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        //then load image from storage to memory
                        Picasso.get().load(imageFile).transform(WidthTransformation.getInstance(imageView.getContext(), imageBean, imageView.getWidth())).into(imageView);
                    }
                };
                //download image to storage
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ImageUtils.downloadImage(imageBean);
                        handler.sendEmptyMessage(1);
                    }
                }).start();
            }
        } else {
            LogUtils.e("ImageRecycleAdapter->imageFile not exists, picasso load image from network directly");
            Picasso.get().load(imageUrl).transform(WidthTransformation.getInstance(imageView.getContext(), imageBean, imageView.getWidth())).into(imageView);
        }
    }
}
