package com.petsaas.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class AiDiagnosisVo implements Serializable {
    private String diagnosis;
    private String advice;
    private String recommendedService;
}