package com.zhien.qizhiapi.service;

import com.zhien.qizhiapi.model.entity.InterfaceInfo;
import com.zhien.qizhiapi.model.entity.User;
import com.zhien.qizhiapi.model.entity.UserInterfaceInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author zhien
* @description 针对表【user_interface_info(用户调用关系表)】的数据库操作Service
* @createDate 2024-06-22 15:57:09
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {
    /**
     * 校验
     *
     * @param userInterfaceInfo
     * @param add
     */
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);


    /**
     * 统计用户接口调用次数
     */
    boolean invokeCount(long userId, long interfaceInfoId);
}
