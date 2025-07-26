package net.convertor.application.FileConvertorApp.Controller;

import lombok.RequiredArgsConstructor;
import net.convertor.application.FileConvertorApp.Service.FileConversionService;
import net.convertor.application.FileConvertorApp.dto.FileResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class FileController {
    private final FileConversionService conversionService;
    @PostMapping("/convert")
    public ResponseEntity<Resource> convertAndCompress(
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetFormat") String targetFormat,
            @RequestParam(value = "compress", defaultValue = "false") boolean compress
    ) throws IOException {
        FileResult result = conversionService.convertAndCompress(file, targetFormat, compress);
        InputStreamResource body = new InputStreamResource(new FileInputStream(result.getFile()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(result.getFilename())
                        .build()
        );
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(result.getFile().length())
                .body(body);
    }
}
