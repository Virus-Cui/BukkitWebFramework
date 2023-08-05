package cn.mrcsh.bukkitwebframework.Utils;

import javax.servlet.ServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
    public static boolean readFileToHttpServletResponse(ServletResponse response, String filePath) {
        OutputStream out = null;
        InputStream in = null;
        try {
            File file = new File(filePath);
            LogUtils.info("&a %s"+filePath);
            if (!file.exists()) {
                return false;
            }

            String suffix = getFileSuffix(filePath);

            response.setContentType("text/"+suffix);

            in = new FileInputStream(file);
            byte[] byteData = new byte[1042];
            out = response.getOutputStream();
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
        } finally {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
            } catch (Exception e) {
            }

        }
        return true;
    }

    public static String getFileSuffix(String FileName){
        return FileName.substring(FileName.lastIndexOf(".")+1);
    }
}
