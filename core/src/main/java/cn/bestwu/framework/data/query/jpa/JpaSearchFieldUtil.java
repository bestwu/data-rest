package cn.bestwu.framework.data.query.jpa;

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
		return fields.toArray(new String[fields.size()]);
	}

	private static void addAnnotationedFields(Set<String> fields, java.lang.reflect.Field field, Class<? extends Annotation> annotationType, String parentFieldName) {
		String fieldName = field.getName();
		if (parentFieldName != null) {
			fieldName = parentFieldName + "." + fieldName;
		}
		if (field.isAnnotationPresent(annotationType)) {
			fields.add(fieldName);
		} else if (field.isAnnotationPresent(IndexedEmbedded.class)) {
			for (java.lang.reflect.Field fieldField : field.getType().getDeclaredFields()) {
				addAnnotationedFields(fields, fieldField, annotationType, fieldName);
			}
		}
	}
}
