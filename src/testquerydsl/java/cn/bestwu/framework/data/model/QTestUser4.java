package cn.bestwu.framework.data.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QTestUser4 is a Querydsl query type for TestUser4
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QTestUser4 extends EntityPathBase<TestUser4> {

    private static final long serialVersionUID = 1098448409L;

    public static final QTestUser4 testUser4 = new QTestUser4("testUser4");

    public final StringPath firstName = createString("firstName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath lastName = createString("lastName");

    public QTestUser4(String variable) {
        super(TestUser4.class, forVariable(variable));
    }

    public QTestUser4(Path<? extends TestUser4> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTestUser4(PathMetadata<?> metadata) {
        super(TestUser4.class, metadata);
    }

}

