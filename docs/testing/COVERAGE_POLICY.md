# Coverage policy

Coverage is evidence about executed code, not a substitute for assertions. Generated code may be excluded only with a documented reason. Security, provider authorization, and rollback logic must not be excluded merely to improve a percentage.

The repository currently has focused JVM tests for registry, shell safety, DNS/package validation, audio profiles, call recording models, and UI search/filter logic. A measured baseline must be collected before enforcing numeric thresholds.

