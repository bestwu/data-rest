package cn.bestwu.framework.data.query.jpa;

import cn.bestwu.framework.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * jpa搜索字段工具类
 *
 * @author Peter Wu
 */
@Slf4j
public class JpaSearchFieldUtil {

	/**
	 * domainType 类上注解了AnnotationType 的字段
	 *
	 * @param domainType      domainType
	 * @param AnnotationType AnnotationType
	 * @param <T>            T
	 * @return 字段名
	 */
	public static <T> String[] getAnnotationedFields(Class<T> domainType, Class<? extends Annotation> AnnotationType) {
		Set<String> fields = new HashSet<>();
		getAnnotationedFields(fields, domainType, AnnotationType, null);
		if (log.isDebugEnabled()) {
			log.debug("查找到" + AnnotationType + "注解的字段：" + fields);
		}
		return fields.toArray(new String[fields.size()]);
	}

	private static <T> void getAnnotationedFields(Set<String> fields, Class<T> domainType, Class<? extends Annotation> AnnotationType, String parentFieldName) {
		Arrays.stream(domainType.getDeclaredFields()).forEach(field -> addAnnotationedFields(fields, field, AnnotationType, parentFieldName));
		Arrays.stream(BeanUtils.getPropertyDescriptors(domainType)).forEach(propertyDescriptor -> addAnnotationedPropertys(fields, propertyDescriptor, AnnotationType, parentFieldName));
	}

	private static void addAnnotationedPropertys(Set<String> fields, PropertyDescriptor propertyDescriptor, Class<? extends Annotation> annotationType, String parentFieldName) {
		String propertyName = propertyDescriptor.getName();
		if (parentFieldName != null) {
			propertyName = parentFieldName + "." + propertyName;
		}
		Method readMethod = propertyDescriptor.getReadMethod();
		if (readMethod.isAnnotationPresent(annotationType)) {
			if (annotationType.equals(Field.class)) {
				Field fieldAnnotation = readMethod.getAnnotation(Field.class);
				FieldBridge bridge = fieldAnnotation.bridge();
				if (void.class.equals(bridge.impl())) {//默认只查询文本字段
					fields.add(propertyName);
				}
			} else {
				fields.add(propertyName);
			}
		} else if (readMethod.isAnnotationPresent(IndexedEmbedded.class)) {
			IndexedEmbedded annotation = readMethod.getAnnotation(IndexedEmbedded.class);
			if (annotation.depth() > StringUtil.countSubString(propertyName, "."))
				getAnnotationedFields(fields, readMethod.getReturnType(), annotationType, propertyName);
		}
	}

	private static void addAnnotationedFields(Set<String> fields, java.lang.reflect.Field field, Class<? extends Annotation> annotationType, String parentFieldName) {
		String fieldName = field.getName();
		if (parentFieldName != null) {
			fieldName = parentFieldName + "." + fieldName;
		}
		if (field.isAnnotationPresent(annotationType)) {
			if (annotationType.equals(Field.class)) {
				Field fieldAnnotation = field.getAnnotation(Field.class);
				FieldBridge bridge = fieldAnnotation.bridge();
				if (void.class.equals(bridge.impl())) {//默认只查询文本字段
					fields.add(fieldName);
				}
			} else {
				fields.add(fieldName);
			}
		} else if (field.isAnnotationPresent(IndexedEmbedded.class)) {
			IndexedEmbedded annotation = field.getAnnotation(IndexedEmbedded.class);
			if (annotation.depth() > StringUtil.countSubString(fieldName, "."))
				getAnnotationedFields(fields, field.getType(), annotationType, fieldName);
		}
	}
}
