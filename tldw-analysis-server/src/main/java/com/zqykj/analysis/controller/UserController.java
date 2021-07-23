package com.zqykj.analysis.controller;

import com.zqykj.app.service.IUserService;
import com.zqykj.app.service.impl.UserServiceImpl;
import com.zqykj.domain.user.User;
import com.zqykj.infrastructure.core.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName UserController
 * @Description TODO
 * @Author zhangkehou
 * @Date 2021/7/22 15:40
 */
@RestController
@RequestMapping("/passbook")
public class UserController {

    /** 创建用户服务 */
    private final UserServiceImpl userServiceImpl;

    /** HttpServletRequest */
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public UserController(UserServiceImpl userServiceImpl,
                                HttpServletRequest httpServletRequest) {
        this.userServiceImpl = userServiceImpl;
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * <h2>创建用户</h2>
     * @param user {@link User}
     * */
    @ResponseBody
    @PostMapping("/createuser")
    ServerResponse createUser(@RequestBody User user) throws Exception {

        return userServiceImpl.createUser(user);
    }


    /**
     * <h2>用户获取评论信息</h2>
     * @param userId 用户 id
     * @return {@link ServerResponse}
     * */
    @ResponseBody
    @GetMapping("/getuserinfo")
    ServerResponse getFeedback(Long userId) throws Exception {


        return userServiceImpl.find(userId);
    }


}
