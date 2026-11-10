package com.magao.backend.module.message.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.annotation.LogOperation;
import com.magao.backend.common.annotation.RequireRole;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.enums.UserRoleEnum;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.message.entity.Message;
import com.magao.backend.module.message.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 消息管理控制器
 * <p>
 * 提供消息管理相关的API接口：消息列表、发送、已读标记、未读数
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Tag(name = "消息管理", description = "消息发送、查询、已读标记等接口")
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController extends BaseController {

    private final MessageService messageService;

    @Operation(summary = "消息列表")
    @GetMapping("/list")
    public Result<Page<Message>> listMessages(PageDTO pageDTO,
                                              @RequestParam(required = false) Integer isRead) {
        return Result.success(messageService.listMessages(pageDTO, getCurrentUserId(), isRead));
    }

    @Operation(summary = "发送消息")
    @PostMapping("/send")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "消息管理", type = "新增", description = "发送消息")
    public Result<Void> sendMessage(@RequestParam Long receiverId,
                                   @RequestParam String title,
                                   @RequestParam String content,
                                   @RequestParam(required = false) Integer messageType) {
        messageService.sendMessage(receiverId, title, content, messageType);
        return Result.success();
    }

    @Operation(summary = "标记消息已读")
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable("id") Long messageId) {
        messageService.markRead(messageId, getCurrentUserId());
        return Result.success();
    }

    @Operation(summary = "标记全部消息已读")
    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        messageService.markAllRead(getCurrentUserId());
        return Result.success();
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@PathVariable("id") Long messageId) {
        messageService.deleteMessage(messageId, getCurrentUserId());
        return Result.success();
    }

    @Operation(summary = "未读消息数")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.success(messageService.getUnreadCount(getCurrentUserId()));
    }
}
