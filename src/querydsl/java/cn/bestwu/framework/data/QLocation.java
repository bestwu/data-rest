package cn.bestwu.framework.data;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QLocation is a Querydsl query type for Location
 */
@Generated("com.mysema.query.codegen.EmbeddableSerializer")
public class QLocation extends BeanPath<Location> {

    private static final long serialVersionUID = 1567179104L;

    public static final QLocation location = new QLocation("location");

    public final NumberPath<Double> lat = createNumber("lat", Double.class);

    public final NumberPath<Double> lng = createNumber("lng", Double.class);

    public QLocation(String variable) {
        super(Location.class, forVariable(variable));
    }

    public QLocation(Path<? extends Location> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLocation(PathMetadata<?> metadata) {
        super(Location.class, metadata);
    }

}

