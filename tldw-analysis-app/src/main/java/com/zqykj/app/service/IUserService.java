package com.zqykj.app.service;


import com.zqykj.domain.user.User;
import com.zqykj.infrastructure.core.ServerResponse;

/**
 * <h1>用户服务: 创建 User 服务</h1>
 */
public interface IUserService {

    /**
     * <h2>创建用户</h2>
     * @param user {@link User}
     * @return {@link ServerResponse}
     * */
    ServerResponse createUser(User user) throws Exception;


    /**
     * <h2>查找所有用户列表</h2>
     * @param userId: 根据用户id查找
     * @return: com.zqykj.infrastructure.core.ServerResponse
     **/
    ServerResponse find(Long userId) throws Exception;


}
