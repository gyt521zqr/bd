package com.magao.backend.common.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA路由转发控制器
 * <p>
 * 将所有非API、非静态资源的前端路由请求转发到index.html，
 * 由Vue Router处理客户端路由。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Controller
public class SpaForwardController {

    /**
     * 转发前端路由到index.html
     * <p>
     * 匹配所有不以/api/开头的路径，排除静态资源后缀，
     * 转发到/index.html由Vue Router处理。
     * </p>
     *
     * @return 转发到index.html
     */
    @GetMapping(value = {
            "/login",
            "/register",
            "/home",
            "/courses",
            "/courses/**",
            "/exams",
            "/exams/**",
            "/practice",
            "/practice/**",
            "/question-bank",
            "/question-bank/**",
            "/membership",
            "/profile",
            "/profile/**",
            "/growth",
            "/community",
            "/news",
            "/news/**",
            "/leaderboard",
            "/admin",
            "/admin/**",
            "/about",
            "/settings"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
