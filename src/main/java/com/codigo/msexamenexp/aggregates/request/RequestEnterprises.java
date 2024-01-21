package com.codigo.msexamenexp.aggregates.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RequestEnterprises {
    private String numDocument;
    private String businessName;
    private String tradeName;
    private int enterprisesTypeEntity;
    private int documentsTypeEntity;
}
