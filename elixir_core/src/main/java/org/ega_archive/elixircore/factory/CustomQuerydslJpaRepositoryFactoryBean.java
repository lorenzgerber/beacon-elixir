package org.ega_archive.elixircore.factory;

import org.ega_archive.elixircore.repository.CustomQuerydslJpaRepository;
import org.ega_archive.elixircore.repository.CustomQuerydslJpaRepositoryImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

import javax.persistence.EntityManager;


public class CustomQuerydslJpaRepositoryFactoryBean<R extends JpaRepository<T, I>, T, I extends Serializable>
    extends JpaRepositoryFactoryBean<R, T, I> {

  protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {

    return new CustomQuerydslJPARepositoryFactory<T, I>(entityManager);
  }

  private static class CustomQuerydslJPARepositoryFactory<T, I extends Serializable>
      extends JpaRepositoryFactory {

    private EntityManager entityManager;

    public CustomQuerydslJPARepositoryFactory(EntityManager entityManager) {
      super(entityManager);
      this.entityManager = entityManager;
    }

    @Override
    protected <T, ID extends Serializable> SimpleJpaRepository<?, ?> getTargetRepository(
        RepositoryMetadata metadata, EntityManager entityManager) {

      JpaEntityInformation<?, Serializable> entityInformation =
          getEntityInformation(metadata.getDomainType());
      return new CustomQuerydslJpaRepositoryImpl(entityInformation, entityManager);
    }

    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {

      return CustomQuerydslJpaRepository.class;
    }
  }
}
