package cn.bestwu.framework.mongodb.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QTestUser is a Querydsl query type for TestUser
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QTestUser extends EntityPathBase<TestUser> {

    private static final long serialVersionUID = -1794564649L;

    public static final QTestUser testUser = new QTestUser("testUser");

    public final StringPath firstName = createString("firstName");

    public final StringPath id = createString("id");

    public final StringPath lastName = createString("lastName");

    public QTestUser(String variable) {
        super(TestUser.class, forVariable(variable));
    }

    public QTestUser(Path<? extends TestUser> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTestUser(PathMetadata<?> metadata) {
        super(TestUser.class, metadata);
    }

}

