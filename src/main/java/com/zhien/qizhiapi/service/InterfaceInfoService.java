package com.zhien.qizhiapi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhien.qizhiapi.model.dto.post.PostQueryRequest;
import com.zhien.qizhiapi.model.entity.InterfaceInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhien.qizhiapi.model.entity.Post;
import com.zhien.qizhiapi.model.vo.PostVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author zhien
* @description 针对表【interface_info(接口信息表)】的数据库操作Service
* @createDate 2024-06-16 20:42:09
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {
    /**
     * 校验
     *
     * @param interfaceInfo
     * @param add
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

}
