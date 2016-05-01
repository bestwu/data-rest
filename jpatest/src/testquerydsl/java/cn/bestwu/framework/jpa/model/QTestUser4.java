package cn.bestwu.framework.jpa.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QTestUser4 is a Querydsl query type for TestUser4
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QTestUser4 extends EntityPathBase<TestUser4> {

    private static final long serialVersionUID = -1565973978L;

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

    public QTestUser4(PathMetadata metadata) {
        super(TestUser4.class, metadata);
    }

}

