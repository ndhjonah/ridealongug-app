package com.ridealongug.backend.permissions;

import com.ridealongug.backend.models.jpahelpers.enums.AppDomains;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Permission {
    private String code;
    private String name;
    private AppDomains domain;
    private Boolean shipWithAdmin = false;

    public Permission(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Permission(String code, String name, AppDomains domain) {
        this.code = code;
        this.name = name;
        this.domain = domain;
    }

    public Permission(String code, String name, AppDomains domain, Boolean shipWithAdmin) {
        this.code = code;
        this.name = name;
        this.domain = domain;
        this.shipWithAdmin = shipWithAdmin;
    }
}
