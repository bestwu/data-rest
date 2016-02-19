package cn.bestwu.framework.data.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QTestUser5 is a Querydsl query type for TestUser5
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QTestUser5 extends EntityPathBase<TestUser5> {

    private static final long serialVersionUID = 1098448410L;

    public static final QTestUser5 testUser5 = new QTestUser5("testUser5");

    public final StringPath firstName = createString("firstName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath lastName = createString("lastName");

    public QTestUser5(String variable) {
        super(TestUser5.class, forVariable(variable));
    }

    public QTestUser5(Path<? extends TestUser5> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTestUser5(PathMetadata<?> metadata) {
        super(TestUser5.class, metadata);
    }

}

