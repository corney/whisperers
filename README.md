# Результат выполнения тестового задания

## Описание задачи
AKKA messaging

Есть распределенные по сети ноды приложения, которые обмениваются друг с другом сообщениями.  
Каждая нода отправляет сообщение, по умолчанию, не реже, чем раз в 100 мс (Ts). 
Каждое сообщение должно быть, рано или поздно, отправлено каждой из соседних нод. 
Чем быстрее, тем лучше. Требования обязательной доставки нет. 
Количество нод может меняться динамически, в процессе работы.  
Получив сообщение каждая нода должна выводить в консоль или лог 
информацию о том, сколько, примерно, сообщений было обработано за 
последнюю секунду. 

Необходимо на базе AKKA remote на одной машине эмулировать кластер 
распределенных в сети нод приложения и реализовать средство, которое позволит:

* добавлять и убирать ноды приложения из набора работающих  нод
* оценить количество сообщений обрабатываемых в секунду каждой нодой
* менять интервал отправки на всех нодах. Т.е. не для каждой в отдельности, а всем сразу.
* оценить предел производительности системы. Пределом можно считать 
  параметры системы, при которых добавление новых нод 
  или сокращение интервала отправки сообщений не приводит к росту 
  сообщений, обрабатываемых  в секунду.

## Замечания по реализации

- По умолчанию система логгирования настроена таким образом: логи всех уровней отправляются в
файл `logs/application.log`
- Логи с уровнем INFO и выше отображаются в консоль
- При поступлении каждого сообщения статистика за последнюю секунду выводится с уровнем DEBUG

## Инструкция по развертыванию

Для сборки системы используется система управления проектом `sbt`
Чтобы получить jar-файл, необходимо выполнить команду
```sbt clean assembly```

Будет собран результирующий jar-файл `target/scala-2.11/whisperers-assembly-1.0.jar`

Для запуска системы нужно вызвать команду
```java -Xmx4G -jar target/scala-2.11/whisperers-assembly-1.0.jar -h```
Система распечатает короткую инструкцию по использованию. Параметры jvm следует выбрать соответственно предполагаемому
количеству запускаемых нод.

Используются следующие опции:
- -w n: запустить jvm с порождением n экземпляров нод типа whisperer. При этом система проверяет, доступны ли сокеты
 127.0.0.1:2551 и 127.0.0.1:2552. Первый запущеный экземпляр jvm слушает порт 2551, второй запущеный экземпляр jvm
 пытается слушать порт 2552. Это - управляющие порты кластера. Эта опция взаимоисключающая с остальными опциями,
 если она указана, остальные опции будут проигнорированы.
- -m: вывести в лог статистику по посланным сообщениям за последнюю секунду
- -d ms: установить промежуток между отсылкой сообщений в ms миллисекунд
- -s n: породить n нод в одной из jvm. Данная опция может породить `OutOfMemoryError` в случае, если jvm запущена с
параметрами, недостаточными для запуска указанного количества нод
- -r n: остановить n нод.

## Предел производительности системы

- При попытке запустить на локальном компьютере больше 20 нод одновременно при времени задержки меньше 50 миллисекунд
система начинает повышать значение `Load Average` до 70-80. Оптимальное количество нод на локальном компьютере - 15
- При 15 одновременно запущеных нодах при уменьшении времени задержки до 6 миллисекунд количество обрабатываемых
запросов растет практически линейно. Дальше наступает предел:

+--------------+------------------+----------------+
| Задержка, мс | Запросов на ноду| Всего запросов |
+--------------+------------------+----------------+
| 100          | 143              | 2145           |
+--------------+------------------+----------------+
| 50           | 288              | 4321           |
+--------------+------------------+----------------+
| 25           | 574              | 8611           |
+--------------+------------------+----------------+
| 12           | 1193             | 17895          |
+--------------+------------------+----------------+
| 6            | 1455             | 21825          |
+--------------+------------------+----------------+
| 3            | 1468             | 22020          |
+--------------+------------------+----------------+
| 1            | 1497             | 22455          |
+--------------+------------------+----------------+
