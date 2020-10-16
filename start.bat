IF EXIST PTM.jar (
  ECHO PTM.jar exists, will use jar
  java -ea -jar PTM.jar
) ELSE (
  ECHO PTM.jar not exists, will call class directly
  java -ea Main
)
