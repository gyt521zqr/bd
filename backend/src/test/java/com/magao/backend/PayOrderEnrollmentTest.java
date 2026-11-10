package com.magao.backend;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.magao.backend.module.course.entity.Course;
import com.magao.backend.module.course.entity.CoursePurchase;
import com.magao.backend.module.course.mapper.CourseMapper;
import com.magao.backend.module.course.mapper.CoursePurchaseMapper;
import com.magao.backend.module.payment.service.OrderService;
import com.magao.backend.module.payment.vo.OrderItemVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证「支付成功后写入课程入学记录」增强项：
 * 直接调用 OrderService.payOrder（不经 HTTP/Redis），
 * 断言订单状态置为已支付后，CoursePurchaseListener 同步写入 course_purchase，
 * 且课程 student_count 自增 1。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
class PayOrderEnrollmentTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CoursePurchaseMapper coursePurchaseMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Test
    @Transactional
    void payOrder_shouldWriteCoursePurchaseAndIncrementStudentCount() {
        // 选一个真实存在的课程ID，避免 student_count 更新落在不存在的行上
        Course anyCourse = courseMapper.selectList(null).stream().findFirst().orElse(null);
        assertTrue(anyCourse != null, "测试库需至少存在一门课程");
        Long courseId = anyCourse.getId();

        // 使用一个不会与真实用户冲突的测试用户ID
        Long testUserId = 8_888_888L;

        // 前置清理（幂等保障）：确保该用户对该课程无残留入学记录
        coursePurchaseMapper.delete(new LambdaQueryWrapper<CoursePurchase>()
                .eq(CoursePurchase::getUserId, testUserId)
                .eq(CoursePurchase::getCourseId, courseId));

        int studentCountBefore = courseMapper.selectById(courseId).getStudentCount() == null
                ? 0 : courseMapper.selectById(courseId).getStudentCount();

        // 1) 创建课程订单（前端下单）
        OrderItemVO order = orderService.createOrderFrontend(testUserId, "course", courseId, null);
        Long orderId = order.getId();
        assertEquals("course", order.getProductType());

        // 2) 发起支付（支付成功钩子在此触发 OrderPaidEvent）
        orderService.payOrder(orderId, "wechat");

        // 3) 断言：course_purchase 已写入入学记录
        long purchaseCount = coursePurchaseMapper.selectCount(new LambdaQueryWrapper<CoursePurchase>()
                .eq(CoursePurchase::getUserId, testUserId)
                .eq(CoursePurchase::getCourseId, courseId));
        assertEquals(1, purchaseCount, "支付成功后应写入一条课程入学记录");

        // 4) 断言：课程学习人数 +1
        int studentCountAfter = courseMapper.selectById(courseId).getStudentCount();
        assertEquals(studentCountBefore + 1, studentCountAfter, "支付成功后课程学习人数应自增 1");

        // 5) 幂等二次支付：再次支付同一订单应被阻止（订单已支付），且不会重复写入
        try {
            orderService.payOrder(orderId, "wechat");
        } catch (Exception ignored) {
            // 预期抛 OPERATION_NOT_ALLOWED（订单已支付），忽略
        }
        long purchaseCountAfterRetry = coursePurchaseMapper.selectCount(new LambdaQueryWrapper<CoursePurchase>()
                .eq(CoursePurchase::getUserId, testUserId)
                .eq(CoursePurchase::getCourseId, courseId));
        assertEquals(1, purchaseCountAfterRetry, "重复支付不应产生额外入学记录");
    }
}
