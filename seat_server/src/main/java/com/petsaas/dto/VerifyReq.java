package com.petsaas.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class VerifyReq implements Serializable {
    private String code; // 核销码
}