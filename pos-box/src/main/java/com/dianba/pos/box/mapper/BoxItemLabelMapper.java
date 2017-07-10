package com.dianba.pos.box.mapper;

import com.dianba.pos.box.po.BoxItemLabel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by zhangyong on 2017/7/4.
 */
@Mapper
public interface BoxItemLabelMapper {

    List<BoxItemLabel> findItemsByRFID(@Param("rfids") List<String> rfids);
}
