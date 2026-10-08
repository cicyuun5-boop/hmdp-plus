package org.javaup.api;

import org.javaup.dto.Result;
import org.javaup.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 用户服务对外契约。
 * <p>
 * /user/inner/** 是供其他服务内部调用的接口，不参与用户态登录鉴权。
 */
@FeignClient(name = "hmdp-user-service", path = "/user", contextId = "userClient")
public interface UserClient {

    /**
     * 按用户 id 查询用户基础信息。
     */
    @GetMapping("/inner/{id}")
    Result<UserDTO> queryUserById(@PathVariable("id") Long userId);

    /**
     * 按用户 id 批量查询用户基础信息，返回顺序与入参一致。
     */
    @PostMapping("/inner/list")
    Result<List<UserDTO>> queryUsersByIds(@RequestBody List<Long> ids);

    /**
     * 查询用户会员等级。
     */
    @GetMapping("/inner/level/{userId}")
    Result<Integer> queryUserLevel(@PathVariable("userId") Long userId);

    /**
     * 按会员等级批量查询候选用户 id，最多返回 limit 条。
     */
    @GetMapping("/inner/level/users")
    Result<List<Long>> queryUserIdsByLevels(@RequestParam("levels") List<Integer> levels,
                                            @RequestParam("limit") int limit);
}
