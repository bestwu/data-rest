package cn.bestwu.framework.data.query.jpa;

import cn.bestwu.framework.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.IndexedEmbedded;

import java.lang.annotation.Annotation;
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
	 * modelType类上注解了AnnotationType 的字段
	 *
	 * @param modelType      modelType
	 * @param AnnotationType AnnotationType
	 * @param <T>            T
	 * @return 字段名
	 */
	public static <T> String[] getAnnotationedFields(Class<T> modelType, Class<? extends Annotation> AnnotationType) {

		Set<String> fields = new HashSet<>();
		Arrays.stream(modelType.getDeclaredFields()).forEach(field -> addAnnotationedFields(fields, field, AnnotationType, null));

		if (log.isDebugEnabled()) {
			log.debug("查找到" + AnnotationType + "注解的字段：" + fields);
		}
		return fields.toArray(new String[fields.size()]);
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
				for (java.lang.reflect.Field fieldField : field.getType().getDeclaredFields()) {
					addAnnotationedFields(fields, fieldField, annotationType, fieldName);
				}
		}
	}
}
