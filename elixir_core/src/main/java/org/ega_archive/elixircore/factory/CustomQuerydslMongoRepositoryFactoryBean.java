package org.ega_archive.elixircore.factory;

import org.ega_archive.elixircore.repository.CustomQuerydslMongoRepository;
import org.ega_archive.elixircore.repository.CustomQuerydslMongoRepositoryImpl;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;
import org.springframework.data.mongodb.repository.support.QueryDslMongoRepository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

public class CustomQuerydslMongoRepositoryFactoryBean<R extends QueryDslMongoRepository<T, I>, T, I extends Serializable>
    extends MongoRepositoryFactoryBean<R, T, I> {

  @Override
  protected RepositoryFactorySupport getFactoryInstance(MongoOperations operations) {
    return new CustomQuerydslMongodbRepositoryFactory<T, I>(operations);
  }

  public static class CustomQuerydslMongodbRepositoryFactory<T, I extends Serializable>
      extends MongoRepositoryFactory {

    private MongoOperations operations;

    public CustomQuerydslMongodbRepositoryFactory(MongoOperations mongoOperations) {
      super(mongoOperations);
      this.operations = mongoOperations;
    }

    @Override
    protected Object getTargetRepository(RepositoryMetadata metadata) {
      return new CustomQuerydslMongoRepositoryImpl(getEntityInformation(metadata.getDomainType()),
                                                   operations);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
      return CustomQuerydslMongoRepository.class;
    }
  }
}
