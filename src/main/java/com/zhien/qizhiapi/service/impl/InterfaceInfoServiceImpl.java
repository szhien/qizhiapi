package com.zhien.qizhiapi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhien.qizhiapi.common.ErrorCode;
import com.zhien.qizhiapi.exception.BusinessException;
import com.zhien.qizhiapi.exception.ThrowUtils;
import com.zhien.qizhiapi.mapper.InterfaceInfoMapper;
import com.zhien.qizhiapi.model.entity.InterfaceInfo;
import com.zhien.qizhiapi.service.InterfaceInfoService;
import com.zhien.qizhiapi.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author zhien
* @description 针对表【interface_info(接口信息表)】的数据库操作Service实现
* @createDate 2024-06-16 20:42:09
*/
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
    implements InterfaceInfoService{
    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name =interfaceInfo.getName();
        String url = interfaceInfo.getUrl();
        String description = interfaceInfo.getDescription();
        String method = interfaceInfo.getMethod();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(name,url,description,method), ErrorCode.PARAMS_ERROR,"请求参数不能为空");
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(name) && name.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口名过长");
        }
    }

}




