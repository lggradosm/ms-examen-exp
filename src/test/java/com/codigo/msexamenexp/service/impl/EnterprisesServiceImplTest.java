package com.codigo.msexamenexp.service.impl;

import com.codigo.msexamenexp.aggregates.request.RequestEnterprises;
import com.codigo.msexamenexp.aggregates.response.ResponseBase;
import com.codigo.msexamenexp.aggregates.response.ResponseSunat;
import com.codigo.msexamenexp.entity.DocumentsTypeEntity;
import com.codigo.msexamenexp.entity.EnterprisesEntity;
import com.codigo.msexamenexp.entity.EnterprisesTypeEntity;
import com.codigo.msexamenexp.feignClient.SunatClient;
import com.codigo.msexamenexp.repository.DocumentsTypeRepository;
import com.codigo.msexamenexp.repository.EnterprisesRepository;
import com.codigo.msexamenexp.service.RedisService;
import com.codigo.msexamenexp.util.EnterprisesValidations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EnterprisesServiceImplTest {
    @Mock
    EnterprisesRepository enterprisesRepository;
    @Mock
    DocumentsTypeRepository documentsTypeRepository;
    @Mock
    EnterprisesValidations enterprisesValidations;
    @Mock
    SunatClient sunatClient;
    @Mock
    RedisService redisService;
    @InjectMocks
    EnterprisesServiceImpl enterprisesService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        enterprisesService = new EnterprisesServiceImpl(enterprisesRepository,enterprisesValidations,documentsTypeRepository,sunatClient,redisService);
    }

    @Test
    void createEnterpriseSuccess() {
        RequestEnterprises requestEnterprises = new RequestEnterprises("20100035040","","",2,6);
        EnterprisesTypeEntity enterprisesTypeEntity = new EnterprisesTypeEntity();
        enterprisesTypeEntity.setIdEnterprisesType(2);
        DocumentsTypeEntity documentsTypeEntity = new DocumentsTypeEntity();
        documentsTypeEntity.setIdDocumentsType(6);
        EnterprisesEntity enterprisesEntityExpected = new EnterprisesEntity(1,"20100035040","P Y A D'ONOFRIO S A","P Y A D'ONOFRIO S A",1,enterprisesTypeEntity,documentsTypeEntity);
        Mockito.when(enterprisesRepository.save(Mockito.any(EnterprisesEntity.class))).thenReturn(enterprisesEntityExpected);
        Mockito.when(enterprisesValidations.validateInput(Mockito.any(RequestEnterprises.class))).thenReturn(true);
        ResponseBase responseBase = enterprisesService.createEnterprise(requestEnterprises);
        assertNotNull(responseBase.getData());
    }

    @Test
    void findOneEnterpriseSuccess() {

    }

    @Test
    void findAllEnterprises() {
        EnterprisesTypeEntity enterprisesTypeEntity = new EnterprisesTypeEntity();
        enterprisesTypeEntity.setIdEnterprisesType(2);
        DocumentsTypeEntity documentsTypeEntity = new DocumentsTypeEntity();
        documentsTypeEntity.setIdDocumentsType(6);
        EnterprisesEntity enterprisesEntityExpected = new EnterprisesEntity(1,"20602145248","RAFO ALFARO S.A.C.","RAFO ALFARO S.A.C.",1,enterprisesTypeEntity,documentsTypeEntity);
        List<EnterprisesEntity> listExpected = new ArrayList<>();
        listExpected.add(enterprisesEntityExpected);
        Mockito.when(enterprisesRepository.findAll()).thenReturn(listExpected);
        ResponseBase responseBase = enterprisesService.findAllEnterprises();
        assertNotNull(responseBase);
        assertEquals(listExpected, responseBase.getData().get());
    }

    @Test
    void updateEnterprise() {
    }

    @Test
    void delete() {
    }

}