from __future__ import annotations

import re
import shutil
from pathlib import Path


BASE_DIR = Path(__file__).resolve().parent
COMMON_LIB_DIR = BASE_DIR / "common-lib"
COMMON_JAVA_DIR = COMMON_LIB_DIR / "src" / "main" / "java" / "com" / "fixlocal"

SERVICES = [
    "auth-service",
    "admin-service",
    "user-service",
    "booking-service",
    "location-service",
    "chat-service",
    "dispute-service",
    "notification-service",
    "payment-service",
    "review-service",
    "testimonial-service",
]


def read_common_sources() -> tuple[str, str, str]:
    http_text = (COMMON_JAVA_DIR / "config" / "HttpClientConfig.java").read_text(encoding="utf-8")
    jwt_text = (COMMON_JAVA_DIR / "security" / "JwtService.java").read_text(encoding="utf-8")
    corr_text = (COMMON_JAVA_DIR / "observability" / "CorrelationIdFilter.java").read_text(encoding="utf-8")

    # Do not copy exception package dependency into services.
    jwt_text = re.sub(
        r"^\s*import\s+com\.fixlocal\.exception\.UnauthorizedException;\s*$\n?",
        "",
        jwt_text,
        flags=re.MULTILINE,
    )
    jwt_text = jwt_text.replace(
        'throw new UnauthorizedException("Invalid or expired JWT token");',
        'throw new IllegalArgumentException("Invalid or expired JWT token", e);',
    )

    return http_text, jwt_text, corr_text


def remove_common_lib_dependency(pom_path: Path) -> None:
    pom_text = pom_path.read_text(encoding="utf-8")
    dep_pattern = re.compile(
        r"\s*<dependency>\s*"
        r"<groupId>com\.fixlocal\.microservices</groupId>\s*"
        r"<artifactId>common-lib</artifactId>\s*"
        r"<version>0\.0\.1-SNAPSHOT</version>\s*"
        r"</dependency>\s*",
        flags=re.DOTALL,
    )
    updated = dep_pattern.sub("\n", pom_text)
    pom_path.write_text(updated, encoding="utf-8")


def remove_common_lib_module_from_root() -> None:
    root_pom = BASE_DIR / "pom.xml"
    text = root_pom.read_text(encoding="utf-8")
    text = re.sub(r"^\s*<module>common-lib</module>\s*$\n?", "", text, flags=re.MULTILINE)
    root_pom.write_text(text, encoding="utf-8")


def copy_common_sources_to_services(http_text: str, jwt_text: str, corr_text: str) -> None:
    for service in SERVICES:
        fixlocal_java = BASE_DIR / service / "src" / "main" / "java" / "com" / "fixlocal"
        (fixlocal_java / "config").mkdir(parents=True, exist_ok=True)
        (fixlocal_java / "security").mkdir(parents=True, exist_ok=True)
        (fixlocal_java / "observability").mkdir(parents=True, exist_ok=True)

        (fixlocal_java / "config" / "HttpClientConfig.java").write_text(http_text, encoding="utf-8")
        (fixlocal_java / "security" / "JwtService.java").write_text(jwt_text, encoding="utf-8")
        (fixlocal_java / "observability" / "CorrelationIdFilter.java").write_text(corr_text, encoding="utf-8")

        remove_common_lib_dependency(BASE_DIR / service / "pom.xml")


def main() -> None:
    http_text, jwt_text, corr_text = read_common_sources()
    copy_common_sources_to_services(http_text, jwt_text, corr_text)
    remove_common_lib_module_from_root()

    if COMMON_LIB_DIR.exists():
        shutil.rmtree(COMMON_LIB_DIR)

    print("common-lib migration completed")


if __name__ == "__main__":
    main()
