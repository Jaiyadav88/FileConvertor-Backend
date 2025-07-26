package net.convertor.application.FileConvertorApp.dto;

import lombok.Builder;
import lombok.Data;

import java.io.File;

@Data
@Builder
public class FileResult {
    private final File file;
    public FileResult(File file) { this.file = file; }
    public File getFile()       { return file; }
    public String getFilename(){ return file.getName(); }
}

