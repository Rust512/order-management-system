package com.design.order_management_system.config;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@TestComponent
public class DatabaseCleanupExtension implements AfterEachCallback {

  @Override
  public void afterEach(@NonNull ExtensionContext context) {
    var jdbcTemplate = SpringExtension.getApplicationContext(context).getBean(JdbcTemplate.class);
    // 1. Fetch all user-defined tables in the 'public' schema
    List<String> tables =
        jdbcTemplate.queryForList(
            """
                        SELECT table_name FROM information_schema.tables
                        WHERE table_schema = 'public'
                            AND table_type = 'BASE TABLE'
                            AND table_name NOT IN ('flyway_schema_history', 'roles', 'app_users', 'user_role_mappings')
                        """,
            String.class);

    // 2. Execute the truncation with RESTART IDENTITY and CASCADE
    if (!tables.isEmpty()) {
      String tableList = String.join(", ", tables);
      String truncateQuery = "TRUNCATE TABLE " + tableList + " RESTART IDENTITY CASCADE";
      jdbcTemplate.execute(truncateQuery);
    }
  }
}
