package pl.kawaleria.auctsys.configs

import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import pl.kawaleria.auctsys.categories.domain.CategoryBuilder
import pl.kawaleria.auctsys.categories.domain.CategoryRepository

@ChangeLog(order = "002")
class CategoryDatabaseChangeLog {

    @ChangeSet(order = "001", id = "insertAntiquesCategoryWithSubcategories", author = "filip-kaminski")
    fun insertAntiquesCategoryWithSubcategories(categoryRepository: CategoryRepository) {

        // Antyki i sztuka
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Antyki i sztuka")
            .description("Antyki, sztuka oraz inne dzieła")
            .topLevel()

            // Antyki
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Antyki")
                    .description("Antyczne przedmioty")
                    .finalNode()
            )

            // Kolekcje
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Kolekcje")
                    .description("Przedmioty kolekcjonerskie")
                    .finalNode()
            )

            // Sztuka
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Sztuka")
                    .description("Dzieła sztuki")
                    .finalNode()
            )

            // Rękodzieło
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Rękodzieło")
                    .description("Wszelakie rękodzieła")
                    .finalNode()
            )
            .save()
    }

    @ChangeSet(order = "002", id = "insertMotoringCategoryWithSubcategories", author = "filip-kaminski")
    fun insertMotoringCategoryWithSubcategories(categoryRepository: CategoryRepository) {

        // Motoryzacja
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Motoryzacja")
            .description("Samochody, motocykle, dostawcze, ciężarowe, budowlane, przyczepy, naczepy, części samochodowe, części motocyklowe, opony i felgi, sprzęt car audio, pozostała motoryzacja, wyposażenie i akcesoria samochodowe")
            .topLevel()

            // Samochody Osobowe
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Samochody Osobowe")
                    .description("Sprzedaż samochodów osobowych")
                    .finalNode()
            )

            // Motocykle i Skutery
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Motocykle i Skutery")
                    .description("Sprzedaż motocykli i skuterów")
                    .finalNode()
            )

            // Dostawcze
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Dostawcze")
                    .description("Sprzedaż samochodów dostawczych")
                    .finalNode()
            )

            // Ciężarowe
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Ciężarowe")
                    .description("Sprzedaż samochodów ciężarowych")
                    .finalNode()
            )

            // Budowlane
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Budowlane")
                    .description("Sprzęt budowlany")
                    .finalNode()
            )

            // Przyczepy i Naczepy
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Przyczepy i Naczepy")
                    .description("Sprzedaż przyczep i naczep")
                    .finalNode()
            )

            // Części Samochodowe
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Części Samochodowe")
                    .description("Części zamienne do samochodów")
                    .finalNode()
            )

            // Części Motocyklowe
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Części Motocyklowe")
                    .description("Części zamienne do motocykli")
                    .finalNode()
            )

            // Opony i Felgi
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Opony i Felgi")
                    .description("Opony i felgi do samochodów")
                    .finalNode()
            )

            // Sprzęt Car Audio
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Sprzęt Car Audio")
                    .description("Sprzęt car audio do samochodów")
                    .finalNode()
            )

            // Pozostała Motoryzacja
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Pozostała Motoryzacja")
                    .description("Inne oferty z kategorii motoryzacyjnej")
                    .finalNode()
            )

            // Wyposażenie i Akcesoria Samochodowe
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Wyposażenie i Akcesoria Samochodowe")
                    .description("Akcesoria i wyposażenie do samochodów")
                    .finalNode()
            )
            .save()
    }

    @ChangeSet(order = "003", id = "insertRealEstatesCategoryWithSubcategories", author = "filip-kaminski")
    fun insertRealEstatesCategoryWithSubcategories(categoryRepository: CategoryRepository) {

        // Nieruchomości
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Nieruchomości")
            .description("Mieszkania, domy, działki, biura, lokale, garaże, parkingi, stancje, pokoje, hale, magazyny, pozostałe nieruchomości, nieruchomości za granicą")
            .topLevel()

            // Mieszkania
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Mieszkania")
                    .description("Oferty sprzedaży i wynajmu mieszkań")
                    .finalNode()
            )

            // Domy
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Domy")
                    .description("Oferty sprzedaży i wynajmu domów")
                    .finalNode()
            )

            // Działki
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Działki")
                    .description("Oferty sprzedaży działek")
                    .finalNode()
            )

            // Biura i Lokale
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Biura i Lokale")
                    .description("Oferty biur i lokali na sprzedaż i wynajem")
                    .finalNode()
            )

            // Garaże i Parkingi
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Garaże i Parkingi")
                    .description("Oferty garaży i miejsc parkingowych")
                    .finalNode()
            )

            // Stancje i Pokoje
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Stancje i Pokoje")
                    .description("Oferty stancji i pokoi do wynajęcia")
                    .finalNode()
            )

            // Hale i Magazyny
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Hale i Magazyny")
                    .description("Oferty hal i magazynów")
                    .finalNode()
            )

            // Pozostałe Nieruchomości
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Pozostałe Nieruchomości")
                    .description("Inne oferty nieruchomościowe")
                    .finalNode()
            )

            // Nieruchomości za Granicą
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Nieruchomości za Granicą")
                    .description("Oferty nieruchomości za granicą")
                    .finalNode()
            )
            .save()
    }

    @ChangeSet(order = "004", id = "insertJobCategoryWithSubcategories", author = "filip-kaminski")
    fun insertJobCategoryWithSubcategories(categoryRepository: CategoryRepository) {

        // Praca
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Praca")
            .description("Oferty pracy w różnych dziedzinach")
            .topLevel()

            // Administracja Biurowa
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Administracja Biurowa")
                    .description("Oferty pracy w administracji biurowej")
                    .finalNode()
            )

            // Badania i Rozwój
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Badania i Rozwój")
                    .description("Oferty pracy w dziale badań i rozwoju")
                    .finalNode()
            )

            // Budowa/Remonty
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Budowa/Remonty")
                    .description("Oferty pracy w budowie i remontach")
                    .finalNode()
            )

            // Dostawca
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Dostawca")
                    .description("Oferty pracy dla dostawców")
                    .finalNode()
            )

            // Kurier Miejski
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Kurier Miejski")
                    .description("Oferty pracy dla kurierów miejskich")
                    .finalNode()
            )

            // E-commerce
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("E-commerce")
                    .description("Oferty pracy w e-commerce")
                    .finalNode()
            )

            // Edukacja
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Edukacja")
                    .description("Oferty pracy w dziedzinie edukacji")
                    .finalNode()
            )

            // Energetyka
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Energetyka")
                    .description("Oferty pracy w energetyce")
                    .finalNode()
            )

            // Finanse/Księgowość
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Finanse/Księgowość")
                    .description("Oferty pracy w dziedzinie finansów i księgowości")
                    .finalNode()
            )

            // Franczyza/Własna Firma
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Franczyza/Własna Firma")
                    .description("Oferty pracy związane z franczyzą i własną firmą")
                    .finalNode()
            )

            // Fryzjerstwo/Kosmetyka
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Fryzjerstwo/Kosmetyka")
                    .description("Oferty pracy w fryzjerstwie i kosmetyce")
                    .finalNode()
            )

            // Gastronomia
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Gastronomia")
                    .description("Oferty pracy w gastronomii")
                    .finalNode()
            )

            // HR
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("HR")
                    .description("Oferty pracy w dziale zasobów ludzkich")
                    .finalNode()
            )

            // Hostessa/Roznoszenie Ulotek
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Hostessa/Roznoszenie Ulotek")
                    .description("Oferty pracy dla hostess i osób roznoszących ulotki")
                    .finalNode()
            )

            // Hotelarstwo
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Hotelarstwo")
                    .description("Oferty pracy w hotelarstwie")
                    .finalNode()
            )

            // Inżynieria
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Inżynieria")
                    .description("Oferty pracy w dziedzinie inżynierii")
                    .finalNode()
            )

            // IT/Telekomunikacja
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("IT/Telekomunikacja")
                    .description("Oferty pracy w dziedzinie IT i telekomunikacji")
                    .finalNode()
            )

            // Kierowca
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Kierowca")
                    .description("Oferty pracy dla kierowców")
                    .finalNode()
            )

            // Logistyka/Zakupy/Spedycja
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Logistyka/Zakupy/Spedycja")
                    .description("Oferty pracy w dziedzinie logistyki, zakupów i spedycji")
                    .finalNode()
            )

            // Marketing i PR
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Marketing i PR")
                    .description("Oferty pracy w dziedzinie marketingu i public relations")
                    .finalNode()
            )

            // Mechanika i Lakiernictwo
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Mechanika i Lakiernictwo")
                    .description("Oferty pracy w dziedzinie mechaniki i lakiernictwa")
                    .finalNode()
            )

            // Montaż i Serwis
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Montaż i Serwis")
                    .description("Oferty pracy w dziedzinie montażu i serwisu")
                    .finalNode()
            )

            // Obsługa Klienta i Call Center
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Obsługa Klienta i Call Center")
                    .description("Oferty pracy w obszarze obsługi klienta i call center")
                    .finalNode()
            )

            // Ochrona
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Ochrona")
                    .description("Oferty pracy w ochronie")
                    .finalNode()
            )

            // Opieka
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Opieka")
                    .description("Oferty pracy w opiece")
                    .finalNode()
            )

            // Praca za Granicą
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Praca za Granicą")
                    .description("Oferty pracy za granicą")
                    .finalNode()
            )

            // Prace Magazynowe
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Prace Magazynowe")
                    .description("Oferty pracy w magazynach")
                    .finalNode()
            )

            // Pracownik Sklepu
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Pracownik Sklepu")
                    .description("Oferty pracy dla pracowników sklepów")
                    .finalNode()
            )

            // Produkcja
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Produkcja")
                    .description("Oferty pracy w produkcji")
                    .finalNode()
            )

            // Rolnictwo i Ogrodnictwo
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Rolnictwo i Ogrodnictwo")
                    .description("Oferty pracy w rolnictwie i ogrodnictwie")
                    .finalNode()
            )

            // Sprzątanie
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Sprzątanie")
                    .description("Oferty pracy w dziedzinie sprzątania")
                    .finalNode()
            )

            // Sprzedaż
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Sprzedaż")
                    .description("Oferty pracy w sprzedaży")
                    .finalNode()
            )

            // Wykładanie i Ekspozycja Towaru
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Wykładanie i Ekspozycja Towaru")
                    .description("Oferty pracy w wykładaniu i ekspozycji towaru")
                    .finalNode()
            )

            // Zdrowie
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Zdrowie")
                    .description("Oferty pracy w dziedzinie zdrowia")
                    .finalNode()
            )

            // Pozostałe Oferty Pracy
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Pozostałe Oferty Pracy")
                    .description("Inne oferty pracy")
                    .finalNode()
            )

            // Praktyki/Staże
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Praktyki/Staże")
                    .description("Oferty praktyk i staży")
                    .finalNode()
            )

            // Kadra Kierownicza
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Kadra Kierownicza")
                    .description("Oferty pracy dla kadry kierowniczej")
                    .finalNode()
            )

            // Praca Sezonowa
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Praca Sezonowa")
                    .description("Oferty pracy sezonowej")
                    .finalNode()
            )

            // Praca Dodatkowa
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Praca Dodatkowa")
                    .description("Oferty pracy dodatkowej")
                    .finalNode()
            )
            .save()
    }

    @ChangeSet(order = "005", id = "insertHomeAndGardenCategoryWithSubcategories", author = "filip-kaminski")
    fun insertHomeAndGardenCategoryWithSubcategories(categoryRepository: CategoryRepository) {

        // Dom i Ogród
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Dom i Ogród")
            .description("Budowa, instalacje, meble, ogród, narzędzia, ogrzewanie, oświetlenie, supermarket, wykończenia wnętrz, wyposażenie wnętrz, pozostałe dom i ogród")
            .topLevel()

            // Budowa
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Budowa")
                    .description("Materiały i usługi budowlane")
                    .finalNode()
            )

            // Instalacje
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Instalacje")
                    .description("Usługi instalacyjne")
                    .finalNode()
            )

            // Meble
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Meble")
                    .description("Sprzedaż mebli")
                    .finalNode()
            )

            // Ogród
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Ogród")
                    .description("Produkty ogrodnicze")
                    .finalNode()
            )

            // Narzędzia
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Narzędzia")
                    .description("Narzędzia do domu i ogrodu")
                    .finalNode()
            )

            // Ogrzewanie
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Ogrzewanie")
                    .description("Sprzęt do ogrzewania")
                    .finalNode()
            )

            // Oświetlenie
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Oświetlenie")
                    .description("Oświetlenie do domu i ogrodu")
                    .finalNode()
            )

            // Supermarket
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Supermarket")
                    .description("Produkty spożywcze i inne artykuły")
                    .finalNode()
            )

            // Wykończenia Wnętrz
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Wykończenia Wnętrz")
                    .description("Materiały do wykończenia wnętrz")
                    .finalNode()
            )

            // Wyposażenie Wnętrz
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Wyposażenie Wnętrz")
                    .description("Akcesoria do wnętrz domu")
                    .finalNode()
            )

            // Pozostałe Dom i Ogród
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Pozostałe Dom i Ogród")
                    .description("Inne produkty i usługi związane z domem i ogrodem")
                    .finalNode()
            )
            .save()
    }

    @ChangeSet(order = "006", id = "insertElectronicsCategoryWithSubcategories", author = "filip-kaminski")
    fun insertElectronicsCategoryWithSubcategories(categoryRepository: CategoryRepository) {

        // Elektronika
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Elektronika")
            .description("Fotografia, gry i konsole, komputery, smartwatche i opaski, sprzęt agd, sprzęt audio, sprzęt video, telefony, tv, pozostała elektronika")
            .topLevel()

            // Fotografia
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Fotografia")
                    .description("Sprzęt fotograficzny")
                    .finalNode()
            )

            // Gry i Konsole
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Gry i Konsole")
                    .description("Gry komputerowe i konsole")
                    .finalNode()
            )

            // Komputery
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Komputery")
                    .description("Sprzęt komputerowy")
                    .finalNode()
            )

            // Smartwatche i Opaski
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Smartwatche i Opaski")
                    .description("Akcesoria do monitorowania zdrowia")
                    .finalNode()
            )

            // Sprzęt AGD
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Sprzęt AGD")
                    .description("Sprzęt AGD do domu")
                    .finalNode()
            )

            // Sprzęt Audio
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Sprzęt Audio")
                    .description("Sprzęt audio do domu")
                    .finalNode()
            )

            // Sprzęt Video
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Sprzęt Video")
                    .description("Sprzęt video do domu")
                    .finalNode()
            )

            // Telefony
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Telefony")
                    .description("Sprzedaż telefonów")
                    .finalNode()
            )

            // TV
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("TV")
                    .description("Sprzedaż telewizorów")
                    .finalNode()
            )

            // Pozostała Elektronika
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Pozostała Elektronika")
                    .description("Inne produkty elektroniczne")
                    .finalNode()
            )
            .save()
    }

    @ChangeSet(order = "007", id = "insertFashionCategoryWithSubcategories", author = "filip-kaminski")
    fun insertFashionCategoryWithSubcategories(categoryRepository: CategoryRepository) {

        // Moda
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Moda")
            .description("Ubrania damskie, ubrania męskie, akcesoria, bielizna damska, bielizna męska, biżuteria, buty damskie, buty męskie, czapki i kapelusze, odzież ciążowa, do ślubu, torby i torebki, zegarki, pozostała moda")
            .topLevel()

            // Ubrania Damskie
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Ubrania Damskie")
                    .description("Moda damska")
                    .finalNode()
            )

            // Ubrania Męskie
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Ubrania Męskie")
                    .description("Moda męska")
                    .finalNode()
            )

            // Akcesoria
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Akcesoria")
                    .description("Akcesoria modowe")
                    .finalNode()
            )

            // Bielizna Damska
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Bielizna Damska")
                    .description("Bielizna dla kobiet")
                    .finalNode()
            )

            // Bielizna Męska
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Bielizna Męska")
                    .description("Bielizna dla mężczyzn")
                    .finalNode()
            )

            // Biżuteria
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Biżuteria")
                    .description("Biżuteria modowa")
                    .finalNode()
            )

            // Buty Damskie
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Buty Damskie")
                    .description("Obuwie damskie")
                    .finalNode()
            )

            // Buty Męskie
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Buty Męskie")
                    .description("Obuwie męskie")
                    .finalNode()
            )

            // Czapki i Kapelusze
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Czapki i Kapelusze")
                    .description("Czapki, kapelusze i nakrycia głowy")
                    .finalNode()
            )

            // Odzież Ciążowa
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Odzież Ciążowa")
                    .description("Moda ciążowa")
                    .finalNode()
            )

            // Do Ślubu
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Do Ślubu")
                    .description("Moda ślubna")
                    .finalNode()
            )

            // Torby i Torebki
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Torby i Torebki")
                    .description("Torby i torebki modowe")
                    .finalNode()
            )

            // Zegarki
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Zegarki")
                    .description("Zegarki modowe")
                    .finalNode()
            )

            // Pozostała Moda
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Pozostała Moda")
                    .description("Inne produkty modowe")
                    .finalNode()
            )
            .save()
    }

    @ChangeSet(order = "008", id = "insertAgricultureCategoryWithSubcategories", author = "filip-kaminski")
    fun insertAgricultureCategoryWithSubcategories(categoryRepository: CategoryRepository) {

        // Rolnictwo
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Rolnictwo")
            .description("Ciągniki, maszyny rolnicze, przyczepy, części do maszyn rolniczych, nawozy, opony rolnicze, produkty rolne, giełda zwierząt, ryneczek, środki ochrony roślin, worki, zbiorniki, pozostałe rolnicze")
            .topLevel()

            // Ciągniki
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Ciągniki")
                    .description("Sprzedaż ciągników rolniczych")
                    .finalNode()
            )

            // Maszyny Rolnicze
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Maszyny Rolnicze")
                    .description("Sprzedaż maszyn rolniczych")
                    .finalNode()
            )

            // Przyczepy
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Przyczepy")
                    .description("Sprzedaż przyczep rolniczych")
                    .finalNode()
            )

            // Części do Maszyn Rolniczych
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Części do Maszyn Rolniczych")
                    .description("Części zamienne do maszyn rolniczych")
                    .finalNode()
            )

            // Nawozy
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Nawozy")
                    .description("Sprzedaż nawozów")
                    .finalNode()
            )

            // Opony Rolnicze
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Opony Rolnicze")
                    .description("Sprzedaż opon rolniczych")
                    .finalNode()
            )

            // Produkty Rolne
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Produkty Rolne")
                    .description("Sprzedaż produktów rolnych")
                    .finalNode()
            )

            // Giełda Zwierząt
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Giełda Zwierząt")
                    .description("Handel zwierzętami")
                    .finalNode()
            )

            // Ryneczek
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Ryneczek")
                    .description("Ryneczek rolniczy")
                    .finalNode()
            )

            // Środki Ochrony Roślin
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Środki Ochrony Roślin")
                    .description("Środki do ochrony roślin")
                    .finalNode()
            )

            // Worki
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Worki")
                    .description("Worki na zboże i paszę")
                    .finalNode()
            )

            // Zbiorniki
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Zbiorniki")
                    .description("Zbiorniki na płyny rolnicze")
                    .finalNode()
            )

            // Pozostałe Rolnicze
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Pozostałe Rolnicze")
                    .description("Inne produkty i usługi rolnicze")
                    .finalNode()
            )
            .save()
    }

    @ChangeSet(order = "009", id = "insertAnimalsCategoryWithSubcategories", author = "filip-kaminski")
    fun insertAnimalsCategoryWithSubcategories(categoryRepository: CategoryRepository) {

        // Zwierzęta
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Zwierzęta")
            .description("Akcesoria dla zwierząt, karma i przysmaki, akwarystyka, psy, koty, ptaki, gryzonie i króliki, konie, pozostałe zwierzęta, terrarystyka, zaginione i znalezione")
            .topLevel()

            // Akcesoria dla Zwierząt
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Akcesoria dla Zwierząt")
                    .description("Akcesoria i artykuły dla zwierząt")
                    .finalNode()
            )

            // Karma i Przysmaki
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Karma i Przysmaki")
                    .description("Karma i przysmaki dla zwierząt")
                    .finalNode()
            )

            // Akwarystyka
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Akwarystyka")
                    .description("Sprzęt i akcesoria do akwarium")
                    .finalNode()
            )

            // Psy
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Psy")
                    .description("Psy na sprzedaż i akcesoria dla psów")
                    .finalNode()
            )

            // Koty
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Koty")
                    .description("Koty na sprzedaż i akcesoria dla kotów")
                    .finalNode()
            )

            // Ptaki
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Ptaki")
                    .description("Ptaki na sprzedaż i akcesoria dla ptaków")
                    .finalNode()
            )

            // Gryzonie i Króliki
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Gryzonie i Króliki")
                    .description("Gryzonie i króliki na sprzedaż i akcesoria")
                    .finalNode()
            )

            // Konie
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Konie")
                    .description("Konie na sprzedaż i akcesoria dla koni")
                    .finalNode()
            )

            // Pozostałe Zwierzęta
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Pozostałe Zwierzęta")
                    .description("Inne zwierzęta na sprzedaż i akcesoria")
                    .finalNode()
            )

            // Terrarystyka
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Terrarystyka")
                    .description("Sprzęt i akcesoria terrarystyczne")
                    .finalNode()
            )

            // Zaginione i Znalezione
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Zaginione i Znalezione")
                    .description("Zaginione i znalezione zwierzęta")
                    .finalNode()
            )
            .save()
    }

    @ChangeSet(order = "010", id = "insertForChildrenCategoryWithSubcategories", author = "filip-kaminski")
    fun insertForChildrenCategoryWithSubcategories(categoryRepository: CategoryRepository) {

        // Dla Dzieci
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Dla Dzieci")
            .description("Akcesoria dla niemowląt, artykuły szkolne, buciki, foteliki - nosidełka, meble dla dzieci, odzież niemowlęca, ubranka dla chłopców, ubranka dla dziewczynek, wózki dziecięce, zabawki, pozostałe dla dzieci")
            .topLevel()

            // Akcesoria dla Niemowląt
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Akcesoria dla Niemowląt")
                    .description("Akcesoria dla najmłodszych")
                    .finalNode()
            )

            // Artykuły Szkolne
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Artykuły Szkolne")
                    .description("Artykuły potrzebne w szkole")
                    .finalNode()
            )

            // Buciki
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Buciki")
                    .description("Obuwie dla dzieci")
                    .finalNode()
            )

            // Foteliki - Nosidełka
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Foteliki - Nosidełka")
                    .description("Foteliki samochodowe i nosidełka dla dzieci")
                    .finalNode()
            )

            // Meble dla Dzieci
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Meble dla Dzieci")
                    .description("Meble dziecięce")
                    .finalNode()
            )

            // Odzież Niemowlęca
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Odzież Niemowlęca")
                    .description("Odzież dla niemowląt")
                    .finalNode()
            )

            // Ubranka dla Chłopców
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Ubranka dla Chłopców")
                    .description("Ubranka dla chłopców")
                    .finalNode()
            )

            // Ubranka dla Dziewczynek
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Ubranka dla Dziewczynek")
                    .description("Ubranka dla dziewczynek")
                    .finalNode()
            )

            // Wózki Dziecięce
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Wózki Dziecięce")
                    .description("Wózki dla dzieci")
                    .finalNode()
            )

            // Zabawki
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Zabawki")
                    .description("Zabawki dla dzieci")
                    .finalNode()
            )

            // Pozostałe dla Dzieci
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Pozostałe dla Dzieci")
                    .description("Inne produkty dla dzieci")
                    .finalNode()
            )
            .save()
    }

    @ChangeSet(order = "011", id = "insertSportAndHobbyCategoryWithSubcategories", author = "filip-kaminski")
    fun insertSportAndHobbyCategoryWithSubcategories(categoryRepository: CategoryRepository) {

        // Sport i Hobby
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Sport i Hobby")
            .description("Akcesoria jeździeckie, bilety, fitness, gry planszowe, militaria, pojazdy elektryczne, rowery, skating, społeczność, sporty wodne, sporty zimowe, turystyka, wędkarstwo, pozostałe sport i hobby, żeglarstwo")
            .topLevel()

            // Akcesoria Jeździeckie
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Akcesoria Jeździeckie")
                    .description("Akcesoria do jazdy konnej")
                    .finalNode()
            )

            // Bilety
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Bilety")
                    .description("Bilety na różne wydarzenia")
                    .finalNode()
            )

            // Fitness
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Fitness")
                    .description("Sprzęt i akcesoria do fitnessu")
                    .finalNode()
            )

            // Gry Planszowe
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Gry Planszowe")
                    .description("Gry planszowe i karciane")
                    .finalNode()
            )

            // Militaria
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Militaria")
                    .description("Sprzęt militarny i kolekcjonerski")
                    .finalNode()
            )

            // Pojazdy Elektryczne
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Pojazdy Elektryczne")
                    .description("Hulajnogi, rowery elektryczne, itp.")
                    .finalNode()
            )

            // Rowery
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Rowery")
                    .description("Rowery i akcesoria rowerowe")
                    .finalNode()
            )

            // Skating
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Skating")
                    .description("Rolki, deskorolki, hulajnogi")
                    .finalNode()
            )

            // Społeczność
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Społeczność")
                    .description("Zbiórki, wydarzenia społecznościowe")
                    .finalNode()
            )

            // Sporty Wodne
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Sporty Wodne")
                    .description("Sprzęt do sportów wodnych")
                    .finalNode()
            )

            // Sporty Zimowe
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Sporty Zimowe")
                    .description("Sprzęt do sportów zimowych")
                    .finalNode()
            )

            // Turystyka
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Turystyka")
                    .description("Sprzęt turystyczny, wycieczki")
                    .finalNode()
            )

            // Wędkarstwo
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Wędkarstwo")
                    .description("Sprzęt do wędkarstwa")
                    .finalNode()
            )

            // Pozostałe Sport i Hobby
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Pozostałe Sport i Hobby")
                    .description("Inne produkty i usługi związane ze sportem i hobby")
                    .finalNode()
            )

            // Żeglarstwo
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Żeglarstwo")
                    .description("Sprzęt i akcesoria żeglarskie")
                    .finalNode()
            )
            .save()
    }

    @ChangeSet(order = "012", id = "insertMusicAndEducationCategoryWithSubcategories", author = "filip-kaminski")
    fun insertMusicAndEducationCategoryWithSubcategories(categoryRepository: CategoryRepository) {

        // Muzyka i Edukacja
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Muzyka i Edukacja")
            .description("Książki, muzyka, filmy, instrumenty, materiały językowe, pozostała muzyka i edukacja")
            .topLevel()

            // Książki
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Książki")
                    .description("Nowe i używane książki")
                    .finalNode()
            )

            // Muzyka
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Muzyka")
                    .description("Płyty CD, winyle, koncerty")
                    .finalNode()
            )

            // Filmy
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Filmy")
                    .description("Filmy DVD, Blu-ray, streaming")
                    .finalNode()
            )

            // Instrumenty
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Instrumenty")
                    .description("Sprzęt muzyczny i instrumenty")
                    .finalNode()
            )

            // Materiały Językowe
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Materiały Językowe")
                    .description("Kursy językowe, materiały do nauki")
                    .finalNode()
            )

            // Pozostała Muzyka i Edukacja
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Pozostała Muzyka i Edukacja")
                    .description("Inne produkty związane z muzyką i edukacją")
                    .finalNode()
            )
            .save()
    }

    @ChangeSet(order = "013", id = "insertHealthAndBeautyCategoryWithSubcategories", author = "filip-kaminski")
    fun insertHealthAndBeautyCategoryWithSubcategories(categoryRepository: CategoryRepository) {

        // Zdrowie i Uroda
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Zdrowie i Uroda")
            .description("Ciało, twarz, paznokcie, włosy, makijaż, perfumy, higiena jamy ustnej, korekcja wzroku, produkty CBD, sprzęt rehabilitacyjny i ortopedyczny, transport i poruszanie, urządzenia do masażu, urządzenia medyczne, środki ochrony, witaminy i suplementy, pozostałe")
            .topLevel()

            // Ciało
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Ciało")
                    .description("Kosmetyki do pielęgnacji ciała")
                    .finalNode()
            )

            // Twarz
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Twarz")
                    .description("Kosmetyki do pielęgnacji twarzy")
                    .finalNode()
            )

            // Paznokcie
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Paznokcie")
                    .description("Kosmetyki do paznokci")
                    .finalNode()
            )

            // Włosy
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Włosy")
                    .description("Kosmetyki do pielęgnacji włosów")
                    .finalNode()
            )

            // Makijaż
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Makijaż")
                    .description("Kosmetyki do makijażu")
                    .finalNode()
            )

            // Perfumy
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Perfumy")
                    .description("Zestawy perfum, perfumy damskie i męskie")
                    .finalNode()
            )

            // Higiena Jamy Ustnej
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Higiena Jamy Ustnej")
                    .description("Pasty, szczoteczki, nici dentystyczne")
                    .finalNode()
            )

            // Korekcja Wzroku
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Korekcja Wzroku")
                    .description("Okulary, soczewki, operacje wzroku")
                    .finalNode()
            )

            // Produkty CBD
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Produkty CBD")
                    .description("Produkty z kannabidiolem")
                    .finalNode()
            )

            // Sprzęt Rehabilitacyjny i Ortopedyczny
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Sprzęt Rehabilitacyjny i Ortopedyczny")
                    .description("Kule, wózki inwalidzkie, ortezy")
                    .finalNode()
            )

            // Transport i Poruszanie
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Transport i Poruszanie")
                    .description("Hulajnogi, wózki, akcesoria dla osób niepełnosprawnych")
                    .finalNode()
            )

            // Urządzenia do Masażu
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Urządzenia do Masażu")
                    .description("Masażery, maty do masażu")
                    .finalNode()
            )

            // Urządzenia Medyczne
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Urządzenia Medyczne")
                    .description("Sprzęt medyczny")
                    .finalNode()
            )

            // Środki Ochrony
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Środki Ochrony")
                    .description("Maski, rękawice, środki dezynfekcyjne")
                    .finalNode()
            )

            // Witaminy i Suplementy
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Witaminy i Suplementy")
                    .description("Preparaty witaminowe i suplementy diety")
                    .finalNode()
            )

            // Pozostałe
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Pozostałe")
                    .description("Pozostałe produkty związane z zdrowiem i urodą")
                    .finalNode()
            )
            .save()
    }

    @ChangeSet(order = "014", id = "insertServicesCategoryWithSubcategories", author = "filip-kaminski")
    fun insertServicesCategoryWithSubcategories(categoryRepository: CategoryRepository) {

        // Usługi
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Usługi")
            .description("Budowa i remont, obsługa imprez, sprzątanie, tłumaczenia, usługi finansowe, usługi informatyczne, usługi motoryzacyjne, usługi ogrodnicze, usługi reklamowe, usługi transportowe, korepetycje, kursy i warsztaty, usługi dla zwierząt, współpraca biznesowa, usługi ślubne, uroda i zdrowie, usługi meblarskie, serwis i naprawa rtv/agd/elektroniki, pozostałe usługi")
            .topLevel()

            // Budowa i Remont
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Budowa i Remont")
                    .description("Usługi budowlane, remontowe")
                    .finalNode()
            )

            // Obsługa Imprez
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Obsługa Imprez")
                    .description("Organizacja imprez, obsługa techniczna")
                    .finalNode()
            )

            // Sprzątanie
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Sprzątanie")
                    .description("Usługi sprzątania mieszkań, biur")
                    .finalNode()
            )

            // Tłumaczenia
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Tłumaczenia")
                    .description("Usługi tłumaczeniowe")
                    .finalNode()
            )

            // Usługi Finansowe
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Usługi Finansowe")
                    .description("Usługi księgowe, doradztwo finansowe")
                    .finalNode()
            )

            // Usługi Informatyczne
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Usługi Informatyczne")
                    .description("Programowanie, wsparcie IT")
                    .finalNode()
            )

            // Usługi Motoryzacyjne
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Usługi Motoryzacyjne")
                    .description("Serwis samochodowy, wulkanizacja")
                    .finalNode()
            )

            // Usługi Ogrodnicze
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Usługi Ogrodnicze")
                    .description("Projektowanie ogrodów, pielęgnacja")
                    .finalNode()
            )

            // Usługi Reklamowe
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Usługi Reklamowe")
                    .description("Reklama, marketing")
                    .finalNode()
            )

            // Usługi Transportowe
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Usługi Transportowe")
                    .description("Transport osób, przeprowadzki")
                    .finalNode()
            )

            // Korepetycje
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Korepetycje")
                    .description("Nauka, korepetycje")
                    .finalNode()
            )

            // Kursy i Warsztaty
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Kursy i Warsztaty")
                    .description("Szkolenia, kursy różnego rodzaju")
                    .finalNode()
            )

            // Usługi dla Zwierząt
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Usługi dla Zwierząt")
                    .description("Opieka, szkolenie, pielęgnacja zwierząt")
                    .finalNode()
            )

            // Współpraca Biznesowa
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Współpraca Biznesowa")
                    .description("Oferty współpracy, poszukiwanie partnerów biznesowych")
                    .finalNode()
            )

            // Usługi Ślubne
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Usługi Ślubne")
                    .description("Organizacja ślubu, usługi dla nowożeńców")
                    .finalNode()
            )

            // Uroda i Zdrowie
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Uroda i Zdrowie")
                    .description("Salony urody, zabiegi kosmetyczne, spa")
                    .finalNode()
            )

            // Usługi Meblarskie
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Usługi Meblarskie")
                    .description("Meble na zamówienie, naprawa mebli")
                    .finalNode()
            )

            // Serwis i Naprawa RTV/AGD/Elektroniki
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Serwis i Naprawa RTV/AGD/Elektroniki")
                    .description("Naprawa sprzętu elektronicznego")
                    .finalNode()
            )

            // Pozostałe Usługi
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Pozostałe Usługi")
                    .description("Inne usługi")
                    .finalNode()
            )
            .save()
    }

    @ChangeSet(order = "015", id = "insertAccommodationCategoryWithSubcategories", author = "filip-kaminski")
    fun insertAccommodationCategoryWithSubcategories(categoryRepository: CategoryRepository) {

        // Noclegi
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Noclegi")
            .description("Noclegi za granicą, noclegi Polska, kwatery pracownicze")
            .topLevel()

            // Noclegi za Granicą
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Noclegi za Granicą")
                    .description("Hotele, pensjonaty za granicą")
                    .finalNode()
            )

            // Noclegi Polska
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Noclegi Polska")
                    .description("Hotele, pensjonaty w Polsce")
                    .finalNode()
            )

            // Kwatery Pracownicze
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Kwatery Pracownicze")
                    .description("Noclegi dla pracowników")
                    .finalNode()
            )
            .save()
    }

    @ChangeSet(order = "016", id = "insertRentalCategoryWithSubcategories", author = "filip-kaminski")
    fun insertRentalCategoryWithSubcategories(categoryRepository: CategoryRepository) {

        // Wypożyczalnia
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Wypożyczalnia")
            .description("Imprezy i wydarzenia, samochody i pojazdy, pozostałe wypożyczalnia, sprzęt medyczny i rehabilitacyjny, urządzenia/maszyny i narzędzia, jachty/łodzie i sporty wodne")
            .topLevel()

            // Imprezy i Wydarzenia
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Imprezy i Wydarzenia")
                    .description("Wynajem sprzętu eventowego")
                    .finalNode()
            )

            // Samochody i Pojazdy
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Samochody i Pojazdy")
                    .description("Wypożyczalnia samochodów, motocykli, rowerów")
                    .finalNode()
            )

            // Pozostałe Wypożyczalnia
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Pozostałe Wypożyczalnia")
                    .description("Inne usługi wypożyczalni")
                    .finalNode()
            )

            // Sprzęt Medyczny i Rehabilitacyjny
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Sprzęt Medyczny i Rehabilitacyjny")
                    .description("Wynajem sprzętu medycznego")
                    .finalNode()
            )

            // Urządzenia/Maszyny i Narzędzia
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Urządzenia/Maszyny i Narzędzia")
                    .description("Wynajem urządzeń, maszyn, narzędzi")
                    .finalNode()
            )

            // Jachty/Łodzie i Sporty Wodne
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Jachty/Łodzie i Sporty Wodne")
                    .description("Wynajem jachtów, łodzi, sprzętu do sportów wodnych")
                    .finalNode()
            )
            .save()
    }

    @ChangeSet(order = "017", id = "insertForCompaniesCategoryWithSubcategories", author = "filip-kaminski")
    fun insertForCompaniesCategoryWithSubcategories(categoryRepository: CategoryRepository) {

        // Dla Firm
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Dla Firm")
            .description("Akcesoria sklepowe, maszyny i urządzenia dla firm, meble do biur i sklepów, odzież robocza, sprzedam firmę, pozostałe")
            .topLevel()

            // Akcesoria Sklepowe
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Akcesoria Sklepowe")
                    .description("Wieszaki, gabloty, systemy kasowe")
                    .finalNode()
            )

            // Maszyny i Urządzenia dla Firm
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Maszyny i Urządzenia dla Firm")
                    .description("Sprzęt i maszyny przemysłowe")
                    .finalNode()
            )

            // Meble do Biur i Sklepów
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Meble do Biur i Sklepów")
                    .description("Biurka, regały, lady sklepowe")
                    .finalNode()
            )

            // Odzież Robocza
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Odzież Robocza")
                    .description("Odzież ochronna, kombinezony")
                    .finalNode()
            )

            // Sprzedam Firmę
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Sprzedam Firmę")
                    .description("Oferty sprzedaży firm")
                    .finalNode()
            )

            // Pozostałe
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Pozostałe")
                    .description("Pozostałe produkty i usługi dla firm")
                    .finalNode()
            )
            .save()
    }

    @ChangeSet(order = "018", id = "insertForFreeCategory", author = "filip-kaminski")
    fun insertForFreeCategory(categoryRepository: CategoryRepository) {

        // Oddam za darmo
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Oddam za Darmo")
            .description("Darmowe rzeczy, przedmioty do oddania")
            .topLevel()
            .subCategory(
                CategoryBuilder(categoryRepository = categoryRepository)
                    .name("Zwierzęta za darmo")
                    .description("Zwierzęta do adopcji")
                    .subCategory(
                        CategoryBuilder(categoryRepository = categoryRepository)
                            .name("Małe zwierzęta domowe za darmo")
                            .description("Małe zwierzęta domowe do adopcji")
                            .subCategory(
                                CategoryBuilder(categoryRepository = categoryRepository)
                                    .name("Psy i koty za darmo")
                                    .description("Małe psy i koty do adopcji")
                                    .subCategory(
                                        CategoryBuilder(categoryRepository = categoryRepository)
                                            .name("Psy do adopcji za darmo")
                                            .description("Małe psy do adopcji")
                                            .finalNode()
                                    )
                                    .subCategory(
                                        CategoryBuilder(categoryRepository = categoryRepository)
                                            .name("Koty do adopcji za darmo")
                                            .description("Małe koty do adopcji")
                                            .finalNode()
                                    )
                            )
                    )
            )
            .save()
    }

    @ChangeSet(order = "019", id = "insertFuelCategory", author = "filip-kaminski")
    fun insertFuelCategory(categoryRepository: CategoryRepository) {

        // Opał
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Opał")
            .description("Drewno opałowe, węgiel, brykiety")
            .finalNode()
            .save()
    }

    @ChangeSet(order = "020", id = "insertTreasuresFromThePrlCategory", author = "filip-kaminski")
    fun insertTreasuresFromThePrlCategory(categoryRepository: CategoryRepository) {

        // Skarby z PRL
        CategoryBuilder(categoryRepository = categoryRepository)
            .name("Skarby z PRL")
            .description("Kolekcje związane z PRL, memorabilia")
            .finalNode()
            .save()
    }

}
