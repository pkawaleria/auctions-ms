#!/bin/bash

licznik=1

for plik in *.jpg; do
    if [ -f "${plik}" ]; then  # Sprawdź, czy to plik JPEG
        rozszerzenie="${plik##*.}"  # Pobierz rozszerzenie pliku
        nowa_nazwa="${licznik}.${rozszerzenie}"
        mv "${plik}" "${nowa_nazwa}"
        licznik=$((licznik + 1))
    fi
done
