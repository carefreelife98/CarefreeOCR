package carefree.CarefreeOCR.controller;

import carefree.CarefreeOCR.api.NaverOcrApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@Controller
@Slf4j
@RequiredArgsConstructor
public class uploadController {
    @Value("${naver.service.secretKey}")
    private String secretKey;
    private final NaverOcrApi naverApi;

    // 파일 업로드 폼을 보여주기 위한 GET 요청 핸들러 메서드
    @GetMapping("/upload-form")
    public String uploadForm() throws Exception {
        return "/upload-form"; // HTML 템플릿의 이름을 반환 (upload-form.html)
    }

    // 파일 업로드 및 OCR 수행을 위한 POST 요청 핸들러 메서드
    @PostMapping("/uploadAndOcr")
    public String uploadAndOcr(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        if (file.isEmpty()) {
            return "error"; // 파일이 비어있을 경우 에러를 처리하는 HTML 템플릿으로 이동
        }

        String naverSecretKey = secretKey; // 본인의 네이버 Clova OCR 시크릿 키로 대체

        File tempFile = File.createTempFile("temp", file.getOriginalFilename());
        file.transferTo(tempFile);

        List<String> result = naverApi.callApi("POST", tempFile.getPath(), naverSecretKey, "jpeg");

        tempFile.delete(); // 임시 파일 삭제

        ListIterator<String> iter = result.listIterator();
        StringBuilder sb = new StringBuilder();
        ArrayList<String> afterFmt = new ArrayList<>();
        while (iter.hasNext()) {
            String text = iter.next();
            // 우편 변호 이면,
            if (text.matches("\\(\\d{5}\\)")) {
                // sb 누적 string 을 ArrayList 에 추가 후 초기화. (우편 번호 제외)
                if(!sb.isEmpty()) afterFmt.add(String.valueOf(sb));
                sb.setLength(0);
            } else {
                // 우편 번호가 아니면 sb 에 해당 문자열 추가.
                sb.append(' ').append(text);
            }
//            if (!text.matches("\\(\\d{5}\\)")) {
//                sb.append(' ').append(text);
//            } else {
//                afterFmt.add(text);
//                if(!sb.isEmpty())
//                    afterFmt.add(String.valueOf(sb));
//                sb.setLength(0);
//            }
        }

        model.addAttribute("ocrResult", afterFmt); // OCR 결과를 HTML 템플릿에 전달

        return "ocr-result"; // OCR 결과를 표시하는 HTML 템플릿 이름 반환
    }
}
