package net.convertor.application.FileConvertorApp.Service;

import net.convertor.application.FileConvertorApp.dto.FileResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileConversionService {
    /**
     * @param inputFile    the uploaded MultipartFile
     * @param targetFormat e.g. "pdf", "txt", "jpg"
     * @param compress     whether to zip the result
     * @return holds the File on disk and its final filename
     */
    FileResult convertAndCompress(MultipartFile inputFile,
                                  String targetFormat,
                                  boolean compress) throws IOException;
}

