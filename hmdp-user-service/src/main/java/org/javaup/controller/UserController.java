package org.javaup.controller;


import cn.hutool.core.bean.BeanUtil;
import org.javaup.dto.LoginFormDTO;
import org.javaup.dto.Result;
import org.javaup.dto.UserDTO;
import org.javaup.entity.User;
import org.javaup.entity.UserInfo;
import org.javaup.service.IUserInfoService;
import org.javaup.service.IUserService;
import org.javaup.utils.UserHolder;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @program: 黑马点评-plus升级版实战项目。添加 阿星不是程序员 微信，添加时备注 点评 来获取项目的完整资料
 * @description: 用户api
 * @author: 阿星不是程序员
 **/
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 发送手机验证码
     */
    @PostMapping("code")
    public Result<String> sendCode(@RequestParam("phone") String phone, HttpSession session) {
        // 发送短信验证码并保存验证码
        return userService.sendCode(phone, session);
    }

    /**
     * 登录功能
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginFormDTO loginForm, HttpSession session){
        // 实现登录功能
        return userService.login(loginForm, session);
    }

    /**
     * 登出功能
     * @return 无
     */
    @PostMapping("/logout")
    public Result<Void> logout(){
        // TODO 实现登出功能
        return Result.fail("功能未完成");
    }

    @GetMapping("/me")
    public Result<UserDTO> me(){
        // 获取当前登录的用户并返回
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }

    @GetMapping("/info/{id}")
    public Result<UserInfo> info(@PathVariable("id") String userId){
        // 查询详情
        UserInfo info = userInfoService.getById(Long.parseLong(userId));
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }

    /**
     * 当前登录用户更新等级
     */
    @PostMapping("/level/update")
    public Result<Void> updateLevel(@RequestParam("newLevel") Integer newLevel) {
        UserDTO current = UserHolder.getUser();
        if (Objects.isNull(current)) {
            return Result.fail("未登录");
        }
        return userInfoService.updateUserLevel(current.getId(), newLevel);
    }

    @GetMapping("/{id}")
    public Result<UserDTO> queryUserById(@PathVariable("id") Long userId){
        // 查询详情
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 返回
        return Result.ok(userDTO);
    }

    @GetMapping("/inner/{id}")
    public Result<UserDTO> innerQueryUserById(@PathVariable("id") Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        return Result.ok(BeanUtil.copyProperties(user, UserDTO.class));
    }

    @PostMapping("/inner/list")
    public Result<List<UserDTO>> innerQueryUsersByIds(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        String idStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<UserDTO> dtos = userService.query()
                .in("id", ids)
                .last("ORDER BY FIELD(id," + idStr + ")")
                .list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(dtos);
    }

    @PostMapping("/sign")
    public Result<Void> sign(){
        return userService.sign();
    }

    @GetMapping("/sign/count")
    public Result signCount(){
        return userService.signCount();
    }

    @GetMapping("/inner/level/{userId}")
    public Result<Integer> innerQueryUserLevel(@PathVariable("userId") Long userId) {
        UserInfo userInfo = userInfoService.getByUserId(userId);
        if (userInfo == null) {
            return Result.fail("用户不存在");
        }
        return Result.ok(userInfo.getLevel());
    }

    @GetMapping("/inner/level/users")
    public Result<List<Long>> innerQueryUserIdsByLevels(@RequestParam("levels") List<Integer> levels,
                                                       @RequestParam("limit") int limit) {
        if (levels == null || levels.isEmpty() || limit <= 0) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> userIds = userInfoService.lambdaQuery()
                .select(UserInfo::getUserId)
                .in(UserInfo::getLevel, levels)
                .last("limit " + limit)
                .list()
                .stream()
                .map(UserInfo::getUserId)
                .collect(Collectors.toList());
        return Result.ok(userIds);
    }
}