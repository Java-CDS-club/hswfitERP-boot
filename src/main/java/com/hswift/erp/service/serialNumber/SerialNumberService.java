package com.hswift.erp.service.serialNumber;

import com.alibaba.fastjson.JSONObject;
import com.hswift.erp.constants.BusinessConstants;
import com.hswift.erp.constants.ExceptionConstants;
import com.hswift.erp.datasource.entities.*;
import com.hswift.erp.datasource.mappers.*;
import com.hswift.erp.exception.BusinessRunTimeException;
import com.hswift.erp.exception.hswiftException;
import com.hswift.erp.service.log.LogService;
import com.hswift.erp.service.material.MaterialService;
import com.hswift.erp.service.user.UserService;
import com.hswift.erp.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Description
 *
 * @Author: cjl
 * @Date: 2019/1/21 16:33
 */
@Service
public class SerialNumberService {
    private Logger logger = LoggerFactory.getLogger(SerialNumberService.class);

    @Resource
    private SerialNumberMapper serialNumberMapper;
    @Resource
    private SerialNumberMapperEx serialNumberMapperEx;
    @Resource
    private MaterialMapperEx materialMapperEx;
    @Resource
    private MaterialMapper materialMapper;
    @Resource
    private MaterialService materialService;
    @Resource
    private UserService userService;
    @Resource
    private LogService logService;


    public SerialNumber getSerialNumber(long id)throws Exception {
        SerialNumber result=null;
        try{
            result=serialNumberMapper.selectByPrimaryKey(id);
        }catch(Exception e){
            hswiftException.readFail(logger, e);
        }
        return result;
    }

    public List<SerialNumber> getSerialNumberListByIds(String ids)throws Exception {
        List<Long> idList = StringUtil.strToLongList(ids);
        List<SerialNumber> list = new ArrayList<>();
        try{
            SerialNumberExample example = new SerialNumberExample();
            example.createCriteria().andIdIn(idList);
            list = serialNumberMapper.selectByExample(example);
        }catch(Exception e){
            hswiftException.readFail(logger, e);
        }
        return list;
    }

    public List<SerialNumber> getSerialNumber()throws Exception {
        SerialNumberExample example = new SerialNumberExample();
        List<SerialNumber> list=null;
        try{
            list=serialNumberMapper.selectByExample(example);
        }catch(Exception e){
            hswiftException.readFail(logger, e);
        }
        return list;
    }

    public List<SerialNumberEx> select(String serialNumber, String materialName, Integer offset, Integer rows)throws Exception {
        List<SerialNumberEx> list=null;
        try{
            list=serialNumberMapperEx.selectByConditionSerialNumber(serialNumber, materialName,offset, rows);
        }catch(Exception e){
            hswiftException.readFail(logger, e);
        }
        return list;

    }

    public Long countSerialNumber(String serialNumber,String materialName)throws Exception {
        Long result=null;
        try{
            result=serialNumberMapperEx.countSerialNumber(serialNumber, materialName);
        }catch(Exception e){
            hswiftException.readFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertSerialNumber(JSONObject obj, HttpServletRequest request)throws Exception {
        int result=0;
        try{
            SerialNumberEx serialNumberEx = JSONObject.parseObject(obj.toJSONString(), SerialNumberEx.class);
            /**处理商品id*/
            serialNumberEx.setMaterialId(getSerialNumberMaterialIdByBarCode(serialNumberEx.getMaterialCode()));
            //删除标记,默认未删除
            serialNumberEx.setDeleteFlag(BusinessConstants.DELETE_FLAG_EXISTS);
            //已卖出，默认未否
            serialNumberEx.setIsSell(BusinessConstants.IS_SELL_HOLD);
            Date date=new Date();
            serialNumberEx.setCreateTime(date);
            serialNumberEx.setUpdateTime(date);
            User userInfo=userService.getCurrentUser();
            serialNumberEx.setCreator(userInfo==null?null:userInfo.getId());
            serialNumberEx.setUpdater(userInfo==null?null:userInfo.getId());
            result = serialNumberMapperEx.addSerialNumber(serialNumberEx);
            logService.insertLog("序列号",BusinessConstants.LOG_OPERATION_TYPE_ADD,
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        }catch(Exception e){
            hswiftException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateSerialNumber(JSONObject obj, HttpServletRequest request) throws Exception{
        SerialNumberEx serialNumberEx = JSONObject.parseObject(obj.toJSONString(), SerialNumberEx.class);
        int result=0;
        try{
            serialNumberEx.setMaterialId(getSerialNumberMaterialIdByBarCode(serialNumberEx.getMaterialCode()));
            Date date=new Date();
            serialNumberEx.setUpdateTime(date);
            User userInfo=userService.getCurrentUser();
            serialNumberEx.setUpdater(userInfo==null?null:userInfo.getId());
            result = serialNumberMapperEx.updateSerialNumber(serialNumberEx);
            logService.insertLog("序列号",
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_EDIT).append(serialNumberEx.getId()).toString(),
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        }catch(Exception e){
            hswiftException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteSerialNumber(Long id, HttpServletRequest request)throws Exception {
        return batchDeleteSerialNumberByIds(id.toString());
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteSerialNumber(String ids, HttpServletRequest request)throws Exception {
        return batchDeleteSerialNumberByIds(ids);
    }

    /**
     * create by: qiankunpingtai
     *  逻辑删除序列号信息
     * create time: 2019/3/27 17:43
     * @Param: ids
     * @return
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteSerialNumberByIds(String ids) throws Exception{
        StringBuffer sb = new StringBuffer();
        sb.append(BusinessConstants.LOG_OPERATION_TYPE_DELETE);
        List<SerialNumber> list = getSerialNumberListByIds(ids);
        for(SerialNumber serialNumber: list){
            sb.append("[").append(serialNumber.getSerialNumber()).append("]");
        }
        logService.insertLog("序列号", sb.toString(),
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        User userInfo=userService.getCurrentUser();
        String [] idArray=ids.split(",");
        int result=0;
        try{
            result = serialNumberMapperEx.batchDeleteSerialNumberByIds(new Date(),userInfo==null?null:userInfo.getId(),idArray);
        }catch(Exception e){
            hswiftException.writeFail(logger, e);
        }
        return result;
    }

    public int checkIsNameExist(Long id, String serialNumber)throws Exception {
        SerialNumberExample example = new SerialNumberExample();
        example.createCriteria().andIdNotEqualTo(id).andSerialNumberEqualTo(serialNumber).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<SerialNumber> list=null;
        try{
            list=serialNumberMapper.selectByExample(example);
        }catch(Exception e){
            hswiftException.readFail(logger, e);
        }
        return list==null?0:list.size();
    }

    public List<SerialNumberEx> findById(Long id)throws Exception{
        List<SerialNumberEx> list=null;
        try{
            list=serialNumberMapperEx.findById(id);
        }catch(Exception e){
            hswiftException.readFail(logger, e);
        }
        return list;
    }

    public void checkIsExist(Long id, String materialName, String serialNumber) throws Exception{
        /**
         * 商品名称不为空时，检查商品名称是否存在
         * */
            if(StringUtil.isNotEmpty(materialName)){
                List<Material> mlist=null;
                try{
                     mlist = materialMapperEx.findByMaterialName(materialName);
                }catch(Exception e){
                    hswiftException.readFail(logger, e);
                }

               if(mlist==null||mlist.size()<1){
                   //商品名称不存在
                   throw new BusinessRunTimeException(ExceptionConstants.MATERIAL_NOT_EXISTS_CODE,
                           ExceptionConstants.MATERIAL_NOT_EXISTS_MSG);
               }else if(mlist.size()>1){
                   //商品信息不唯一
                   throw new BusinessRunTimeException(ExceptionConstants.MATERIAL_NOT_ONLY_CODE,
                           ExceptionConstants.MATERIAL_NOT_ONLY_MSG);

               }
            }
            /***
             * 判断序列号是否已存在
             * */
            List <SerialNumberEx> list=null;
            try{
                 list = serialNumberMapperEx.findBySerialNumber(serialNumber);
            }catch(Exception e){
                hswiftException.readFail(logger, e);
            }
            if(list!=null&&list.size()>0){
                if(list.size()>1){
                    //存在多个同名序列号
                    throw new BusinessRunTimeException(ExceptionConstants.SERIAL_NUMBERE_ALREADY_EXISTS_CODE,
                            ExceptionConstants.SERIAL_NUMBERE_ALREADY_EXISTS_MSG);
                }else{
                    //存在一个序列号
                    if(id==null){
                        //新增，存在要添加的序列号
                        throw new BusinessRunTimeException(ExceptionConstants.SERIAL_NUMBERE_ALREADY_EXISTS_CODE,
                                ExceptionConstants.SERIAL_NUMBERE_ALREADY_EXISTS_MSG);
                    }
                        if(id.equals(list.get(0).getId())){
                            //修改的是同一条数据
                        }else{
                            //存在一条不同的序列号信息
                            throw new BusinessRunTimeException(ExceptionConstants.SERIAL_NUMBERE_ALREADY_EXISTS_CODE,
                                    ExceptionConstants.SERIAL_NUMBERE_ALREADY_EXISTS_MSG);
                        }
                }

            }
    }
    /**
     * create by: cjl
     * description:
     *  根据商品名称判断商品名称是否有效
     * create time: 2019/1/23 17:04
     * @Param: materialName
     * @return Long 满足使用条件的商品的id
     */
    public Long checkMaterialName(String materialName)throws Exception{
        if(StringUtil.isNotEmpty(materialName)) {
            List<Material> mlist=null;
            try{
                mlist = materialMapperEx.findByMaterialName(materialName);
            }catch(Exception e){
                hswiftException.readFail(logger, e);
            }
            if (mlist == null || mlist.size() < 1) {
                //商品名称不存在
                throw new BusinessRunTimeException(ExceptionConstants.MATERIAL_NOT_EXISTS_CODE,
                        ExceptionConstants.MATERIAL_NOT_EXISTS_MSG);
            }
            if (mlist.size() > 1) {
                //商品信息不唯一
                throw new BusinessRunTimeException(ExceptionConstants.MATERIAL_NOT_ONLY_CODE,
                        ExceptionConstants.MATERIAL_NOT_ONLY_MSG);

            }
            //获得唯一商品
            if (BusinessConstants.ENABLE_SERIAL_NUMBER_NOT_ENABLED.equals(mlist.get(0).getEnableSerialNumber())) {
                //商品未开启序列号
                throw new BusinessRunTimeException(ExceptionConstants.MATERIAL_NOT_ENABLE_SERIAL_NUMBER_CODE,
                        ExceptionConstants.MATERIAL_NOT_ENABLE_SERIAL_NUMBER_MSG);
            }
            return mlist.get(0).getId();
        }
        return null;
    }
    /**
     * create by: cjl
     * description:
     *  根据商品名称判断给商品添加序列号是否可行
     *  1、根据商品名称必须查询到唯一的商品
     *  2、该商品必须已经启用序列号
     *  3、该商品已绑定序列号数量小于商品现有库存
     *  2019-02-01
     *  用商品的库存去限制序列号的添加有点不合乎道理，去掉此限制
     * create time: 2019/1/23 17:04
     * @Param: materialName
     * @return Long 满足使用条件的商品的id
     */
    public Long getSerialNumberMaterialIdByBarCode(String materialCode)throws Exception{
        if(StringUtil.isNotEmpty(materialCode)){
            //计算商品库存和目前占用的可用序列号数量关系
            //库存=入库-出库
            //入库数量
            Long materialId = 0L;
            List<MaterialVo4Unit> list = materialService.getMaterialByBarCode(materialCode);
            if(list!=null && list.size()>0) {
                materialId = list.get(0).getId();
            }
            return materialId;
        }
        return null;
    }

    /**
     * create by: cjl
     * description:
     * 出库时判断序列号库存是否足够，
     * 同时将对应的序列号绑定单据
     * create time: 2019/1/24 16:24
     * @Param: List<DepotItem>
     * @return void
     */
    public void checkAndUpdateSerialNumber(DepotItem depotItem,User userInfo) throws Exception{
        if(depotItem!=null){
            //查询商品下已分配的可用序列号数量
            int SerialNumberSum= serialNumberMapperEx.countSerialNumberByMaterialIdAndDepotheadId(depotItem.getMaterialId(),null,BusinessConstants.IS_SELL_HOLD);
            //BasicNumber=OperNumber*ratio
            if((depotItem.getBasicNumber()==null?0:depotItem.getBasicNumber()).intValue()>SerialNumberSum){
                //获取商品名称
                Material material= materialMapper.selectByPrimaryKey(depotItem.getMaterialId());
                throw new BusinessRunTimeException(ExceptionConstants.MATERIAL_SERIAL_NUMBERE_NOT_ENOUGH_CODE,
                        String.format(ExceptionConstants.MATERIAL_SERIAL_NUMBERE_NOT_ENOUGH_MSG,material==null?"":material.getName()));
            }
            //商品下序列号充足，分配序列号
            sellSerialNumber(depotItem.getMaterialId(),depotItem.getHeaderId(),(depotItem.getBasicNumber()==null?0:depotItem.getBasicNumber()).intValue(),userInfo);
        }
    }
    /**
     *
     *
     * */
    /**
     * create by: cjl
     * description:
     * 卖出序列号
     * create time: 2019/1/25 9:17
     * @Param: materialId
     * @Param: depotheadId
     * @Param: isSell 卖出'1'
     * @Param: Count 卖出或者赎回的数量
     * @return com.hswift.erp.datasource.entities.SerialNumberEx
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int sellSerialNumber(Long materialId, Long depotHeadId,int count,User user) throws Exception{
        int result=0;
        try{
            result = serialNumberMapperEx.sellSerialNumber(materialId,depotHeadId,count,new Date(),user==null?null:user.getId());
        }catch(Exception e){
            hswiftException.writeFail(logger, e);
        }
        return result;
    }

    /**
     * create by: cjl
     * description:
     * 赎回序列号
     * create time: 2019/1/25 9:17
     * @Param: materialId
     * @Param: depotheadId
     * @Param: isSell 赎回'0'
     * @Param: Count 卖出或者赎回的数量
     * @return com.hswift.erp.datasource.entities.SerialNumberEx
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int cancelSerialNumber(Long materialId, Long depotHeadId,int count,User user) throws Exception{
        int result=0;
        try{
            result = serialNumberMapperEx.cancelSerialNumber(materialId,depotHeadId,count,new Date(),user==null?null:user.getId());
        }catch(Exception e){
            hswiftException.writeFail(logger, e);
        }
        return result;
    }

    /**
     * create by: cjl
     * description:
     *批量添加序列号，最多500个
     * create time: 2019/1/29 15:11
     * @Param: materialName
     * @Param: serialNumberPrefix
     * @Param: batAddTotal
     * @Param: remark
     * @return java.lang.Object
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batAddSerialNumber(String materialCode, String serialNumberPrefix, Integer batAddTotal, String remark)throws Exception {
        int result=0;
        try {
            if (StringUtil.isNotEmpty(materialCode)) {
                //查询商品id
                Long materialId = getSerialNumberMaterialIdByBarCode(materialCode);
                List<SerialNumberEx> list = null;
                //当前用户
                User userInfo = userService.getCurrentUser();
                Long userId = userInfo == null ? null : userInfo.getId();
                Date date = null;
                Long million = null;
                synchronized (this) {
                    date = new Date();
                    million = date.getTime();
                }
                int insertNum = 0;
                StringBuffer prefixBuf = new StringBuffer(serialNumberPrefix).append(million);
                list = new ArrayList<SerialNumberEx>();
                int forNum = BusinessConstants.BATCH_INSERT_MAX_NUMBER >= batAddTotal ? batAddTotal : BusinessConstants.BATCH_INSERT_MAX_NUMBER;
                for (int i = 0; i < forNum; i++) {
                    insertNum++;
                    SerialNumberEx each = new SerialNumberEx();
                    each.setMaterialId(materialId);
                    each.setCreator(userId);
                    each.setCreateTime(date);
                    each.setUpdater(userId);
                    each.setUpdateTime(date);
                    each.setRemark(remark);
                    each.setSerialNumber(new StringBuffer(prefixBuf.toString()).append(insertNum).toString());
                    list.add(each);
                }
                result = serialNumberMapperEx.batAddSerialNumber(list);
                logService.insertLog("序列号",
                        new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_BATCH_ADD).append(batAddTotal).append(BusinessConstants.LOG_DATA_UNIT).toString(),
                        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
            }
        } catch (Exception e) {
            hswiftException.writeFail(logger, e);
        }
        return result;
    }
}
