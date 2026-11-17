FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN mkdir -p bin && javac -encoding UTF-8 -d bin src/*.java
EXPOSE 10000
CMD ["sh", "-c", "java -Dcesavet.puerto=${PORT:-10000} -cp bin Main"]
