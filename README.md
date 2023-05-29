# Локальный поисковой движок SearchEngine по сайтам

## Описание 
Поисковый движок представляет собой Spring-приложение (JAR-файл, запускаемый на любом сервере или компьютере), работающее с локально установленной базой данных MySQL, имеющее простой веб-интерфейс и API, через который им можно управлять и получать результаты поисковой выдачи по запросу.

## Стек используемых технологий
- Java Core - основной язык программирования серверной части
- Spring Framework - фреймворк
- MySQL - основное хранилище данных
- Morphology Library Lucene - модуль морфологического анализа
- JSOUP - библиотека, используемая для анализа HTML документов.

### Принципы работы поискового движка:
1. В&nbsp;конфигурационном файле перед запуском приложения задаются
    адреса сайтов, по&nbsp;которым движок должен осуществлять поиск:
    application.yaml
```yaml
indexing-settings:
  s  sites:
    - url: https://www.lenta.ru
      name: Лента.ру
    - url: https://www.skillbox.ru
      name: Skillbox
    - url: https://www.playback.ru
      name: PlayBack.Ru
```
2. Поисковый движок должен самостоятельно обходить все страницы
    заданных сайтов и&nbsp;индексировать&nbsp;их (создавать так называемый индекс)
    так, чтобы потом находить наиболее релевантные страницы по&nbsp;любому
    поисковому запросу.
3. Пользователь присылает запрос через API движка. Запрос&nbsp;&mdash; это набор
    слов, по&nbsp;которым нужно найти страницы сайта.
4. Запрос определённым образом трансформируется в&nbsp;список слов,
    переведённых в&nbsp;базовую форму. Например, для существительных &mdash;
    именительный падеж, единственное число.
5. В&nbsp;индексе ищутся страницы, на&nbsp;которых встречаются все эти слова.
6. Результаты поиска ранжируются, сортируются и&nbsp;отдаются пользователю.
  
## Техническое описание проекта и проектных решений
1. Для доступа к&nbsp;БД был использован механизм JDBC API.
2. При полной индексации перечня сайтов задачи запускаются функцией execute() общего пула потоков для каждого сайта в&nbsp;цикле. Что приводит к&nbsp;последовательной обработке сайтов в&nbsp;многопоточном режиме.
3. Вкладка Dashboard открывается по&nbsp;умолчанию. На&nbsp;ней отображается общая статистика по&nbsp;всем сайтам, а&nbsp;также детальная статистика и&nbsp;статус по&nbsp;каждому из&nbsp;сайтов (статистика, получаемая по&nbsp;запросу /api/statistics).
4. На&nbsp;вкладке Management находятся инструменты управления поисковым движком&nbsp;&mdash; запуск и&nbsp;остановка полной индексации (переиндексации), а&nbsp;также возможность добавить (обновить) отдельную страницу по&nbsp;ссылке.
7. Страница Search предназначена для тестирования поискового движка. На&nbsp;ней находится поле поиска, выпадающий список с&nbsp;выбором сайта для поиска, а&nbsp;при нажатии на&nbsp;кнопку &laquo;Найти&raquo; выводятся результаты поиска 
(по&nbsp;API-запросу /api/search).
8. Статус &laquo;INDEXED&raquo; для сайта выставляется при индексации всех доступных страниц сайта.
