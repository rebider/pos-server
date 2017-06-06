package com.dianba.pos.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.dianba.pos.base.BasicResult;
import com.dianba.pos.base.exception.PosNullPointerException;
import com.dianba.pos.common.util.JsonHelper;
import com.dianba.pos.order.mapper.OrderMapper;
import com.dianba.pos.order.po.LifeOrder;
import com.dianba.pos.order.pojo.OrderItemPojo;
import com.dianba.pos.order.pojo.OrderPojo;
import com.dianba.pos.order.repository.LifeOrderJpaRepository;
import com.dianba.pos.order.service.OrderManager;
import com.dianba.pos.order.support.OrderRemoteService;
import com.dianba.pos.passport.po.LifePassportAddress;
import com.dianba.pos.passport.po.Passport;
import com.dianba.pos.passport.repository.LifePassportAddressJpaRepository;
import com.dianba.pos.passport.service.PassportManager;
import com.dianba.pos.supplychain.service.LifeSupplyChainPrinterManager;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xlibao.common.constant.device.DeviceTypeEnum;
import com.xlibao.common.constant.order.OrderTypeEnum;
import com.xlibao.common.constant.payment.PaymentTypeEnum;
import com.xlibao.metadata.order.OrderEntry;
import com.xlibao.metadata.order.OrderItemSnapshot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DefaultOrderManager extends OrderRemoteService implements OrderManager {

    private static Logger logger = LogManager.getLogger(DefaultOrderManager.class);

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private LifeOrderJpaRepository orderJpaRepository;
    @Autowired
    private LifePassportAddressJpaRepository passportAddressJpaRepository;
    @Autowired
    private LifeSupplyChainPrinterManager supplyChainPrinterManager;
    @Autowired
    private PassportManager passportManager;

    public OrderEntry getOrder(long orderId) {
        Map<String, String> params = new HashMap<>();
        params.put("orderId", orderId + "");
        BasicResult basicResult = postOrder(GET_ORDER, params);
        if (basicResult.isSuccess()) {
            JSONObject jsonObject = basicResult.getResponse();
            OrderEntry orderEntry = jsonObject.toJavaObject(OrderEntry.class);
            return orderEntry;
        }
        return null;
    }

    public LifeOrder getLifeOrder(long orderId) {
        return orderJpaRepository.findOne(orderId);
    }

    public LifeOrder getLifeOrder(String sequenceNumber) {
        return orderJpaRepository.findBySequenceNumber(sequenceNumber);
    }

    public BasicResult prepareCreateOrder(long passportId, OrderTypeEnum orderType) {
        Map<String, String> params = new HashMap<>();
        params.put("partnerUserId", passportId + "");
        params.put("orderType", orderType.getKey() + "");
        return postOrder(PREPARE_CREATE_ORDER, params);
    }

    public BasicResult generateOrder(long passportId, String sequenceNumber, String phoneNumber
            , long actualPrice, long totalPrice
            , List<OrderItemPojo> orderItems) throws Exception {
        Passport merchantPassport = passportManager.getPassportInfoByCashierId(passportId);
        Map<String, String> params = new HashMap<>();
        params.put("sequenceNumber", sequenceNumber);
        params.put("partnerUserId", passportId + "");

        params.put("userSource", DeviceTypeEnum.DEVICE_TYPE_ANDROID.getKey() + "");
        params.put("transType", PaymentTypeEnum.UNKNOWN.getKey());
        //商家ID
        params.put("shippingPassportId", merchantPassport.getId() + "");
        //商家名称
        params.put("shippingNickName", merchantPassport.getShowName() + "");
        //收货人手机号码
        if (!StringUtils.isEmpty(phoneNumber)) {
            params.put("receipt_phone", phoneNumber);
        }
        //订单应收费用
        params.put("actualAmount", actualPrice + "");
        //订单实际收费
        params.put("totalAmount", totalPrice + "");
        params.put("discountAmount", "0");
        params.put("priceLogger", "0");
        params.put("items", JsonHelper.toJSONString(createOrderItemSnapshots(orderItems)));
        return postOrder(GENERATE_ORDER, params);
    }

    public BasicResult generatePurchaseOrder(long passportId, String sequenceNumber, Long warehouseId
            , Map<String, Object> itemSet) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("passportId", passportId + "");
        params.put("sequenceNumber", sequenceNumber);
        params.put("partnerUserId", passportId + "");
        params.put("userSource", DeviceTypeEnum.DEVICE_TYPE_ANDROID.getKey() + "");
        LifePassportAddress merchantPassportAdress = passportAddressJpaRepository
                .findByPassportId(passportId);
        if (merchantPassportAdress == null) {
            throw new PosNullPointerException("商家地址信息不存在！" + passportId);
        }
        //商家ID
        params.put("receiptProvince", merchantPassportAdress.getProvince());
        params.put("receiptCity", merchantPassportAdress.getCity());
        params.put("receiptDistrict", merchantPassportAdress.getDistrict());
        params.put("receiptAddress", merchantPassportAdress.getStreet());
        params.put("receiptNickName", merchantPassportAdress.getName());
        params.put("receiptPhone", merchantPassportAdress.getPhoneNumber());
        params.put("receiptLocation", merchantPassportAdress.getLatitude()
                + "," + merchantPassportAdress.getLongitude());
        JSONObject jsonObject = new JSONObject();
        for (String key : itemSet.keySet()) {
            jsonObject.put(key, warehouseId);
        }
        params.put("warehouseRemarkSet", jsonObject.toJSONString());
        JSONObject itemSetObj = (JSONObject) itemSet;
        params.put("itemSet", itemSetObj.toJSONString());
        return postPurchaseOrder(GENERATE_ORDER, params);
    }

    private List<OrderItemSnapshot> createOrderItemSnapshots(List<OrderItemPojo> orderItems) {
        List<OrderItemSnapshot> orderItemSnapshots = new ArrayList<>();
        for (OrderItemPojo item : orderItems) {
            long itemCostPrice = item.getCostPrice().multiply(BigDecimal.valueOf(100))
                    .longValue();
            long itemSalePrice = item.getTotalPrice().multiply(BigDecimal.valueOf(100))
                    .longValue();
            OrderItemSnapshot orderItemSnapshot = new OrderItemSnapshot();
            orderItemSnapshot.setItemId(item.getItemId());
            orderItemSnapshot.setItemTemplateId(item.getItemTemplateId());
            orderItemSnapshot.setItemName(item.getItemName());
            orderItemSnapshot.setItemTypeId(item.getItemTypeId());
            orderItemSnapshot.setItemTypeName(item.getItemTypeName());
            orderItemSnapshot.setItemUnitId(item.getItemTypeUnitId());
            orderItemSnapshot.setItemUnitName(item.getItemTypeUnitName());
            orderItemSnapshot.setItemBarcode(item.getItemBarcode());
            orderItemSnapshot.setCostPrice(itemCostPrice);
            orderItemSnapshot.setNormalPrice(itemSalePrice);
            orderItemSnapshot.setTotalPrice(itemSalePrice * item.getNormalQuantity());
            orderItemSnapshot.setNormalQuantity(item.getNormalQuantity());
            orderItemSnapshots.add(orderItemSnapshot);
        }
        return orderItemSnapshots;
    }

    public BasicResult paymentOrder(Long orderId, PaymentTypeEnum paymentTypeEnum) {
        Map<String, String> params = new HashMap<>();
        params.put("orderId", orderId + "");
        params.put("transType", paymentTypeEnum.getKey());
        BasicResult basicResult = postOrder(PAYMENT_ORDER, params);
        if (basicResult.isSuccess()) {
            OrderEntry orderEntry = getOrder(orderId);
            if (OrderTypeEnum.PURCHASE_ORDER_TYPE.getKey() == orderEntry.getType()) {
                //打印采购单
                BasicResult result
                        = supplyChainPrinterManager.printerPurchaseOrder(orderEntry.getShippingPassportId(), orderId);
                if (!result.isSuccess()) {
                    logger.error("采购订单打印失败！" + basicResult.getMsg() + "，订单ID:" + orderId);
                }
            }
        }
        return basicResult;
    }

    public BasicResult confirmOrder(long passportId, long orderId) {
        Map<String, String> params = new HashMap<>();
        params.put("orderId", orderId + "");
        params.put("partnerUserId", passportId + "");
        return postOrder(CONFIRM_ORDER, params);
    }

    public BasicResult syncOfflineOrders(List<OrderPojo> orders) {
        List<LifeOrder> lifeOrders = new ArrayList<>();
        //TODO 保存离线订单
        List<Map<String, String>> faileOrderIds = new ArrayList<>();
        for (OrderPojo orderPojo : orders) {
            Map<String, String> map = new HashMap<>();
            map.put("id", orderPojo.getId());
            faileOrderIds.add(map);
        }
        BasicResult basicResult = BasicResult.createSuccessResult();
        basicResult.setResponseDatas(faileOrderIds);
        return basicResult;
    }

    public BasicResult getOrderForPos(Long passportId, Integer orderType, Integer orderStatus
            , Integer pageNum, Integer pageSize) {
        Page<List<OrderEntry>> orderPage = PageHelper.startPage(pageNum, pageSize).doSelectPage(()
                -> orderMapper.findOrderForPos(passportId, orderType, orderStatus));
        BasicResult basicResult = BasicResult.createSuccessResult();
        basicResult.setResponseDatas(orderPage);
        basicResult.getResponse().put("pageNum", pageNum);
        basicResult.getResponse().put("pageSize", pageSize);
        basicResult.getResponse().put("total", orderPage.getTotal());
        return basicResult;
    }
}
