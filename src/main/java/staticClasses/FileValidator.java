/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package staticClasses;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Mohammad
 */

public class FileValidator {

    private static final Tika tika = new Tika();

    // قائمة بأنواع الملفات المسموحة
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>();

    static {
        ALLOWED_MIME_TYPES.add("image/png");
        ALLOWED_MIME_TYPES.add("image/jpeg");
        ALLOWED_MIME_TYPES.add("image/jpg");
    ALLOWED_MIME_TYPES.add("image/webp"); 
    }

    public static void validateImageFile(MultipartFile file,int maxSizeInMB) throws IOException {
        // الحصول على نوع MIME الفعلي
        String mimeType;
        try (var inputStream = file.getInputStream()) { // AutoCloseable
            mimeType = tika.detect(inputStream);
            System.out.println(mimeType);
        }

        // التأكد من أن الملف هو صورة ومن الأنواع المسموحة
        if (!ALLOWED_MIME_TYPES.contains(mimeType) || file.getSize() > maxSizeInMB * 1024 * 1024) {
            throw new IOException("THE FILE TYPE ISNT IMAGE " + mimeType);
        }
    }
}

