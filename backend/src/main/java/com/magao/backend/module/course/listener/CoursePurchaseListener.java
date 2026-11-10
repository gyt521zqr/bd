package com.magao.backend.module.course.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.magao.backend.module.course.entity.Course;
import com.magao.backend.module.course.entity.CoursePurchase;
import com.magao.backend.module.course.mapper.CourseMapper;
import com.magao.backend.module.course.mapper.CoursePurchaseMapper;
import com.magao.backend.module.payment.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 课程入学记录监听器。
 * <p>
 * 监听 {@link OrderPaidEvent}：当支付的商品类型为课程（productType=1）时，
 * 写入 {@code course_purchase} 入学记录，并累加课程学习人数。
 * 该监听器在 OrderServiceImpl.payOrder 的同一事务内同步执行，
 * 因此支付与入学记录要么同时成功、要么同时回滚，保证数据一致。
 * </p>
 *
 * @author Magao开发团队
 * @since 2026-11-08
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoursePurchaseListener {

    private final CoursePurchaseMapper coursePurchaseMapper;
    private final CourseMapper courseMapper;

    /** 课程订单支付成功后写入入学记录 */
    @EventListener
    public void onOrderPaid(OrderPaidEvent event) {
        // 仅处理课程类订单（productType == 1）；会员/积分商品由各自模块处理
        if (event.getProductType() == null || event.getProductType() != 1) {
            return;
        }
        Long userId = event.getUserId();
        Long courseId = event.getProductId();

        // 幂等：同一用户对同一课程已存在入学记录则跳过（表 uk_user_course 唯一索引兜底）
        long existing = coursePurchaseMapper.selectCount(
                new LambdaQueryWrapper<CoursePurchase>()
                        .eq(CoursePurchase::getUserId, userId)
                        .eq(CoursePurchase::getCourseId, courseId));
        if (existing > 0) {
            log.info("课程入学记录已存在，跳过写入：userId={}, courseId={}, orderId={}",
                    userId, courseId, event.getOrderId());
            return;
        }

        CoursePurchase purchase = new CoursePurchase();
        purchase.setUserId(userId);
        purchase.setCourseId(courseId);
        purchase.setOrderId(event.getOrderId());
        purchase.setPayAmount(event.getPayAmount());
        purchase.setExpireTime(null); // 购买后长期有效
        coursePurchaseMapper.insert(purchase);

        // 同步累加课程学习人数（student_count）
        courseMapper.update(null, new LambdaUpdateWrapper<Course>()
                .eq(Course::getId, courseId)
                .setSql("student_count = student_count + 1"));

        log.info("课程入学记录写入成功：userId={}, courseId={}, orderId={}",
                userId, courseId, event.getOrderId());
    }
}
