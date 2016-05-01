package cn.bestwu.framework.jpa.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QTestUser2 is a Querydsl query type for TestUser2
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QTestUser2 extends EntityPathBase<TestUser2> {

    private static final long serialVersionUID = -1565973980L;

    public static final QTestUser2 testUser2 = new QTestUser2("testUser2");

    public final StringPath firstName = createString("firstName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath lastName = createString("lastName");

    public QTestUser2(String variable) {
        super(TestUser2.class, forVariable(variable));
    }

    public QTestUser2(Path<? extends TestUser2> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTestUser2(PathMetadata metadata) {
        super(TestUser2.class, metadata);
    }

}

