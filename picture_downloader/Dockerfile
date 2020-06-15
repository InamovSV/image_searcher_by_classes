FROM hseeberger/scala-sbt:8u212_1.2.8_2.12.8

RUN mkdir /app

ADD . /app

WORKDIR /app

RUN sbt assembly

ENTRYPOINT java -Xmx6G -jar -Dconfig.resource=dev.conf target/scala-2.12/Downloader.jar