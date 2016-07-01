package cn.bestwu.framework.event.annotation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 事件处理类，注解的类中包含有注解了处理事件的注解的方法。
 * 例子：
 *
 * <pre class="code">
 *
 * &#064;RestEventHandler
 * public class MyEventHandler {
 *
 * &#064;HandleBeforeSave
 * public void handleBeforeSave(MyModel model) {
 *		 //do something
 * }
 *
 * </pre>
 *
 * @author Peter Wu
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Component
@ConditionalOnWebApplication
public @interface RestEventHandler {
}
