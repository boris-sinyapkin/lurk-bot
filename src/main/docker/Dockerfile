FROM azul/zulu-openjdk:17-latest

WORKDIR /home/lurk-bot/

ADD ./lurkbot*.jar .
ADD ./entrypoint.sh .

ENTRYPOINT ["bash", "entrypoint.sh"]