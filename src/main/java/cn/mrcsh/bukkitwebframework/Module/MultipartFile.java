package cn.mrcsh.bukkitwebframework.Module;

import org.apache.commons.fileupload.FileItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MultipartFile {
    private String fileName;

    private FileItem fileItem;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileItem getFileItem() {
        return fileItem;
    }

    public void setFileItem(FileItem fileItem) {
        this.fileItem = fileItem;
    }

    public void transfer4File(File file){
        InputStream in = null;   //得到上传数据
        try {
            in = fileItem.getInputStream();
            int len = 0;
            byte buffer[]= new byte[1024];
            FileOutputStream out = new FileOutputStream(file);  //向upload目录中写入文件
            while((len=in.read(buffer))>0){
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
