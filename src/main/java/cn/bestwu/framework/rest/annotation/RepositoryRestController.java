package cn.bestwu.framework.rest.annotation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.*;

/**
 * 仓库类型的RestController
 *
 * @author Peter Wu
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
@ResponseBody
@ConditionalOnWebApplication
public @interface RepositoryRestController {
}
