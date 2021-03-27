Основной функционал:
1. ААА - обязательно аутентификация и авторизация
2. Смена пароля, удаление аккаунта
3. Загрузка, скачивание файлов
4. 1 репозиторий - 1 юзер
5. Копирование, перемещение, удаление, сортировка файлов. Создание папок
6. Поиск файлов
7. Пометка на удаление / корзина
8. Ограничение на размер

Дополнительно по желанию:
*   1. Шифрование паролей
**  2. Древовидная структура (опция)
*** 3. Сбор статистики (на выбор)


##О приложении
Облачное хранилище, предназначенное для хранения данных на сервере. Дает возможность манипулировать файлами -
загружать, скачивать, перемещать по папкам, копировать, есть возможность поиска. При регистрации для пользователя 
создается отдельный репозиторий. После удаления пользователя его репозиторий также удаляется.

##Из чего состоит
Включает в себя сервер и клиент. 

##Минимальные системные требования:
###Сервер
* JRE 15
* MySQL 8
###Клиент
* JRE 15

##Как пользоваться
###Сервер
Для запуска приложения необходимо запустить сервер командой:

```java -jar netty_Server-1.0-SNAPSHOT-jar-with-dependencies.jar```
 
После вывода в консоль "Server started" сервер готов к работе.

###Клиент
Клиент неинтерактивный, выполняет одну команду за раз.

Клиент запускается строкой:

```java -jar console_Client-1.0-SNAPSHOT-jar-with-dependencies.jar [-uUserName -pPassword] command [options]```

* -uUserName: здесь UserName - логин пользователя (отсутствие пробела между -u и UserName обязательно)
* -pPassword: здесь Password - пароль пользователя (отсутствие пробела между -p и Password обязательно)
* command: имя команды
* options аргументы команды

####Перечень доступных команд
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

##Сборка приложения
###Требования
* JDK 15
* Maven
###
