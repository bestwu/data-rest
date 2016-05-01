package cn.bestwu.framework.jpa.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QTestUser is a Querydsl query type for TestUser
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QTestUser extends EntityPathBase<TestUser> {

    private static final long serialVersionUID = 1334958030L;

    public static final QTestUser testUser = new QTestUser("testUser");

    public final StringPath firstName = createString("firstName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath lastName = createString("lastName");

    public QTestUser(String variable) {
        super(TestUser.class, forVariable(variable));
    }

    public QTestUser(Path<? extends TestUser> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTestUser(PathMetadata metadata) {
        super(TestUser.class, metadata);
    }

}

