/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengoofy.index12306.biz.ticketservice.service.handler.ticket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.opengoofy.index12306.biz.ticketservice.common.enums.VehicleSeatTypeEnum;
import org.opengoofy.index12306.biz.ticketservice.common.enums.VehicleTypeEnum;
import org.opengoofy.index12306.biz.ticketservice.dto.domain.PurchaseTicketPassengerDetailDTO;
import org.opengoofy.index12306.biz.ticketservice.dto.req.PurchaseTicketReqDTO;
import org.opengoofy.index12306.biz.ticketservice.service.CarriageService;
import org.opengoofy.index12306.biz.ticketservice.service.SeatService;
import org.opengoofy.index12306.biz.ticketservice.service.handler.ticket.base.AbstractTrainPurchaseTicketTemplate;
import org.opengoofy.index12306.biz.ticketservice.service.handler.ticket.dto.TrainPurchaseTicketRespDTO;
import org.opengoofy.index12306.framework.starter.cache.DistributedCache;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.opengoofy.index12306.biz.ticketservice.common.constant.RedisKeyConstant.TRAIN_STATION_REMAINING_TICKET;

/**
 * 高铁商务座购票组件
 */
@Component
@RequiredArgsConstructor
public class TrainBusinessClassPurchaseTicketHandler extends AbstractTrainPurchaseTicketTemplate {

    private final CarriageService carriageService;
    private final DistributedCache distributedCache;
    private final SeatService seatService;

    @Override
    public String mark() {
        return VehicleTypeEnum.HIGH_SPEED_RAIN.getName() + VehicleSeatTypeEnum.BUSINESS_CLASS.getName();
    }

    @Override
    protected List<TrainPurchaseTicketRespDTO> selectSeats(PurchaseTicketReqDTO requestParam) {
        List<PurchaseTicketPassengerDetailDTO> passengerDetails = requestParam.getPassengers();
        // 判断哪个车厢有座位。获取对应座位类型的车厢号集合，依次进行判断数据是否有余票
        List<String> trainCarriageList = carriageService.listCarriageNumber(requestParam.getTrainId(), requestParam.getPassengers().get(0).getSeatType());
        // 获取车厢余票
        List<Integer> trainStationCarriageRemainingTicket = seatService.listSeatRemainingTicket(requestParam.getTrainId(), requestParam.getDeparture(), requestParam.getArrival(), trainCarriageList);
        // 尽量让一起买票的乘车人在一个车厢
        String carriagesNumber;
        List<TrainPurchaseTicketRespDTO> actualResult = new ArrayList<>();
        for (int i = 0; i < trainStationCarriageRemainingTicket.size(); i++) {
            int remainingTicket = trainStationCarriageRemainingTicket.get(i);
            if (remainingTicket > passengerDetails.size()) {
                carriagesNumber = trainCarriageList.get(i);
                List<String> listAvailableSeat = seatService.listAvailableSeat(requestParam.getTrainId(), carriagesNumber);
                List<String> selectSeats = selectSeats(listAvailableSeat, passengerDetails.size());
                for (int j = 0; j < selectSeats.size(); j++) {
                    TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                    String seatNumber = selectSeats.get(j);
                    PurchaseTicketPassengerDetailDTO currentTicketPassenger = passengerDetails.get(j);
                    result.setSeatNumber(seatNumber);
                    result.setSeatType(currentTicketPassenger.getSeatType());
                    result.setCarriageNumber(carriagesNumber);
                    result.setPassengerId(currentTicketPassenger.getPassengerId());
                    actualResult.add(result);
                }
                break;
            }
        }
        // TODO 如果一个车厢不满足乘客人数，需要进行拆分
        // 扣减车厢余票缓存，扣减站点余票缓存
        if (CollUtil.isNotEmpty(actualResult)) {
            String keySuffix = StrUtil.join("_", requestParam.getTrainId(), requestParam.getDeparture(), requestParam.getArrival());
            StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
            stringRedisTemplate.opsForHash().increment(TRAIN_STATION_REMAINING_TICKET + keySuffix, String.valueOf(requestParam.getPassengers().get(0).getSeatType()), -passengerDetails.size());
        }
        return actualResult;
    }

    private static List<String> selectSeats(List<String> availableSeats, int requiredPassengers) {
        List<String> selectedSeats = new ArrayList<>();
        for (String seat : availableSeats) {
            if (selectedSeats.size() >= requiredPassengers) {
                break;
            }
            selectedSeats.add(seat);
        }
        return selectedSeats;
    }
}
