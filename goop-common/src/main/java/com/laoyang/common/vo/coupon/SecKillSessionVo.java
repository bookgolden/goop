package com.laoyang.common.vo.coupon;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author yyy
 * @Date 2020-07-22 20:05
 * @Email yangyouyuhd@163.com
 * @apiNote 秒杀基本详情
 */
@Data
public class SecKillSessionVo {
    /**
     * id
     */
    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;

    private List<SecKillSkuRelationVo> secKillSkuRelationVoList;
}
