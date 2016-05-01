package org.springframework.data.querydsl.binding;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.Property;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 修复entity有 get // set方法,无field时空指针异常bug
 *
 * @author Peter Wu
 */
public class FixQuerydslPredicateBuilder {

	private final ConversionService conversionService;
	private final MultiValueBinding<?, ?> defaultBinding;
	private final Map<PropertyPath, Path<?>> paths;
	private final EntityPathResolver resolver;

	/**
	 * Creates a new {@link QuerydslPredicateBuilder} for the given {@link ConversionService} and
	 * {@link EntityPathResolver}.
	 *
	 * @param conversionService must not be {@literal null}.
	 * @param resolver          can be {@literal null}.
	 */
	public FixQuerydslPredicateBuilder(ConversionService conversionService, EntityPathResolver resolver) {

		Assert.notNull(conversionService, "ConversionService must not be null!");

		this.defaultBinding = new QuerydslDefaultBinding();
		this.conversionService = conversionService;
		this.paths = new HashMap<>();
		this.resolver = resolver;
	}

	/**
	 * Creates a Querydsl {@link Predicate} for the given values, {@link QuerydslBindings} on the given
	 * {@link TypeInformation}.
	 *
	 * @param type     the type to create a predicate for.
	 * @param values   the values to bind.
	 * @param bindings the {@link QuerydslBindings} for the predicate.
	 * @return Predicate
	 */
	public Predicate getPredicate(TypeInformation<?> type, MultiValueMap<String, String> values,
			QuerydslBindings bindings) {

		Assert.notNull(bindings, "Context must not be null!");

		BooleanBuilder builder = new BooleanBuilder();

		if (values.isEmpty()) {
			return builder.getValue();
		}

		for (Map.Entry<String, List<String>> entry : values.entrySet()) {

			if (isSingleElementCollectionWithoutText(entry.getValue())) {
				continue;
			}

			String path = entry.getKey();

			if (!bindings.isPathVisible(path, type.getType())) {
				continue;
			}

			PropertyPath propertyPath = bindings.getPropertyPath(path, type);

			if (propertyPath == null) {
				continue;
			}

			Collection<Object> value = convertToPropertyPathSpecificType(entry.getValue(), propertyPath);
			Predicate predicate = invokeBinding(propertyPath, bindings, value);

			if (predicate != null) {
				builder.and(predicate);
			}
		}

		return builder.getValue();
	}

	/**
	 * Invokes the binding of the given values, for the given {@link PropertyPath} and {@link QuerydslBindings}.
	 *
	 * @param dotPath  must not be {@literal null}.
	 * @param bindings must not be {@literal null}.
	 * @param values   must not be {@literal null}.
	 * @return Predicate
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Predicate invokeBinding(PropertyPath dotPath, QuerydslBindings bindings, Collection<Object> values) {

		Path<?> path = getPath(dotPath, bindings);
		if (path == null) {
			return null;
		}

		MultiValueBinding binding = bindings.getBindingForPath(dotPath);
		binding = binding == null ? defaultBinding : binding;

		return binding.bind(path, values);
	}

	/**
	 * Returns the {@link Path} for the given {@link PropertyPath} and {@link QuerydslBindings}. Will try to obtain the
	 * {@link Path} from the bindings first but fall back to reifying it from the PropertyPath in case no specific binding
	 * has been configured.
	 *
	 * @param path     must not be {@literal null}.
	 * @param bindings must not be {@literal null}.
	 * @return Path
	 */
	private Path<?> getPath(PropertyPath path, QuerydslBindings bindings) {

		Path<?> resolvedPath = bindings.getExistingPath(path);

		if (resolvedPath != null) {
			return resolvedPath;
		}

		resolvedPath = paths.get(resolvedPath);

		if (resolvedPath != null) {
			return resolvedPath;
		}

		resolvedPath = reifyPath(path, null);
		paths.put(path, resolvedPath);

		return resolvedPath;
	}

	/**
	 * Tries to reify a Querydsl {@link Path} from the given {@link PropertyPath} and base.
	 *
	 * @param path must not be {@literal null}.
	 * @param base can be {@literal null}.
	 * @return Path
	 */
	private Path<?> reifyPath(PropertyPath path, Path<?> base) {

		Path<?> entityPath = base != null ? base : resolver.createPath(path.getOwningType().getType());

		Field field = ReflectionUtils.findField(entityPath.getClass(), path.getSegment());
		Object value = ReflectionUtils.getField(field, entityPath);

		if (path.hasNext()) {
			return reifyPath(path.next(), (Path<?>) value);
		}

		return (Path<?>) value;
	}

	/**
	 * Converts the given source values into a collection of elements that are of the given {@link PropertyPath}'s type.
	 * Considers a single element list with an empty {@link String} an empty collection because this basically indicates
	 * the property having been submitted but no value provided.
	 *
	 * @param source must not be {@literal null}.
	 * @param path   must not be {@literal null}.
	 * @return Collection
	 */
	private Collection<Object> convertToPropertyPathSpecificType(List<String> source, PropertyPath path) {

		PropertyPath leafProperty = path.getLeafProperty();
		Class<?> targetType = leafProperty.getOwningType().getProperty(leafProperty.getSegment()).getType();

		if (source.isEmpty() || isSingleElementCollectionWithoutText(source)) {
			return Collections.emptyList();
		}

		Collection<Object> target = new ArrayList<>(source.size());

		for (String value : source) {

			target.add(conversionService.canConvert(String.class, targetType)
					? conversionService.convert(value, TypeDescriptor.forObject(value), getTargetTypeDescriptor(path)) : value);
		}

		return target;
	}

	/**
	 * Returns the target {@link TypeDescriptor} for the given {@link PropertyPath} by either inspecting the field or
	 * property (the latter preferred) to pick up annotations potentially defined for formatting purposes.
	 *
	 * @param path must not be {@literal null}.
	 * @return TypeDescriptor
	 */
	private static TypeDescriptor getTargetTypeDescriptor(PropertyPath path) {

		PropertyPath leafProperty = path.getLeafProperty();
		Class<?> owningType = leafProperty.getOwningType().getType();

		PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(owningType, leafProperty.getSegment());

		if (descriptor == null) {
			return TypeDescriptor.nested(ReflectionUtils.findField(owningType, leafProperty.getSegment()), 0);
		}

		return TypeDescriptor.nested(
				new Property(owningType, descriptor.getReadMethod(), descriptor.getWriteMethod(), leafProperty.getSegment()),
				0);
	}

	/**
	 * Returns whether the given collection has exactly one element that doesn't contain any text. This is basically an
	 * indicator that a request parameter has been submitted but no value for it.
	 *
	 * @param source must not be {@literal null}.
	 * @return boolean
	 */
	private static boolean isSingleElementCollectionWithoutText(List<String> source) {
		return source.size() == 1 && !StringUtils.hasText(source.get(0));
	}
}