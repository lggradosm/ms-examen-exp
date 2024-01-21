package com.codigo.msexamenexp.service.impl;

import com.codigo.msexamenexp.aggregates.request.RequestEnterprises;
import com.codigo.msexamenexp.aggregates.response.ResponseBase;
import com.codigo.msexamenexp.aggregates.constants.Constants;
import com.codigo.msexamenexp.aggregates.response.ResponseSunat;
import com.codigo.msexamenexp.entity.DocumentsTypeEntity;
import com.codigo.msexamenexp.entity.EnterprisesEntity;
import com.codigo.msexamenexp.entity.EnterprisesTypeEntity;
import com.codigo.msexamenexp.feignClient.SunatClient;
import com.codigo.msexamenexp.repository.DocumentsTypeRepository;
import com.codigo.msexamenexp.repository.EnterprisesRepository;
import com.codigo.msexamenexp.service.EnterprisesService;
import com.codigo.msexamenexp.service.RedisService;
import com.codigo.msexamenexp.util.EnterprisesValidations;
import com.codigo.msexamenexp.util.Util;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;

@Service
public class EnterprisesServiceImpl implements EnterprisesService {

    private final EnterprisesRepository enterprisesRepository;
    private final EnterprisesValidations enterprisesValidations;
    private final DocumentsTypeRepository typeRepository;
    private final SunatClient sunatClient;

    private final RedisService redisService;

    @Value("${token.api.sunat}")
    private String tokenSunat;
    @Value("${time.expiration.sunat.info}")
    private String timeExpirationSunatInfo;

    public EnterprisesServiceImpl(EnterprisesRepository enterprisesRepository, EnterprisesValidations enterprisesValidations, DocumentsTypeRepository typeRepository, SunatClient sunatClient, RedisService redisService) {
        this.enterprisesRepository = enterprisesRepository;
        this.enterprisesValidations = enterprisesValidations;
        this.typeRepository = typeRepository;
        this.sunatClient = sunatClient;
        this.redisService = redisService;
    }
    @Override
    public ResponseBase getInfoFromSunat (String numero) {
        ResponseSunat responseSunat = getExecutionSunat(numero);
        if(responseSunat != null){
            return new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS,Optional.of(responseSunat));
        }
        return new ResponseBase(Constants.CODE_ERROR_DATA_NOT, Constants.MESS_NON_DATA_SUNAT,Optional.empty());
    }

    @Override
    public ResponseBase createEnterprise(RequestEnterprises requestEnterprises) {
        boolean validate = enterprisesValidations.validateInput(requestEnterprises);
        if(validate){
                EnterprisesEntity enterprisesEntity = getEntity(requestEnterprises);
                if(enterprisesEntity != null){
                    enterprisesRepository.save(enterprisesEntity);
                    return new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS, Optional.of(enterprisesEntity));
                }
            return new ResponseBase(Constants.CODE_ERROR_GENERIC,Constants.MESS_SUCCESS, Optional.empty());
        }else{
            return new ResponseBase(Constants.CODE_ERROR_DATA_INPUT,Constants.MESS_ERROR_DATA_NOT_VALID,Optional.empty());
        }
    }

    @Override
    public ResponseBase findOneEnterprise(String doc) {
        String redisCache  = redisService.getValueByKey(Constants.REDIS_KEY_INFO_SUNAT+doc);
        EnterprisesEntity  enterprisesEntity= new EnterprisesEntity();
        if(redisCache != null){
            enterprisesEntity = Util.convertFromJson(redisCache,EnterprisesEntity.class);
        }else{
            enterprisesEntity = enterprisesRepository.findByNumDocument(doc);
            String redisData = Util.convertToJsonEntity(enterprisesEntity);
            redisService.saveKeyValue(Constants.REDIS_KEY_INFO_SUNAT+doc,redisData,Integer.valueOf(timeExpirationSunatInfo));
        }
        return new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS, Optional.of(enterprisesEntity));
    }

    @Override
    public ResponseBase findAllEnterprises() {
        Optional allEnterprises = Optional.of(enterprisesRepository.findAll());
        if(allEnterprises.isPresent()){
            return new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS,allEnterprises);
        }
        return new ResponseBase(Constants.CODE_ERROR_DATA_NOT,Constants.MESS_ZERO_ROWS,Optional.empty());
    }

    @Override
    public ResponseBase updateEnterprise(Integer id, RequestEnterprises requestEnterprises) {
            boolean validationEntity = enterprisesValidations.validateInputUpdate(requestEnterprises);
            boolean existEnterprise = enterprisesRepository.existsById(id);
            if(existEnterprise){
                if(validationEntity){
                    Optional<EnterprisesEntity> enterprisesUpdate = enterprisesRepository.findById(id);
                    EnterprisesEntity enterprisesEntity = getEntityUpdate(requestEnterprises,enterprisesUpdate.get());
                    enterprisesRepository.save(enterprisesEntity);
                    return new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS,Optional.of(enterprisesUpdate));
                }else {
                    return new ResponseBase(Constants.CODE_ERROR_DATA_INPUT,Constants.MESS_ERROR_DATA_NOT_VALID,Optional.empty());
                }

            }
        return new ResponseBase(Constants.CODE_ERROR_EXIST,Constants.MESS_ERROR_NOT_UPDATE,Optional.empty());

    }

    @Override
    public ResponseBase delete(Integer id) {
        Optional<EnterprisesEntity> enterprisesEntity = enterprisesRepository.findById(id);
        if (enterprisesEntity.isPresent()){
            EnterprisesEntity deleteEntity = getEntityDelete(enterprisesEntity.get());
            enterprisesRepository.save(deleteEntity);
            return new ResponseBase(Constants.CODE_SUCCESS,Constants.MESS_SUCCESS,Optional.of(deleteEntity));

        }
        return new ResponseBase(Constants.CODE_ERROR_DATA_NOT,Constants.MESS_ERROR,Optional.empty());
    }

    private EnterprisesEntity getEntity(RequestEnterprises requestEnterprises){
        EnterprisesEntity enterprisesEntity = new EnterprisesEntity();
        enterprisesEntity.setStatus(Constants.STATUS_ACTIVE);
        enterprisesEntity.setEnterprisesTypeEntity(getEnterprisesType(requestEnterprises));
        enterprisesEntity.setDocumentsTypeEntity(getDocumentsType(requestEnterprises));
        enterprisesEntity.setUserCreate(Constants.AUDIT_ADMIN);
        enterprisesEntity.setDateCreate(getTimestamp());
        enterprisesEntity.setNumDocument(requestEnterprises.getNumDocument());
        ResponseSunat sunat = getExecutionSunat(requestEnterprises.getNumDocument());
        if(sunat != null){
            enterprisesEntity.setBusinessName(sunat.getRazonSocial());
            enterprisesEntity.setTradeName(enterprisesValidations.isNullOrEmpty(requestEnterprises.getTradeName())?sunat.getRazonSocial():requestEnterprises.getTradeName());
        } else{
            return null;
        }
        return enterprisesEntity;
    }
    private EnterprisesEntity getEntityUpdate(RequestEnterprises requestEnterprises, EnterprisesEntity enterprisesEntity){
        enterprisesEntity.setNumDocument(requestEnterprises.getNumDocument());
        enterprisesEntity.setEnterprisesTypeEntity(getEnterprisesType(requestEnterprises));
        enterprisesEntity.setDocumentsTypeEntity(getDocumentsType(requestEnterprises));
        ResponseSunat sunat = getExecutionSunat(requestEnterprises.getNumDocument());
        if(sunat != null) {
            enterprisesEntity.setBusinessName(sunat.getRazonSocial());
            enterprisesEntity.setTradeName(enterprisesValidations.isNullOrEmpty(requestEnterprises.getTradeName())?sunat.getRazonSocial():requestEnterprises.getTradeName());
        }else{
            return null;
        }
        enterprisesEntity.setUserModif(Constants.AUDIT_ADMIN);
        enterprisesEntity.setDateModif(getTimestamp());
        return enterprisesEntity;
    }
    private EnterprisesEntity getEntityDelete(EnterprisesEntity enterprisesEntity){
        enterprisesEntity.setStatus(Constants.STATUS_INACTIVE);
        enterprisesEntity.setDateDelete(getTimestamp());
        enterprisesEntity.setUserDelete(Constants.AUDIT_ADMIN);
        return enterprisesEntity;
    }

    private EnterprisesTypeEntity getEnterprisesType(RequestEnterprises requestEnterprises){
        EnterprisesTypeEntity typeEntity = new EnterprisesTypeEntity();
        typeEntity.setIdEnterprisesType(requestEnterprises.getEnterprisesTypeEntity());
        return typeEntity;
    }

    private DocumentsTypeEntity getDocumentsType(RequestEnterprises requestEnterprises){
        DocumentsTypeEntity typeEntity = typeRepository.findByCodType(Constants.COD_TYPE_RUC);
        return  typeEntity;
    }

    private Timestamp getTimestamp(){
        long currentTime = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(currentTime);
        return timestamp;
    }
    public ResponseSunat getExecutionSunat(String numero){
        String authorization = "Bearer" + tokenSunat;
        ResponseSunat responseSunat = sunatClient.getInfoSunat(numero, authorization);
        return responseSunat;
//        String redisCache = redisService.getValueByKey(Constants.REDIS_KEY_INFO_SUNAT+numero);
//        if(redisCache!=null){
//            ResponseSunat sunat = Util.convertFromJson(redisCache, ResponseSunat.class);
//            System.out.println("cache data");
//            return sunat;
//        }else{
//
//            String redisData = Util.convertToJsonEntity(responseSunat);
//            redisService.saveKeyValue(Constants.REDIS_KEY_INFO_SUNAT+numero,redisData,Integer.valueOf(timeExpirationSunatInfo));
//            return responseSunat;
//        }

    }
}
