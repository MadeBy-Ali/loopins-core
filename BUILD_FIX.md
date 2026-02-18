# 🔧 Build Instructions

## Fixed Midtrans Integration Issue

The Midtrans Java SDK uses **static configuration**, not dependency injection with Config objects.

### Changes Made:

1. **MidtransConfig.java** - Updated to use static Midtrans configuration
2. **MidtransPaymentService.java** - Updated to use static SnapApi methods

### Build the Project:

```bash
# Option 1: If you have Maven installed
mvn clean install

# Option 2: If you have IntelliJ IDEA
# File → Reload All from Disk
# Then: Right-click pom.xml → Maven → Reload Project

# Option 3: Build from IDE
# Build → Rebuild Project
```

### Verify Dependencies:

The following dependencies should be loaded:
- `com.midtrans:java-library:3.2.1`
- `org.json:json:20231013`

If IntelliJ shows import errors, try:
1. File → Invalidate Caches → Invalidate and Restart
2. Or: Maven → Reload Project (in Maven tool window)

### Start the Application:

```bash
./start.sh
```

Or manually:
```bash
mvn spring-boot:run
```

---

The compilation errors you see in the IDE are due to Maven dependencies not being reloaded. After running `mvn clean install` or reloading the Maven project in your IDE, all imports should resolve correctly.
