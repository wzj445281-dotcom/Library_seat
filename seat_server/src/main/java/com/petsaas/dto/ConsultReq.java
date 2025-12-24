package com.petsaas.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ConsultReq implements Serializable {
    private Long petId;
    private String symptoms;
}