package lielietea.mirai.plugin.utils.image;

import lielietea.mirai.plugin.core.messagehandler.responder.help.DisclTemporary;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

public class ImageSender {
    public static void sendImageFromURL(Contact contact, URL url) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (InputStream inputStream = url.openStream()) {
            int n = 0;
            byte[] buffer = new byte[1024];
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getGlobal().warning("图片文件下载失败！");
        }

        ExternalResource externalResource = ExternalResource.create(output.toByteArray());
        Image imageReady;
        imageReady = contact.uploadImage(externalResource);
        contact.sendMessage(imageReady);

        try {
            externalResource.close();
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getGlobal().warning("资源关闭失败！");
        }

    }

    public static void sendImageFromBufferedImage(Contact contact, BufferedImage image) throws IOException {
        InputStream is;
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ImageOutputStream imOut;
        imOut = ImageIO.createImageOutputStream(bs);
        ImageIO.write(image, "png", imOut);
        is = new ByteArrayInputStream(bs.toByteArray());
        contact.sendMessage(Contact.uploadImage(contact, is));
    }

    public static InputStream getBufferedImageAsSource(BufferedImage image){
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ImageOutputStream imOut;
        try {
            imOut = ImageIO.createImageOutputStream(bs);
            ImageIO.write(image, "png", imOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ByteArrayInputStream(bs.toByteArray());

    }

}
