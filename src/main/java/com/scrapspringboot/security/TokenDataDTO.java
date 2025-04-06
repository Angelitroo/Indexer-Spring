package com.scrapspringboot.security;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenDataDTO {
    private Long id;
    private String username;
    private String rol;
    private Long fecha_creacion;
    private Long fecha_expiracion;
}