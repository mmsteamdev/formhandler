**Obsługa formularza kontaktowego**

Obsługa formularza kontaktowego wymaga wysłania na adres serwisu Jsona z kluczami zgodnymi z nazwami kolumn w tabeli 'kontakt' w bazie danych. Nazwy kolumnm to:
- strona
- imie
- nazwisko
- mail
- telefon
- adres
- tresc

Żadne z tych pól nie jest wymagane. Mozna więc opuścić niektóre z nich. Zachowanie kolejności pól nie jest wymagane.

Serwis w responsie na request odsyła "success" bądź "failure" w zależności od wyniku operacji dodania rekordu do bazy danych.

Przykładowy Json do wysłania:

	{
    "imie" : "Jan",
    "nazwisko": "Nowak",
    "mail": "jan_nowak@example.com",
    "tresc": "Lorem ipsum?"
	}

Komenda do zbudowania:

	docker build -t form .	

Komenda do wystartowania:

	docker run -d -p 8080:4000 --link twojewykladzinybaza_wykladzinymysql_1:db --name form1 --net twojewykladzinybaza_default form
	
Adres serwisu postawionego lokalnie:

	http://localhost:8080/form?wsdl
