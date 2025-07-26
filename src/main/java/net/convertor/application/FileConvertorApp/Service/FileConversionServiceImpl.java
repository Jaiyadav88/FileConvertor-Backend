package net.convertor.application.FileConvertorApp.Service;

import net.convertor.application.FileConvertorApp.Utils.ConversionUtils;
import net.convertor.application.FileConvertorApp.dto.FileResult;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileConversionServiceImpl implements FileConversionService {
    @Override
    public FileResult convertAndCompress(MultipartFile inputFile, String targetFormat, boolean compress) throws IOException {
        Path uploaded = Files.createTempFile("upload-", "-" + inputFile.getOriginalFilename());
        inputFile.transferTo(uploaded);
        Path converted = ConversionUtils.convert(uploaded, targetFormat);
        Path output = converted;
        if (compress) {
            Path zip = Files.createTempFile("compressed-", ".zip");
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip));
                 InputStream is = Files.newInputStream(converted)) {
                zos.putNextEntry(new ZipEntry(converted.getFileName().toString()));
                IOUtils.copy(is, zos);
                zos.closeEntry();
            }
            output = zip;
        }
        return new FileResult(output.toFile());
    }
}
