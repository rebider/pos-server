package com.dianba.pos.settlement.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.dianba.pos.base.BasicResult;
import com.dianba.pos.base.exception.PosNullPointerException;
import com.dianba.pos.passport.po.Passport;
import com.dianba.pos.passport.service.PassportManager;
import com.dianba.pos.settlement.mapper.SettlementMapper;
import com.dianba.pos.settlement.po.PosSettlementDayly;
import com.dianba.pos.settlement.repository.PosSettlementDaylyJpaRepository;
import com.dianba.pos.settlement.service.SettlementManager;
import com.dianba.pos.settlement.vo.PosSettlementDaylyVo;
import com.xlibao.common.constant.payment.PaymentTypeEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultSettlementManager implements SettlementManager {

    private static Logger logger = LogManager.getLogger(DefaultSettlementManager.class);

    @Autowired
    private SettlementMapper settlementMapper;
    @Autowired
    private PassportManager passportManager;
    @Autowired
    private PosSettlementDaylyJpaRepository settlementDaylyJpaRepository;

    @Transactional
    public BasicResult getSettlementOrder(Long passportId) {
        List<PosSettlementDayly> posSettlementDaylies = settlementDaylyJpaRepository.findByPassportId(passportId);
        if (posSettlementDaylies == null || posSettlementDaylies.size() == 0) {
            Passport passport = passportManager.getPassportInfoByCashierId(passportId);
            if (passport == null) {
                throw new PosNullPointerException("商家信息不存在！");
            }
            //计算结算信息
            posSettlementDaylies = settlementMapper.statisticsOrderByDay(passportId);
            for (PosSettlementDayly posSettlementDayly : posSettlementDaylies) {
                BigDecimal amount = posSettlementDayly.getAmount()
                        .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
                posSettlementDayly.setAmount(amount);
                posSettlementDayly.setPassportId(passportId);
                posSettlementDayly.setMerchantPassportId(passport.getId());
            }
            posSettlementDaylies = settlementDaylyJpaRepository.save(posSettlementDaylies);
        }
        List<PosSettlementDaylyVo> settlementDaylyVos = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PosSettlementDayly settlementDayly : posSettlementDaylies) {
            PosSettlementDaylyVo settlementDaylyVo = new PosSettlementDaylyVo();
            BeanUtils.copyProperties(settlementDayly, settlementDaylyVo);
            if (PaymentTypeEnum.ALIPAY.getKey().equals(settlementDaylyVo.getPaymentType())) {
                settlementDaylyVo.setPaymentTypeTitle(PaymentTypeEnum.ALIPAY.getValue());
            } else if (PaymentTypeEnum.WEIXIN_NATIVE.getKey().equals(settlementDaylyVo.getPaymentType())) {
                settlementDaylyVo.setPaymentTypeTitle(PaymentTypeEnum.WEIXIN_NATIVE.getValue());
            } else if (PaymentTypeEnum.CASH.getKey().equals(settlementDaylyVo.getPaymentType())) {
                settlementDaylyVo.setPaymentTypeTitle(PaymentTypeEnum.CASH.getValue());
            }
            settlementDaylyVos.add(settlementDaylyVo);
            totalAmount = totalAmount.add(settlementDaylyVo.getAmount());
        }
        BasicResult basicResult = BasicResult.createSuccessResult();
        basicResult.setResponseDatas(settlementDaylyVos);
        JSONObject jsonObject = basicResult.getResponse();
        jsonObject.put("totalAmount", totalAmount);
        return basicResult;
    }
}
