# Security testing

Security checks are release gates when they execute successfully. Findings are not converted into warnings.

The current test foundation covers shell-argument and intent/package validation in JVM tests. Static workflows additionally inspect Kotlin, Gradle, manifests, dependencies, and generated artifacts. Privileged shell tests must run only with isolated inputs and disposable devices.

Critical paths to extend next:

1. Provider authorization revocation during execution.
2. Display snapshot rollback.
3. File-scope and symlink traversal.
4. Binder disconnect and dead-object recovery.

No workflow should use `continue-on-error` or `|| true` to hide a security result.

