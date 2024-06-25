package com.zhien.qizhiapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhien.qizhiapi.common.ErrorCode;
import com.zhien.qizhiapi.exception.BusinessException;
import com.zhien.qizhiapi.mapper.UserInterfaceInfoMapper;
import com.zhien.qizhiapi.model.entity.UserInterfaceInfo;
import com.zhien.qizhiapi.service.UserInterfaceInfoService;
import org.springframework.stereotype.Service;

/**
* @author zhien
* @description 针对表【user_interface_info(用户调用关系表)】的数据库操作Service实现
* @createDate 2024-06-22 15:57:09
*/
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService {
    /**
     * 参数校验
     */
    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = userInterfaceInfo.getUserId();
        Long interfaceInfoId = userInterfaceInfo.getInterfaceInfoId();
        // 创建时，参数不能为空
        if (add) {
            if (interfaceInfoId < 0 || userId < 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口或用户不存在");
            }
        }
        // 有参数则校验
        if (userInterfaceInfo.getLeftNum() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余调用次数不能小于0");
        }
    }

    /**
     * 统计用户接口调用次数: 调用接口一次就修改一次对应接口的总调用次数和剩余调用次数
     * TODO 分布式事务实现
     */
    @Override
    public boolean invokeCount(long userId, long interfaceInfoId) {

        if (userId < 0 || interfaceInfoId < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户或接口不存在");
        }

        // 根据userId 和 interfaceId 作为条件，查出对应的记录，然后修改 totalNum+1 和 leftNum-1
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userId", userId);
        updateWrapper.eq("interfaceInfoId", interfaceInfoId);
        updateWrapper.gt("leftNum", 0);   //剩余调用次数大于0
        updateWrapper.setSql("leftNum = leftNum - 1, totalNum = totalNum + 1");
        return this.update(updateWrapper);
    }
}




