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

package org.opengoofy.index12306.biz.userservice.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.opengoofy.index12306.biz.userservice.common.enums.UserChainMarkEnum;
import org.opengoofy.index12306.biz.userservice.dao.entity.UserDO;
import org.opengoofy.index12306.biz.userservice.dao.mapper.UserMapper;
import org.opengoofy.index12306.biz.userservice.dto.req.UserLoginReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.req.UserRegisterReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserLoginRespDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserRegisterRespDTO;
import org.opengoofy.index12306.biz.userservice.service.UserLoginService;
import org.opengoofy.index12306.framework.starter.cache.DistributedCache;
import org.opengoofy.index12306.framework.starter.common.toolkit.BeanUtil;
import org.opengoofy.index12306.framework.starter.convention.exception.ServiceException;
import org.opengoofy.index12306.framework.starter.designpattern.chain.AbstractChainContext;
import org.opengoofy.index12306.frameworks.starter.user.core.UserInfoDTO;
import org.opengoofy.index12306.frameworks.starter.user.toolkit.JWTUtil;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 用户登录接口实现
 */
@Service
@RequiredArgsConstructor
public class UserLoginServiceImpl implements UserLoginService {

    private final UserMapper userMapper;
    private final DistributedCache distributedCache;
    private final AbstractChainContext abstractChainContext;

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsernameOrMailOrPhone())
                .eq(UserDO::getPassword, requestParam.getPassword());
        UserDO userDO = userMapper.selectOne(queryWrapper);
        if (userDO != null) {
            UserInfoDTO userInfo = UserInfoDTO.builder()
                    .userId(String.valueOf(userDO.getId()))
                    .username(userDO.getUsername())
                    .realName(userDO.getRealName())
                    .build();
            String accessToken = JWTUtil.generateAccessToken(userInfo);
            UserLoginRespDTO actual = new UserLoginRespDTO(requestParam.getUsernameOrMailOrPhone(), userDO.getRealName(), accessToken);
            distributedCache.put(accessToken, JSON.toJSONString(actual), 30, TimeUnit.MINUTES);
            return actual;
        }
        throw new ServiceException("用户名不存在或密码错误");
    }

    @Override
    public UserLoginRespDTO checkLogin(String accessToken) {
        return distributedCache.get(accessToken, UserLoginRespDTO.class);
    }

    @Override
    public void logout(String accessToken) {
        if (StrUtil.isNotBlank(accessToken)) {
            distributedCache.delete(accessToken);
        }
    }

    @Override
    public Boolean hasUsername(String username) {
        // TODO 需要使用布隆过滤器防止缓存穿透
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        return userMapper.selectOne(queryWrapper) == null ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public UserRegisterRespDTO register(UserRegisterReqDTO requestParam) {
        abstractChainContext.handler(UserChainMarkEnum.USER_REGISTER_FILTER.name(), requestParam);
        int inserted = userMapper.insert(BeanUtil.convert(requestParam, UserDO.class));
        if (inserted < 1) {
            throw new ServiceException("用户注册失败");
        }
        return BeanUtil.convert(requestParam, UserRegisterRespDTO.class);
    }
}
