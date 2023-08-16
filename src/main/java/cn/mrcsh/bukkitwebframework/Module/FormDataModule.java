package cn.mrcsh.bukkitwebframework.Module;

import lombok.Data;
import org.apache.commons.fileupload.FileItem;

import java.io.File;
import java.util.LinkedHashMap;

@Data
public class FormDataModule {
    public LinkedHashMap<String, String> simpleParams = new LinkedHashMap<>();
    public LinkedHashMap<String, FileItem> fileParams = new LinkedHashMap<>();
}
