FROM maven:3.9-eclipse-temurin-21

WORKDIR /tests

COPY pom.xml .
RUN mvn -B -q dependency:go-offline || true

COPY src ./src

ENV API_BASE_URL=http://ecocompliance-api:8080 \
    API_WAIT_TIMEOUT_SECONDS=300 \
    CUCUMBER_FILTER_TAGS="not @defeito"

CMD ["sh", "-c", "mvn -B test -Dcucumber.filter.tags=\"$CUCUMBER_FILTER_TAGS\""]
