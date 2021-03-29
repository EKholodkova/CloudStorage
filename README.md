
## О приложении
Облачное хранилище, предназначенное для хранения данных на сервере. Дает возможность манипулировать файлами -
загружать, скачивать, перемещать по папкам, копировать, есть возможность поиска. При регистрации для пользователя 
создается отдельный репозиторий. После удаления пользователя его репозиторий также удаляется.

Видео с демонстрацией работы: https://disk.yandex.ru/i/rO_QQs7s2lYfDA

## Из чего состоит
Включает в себя сервер и клиент. 

## Минимальные системные требования:
### Сервер
* JRE 15
* MySQL 8
### Клиент
* JRE 15

## Как пользоваться
Скачать релиз: https://github.com/EKholodkova/CloudStorage/releases/download/1.0/CloudStorage.1.0.zip

Распаковать.
### Сервер
Перед первым запуском сервера нужно создать базу данных Cloud_Storage_db

```CREATE DATABASE IF NOT EXISTS Cloud_Storage_db```

После чего восстановить резервную копию базы данных из приложенного sql скрипта командой:

```mysql -u[пользователь] -p[пароль_пользователя] [имя_базы] ‹ [название_файла_резервной_копии_базы].sql``` 

Например:

```mysql -uroot -p123 Cloud_Storage_db ‹ Cloud_Storage_db_users.sql```
 
Для запуска приложения необходимо запустить сервер командой:

```java -jar netty_Server-1.0-SNAPSHOT-jar-with-dependencies.jar```

либо можно запустить через скрипт csserver.sh (для linux и macOS) или csserver.bat (для windows).

Логин и пароль для доступа к базе данных MySQL можно передать через аргументы командной строки.

```./csserver.sh [-uUserName -pPassword]```

В противном случае сервер использует логин и пароль, заданные по умолчанию: username=root password=123.
 
После вывода в консоль "Server started" сервер готов к работе.

### Клиент
Клиент неинтерактивный, выполняет одну команду за раз.

Клиент запускается строкой:

```java -jar console_Client-1.0-SNAPSHOT-jar-with-dependencies.jar [-uUserName -pPassword] command [options]```

либо через скрипт csclient.sh (для linux и macOS) или csclient.bat (для windows):

```./csclient.sh [-uUserName -pPassword] command [options]```

* -uUserName: здесь UserName - логин пользователя (отсутствие пробела между -u и UserName обязательно)
* -pPassword: здесь Password - пароль пользователя (отсутствие пробела между -p и Password обязательно)
* command: имя команды
* options аргументы команды

#### Перечень доступных команд
* `change_psw newPassword` : замена существующего пароля на новый
* `copy srcFile dstFile` : копирование файла. Указывается путь исходного файла, путь файла-назначения
* `delete file` : удаление файла или директории. Удаление директории возможно в случае, если она пустая
* `delete_user` : удаление пользователя
* `download file` : скачивание файла из репозитория
* `help` : вывод списка доступных команд. можно вызывать без регистрации и работающего подключения к серверу. Не требует указания логина и пароля.  
* `ls [dirPath]` : вывод списка файлов в указанной директории. Если директория не указана, выводится список файлов корневой папки
* `mkdir dirName` : создание директории
* `move srcFile dstFile` : перемещение файла. Указывается путь исходного файла, путь файла-назначения
* `register` : регистрация пользователя
* `search fileName` : поиск указанного файла по всем директориям пользователя
* `upload srcFile` : загрузка файла в репозиторий. Указывается название локального файла. Загружается всегда в корневую директорию.

Команды register (добавление пользователя), delete_user (удаление пользователя) и help (вывод всех доступных команд) не требуют аргументов. Команду `help` 
можно вызывать без регистрации и работающего подключения к серверу. Все команды кроме `help` требуют указания логина и пароля (опции `-u` и `-p`).   

## Сборка приложения
### Требования
* JDK 15
* Maven
### Сборка
```
git clone https://github.com/EKholodkova/CloudStorage.git
cd CloudStorage
mvn package
cp csclient.* console_Client/target
cp csserver.* netty_Server/target
```
