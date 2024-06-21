package com.zhien.qizhiapi.model.dto.interfaceInfo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 创建请求
 *
 * @author zhien
 */
@Data
public class InterfaceInfoAddRequest implements Serializable {

    /**
     * 接口名
     */
    private String name;

    /**
     * 接口地址
     */
    private String url;

    /**
     * 接口类型
     */
    private String method;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 状态[0 关闭 1开启]
     */
    private Integer status;
    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;


    private static final long serialVersionUID = 1L;


}