package com.hswift.erp.service.tenant;

import com.alibaba.fastjson.JSONObject;
import com.hswift.erp.datasource.entities.*;
import com.hswift.erp.datasource.mappers.TenantMapper;
import com.hswift.erp.datasource.mappers.TenantMapperEx;
import com.hswift.erp.exception.hswiftException;
import com.hswift.erp.utils.StringUtil;
import com.hswift.erp.utils.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class TenantService {
    private Logger logger = LoggerFactory.getLogger(TenantService.class);

    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private TenantMapperEx tenantMapperEx;

    @Value("${tenant.userNumLimit}")
    private Integer userNumLimit;

    @Value("${tenant.billsNumLimit}")
    private Integer billsNumLimit;


    public Tenant getTenant(long id)throws Exception {
        Tenant result=null;
        try{
            result=tenantMapper.selectByPrimaryKey(id);
        }catch(Exception e){
            hswiftException.readFail(logger, e);
        }
        return result;
    }

    public List<Tenant> getTenant()throws Exception {
        TenantExample example = new TenantExample();
        List<Tenant> list=null;
        try{
            list=tenantMapper.selectByExample(example);
        }catch(Exception e){
            hswiftException.readFail(logger, e);
        }
        return list;
    }

    public List<TenantEx> select(String loginName, int offset, int rows)throws Exception {
        List<TenantEx> list= new ArrayList<>();
        try{
            list = tenantMapperEx.selectByConditionTenant(loginName, offset, rows);
            if (null != list) {
                for (TenantEx tenantEx : list) {
                    tenantEx.setCreateTimeStr(Tools.getCenternTime(tenantEx.getCreateTime()));
                }
            }
        }catch(Exception e){
            hswiftException.readFail(logger, e);
        }
        return list;
    }

    public Long countTenant(String loginName)throws Exception {
        Long result=null;
        try{
            result=tenantMapperEx.countsByTenant(loginName);
        }catch(Exception e){
            hswiftException.readFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertTenant(JSONObject obj, HttpServletRequest request)throws Exception {
        Tenant tenant = JSONObject.parseObject(obj.toJSONString(), Tenant.class);
        int result=0;
        try{
            if(tenant.getUserNumLimit()==null) {
                tenant.setUserNumLimit(userNumLimit); //默认用户限制数量
            }
            if(tenant.getBillsNumLimit()==null) {
                tenant.setBillsNumLimit(billsNumLimit); //默认单据限制数量
            }
            tenant.setCreateTime(new Date());
            result=tenantMapper.insertSelective(tenant);
        }catch(Exception e){
            hswiftException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateTenant(JSONObject obj, HttpServletRequest request)throws Exception {
        Tenant tenant = JSONObject.parseObject(obj.toJSONString(), Tenant.class);
        int result=0;
        try{
            result=tenantMapper.updateByPrimaryKeySelective(tenant);
        }catch(Exception e){
            hswiftException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteTenant(Long id, HttpServletRequest request)throws Exception {
        int result=0;
        try{
            result= tenantMapper.deleteByPrimaryKey(id);
        }catch(Exception e){
            hswiftException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteTenant(String ids, HttpServletRequest request)throws Exception {
        List<Long> idList = StringUtil.strToLongList(ids);
        TenantExample example = new TenantExample();
        example.createCriteria().andIdIn(idList);
        int result=0;
        try{
            result= tenantMapper.deleteByExample(example);
        }catch(Exception e){
            hswiftException.writeFail(logger, e);
        }
        return result;
    }

    public int checkIsNameExist(Long id, String name)throws Exception {
        TenantExample example = new TenantExample();
        example.createCriteria().andIdNotEqualTo(id).andLoginNameEqualTo(name);
        List<Tenant> list=null;
        try{
            list= tenantMapper.selectByExample(example);
        }catch(Exception e){
            hswiftException.readFail(logger, e);
        }
        return list==null?0:list.size();
    }

    public Tenant getTenantByTenantId(long tenantId) {
        Tenant tenant = new Tenant();
        TenantExample example = new TenantExample();
        example.createCriteria().andTenantIdEqualTo(tenantId);
        List<Tenant> list = tenantMapper.selectByExample(example);
        if(list.size()>0) {
            tenant = list.get(0);
        }
        return tenant;
    }
}
