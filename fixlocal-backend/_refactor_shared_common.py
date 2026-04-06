from pathlib import Path
import shutil


BASE = Path("microservices")
COMMON = BASE / "common-lib"


def ensure_common_lib():
    common_java = COMMON / "src" / "main" / "java" / "com" / "fixlocal"
    (common_java / "exception").mkdir(parents=True, exist_ok=True)
    (common_java / "security").mkdir(parents=True, exist_ok=True)

    exception_sources = [
        BASE / "auth-service" / "src" / "main" / "java" / "com" / "fixlocal" / "exception",
        BASE / "user-service" / "src" / "main" / "java" / "com" / "fixlocal" / "exception",
    ]

    for source in exception_sources:
        if source.exists():
            for file in source.glob("*.java"):
                shutil.copy2(file, common_java / "exception" / file.name)

    jwt_candidates = sorted(BASE.glob("*/src/main/java/com/fixlocal/**/JwtService.java"))
    if jwt_candidates:
        shutil.copy2(jwt_candidates[0], common_java / "security" / "JwtService.java")

    pom = COMMON / "pom.xml"
    pom.write_text(
        """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<project xmlns=\"http://maven.apache.org/POM/4.0.0\"
         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.10</version>
        <relativePath/>
    </parent>

    <groupId>com.fixlocal.microservices</groupId>
    <artifactId>common-lib</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>fixlocal-common-lib</name>
    <description>Shared cross-cutting classes for FixLocal microservices</description>
    <packaging>jar</packaging>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.11.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
    </dependencies>
</project>
""",
        encoding="utf-8",
    )


def wire_and_cleanup():
    services = [
        "auth-service",
        "user-service",
        "booking-service",
        "chat-service",
        "notification-service",
        "payment-service",
        "review-service",
        "dispute-service",
        "testimonial-service",
    ]

    dep_block = (
        "\n        <dependency>\n"
        "            <groupId>com.fixlocal.microservices</groupId>\n"
        "            <artifactId>common-lib</artifactId>\n"
        "            <version>0.0.1-SNAPSHOT</version>\n"
        "        </dependency>\n"
    )

    for svc in services:
        pom = BASE / svc / "pom.xml"
        if pom.exists():
            text = pom.read_text(encoding="utf-8")
            if "<artifactId>common-lib</artifactId>" not in text and "</dependencies>" in text:
                text = text.replace("</dependencies>", dep_block + "\n\t</dependencies>")
                pom.write_text(text, encoding="utf-8")

        root = BASE / svc / "src" / "main" / "java" / "com" / "fixlocal"
        exc_dir = root / "exception"
        if exc_dir.exists():
            for file in exc_dir.glob("*.java"):
                file.unlink()
            try:
                exc_dir.rmdir()
            except OSError:
                pass

        jwt_file = root / "security" / "JwtService.java"
        if jwt_file.exists():
            jwt_file.unlink()


if __name__ == "__main__":
    ensure_common_lib()
    wire_and_cleanup()
    print("Shared common-lib created and service duplicates removed.")
