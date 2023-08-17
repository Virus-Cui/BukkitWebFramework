package cn.mrcsh.bukkitwebframework.Module;

import org.apache.commons.fileupload.FileItem;

import java.io.*;

public class MultipartFile {
    private String fileName;

    private FileItem fileItem;



    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileItem getFileItem() {
        return fileItem;
    }

    public void setFileItem(FileItem fileItem) {
        this.fileItem = fileItem;
    }

    public void transfer2File(File file){
        InputStream in = null;   //得到上传数据
        try {
            in = fileItem.getInputStream();
            int len = 0;
            byte[] buffer = new byte[1024];
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

    public String getFileName() {
        return fileName;
    }

    public String getOriginName(){
        return this.fileItem.getName();
    }

    public InputStream getInputStream() throws IOException {
        return this.fileItem.getInputStream();
    }

    public Reader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.fileItem.getInputStream()));
    }
}
