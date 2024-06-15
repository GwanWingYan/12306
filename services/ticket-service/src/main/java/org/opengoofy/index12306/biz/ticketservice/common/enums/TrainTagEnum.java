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

package org.opengoofy.index12306.biz.ticketservice.common.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 列车标签枚举
 */
@RequiredArgsConstructor
public enum TrainTagEnum {

    FU_XING("0", "复兴号"),

    HIGH_SPEED_TRAIN("1", "GC-高铁/城际");

    @Getter
    private final String code;

    @Getter
    private final String name;

    /**
     * 根据编码查找名称
     */
    public static String findNameByCode(String code) {
        return Arrays.stream(TrainTagEnum.values())
                .filter(each -> Objects.equals(each.getCode(), code))
                .findFirst()
                .map(TrainTagEnum::getName)
                .orElse(null);
    }

    /**
     * 根据编码查找名称
     */
    public static List<String> findNameByCode(List<String> codes) {
        List<String> resultNames = new ArrayList<>();
        for (String code : codes) {
            String name = Arrays.stream(TrainTagEnum.values())
                    .filter(each -> Objects.equals(each.getCode(), code))
                    .findFirst()
                    .map(TrainTagEnum::getName)
                    .orElse(null);
            if (StrUtil.isNotBlank(name)) {
                resultNames.add(name);
            }
        }
        return resultNames;
    }
}
