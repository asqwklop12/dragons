package com.dragons.utils;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCleanUp implements InitializingBean {

  @PersistenceContext
  private EntityManager entityManager;

  private final List<String> tableNames = new ArrayList<>();

  @Override
  public void afterPropertiesSet() {
    entityManager.getMetamodel().getEntities().stream()
        .filter(entity -> entity.getJavaType().getAnnotation(Entity.class) != null)
        .map(entity -> {
          Table table = entity.getJavaType().getAnnotation(Table.class);
          return (table != null && !table.name().isEmpty())
              ? table.name()
              : entity.getJavaType().getSimpleName();
        })
        .forEach(tableNames::add);
  }

  @Transactional
  public void truncateAllTables() {
    entityManager.flush();
    entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

    for (String table : tableNames) {
      entityManager.createNativeQuery("TRUNCATE TABLE `" + table + "`").executeUpdate();
    }

    entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
  }
}
