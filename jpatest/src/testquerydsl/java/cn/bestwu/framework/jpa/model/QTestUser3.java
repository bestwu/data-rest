package cn.bestwu.framework.jpa.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QTestUser3 is a Querydsl query type for TestUser3
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QTestUser3 extends EntityPathBase<TestUser3> {

    private static final long serialVersionUID = -1565973979L;

    public static final QTestUser3 testUser3 = new QTestUser3("testUser3");

    public final StringPath firstName = createString("firstName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath lastName = createString("lastName");

    public QTestUser3(String variable) {
        super(TestUser3.class, forVariable(variable));
    }

    public QTestUser3(Path<? extends TestUser3> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTestUser3(PathMetadata metadata) {
        super(TestUser3.class, metadata);
    }

}

