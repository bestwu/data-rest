package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.util.StringUtil;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.repository.support.Repositories;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.servlet.http.HttpServletRequest;

/**
 * {@code Device} 对应的监听器
 *
 * @author Peter Wu
 */
public class DeviceListener {
	/**
	 * 设置user-agent
	 *
	 * @param object entity
	 */
	@PrePersist
	@PreUpdate
	public void setDevice(Object object) {
		Repositories repositories = AutowireHelper.getBean(Repositories.class);
		PersistentEntity<?, ?> persistentEntity = repositories.getPersistentEntity(object.getClass());
		PersistentProperty<?> property = persistentEntity.getPersistentProperty(Device.class);
		if (property != null) {
			PersistentPropertyAccessor accessor = persistentEntity.getPropertyAccessor(object);
			HttpServletRequest request = AutowireHelper.getRequest();
			if (request != null) {
				accessor.setProperty(property, StringUtil.subStringWithEllipsis(request.getHeader("user-agent"), 200));
			}
		}
	}
}
