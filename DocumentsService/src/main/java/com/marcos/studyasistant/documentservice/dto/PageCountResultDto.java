package com.marcos.studyasistant.documentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageCountResultDto {

    private Integer pageCount;
    private String method; // "exact" o "estimated"
    private String fileType;
    private String details;

    public static PageCountResultDto exact(Integer count, String fileType) {
        return new PageCountResultDto(count, "exact", fileType, null);
    }

    public static PageCountResultDto estimated(Integer count, String fileType, String details) {
        return new PageCountResultDto(count, "estimated", fileType, details);
    }

    public static PageCountResultDto unsupported(String fileType) {
        return new PageCountResultDto(null, "unsupported", fileType, "File type not supported for page counting");
    }
}
